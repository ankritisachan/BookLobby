package com.example.acer.booklobby;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ManageMyBooksActivity extends AppCompatActivity  implements EnableInternetReceiver.InternetStateReceiver {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Button btnAddBook;
    private static ArrayList<Books> books=new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ManageMyBooksRecyclerViewAdapter manageMyBooksRecyclerViewAdapter;
    private EnableInternetReceiver enableInternetReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_my_books);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Manage My Books");
        enableInternetReceiver = new EnableInternetReceiver();
        enableInternetReceiver.addListener(this);
        this.registerReceiver(enableInternetReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        recyclerView = findViewById(R.id.manageBooks);
        final String userId=FirebaseAuth.getInstance().getCurrentUser().getEmail();
        db.collection("books")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            books.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.e("userId",userId);
                                Log.e("ownerId",document.getString("ownerId"));
                                if(userId.equals(document.getString("ownerId")) && (document.getString("borrowerId")==null || document.getString("borrowerId").equals(""))) {
                                    Books book = new Books();
                                    book.setBookId(document.getId());
                                    book.setAuthorName(document.getString("author"));
                                    Log.e("ERROR", "ERROR============" + document.getString("name"));
                                    book.setBookName(document.getString("name"));
                                    Log.e("ERROR", "ERROR============" + document.getString("ownerId"));
                                    book.setOwnerName(document.getString("ownerId"));
                                    book.setCategory(document.getString("category"));
                                    book.setRentDays(document.getLong("rentDays").intValue());
                                    book.setRentRate(document.getDouble("rentRate"));
                                    book.setFree(document.getBoolean("isFree"));
                                    book.setImageURL(document.getString("imagePath"));
                                    books.add(book);
                                    Log.d("CHECK", "inside complete+=========size========" + books.size());
                                }
                                //Log.d("LOG", document.getId() + " => " + document.getData());
                            }
                            if(books!=null && books.size()>0) {
                            recyclerView = findViewById(R.id.manageBooks);
                            layoutManager = new GridLayoutManager(ManageMyBooksActivity.this, 2);
                            recyclerView.setHasFixedSize(true);
                            recyclerView.setLayoutManager(layoutManager);
                            manageMyBooksRecyclerViewAdapter = new ManageMyBooksRecyclerViewAdapter(books,ManageMyBooksActivity.this);
                            recyclerView.setAdapter(manageMyBooksRecyclerViewAdapter);
                            manageMyBooksRecyclerViewAdapter.notifyDataSetChanged();
                            }
                            else
                            {
                                TextView noBooks=(TextView)findViewById(R.id.noBooks);
                                noBooks.setText("There are no books to display here!");
                            }
                        } else {
                            Log.d("ERROR", "Error getting documents: ", task.getException());
                        }
                    }
                });


        btnAddBook=(Button)findViewById(R.id.addBook);
        btnAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ManageMyBooksActivity.this, AddBookActivity.class));
                finish();
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


    }

    @Override
    public void networkUnavailable() {
        Log.e("nc","Internet not available");
        Toast.makeText(this,"net not connected",Toast.LENGTH_SHORT).show();

    }
}
