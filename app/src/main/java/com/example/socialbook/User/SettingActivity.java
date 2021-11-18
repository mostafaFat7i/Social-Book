package com.example.socialbook.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.socialbook.MainActivity;
import com.example.socialbook.R;
import com.example.socialbook.databinding.ActivitySettingBinding;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class SettingActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private DatabaseReference settingUserRef;
    private StorageReference userProfileImageRef;
    private FirebaseAuth mAuth;
    private ProgressDialog progressBar;
    private String currentUserID;
    final static int Galary_Pic=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivitySettingBinding binding= DataBindingUtil.setContentView(this,R.layout.activity_setting);

        settingUserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        userProfileImageRef= FirebaseStorage.getInstance().getReference().child("profile images");
        mToolbar=findViewById(R.id.setting_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settingUserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Picasso.get().load(snapshot.child("profileimages").getValue().toString()).into(binding.settingProfileImage);
                    binding.settingProfileUsername.setText(snapshot.child("username").getValue().toString());
                    binding.settingProfileFullname.setText(snapshot.child("fullname").getValue().toString());
                    binding.settingProfileStatus.setText(snapshot.child("status").getValue().toString());
                    binding.settingProfileCountery.setText(snapshot.child("country").getValue().toString());
                    binding.settingProfileGender.setText(snapshot.child("gender").getValue().toString());
                    binding.settingProfileBirthday.setText(snapshot.child("dob").getValue().toString());
                    binding.settingProfileRelationshipStatus.setText(snapshot.child("relationshipstatus").getValue().toString());
                }
                else {
                    Toast.makeText(SettingActivity.this, "Data not founded", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.settingUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSetting(binding);
            }
        });

        binding.settingProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galaryIntent=new Intent();
                galaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galaryIntent.setType("image/*");
                startActivityForResult(galaryIntent,Galary_Pic);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Galary_Pic && resultCode==RESULT_OK && data!=null){
            Uri imageUri=data.getData();

            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1).start(this);
        }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);

            if (resultCode==RESULT_OK){
                Uri resultUri=result.getUri();

                StorageReference filePath=userProfileImageRef.child(currentUserID+".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()){
                            Toast.makeText(SettingActivity.this, "profile image stored successfully..", Toast.LENGTH_SHORT).show();

                            Task<Uri> result=task.getResult().getMetadata().getReference().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUri=uri.toString();
                                    settingUserRef.child(currentUserID).child("profileimages").setValue(downloadUri)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Toast.makeText(SettingActivity.this, "Profile image store in firebase database", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(SettingActivity.this,SettingActivity.class));
                                                    }
                                                    else {
                                                        Toast.makeText(SettingActivity.this, "error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    }
                });
            }
            else {
                Toast.makeText(this, "error: Image can't be cropped try again..", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void UpdateSetting(ActivitySettingBinding binding) {
        String userName=binding.settingProfileUsername.getText().toString();
        String fullName=binding.settingProfileFullname.getText().toString();
        String status=binding.settingProfileStatus.getText().toString();
        String relationshipStatus=binding.settingProfileRelationshipStatus.getText().toString();
        String country=binding.settingProfileCountery.getText().toString();
        String gender=binding.settingProfileGender.getText().toString();
        String birthday=binding.settingProfileBirthday.getText().toString();

        if (TextUtils.isEmpty(userName)){
            Toast.makeText(this, "Please,enter your name", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(fullName)){
            Toast.makeText(this, "Please,enter your fullName", Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(status)){
            Toast.makeText(this, "Please,enter your status", Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(relationshipStatus)){
            Toast.makeText(this, "Please,enter your relationship status", Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(country)){
            Toast.makeText(this, "Please,enter your contry", Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(gender)){
            Toast.makeText(this, "Please,enter your gender", Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(birthday)){
            Toast.makeText(this, "Please,enter your birthday", Toast.LENGTH_SHORT).show();
        }
        else {

            HashMap userMap=new HashMap();
            userMap.put("username",userName);
            userMap.put("fullname",fullName);
            userMap.put("gender",gender);
            userMap.put("dob",birthday);
            userMap.put("country",country);
            userMap.put("status",status);
            userMap.put("relationshipstatus",relationshipStatus);

            settingUserRef.child(currentUserID).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(SettingActivity.this, "Account Setting updated...", Toast.LENGTH_SHORT).show();
                        SendUserToMainActivity();
                    }
                    else {
                        Toast.makeText(SettingActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }


    }
    private void SendUserToMainActivity() {
        Intent intent=new Intent(SettingActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}