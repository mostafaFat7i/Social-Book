package com.example.socialbook.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialbook.Adapter.MessagesAdapter;
import com.example.socialbook.Model.Messages;
import com.example.socialbook.R;
import com.example.socialbook.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView recyclerView;
    private MessagesAdapter adapter;
    private final List<Messages> messagesList=new ArrayList<>();
    private String messageReceiverId, messageReceiverName, messageSenderId, saveCurrentDate, saveCurrentTime;
    private TextView receiverName,lastSeen;
    private CircleImageView receiverImage;
    private String cheker="",myUri="";
    private Uri fileUri;
    private StorageTask uploadTask;
    private ProgressDialog progressBar;
    private DatabaseReference rootRef,userRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityChatBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);

        progressBar=new ProgressDialog(this);

        recyclerView=binding.messagesList;
        adapter=new MessagesAdapter(messagesList);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);



        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");


        messageReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("userName").toString();

        messageSenderId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mToolbar = findViewById(R.id.chat_bar_layout);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(view);

        receiverName = findViewById(R.id.custom_bar_profile_name);
        receiverImage = findViewById(R.id.custom_bar_profile_image);
        lastSeen=findViewById(R.id.custom_bar_user_last_seen);

        DisplayReceiverInformation();

        binding.chatSendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage(binding);
            }
        });

        FetchMessages();

    }

    @Override
    protected void onStart() {
        super.onStart();
        UpdateUserStatus("online");
    }

    private void FetchMessages() {
        rootRef.child("Message").child(messageSenderId).child(messageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if (snapshot.exists()){
                    Messages message=snapshot.getValue(Messages.class);
                    messagesList.add(message);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void UpdateUserStatus(String state){

        String saveCurrentDate, saveCurrentTime;

        Calendar calendarForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calendarForDate.getTime());

        Calendar calendarForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm a");
        saveCurrentTime = currentTime.format(calendarForTime.getTime());

        Map currentStateMap=new HashMap();

        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);

        userRef.child(messageSenderId).child("userState").updateChildren(currentStateMap);

    }
    private void SendMessage(final ActivityChatBinding binding) {
        UpdateUserStatus("online");

        String message = binding.chatInputMessage.getText().toString();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "please,enter your message first", Toast.LENGTH_SHORT).show();
        } else {

            String message_sender_ref = "Message/" + messageSenderId + "/" + messageReceiverId;
            String message_receiver_ref = "Message/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference user_message_key = rootRef.child("Messages").child(messageSenderId).child(messageReceiverId).push();
            String message_push_id = user_message_key.getKey();

            Calendar calendarForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calendarForDate.getTime());

            Calendar calendarForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
            saveCurrentTime = currentTime.format(calendarForTime.getTime());

            HashMap messageTextBody = new HashMap();
            messageTextBody.put("message", binding.chatInputMessage.getText().toString());
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderId);

            HashMap messageBodyDetails=new HashMap();
            messageBodyDetails.put(message_sender_ref+"/"+message_push_id,messageTextBody);
            messageBodyDetails.put(message_receiver_ref+"/"+message_push_id,messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message sent successfully..", Toast.LENGTH_SHORT).show();
                        binding.chatInputMessage.setText("");
                    }
                    else {
                        Toast.makeText(ChatActivity.this, "Error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        binding.chatInputMessage.setText("");
                    }

                }
            });



        }
    }

    private void DisplayReceiverInformation() {
        receiverName.setText(messageReceiverName);
        rootRef.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Picasso.get().load(snapshot.child("profileimages").getValue().toString()).placeholder(R.drawable.profile).into(receiverImage);

                if (snapshot.child("userState").child("type").exists()){
                    String type=snapshot.child("userState").child("type").getValue().toString();
                    String time=snapshot.child("userState").child("time").getValue().toString();
                    String date=snapshot.child("userState").child("date").getValue().toString();

                    if (type.equals("online")){
                        lastSeen.setText("online");
                    }
                    else {
                        lastSeen.setText("last seen: "+time+" "+date);
                    }
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode==430 && resultCode==RESULT_OK && data!=null && data.getData()!=null){

            progressBar.setTitle("Sending Message");
            progressBar.setMessage("Please Wait message is sending");
            progressBar.setCanceledOnTouchOutside(true);
            progressBar.show();

            fileUri=data.getData();

            if (!cheker.equals("image")){
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Document Messages");

                final String message_sender_ref = "Message/" + messageSenderId + "/" + messageReceiverId;
                final String message_receiver_ref = "Message/" + messageReceiverId + "/" + messageSenderId;

                DatabaseReference user_message_key = rootRef.child("Messages").child(messageSenderId).child(messageReceiverId).push();
                final String message_push_id = user_message_key.getKey();

                final StorageReference filePath=storageReference.child(message_push_id+"."+cheker);

                Calendar calendarForDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                saveCurrentDate = currentDate.format(calendarForDate.getTime());

                Calendar calendarForTime = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
                saveCurrentTime = currentTime.format(calendarForTime.getTime());

                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    HashMap messageTextBody = new HashMap();
                                    messageTextBody.put("message", downloadUrl);
                                    messageTextBody.put("name", fileUri.getLastPathSegment());
                                    messageTextBody.put("date", saveCurrentDate);
                                    messageTextBody.put("time", saveCurrentTime);
                                    messageTextBody.put("type", cheker);
                                    messageTextBody.put("from", messageSenderId);

                                    HashMap messageBodyDetails=new HashMap();
                                    messageBodyDetails.put(message_sender_ref+"/"+message_push_id,messageTextBody);
                                    messageBodyDetails.put(message_receiver_ref+"/"+message_push_id,messageTextBody);

                                    rootRef.updateChildren(messageBodyDetails);
                                    progressBar.dismiss();
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.dismiss();
                        Toast.makeText(ChatActivity.this, "Error"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double p=(100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                        progressBar.setMessage((int) p+" %  Uploading....");
                    }
                });
            }
            else if (cheker.equals("image")){
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Image Messages");

                final String message_sender_ref = "Message/" + messageSenderId + "/" + messageReceiverId;
                final String message_receiver_ref = "Message/" + messageReceiverId + "/" + messageSenderId;

                DatabaseReference user_message_key = rootRef.child("Messages").child(messageSenderId).child(messageReceiverId).push();
                final String message_push_id = user_message_key.getKey();

                final StorageReference filePath=storageReference.child(message_push_id+".jpg");

                uploadTask=filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful()){
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadUri=task.getResult();
                            myUri=downloadUri.toString();

                            Calendar calendarForDate = Calendar.getInstance();
                            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                            saveCurrentDate = currentDate.format(calendarForDate.getTime());

                            Calendar calendarForTime = Calendar.getInstance();
                            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
                            saveCurrentTime = currentTime.format(calendarForTime.getTime());

                            HashMap messageTextBody = new HashMap();
                            messageTextBody.put("message", myUri);
                            messageTextBody.put("date", saveCurrentDate);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("type", cheker);
                            messageTextBody.put("from", messageSenderId);

                            HashMap messageBodyDetails=new HashMap();
                            messageBodyDetails.put(message_sender_ref+"/"+message_push_id,messageTextBody);
                            messageBodyDetails.put(message_receiver_ref+"/"+message_push_id,messageTextBody);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(ChatActivity.this, "Message sent successfully..", Toast.LENGTH_SHORT).show();
                                        progressBar.dismiss();
                                    }
                                    else {
                                        Toast.makeText(ChatActivity.this, "Error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        progressBar.dismiss();
                                    }

                                }
                            });
                        }
                    }
                });
            }
            else {
                Toast.makeText(this, "Nothing Selected", Toast.LENGTH_SHORT).show();
                progressBar.dismiss();
            }
        }
    }

    public void SelectImageMessage(View view) {

        cheker="image";
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent.createChooser(intent,"Select Image"),430);
    }

    public void SelectDocumentMessage(View view) {
        CharSequence options[]=new CharSequence[]{
                "PDF Files",
                "MS Word Files"
        };
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Select the Document Type");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    cheker="pdf";
                    Intent intent=new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    startActivityForResult(intent.createChooser(intent,"Select Pdf Files"),430);
                }
                if (which==1){
                    cheker="word";
                    Intent intent=new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/msword");
                    startActivityForResult(intent.createChooser(intent,"Select Word Files"),430);
                }
            }
        });
        builder.show();
    }
}