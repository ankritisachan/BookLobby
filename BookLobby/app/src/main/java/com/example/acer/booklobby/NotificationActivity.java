package com.example.acer.booklobby;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Queue;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private NotificationRecyclerViewAdapter notificationRecyclerViewAdapter;

    private static ArrayList<Transaction> transactions;

//    private static String bookID;
//    private static String bookName;
    private static Transaction transaction;

    public NotificationActivity(){

        transactions = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Notifications");
        final String userId= FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Log.d("CHECK", "CHECK++++++++++++++++++++++++++"+userId);
        LinearLayout noBooks=(LinearLayout)findViewById(R.id.noBooks);
        noBooks.setVisibility(View.GONE);
        layoutManager = new LinearLayoutManager(NotificationActivity.this);

//        db.collection("books")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
//                            if(documentSnapshot.getId().equalsIgnoreCase(bookID)){
//                                Log.d("CHECK", "CHECK+++++++++++ getting book name in notific RV "+documentSnapshot.getString("name"));
//                                bookName = documentSnapshot.getString("name");
//                                break;
//                            }
//                        }
//                    }
//                });

        db.collection("transactions")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        transactions.clear();
                        for(QueryDocumentSnapshot document : task.getResult()) {

                            if(userId.equals(document.getString("owner")) && (document.getString("status").equalsIgnoreCase("Unverified"))){
                                Log.d("CHECK", "CHECK++++++++++++++++++++++++++"+document.getString("owner")+"========"+document.getString("status"));
                                transaction = new Transaction();
                                transaction.setBookId(document.getString("bookId"));
                                //bookID = document.getString("bookId");

//                                transaction.setBookName(bookName);
//
//                                Log.d("CHECK", "CHECK +++++++++++++++++GET BOOK NAME FROM TRANSACTION++++"+transaction.getBookName());

                                transaction.setBookName(document.getString("bookName"));
                                transaction.setBorrowDate(document.getString("borrowDate"));
                                transaction.setBorrower(document.getString("borrower"));
                                transaction.setOtp(document.getString("otp"));
                                transaction.setOwner(document.getString("owner"));
                                transaction.setSelectedLocation(document.getString("selectedLocation"));
                                transaction.setStatus(document.getString("status"));
                                transaction.setTransactionId(document.getId());
                                transaction.setRentDays(document.getLong("rentDays").intValue());
                                transactions.add(transaction);
                            }
                        }
                        Log.d("CHECK", "CHECK++++++++++++++++transaction size++++++++++"+transactions.size());
                        if(transactions.size()==0){
                            Toast.makeText(getApplicationContext(), "NO NEW BORROW REQUEST AVAILABLE !", Toast.LENGTH_LONG).show();
                            LinearLayout noBooks=(LinearLayout)findViewById(R.id.noBooks);
                            noBooks.setVisibility(View.VISIBLE);
                        }
                        else{
                            recyclerView = findViewById(R.id.notifRecycler);
                            recyclerView.setHasFixedSize(true);
                            recyclerView.setLayoutManager(layoutManager);
                          //  recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
                            notificationRecyclerViewAdapter = new NotificationRecyclerViewAdapter(NotificationActivity.this, transactions);
                            notificationRecyclerViewAdapter.notifyDataSetChanged();
                            recyclerView.setAdapter(notificationRecyclerViewAdapter);
                        }
                    }
                });
    }
}
