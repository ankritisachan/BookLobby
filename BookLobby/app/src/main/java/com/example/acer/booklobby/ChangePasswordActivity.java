package com.example.acer.booklobby;

import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity implements EnableInternetReceiver.InternetStateReceiver {
private EditText oldPwd;
private EditText newPwd;
private EditText confirmPwd;
private Button btnChangePassword;
private EnableInternetReceiver enableInternetReceiver;
private FirebaseAuth auth;
private String TAG="ChangePasswordActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Change Password");
        oldPwd=(EditText)findViewById(R.id.oldPwd);
        newPwd=(EditText)findViewById(R.id.newPwd);
        confirmPwd=(EditText)findViewById(R.id.confirmPwd) ;
        btnChangePassword=(Button)findViewById(R.id.btnChangePwd);
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
        enableInternetReceiver = new EnableInternetReceiver();
        enableInternetReceiver.addListener(this);
        this.registerReceiver(enableInternetReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

    }
    private boolean checkvalidations(){
        if(TextUtils.isEmpty(oldPwd.getText().toString())){
            oldPwd.setError("Cannot be empty");
            return false;
        }
        else if(TextUtils.isEmpty(newPwd.getText().toString())){
            newPwd.setError("Cannot be empty");
            return false;
        }
        else if(newPwd.getText().toString().length()<6){
            newPwd.setError("Password length should be atleast 6 character.");
            return false;
        }
        if(TextUtils.isEmpty(confirmPwd.getText().toString())){
            confirmPwd.setError("Cannot be empty");
            return false;
        }
        else if(!newPwd.getText().toString().equals(confirmPwd.getText().toString())){
            newPwd.setError("Password is not same.");
            return false;
        }
        return true;
    }
    private void changePassword(){
        if(checkvalidations()) {
            auth = FirebaseAuth.getInstance();
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            AuthCredential credential = EmailAuthProvider
                    .getCredential(auth.getCurrentUser().getEmail(), oldPwd.getText().toString());
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updatePassword(newPwd.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "Password updated");
                                            Toast.makeText(ChangePasswordActivity.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Log.d(TAG, "Error password not updated");
                                            Toast.makeText(ChangePasswordActivity.this, "Password could not be updated!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Log.d(TAG, "Error auth failed");
                                Toast.makeText(ChangePasswordActivity.this, "Authentication failed.Please re enter your current password.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
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
        btnChangePassword.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        btnChangePassword.setEnabled(true);

    }

    @Override
    public void networkUnavailable() {
        Log.e("nc","Internet not available");
        Toast.makeText(this,"Internet not available",Toast.LENGTH_SHORT).show();
        btnChangePassword.setBackgroundColor(getResources().getColor(R.color.input_login_hint));
        btnChangePassword.setEnabled(false);
    }
}


