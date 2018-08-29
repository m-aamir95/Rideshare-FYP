package com.andromeda.djzaamir.rideshare;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.andromeda.djzaamir.rideshare.utils.ButtonUtils;
import com.andromeda.djzaamir.rideshare.utils.InputUtils;
import com.andromeda.djzaamir.rideshare.utils.PasswordManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import static com.andromeda.djzaamir.rideshare.DataSanitization.RideShareUniversalDataSanitizer.sanitizeEmail;
import static com.andromeda.djzaamir.rideshare.DataSanitization.RideShareUniversalDataSanitizer.sanitizePassword;

public class LoginActivity extends AppCompatActivity {

    //Gui references
    private EditText editTextEmail,editTextPassword;
    private ProgressBar loading_spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Establish references to gui
        editTextEmail    =  findViewById(R.id.edittextbox_email_login);
        editTextPassword =  findViewById(R.id.edittextbox_password_login);
        loading_spinner = findViewById(R.id.loading_spinner);
    }

    public void logIn(View view) {
            if (sanitizeAllData()){

          //Disable signin buttin, and change title
            Button login_button  = findViewById(R.id.button_login);
            Button signup_button = findViewById(R.id.embeded_signup_inside_login_activity);

            ButtonUtils.disableAndChangeText(login_button,"Logging in...");

            //disable input controls
            InputUtils.disableInputControls(editTextEmail ,  editTextPassword , signup_button);

            //Fire loading spinner
            loading_spinner.setVisibility(View.VISIBLE);


            String email    =  editTextEmail.getText().toString().trim();
            String password =  editTextPassword.getText().toString();
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnSuccessListener(new                                          OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {

                    String email  = editTextEmail.getText().toString().trim();
                    String pass   = editTextPassword.getText().toString().trim();

                    PasswordManager.saveChanges(email, pass,getApplicationContext());



                    //save password and email Inside our PasswordManager class
                    //you can read PasswordManager docs for why i am doing this
                    PasswordManager.raw_email    = email;
                    PasswordManager.raw_password = pass;

                    //Start Core RideShare Activity
                    Intent rideShareCoreIntent =  new Intent(LoginActivity.this, NavigationDrawer.class);
                    startActivity(rideShareCoreIntent);

                    //Dispose off error
                    editTextEmail.setError(null);

                    loading_spinner.setVisibility(View.GONE);

                    //Dispose this activity
                    finish();
                    return;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loading_spinner.setVisibility(View.GONE);
                    editTextPassword.setText("");
                    editTextEmail.setError("Invalid Email or Password!");

                   ButtonUtils.enableButtonRestoreTitle();
                   InputUtils.enableInputControls();
                }
            });
        }
    }


    boolean sanitizeAllData(){
        return sanitizeEmail(editTextEmail) && sanitizePassword(editTextPassword);
    }

    public void on_signup(View view) {

        //Put email as intent data and start signup Activity
        Intent signupActivityIntent  = new Intent(getApplicationContext(),SignupActivity.class);
        signupActivityIntent.putExtra("potential_email" ,  editTextEmail.getText().toString());
        startActivity(signupActivityIntent);
    }
}
