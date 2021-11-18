package com.example.socialbook.Adapter;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialbook.Model.Messages;
import com.example.socialbook.R;
import com.example.socialbook.ViewHolder.MessageViewHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private List<Messages> userMessagesList;
    private DatabaseReference userRef;

    public MessagesAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_users_layout,parent,false);

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        String senderMessageID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        Messages messages=userMessagesList.get(position);

        String fromUserID=messages.getFrom();
        String fromMessageType=messages.getType();

        userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Picasso.get().load(snapshot.child("profileimages").getValue().toString()).placeholder(R.drawable.profile).into(holder.profImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (fromMessageType.equals("text")){
            holder.receiverMessage.setVisibility(View.INVISIBLE);
            holder.profImage.setVisibility(View.INVISIBLE);

            if (fromUserID.equals(senderMessageID)){
                holder.senderMessage.setBackgroundResource(R.drawable.sender_message_text_background);
                holder.senderMessage.setTextColor(Color.WHITE);
                holder.senderMessage.setGravity(Gravity.LEFT);
                holder.senderMessage.setText(messages.getMessage());
            }
            else {
                holder.senderMessage.setVisibility(View.INVISIBLE);
                holder.receiverMessage.setVisibility(View.VISIBLE);
                holder.profImage.setVisibility(View.VISIBLE);


                holder.receiverMessage.setBackgroundResource(R.drawable.receiver_message_text_background);
                holder.receiverMessage.setTextColor(Color.WHITE);
                holder.receiverMessage.setGravity(Gravity.LEFT);
                holder.receiverMessage.setText(messages.getMessage());

            }
        }
        else if (fromMessageType.equals("image")){
            holder.receiverMessage.setVisibility(View.GONE);
            holder.senderMessage.setVisibility(View.GONE);
            holder.profImage.setVisibility(View.GONE);
            if (fromUserID.equals(senderMessageID)){
                holder.senderImageMessage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.select_image).into(holder.senderImageMessage);
            }
            else {
                holder.receiverImageMessage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.select_image).into(holder.receiverImageMessage);
                holder.profImage.setVisibility(View.VISIBLE);

            }
        }
        else {
            holder.receiverMessage.setVisibility(View.GONE);
            holder.senderMessage.setVisibility(View.GONE);
            holder.profImage.setVisibility(View.GONE);
            if (fromUserID.equals(senderMessageID)){
                holder.senderImageMessage.setVisibility(View.VISIBLE);
                holder.senderImageDescriptionMessage.setVisibility(View.VISIBLE);
                holder.senderImageDescriptionMessage.setText(userMessagesList.get(position).getName());
                if (fromMessageType.equals("pdf")){
                    holder.senderImageMessage.setImageResource(R.drawable.pdf);
                }
                else {
                    holder.senderImageMessage.setImageResource(R.drawable.word);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }
            else {
                holder.receiverImageMessage.setVisibility(View.VISIBLE);
                holder.profImage.setVisibility(View.VISIBLE);
                holder.receiverImageDescriptionMessage.setVisibility(View.VISIBLE);
                holder.receiverImageDescriptionMessage.setText(userMessagesList.get(position).getName());
                if (fromMessageType.equals("pdf")){
                    holder.receiverImageMessage.setImageResource(R.drawable.pdf);
                }
                else {
                    holder.receiverImageMessage.setImageResource(R.drawable.word);
                }

            }
        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }
}
