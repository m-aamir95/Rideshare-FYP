package com.andromeda.djzaamir.rideshare;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.andromeda.djzaamir.rideshare.utils.App_Wide_Static_Vars;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class RideScheduledFragment extends Fragment {

    private TextInputLayout name_hint;
    private TextView greetings_msg_TextView;
    private EditText name_editText, vehicle_no_editText, vehicle_color_editText;
    private Button ride_details_button, chat_button, finish_button;


    public RideScheduledFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ride_scheduled, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initGuiReferences();

        final String u_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //region Firebase DataGrab
        FirebaseDatabase.getInstance().getReference()
                .child("scheduled_rides")
                .child(App_Wide_Static_Vars.unique_ride_scheduled_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                            String driver_id = dataSnapshot.child("driver_id").getValue().toString();
                            String customer_id = dataSnapshot.child("customer_id").getValue().toString();

                            //Display Greetings
                            //region Data grab for username
                            FirebaseDatabase.getInstance().getReference()
                                    .child("Users")
                                    .child(u_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot != null && dataSnapshot.getValue() != null){
                                        String name =  dataSnapshot.child("name").getValue().toString();
                                        greetings_msg_TextView.setText(name + ", your Ride has been Scheduled");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            //endregion


                            //region Data Grab for driver vehicle Data
                            FirebaseDatabase.getInstance().getReference()
                                    .child("Driver_vehicle_info")
                                    .child(driver_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                                        String v_no = dataSnapshot.child("vehicle_no").getValue().toString();
                                        String v_color = dataSnapshot.child("vehicle_color").getValue().toString();
                                        vehicle_no_editText.setText(v_no);
                                        vehicle_color_editText.setText(v_color);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            //endregion


                            if (u_id.equals(driver_id)) { //Current user is a driver
                                name_hint.setHint("Customer Name");
                                //region Data grab for username
                                FirebaseDatabase.getInstance().getReference()
                                        .child("Users")
                                        .child(customer_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                                            String name = dataSnapshot.child("name").getValue().toString();
                                            name_editText.setText(name);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                //endregion
                            } else { //is a customer
                                name_hint.setHint("Driver Name");
                                //region Data grab for username
                                FirebaseDatabase.getInstance().getReference()
                                        .child("Users")
                                        .child(driver_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                                            String name = dataSnapshot.child("name").getValue().toString();
                                            name_editText.setText(name);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                //endregion
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        //endregion

        //region Button Listeners
         ride_details_button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent ride_details_intent =  new Intent(getContext() , DisplayScheduledRideInfoActiviy.class);
                 startActivity(ride_details_intent);
             }
         });
        //endregion
    }


    private void initGuiReferences() {
        name_hint = getView().findViewById(R.id.name_hint);
        greetings_msg_TextView = getView().findViewById(R.id.grettings_msg);
        name_editText = getView().findViewById(R.id.name);
        vehicle_no_editText = getView().findViewById(R.id.vehicle_no);
        vehicle_color_editText = getView().findViewById(R.id.vehicle_colour);
        ride_details_button = getView().findViewById(R.id.ride_details_button);
        chat_button = getView().findViewById(R.id.chat_button);
        finish_button = getView().findViewById(R.id.ride_finish_button);
    }
}
