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
    }

    public void openSignupActivity(View view) {
        Intent signupActiviyIntent = new Intent(WelcomeActivity.this,SignupActivity.class);
        startActivity(signupActiviyIntent);

        //Dispose off current activity
        finish();
        return;
    }
}
