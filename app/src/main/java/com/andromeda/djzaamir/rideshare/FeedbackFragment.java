package com.andromeda.djzaamir.rideshare;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FeedbackFragment extends Fragment {

    private String u_id;

    private TextView gretting_msg;
    private EditText feedback_msg;
    private Button submit_button;

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

    }
}
