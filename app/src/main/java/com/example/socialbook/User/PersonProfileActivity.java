package com.example.socialbook.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.example.socialbook.R;
import com.example.socialbook.databinding.ActivityPersonProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PersonProfileActivity extends AppCompatActivity {

    private DatabaseReference friendRequestRef, userRef, friendsRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId, CURRENT_STATE, saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityPersonProfileBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_person_profile);

        CURRENT_STATE = "not_friends";
        mAuth = FirebaseAuth.getInstance();


        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();

        senderUserId = mAuth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequest");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");


        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.personProfileUsername.setText("@" + snapshot.child("username").getValue().toString());
                    binding.personProfileFullname.setText(snapshot.child("fullname").getValue().toString());
                    binding.personProfileStatus.setText(snapshot.child("status").getValue().toString());
                    binding.personProfileBirthday.setText(snapshot.child("dob").getValue().toString());
                    binding.personProfileCountry.setText(snapshot.child("country").getValue().toString());
                    binding.personProfileGender.setText(snapshot.child("gender").getValue().toString());
                    binding.personProfileRelationStatus.setText(snapshot.child("relationshipstatus").getValue().toString());

                    Picasso.get().load(snapshot.child("profileimages").getValue().toString()).into(binding.personProfileImage);

                    MaintanceOfButtons(binding);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.personDeclineFriendRequest.setVisibility(View.GONE);
        binding.personDeclineFriendRequest.setEnabled(false);

        if (!senderUserId.equals(receiverUserId)) {
            binding.personSendFriendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.personSendFriendRequest.setEnabled(false);
                    if (CURRENT_STATE.equals("not_friends")) {
                        SendFreiendRequest(binding);
                    }
                    if (CURRENT_STATE.equals("request_sent")) {
                        CancelFriendRequest(binding);
                    }
                    if (CURRENT_STATE.equals("request_received")) {
                        AcceptFriendRequest(binding);
                    }
                    if (CURRENT_STATE.equals("friends")) {
                        Unfriend(binding);
                    }

                }
            });
        } else {
            binding.personDeclineFriendRequest.setVisibility(View.GONE);
            binding.personSendFriendRequest.setVisibility(View.GONE);
        }
    }

    private void Unfriend(final ActivityPersonProfileBinding binding) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        friendsRef.child(senderUserId).child(receiverUserId)
                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    friendsRef.child(receiverUserId).child(senderUserId).removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        binding.personSendFriendRequest.setEnabled(true);
                                                        CURRENT_STATE = "not_friends";
                                                        binding.personSendFriendRequest.setText("Add");
                                                        binding.personDeclineFriendRequest.setVisibility(View.GONE);
                                                        binding.personDeclineFriendRequest.setEnabled(false);
                                                    }
                                                }
                                            });
                                }
                            }
                        });

                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorBlack));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorBlack));

            }
        });
        dialog.show();


    }

    private void AcceptFriendRequest(final ActivityPersonProfileBinding binding) {
        Calendar calendarForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calendarForDate.getTime());

        friendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                friendRequestRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            friendRequestRef.child(receiverUserId).child(senderUserId).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                binding.personSendFriendRequest.setEnabled(true);
                                                                                CURRENT_STATE = "friends";
                                                                                binding.personSendFriendRequest.setText("Unfriend");
                                                                                binding.personDeclineFriendRequest.setVisibility(View.GONE);
                                                                                binding.personDeclineFriendRequest.setEnabled(false);
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }
                });


    }

    private void CancelFriendRequest(final ActivityPersonProfileBinding binding) {
        friendRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    friendRequestRef.child(receiverUserId).child(senderUserId).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        binding.personSendFriendRequest.setEnabled(true);
                                        CURRENT_STATE = "not_friends";
                                        binding.personSendFriendRequest.setText("Add");
                                        binding.personDeclineFriendRequest.setVisibility(View.GONE);
                                        binding.personDeclineFriendRequest.setEnabled(false);
                                    }
                                }
                            });
                }
            }
        });
    }

    private void MaintanceOfButtons(final ActivityPersonProfileBinding binding) {
        friendRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(receiverUserId)) {
                    String requestType = snapshot.child(receiverUserId).child("request_type").getValue().toString();

                    if (requestType.equals("sent")) {
                        CURRENT_STATE = "request_sent";
                        binding.personSendFriendRequest.setText("Cancel Friend Request");
                        binding.personDeclineFriendRequest.setVisibility(View.GONE);
                        binding.personDeclineFriendRequest.setEnabled(false);
                    } else if (requestType.equals("received")) {
                        CURRENT_STATE = "request_received";
                        binding.personSendFriendRequest.setText("Accept");
                        binding.personDeclineFriendRequest.setVisibility(View.VISIBLE);
                        binding.personDeclineFriendRequest.setEnabled(true);

                        binding.personDeclineFriendRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelFriendRequest(binding);
                            }
                        });
                    }
                } else {
                    friendsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(receiverUserId)) {
                                CURRENT_STATE = "friends";
                                binding.personSendFriendRequest.setText("Unfriend");
                                binding.personDeclineFriendRequest.setVisibility(View.GONE);
                                binding.personDeclineFriendRequest.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendFreiendRequest(final ActivityPersonProfileBinding binding) {
        friendRequestRef.child(senderUserId).child(receiverUserId).child("request_type")
                .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    friendRequestRef.child(receiverUserId).child(senderUserId).child("request_type")
                            .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                binding.personSendFriendRequest.setEnabled(true);
                                CURRENT_STATE = "request_sent";
                                binding.personSendFriendRequest.setText("Cancel Friend Request");
                                binding.personDeclineFriendRequest.setVisibility(View.GONE);
                                binding.personDeclineFriendRequest.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }
}

