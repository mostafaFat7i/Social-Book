package com.example.socialbook.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialbook.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsViewHolder extends RecyclerView.ViewHolder {

    public CircleImageView profImage;
    public TextView userName,commentDate,commentTime,commentContent;

    public CommentsViewHolder(@NonNull View itemView) {
        super(itemView);

        profImage=itemView.findViewById(R.id.comment_profile_imagge);
        userName=itemView.findViewById(R.id.comment_username);
        commentDate=itemView.findViewById(R.id.comment_date);
        commentTime=itemView.findViewById(R.id.comment_time);
        commentContent=itemView.findViewById(R.id.comment_content);
    }
}
