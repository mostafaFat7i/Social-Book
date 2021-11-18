package com.example.socialbook.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.socialbook.Model.FindFriends;
import com.example.socialbook.Model.Friends;
import com.example.socialbook.Model.Posts;
import com.example.socialbook.R;
import com.example.socialbook.ViewHolder.FindFriendsViewHolder;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView friendsList;
    private DatabaseReference friendsRef, userRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        friendsList = findViewById(R.id.friends_list);
        friendsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        friendsList.setLayoutManager(linearLayoutManager);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        DisplayAllFriends();
    }

    public void UpdateUserStatus(String state){

        String saveCurrentDate, saveCurrentTime;

        Calendar calendarForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calendarForDate.getTime());

        Calendar calendarForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm a");
        saveCurrentTime = currentTime.format(calendarForTime.getTime());

        Map currentStateMap=new HashMap();

        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);

        userRef.child(currentUserId).child("userState").updateChildren(currentStateMap);

    }

    @Override
    protected void onStart() {
        super.onStart();
        UpdateUserStatus("online");
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        UpdateUserStatus("offline");
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        UpdateUserStatus("offline");
//    }

    private void DisplayAllFriends() {

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(friendsRef, Friends.class).build();
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {
                        final String usersIDs = getRef(position).getKey();
                        holder.profStatus.setText("friends since: " + model.getDate());

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
                                            CharSequence itemOption[] = new CharSequence[]
                                                    {
                                                            username+"'s Profile",
                                                            "Send Message"
                                                    };
                                            AlertDialog.Builder builder=new AlertDialog.Builder(FriendsActivity.this);
                                            builder.setTitle("Select Options");

                                            builder.setItems(itemOption, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which==0){
                                                        Intent profIntent=new Intent(FriendsActivity.this,PersonProfileActivity.class);
                                                        profIntent.putExtra("visit_user_id",usersIDs);
                                                        startActivity(profIntent);
                                                    }
                                                    if (which==1){
                                                        Intent chatIntent=new Intent(FriendsActivity.this,ChatActivity.class);
                                                        chatIntent.putExtra("visit_user_id",usersIDs);
                                                        chatIntent.putExtra("userName",username);
                                                        startActivity(chatIntent);
                                                    }
                                                }
                                            });
                                            builder.show();
                                        }
                                    });

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

        friendsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
}