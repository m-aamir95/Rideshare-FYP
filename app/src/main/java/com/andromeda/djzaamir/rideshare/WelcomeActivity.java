package com.andromeda.djzaamir.rideshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    public void openLoginActivity(View view) {
        Intent loginActivityIntent =  new Intent(WelcomeActivity.this,LoginActivity.class);
        startActivity(loginActivityIntent);

        //Do'nt shutdown this activity, if the user fail to login
        //Maybe he/she will come back to this activity for signup
    }

    public void openSignupActivity(View view) {
        Intent signupActiviyIntent = new Intent(WelcomeActivity.this,SignupActivity.class);
        startActivity(signupActiviyIntent);
    }
}
