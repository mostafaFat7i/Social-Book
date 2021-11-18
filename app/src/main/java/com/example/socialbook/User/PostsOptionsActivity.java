package com.example.socialbook.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.socialbook.MainActivity;
import com.example.socialbook.R;
import com.example.socialbook.databinding.ActivityPostsOptionsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class PostsOptionsActivity extends AppCompatActivity {

    private String postKey,currentUser,databaseUserID,description,image;
    private DatabaseReference clickedPostRef;
    private StorageReference postRef;
    private EditText descriptionEditeText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityPostsOptionsBinding binding= DataBindingUtil.setContentView(this,R.layout.activity_posts_options);

        postKey=getIntent().getExtras().get("postkey").toString();
        clickedPostRef= FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);
        mAuth=FirebaseAuth.getInstance();
        postRef=FirebaseStorage.getInstance().getReference().child("post images");
        currentUser=mAuth.getCurrentUser().getUid();
        binding.deletPostOptions.setVisibility(View.INVISIBLE);
        binding.updatePostOptions.setVisibility(View.INVISIBLE);
        descriptionEditeText=binding.descriptionPostOptions;
        clickedPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

              if (snapshot.exists()){
                  description=snapshot.child("description").getValue().toString();
                  image=snapshot.child("postImage").getValue().toString();

                  binding.descriptionPostOptions.setText(description);
                  Picasso.get().load(image).into(binding.postImageOptions);

                  databaseUserID=snapshot.child("uid").getValue().toString();

                  if (currentUser.equals(databaseUserID)){
                      binding.deletPostOptions.setVisibility(View.VISIBLE);
                      binding.updatePostOptions.setVisibility(View.VISIBLE);
                      binding.descriptionPostOptions.setEnabled(true);
                  }
              }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void DeletePost(View view) {
        clickedPostRef.removeValue();
        postRef.child(postKey+".jpg").delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(PostsOptionsActivity.this, "Delete from storage", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Toast.makeText(this, "Post Deleted Successfully..", Toast.LENGTH_SHORT).show();
        SendUserToMainActivity();
    }

    private void SendUserToMainActivity() {
        Intent intent=new Intent(PostsOptionsActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void UpdatePost(View view) {
        clickedPostRef.child("description").setValue(descriptionEditeText.getText().toString());
        SendUserToMainActivity();

    }
}