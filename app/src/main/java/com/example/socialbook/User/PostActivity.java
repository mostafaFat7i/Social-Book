package com.example.socialbook.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.socialbook.MainActivity;
import com.example.socialbook.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;
    private EditText descriptionInput;
    final static int Galary_Pic = 1;
    private Uri imageUri;
    private ImageView selectPostImage;

    private StorageReference postsImagesRef;
    private DatabaseReference userRef,postRef;
    private FirebaseAuth mAuth;

    private String saveCurrentDate, saveCurrentTime, postRandomName,downloadUrl,currentUser,description;
    private long countPosts=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Initialization();
    }

    private void Initialization() {
        mToolbar = findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("New Post");

        selectPostImage = findViewById(R.id.selected_post_image);
        descriptionInput = findViewById(R.id.input_post_text);
        postsImagesRef = FirebaseStorage.getInstance().getReference();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        postRef= FirebaseDatabase.getInstance().getReference().child("Posts");
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser().getUid();
        loadingBar=new ProgressDialog(this);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent intent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void SelectFromGalary(View view) {
        Intent galaryIntent = new Intent();
        galaryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galaryIntent.setType("image/*");
        startActivityForResult(galaryIntent, Galary_Pic);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Galary_Pic && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            selectPostImage.setImageURI(imageUri);
        }
    }

    public void VailidatePostInfo(View view) {
        description = descriptionInput.getText().toString();
        if (imageUri == null) {
            Toast.makeText(this, "Please select post image ", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(description))
        {
            Toast.makeText(this, "please write about post image", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("please wait, post is uploading");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            StoreImageInDatabase();
        }
    }

    private void StoreImageInDatabase() {
        Calendar calendarForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calendarForDate.getTime());

        Calendar calendarForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calendarForTime.getTime());
        postRandomName=saveCurrentDate+saveCurrentTime;

        StorageReference filePath=postsImagesRef.child("post images")
                .child(currentUser+postRandomName+".jpg");

        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    Task<Uri> result=task.getResult().getMetadata().getReference().getDownloadUrl();
                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl=uri.toString();
                            Toast.makeText(PostActivity.this, "post image uploaded...", Toast.LENGTH_SHORT).show();
                            SavingPostInformationInDatabase();
                        }
                    });
                } 
                else {
                    Toast.makeText(PostActivity.this, "Error:"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });




    }

    private void SavingPostInformationInDatabase()
    {
        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    countPosts=snapshot.getChildrenCount();
                }
                else {
                    countPosts=0;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        userRef.child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    String userFullName=snapshot.child("fullname").getValue().toString();
                    String userProfileImage=snapshot.child("profileimages").getValue().toString();

                    HashMap postMap=new HashMap();
                    postMap.put("uid",currentUser);
                    postMap.put("date",saveCurrentDate);
                    postMap.put("time",saveCurrentTime);
                    postMap.put("description",description);
                    postMap.put("postImage",downloadUrl);
                    postMap.put("profileimage",userProfileImage);
                    postMap.put("fullname",userFullName);
                    postMap.put("counter",countPosts);


                    postRef.child(currentUser+postRandomName).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                loadingBar.dismiss();
                                SendUserToMainActivity();
                                Toast.makeText(PostActivity.this, "New post is updated successfully...", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                loadingBar.dismiss();
                                Toast.makeText(PostActivity.this, "Error pccure: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}