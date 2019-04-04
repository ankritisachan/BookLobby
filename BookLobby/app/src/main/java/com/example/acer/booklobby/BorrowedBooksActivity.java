package com.example.acer.booklobby;

import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class BorrowedBooksActivity extends AppCompatActivity implements EnableInternetReceiver.InternetStateReceiver {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressBar progressBar;
    private EnableInternetReceiver enableInternetReceiver;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private HomeRecyclerViewAdapter homeRecyclerViewAdapter;
    private static ArrayList<Books> books=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrowed_books);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Borrowed Books");
        enableInternetReceiver = new EnableInternetReceiver();
        enableInternetReceiver.addListener(this);
        this.registerReceiver(enableInternetReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        final String userId= FirebaseAuth.getInstance().getCurrentUser().getEmail();
        LinearLayout noBooks=(LinearLayout)findViewById(R.id.noBooks);
        noBooks.setVisibility(View.GONE);
        db.collection("books")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            books.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if( (document.getString("borrowerId")!=null && !document.getString("borrowerId").equals("")) && userId.equals(document.getString("borrowerId")) ) {
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
                                }
                                Log.d("CHECK", "inside complete+=========size========"+books.size());
                                //Log.d("LOG", document.getId() + " => " + document.getData());
                            }
                            if(books!=null && books.size()>0) {
                                recyclerView = findViewById(R.id.allBooks);
                                layoutManager = new GridLayoutManager(BorrowedBooksActivity.this, 2);
                                //
                                recyclerView.setLayoutManager(layoutManager);

                                homeRecyclerViewAdapter = new HomeRecyclerViewAdapter(books, BorrowedBooksActivity.this);
                                recyclerView.setHasFixedSize(true);
                                recyclerView.setAdapter(homeRecyclerViewAdapter);
                            }
                            else
                            {
                                LinearLayout noBooks=(LinearLayout)findViewById(R.id.noBooks);
                                noBooks.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.d("ERROR", "Error getting documents: ", task.getException());
                        }
                    }
                });

        Log.d("CHECK", "outside complete+=========size========"+books.size());


        Log.d("CHECK", "CHECK+=========size========"+books.size());

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
        Toast.makeText(this,"Internet not available",Toast.LENGTH_SHORT).show();

    }
}

