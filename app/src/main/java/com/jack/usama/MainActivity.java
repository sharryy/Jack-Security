package com.jack.usama;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PERMISSION_CODE = 123;
    SessionManager sessionManager;
    private Button btn_SignUp_Main;
    private Button btn_SignIn_Main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.WRITE_CONTACTS, Manifest.permission.SEND_SMS,
                                    Manifest.permission.CALL_PHONE},
                    PERMISSION_CODE);
        }

        sessionManager = new SessionManager(this);

        if (sessionManager.getLogin()) {
            startActivity(new Intent(getApplicationContext(), Home.class));
            finish();
        }

        btn_SignUp_Main = findViewById(R.id.btn_SignUp_Main);
        btn_SignIn_Main = findViewById(R.id.btn_SignIn_Main);

        btn_SignUp_Main.setOnClickListener(this);
        btn_SignIn_Main.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_SignIn_Main:
                Intent intent_SignIn = new Intent(MainActivity.this, Signin.class);
                startActivity(intent_SignIn);
                break;
            case R.id.btn_SignUp_Main:
                Intent intent_SignUp = new Intent(MainActivity.this, Signup.class);
                startActivity(intent_SignUp);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We're Good To Go.
            } else {
                Toast.makeText(this, "Permission Denied ! Can't Send Messages", Toast.LENGTH_SHORT).show();
            }
        }
    }
}