package com.andromeda.djzaamir.rideshare;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.andromeda.djzaamir.rideshare.DataSanitization.RideShareUniversalDataSanitizer;
import com.andromeda.djzaamir.rideshare.utils.ButtonUtils;
import com.andromeda.djzaamir.rideshare.utils.InputUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import static com.andromeda.djzaamir.rideshare.DataSanitization.RideShareUniversalDataSanitizer.sanitizeEmail;
import static com.andromeda.djzaamir.rideshare.DataSanitization.RideShareUniversalDataSanitizer.sanitizePassword;

public class ChangeSettingsActivity extends AppCompatActivity {

    //region VARS
    private TextView content_header_textview, content_description_textview;
    private EditText user_input_editext;
    private Button submit_button;
    private ProgressBar progressBar;

    private String content_header_str;
    private String content_desc_str;
    private String intent_type;

    private boolean most_recent_user_data_loaded = false; //TO make sure the most recent user data exist, this will also
    //make sure that try_to_send_data fires again as soon as data is loaded
    //from firebase
    private boolean pending_request_to_call_Update_data = false;
    private boolean email_pass_verfication_state = false;
    private boolean pass_email_being_authenticated = false;


    private String u_id;
    private Current_User_data current_user_data;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_settings);


        u_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //Load Most updated recent data
        FirebaseDatabase.getInstance().getReference("Users").child(u_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    current_user_data = new Current_User_data();

                    current_user_data.email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    current_user_data.name = dataSnapshot.child("name").getValue().toString();
                    current_user_data.cell = dataSnapshot.child("cell").getValue().toString();

                    most_recent_user_data_loaded = true;

                    //Check if there is a pending request to update data
                    if (pending_request_to_call_Update_data) {
                        try_to_update_data();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        content_header_textview = findViewById(R.id.content_header_msg);
        content_description_textview = findViewById(R.id.content_description);
        user_input_editext = findViewById(R.id.content_editextbox);
        submit_button = findViewById(R.id.content_submit_button);
        progressBar = findViewById(R.id.update_data_progress_bar);


        Intent incoming_intent = getIntent();

        //Error-Check
        makeSureIntentHasValidData(incoming_intent);

        intent_type = incoming_intent.getStringExtra("intent_type");
        content_header_str = incoming_intent.getStringExtra("content_header_title");
        content_desc_str = incoming_intent.getStringExtra("content_description");

        updateGUIAccordingToIntentType();

    }

    private void updateGUIAccordingToIntentType() {
        content_header_textview.setText(content_header_str);
        content_description_textview.setText(content_desc_str);

        //Put hint according to intent type
        if (intent_type.equals("NAME_CHANGE")) {

            user_input_editext.setHint("Enter New Full Name");
        } else if (intent_type.equals("EMAIL_CHANGE")) {

            user_input_editext.setHint("Enter New Email");
            user_input_editext.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        } else if (intent_type.equals("CELL_CHANGE")) {

            user_input_editext.setHint("Enter New Cell number");
            user_input_editext.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

    }

    public void on_submit_data(View view) {
        try_to_update_data();
    }

    private void try_to_update_data() {
        if (sanitizeData()) {
            if (most_recent_user_data_loaded) {


                //For data Science reasons
                New_Modified_Data_History new_modified_dataHistory = null;

                //Update newly modified data to proper locations
                if (intent_type.equals("NAME_CHANGE")) {

                    new_modified_dataHistory = new New_Modified_Data_History(intent_type, current_user_data.name, user_input_editext.getText().toString());

                    updateDataAtFirebase("name", "Name", false);

                } else if (intent_type.equals("EMAIL_CHANGE")) {

                    new_modified_dataHistory = new New_Modified_Data_History(intent_type, current_user_data.email, user_input_editext.getText().toString());


                    updateDataAtFirebase("email", "Email", true);

                } else if (intent_type.equals("CELL_CHANGE")) {

                    new_modified_dataHistory = new New_Modified_Data_History(intent_type, current_user_data.cell, user_input_editext.getText().toString());

                    updateDataAtFirebase("cell", "Cell No", false);

                }


                //Upload current and modified  user data image under user_id node with event type
                //This is being done just for record keeping for maintaining a user data image for data science projects
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(u_id)
                        .child("old_user_data").push();

                reference.child("data").setValue(new_modified_dataHistory);
                reference.child("server_timestamp").setValue(ServerValue.TIMESTAMP);


            } else {
                pending_request_to_call_Update_data = true;
            }
        }
    }


    private void updateDataAtFirebase(String node_name, final String alertDialog_msg_filler_Text, boolean isEmail) {


        progressBar.setVisibility(View.VISIBLE);
        ButtonUtils.disableAndChangeText(submit_button, "Updating...");
        InputUtils.disableInputControls(user_input_editext);

        String new_data_str = user_input_editext.getText().toString().trim();
        Task<Void> update_background_task = null;

        if (isEmail == false) { //Then access User Data Node at firebase

            DatabaseReference update_reference = FirebaseDatabase.getInstance().getReference().child("Users").child(u_id);
            update_background_task = update_reference.child(node_name).setValue(new_data_str);

        } else { //Access Email Node from FirebaseAuth

            //Fresh Authentication Needed in order to change Email
            //Please Don't look at this function , coz here lies Madness
            getEmailAndPasswordAgainViaDialogAlertGood(new_data_str);
            return;

        }

        //Execute Task , wont fire in case User not Re authenticated
        if (update_background_task != null) {
            update_background_task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    showAlertDialog("SUCCESS",
                            alertDialog_msg_filler_Text + " updated Successfully",
                            R.drawable.ic_check_black_success_24dp,
                            "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            }, false);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showAlertDialog("FAILURE",
                            "Error updating " + alertDialog_msg_filler_Text + ".Please try again...",
                            R.drawable.ic_error_black_24dp,
                            "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    progressBar.setVisibility(View.GONE);
                                    ButtonUtils.enableButtonRestoreTitle();
                                    InputUtils.enableInputControls();
                                }
                            }, false);
                }
            });
        }
    }

    private boolean getEmailAndPasswordAgainViaDialogAlertGood(final String email_to_update) {
        //Prepare layout
        LayoutInflater login_dialog_inflator = getLayoutInflater();
        View login_layout_view = login_dialog_inflator.inflate(R.layout.login_dialog_layout, null);

        final EditText email_edittext = login_layout_view.findViewById(R.id.login_dialog_email_editviews);
        final EditText pass_edittext = login_layout_view.findViewById(R.id.login_dialog_password_editviews);

        AlertDialog login_dialog = new AlertDialog.Builder(this)
                .setTitle("Authentication Required!")
                .setView(login_layout_view)
                .setCancelable(false)
                .setIcon(R.drawable.ic_priority_info_high_black_24dp)
                .setPositiveButton("Authenticate", null) //null Coz need to disable auto-dismiss
                .setNegativeButton("Cancel", null).create(); //null Coz need to disable auto-dismiss , we override it in following code


        //Override buttons click functionality
        login_dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                final Button button_positive = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                final Button button_negative = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEGATIVE);


                button_positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (pass_email_being_authenticated == false) {
                            //Data sanitize
                            if (sanitizeEmail(email_edittext) && sanitizePassword(pass_edittext)) {

                                pass_email_being_authenticated = true;

                                button_positive.setEnabled(false);
                                final String btn_text = button_positive.getText().toString();
                                button_positive.setText("Authenticating...");
                                button_negative.setEnabled(false);
                                email_edittext.setEnabled(false);
                                pass_edittext.setEnabled(false);

                                AuthCredential credential = EmailAuthProvider.getCredential(email_edittext.getText().toString().trim(),
                                        pass_edittext.getText().toString().trim());


                                FirebaseAuth.getInstance().getCurrentUser()
                                        .reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        email_pass_verfication_state = true;
                                        FirebaseAuth.getInstance().getCurrentUser().updateEmail(email_to_update)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        progressBar.setVisibility(View.GONE);
                                                        ButtonUtils.enableButtonRestoreTitle();
                                                        InputUtils.enableInputControls();
                                                        showAlertDialog("SUCCESS",
                                                                "Email updated Successfully",
                                                                R.drawable.ic_check_black_success_24dp,
                                                                "OK",
                                                                new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                                        finish();
                                                                    }
                                                                }, false);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                showAlertDialog("FAILURE",
                                                        "Error updating Email.Please try again...",
                                                        R.drawable.ic_error_black_24dp,
                                                        "OK",
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                progressBar.setVisibility(View.GONE);
                                                                ButtonUtils.enableButtonRestoreTitle();
                                                                InputUtils.enableInputControls();
                                                            }
                                                        }, false);
                                            }
                                        });
                                        ;
                                        dialogInterface.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pass_email_being_authenticated = false;
                                        pass_edittext.setText("");
                                        email_edittext.setError("Invalid Username Or Password!");

                                        button_positive.setEnabled(true);
                                        button_positive.setText(btn_text);
                                        button_negative.setEnabled(true);
                                        email_edittext.setEnabled(true);
                                        pass_edittext.setEnabled(true);


                                    }
                                });
                            }
                        }
                    }
                });


                button_negative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAlertDialog("Info", "Re authentication Required!\nIn order to update email",
                                R.drawable.ic_error_black_24dp, "OK", null, false);
                        progressBar.setVisibility(View.GONE);
                        ButtonUtils.enableButtonRestoreTitle();
                        InputUtils.enableInputControls();
                        dialogInterface.dismiss();
                    }
                });

            }
        });

        login_dialog.show();


        return email_pass_verfication_state;
    }

    private boolean sanitizeData() {
        boolean dataGood = false;
        //Put hint according to intent type
        if (intent_type.equals("NAME_CHANGE")) {

            dataGood = RideShareUniversalDataSanitizer.sanitizeName(user_input_editext);

        } else if (intent_type.equals("EMAIL_CHANGE")) {

            dataGood = sanitizeEmail(user_input_editext);

        } else if (intent_type.equals("CELL_CHANGE")) {

            dataGood = RideShareUniversalDataSanitizer.sanitizeCell(user_input_editext);

        }
        return dataGood;
    }

    private void makeSureIntentHasValidData(Intent incoming_intent) {
        if (incoming_intent.getStringExtra("intent_type") == null) {
            showAlertDialog("Error", "Failure in changing Settings\nError Code: 1000", R.drawable.ic_error_black_24dp, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
        }
        if (incoming_intent.getStringExtra("content_header_title") == null) {
            showAlertDialog("Error", "Failure in changing Settings\nError Code: 1001", R.drawable.ic_error_black_24dp, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
        }
        if (incoming_intent.getStringExtra("content_description") == null) {
            showAlertDialog("Error", "Failure in changing Settings\nError Code: 1002", R.drawable.ic_error_black_24dp, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
        }
    }


    private void showAlertDialog(String header_title, String msg, @DrawableRes int drawable_icon_id, String button_msg, DialogInterface.OnClickListener btn_callback) {
        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(this);
        dialog_builder.setTitle(header_title)
                .setMessage(msg)
                .setIcon(drawable_icon_id)
                .setPositiveButton(button_msg, btn_callback).create().show();
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

//Data Model Class, To keep track of old user data
//This can be useful for data science projects
//Such as , to calculate Resemblebance between Username , Email , cell
//Or How much does a User change His/Her Name e.g Old_Name =  Muhammad Aamir , new_name = Aamir
//Following model class will be used to keep track of such changes
class New_Modified_Data_History {

    @PropertyName("event_type")
    public String event_type; //To let us know what is being changed

    @PropertyName("old_data")
    public String old_data; //To let us know what is being changed

    @PropertyName("new_data")
    public String new_data; //To let us know what is being changed

    public New_Modified_Data_History() {
    }

    public New_Modified_Data_History(String event_type, String old_data, String new_data) {
        this.event_type = event_type;
        this.old_data = old_data;
        this.new_data = new_data;
    }
}

//Simple User data container for ease of use
class Current_User_data {

    public String name;
    public String email;
    public String cell;


    public Current_User_data() {
    }

    public Current_User_data(String name, String email, String cell) {
        this.name = name;
        this.email = email;
        this.cell = cell;
    }
}