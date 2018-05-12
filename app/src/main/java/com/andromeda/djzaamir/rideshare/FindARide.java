package com.andromeda.djzaamir.rideshare;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.andromeda.djzaamir.rideshare.utils.ButtonUtils;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FindARide extends AppCompatActivity {

    //region Vars
    //Gui references
    private EditText start_point_edittext, end_point_edittext;
    private Button findADriver_button;

    //On Activiy Result tags
    private final int start_loc_intent=1 , end_loc_intent=2;

    //Latlng for start and endn point
    private LatLng start_loc_point,end_loc_point;

    //GeoCoder
    private Geocoder geocoder;

    //to let the know if the start and ending locations were pushed Successfully or not
    private boolean start_location_push_good = false ,  end_location_push_good = false;

    //Is both the Starting Driver and Ending Driver Match making is complete
    private boolean starting_point_driver_match_complete = false;
    private boolean ending_point_driver_match_complete = false;

    //Starting and ending point Driver Data holder list's
    private ArrayList<Driver_ID_Latlng> nearest_drivers_at_starting_position;
    private ArrayList<Driver_ID_Latlng> nearest_drivers_at_ending_position;
    private ArrayList<Matched_Driver_Data> matched_drivers;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_aride);

        //Init Starting and ending point Driver Data holders
        nearest_drivers_at_starting_position =  new ArrayList<>();
        nearest_drivers_at_ending_position   =  new ArrayList<>();
        matched_drivers =  new ArrayList<>();

        //init geocoder
        geocoder =  new Geocoder(this, Locale.getDefault());

        //Establish gui references
        start_point_edittext =  findViewById(R.id.start_point_findAdriver_txtview);
        end_point_edittext   =  findViewById(R.id.end_point_findAdriver_txtview);

        findADriver_button =  findViewById(R.id.findADriver_button);


        //Place event listeners
        start_point_edittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //start Another Activity to get Start location using Google Maps API and Google Places API
                Intent grabLocationActivity =  new Intent(FindARide.this,grabLocationMapsActivity.class);
                startActivityForResult(grabLocationActivity ,start_loc_intent); //lat,lng will be returned
            }
        });

        end_point_edittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start Another Activity to get Start location using Google Maps API and Google Places API
                Intent grabLocationActivity =  new Intent(FindARide.this,grabLocationMapsActivity.class);
                startActivityForResult(grabLocationActivity ,end_loc_intent); //lat,lng will be returned
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case start_loc_intent:
                 if (resultCode == Activity.RESULT_OK){
                    //grab lat,lng
                   if (data != null){
                     String latitude  = data.getExtras().getString("latitude");
                     String longitude = data.getExtras().getString("longitude");
                      start_loc_point = new LatLng(Double.valueOf(latitude) , Double.valueOf(longitude));
                     List<Address> addresses = null;
                       try {
                           addresses = geocoder.getFromLocation(start_loc_point.latitude,start_loc_point.longitude,1);
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                       start_point_edittext.setText(addresses.get(0).getAddressLine(0));
                   }
                 }
                break;
            case end_loc_intent:
                 if (resultCode == Activity.RESULT_OK){
                 //grab lat,lng
                   if (data != null){
                     String latitude  = data.getExtras().getString("latitude");
                     String longitude = data.getExtras().getString("longitude");
                     end_loc_point = new LatLng(Double.valueOf(latitude) , Double.valueOf(longitude));
                     List<Address> addresses = null;
                       try {
                           addresses = geocoder.getFromLocation(end_loc_point.latitude,end_loc_point.longitude,1);
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                       end_point_edittext.setText(addresses.get(0).getAddressLine(0));
                   }
                 }
                break;
            default:
                break;
        }
    }

    //region Validation
     private boolean validateData(){

        //Validate Start Point
        //All of the Error Displaying part to the user, has been taken care within the function
        if (!validateLocation(start_loc_point,start_point_edittext,false)){
            return  false;
        }

        //Valdiate for end point
        if (!validateLocation(end_loc_point,end_point_edittext,true)){
            return  false;
        }

        //if everything good
        return true;
    }
    private boolean validateLocation(LatLng location , EditText target_widget , boolean isForEndPoint){

        String descriminator_msg = isForEndPoint ? "End": "Start";


        //validate end position
        if (location == null){
            target_widget.setError("Invalid "+ descriminator_msg +" Point");

            return  false;
        }else{
           target_widget.setError(null);
        }

        //if all good
        return  true;
    }
    //end region

    public void findADriver_onClick(View view) {
        if (validateData()){

            ButtonUtils.disableAndChangeText(findADriver_button,"Searching For Drivers...");

            //Grab references to firebase
            final  String u_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

            //Push starting point via Geofire
            DatabaseReference available_cust_start_loc_ref = FirebaseDatabase.getInstance().getReference().child                                   ("available_cust_start_loc");
            GeoFire start_point_geofire_ref = new GeoFire(available_cust_start_loc_ref);
            start_point_geofire_ref.setLocation(u_id, new GeoLocation(start_loc_point.latitude, start_loc_point.longitude), new                   GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                    //Spaggeti Code, dont like this kind of spaggeti code

                   //Push ending point via geofire
                     DatabaseReference available_cust_end_loc_ref = FirebaseDatabase.getInstance().getReference().child                                   ("available_cust_end_loc");
                     GeoFire end_point_geofire_ref = new GeoFire(available_cust_end_loc_ref);
                      end_point_geofire_ref.setLocation(u_id, new GeoLocation(end_loc_point.latitude, end_loc_point.longitude), new                         GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                           //Find the matching drivers
                            ButtonUtils.enableButtonRestoreTitle();


                            try {
                                searchForMatchingDrivers();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                     });

                }
            });

        }
    }

    private  void searchForMatchingDrivers() throws InterruptedException {
        /*
        * 1) Get All the Driver's starting location In 1-km Or 1-Mile Radius Around the Customer Starting position
        * 2) Get All the Driver's ending location in 1-km or 1-Mile Radius Around the Customer ending position
        * 3) Now make another list which will be basically a OR operating between above two list's of driver's
        *    So we may only have those drivers who are sharing the similar route with the customer
        *    Let the algorithm Begin
        * */

        final int radius = 1;


        //Find the nearest drivers at starting point of customer
        DatabaseReference cust_starting_latlng_fire_ref = FirebaseDatabase.getInstance().getReference().child                                 ("available_drivers_start_point");
        GeoFire cust_starting_geofire_ref = new GeoFire(cust_starting_latlng_fire_ref);
        cust_starting_geofire_ref.queryAtLocation(new GeoLocation(start_loc_point.latitude,start_loc_point.longitude),radius)                   .addGeoQueryEventListener(new GeoQueryEventListener() {

            //Will be called for every matching result
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                nearest_drivers_at_starting_position.add(new Driver_ID_Latlng(key,new LatLng(location.latitude,location.longitude)));
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
              filterOutNotMatchingDrivers();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

        //Find the nearest Drivers at the ending point of customer
        DatabaseReference cust_ending_latlng_fire_ref = FirebaseDatabase.getInstance().getReference().child                                   ("available_drivers_end_point");
        GeoFire cust_ending_geofire_ref = new GeoFire(cust_ending_latlng_fire_ref);
        cust_ending_geofire_ref.queryAtLocation(new GeoLocation(end_loc_point.latitude,end_loc_point.longitude),radius)
          .addGeoQueryEventListener(new GeoQueryEventListener() {

              //Whenever a key matches criteria
              //For more info checkout github Docs
              // https://github.com/firebase/geofire-java
              @Override
              public void onKeyEntered(String key, GeoLocation location) {
                  nearest_drivers_at_ending_position.add(new Driver_ID_Latlng(key,new LatLng(end_loc_point.latitude,end_loc_point                       .longitude)));
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
                filterOutNotMatchingDrivers();
              }

              @Override
              public void onGeoQueryError(DatabaseError error) {

              }
          });



    }



    void filterOutNotMatchingDrivers(){

      //Before Filtering, Make sure that the result of both Startin and ending List's is available
        if (starting_point_driver_match_complete && ending_point_driver_match_complete){
             //Now in the final list we will only keep those drivers whose starting point and ending point match with Our customer
            //One way to do this is by matching driver ID's in both starting and ending point list's

            for (Driver_ID_Latlng start_point_driver_Data:
                 nearest_drivers_at_starting_position) {
                for (Driver_ID_Latlng end_point_driver_Data:
                     nearest_drivers_at_ending_position) {

                    //If their Id's match keep them
                    if (start_point_driver_Data.id.equals(end_point_driver_Data.id)){
                        Matched_Driver_Data matched_driver_data = new Matched_Driver_Data(start_point_driver_Data.id,                                         start_point_driver_Data.location,end_point_driver_Data.location);
                        matched_drivers.add(matched_driver_data);
                    }

                }
            }

            Log.e("haha" ,"haha filter init good");
            for (Matched_Driver_Data d:
                 matched_drivers) {
               Log.e("haha" ,d.id);
            }

            //finish for now
            finish();
        }
    }


    //region Data Model Classes
    //Data Model Container Class to Hold Driver ID and Latlng
    class Driver_ID_Latlng{
        public String id;
        public LatLng location;

        private Driver_ID_Latlng(){}
        public Driver_ID_Latlng(String id, LatLng location) {
            this.id = id;
            this.location = location;
        }
    }

    //Data Model for matched driver's
    class Matched_Driver_Data{
        public String id;
        public LatLng start_point, end_point;

        private Matched_Driver_Data(){}

        public Matched_Driver_Data(String id, LatLng start_point, LatLng end_point) {
            this.id = id;
            this.start_point = start_point;
            this.end_point = end_point;
        }
    }
    //endregion
}
