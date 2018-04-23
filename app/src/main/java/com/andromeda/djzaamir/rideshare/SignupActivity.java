package com.andromeda.djzaamir.rideshare;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignupActivity extends AppCompatActivity {

    //GUI references
    private EditText editTextName , editTextEmail ,editTextCell,editTextPassword ,editTextComfirmPassowrd;
    private Button signupButton;

    //Flags to make sure good data is being entered
    private boolean nameGood,emailGood, cellGood,passwordGood,primary_pass_field_visited = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //init gui references
        editTextName            = findViewById(R.id.edittextbox_name);
        editTextEmail           = findViewById(R.id.edittextbox_email);
        editTextCell            = findViewById(R.id.edittextbox_cell);
        editTextPassword        = findViewById(R.id.edittextbox_password);
        editTextComfirmPassowrd = findViewById(R.id.edittextbox_password_comfirm);

        signupButton =  findViewById(R.id.signup_button);

    }


    public void performSignup(View view) {



        //Perform firebase auth request and other data upload's
        if (!isDataGood()){//Run error checks
            return; //Exit Singup function
        }

        //Perform singup
        Toast.makeText(this,"Signup in process Sire!",Toast.LENGTH_SHORT).show();

    }



    boolean sanitizeName(){
        //Enforce Minimum name length = 3
       if (editTextName.getText().toString().trim().length() >= 3){
            editTextName.setError(null);
            editTextName.clearFocus();
            nameGood = true;
       }else{
           editTextName.setError("Name can't be this short");
           nameGood = false;
       }
       return nameGood;
    }
    boolean sanitizeEmail(){
              String email = editTextEmail.getText().toString().trim();
              String email_regex_RFC_5322 = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
              if (!email.equals("") && email.matches(email_regex_RFC_5322)){
                  editTextEmail.setError(null);
                  editTextEmail.clearFocus();
                  emailGood =  true;
              }else{
                  editTextEmail.setError("Invalid Email");
                  emailGood = false;
              }
      return emailGood;
    }
    boolean sanitizeCell(){
      if (editTextCell.getText().toString().trim().length() == 11){
        cellGood = true;
        editTextCell.setError(null);
        editTextCell.clearFocus();
        }else{
            cellGood = false;
            editTextCell.setError("Invalid Cell No");
        }
        return cellGood;
    }
    boolean sanitizePassword(){
       String pass = editTextPassword.getText().toString().trim();
       String pass_comfirm = editTextComfirmPassowrd.getText().toString().trim();

       if (pass.equals(pass_comfirm)){

           //Make sure that they are not small passwords
           if (pass.length() >= 7){
               passwordGood = true;
               editTextPassword.setError(null);
               editTextPassword.clearFocus();
           }else{
               passwordGood = false;
               editTextPassword.setError("Weak Password!");
           }

        }else{
            passwordGood = false;
            editTextPassword.setError("Password's don't match!");
        }
        return passwordGood;
    }

    boolean isDataGood(){
        return sanitizeName() && sanitizeEmail() && sanitizeCell() && sanitizePassword();
    }


}
