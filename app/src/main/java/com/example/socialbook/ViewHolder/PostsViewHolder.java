package com.example.socialbook.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialbook.Interface.ItemClickListener;
import com.example.socialbook.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ItemClickListener itemClickListener;
    public CircleImageView postProfileImage;
    public TextView userName,updatePost,postDate,postTime,postDiscribtion,numOfLikes,numOfComments;
    public ImageView postImage,likePostBtn,commentPostBtn;
    int likesCounter,commentsCounter;
    String currentUserId;
    DatabaseReference likesRef,postRef;
    public PostsViewHolder(@NonNull View itemView) {
        super(itemView);

        postProfileImage=itemView.findViewById(R.id.post_profile_imagge);
        userName=itemView.findViewById(R.id.post_username);
        updatePost=itemView.findViewById(R.id.text);
        postDate=itemView.findViewById(R.id.post_date);
        postTime=itemView.findViewById(R.id.post_time);
        postDiscribtion=itemView.findViewById(R.id.post_description);
        postImage=itemView.findViewById(R.id.post_image);
        numOfLikes=itemView.findViewById(R.id.number_of_likes);
        numOfComments=itemView.findViewById(R.id.number_of_comments);
        likePostBtn=itemView.findViewById(R.id.like_btn);
        commentPostBtn=itemView.findViewById(R.id.comment_btn);

        likesRef= FirebaseDatabase.getInstance().getReference().child("Likes");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        currentUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void SetCommentsStatus(final String postKey) {

        postRef.child(postKey).child("comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentsCounter = (int) snapshot.getChildrenCount();
                numOfComments.setText(commentsCounter + " Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void SetLikeButtonStatus(final String postKey) {


        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child(postKey).hasChild(currentUserId)){
                    likesCounter=(int)snapshot.child(postKey).getChildrenCount();
                    likePostBtn.setImageResource(R.drawable.like);
                    numOfLikes.setText(likesCounter+" Likes");
                }
                else {
                    likesCounter=(int)snapshot.child(postKey).getChildrenCount();
                    likePostBtn.setImageResource(R.drawable.dislike);
                    numOfLikes.setText(likesCounter+" Likes");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;

    }


}
