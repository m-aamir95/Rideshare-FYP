package com.andromeda.djzaamir.rideshare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class ChatActivity extends AppCompatActivity {

    private String driver_id , u_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        //Grab driver id from intent
        driver_id =  getIntent().getStringExtra("driver_id");
        u_id = FirebaseAuth.getInstance().getCurrentUser().getUid();



    }
}
