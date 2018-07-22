package com.andromeda.djzaamir.rideshare;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.andromeda.djzaamir.rideshare.utils.ButtonUtils;
import com.andromeda.djzaamir.rideshare.utils.InputUtils;
import com.andromeda.djzaamir.rideshare.utils.PasswordManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import static com.andromeda.djzaamir.rideshare.DataSanitization.RideShareUniversalDataSanitizer.sanitizePassword;

public class ChangePasswordActivity extends AppCompatActivity {

    //region VARS
    private EditText current_pass, new_pass, new_pass_comfirm;
    private Button submit_button;
    private ProgressBar progressBar;
    private boolean isFirebaseAuthenticationGood = false;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        current_pass     = findViewById(R.id.current_password);
        new_pass         = findViewById(R.id.new_password);
        new_pass_comfirm = findViewById(R.id.new_password_comfirm);
        submit_button    = findViewById(R.id.password_update_button);
        progressBar      = findViewById(R.id.update_data_progress_bar);
    }

    public void on_submit_data(View view) {
        String current_pass_str = current_pass.getText().toString().trim();
        if (!sanitizeGood()) {
            return;
        }

        //Authenticate Password Locally
        if (!LocalPasswordAuthenticationGood(current_pass_str)) {
            current_pass.setError("Invalid Current Password!");
            return;
        } else {
            current_pass.setError(null);
        }

        //Authenticate Via Firebase
        if (!isAuthenticationGood()) {
            return;

        }

        tryToUpdatePassword();
    }

    private void tryToUpdatePassword(){

        //Disable controls
        InputUtils.disableInputControls(current_pass,new_pass , new_pass_comfirm);
        ButtonUtils.disableAndChangeText(submit_button , "Updating...");
        progressBar.setVisibility(View.VISIBLE);

        final String pass_str_to_update = new_pass.getText().toString().trim();

        FirebaseAuth.getInstance().getCurrentUser().updatePassword(pass_str_to_update)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showAlertDialog("Success!"
                                , "Password Updated Successfully"
                                , R.drawable.ic_check_black_success_24dp
                                , "OK"
                                , new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        //Update PasswordManager data
                                        PasswordManager.saveChanges(pass_str_to_update
                                                , PasswordManager.Update_Type.PASSWORD
                                                ,getApplicationContext());

                                        finish();
                                    }
                                }
                                , false);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showAlertDialog("Error"
                                , "Error Updating Password, Please try latter"
                                , R.drawable.ic_error_black_24dp
                                , "OK"
                                , new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                       progressBar.setVisibility(View.GONE);
                                       ButtonUtils.enableButtonRestoreTitle();
                                       InputUtils.enableInputControls();
                                    }
                                }
                                , false);
                    }
                });
    }

    private boolean sanitizeGood() {
        return sanitizePassword(current_pass) && sanitizePassword(new_pass, new_pass_comfirm);
    }

    private boolean LocalPasswordAuthenticationGood(String current_pass_str) {
        return current_pass_str.equals(PasswordManager.raw_password);
    }

    private boolean isAuthenticationGood() {
        AuthCredential credential = EmailAuthProvider.getCredential(PasswordManager.raw_email, PasswordManager.raw_password);

        FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        isFirebaseAuthenticationGood = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //Display Error AlertBox
                        showAlertDialog("Error", "Unable to authenticate, Due to Internal Error, please try latter",
                                R.drawable.ic_error_black_24dp, "OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //Do nothing
                                    }
                                }, false);

                        isFirebaseAuthenticationGood = false;
                    }
                });


        return isFirebaseAuthenticationGood;
    }

    private void showAlertDialog(String header_title, String msg, @DrawableRes int drawable_icon_id, String button_msg, DialogInterface.OnClickListener btn_callback, boolean setCancelableOnOutSideTouch) {

        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(this);
        dialog_builder.setTitle(header_title)
                .setMessage(msg)
                .setIcon(drawable_icon_id)
                .setCancelable(setCancelableOnOutSideTouch)
                .setPositiveButton(button_msg, btn_callback).create().show();
    }
}
