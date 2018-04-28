package com.andromeda.djzaamir.rideshare;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    //Gui references
    private EditText editTextEmail,editTextPassword;
    private boolean passwordGood=false,emailGood=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Establish references to gui
        editTextEmail    =  findViewById(R.id.edittextbox_email_login);
        editTextPassword =  findViewById(R.id.edittextbox_password_login);
    }

    public void logIn(View view) {
        if (sanitizeAllData()){
          String email    =  editTextEmail.getText().toString();
          String password =  editTextPassword.getText().toString();
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>            () {
                @Override
                public void onSuccess(AuthResult authResult) {

                    //Start Core RideShare Activity
                    Intent rideShareCoreIntent =  new Intent(LoginActivity.this, NavigationDrawer.class);
                    startActivity(rideShareCoreIntent);

                    //Dispose off error
                    editTextEmail.setError(null);

                    //Dispose this activity
                    finish();
                    return;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    editTextEmail.setText("");
                    editTextPassword.setText("");
                    editTextEmail.setError("Invalid Email or Password!");
                }
            });
        }
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
    boolean sanitizePassword(){
       String pass = editTextPassword.getText().toString().trim();

           //Make sure that they are not small passwords
           if (pass.length() >= 7){
               passwordGood = true;
               editTextPassword.setError(null);
               editTextPassword.clearFocus();
           }else{
               passwordGood = false;
               editTextPassword.setError("Please enter a valid password");
           }

        return passwordGood;
    }
    boolean sanitizeAllData(){
        return sanitizeEmail() && sanitizePassword();
    }
}
