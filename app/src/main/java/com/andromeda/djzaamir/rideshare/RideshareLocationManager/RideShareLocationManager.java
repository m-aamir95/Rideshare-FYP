package com.andromeda.djzaamir.rideshare.RideshareLocationManager;


import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;


/*
*        =====================   IMPORTANT NOTE ========================
*
*
*        This class is high level class to simplify the fusedLocationAPI access from different classes in the APP
*        since fusedLocationAPI requires Runtime User Permissions and i will be taking those at the very Beginning of launch of
*        core HomeFragment, that is the Fragment which is presented to the user when the APP is launched and waiting for further
*        User Interaction
*
*
*
* */

public class RideShareLocationManager extends com.google.android.gms.location.LocationCallback {

    //region VARS
    private FusedLocationProviderClient fusedLocationProviderClient;
    private onLocationUpdateInterface location_update_susbcriber;
    private LocationCallback locationCallback;
    private Context context;
    private boolean QUICK_LESS_ACCURATE_LOCATION_RETERIVAL = false;
    //endregion


    public RideShareLocationManager(){
         locationCallback =  new LocationCallback(){
             @Override
             public void onLocationResult(LocationResult locationResult) {
                 super.onLocationResult(locationResult);

                   //Invoke user supplied callback
                   LatLng latLng = new LatLng(locationResult.getLastLocation().getLatitude() ,
                                   locationResult.getLastLocation().getLongitude());

                      location_update_susbcriber.onLocationUpdate(latLng);
             }
         };
    }

    public void setOnLocationUpdate(onLocationUpdateInterface new_subscriber , Context context , boolean                                                                QUICK_LESS_ACCURATE_LOCATION_RETERIVAL)
    {
        this.QUICK_LESS_ACCURATE_LOCATION_RETERIVAL = QUICK_LESS_ACCURATE_LOCATION_RETERIVAL;
        setOnLocationUpdate(new_subscriber , context);
    }

    @SuppressLint("MissingPermission")
    public void setOnLocationUpdate(final onLocationUpdateInterface new_subscriber , Context context){

            this.context =  context;
            this.location_update_susbcriber = new_subscriber;

            //Initiate FusedLocationAPI
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);


            if (QUICK_LESS_ACCURATE_LOCATION_RETERIVAL){
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                     new_subscriber.onLocationUpdate(new LatLng(location.getLatitude(),location.getLongitude()));
                    }
                });
            }


            //Init Periodic Location Updates
            fusedLocationProviderClient.requestLocationUpdates(prepareLocationRequest(),locationCallback,null);

    }


    private LocationRequest prepareLocationRequest(){
        @SuppressLint("RestrictedApi") LocationRequest locationRequest =  new LocationRequest();

         long interval_period = 3000l;
         locationRequest.setInterval(interval_period); //After Every Second
         locationRequest.setFastestInterval(interval_period/2);
         locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        return locationRequest;
    }

    public void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @SuppressLint("MissingPermission")
    public void resumeLocationUpdate(){
        fusedLocationProviderClient.requestLocationUpdates(prepareLocationRequest() , locationCallback,null);
    }
}
