package com.example.acer.booklobby;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity implements EnableInternetReceiver.InternetStateReceiver {
    private static final String TAG = "Sign Up";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    Button  btn_submit;
    int index = 0;
    int validity = 5;//default validity of PhD students and faculty is 5
    private EnableInternetReceiver enableInternetReceiver;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("SignUp");
        enableInternetReceiver = new EnableInternetReceiver();
        enableInternetReceiver.addListener(this);
        this.registerReceiver(enableInternetReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.userType);

        btn_submit = findViewById(R.id.submit);
        final TextView tv = findViewById(R.id.user_type_id);
        final EditText et_name=  findViewById(R.id.name);
        final EditText et_email=  findViewById(R.id.email);
        final EditText et_userId = findViewById(R.id.unique_id);
        final EditText et_phoneNo=  findViewById(R.id.phoneNo);
        final EditText et_password=  findViewById(R.id.password);
        final EditText et_confirm_password=  findViewById(R.id.password2);
        final TextView tv_error = findViewById(R.id.error);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected

                index= radioGroup.indexOfChild(radioGroup.findViewById(checkedId));


                switch (index)
                {
                    case 0:{
                        tv.setText("Roll No.*");
                        et_userId.setHint("Enter Roll No.");
                        break;
                    }
                    case 1:
                    {
                        tv.setText("Faculty ID*");
                        et_userId.setHint("Enter faculty ID");
                        break;
                    }
                }

            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int flag = 1;

                tv_error.setVisibility(View.GONE);

                if(TextUtils.isEmpty(et_name.getText().toString())){
                    et_name.setError("Cannot be empty");
                    flag=0;
                }
                if(TextUtils.isEmpty(et_email.getText().toString())){
                    et_email.setError("Cannot be empty");
                    flag=0;
                }
                else{
                    String email = et_email.getText().toString().trim();
                    String substring = email.substring(email.lastIndexOf('@'));
                    if(!substring.equals("@iiitd.ac.in")){
                        et_email.setError("Enter IIITD Email Id.");
                        flag=0;
                    }
                }
                if(TextUtils.isEmpty(et_userId.getText().toString())){
                    et_userId.setError("Cannot be empty");
                    flag=0;
                }
                if(TextUtils.isEmpty(et_phoneNo.getText().toString())){
                    et_phoneNo.setError("Cannot be empty");
                    flag=0;
                }
                else if(et_phoneNo.getText().toString().length()<10){
                    et_phoneNo.setError("Phone No. should be atleast 10 characters wide.");
                    flag=0;
                }
                if(TextUtils.isEmpty(et_password.getText().toString())){
                    et_password.setError("Cannot be empty");
                    flag=0;
                }
                else if(et_password.getText().toString().length()<6){
                    et_password.setError("Password length should be atleast 6 character.");
                    flag = 0;
                }
                if(TextUtils.isEmpty(et_confirm_password.getText().toString())){
                    et_confirm_password.setError("Cannot be empty");
                    flag=0;
                }
                else if(!et_password.getText().toString().equals(et_confirm_password.getText().toString())){
                    et_confirm_password.setError("Password is not same.");
                    flag = 0;
                }
//                else {
//                    String roll_sub = et_userId.getText().toString().substring(0,3);
//                    if (index == 0 && !roll_sub.equalsIgnoreCase("phd") ) {
//                        String email = et_email.getText().toString().trim();
//                        String roll_no = et_userId.getText().toString().trim();
//                        int index = email.indexOf('@');
//                        Log.e("TAG", Integer.toString(index));
//                        String sub_email = email.substring(index - 5, index);
//                        Log.e("TAG", sub_email);
//                        String sub_roll = roll_no.substring(2);
//                        if (!sub_email.equals(sub_roll)) {
//                            tv_error.setText("*Your Email or Roll No. is incorrect.");
//                            tv_error.setVisibility(View.VISIBLE);
//                            flag = 0;
//                        }
//
//                        int year = Calendar.getInstance().get(Calendar.YEAR);
//                        String sub_year = roll_no.substring(2, 4);
//                        String sub_branch = roll_no.substring(0, 2);
//
//                        if(Integer.parseInt(sub_year) > (year-2000)){
//                            tv_error.setText("Invalid email or roll no.");
//                            tv_error.setVisibility(View.VISIBLE);
//                            flag = 0;
//                        }

//                        if (sub_branch.equalsIgnoreCase("MT") && Integer.parseInt(sub_year) < (year - 2001)) {
//                            tv_error.setText("*You cannot register since you are not a current student of IIITD.");
//                            tv_error.setVisibility(View.VISIBLE);
//                            flag = 0;}
//                        else if (sub_branch.equalsIgnoreCase("20") && Integer.parseInt(sub_year) < (year - 2003)) {
//                            tv_error.setText("*You cannot register since you are not a current student of IIITD.");
//                            tv_error.setVisibility(View.VISIBLE);
//                            flag = 0;
//                        }
//
//                        Log.d("TAG",sub_branch);
//
//                        if (sub_branch.equalsIgnoreCase("MT"))
//                            validity = 2;
//                        else if (sub_branch.equalsIgnoreCase("20"))
//                            validity = 4;
//                    }
//                }

                if(flag == 1){
                    auth=FirebaseAuth.getInstance();
                    auth.createUserWithEmailAndPassword(et_email.getText().toString(),et_password.getText().toString())
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        db = FirebaseFirestore.getInstance();
                                        Map<String, Object> user = new HashMap<>();
                                        if(index == 0)
                                            user.put("userType", "student");
                                        else if(index == 1)
                                            user.put("userType", "faculty");

                                        user.put("name", et_name.getText().toString().trim());
                                        user.put("email",et_email.getText().toString().trim());
                                        user.put("uniqueId", et_userId.getText().toString().trim());
                                        user.put("phoneNo",et_phoneNo.getText().toString().trim());
                                        user.put("validity", validity);
                                        db.collection("users")
                                                .add(user)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                                        Intent i = new Intent(getBaseContext(), Temporary.class);
                                                        startActivity(i);
                                                        finish();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error adding document", e);
                                                    }
                                                });
                                        Toast.makeText(SignupActivity.this, "User Created Successfully", Toast.LENGTH_LONG).show();
                                    }
                                    else
                                        Toast.makeText(SignupActivity.this,"User already exist.",Toast.LENGTH_LONG).show();
                                }
                            });
                }
                else {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                }
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
        btn_submit.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btn_submit.setEnabled(true);

    }

    @Override
    public void networkUnavailable() {
        Log.e("nc","Internet not available");
        Toast.makeText(this,"Internet not available",Toast.LENGTH_SHORT).show();
        btn_submit.setBackgroundColor(getResources().getColor(R.color.input_login_hint));
        btn_submit.setEnabled(false);
    }
}