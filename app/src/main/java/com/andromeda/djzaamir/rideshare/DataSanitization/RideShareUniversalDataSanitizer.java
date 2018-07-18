package com.andromeda.djzaamir.rideshare.DataSanitization;


import android.widget.EditText;

public class RideShareUniversalDataSanitizer {

    public static boolean sanitizeName(EditText editTextName) {
        //Enforce Minimum nameStringBuilder length = 3
        boolean nameGood;
        if (editTextName.getText().toString().trim().length() >= 3) {
            editTextName.setError(null);
            editTextName.clearFocus();
            nameGood = true;
        } else {
            editTextName.setError("Name can't be this short");
            nameGood = false;
        }
        return nameGood;
    }

    public static boolean sanitizeEmail(EditText editTextEmail) {
        boolean emailGood;
        String email = editTextEmail.getText().toString().trim();
        String email_regex_RFC_5322 = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
        if (!email.equals("") && email.matches(email_regex_RFC_5322)) {
            editTextEmail.setError(null);
            editTextEmail.clearFocus();
            emailGood = true;
        } else {
            editTextEmail.setError("Invalid Email");
            emailGood = false;
        }
        return emailGood;
    }

    public static boolean sanitizeCell(EditText editTextCell) {
        boolean cellGood;
        if (editTextCell.getText().toString().trim().length() == 11) {
            cellGood = true;
            editTextCell.setError(null);
            editTextCell.clearFocus();
        } else {
            cellGood = false;
            editTextCell.setError("Invalid Cell No");
        }
        return cellGood;
    }




    public static boolean sanitizePassword(EditText editTextPassword , EditText editTextComfirmPassowrd) {
        boolean passwordGood;
        String pass = editTextPassword.getText().toString().trim();
        String pass_comfirm = editTextComfirmPassowrd.getText().toString().trim();

        if (pass.equals(pass_comfirm)) {

            //Further Rules check
            passwordGood =  sanitizePassword(editTextPassword);

        } else {
            passwordGood = false;
            editTextPassword.setError("Password's don't match!");
        }
        return passwordGood;
    }

    public static boolean sanitizePassword(EditText editTextPassword){
        boolean passwordGood;

         //Make sure that they are not small passwords
            if (editTextPassword.getText().toString().trim().length() >= 7) {
                passwordGood = true;
                editTextPassword.setError(null);
                editTextPassword.clearFocus();
            } else {
                passwordGood = false;
                editTextPassword.setError("Weak Password!");
            }
            return passwordGood;
    }
}
