package com.example.socialbook.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.socialbook.Model.Friends;
import com.example.socialbook.R;
import com.example.socialbook.ViewHolder.FriendsViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MessagesActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView messagesList;
    private DatabaseReference friendsRef, userRef,messageRef;
    private String currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        mToolbar=findViewById(R.id.messages_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Messages");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        messagesList = findViewById(R.id.messages_list);
        messagesList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        messagesList.setLayoutManager(linearLayoutManager);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);
        messageRef = FirebaseDatabase.getInstance().getReference().child("Message").child(currentUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        DisplayAllFriendsMessages();

    }

    private void DisplayAllFriendsMessages() {

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(friendsRef, Friends.class).build();
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {
                        final String usersIDs = getRef(position).getKey();
                        holder.profStatus.setText("friends since: " + model.getDate());

                        messageRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.hasChild(usersIDs)){
                                    userRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                final String username=snapshot.child("fullname").getValue().toString();
                                                final String type;
                                                if (snapshot.hasChild("userState")){
                                                    type=snapshot.child("userState").child("type").getValue().toString();
                                                    if (type.equals("online")){
                                                        holder.userState.setVisibility(View.VISIBLE);
                                                    }
                                                    else {
                                                        holder.userState.setVisibility(View.INVISIBLE);
                                                    }
                                                }
                                                holder.profFullName.setText(username);

                                                Picasso.get().load(snapshot.child("profileimages").getValue().toString()).placeholder(R.drawable.profile).into(holder.profImage);

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Intent chatIntent=new Intent(MessagesActivity.this,ChatActivity.class);
                                                        chatIntent.putExtra("visit_user_id",usersIDs);
                                                        chatIntent.putExtra("userName",username);
                                                        startActivity(chatIntent);
                                                    }
                                                });

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                                else {
                                    holder.itemView.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_user_display_layout, parent, false);
                        FriendsViewHolder holder = new FriendsViewHolder(view);
                        return holder;
                    }
                };

        messagesList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
}