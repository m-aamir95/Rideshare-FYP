package com.andromeda.djzaamir.rideshare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class SettingsFragment extends Fragment {

    private String u_id;
    private String u_email;
    private TextView name_textview ,  email_textview ,  cell_textview;
    private LinearLayout name_layout ,  email_layout , cell_layout, password_layout;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        u_id    = FirebaseAuth.getInstance().getCurrentUser().getUid();
        u_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        name_textview  =  getView().findViewById(R.id.person_name_textview);
        email_textview =  getView().findViewById(R.id.person_email_textview);
        cell_textview  =  getView().findViewById(R.id.person_cell_textview);


        name_layout        = getView().findViewById(R.id.name_layout);
        email_layout       = getView().findViewById(R.id.email_layout);
        cell_layout        = getView().findViewById(R.id.cell_layout);
        password_layout    = getView().findViewById(R.id.password_layout);

        //Initiate data load
        FirebaseDatabase.getInstance().getReference().child("Users").child(u_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              if (dataSnapshot != null && dataSnapshot.getValue() != null){

                  name_textview.setText(dataSnapshot.child("name").getValue().toString());
                  cell_textview.setText(dataSnapshot.child("cell").getValue().toString());

                  email_textview.setText(u_email);


              }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        //Put event listeners for different layout sections from them to act as buttons
        name_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              startIntentForSettingChange("Update your Name" ,
                      "Please use your real `Full Name` , Using a fake name may result in account termination",
                      "NAME_CHANGE");
            }
        });



        email_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startIntentForSettingChange("Update your Email" ,
                      "Please use a valid Email, This will be used incase you forget your password",
                      "EMAIL_CHANGE");
            }
        });


        cell_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  startIntentForSettingChange("Update your Cell Number" ,
                      "Please use a valid Cell Number, Because this will be used to contact you for any complaints or queries",
                      "CELL_CHANGE");
            }
        });



        password_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });



    }

    private void  startIntentForSettingChange(String header_title, String description , String intent_type){
        Intent settingsUpdateIntent = new Intent(getActivity().getApplicationContext() ,ChangeSettingsActivity.class);
        settingsUpdateIntent.putExtra("content_header_title" , header_title);
        settingsUpdateIntent.putExtra("content_description" , description);
        settingsUpdateIntent.putExtra("intent_type" , intent_type);
        startActivity(settingsUpdateIntent);
    }
}
