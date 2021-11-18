package com.example.socialbook.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
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
import com.hbb20.CountryCodePicker;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetUpActivity extends AppCompatActivity {

    private EditText userName, fullName, dateOfBirthday;
    private CountryCodePicker ccp;
    private CircleImageView Uimage;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference userProfileImageRef;
    private String currentUID,relationshipStatus="none",gender="none";
    private Calendar myCalendar;
    private ProgressDialog progressDialog;
    final static int Galary_Pic=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);
        Initialization();

        myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        dateOfBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(SetUpActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    if (snapshot.hasChild("profileimages")){
                        String image=snapshot.child("profileimages").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(Uimage);
                    }
                    else {
                        Toast.makeText(SetUpActivity.this, "profile image not exist...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        dateOfBirthday.setText(sdf.format(myCalendar.getTime()));
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.male:
                if (checked)
                    gender="Male";
                break;
            case R.id.female:
                if (checked)
                    gender="Female";
                break;
            case R.id.single:
                if (checked)
                    relationshipStatus="Single";
                break;
            case R.id.married:
                if (checked)
                    relationshipStatus="Married";
                break;
        }
    }

    private void Initialization() {
        userName = findViewById(R.id.setup_name);
        fullName = findViewById(R.id.setup_full_name);
        dateOfBirthday= findViewById(R.id.Birthday);
//        country = findViewById(R.id.setup_country);
        ccp=findViewById(R.id.ccp);
        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUID);
        progressDialog=new ProgressDialog(this);
        userProfileImageRef= FirebaseStorage.getInstance().getReference().child("profile images");
        Uimage=findViewById(R.id.setup_image);


    }

    public void SaveAccountSetUpInformation(View view) {
        String Uname = userName.getText().toString();
        String UFname = fullName.getText().toString();
//        String Country = country.getText().toString();
        String Country=ccp.getSelectedCountryName();
        String dateOfbirth=dateOfBirthday.getText().toString();
        String genderType=gender;
        String relationState=relationshipStatus;



        if (TextUtils.isEmpty(Uname)) {
            userName.setError("required");
            Toast.makeText(this, "Please, Enter your name...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(UFname)) {
            fullName.setError("required");
            Toast.makeText(this, "Please, Enter your full name...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(dateOfbirth)) {
            dateOfBirthday.setError("required");
            Toast.makeText(this, "Please, Enter your date of birthday...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(genderType)) {
            Toast.makeText(this, "Please, choose your gender...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(relationState)) {
            Toast.makeText(this, "Please, choose your relationship state...", Toast.LENGTH_SHORT).show();
        }
        else {
            progressDialog.setTitle("Saving Information");
            progressDialog.setMessage("Please wait your account information are saving...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);


            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("username", Uname);
            userMap.put("fullname", UFname);
            userMap.put("country", Country);
            userMap.put("status", "hey, i'm new member here..");
            userMap.put("gender", genderType);
            userMap.put("dob", dateOfbirth);
            userMap.put("relationshipstatus", relationState);

            userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        SendUserToMainActivity();
                        Toast.makeText(SetUpActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                    else {
                        Toast.makeText(SetUpActivity.this, "error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });

        }
    }
    private void SendUserToMainActivity() {
        Intent intent=new Intent(SetUpActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void SaveProfileImage(View view)
    {
        Intent galaryIntent=new Intent();
        galaryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galaryIntent.setType("image/*");
        startActivityForResult(galaryIntent,Galary_Pic);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==Galary_Pic && resultCode==RESULT_OK && data!=null){
            Uri imageUri=data.getData();

            CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1).start(this);
        }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);

            if (resultCode==RESULT_OK){
                Uri resultUri=result.getUri();

                StorageReference filePath=userProfileImageRef.child(currentUID+".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        
                        if (task.isSuccessful()){
                            Toast.makeText(SetUpActivity.this, "profile image stored successfully..", Toast.LENGTH_SHORT).show();

                            Task<Uri> result=task.getResult().getMetadata().getReference().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUri=uri.toString();
                                    userRef.child("profileimages").setValue(downloadUri)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Toast.makeText(SetUpActivity.this, "Profile image store in firebase database", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(SetUpActivity.this,SetUpActivity.class));
                                                    }
                                                    else {
                                                        Toast.makeText(SetUpActivity.this, "error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
}