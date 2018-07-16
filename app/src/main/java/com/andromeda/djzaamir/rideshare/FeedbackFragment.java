package com.andromeda.djzaamir.rideshare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.andromeda.djzaamir.rideshare.utils.InputUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;

public class FeedbackFragment extends Fragment {

    private String u_id;

    private TextView gretting_msg;
    private EditText feedback_msg;
    private Button submit_button;
    private ProgressBar feedback_submission_progressbar;

    public FeedbackFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {




        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feedback, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gretting_msg  =  getView().findViewById(R.id.feedback_greeting_msg);
        feedback_msg  =  getView().findViewById(R.id.feedback_text);
        submit_button =  getView().findViewById(R.id.feedback_submit_button);
        feedback_submission_progressbar = getView().findViewById(R.id.data_submission_progressbar_feedback);

        u_id = FirebaseAuth.getInstance().getCurrentUser().getUid();


         //Load Name and greeting msg into TextBox
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(u_id)
                .child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null){
                    String name =  dataSnapshot.getValue().toString();
                    String greet_msg = "Hey " + name + ", your Feedback is very valuable to us";
                    gretting_msg.setText(greet_msg);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //Event listener for submit button
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (feedback_msg.getText().toString().trim().length() > 0){

                    feedback_submission_progressbar.setVisibility(View.VISIBLE);
                    InputUtils.disableInputControls(submit_button , feedback_msg);
                    feedback_msg.setError(null);

                    String msg_to_submit =  feedback_msg.getText().toString();
                    long timestamp = Calendar.getInstance().getTimeInMillis();

                    Feedback_data feedback_data = new Feedback_data(Long.toString(timestamp) , msg_to_submit);

                    DatabaseReference new_feedback_ref = FirebaseDatabase.getInstance().getReference()
                            .child("Feedback")
                            .child(u_id).push();


                        new_feedback_ref.setValue(feedback_data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            feedback_submission_progressbar.setVisibility(View.GONE);
                            InputUtils.enableInputControls();
                            feedback_msg.setText("");

                            //Show Success Modal
                            AlertDialog.Builder dialog_builder =  new AlertDialog.Builder(getContext());
                            dialog_builder
                                    .setIcon(R.drawable.ic_check_black_success_24dp)
                                    .setTitle("Success!")
                                    .setMessage("Thank you for your Feedback :)")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //Do nothing
                                        }
                                    })
                                    .create().show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        feedback_submission_progressbar.setVisibility(View.GONE);
                            InputUtils.enableInputControls();
                            //Show Failure Modal
                            AlertDialog.Builder dialog_builder =  new AlertDialog.Builder(getContext());
                            dialog_builder
                                    .setIcon(R.drawable.ic_error_black_24dp)
                                    .setTitle("Failure!")
                                    .setMessage("There was some problem\nSubmitting your feedback")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //Do nothing
                                        }
                                    })
                                    .create().show();

                        }
                    });


                }else{
                    feedback_msg.setError("Invalid Feedback!");
                }

            }
        });

    }
}



//Modal Class
class Feedback_data{

    /*
    * ProperName annotation is being used, in order for firebase to serialize this class
    * */

    @PropertyName("timestamp")
    public String timestamp;

    @PropertyName("msg")
    public String msg;

    public Feedback_data(String timestamp, String msg) {
        this.timestamp = timestamp;
        this.msg = msg;
    }
}
