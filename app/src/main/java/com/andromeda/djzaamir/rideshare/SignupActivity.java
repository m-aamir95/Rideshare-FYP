package com.andromeda.djzaamir.rideshare;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

import static com.andromeda.djzaamir.rideshare.DataSanitization.RideShareUniversalDataSanitizer.sanitizeCell;
import static com.andromeda.djzaamir.rideshare.DataSanitization.RideShareUniversalDataSanitizer.sanitizeEmail;
import static com.andromeda.djzaamir.rideshare.DataSanitization.RideShareUniversalDataSanitizer.sanitizeName;
import static com.andromeda.djzaamir.rideshare.DataSanitization.RideShareUniversalDataSanitizer.sanitizePassword;


public class SignupActivity extends AppCompatActivity {

    //GUI references
    private EditText editTextName, editTextEmail, editTextCell, editTextPassword, editTextComfirmPassowrd;
    private Button signupButton;
    private ProgressBar loading_spinner;


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
                    pushOtherSignupDataToFirebaseAndFinish();
                }
            }
        };
    }

    /*
    * Will be called when user's auth status change
    *
    * */
    private void pushOtherSignupDataToFirebaseAndFinish() {
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
                loading_spinner.setVisibility(View.GONE);
                finish();
            }
        });

    }


    public void performSignup(View view) {

        //Run error checks
        if (!isDataGood()) {
            return; //Exit Sign up function
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


    boolean isDataGood() {
        return sanitizeName(editTextName) &&
                sanitizeEmail(editTextEmail) &&
                sanitizeCell(editTextCell) &&
                sanitizePassword(editTextPassword,editTextComfirmPassowrd);
    }

}
