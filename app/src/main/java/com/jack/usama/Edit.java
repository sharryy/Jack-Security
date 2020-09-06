package com.jack.usama;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Edit extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    static Map<String, String> hashMap = new HashMap<>();
    ArrayList<String> contactName = new ArrayList<>();
    private Spinner spinner;
    private EditText editTextMessage;
    private Button saveButton;
    private String contact, number;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        spinner = findViewById(R.id.spinnerEdit);
        editTextMessage = findViewById(R.id.editTextMessage);
        saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(v -> {
            String message =  editTextMessage.getText().toString().trim();
            sessionManager.setMessage(message);
            Intent intent = new Intent(Edit.this, Home.class);
            startActivity(intent);
        });
        readContacts();
    }


    public void readContacts() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (phoneCursor.moveToNext()) {
                        String phone = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        hashMap.put(name, phone);
                        contactName.add(name);
                    }

                    Set<String> set = new LinkedHashSet<>(contactName);
                    contactName.clear();
                    contactName.addAll(set);
                    String[] contacts = new String[contactName.size()];

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, contactName.toArray(contacts));
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(arrayAdapter);
                    phoneCursor.close();
                }
            }
        }
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        contact = parent.getItemAtPosition(position).toString();
        number = hashMap.get(contact);
        sessionManager = new SessionManager(this);
        sessionManager.setContactName(contact);
        sessionManager.setContactNumber(number);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //don't need to do anything.
    }
}
