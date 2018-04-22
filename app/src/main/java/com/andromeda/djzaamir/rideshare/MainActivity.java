package com.andromeda.djzaamir.rideshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            @Override
            public void run() {

                long sleep_for = 1500L;

                try {
                    //Sleep for S seconds and then start Welcome Login/Signup activity
                    Thread.sleep(sleep_for);

                    Intent welcomeActivityIntent = new Intent(MainActivity.this,WelcomeActivity.class);
                    startActivity(welcomeActivityIntent);

                    //Dispose off current activity
                    finish();
                    return;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
