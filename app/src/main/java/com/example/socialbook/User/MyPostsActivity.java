package com.example.socialbook.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.socialbook.MainActivity;
import com.example.socialbook.Model.Posts;
import com.example.socialbook.R;
import com.example.socialbook.ViewHolder.PostsViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myPosts;
    private DatabaseReference postsRef,userRef,likesRef;
    private String currentUserID;
    private Boolean likeChecker=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        postsRef= FirebaseDatabase.getInstance().getReference().child("Posts");
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        currentUserID= FirebaseAuth.getInstance().getCurrentUser().getUid();

        likesRef=FirebaseDatabase.getInstance().getReference().child("Likes");


        mToolbar=findViewById(R.id.my_posts_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");

        myPosts=findViewById(R.id.my_Posts_list);
        myPosts.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPosts.setLayoutManager(linearLayoutManager);

        DisplayMyAllPosts();
    }

    private void DisplayMyAllPosts() {

        Query sortPostsInDecindindOrder=postsRef.orderByChild("uid").startAt(currentUserID).endAt(currentUserID+"\uf8ff");

        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(sortPostsInDecindindOrder,Posts.class).build();
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model) {

                final String postKey=getRef(position).getKey();

                holder.userName.setText(model.getFullname());
                holder.postTime.setText(model.getTime());
                holder.postDate.setText(model.getDate()+"__");
                holder.postDiscribtion.setText(model.getDescription());
                final CircleImageView image=holder.postProfileImage;

                holder.SetLikeButtonStatus(postKey);
                holder.SetCommentsStatus(postKey);

                userRef.child(model.getUid()).child("profileimages").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String value=snapshot.getValue().toString();
                        Picasso.get().load(value).placeholder(R.drawable.profile).into(image);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                Picasso.get().load(model.getPostImage()).into(holder.postImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(MyPostsActivity.this, PostsOptionsActivity.class);
                        intent.putExtra("postkey",postKey);
                        startActivity(intent);
                    }
                });

                holder.commentPostBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent=new Intent(MyPostsActivity.this, CommentsActivity.class);
                        commentsIntent.putExtra("postkey",postKey);
                        startActivity(commentsIntent);
                    }
                });


                holder.likePostBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        likeChecker=true;
                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                if (likeChecker==true){
                                    if (snapshot.child(postKey).hasChild(currentUserID)){
                                        likesRef.child(postKey).child(currentUserID).removeValue();
                                        likeChecker=false;
                                    }
                                    else {
                                        likesRef.child(postKey).child(currentUserID).setValue(true);
                                        likeChecker=false;
                                    }
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
                PostsViewHolder holder = new PostsViewHolder(view);
                return holder;
            }
        };

        myPosts.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
}