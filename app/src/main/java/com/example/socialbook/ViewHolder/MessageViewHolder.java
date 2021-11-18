package com.example.socialbook.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialbook.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    public CircleImageView profImage;
    public TextView senderMessage,receiverMessage,senderImageDescriptionMessage,receiverImageDescriptionMessage;
    public ImageView senderImageMessage,receiverImageMessage;
    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);

        profImage=itemView.findViewById(R.id.message_profile_image);
        senderMessage=itemView.findViewById(R.id.sender_text_message);
        receiverMessage=itemView.findViewById(R.id.receiver_text_message);
        senderImageMessage=itemView.findViewById(R.id.sender_image_message);
        receiverImageMessage=itemView.findViewById(R.id.receiver_image_message);
        senderImageDescriptionMessage=itemView.findViewById(R.id.sender_image_descripe_message);
        receiverImageDescriptionMessage=itemView.findViewById(R.id.receiver_image_descripe_message);


    }
}
