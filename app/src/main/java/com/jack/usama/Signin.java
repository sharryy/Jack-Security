package com.jack.usama;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import dmax.dialog.SpotsDialog;

public class Signin extends AppCompatActivity {
    private EditText email_SignIn;
    private EditText password_SignIn;
    private Button button_SignIn;

    private FirebaseAuth firebaseAuth;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        email_SignIn = findViewById(R.id.email_SignIn);
        password_SignIn = findViewById(R.id.password_SignIn);
        button_SignIn = findViewById(R.id.button_SignIn);

        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);

        button_SignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(Signin.this).build();
                waitingDialog.show();

                String email = email_SignIn.getText().toString().trim();
                String password = password_SignIn.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    email_SignIn.setError("Email can't be Empty.");
                    email_SignIn.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    password_SignIn.setError("Password can't be Empty.");
                    password_SignIn.requestFocus();
                    return;
                }

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sessionManager.setLogin(true);
                            Intent intent = new Intent(getApplicationContext(), Home.class);
                            Toast.makeText(Signin.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
                            waitingDialog.dismiss();
                            startActivity(intent);
                            finish();
                        } else {
                            waitingDialog.dismiss();
                            Toast.makeText(Signin.this, "Incorrect Email or Password!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Toast.makeText(Signin.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}