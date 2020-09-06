package com.jack.usama;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences("AppKey", 0);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    public void setLogin(boolean login) {
        editor.putBoolean("KEY_LOGIN", login);
        editor.commit();
    }

    public boolean getLogin() {
        return sharedPreferences.getBoolean("KEY_LOGIN", false);
    }

    public void setUsername(String username) {
        editor.putString("KEY_USERNAME", username);
        editor.commit();
    }

    public String getUsername() {
        return sharedPreferences.getString("KEY_USERNAME", "");
    }

    public void setContactName(String contactName){
        editor.putString("KEY_CONTACT_NAME", contactName);
        editor.commit();
    }

    public String getContactName(){
        return sharedPreferences.getString("KEY_CONTACT_NAME", "");
    }

    public void setContactNumber(String contactNumber){
        editor.putString("KEY_CONTACT_NUMBER", contactNumber);
        editor.commit();
    }

    public String getContactNumber(){
        return sharedPreferences.getString("KEY_CONTACT_NUMBER", "");
    }

    public void setMessage(String message){
        editor.putString("KEY_MESSAGE", message);
        editor.commit();
    }

    public String getMessage(){
        return sharedPreferences.getString("KEY_MESSAGE", "");
    }
}
