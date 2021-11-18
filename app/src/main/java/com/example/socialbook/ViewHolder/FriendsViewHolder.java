package com.example.socialbook.ViewHolder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialbook.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsViewHolder extends RecyclerView.ViewHolder {
    public CircleImageView profImage,userState;
    public TextView profFullName,profStatus;

    public FriendsViewHolder(@NonNull View itemView) {
        super(itemView);

        profImage=itemView.findViewById(R.id.all_user_display_image);
        profFullName=itemView.findViewById(R.id.all_user_display_fullname);
        profStatus=itemView.findViewById(R.id.all_user_display_status);
        userState=itemView.findViewById(R.id.online_icon);
    }
}
