package com.andromeda.djzaamir.rideshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DisplayDriverDetails extends AppCompatActivity {

    private ImageView driver_image_imageview;
    private TextView driver_name_textview;
    private String u_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_driver_details);

        //Establish references with gui
        driver_image_imageview = findViewById(R.id.driver_image_imageview);
        driver_name_textview =  findViewById(R.id.driver_name);

        //grab id from intent
        Intent  intent_data = getIntent();
        u_id =  intent_data.getStringExtra("id");

        //grab Driver Picture from firebase
        FirebaseDatabase.getInstance().getReference().child("Users").child(u_id).addValueEventListener(new                                      ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null &&  dataSnapshot.getValue() != null){
                    //grab image url
                    String image_url  = dataSnapshot.child("driver_image").getValue().toString();
                    Glide.with(DisplayDriverDetails.this).load(image_url).into(driver_image_imageview);

                    //grab name
                    String d_name = dataSnapshot.child("name").getValue().toString();
                    driver_name_textview.setText(d_name);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        //grab other details

    }
}
