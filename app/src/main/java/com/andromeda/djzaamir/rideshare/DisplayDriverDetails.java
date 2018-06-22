package com.andromeda.djzaamir.rideshare;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.andromeda.djzaamir.rideshare.AdsManager.AdManager;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DisplayDriverDetails extends AppCompatActivity {

    //region VARS
    private ImageView driver_image_imageview;
    private TextView driver_name_textview , driver_pickup_address , driver_destination_address,
                     driver_pickup_time,driver_return_time , distance_textview, fair_textview;
    private String u_id;

    private LatLng driver_start_point = null, driver_end_point = null;
    private double journey_Distance;
    private boolean journey_Distance_set = false;
    //endregion



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_driver_details);

        //Establish references with gui
        driver_image_imageview     =  findViewById(R.id.driver_image_imageview);
        driver_name_textview       =  findViewById(R.id.driver_name);
        driver_pickup_address      =  findViewById(R.id.driver_pickup_address);
        driver_destination_address =  findViewById(R.id.driver_destination_address);
        driver_pickup_time         =  findViewById(R.id.driver_pickup_time);
        driver_return_time         =  findViewById(R.id.driver_return_time);
        distance_textview          =  findViewById(R.id.distance_textView);
        fair_textview              =  findViewById(R.id.fair_textView);

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

                    driver_start_point = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                    calcAndUpdateDistance();
                    calcAndUpdateFair();

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

                    driver_end_point = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                    calcAndUpdateDistance();
                    calcAndUpdateFair();

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
        FirebaseDatabase.getInstance().getReference().child("available_drivers_time_info").child(u_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null && dataSnapshot.getValue() != null){
                    Calendar c = Calendar.getInstance();

                    //Fetch start and end timestamp from firebase
                    Long start_time = Long.parseLong(dataSnapshot.child("start_time_stamp").getValue().toString());
                    Long end_time   = Long.parseLong(dataSnapshot.child("end_time_stamp").getValue().toString());

                    //setup start textView with timestamp
                    parseTimestampAndSetupTextView(start_time , driver_pickup_time);

                    //If return time exist then set it up
                    if (end_time > 0 ){
                        parseTimestampAndSetupTextView(end_time, driver_return_time);
                    }else{
                        driver_return_time.setText("Not Specified");
                    }



                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    private void calcAndUpdateFair() {
      if (journey_Distance_set){
        int fair_per_km = 10;

        //Distance fair
        double calculated_fair = fair_per_km * journey_Distance;

        //Base Fair
        calculated_fair += 20;

        String formatted_fair = new DecimalFormat("#0").format(calculated_fair);

        fair_textview.setText("Fair: " + formatted_fair + " Rs");
      }
    }

    private void calcAndUpdateDistance() {
        if (driver_start_point != null && driver_end_point != null){

            //enable fair calculation
            journey_Distance_set = true;

           journey_Distance = getDistance(driver_start_point ,  driver_end_point);

           journey_Distance /= 1000; //km conversion

           //Perform Decimal Formatting before sending to display
           String formatted_distance = new DecimalFormat("#0.0").format(journey_Distance);

           distance_textview.setText("Distance: " + formatted_distance + " km");
        }
    }

    private void parseTimestampAndSetupTextView(long timestamp, TextView target_textview){
        Calendar c =  Calendar.getInstance();
        c.setTimeInMillis(timestamp);

        int day   = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year  = c.get(Calendar.YEAR);

        int hour  = c.get(Calendar.HOUR);
        int min   = c.get(Calendar.MINUTE);

        String str_representation = day + "-" + (month+1) + "-" + year + ", " + hour + ":" + formatMinutes(min);

        target_textview.setText(str_representation);
    }

    private String formatMinutes(int min) {
        if (min < 10){
            return "0" + String.valueOf(min);
        }
        return String.valueOf(min);
    }


    public void contact_button(View view) {

         //Check if Ad has been Dislayed And not Waiting to be displayed
        if (AdManager.adShown() == false){

            //show the add , pass driver ID to it via intent
            Intent adActivityIntent =  new Intent(getApplicationContext(), AdActivity.class);
            adActivityIntent.putExtra("driver_id" ,  u_id);

            startActivity(adActivityIntent);

        }else{

            //Directly take to Driver-Customer Communication Module
            Intent chatActivityIntent =  new Intent(getApplicationContext() , ChatActivity.class);
            chatActivityIntent.putExtra("driver_id" ,  u_id);

            startActivity(chatActivityIntent);

        }
    }



    //Calculate Distance between two points On earth using Haversine formula
    double degreeToRad(double degrees){
        return degrees * Math.PI / 180;
    }
    /*
    * Here we can consider point_A as Starting Point of the Journey
    * And point_B as the Ending point of the journey
    * */
    double getDistance(LatLng  point_A ,  LatLng point_B){
        double R = 6378137; // Earth’s mean radius in meter;

        //Subtract Both Distance's from each other And Convert them to Radians
        double dLat =  degreeToRad(point_B.latitude  - point_A.latitude);
        double dLng =  degreeToRad(point_B.longitude - point_A.longitude);

        //Some weired mathematics
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(degreeToRad(point_A.latitude)) * Math.cos(degreeToRad(point_B.latitude)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);

        //More Weired mathematics
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));


        double d = R * c;
        return d;
    }
}
