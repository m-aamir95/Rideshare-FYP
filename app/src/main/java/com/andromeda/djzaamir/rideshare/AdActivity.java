package com.andromeda.djzaamir.rideshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.andromeda.djzaamir.rideshare.AdsManager.AdManager;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdActivity extends AppCompatActivity  {

    //vars
    private ImageView ad_image;
    protected TextView ad_text , ad_countdown;
    private ProgressBar ad_progressBar;

    private  String u_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);

        //Init GUI references
        ad_image =  findViewById(R.id.ad_image);
        ad_text  =  findViewById(R.id.ad_text);
        ad_countdown = findViewById(R.id.ad_countdown);
        ad_progressBar = findViewById(R.id.ad_progressbar);

        //Get Driver ID , which will be passed to Customer-Driver Communication modules
        Intent intent_data =  getIntent();
        u_id =  intent_data.getStringExtra("driver_id");


        //Load Ad Data from firebase
        FirebaseDatabase.getInstance().getReference().child("ads/ad_1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null){
                    String description = dataSnapshot.child("description").getValue().toString();
                    String url = dataSnapshot.child("img_url").getValue().toString();

                    //update gui element's
                    Glide.with(getApplicationContext()).load(url).into(ad_image);
                    ad_text.setText(description);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        //Self close timer Invoke
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
                int count =0;
                final int ad_duration = 10; // 10 seconds
                while(count  <= ad_duration){

                 //Update Progress bar and countdown text
                 ad_progressBar.setProgress(count * 10); //count *10 because we are taking progrss from 10% - 100%
                    try {
                        Thread.sleep(1000l); //1 second pause
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count++;
              }

              //Disable ad for certain time
                AdManager.setAdShownStateToShown();
            }
        }).start();
    }


    //Do not allow the user to go back
    @Override
    public void onBackPressed() {
//        super.onBackPressed();  //  , Remove Default Behavior
    }

}
