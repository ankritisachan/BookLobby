package com.example.acer.booklobby;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Temporary extends AppCompatActivity {

    TextView tv_message1, tv_message2;
    //create reference of auth
    private FirebaseAuth auth;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temporary);

        tv_message1 = findViewById(R.id.message1);
        tv_message1.setText("Sending verification link on email..........");

        tv_message2 = findViewById(R.id.message2);
        tv_message2.setText("Please verify your email address before login.");

        //getinstance of auth
        auth=FirebaseAuth.getInstance();
        //get current user which is login
        final FirebaseUser firebaseUser= auth.getCurrentUser();

        firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    auth.signOut();
                    Intent i=new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(i);
                    finish();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Email not sent",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
