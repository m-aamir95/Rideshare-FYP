package com.andromeda.djzaamir.rideshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AdActivity extends AppCompatActivity {

    //vars
    private ImageView ad_image;
    private TextView ad_text;
    private ProgressBar ad_progressBar;

    private  String u_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);

        //Init GUI references
        ad_image =  findViewById(R.id.ad_image);
        ad_text  =  findViewById(R.id.ad_text);
        ad_progressBar = findViewById(R.id.ad_progressbar);

        //Get Driver ID , which will be passed to Customer-Driver Communication modules
        Intent intent_data =  getIntent();
        u_id =  intent_data.getStringExtra("driver_id");

        try {
            callTimedAdTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    /*
        * Responsible to show this ad for a certain period of time then
        * Set Ad Shown state in AdManager
        * Close this ad
        * switch to Customer-driver module
    * */
    private void callTimedAdTermination() throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count =1;
                final int ad_duration = 10; // 10 seconds
                while(count  <= ad_duration){

                 //Update Progress bar
                 ad_progressBar.setProgress(count * 10); //count *10 because we are taking progrss from 10% - 100%

                    try {
                        Thread.sleep(1000l); //1 second pause
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count++;
              }
            }
        }).start();
    }


    //Do not allow the user to go back
    @Override
    public void onBackPressed() {
//        super.onBackPressed();    , Remove Default Behavior
    }
}
