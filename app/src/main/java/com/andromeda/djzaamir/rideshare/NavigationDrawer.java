package com.andromeda.djzaamir.rideshare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class NavigationDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //region Variables
    //Firebase
    private DatabaseReference userDataNodeRef ,  isUserSharingRideNodeRef;
    private ValueEventListener userDataValueEventListener;

    //For location
    //Manages underlying technology to access location based on permissions given in Manifest
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private final int LOCATION_REQ_CODE = 1;

    //Toolbar
    private Toolbar toolbar;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Get Reference to user data in firebase
        String u_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Firebase Node Ref For user data only
        userDataNodeRef = FirebaseDatabase.getInstance().getReference().child("Users").child(u_id);
        isUserSharingRideNodeRef =  FirebaseDatabase.getInstance().getReference().child("available_drivers_start_point").child(u_id);

        //If This Person is sharing His/her Ride then switch to RideShared Fragment
        isUserSharingRideNodeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               if (dataSnapshot != null && dataSnapshot.getValue() != null ){
                       startNewFragmentActivity(new RideSharedFragment());
               }else{
                   //Switch to default homeFragment
                  startNewFragmentActivity(new HomeFragment());
                  //set home as checked item
                   NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                   navigationView.setCheckedItem(R.id.home_item);
                    //change toolbar title
                  toolbar.setTitle("Home");
               }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Firebase Node Ref to check if the person is sharing his/her Ride


        //Setup Event listener on user data node
        userDataValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Get updated data
                String name = dataSnapshot.child("name").getValue().toString();
                String cell = dataSnapshot.child("cell").getValue().toString();
                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                //Put data in fields
                TextView name_txtview = findViewById(R.id.textview_customerName);
                TextView email_txtview = findViewById(R.id.textview_customerEmail);

                name_txtview.setText(name);
                email_txtview.setText(email);

                //Not updating Cell at navigation drawer header
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        //Get and set image data , if available
        DatabaseReference image_url =  FirebaseDatabase.getInstance().getReference().child("Users").child(u_id);
        image_url.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if driver data is resent then this snapshot wont be empty
                if (dataSnapshot.child("driver_image").getValue() != null){

                    String driver_image_url = dataSnapshot.child("driver_image").getValue().toString();

                    ImageView driver_imageview = findViewById(R.id.profilePic_imageView);

                    //Update via glide
                    Glide.with(getApplication()).load(driver_image_url).into(driver_imageview);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //Setup toolbar
        setContentView(R.layout.activity_navigation_drawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Drawer-Layout
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Navigation view
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);





        //init fusedLocationProviderAPI
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //get last know location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=         PackageManager       .PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=          PackageManager.PERMISSION_GRANTED) {

            //Ask for permission
             ActivityCompat.requestPermissions(NavigationDrawer.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},                      LOCATION_REQ_CODE);
        }

        //Ask for last known location
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                lastKnownLocation = location;
            }
        });
    }

    //Callback For Permission's Response form the user
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQ_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Permissions granted, may be continue
                    Toast.makeText(getApplicationContext(), "Permissiosn Granted", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Location Permissions Required", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                break;
            default:
                break;
        }
    }

    //When the user presses DEFAULT back button on Android
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //Otherwise don't handle event
//            super.onBackPressed();\
        }
    }

    //Inflate Menu Items in the navigation Drawer
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }

    //Callback, When a user clicks an item in the Navigation Drawer
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home_item) {
            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle("Home");
            startNewFragmentActivity(new HomeFragment());
        } else if (id == R.id.settings_item) {
            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle("Settings");
            startNewFragmentActivity(new SettingsFragment());
        } else if (id == R.id.pastRides_item) {
            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle("History");
            startNewFragmentActivity(new PastRidesFragment());
        } else if (id == R.id.signout_item) {
            signout();
        } else if (id == R.id.nav_share_item) {
            //Todo, Need to decide the future of it
        } else if (id == R.id.feedback_item) {
            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle("Feedback");
            startNewFragmentActivity(new FeedbackFragment());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Responsible for switching between different fragments
    void startNewFragmentActivity(android.support.v4.app.Fragment fragmentToSwitchTo) {

        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.activity_container_framelayout, fragmentToSwitchTo);
        fragmentTransaction.commit(); //very imp, signals OS to perform above operation when can
    }

    void signout() {
        FirebaseAuth.getInstance().signOut();
        //take back to login/signup screen
        Intent welcomeActivityIntetn = new Intent(NavigationDrawer.this, WelcomeActivity.class);
        startActivity(welcomeActivityIntetn);
        finish();
        return;
    }


     //region Activity Life-Cycle Callbacks
    @Override
    protected void onStart() {
        super.onStart();

        //Start Listener on Activity Startup
        userDataNodeRef.addValueEventListener(userDataValueEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Stop Listener when activity is on background
        userDataNodeRef.removeEventListener(userDataValueEventListener);
    }


    //endregion


}
