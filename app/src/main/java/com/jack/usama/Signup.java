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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dmax.dialog.SpotsDialog;

public class Signup extends AppCompatActivity {
    private EditText name_SignUp;
    private EditText email_SignUp;
    private EditText password_SignUp;
    private EditText repeatPassword_SignUp;
    private Button btn_SignUp;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        name_SignUp = findViewById(R.id.name_SignUp);
        email_SignUp = findViewById(R.id.email_SignUp);
        password_SignUp = findViewById(R.id.password_SignUp);
        repeatPassword_SignUp = findViewById(R.id.repeatPass_SignUp);
        btn_SignUp = findViewById(R.id.button_SignUp);

        sessionManager = new SessionManager(this);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        btn_SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(Signup.this).build();
                waitingDialog.show();

                final String name = name_SignUp.getText().toString().trim();
                final String email = email_SignUp.getText().toString().trim();
                final String password = password_SignUp.getText().toString().trim();
                String repeatPassword = repeatPassword_SignUp.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    name_SignUp.setError("Name can't be Empty.");
                    name_SignUp.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(email)) {
                    email_SignUp.setError("Email can't be Empty.");
                    email_SignUp.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    password_SignUp.setError("Password can't be Empty.");
                    password_SignUp.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(repeatPassword)) {
                    repeatPassword_SignUp.setError("Please Re-Enter Passsword");
                    repeatPassword_SignUp.requestFocus();
                    return;
                }
                if (!password.equals(repeatPassword)) {
                    password_SignUp.setError("Password doesn't match.");
                    repeatPassword_SignUp.setError("Password doesn't match.");
                    return;
                }

                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            User user = new User(name, email, password);
                            FirebaseDatabase.getInstance().getReference("User's Database")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            sessionManager.setUsername(name);
                                            Toast.makeText(Signup.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), Signin.class);
                                            waitingDialog.dismiss();
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            waitingDialog.dismiss();
                                            Toast.makeText(Signup.this, "Error: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Toast.makeText(Signup.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
}