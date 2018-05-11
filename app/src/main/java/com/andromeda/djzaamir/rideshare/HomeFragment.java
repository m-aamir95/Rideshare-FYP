package com.andromeda.djzaamir.rideshare;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;



/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    //region VARS
    //Gui references
    private Button findADriver,shareMyRide;


    //Firebase
    private ValueEventListener driverDataListener;
    private DatabaseReference driverDataNodeRef;

    //Will be true, once Driver Profile is verified in Firebase
    private boolean isDriver = false;
    //Also to really make sure that, ValueEventListener On Firebase DriverDetail Data node
    //has Atleast fired once, i'm going to put another bool here
    //It will be set to true once the ValueEventListener has fired once
    private boolean valueEventListenerFiredOnce = false;
    private final int DRIVER_DETAILS_RESULT =  12;
    //endregion


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Get Reference to Current User's Driver data node
        //If the User doesn't a driver profile then this node will be NULL
        String u_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        driverDataNodeRef = FirebaseDatabase.getInstance().getReference().child("Driver_vehicle_info").child(u_id);

        //setup ValueEventListener for above DatabaseReference
        driverDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                 valueEventListenerFiredOnce = true;
                //Check if any of the driver profile properties are available
                //For simpliciy sake i am going to check for vehicle no
                if (dataSnapshot.child("vehicle_no").getValue() != null){
                    isDriver =  true; //let the App know that, Driver Profile Exist
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Init gui references
        //Since We are in a fragment, we'll have to take slightly different approach to init gui's
        findADriver = (Button)getView().findViewById(R.id.findADriver);
        shareMyRide = (Button)getView().findViewById(R.id.shareMyRide);

        //setup Event listerner's on both buttons
        findADriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //Open Activity to get params for route and then find the matching driver's
                Intent findADriverActivityIntent =  new Intent(getActivity().getApplicationContext(),FindARide.class);
                startActivity(findADriverActivityIntent);
            }
        });

        shareMyRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             //Open Activity to get Route Param's, and then share the ride
             //But first make sure that, Driver Profile exist
             if (!isDriver && valueEventListenerFiredOnce){
                 //Prompt to fill up driver details, in another activity
                 Intent driverDetailsActivityIntent =  new Intent(getActivity().getApplicationContext(),DriverDetailActivity.class);
                 startActivityForResult(driverDetailsActivityIntent,DRIVER_DETAILS_RESULT);
             }else{
                 //Take to driver route/journey enter Activity
                 Intent shareMyRideActivityIntent =  new Intent(getActivity().getApplicationContext(), com.andromeda.djzaamir.rideshare.                                shareMyRide.class);
                 startActivity(shareMyRideActivityIntent);
             }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DRIVER_DETAILS_RESULT && resultCode == Activity.RESULT_OK){
            isDriver = data.getBooleanExtra("driverDetailsOk",false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        driverDataNodeRef.addValueEventListener(driverDataListener);
    }

    @Override
    public void onStop() {
        super.onStop();
       driverDataNodeRef.removeEventListener(driverDataListener);
    }
}
