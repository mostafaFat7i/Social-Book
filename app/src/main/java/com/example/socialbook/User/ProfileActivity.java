package com.example.socialbook.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.socialbook.MainActivity;
import com.example.socialbook.R;
import com.example.socialbook.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference profileUserRef,friendsRef,postsRef;
    private FirebaseAuth mAuth;
    private String currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityProfileBinding binding= DataBindingUtil.setContentView(this,R.layout.activity_profile);

        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser().getUid();
        profileUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser);
        friendsRef=FirebaseDatabase.getInstance().getReference().child("Friends");
        postsRef=FirebaseDatabase.getInstance().getReference().child("Posts");

        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.profileActivityUsername.setText(snapshot.child("username").getValue().toString());
                binding.profileActivityFullname.setText(snapshot.child("fullname").getValue().toString());
                binding.profileActivityStatus.setText(snapshot.child("status").getValue().toString());
                binding.profileActivityBirthday.setText(snapshot.child("dob").getValue().toString());
                binding.profileActivityCountry.setText(snapshot.child("country").getValue().toString());
                binding.profileActivityGender.setText(snapshot.child("gender").getValue().toString());
                binding.profileActivityRelationStatus.setText(snapshot.child("relationshipstatus").getValue().toString());

                Picasso.get().load(snapshot.child("profileimages").getValue().toString()).into(binding.profileActivityImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        friendsRef.child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    binding.profileFriends.setText(snapshot.getChildrenCount()+" Friends");

                }
                else {
                    binding.profileFriends.setText("0 Friends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postsRef.orderByChild("uid").startAt(currentUser).endAt(currentUser+"\uf8ff")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    binding.profilePosts.setText(snapshot.getChildrenCount()+" Posts");
                }
                else {
                    binding.profilePosts.setText("0 Posts");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void GoToFrindsActivity(View view) {
        Intent intent=new Intent(ProfileActivity.this, FriendsActivity.class);
        startActivity(intent);
    }

    public void GoToMyPostsActivity(View view) {
        Intent intent=new Intent(ProfileActivity.this, MyPostsActivity.class);
        startActivity(intent);
    }
}