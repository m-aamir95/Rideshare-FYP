package com.andromeda.djzaamir.rideshare.utils;

/*
*  This class is only intended to store Successful login time
*  Email and Password
*  This info can be later used before re-authticaing with Firebase
*  for the purposes of Email and Password Change
*
*  Because reauthenticaing with a False `Email Or Password` results in Null User object
*  inside FirebaseAuth and FirebaseAuth is being used APP-WIDE for different purpose
*
*  to avoid it returning a NULL user we can first compare data present in this class
*  and on TRUE comparison re-authenticate via Firebase
* */

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class PasswordManager {
    public static String raw_password;
    public static String raw_email;


    //Only Update Email or password
    public static void saveChanges(String update_data, Update_Type update_type, Context context) {
        if (update_type == Update_Type.EMAIL) {
            saveChanges(update_data, raw_password, context);
        } else {
            saveChanges(raw_email, update_data, context);
        }
    }


    //Update both email and password
    public static void saveChanges(String email, String pass, Context context) {

        //update data in ram
        raw_password = pass;
        raw_email = email;

        //Store data inside SharedPreferences
        SharedPreferences email_and_pass_Preferences = context.getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor email_and_pass_editor = email_and_pass_Preferences.edit();

        email_and_pass_editor.putString("usr_email", email);
        email_and_pass_editor.putString("usr_pass", pass);

        email_and_pass_editor.apply();
    }


    public static void loadData(Context context) {
        SharedPreferences email_pass_preferences = context.getSharedPreferences("settings", MODE_PRIVATE);
        raw_email = email_pass_preferences.getString("usr_email", "NULL");
        raw_password = email_pass_preferences.getString("usr_pass", "NULL");
    }

    public enum Update_Type {
        EMAIL, PASSWORD
    }
}
