package com.andromeda.djzaamir.rideshare;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.andromeda.djzaamir.rideshare.RideshareLocationManager.RideShareLocationManager;
import com.andromeda.djzaamir.rideshare.RideshareLocationManager.onLocationUpdateInterface;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    //region VARS
    //Gui references
    private Button findADriver,shareMyRide;
    private GoogleMap mMap;

    //Firebase
    private ValueEventListener driverDataListener;
    private DatabaseReference driverDataNodeRef;

    //Will be true, once Driver Profile is verified via Firebase
    private boolean isDriver = false;

    //Also to really make sure that, ValueEventListener On Firebase DriverDetail Data node
    //has At least fired once, i'm going to put another bool here
    //It will be set to true once the ValueEventListener has fired once
    private boolean valueEventListenerFiredOnce = false;
    private final int DRIVER_DETAILS_RESULT =  12; //For intent result

    private RideShareLocationManager rideShareLocationManager;

    private final int REQ_FINE_LOC_PERMISSION_TAG = 3;
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
                //For simplicity sake i am going to check for vehicle no
                if (dataSnapshot.child("vehicle_no").getValue() != null){
                    isDriver =  true; //let the App know that, Driver Profile Exist
                }else{
                    isDriver = false;
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

        checkIfUserHasGivenLocationAccessPermissions();
        rideShareLocationManager = new RideShareLocationManager();
        rideShareLocationManager.setOnLocationUpdate(new onLocationUpdateInterface() {
            @Override
            public void onLocationUpdate(LatLng latLng) {

                rideShareLocationManager.stopLocationUpdates();

                LatLng updated_loc = new LatLng(latLng.latitude,latLng.longitude);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(updated_loc, 16));
                mMap.addMarker(new MarkerOptions().position(updated_loc));
            }
        } ,  getView().getContext());



        //Init gui references
        //Since We are in a fragment, we'll have to take slightly different approach to init gui's
        findADriver = (Button)getView().findViewById(R.id.findADriver);
        shareMyRide = (Button)getView().findViewById(R.id.shareMyRide);

         //init google maps
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.home_screen_map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
               mMap =  googleMap;
            }
        });

        //setup Event listener's on both buttons
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

    private void checkIfUserHasGivenLocationAccessPermissions(){
     if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) !=                                 PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission                      .ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
      {
       //Ask for permission
       ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},                      REQ_FINE_LOC_PERMISSION_TAG);

      }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_FINE_LOC_PERMISSION_TAG:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Cool FINE Location Access Provided
                } else {
                    //Because location access is required
                    Toast.makeText(getContext(), "Location Services Are Required...", Toast.LENGTH_LONG).show();
                    System.exit(0);
                }
                break;
            default:
                break;
        }
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
