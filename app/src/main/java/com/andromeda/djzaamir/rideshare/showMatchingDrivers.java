package com.andromeda.djzaamir.rideshare;

import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.andromeda.djzaamir.rideshare.RecyclerViewClasses.DriverDataModel;
import com.andromeda.djzaamir.rideshare.RecyclerViewClasses.MatchingDriversRecyclerViewAdapter;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class showMatchingDrivers extends AppCompatActivity {


    //region Vars

    private RecyclerView recyclerView;

    //Location latlng
    private LatLng start_loc_point, end_loc_point;
    //Is both the Starting Driver and Ending Driver Match making is complete
    private boolean starting_point_driver_match_complete = false;
    private boolean ending_point_driver_match_complete = false;

    //Starting and ending point Driver Data holder list's
    private ArrayList<Driver_ID_Latlng> nearest_drivers_at_starting_position;
    private ArrayList<Driver_ID_Latlng> nearest_drivers_at_ending_position;
    private ArrayList<Matched_Driver_Data> matched_drivers;

    //RecyclerView Adapter
    private MatchingDriversRecyclerViewAdapter matchingDriversRecyclerViewAdapter;

     //Data for adapter
    List<DriverDataModel> data_for_RecyclerView;
    //endregion




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_matching_drivers);

        recyclerView = findViewById(R.id.matching_drivers_container);
        recyclerView.setHasFixedSize(true);

        //Init Starting and ending point Driver Data holders
        nearest_drivers_at_starting_position = new ArrayList<>();
        nearest_drivers_at_ending_position = new ArrayList<>();
        matched_drivers = new ArrayList<>();


        Intent parentActivityIntent = getIntent();
        start_loc_point = (LatLng) parentActivityIntent.getExtras().get("starting_latlng");
        end_loc_point = (LatLng) parentActivityIntent.getExtras().get("ending_latlng");

        //Start Match-making
        try {
            searchForMatchingDrivers();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    //Will fetch Drivers Matching At Start Location
    //and matching at End Location
    private void searchForMatchingDrivers() throws InterruptedException {
        /*
        * 1) Get All the Driver's starting location In 1-km Or 1-Mile Radius Around the Customer Starting position
        * 2) Get All the Driver's ending location in 1-km or 1-Mile Radius Around the Customer ending position
        * 3) Now make another list which will be basically a OR operating between above two list's of driver's
        *    So we may only have those drivers who are sharing the similar route with the customer
        *    Let the algorithm Begin
        * */

        final float radius = 1.0f; //In km


        //Find the nearest drivers at starting point of customer
        DatabaseReference cust_starting_latlng_fire_ref = FirebaseDatabase.getInstance().getReference().child                                                                                                                           ("available_drivers_start_point");

        GeoFire cust_starting_geofire_ref = new GeoFire(cust_starting_latlng_fire_ref);
        cust_starting_geofire_ref.queryAtLocation(new GeoLocation(start_loc_point.latitude, start_loc_point.longitude), radius)                                 .addGeoQueryEventListener(new GeoQueryEventListener() {

            //Will be called for every matching result
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                nearest_drivers_at_starting_position.add(new Driver_ID_Latlng(key, new LatLng(location.latitude, location.longitude)));
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                starting_point_driver_match_complete = true;

                try {
                    filterOutNotMatchingDrivers();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

        //Find the nearest Drivers at the ending point of customer
        DatabaseReference cust_ending_latlng_fire_ref = FirebaseDatabase.getInstance().getReference().child                                     ("available_drivers_end_point");

        GeoFire cust_ending_geofire_ref = new GeoFire(cust_ending_latlng_fire_ref);
        cust_ending_geofire_ref.queryAtLocation(new GeoLocation(end_loc_point.latitude, end_loc_point.longitude), radius)
                .addGeoQueryEventListener(new GeoQueryEventListener() {

                    //Whenever a key matches criteria
                    //For more info checkout github Docs
                    // https://github.com/firebase/geofire-java
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        nearest_drivers_at_ending_position.add(new Driver_ID_Latlng(key, new LatLng(location.latitude, location.longitude)));
                    }

                    @Override
                    public void onKeyExited(String key) {

                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {

                    }


                    //When all the initial events have been fired
                    @Override
                    public void onGeoQueryReady() {
                        ending_point_driver_match_complete = true;
                        try {
                            filterOutNotMatchingDrivers();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {

                    }
                });


    }

    //Will only give relevant drivers and filter out the rest of them
    void filterOutNotMatchingDrivers() throws IOException {

        //Before Filtering, Make sure that the result of both Starting and ending List's is available
        if (starting_point_driver_match_complete && ending_point_driver_match_complete) {
            //Now in the final list we will only keep those drivers whose starting point and ending point match with Our customer
            //One way to do this is by matching driver ID's in both starting and ending point list's


            for (Driver_ID_Latlng start_point_driver_Data :
                    nearest_drivers_at_starting_position) {
                for (Driver_ID_Latlng end_point_driver_Data :
                        nearest_drivers_at_ending_position) {

                    //If their Id's match keep them
                    if (start_point_driver_Data.id.equals(end_point_driver_Data.id)) {
                        Matched_Driver_Data matched_driver_data = new Matched_Driver_Data(start_point_driver_Data.id,                                                          start_point_driver_Data.location, end_point_driver_Data.location);
                        matched_drivers.add(matched_driver_data);
                    }
                }
            }


            //Start putting data into the recyclerView
            data_for_RecyclerView = new ArrayList<>();

            //Start collecting data to be pushed into the list
            Geocoder geocoder =  new Geocoder(getApplicationContext(), Locale.getDefault());

            //Collect Relevant Information about each driver
            for (Matched_Driver_Data d:
                 matched_drivers) {


                //Translate Latlng to place names
                 String pickup_loc = geocoder.getFromLocation(d.start_point.latitude,d.start_point.longitude,1).get(0)                                                                                                                          .getAddressLine                                                                                                                                         (0);
                 String destination_loc = geocoder.getFromLocation(d.end_point.latitude,d.end_point.longitude,1).get(0)                                                                                                                          .getAddressLine                                                                                                                                        (0);

                 DriverDataModel new_data_model  =  new DriverDataModel(d.id,null,pickup_loc,destination_loc);

                 //Push into the list for which willn adapted by the adapter
                 data_for_RecyclerView.add(new_data_model);

            }

            matchingDriversRecyclerViewAdapter = new MatchingDriversRecyclerViewAdapter(getApplicationContext(),data_for_RecyclerView);
            recyclerView.setAdapter(matchingDriversRecyclerViewAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));



        }
    }

    //region Data Model Classes
    //Data Model Container Class to Hold Driver ID and Latlng
    class Driver_ID_Latlng {
        public String id;
        public LatLng location;

        private Driver_ID_Latlng() {
        }

        public Driver_ID_Latlng(String id, LatLng location) {
            this.id = id;
            this.location = location;
        }
    }



    //Data Model for matched driver's
    class Matched_Driver_Data {
        public String id;
        public LatLng start_point, end_point;

        private Matched_Driver_Data() {
        }

        public Matched_Driver_Data(String id, LatLng start_point, LatLng end_point) {
            this.id = id;
            this.start_point = start_point;
            this.end_point = end_point;

        }
    }
    //endregion
}

