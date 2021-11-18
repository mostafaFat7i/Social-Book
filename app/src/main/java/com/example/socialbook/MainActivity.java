package com.example.socialbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialbook.User.CommentsActivity;
import com.example.socialbook.User.FindFriendsActivity;
import com.example.socialbook.User.FriendsActivity;
import com.example.socialbook.User.MessagesActivity;
import com.example.socialbook.User.PostsOptionsActivity;
import com.example.socialbook.User.ProfileActivity;
import com.example.socialbook.User.RegisterActivity;
import com.example.socialbook.User.SettingActivity;
import com.example.socialbook.ViewHolder.PostsViewHolder;
import com.example.socialbook.Model.Posts;
import com.example.socialbook.User.LoginActivity;
import com.example.socialbook.User.PostActivity;
import com.example.socialbook.User.SetUpActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef,postsRef,likesRef;
    private CircleImageView navProfImage;
    private TextView navUserFullName;
    private String currentUserID;
    private FloatingActionButton fab;
    private Boolean likeChecker=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Initialization();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPostActivity();
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;

            }
        });

        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild("fullname")){
                    String fullName=snapshot.child("fullname").getValue().toString();
                    navUserFullName.setText(fullName);
                }
                if (snapshot.hasChild("profileimages")){
                    String image=snapshot.child("profileimages").getValue().toString();

                    Picasso.get().load(image).placeholder(R.drawable.profile).into(navProfImage);
                }
                else {
                    Toast.makeText(MainActivity.this, "profile name not exist...", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DisplayAllUsersPosts();


    }

    private void sendUserToSetupActivity() {
        Intent intent=new Intent(MainActivity.this,SetUpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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

        userRef.child(currentUserID).child("userState").updateChildren(currentStateMap);


    }


    private void DisplayAllUsersPosts() {

        Query sortPostsInDecindindOrder=postsRef.orderByChild("counter");

        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(sortPostsInDecindindOrder,Posts.class).build();
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model) {

                final String postKey=getRef(position).getKey();

                holder.userName.setText(model.getFullname());
                holder.postTime.setText(model.getTime());
                holder.postDate.setText(model.getDate()+"__");
                holder.postDiscribtion.setText(model.getDescription());
                final CircleImageView image=holder.postProfileImage;

                holder.SetLikeButtonStatus(postKey);
                holder.SetCommentsStatus(postKey);

                userRef.child(model.getUid()).child("profileimages").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String value=snapshot.getValue().toString();
                        Picasso.get().load(value).placeholder(R.drawable.profile).into(image);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                Picasso.get().load(model.getPostImage()).into(holder.postImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(MainActivity.this, PostsOptionsActivity.class);
                        intent.putExtra("postkey",postKey);
                        startActivity(intent);
                    }
                });

                holder.commentPostBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent=new Intent(MainActivity.this, CommentsActivity.class);
                        commentsIntent.putExtra("postkey",postKey);
                        startActivity(commentsIntent);
                    }
                });


                holder.likePostBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        likeChecker=true;
                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                if (likeChecker==true){
                                    if (snapshot.child(postKey).hasChild(currentUserID)){
                                        likesRef.child(postKey).child(currentUserID).removeValue();
                                        likeChecker=false;
                                    }
                                    else {
                                        likesRef.child(postKey).child(currentUserID).setValue(true);
                                        likeChecker=false;
                                    }
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
                PostsViewHolder holder = new PostsViewHolder(view);
                return holder;
            }
        };

        postList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

        UpdateUserStatus("online");
    }
    

    private void Initialization() {
        mToolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        fab=findViewById(R.id.fab);
        postsRef=FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef=FirebaseDatabase.getInstance().getReference().child("Likes");


        postList=findViewById(R.id.all_user_posts_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);



        actionBarDrawerToggle=new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        View navView=navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfImage=navView.findViewById(R.id.nav_main_profile_image);
        navUserFullName=navView.findViewById(R.id.nav_main_user_name);
        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");

    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild("fullname")){
                    sendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (currentUser==null)
        {
            SendUserToLoginActivity();
        }
        else {
            CheckUserExistence();
        }
    }

    private void CheckUserExistence()
    {
        final String currentUser=mAuth.getCurrentUser().getUid();
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(currentUser)){
                    GoToSetUpActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void GoToSetUpActivity() {
        Intent intent=new Intent(MainActivity.this, SetUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void SendUserToLoginActivity()
    {
        Intent intent=new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void SendUserToPostActivity()
    {
        Intent intent=new Intent(MainActivity.this, PostActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void SendUserToSettingActivity()
    {
        Intent intent=new Intent(MainActivity.this, SettingActivity.class);
        startActivity(intent);
    }
    private void SendUserToProfileActivity()
    {
        Intent intent=new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }
    private void SendUserToMessagesActivity()
    {
        Intent intent=new Intent(MainActivity.this, MessagesActivity.class);
        startActivity(intent);
    }
    private void SendUserToFindFriendsActivity()
    {
        Intent intent=new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(intent);
    }
    private void SendUserToFriendsActivity() {
        Intent intent=new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    private void UserMenuSelector(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_post:
                SendUserToPostActivity();
                break;
            case R.id.nav_profile:
                SendUserToProfileActivity();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                SendUserToFriendsActivity();
                break;
            case R.id.nav_find_friends:
                SendUserToFindFriendsActivity();
                break;
            case R.id.nav_messages:
                SendUserToMessagesActivity();
                break;
            case R.id.nav_settings:
                SendUserToSettingActivity();
                break;
            case R.id.nav_logout:
                UpdateUserStatus("offline");
                mAuth.signOut();
                SendUserToLoginActivity();
                break;

        }
    }




}

