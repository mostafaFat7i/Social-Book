package com.example.socialbook.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialbook.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsViewHolder extends RecyclerView.ViewHolder {

    public CircleImageView profImage;
    public TextView profFullName,profStatus;

    public FindFriendsViewHolder(@NonNull View itemView) {
        super(itemView);

        profImage=itemView.findViewById(R.id.all_user_display_image);
        profFullName=itemView.findViewById(R.id.all_user_display_fullname);
        profStatus=itemView.findViewById(R.id.all_user_display_status);


    }
}
