package com.andromeda.djzaamir.rideshare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.andromeda.djzaamir.rideshare.RideshareLocationManager.RideShareLocationManager;
import com.andromeda.djzaamir.rideshare.RideshareLocationManager.onLocationUpdateInterface;
import com.andromeda.djzaamir.rideshare.utils.ButtonUtils;
import com.andromeda.djzaamir.rideshare.utils.InputUtils;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class grabLocationMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //region Vars
    private GoogleMap mMap;
    private LatLng last_known_loc;
    private final int REQ_FINE_LOC = 1;
    private Marker current_marker_location;
    private ProgressBar map_loading_progressbar;
    private Button grabMyLocation_button;
    private RideShareLocationManager rideShareLocationManager;
    private boolean latlng_from_manual_loc = false;
    private boolean one_time_GPS_location_obtained = false;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grab_location_maps);


        map_loading_progressbar = findViewById(R.id.map_loading_progressbar);
        grabMyLocation_button = findViewById(R.id.grabMyLocation_button);

        ButtonUtils.disableAndChangeText(grabMyLocation_button, "Getting Location...");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        mapFragment.getMapAsync(this);


        //Google Place Autocomplete Fragment
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);


        autocompleteFragment.setMenuVisibility(true);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                last_known_loc = place.getLatLng();

                latlng_from_manual_loc = true;
                rideShareLocationManager.stopLocationUpdates();

                if (current_marker_location != null) {
                    current_marker_location.remove();
                }

                current_marker_location = mMap.addMarker(new MarkerOptions().position(last_known_loc));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(last_known_loc, 16));

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Place APi error", "An error occurred: " + status);
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setAllGesturesEnabled(false);

        //Put a listener to grab location on tap
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (one_time_GPS_location_obtained) {
                    latlng_from_manual_loc = true;

                    last_known_loc = latLng;
                    rideShareLocationManager.stopLocationUpdates();

                    //Move camera
                    if (current_marker_location != null) {
                        current_marker_location.remove();
                    }
                    current_marker_location = mMap.addMarker(new MarkerOptions().position(latLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest
                    .permission.ACCESS_FINE_LOCATION}, REQ_FINE_LOC);
        }
        mMap.setMyLocationEnabled(true);
    }


    public void confirm_location_button(View view) {

        ButtonUtils.disableAndChangeText(grabMyLocation_button, "Processing...", "#dcf279", "#000000");


        Intent shareMyRideAcitivityIntent = new Intent();
        LatLng selected_location = new LatLng(last_known_loc.latitude, last_known_loc.longitude);
        shareMyRideAcitivityIntent.putExtra("latitude", String.valueOf(selected_location.latitude));
        shareMyRideAcitivityIntent.putExtra("longitude", String.valueOf(selected_location.longitude));
        setResult(RESULT_OK, shareMyRideAcitivityIntent);
        //Because we are returning result to previous activity,hence we don't need to start a brand new activity
        finish();
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (rideShareLocationManager == null) {
            //init Continuous location update
            rideShareLocationManager = new RideShareLocationManager();
            rideShareLocationManager.setOnLocationUpdate(new onLocationUpdateInterface() {
                @Override
                public void onLocationUpdate(LatLng latLng) {

                    if (latLng != null) {
                        map_loading_progressbar.setVisibility(View.GONE);
                        InputUtils.enableInputControls();
                        mMap.getUiSettings().setAllGesturesEnabled(true);
                        one_time_GPS_location_obtained = true;
                        ButtonUtils.enableButtonRestoreTitle();

                        if (!latlng_from_manual_loc) {

                            last_known_loc = new LatLng(latLng.latitude, latLng.longitude);


                            if (current_marker_location != null) {
                                current_marker_location.remove();
                            }

                            current_marker_location = mMap.addMarker(new MarkerOptions().position(last_known_loc).title("You are here"));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(last_known_loc, 16));
                        }
                    }
                }
            }, this);
        } else {
            if (!latlng_from_manual_loc) {
                rideShareLocationManager.resumeLocationUpdate();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        rideShareLocationManager.stopLocationUpdates();
    }
}
