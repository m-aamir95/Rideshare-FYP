package com.andromeda.djzaamir.rideshare.RideshareLocationManager;


import android.annotation.SuppressLint;
import android.content.Context;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;


/*
*        =====================   IMPORTANT NOTE ========================
*
*
*        This class is high level class to simply the fusedLocationAPI access from different classes in the APP
*        since fusedLocationAPI requires Runtime User Permissions and i will be taking those at the very Beginning of launch of
*        core Activity , that is the activity which is presented to the user when the APP is launched and waiting for further
*        User Interaction
*
*         mFusedLocationClient.removeLocationUpdates(mLocationCallback);
*
*
* */

public class RideShareLocationManager extends com.google.android.gms.location.LocationCallback {

    //region VARS
    private FusedLocationProviderClient fusedLocationProviderClient;
    private onLocationUpdateInterface location_update_susbcriber;
    private LocationCallback locationCallback;
    private Context context;
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

    @SuppressLint("MissingPermission")
    public void setOnLocationUpdate(onLocationUpdateInterface new_subscriber , Context context){

            this.context =  context;
            this.location_update_susbcriber = new_subscriber;

            //Initiate FusedLocationAPI
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
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
