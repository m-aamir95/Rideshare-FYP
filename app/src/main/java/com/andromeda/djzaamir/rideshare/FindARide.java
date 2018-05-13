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


    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_aride);


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
    //endregion

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

                            //Start showMatching Drivers Activity with Starting and Ending Latlng And close this one
                            Intent matchingDriverActivityIntent = new Intent(getApplicationContext() , showMatchingDrivers.class);
                            matchingDriverActivityIntent.putExtra("starting_latlng" ,start_loc_point);
                            matchingDriverActivityIntent.putExtra("ending_latlng", end_loc_point);

                            startActivity(matchingDriverActivityIntent);
                            finish();
                        }
                     });

                }
            });

        }
    }


}
