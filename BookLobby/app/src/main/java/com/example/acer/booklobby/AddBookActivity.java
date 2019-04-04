package com.example.acer.booklobby;


import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import de.hdodenhof.circleimageview.CircleImageView;


public class AddBookActivity extends AppCompatActivity implements EnableInternetReceiver.InternetStateReceiver {
    private static FirebaseFirestore db;
    private FirebaseAuth auth;
    private Button btn_addBook;
    private Button btn_browse;
    private Spinner spinner_category;
    private EditText book_name, author, book_rent, rent_duration;
    private TextView user_name;
    private TextView user_type;
    private RadioGroup radioGroup;
    private LinearLayout book_rate_div;
    private LinearLayout rent_duration_div;
    private static CircleImageView user_photo;
    private ImageView bookImage;
    private static StorageReference mStorageRef;
    private static int LOAD_IMAGE = 1;
    private static int REQUEST_CAMERA = 2;
    private static int index;
    private static final String TAG = "Add Book";
    private static Uri bookPhotoUri;
    final static Map<String, Object> book = new HashMap<>();
    private EnableInternetReceiver enableInternetReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add Book");
        try {

            btn_addBook = (Button) findViewById(R.id.btn_addBook);
            btn_browse = (Button) findViewById(R.id.btn_browse);
            spinner_category = findViewById(R.id.spinner_category);
            book_name = (EditText) findViewById(R.id.book_name);
            author = (EditText) findViewById(R.id.author_name);
            book_rent = (EditText) findViewById(R.id.book_rate);
            rent_duration = (EditText) findViewById(R.id.rent_duration);
            radioGroup = (RadioGroup) findViewById(R.id.bookType);
            enableInternetReceiver = new EnableInternetReceiver();
            enableInternetReceiver.addListener(this);
            this.registerReceiver(enableInternetReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

            book_rate_div = (LinearLayout) findViewById(R.id.book_rate_div);
            rent_duration_div = (LinearLayout) findViewById(R.id.rent_duration_div);
            book_rate_div.setVisibility(View.GONE);
            String[] categories = new String[]{"Select Book Category", "Compiler", "Database Management", "Operating Systems", "Linear Algebra", "Mobile Computing", "Data Science","Programming Concepts","Novel","Comics","General Knowledge","Artificial Intelligence","Machine Learning" ,"Others"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
            spinner_category.setAdapter(adapter);
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    index = radioGroup.indexOfChild(radioGroup.findViewById(checkedId));
                    if (index == 0) {
                        book_rate_div.setVisibility(View.GONE);
                    } else {
                        book_rate_div.setVisibility(View.VISIBLE);
                    }
                }
            });
            btn_addBook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addBookNew(v);
                }
            });
            btn_browse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pickFromGallery();
                }
            });
            mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://booklobby-6ff3a.appspot.com");
            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            user_name = (TextView) findViewById(R.id.user_name);
            user_type = (TextView) findViewById(R.id.user_type);
            bookImage = (ImageView) findViewById(R.id.img_book);
            bookImage.setVisibility(View.GONE);
            String userId = auth.getCurrentUser().getUid();
            CollectionReference peopleRef = db.collection("users");
            peopleRef.whereEqualTo("email", auth.getCurrentUser().getEmail())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    user_name.setText(document.getData().get("name").toString());
                                    user_type.setText(document.getData().get("userType").toString());
                                    user_photo=(CircleImageView)findViewById(R.id.user_photo);
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
        catch(Exception ex)
        {
            Log.e(TAG, ex.getMessage(), ex);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOAD_IMAGE && null != data) {
            bookImage.setVisibility(View.VISIBLE);
            bookImage = (ImageView) findViewById(R.id.img_book);
            bookPhotoUri = data.getData();
            bookImage.setImageURI(bookPhotoUri);
        }


    }

    public void pickFromGallery() {
        Intent i1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i1, LOAD_IMAGE);
    }

    public void startCamera() {
        Intent i1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i1, REQUEST_CAMERA);
    }

    private boolean checkValidations() {
        if (TextUtils.isEmpty(book_name.getText().toString())) {
            book_name.setError("Cannot be empty");
            return false;
        } else if (TextUtils.isEmpty(author.getText().toString())) {
            author.setError("Cannot be empty");
            return false;
        } else if (TextUtils.equals(spinner_category.getSelectedItem().toString(),"Select Book Category")) {
            ((TextView) spinner_category.getSelectedView()).setError("Cannot be empty");
            return false;
        } else if (TextUtils.isEmpty(rent_duration.getText().toString())) {
            rent_duration.setError("Cannot be empty");
            return false;
        }
        return true;
    }

    public void addBookNew(View v) {
        if (checkValidations()) {

            btn_addBook.setEnabled(false);
            btn_addBook.setText("Adding....");

            book.put("name", book_name.getText().toString());
            book.put("author", author.getText().toString());
            book.put("category", spinner_category.getSelectedItem().toString());
            book.put("borrowerId", "");
            if (index == 1) {
                book.put("isFree", false);
                book.put("rentRate", Integer.parseInt(book_rent.getText().toString()));
                book.put("rentDays", Integer.parseInt(rent_duration.getText().toString()));
            } else {
                book.put("isFree", true);
                book.put("rentRate", 0);
                book.put("rentDays", 0);
            }
            int n = 0;
            int n3 = 0;

            String[] split1 = book.get("name").toString().split("\\s+");
            int n1 = split1.length;
            String[] split2 = book.get("author").toString().split("\\s+");
            int n2 = split2.length;
            n = n1 + n2;
            String[] temp = new String[n];
            System.arraycopy(split1, 0, temp, 0, n1);
            System.arraycopy(split2, 0, temp, n1, n2);
            book.put("ownerId", auth.getCurrentUser().getEmail());
            if (!book.get("category").equals("Others")) {
                String[] split3 = book.get("category").toString().split("\\s+");
                n3 = split3.length;
                String[] searchKeywords = new String[n + n3];
                System.arraycopy(temp, 0, searchKeywords, 0, n);
                System.arraycopy(split3, 0, searchKeywords, n, n3);
                book.put("searchKeywords", new ArrayList<String>(Arrays.asList(searchKeywords)));
            } else
                book.put("searchKeywords", new ArrayList<String>(Arrays.asList(temp)));
            if (bookPhotoUri != null) {
                String bookPhotoName = bookPhotoUri.toString().substring(bookPhotoUri.toString().lastIndexOf('/') + 1);
                final StorageReference storageReference = mStorageRef.child("images/books/" + bookPhotoName + ".jpg");
                UploadTask uploadTask = storageReference.putFile(bookPhotoUri);
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
                            book.put("imagePath", downloadUri.toString());
                            insertBook();


                        } else {
                            // Handle failures
                            // ...
                            Toast.makeText(AddBookActivity.this, "Image could not be uploaded!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                insertBook();
            }
        }
    }


    private void insertBook() {
        db.collection("books").add(book)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(final DocumentReference documentReference) {
                        Log.d(TAG, "Book created with Id:" + documentReference.getId());
                        Toast.makeText(AddBookActivity.this, "Book has been successfully added!!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(AddBookActivity.this, ManageMyBooksActivity.class);
                        startActivity(i);
                        finish();
                        // Handle failures
                        // ...

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding book", e);
                btn_addBook.setText("     Add  Book    ");
                btn_addBook.setEnabled(true);
                Toast.makeText(AddBookActivity.this, "Book could not be added!!", Toast.LENGTH_SHORT).show();
            }
        });
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
        Log.e("nc","Internet available!!");
        btn_addBook.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btn_addBook.setEnabled(true);

    }

    @Override
    public void networkUnavailable() {
        Log.e("nc","Internet not available");
        Toast.makeText(this,"Internet not available",Toast.LENGTH_SHORT).show();
        btn_addBook.setBackgroundColor(getResources().getColor(R.color.input_login_hint));
        btn_addBook.setEnabled(false);
    }
}


