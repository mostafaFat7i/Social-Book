package com.example.socialbook.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.socialbook.MainActivity;
import com.example.socialbook.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText email,password,confirmPass;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Initialization();
    }

    private void Initialization()
    {
        progressDialog=new ProgressDialog(this);
        email=findViewById(R.id.regist_email);
        password=findViewById(R.id.regist_password);
        confirmPass=findViewById(R.id.regist_confirm_password);
        mAuth=FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if (currentUser != null)
        {
            SendUserToMainActivity();
        }
    }
    private void SendUserToMainActivity() {
        Intent intent=new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void GoToLoginActivity(View view)
    {
        startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
        finish();
    }

    public void CreateNewAccount(View view)
    {
        String userMail=email.getText().toString();
        String userPass=password.getText().toString();
        String userConfirmPass=confirmPass.getText().toString();

        if (TextUtils.isEmpty(userMail))
        {
            Toast.makeText(this, "Please, Enter your email...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(userPass))
        {
            Toast.makeText(this, "Please, Enter your password...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(userConfirmPass))
        {
            Toast.makeText(this, "Please, Confirm your password...", Toast.LENGTH_SHORT).show();
        }
        else if (!userPass.equals(userConfirmPass)){
            confirmPass.setError("Different Password");
            Toast.makeText(this, "Please, Enter password correct...", Toast.LENGTH_SHORT).show();
        }
        else {
            progressDialog.setTitle("Create Account");
            progressDialog.setMessage("please waite you are creating new account...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();

            mAuth.createUserWithEmailAndPassword(userMail,userPass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            
                            if (task.isSuccessful()){
                                SendEmailVerificationMessage();
                                progressDialog.dismiss();
                            }
                            else {
                                Toast.makeText(RegisterActivity.this, "Exception: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }

                        }
                    });
        }

    }

    private void SendEmailVerificationMessage(){

        FirebaseUser user=mAuth.getCurrentUser();

        if (user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Registeration successfull. please check your emal to verify account..", Toast.LENGTH_SHORT).show();
                        SendUserToLoginActivity();
                        mAuth.signOut();
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, "Error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                }
            });
        }
    }

    private void SendUserToLoginActivity(){
        Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
