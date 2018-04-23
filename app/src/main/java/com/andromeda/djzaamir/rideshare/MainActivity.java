package com.andromeda.djzaamir.rideshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         new Thread(new Runnable() {

             @Override
             public void run() {

                 try {
                     Thread.sleep(1500L);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }


                 //Check if the user is signed in
            firebaseAuth = FirebaseAuth.getInstance();
            if (firebaseAuth.getCurrentUser() != null){
                //Take to the Core Application Activity
                Intent rideShareCoreActivity =  new Intent(MainActivity.this,RideShareCoreActivity.class);
                startActivity(rideShareCoreActivity);
            }else{
                //Start login/Signup Activity
                Intent welcomeActivityIntent = new Intent(MainActivity.this,WelcomeActivity.class);
                startActivity(welcomeActivityIntent);
            }


            //Dispose off current activity
            finish();
            return;
             }
         }).start();


    }
}
