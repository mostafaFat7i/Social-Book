package com.example.socialbook.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.socialbook.MainActivity;
import com.example.socialbook.Model.FindFriends;
import com.example.socialbook.Model.Posts;
import com.example.socialbook.R;
import com.example.socialbook.ViewHolder.FindFriendsViewHolder;
import com.example.socialbook.ViewHolder.PostsViewHolder;
import com.example.socialbook.databinding.ActivityFindFriendsBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView recyclerView;
    private DatabaseReference userRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityFindFriendsBinding binding= DataBindingUtil.setContentView(this,R.layout.activity_find_friends);
        mToolbar=findViewById(R.id.find_friends_appbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        recyclerView=binding.accountsSearchList;
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        binding.accountsSearchList.setHasFixedSize(true);
        binding.accountsSearchList.setLayoutManager(new LinearLayoutManager(this));

        binding.searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String accountName=binding.searchInput.getText().toString().trim();

                SearchAboutFriends(accountName);
            }
        });
    }



    private void SearchAboutFriends(String accountName) {

        Query searchPeopleFriendsQuery=userRef.orderByChild("fullname")
                .startAt(accountName).endAt(accountName+"\uf8ff");

        FirebaseRecyclerOptions<FindFriends> options = new FirebaseRecyclerOptions.Builder<FindFriends>()
                .setQuery(searchPeopleFriendsQuery,FindFriends.class).build();
        FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull FindFriends model) {

                holder.profFullName.setText(model.getFullname());
                holder.profStatus.setText(model.getStatus());
                Picasso.get().load(model.getProfileimages()).placeholder(R.drawable.profile).into(holder.profImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visitUserId=getRef(position).getKey();

                        Intent intent=new Intent(FindFriendsActivity.this,PersonProfileActivity.class);
                        intent.putExtra("visit_user_id",visitUserId);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_user_display_layout, parent, false);
                FindFriendsViewHolder holder = new FindFriendsViewHolder(view);
                return holder;
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
}