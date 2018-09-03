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

public class CustomerLoginRegisterActivity extends AppCompatActivity {

    private Button CustomerLoginButton;
    private Button CustomerRegisterButton;
    private TextView CustomerRegisterLink;
    private TextView CustomerStatus;
    private EditText CustomerEmail;
    private EditText CustomerPassword;
     private FirebaseAuth mAuth;
     ProgressDialog loadingbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);

        mAuth=FirebaseAuth.getInstance();
        loadingbar=new ProgressDialog(this);


        CustomerLoginButton=findViewById(R.id.customer_login_btn);
        CustomerRegisterButton=findViewById(R.id.customer_register_btn);
        CustomerRegisterLink=findViewById(R.id.customer_register_link);
        CustomerStatus=findViewById(R.id.customer_status);
        CustomerEmail=findViewById(R.id.customer_email);
        CustomerPassword=findViewById(R.id.customer_password);

        CustomerRegisterButton.setVisibility(View.INVISIBLE);
        CustomerRegisterButton.setEnabled(false);

        CustomerRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CustomerLoginButton.setVisibility(View.INVISIBLE);
                CustomerStatus.setText("Welcome to Customer Registration Page");
                CustomerRegisterLink.setVisibility(View.INVISIBLE);

                CustomerRegisterButton.setVisibility(View.VISIBLE);
                CustomerRegisterButton.setEnabled(true);
            }
        });
        CustomerRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=CustomerEmail.getText().toString();
                String password=CustomerPassword.getText().toString();

                RegisterCustomer(email,password);
            }
        });
        CustomerLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=CustomerEmail.getText().toString();
                String password=CustomerPassword.getText().toString();

                SignInCustomer(email,password);
            }
        });

    }

    private void SignInCustomer(String email, String password) {

        if (TextUtils.isEmpty(email)){
            Toast.makeText(CustomerLoginRegisterActivity.this,"Please write email....",Toast.LENGTH_SHORT).show();

        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(CustomerLoginRegisterActivity.this,"please write password",Toast.LENGTH_SHORT).show();
        }

        loadingbar.setTitle("Customer Login ");
        loadingbar.setMessage("Please wait while checking your credentials");
        loadingbar.show();


        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    Toast.makeText(CustomerLoginRegisterActivity.this,"Login is Successful",Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();

                    Intent customerintent=new Intent(CustomerLoginRegisterActivity.this,CustomerMapActivity.class);
                    startActivity(customerintent);

                }
                else {
                    Toast.makeText(CustomerLoginRegisterActivity.this, "Login is unsuccessful",Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();
                }
            }
        });
    }


    private void RegisterCustomer(String email, String password) {

        if (TextUtils.isEmpty(email)){
            Toast.makeText(CustomerLoginRegisterActivity.this,"Please write email....",Toast.LENGTH_SHORT).show();

        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(CustomerLoginRegisterActivity.this,"please write password",Toast.LENGTH_SHORT).show();
        }

        loadingbar.setTitle("Customer Registration ");
        loadingbar.setMessage("Please wait while your data is getting registered");
        loadingbar.show();


        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    Toast.makeText(CustomerLoginRegisterActivity.this,"Registration is Successful",Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();

                    String customerId=mAuth.getCurrentUser().getUid();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);
                    ref.setValue(true);


                }
                else {
                    Toast.makeText(CustomerLoginRegisterActivity.this, "Registration is unsuccessful",Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();
                }
            }
        });
    }
}

