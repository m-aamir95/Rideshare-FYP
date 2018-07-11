package com.andromeda.djzaamir.rideshare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.tasks.OnSuccessListener;

public class grabLocationMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //region Vars
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng last_known_loc;
    private final int REQ_FINE_LOC = 1;
    private Marker current_marker_location;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grab_location_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //TODO this is probably because i have commented the compile of Google Play location services
        fusedLocationProviderClient = new FusedLocationProviderClient(this);


        //grab last known location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager                .PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=              PackageManager.PERMISSION_GRANTED) {
            //Ask for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest
                    .permission.ACCESS_FINE_LOCATION}, REQ_FINE_LOC);

        }


        ////////////////////////       Buffer Zone for Code ///////////////////////////////////////////////////////////////














        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                last_known_loc = new LatLng(location.getLatitude(), location.getLongitude());
                current_marker_location = mMap.addMarker(new MarkerOptions().position(last_known_loc).title("You are here"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(last_known_loc, 16));

            }
        });























        ////////////////////////       Buffer Zone for Code END  ///////////////////////////////////////////////////////////////


        //Google Place Autocomplete Fragment
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id                 .place_autocomplete_fragment);

        autocompleteFragment.setMenuVisibility(true);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                last_known_loc = place.getLatLng();
                current_marker_location.remove();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_FINE_LOC:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Cool FINE Location Access Provided
                } else {
                    //Because location access is required
                     Toast.makeText(getApplicationContext(), "Location Services Are Required...", Toast.LENGTH_LONG).show();
                    System.exit(0);
                }
                break;
            default:
                break;
        }
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

        //Put a listener to grab location on tap
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                last_known_loc = latLng;

                //Move camera
                current_marker_location.remove();
                current_marker_location = mMap.addMarker(new MarkerOptions().position(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest
                    .permission.ACCESS_FINE_LOCATION}, REQ_FINE_LOC);
        }
        mMap.setMyLocationEnabled(true);
    }


    public void confirm_location_button(View view) {

        Intent shareMyRideAcitivityIntent = new Intent();
        LatLng selected_location = new LatLng(last_known_loc.latitude, last_known_loc.longitude);
        shareMyRideAcitivityIntent.putExtra("latitude", String.valueOf(selected_location.latitude));
        shareMyRideAcitivityIntent.putExtra("longitude", String.valueOf(selected_location.longitude));
        setResult(RESULT_OK, shareMyRideAcitivityIntent);
        //Because we are returning result to previous activity,hence we don't need to start a brand new activity
        finish();
    }
}
