package com.andromeda.djzaamir.rideshare;

import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.andromeda.djzaamir.rideshare.utils.App_Wide_Static_Vars;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

public class DisplayScheduledRideInfoActiviy extends AppCompatActivity {

    private EditText start_point, end_point, start_date, end_date;
    private Button ok_button;
    private String unique_rideScheduled_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_scheduled_ride_info_activiy);

        initGuiReferences();
        unique_rideScheduled_id = App_Wide_Static_Vars.unique_ride_scheduled_id;

        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); //Go back to other Details about Scheduled Ride
            }
        });


        //Start data grab from FirebaseR
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("scheduled_rides")
                .child(unique_rideScheduled_id);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    double start_lat, start_lng, end_lat, end_lng;
                    long start_timestamp, end_timestamp;

                    start_lat = Double.parseDouble(dataSnapshot.child("start_point").child("lat").getValue().toString());
                    start_lng = Double.parseDouble(dataSnapshot.child("start_point").child("lng").getValue().toString());

                    end_lat = Double.parseDouble(dataSnapshot.child("end_point").child("lat").getValue().toString());
                    end_lng = Double.parseDouble(dataSnapshot.child("end_point").child("lng").getValue().toString());

                    start_timestamp = Long.parseLong(dataSnapshot.child("start_end_timestamps").child("start_timestamp").getValue().toString());

                    end_timestamp = Long.parseLong(dataSnapshot.child("start_end_timestamps").child("end_timestamp").getValue().toString());


                    Geocoder addressDecoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                    try {
                        String decoded_addr = addressDecoder.getFromLocation(start_lat, start_lng, 1)
                                .get(0)
                                .getAddressLine(0);
                        start_point.setText(decoded_addr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        String decoded_addr = addressDecoder.getFromLocation(end_lat, end_lng, 1)
                                .get(0)
                                .getAddressLine(0);
                        end_point.setText(decoded_addr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    //Display start date and time
                    parseTimestampAndSetupEdittext(start_timestamp, start_date);

                    //If end timestamp is exist means greater than zero then set it as well
                    if (end_timestamp > 0) {
                        parseTimestampAndSetupEdittext(end_timestamp, end_date);
                    } else {
                        end_date.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private void initGuiReferences() {
        start_point = findViewById(R.id.start_point_txtview);
        end_point = findViewById(R.id.end_point_txtview);
        start_date = findViewById(R.id.start_date_time_textview);
        end_date = findViewById(R.id.return_date_time_textview);
        ok_button = findViewById(R.id.ok);
    }

    private String formatMinutes(int min) {
        if (min < 10) {
            return "0" + String.valueOf(min);
        }
        return String.valueOf(min);
    }

    private void parseTimestampAndSetupEdittext(long timestamp, EditText target_edittext) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);

        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        int hour = c.get(Calendar.HOUR);
        int min = c.get(Calendar.MINUTE);
        int isAm = c.get(Calendar.AM_PM);
        String am_pm_decided_str = isAm == Calendar.AM ? "AM" : "PM";

        String str_representation = day + "-" + (month + 1) + "-" + year + ", " + hour + ":" + formatMinutes(min) + " " + am_pm_decided_str;

        target_edittext.setText(str_representation);
    }
}
