package com.geetapoojakaran.uberapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLoginRegisterActivity extends AppCompatActivity {

    private Button DriverLoginButton;
    private Button DriverRegisterButton;
    private TextView DriverRegisterLink;
    private TextView DriverStatus;
    private EditText DriverEmail;
    private EditText DriverPassword;
     private FirebaseAuth mAuth;
    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_register);

        mAuth=FirebaseAuth.getInstance();

        loadingbar =new ProgressDialog(this);

        DriverLoginButton=findViewById(R.id.driver_login_btn);
        DriverRegisterButton=findViewById(R.id.driver_register_btn);
        DriverRegisterLink=findViewById(R.id.register_driver_link);
        DriverStatus=findViewById(R.id.driver_status);
        DriverEmail=findViewById(R.id.driver_email);
        DriverPassword=findViewById(R.id.driver_password);


        DriverRegisterButton.setVisibility(View.INVISIBLE);
        DriverRegisterButton.setEnabled(false);

        DriverRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DriverLoginButton.setVisibility(View.INVISIBLE);
                DriverRegisterLink.setVisibility(View.INVISIBLE);
                DriverStatus.setText("Welcome to Driver Registration Page");

                DriverRegisterButton.setVisibility(View.VISIBLE);
                DriverRegisterButton.setEnabled(true);
            }
        });
        DriverRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=DriverEmail.getText().toString();
                String password=DriverPassword.getText().toString();

                RegisterDriver(email,password);
            }
        });
        DriverLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=DriverEmail.getText().toString();
                String password=DriverPassword.getText().toString();

                SignInDriver(email,password);
            }
        });

    }

    private void SignInDriver(String email, String password) {

        if (TextUtils.isEmpty(email)){
            Toast.makeText(getApplicationContext(),"Please write email....",Toast.LENGTH_SHORT).show();

        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(),"please write password",Toast.LENGTH_SHORT).show();
        }

        loadingbar.setTitle("Driver Login ");
        loadingbar.setMessage("Please wait while checking your credentials");
        loadingbar.show();


        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){



                    Toast.makeText(getApplicationContext(),"Login is Successful",Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();

                    Intent driverintent=new Intent(getApplicationContext(),DriverMapActivity.class);
                    startActivity(driverintent);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Login is unsuccessful",Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();
                }
            }
        });
    }


    private void RegisterDriver(String email, String password) {

        if (TextUtils.isEmpty(email)){
            Toast.makeText(getApplicationContext(),"Please write email....",Toast.LENGTH_SHORT).show();

        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(),"please write password",Toast.LENGTH_SHORT).show();
        }

        loadingbar.setTitle("Driver Registration ");
        loadingbar.setMessage("Please wait while your data is registering");
        loadingbar.show();


        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Registration is Successful",Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();

                    String driverId=mAuth.getCurrentUser().getUid();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId);
                    ref.setValue(true);

                }
                else {
                    Toast.makeText(getApplicationContext(), "Registration is unsuccessful",Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();
                }
            }
        });
    }
}
