package com.andromeda.djzaamir.rideshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.andromeda.djzaamir.rideshare.utils.PasswordManager;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Remove title bar and notification bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                //Check if the user is signed in
                firebaseAuth = FirebaseAuth.getInstance();
                if (firebaseAuth.getCurrentUser() != null) {


                    //Load Data Into PasswordManger via sharedPreferences ,we can also prompt user to re-login
                    //if this data doesnt exist
                    PasswordManager.loadData(getApplicationContext());


                    //Take to the Core Application Activity
                    Intent rideShareCoreActivity = new Intent(MainActivity.this, NavigationDrawer.class);
                    startActivity(rideShareCoreActivity);
                } else {


                    //Start login/Signup Activity
                    Intent welcomeActivityIntent = new Intent(MainActivity.this, WelcomeActivity.class);
                    startActivity(welcomeActivityIntent);
                }


                //Dispose off current activity
                finish();
                return;
            }
        }).start();


    }
}
