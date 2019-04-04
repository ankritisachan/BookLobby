package com.example.acer.booklobby;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity implements EnableInternetReceiver.InternetStateReceiver {
    private static FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView user_name;
    private TextView user_type;
    private EditText name;
    private TextView email;
    private TextView rollNo;
    private EditText phoneNo;
    private Button btnUpdate;
    private static String userId;
    private CircleImageView user_photo;
    private TextView change_photo;
    private static final String TAG = "View/Edit Profile";
    private static int LOAD_IMAGE = 1;
    private static StorageReference mStorageRef;
    private static Uri userPhotoUri;
    private EnableInternetReceiver enableInternetReceiver;
    final Map<String, Object> userData = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("View/Edit Profile");
        enableInternetReceiver = new EnableInternetReceiver();
        enableInternetReceiver.addListener(this);
        this.registerReceiver(enableInternetReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        setContentView(R.layout.activity_view_profile);
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://booklobby-6ff3a.appspot.com");
        user_name = (TextView) findViewById(R.id.user_name);
        user_type = (TextView) findViewById(R.id.user_type);
        name = (EditText) findViewById(R.id.name);
        email = (TextView) findViewById(R.id.email);
        rollNo = (TextView) findViewById(R.id.rollNo);
        phoneNo = (EditText) findViewById(R.id.phoneNo);
        btnUpdate = (Button) findViewById(R.id.update);
        change_photo=(TextView)findViewById(R.id.change_photo);
        user_photo=(CircleImageView) findViewById(R.id.user_photo);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadUserData();
        change_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFromGallery();
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });


    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOAD_IMAGE && null != data) {
            userPhotoUri = data.getData();
            user_photo.setImageURI(userPhotoUri);
        }


    }

    public void pickFromGallery() {
        Intent i1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i1, LOAD_IMAGE);
    }
    private void loadUserData() {
        CollectionReference users = db.collection("users");
        users.whereEqualTo("email", auth.getCurrentUser().getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId());
                                userId = document.getId();
                                user_name.setText(document.getData().get("name").toString());
                                user_type.setText(document.getData().get("userType").toString());
                                name.setText(document.getData().get("name").toString());
                                email.setText(document.getData().get("email").toString());
                                rollNo.setText(document.getData().get("uniqueId").toString());
                                phoneNo.setText(document.getData().get("phoneNo").toString());
                                if(document.getData().get("imagePath")!=null)
                                {
                                    Picasso.get()
                                            .load(document.getData().get("imagePath").toString())
                                            .fit()
                                            .placeholder(R.drawable.profile_picture)
                                            .centerCrop()
                                            .into(user_photo);
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private boolean checkValidations() {

        if (TextUtils.isEmpty(name.getText().toString())) {
            name.setError("Cannot be empty");
            Log.e("name", "here");
            return false;
        } else if (TextUtils.isEmpty(phoneNo.getText().toString())) {
            phoneNo.setError("Cannot be empty");
            return false;
        } else if (phoneNo.getText().toString().length() < 10) {
            phoneNo.setError("Phone No. should be atleast 10 characters wide.");
            return false;
        }
        return true;
    }

    private void updateData( String temp) {
    final String n=temp;
            db.collection("users").document(userId).update(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "User data successfully updated");
                    user_name.setText(n);
                    Toast.makeText(ViewProfileActivity.this, "User data successfully updated", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "User data could not be updated");
                    Toast.makeText(ViewProfileActivity.this, "User data could not be updated", Toast.LENGTH_SHORT).show();
                    btnUpdate.setEnabled(true);
                    btnUpdate.setText("UPDATE");
                }
            });
        }

    private void update() {
        if (checkValidations()) {
            btnUpdate.setEnabled(false);
            btnUpdate.setText("UPDATING...");
            final String temp = name.getText().toString();
            userData.put("name", temp);
            userData.put("phoneNo", phoneNo.getText().toString());
            if (userPhotoUri != null) {
                String userPhotoName = userPhotoUri.toString().substring(userPhotoUri.toString().lastIndexOf('/') + 1);
                final StorageReference storageReference = mStorageRef.child("images/users/" + userPhotoName + ".jpg");
                UploadTask uploadTask = storageReference.putFile(userPhotoUri);
                Task<Uri> urlTask = uploadTask.continueWithTask(new
                                                                        Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                                                            @Override
                                                                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                                                                if (!task.isSuccessful()) {
                                                                                    throw task.getException();
                                                                                }

                                                                                // Continue with the task to get the download URL
                                                                                return storageReference.getDownloadUrl();
                                                                            }
                                                                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            userData.put("imagePath", downloadUri.toString());
                            updateData(temp);


                        } else {
                            // Handle failures
                            // ...
                            Toast.makeText(ViewProfileActivity.this, "Image could not be uploaded!!", Toast.LENGTH_SHORT).show();
                            btnUpdate.setEnabled(true);
                            btnUpdate.setText("UPDATE");
                        }
                    }
                });
            } else {
                updateData(temp);
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(enableInternetReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause() {
        unregisterReceiver(enableInternetReceiver);
        super.onPause();
    }

    @Override
    public void networkAvailable() {
        Log.e("nc", "Internet available!!");
        btnUpdate.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btnUpdate.setEnabled(true);

    }

    @Override
    public void networkUnavailable() {
        Log.e("nc", "Internet not available");
        Toast.makeText(this, "Internet not available", Toast.LENGTH_SHORT).show();
        btnUpdate.setBackgroundColor(getResources().getColor(R.color.input_login_hint));
        btnUpdate.setEnabled(false);
    }
}
