package com.example.acer.booklobby;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

public class BookDetailsActivity extends AppCompatActivity implements EnableInternetReceiver.InternetStateReceiver {

    ImageView bookImage;
    TextView bookName, authorName, ownerEmail, rentDays, rentRate, labelDueDate, dueDate, labelOTP, OTP;
    Button borrow;
    private EnableInternetReceiver enableInternetReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Book Details");
        enableInternetReceiver = new EnableInternetReceiver();
        enableInternetReceiver.addListener(this);
        this.registerReceiver(enableInternetReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        bookImage = findViewById(R.id.bookImage);
        bookName = findViewById(R.id.bookName);
        authorName = findViewById(R.id.authorName);
        ownerEmail = findViewById(R.id.ownerEmail);
        rentDays = findViewById(R.id.rentDays);
        rentRate = findViewById(R.id.rentRate);
        borrow = findViewById(R.id.borrow);

        labelDueDate = findViewById(R.id.labelDueDate);
        labelOTP = findViewById(R.id.labelOTP);


        Intent intent = getIntent();
        final Books book = (Books)intent.getSerializableExtra("BookObject");
        String callingActivity=intent.getStringExtra("callingActivity");
        if(book.getImageURL()==null)
            bookImage.setImageResource(R.drawable.book);
        else
        Picasso.get()
                .load(book.getImageURL())
                .fit()
                .placeholder(R.drawable.book)
                .centerCrop()
                .into(bookImage);
        bookName.setText(book.getBookName());
        authorName.setText(book.getAuthorName());
        ownerEmail.setText(book.getOwnerName());
        rentDays.setText(String.valueOf(book.getRentDays()));
        rentRate.setText(String.valueOf(book.getRentDays()));

        Log.d("CHECK", "CHECK=================="+book.getBookId());
        if(callingActivity.equals("navigation")) {
            borrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(BookDetailsActivity.this, RequestBookActivity.class);
                    intent.putExtra("bookObject", book);
                    startActivity(intent);
                    finish();
                }
            });
        }
        else if(callingActivity.equals("borrowed")){
            labelOTP.setVisibility(View.VISIBLE);

            borrow.setVisibility(View.INVISIBLE);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("transactions").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(QueryDocumentSnapshot document : task.getResult()){
                                    Log.e("db",document.get("bookId").toString());
                                    Log.e("c",book.getBookId());

                                    if(document.get("bookId").equals(book.getBookId())){
                                        if(document.getString("dueDate")!=null && document.getString("dueDate")!="" ){
                                            dueDate = findViewById(R.id.dueDate);
                                            labelDueDate.setVisibility(View.VISIBLE);
                                            dueDate.setVisibility(View.VISIBLE);
                                            dueDate.setText(document.getString("dueDate"));
                                        }
                                        OTP = findViewById(R.id.otp);
                                        OTP.setVisibility(View.VISIBLE);
                                        OTP.setText(document.getString("otp"));
                                        Log.d("CHECK", "CHECK++++++++++++++++++++"+document.getString("dueDate")+"============"+document.getString("otp"));

                                    }
                                }
                            }
                        }
                    });
        }
        else
        {
            borrow.setVisibility(View.GONE);
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
        Log.e("nc","Internet available!!");
    }

    @Override
    public void networkUnavailable() {
        Log.e("nc","Internet not connected");
        Toast.makeText(this,"Internet not connected",Toast.LENGTH_SHORT).show();

    }
}
