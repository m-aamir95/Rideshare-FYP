package com.andromeda.djzaamir.rideshare;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.andromeda.djzaamir.rideshare.utils.ButtonUtils;
import com.andromeda.djzaamir.rideshare.utils.InputUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class SignupActivity extends AppCompatActivity {

    //GUI references
    private EditText editTextName, editTextEmail, editTextCell, editTextPassword, editTextComfirmPassowrd;
    private Button signupButton;
    private ProgressBar loading_spinner;

    //Flags to make sure good data is being entered
    private boolean nameGood, emailGood, cellGood, passwordGood, primary_pass_field_visited = false;

    //Firebase
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //init gui references
        editTextName = findViewById(R.id.edittextbox_name);
        editTextEmail = findViewById(R.id.edittextbox_email);
        editTextCell = findViewById(R.id.edittextbox_cell);
        editTextPassword = findViewById(R.id.edittextbox_password);
        editTextComfirmPassowrd = findViewById(R.id.edittextbox_password_comfirm);
        signupButton = findViewById(R.id.signup_button);
        loading_spinner =  findViewById(R.id.loading_spinner);

        //init auth instance
        firebaseAuth = FirebaseAuth.getInstance();


        //Register callback for firebaseAuthState Change
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {//Successful signup
                    //Take back to login Activity and display message account created successfully

                    Toast.makeText(SignupActivity.this, "Successful Signup :)", Toast.LENGTH_SHORT).show();



                    //Push Other Signup info to Firebase
                    pushOtherSignupDataToFirebase();

                    //Hide spinner
                    loading_spinner.setVisibility(View.GONE);

                    Intent welcomeActiviyIntent = new Intent(SignupActivity.this, WelcomeActivity.class);

                    startActivity(welcomeActiviyIntent);
                    //Dispose this activity
                    finish();
                    return;
                }
            }
        };
    }

    /*
    * Will be called when user's auth status change
    *
    * */
    private void pushOtherSignupDataToFirebase() {
//       Grab Data
        String name = editTextName.getText().toString();
        String cell = editTextCell.getText().toString();

//       Grab Db EndPoint Reference
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");

//       Prepare data to be sent
        HashMap<String, String> data_pair = new HashMap<>();
        data_pair.put("name", name);
        data_pair.put("cell", cell);

//        Push data
        ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(data_pair).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseAuth.getInstance().signOut();
            }
        });

    }


    public void performSignup(View view) {


        //Perform firebase auth request and other data upload's
        if (!isDataGood()) {//Run error checks
            return; //Exit Singup function
        }


        Button signup_button = findViewById(R.id.signup_button);
        ButtonUtils.disableAndChangeText(signup_button, "Signing up...");


        //disable input-controls
        InputUtils.disableInputControls(editTextName,editTextEmail,editTextCell,editTextPassword,editTextComfirmPassowrd);

        //Perform singup and init spinner
        loading_spinner.setVisibility(View.VISIBLE);

        //Try to register user with new email and password
        String email_to_register = editTextEmail.getText().toString();
        String password_to_register = editTextPassword.getText().toString();
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email_to_register, password_to_register).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignupActivity.this, "Email is in use by\nanother account!", Toast.LENGTH_LONG).show();
                loading_spinner.setVisibility(View.GONE);
                editTextEmail.requestFocus();
                editTextEmail.setError("Email is in use by another account");
                ButtonUtils.enableButtonRestoreTitle();
                InputUtils.enableInputControls();
            }
        }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                editTextEmail.clearFocus();
                editTextEmail.setError(null);
            }
        });

    }


    //Attach and detach FirebaseAuthStateChangeListener on App start and stop
    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    //Data Sanitizers
    boolean sanitizeName() {
        //Enforce Minimum nameStringBuilder length = 3
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

    boolean sanitizeEmail() {
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

    boolean sanitizeCell() {
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

    boolean sanitizePassword() {
        String pass = editTextPassword.getText().toString().trim();
        String pass_comfirm = editTextComfirmPassowrd.getText().toString().trim();

        if (pass.equals(pass_comfirm)) {

            //Make sure that they are not small passwords
            if (pass.length() >= 7) {
                passwordGood = true;
                editTextPassword.setError(null);
                editTextPassword.clearFocus();
            } else {
                passwordGood = false;
                editTextPassword.setError("Weak Password!");
            }

        } else {
            passwordGood = false;
            editTextPassword.setError("Password's don't match!");
        }
        return passwordGood;
    }

    boolean isDataGood() {
        return sanitizeName() && sanitizeEmail() && sanitizeCell() && sanitizePassword();
    }

}
