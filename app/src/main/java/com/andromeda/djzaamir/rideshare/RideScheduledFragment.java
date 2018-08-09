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
import com.andromeda.djzaamir.rideshare.utils.ButtonUtils;
import com.andromeda.djzaamir.rideshare.utils.InputUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class RideScheduledFragment extends Fragment {

    private TextInputLayout name_hint;
    private TextView greetings_msg_TextView;
    private EditText name_editText, vehicle_no_editText, vehicle_color_editText;
    private Button ride_details_button, finish_button;
    private String other_person_id;
    private boolean is_this_driver_id;

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
        if (App_Wide_Static_Vars.unique_ride_scheduled_id != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("scheduled_rides")
                    .child(App_Wide_Static_Vars.unique_ride_scheduled_id)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                                if (dataSnapshot.child("driver_id").getValue() != null &&
                                        dataSnapshot.child("customer_id").getValue() != null) {
                                    final String driver_id = dataSnapshot.child("driver_id").getValue().toString();
                                    final String customer_id = dataSnapshot.child("customer_id").getValue().toString();

                                /*
                                * If driver then return customer
                                * if customer then return driver
                                * */
                                    other_person_id = u_id.equals(driver_id) ? customer_id : driver_id;
                                    is_this_driver_id = u_id.equals(driver_id);

                                    //Display Greetings
                                    //region Data grab for username
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("Users")
                                            .child(u_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                                                String name = dataSnapshot.child("name").getValue().toString();
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
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
        //endregion

        //region Button Listeners
        ride_details_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ride_details_intent = new Intent(getContext(), DisplayScheduledRideInfoActiviy.class);
                startActivity(ride_details_intent);
            }
        });

        finish_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ButtonUtils.disableAndChangeText(finish_button,"Processing...");
                InputUtils.disableInputControls(ride_details_button);

                //Delete Unique_ride ids from both the driver and customer
                //Remove for this person
                FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                        .child(u_id)
                        .child("scheduled_ride_id").removeValue();

                //Remove from other party
                FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                        .child(other_person_id)
                        .child("scheduled_ride_id").removeValue();

                //Delete is booked node from driver profile
                String target_driver_id = is_this_driver_id == true ? u_id : other_person_id;
                FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                        .child(target_driver_id)
                        .child("is_booked")
                        .removeValue();

                //Delete Requested_By field from shared chat node
                FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                        .child(u_id)
                        .child("chat_history")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                                    for (DataSnapshot chat_history_id_node :
                                            dataSnapshot.getChildren()) {
                                        if (chat_history_id_node.child("other_user_id").getValue().toString().equals(other_person_id)) {
                                            //Grab chat id
                                            String unique_chat_id =  chat_history_id_node.getKey();
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("chats")
                                                    .child(unique_chat_id)
                                                    .child("REQUESTED_BY").removeValue();
                                            break;
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                //Delete Ride-Scheduled Data
                FirebaseDatabase.getInstance().getReference()
                        .child("scheduled_rides")
                        .child(App_Wide_Static_Vars.unique_ride_scheduled_id).removeValue();


                //Reset App_wide.RideScheduled_Unqiue_id
                App_Wide_Static_Vars.unique_ride_scheduled_id = null;
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
        finish_button = getView().findViewById(R.id.ride_finish_button);
    }
}
