package com.andromeda.djzaamir.rideshare;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DisplayDriverDetails extends AppCompatActivity {

    private ImageView driver_image_imageview;
    private TextView driver_name_textview , driver_pickup_address , driver_destination_address;
    private String u_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_driver_details);

        //Establish references with gui
        driver_image_imageview     =  findViewById(R.id.driver_image_imageview);
        driver_name_textview       =  findViewById(R.id.driver_name);
        driver_pickup_address      =  findViewById(R.id.driver_pickup_address);
        driver_destination_address =  findViewById(R.id.driver_destination_address);

        //grab id from intent
        Intent  intent_data = getIntent();
        u_id =  intent_data.getStringExtra("id");

        //grab Driver Picture from firebase
        FirebaseDatabase.getInstance().getReference().child("Users").child(u_id).addValueEventListener(new                                                      ValueEventListener() {
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



        //grab Driver Pickup and drop-off loction
        FirebaseDatabase.getInstance().getReference().child("available_drivers_start_point").child(u_id).child("l").addValueEventListener(new                   ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null){
                    String lat =  dataSnapshot.child("0").getValue().toString();
                    String lng =  dataSnapshot.child("1").getValue().toString();
                    Geocoder decodeLatLng =  new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> addresses =  decodeLatLng.getFromLocation(Double.parseDouble(lat) , Double.parseDouble(lng) , 1);

                        //set address to text field
                        driver_pickup_address.setText(addresses.get(0).getAddressLine(0).toString());
                        driver_pickup_address.setSelected(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child("available_drivers_end_point").child(u_id).child("l").addValueEventListener(new                   ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null){
                    String lat =  dataSnapshot.child("0").getValue().toString();
                    String lng =  dataSnapshot.child("1").getValue().toString();
                    Geocoder decodeLatLng =  new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> addresses =  decodeLatLng.getFromLocation(Double.parseDouble(lat) , Double.parseDouble(lng) , 1);

                        //set address to text field
                        driver_destination_address.setText(addresses.get(0).getAddressLine(0).toString());
                        driver_destination_address.setSelected(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //get date and time
        FirebaseDatabase.getInstance().getReference().child("available_drivers_time_info").child(u_id);

    }
}
