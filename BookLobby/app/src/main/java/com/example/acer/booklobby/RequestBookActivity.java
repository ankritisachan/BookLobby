package com.example.acer.booklobby;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RequestBookActivity extends AppCompatActivity  implements EnableInternetReceiver.InternetStateReceiver {

    TextView bookTitle, currentDate, lenderEmail;
    Spinner spinner_location, spinner_date;
    Button confirmBorrow;
    private EnableInternetReceiver enableInternetReceiver;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    String OTP;
    AlertDialog.Builder builder1, builder2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_book);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Request Book");

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        enableInternetReceiver = new EnableInternetReceiver();
        enableInternetReceiver.addListener(this);
        this.registerReceiver(enableInternetReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        bookTitle = findViewById(R.id.bookTitle);
        currentDate = findViewById(R.id.currentdate);
        lenderEmail = findViewById(R.id.lenderEmail);
        spinner_location = findViewById(R.id.spinner_location);
        spinner_date = findViewById(R.id.spinner_date);
        confirmBorrow = findViewById(R.id.confirmBorrow);

        Intent intent = getIntent();
        final Books b = (Books) intent.getSerializableExtra("bookObject");
        bookTitle.setText(b.getBookName());
        lenderEmail.setText(b.getOwnerName());

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        currentDate.setText(dateFormat.format(date));

        ArrayList<String> location = new ArrayList<>();
        location.add("Select Borrow Location");
        location.add("Canteen : Ground Floor");
        location.add("Library : Ground Floor");
        location.add("New Academic Building : Ground Floor");
        location.add("Old Academic Building : Ground Floor");
        location.add("Old Boys Hostel : BCR");
        location.add("New Boys Hostel : Ground Floor");
        location.add("Old Girls Hostel: GCR");
        location.add("New Girls Hostel : Ground Floor");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, location);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_location.setAdapter(adapter);

        Calendar c = Calendar.getInstance();
        c.setTime(date);

        ArrayList<String> nextDates = new ArrayList<>();
        nextDates.add("Select Borrow Date");
        for (int i = 1; i <= 5; i++) {
            c.add(Calendar.DATE, 1);
            Date date1 = c.getTime();
            nextDates.add(dateFormat.format(date1));
        }

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, nextDates);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_date.setAdapter(adapter2);

        OTP = generateOTP();
        builder1 = new AlertDialog.Builder(this);
        builder2 = new AlertDialog.Builder(this);

        confirmBorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CHECK", "CHECK======================On confirm borrow click");
                if (checkValidations()) {
                    builder2.setTitle("Confirm ?")
                            .setMessage("Are you sure want to borrow book ?")
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            })
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    String borrower = auth.getCurrentUser().getEmail();
                                    String owner = b.getOwnerName();
                                    String bookId = b.getBookId();
                                    String bookName = b.getBookName();
                                    String borrowDate = spinner_date.getSelectedItem().toString().trim();
                                    String status = "Unverified";
                                    String place = spinner_location.getSelectedItem().toString().trim();
                                    int rentDays = b.getRentDays();

                                    db.collection("books").document(b.getBookId()).update("borrowerId", borrower);
                                    Map<String, Object> transaction = new HashMap<>();
                                    transaction.put("owner", owner);
                                    transaction.put("borrower", borrower);
                                    transaction.put("bookId", bookId);
                                    transaction.put("bookName", bookName);
                                    transaction.put("borrowDate", borrowDate);
                                    transaction.put("status", status);
                                    transaction.put("selectedLocation", place);
                                    transaction.put("otp", OTP);
                                    transaction.put("rentDays", rentDays);
                                    transaction.put("dueDate", "");
                                    db.collection("transactions")
                                            .add(transaction)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Log.d("CHECK", "CHECK+++++++++++++++++++++++++++transaction added successfully");
                                                    builder1.setTitle("Borrow request has been sent to the owner.")
                                                            .setMessage("Your OTP for this request is " + OTP + ". You can check it again in your Borrowed Books section. Please share it with the owner when you meet them.")
                                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    Intent intent1 = new Intent(RequestBookActivity.this, NavigationActivity.class);
                                                                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    startActivity(intent1);
                                                                    //finish();
                                                                }
                                                            });
                                                    AlertDialog alert = builder1.create();
                                                    alert.show();
                                                }
                                            });
                                }
                            });
                    AlertDialog alert = builder2.create();
                    alert.show();
                }
            }
        });


//                if (spinner_location.getSelectedItem() == null)
//                    Toast.makeText(getApplicationContext(), "Please select a convenient location.", Toast.LENGTH_LONG).show();
//                else if (spinner_date.getSelectedItem() == null)
//                    Toast.makeText(getApplicationContext(), "Please select a convenient borrow date.", Toast.LENGTH_LONG).show();
//                Toast.makeText(getApplicationContext(), "Book borrow request has been sent to owner." +
//                        spinner_date.getSelectedItem().toString() + spinner_location.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
    }

    private String generateOTP() {

        String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        char[] otp = new char[6];

        Random rand = new Random();

        for (int i = 0; i < 6; i++) {
            otp[i] = characters.charAt(rand.nextInt(characters.length()));
        }

        return String.valueOf(otp);

    }
    private boolean checkValidations()
    {
        if (TextUtils.equals(spinner_location.getSelectedItem().toString(),"Select Borrow Location")) {
            ((TextView) spinner_location.getSelectedView()).setError("Cannot be empty");
            return false;
        }
        else if(TextUtils.equals(spinner_date.getSelectedItem().toString(),"Select Borrow Date")) {
            ((TextView) spinner_date.getSelectedView()).setError("Cannot be empty");
            return false;

        }
        return true;
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
        confirmBorrow.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        confirmBorrow.setEnabled(true);

    }

    @Override
    public void networkUnavailable() {
        Log.e("nc","Internet not available");
        Toast.makeText(this,"Internet not available",Toast.LENGTH_SHORT).show();
        confirmBorrow.setBackgroundColor(getResources().getColor(R.color.input_login_hint));
        confirmBorrow.setEnabled(false);
    }
}
