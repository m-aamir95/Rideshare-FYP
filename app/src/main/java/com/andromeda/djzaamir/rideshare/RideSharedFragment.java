package com.andromeda.djzaamir.rideshare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class RideSharedFragment extends Fragment {

    //region VARS
    private EditText start_point, end_point, start_date, end_date;
    private Button edit_ride_button, cancel_ride_button;
    /*
    * 1) Start Point data node of this user In Firebase
    * 2) End Point data node of this user In Firebase
    * 3) Date and time Info, node of this user In Firebase
    * */
    private DatabaseReference start_point_loc_Node_ref, end_point_loc_Node_ref,
            date_and_time_Node_ref;

    //endregion
    public RideSharedFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ride_shared, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Establish gui references
        start_point = getActivity().findViewById(R.id.start_point_txtview);
        end_point = getActivity().findViewById(R.id.end_point_txtview);
        start_date = getActivity().findViewById(R.id.start_date_time_textview);
        end_date = getActivity().findViewById(R.id.return_date_time_textview);
        edit_ride_button = getActivity().findViewById(R.id.edit_ride_btn);
        cancel_ride_button = getActivity().findViewById(R.id.cancel_ride_btn);

        final String u_id = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        edit_ride_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareMyRideActivity = new Intent(getContext(), shareMyRide.class);
                startActivity(shareMyRideActivity);
            }
        });

        cancel_ride_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder dialoge = new AlertDialog.Builder(getActivity());
                dialoge.setTitle("Alert!")
                        .setMessage("Are you sure? \nThis will Stop Sharing your Ride")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Remove Start Position from firebase
                                FirebaseDatabase.getInstance().getReference().child("available_drivers_start_point").child(u_id).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("available_drivers_end_point").child(u_id).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("available_drivers_time_info").child(u_id).removeValue();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Do nothing
                            }
                        }).create().show();

            }
        });


        //Init Different firebase references
        start_point_loc_Node_ref = FirebaseDatabase.getInstance().getReference().child("available_drivers_start_point").child(u_id).child("l");
        end_point_loc_Node_ref = FirebaseDatabase.getInstance().getReference().child("available_drivers_end_point").child(u_id).child("l");
        date_and_time_Node_ref = FirebaseDatabase.getInstance().getReference().child("available_drivers_time_info").child(u_id);


        //Try to get different type of data from firebase
        start_point_loc_Node_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    //Extract data
                    Geocoder start_point_address = new Geocoder(getContext(), Locale.getDefault());

                    String latitude = dataSnapshot.child("0").getValue().toString();
                    String longitude = dataSnapshot.child("1").getValue().toString();
                    try {
                        List<Address> address = start_point_address.getFromLocation(Double.parseDouble(latitude), Double.parseDouble(longitude), 1);
                        start_point.setText(address.get(0).getAddressLine(0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Get End point location and display it to EditText
        end_point_loc_Node_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    //Extract data
                    Geocoder start_point_address = new Geocoder(getContext(), Locale.getDefault());
                    String latitude = dataSnapshot.child("0").getValue().toString();
                    String longitude = dataSnapshot.child("1").getValue().toString();
                    try {
                        List<Address> address = start_point_address.getFromLocation(Double.parseDouble(latitude), Double.parseDouble(longitude), 1);
                        end_point.setText(address.get(0).getAddressLine(0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Get start and date and time and display it in EditextBoxes
        date_and_time_Node_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    //Get the timestamps
                    Long start_timestamp = Long.parseLong(dataSnapshot.child("start_time_stamp").getValue().toString());
                    Long end_timestamp = Long.parseLong(dataSnapshot.child("end_time_stamp").getValue().toString());

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
