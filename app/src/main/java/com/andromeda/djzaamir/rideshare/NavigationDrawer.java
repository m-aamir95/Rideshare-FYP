package com.andromeda.djzaamir.rideshare;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.andromeda.djzaamir.rideshare.utils.App_Wide_Static_Vars;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class NavigationDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TriggerMenuSwitch {

    //region Variables
    //Firebase
    private DatabaseReference userDataNodeRef, isUserSharingRideNodeRef;
    private ValueEventListener userDataValueEventListener;


    NavigationView navigationView;
    //Toolbar
    private Toolbar toolbar;

    private boolean switch_to_rideScheduled_fragment = false;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get Reference to user data in firebase
        String u_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Firebase Node Ref For user data only
        userDataNodeRef = FirebaseDatabase.getInstance().getReference().child("Users").child(u_id);
        isUserSharingRideNodeRef = FirebaseDatabase.getInstance().getReference().child("available_drivers_start_point").child(u_id);

        //If This Person is sharing His/her Ride then switch to RideShared Fragment
        isUserSharingRideNodeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    startNewFragmentActivity(new RideSharedFragment());

                    App_Wide_Static_Vars.switch_to_rideShared_fragment = true;


                    //set home as checked item
                    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                    navigationView.setCheckedItem(R.id.home_item);
                    //change toolbar title
                    toolbar.setTitle("Home");

                } else {
                    tryToSwitchToHomeFragmentIfNoRideScheduled();
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

                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    //Get updated d ata
                    String name = dataSnapshot.child("name").getValue().toString();
                    String cell = dataSnapshot.child("cell").getValue().toString();
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                    //Check if this user has a Scheduled Ride
                    if (dataSnapshot.child("scheduled_ride_id").getValue() != null) {
                        switch_to_rideScheduled_fragment = true;
                        App_Wide_Static_Vars.switch_to_rideShared_fragment = false;
                        App_Wide_Static_Vars.unique_ride_scheduled_id = dataSnapshot.child("scheduled_ride_id").getValue().toString();
                    }else{
                        switch_to_rideScheduled_fragment = false;
                    }

                    //Put data in fields
                    TextView name_txtview = findViewById(R.id.textview_customerName);
                    TextView email_txtview = findViewById(R.id.textview_customerEmail);

                    name_txtview.setText(name);
                    email_txtview.setText(email);

                    if (switch_to_rideScheduled_fragment) {

                        //Remove Firebase Listeners
                        RideScheduledFragment rideScheduledFragment = new RideScheduledFragment();
                        startNewFragmentActivity(rideScheduledFragment);

                        //set home as checked item
                        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                        navigationView.setCheckedItem(R.id.home_item);
                        //change toolbar title
                        toolbar.setTitle("Home");
                    } else {
                        tryToSwitchToHomeFragmentIfNoRideScheduled();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userDataNodeRef.addValueEventListener(userDataValueEventListener);

        //Get and set image data , if available
        DatabaseReference image_url = FirebaseDatabase.getInstance().getReference().child("Users").child(u_id);
        image_url.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if driver data is resent then this snapshot wont be empty
                if (dataSnapshot.child("driver_image").getValue() != null) {

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
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    //Go-NO-Go
    private void tryToSwitchToHomeFragmentIfNoRideScheduled() {
        if (!switch_to_rideScheduled_fragment && !App_Wide_Static_Vars.switch_to_rideShared_fragment) {
            //Switch to default homeFragment
            HomeFragment homeFragment = new HomeFragment();
            startNewFragmentActivity(homeFragment);


            App_Wide_Static_Vars.switch_to_rideShared_fragment = false;
            //set home as checked item
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.home_item);
            //change toolbar title
            toolbar.setTitle("Home");
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

            if (App_Wide_Static_Vars.switch_to_rideShared_fragment) {
                startNewFragmentActivity(new RideSharedFragment());
            } else if (switch_to_rideScheduled_fragment) {
                startNewFragmentActivity(new RideScheduledFragment());

            } else {
                startNewFragmentActivity(new HomeFragment());
            }

        } else if (id == R.id.messages_item) {
            if (!switch_to_rideScheduled_fragment) {
                Toolbar toolbar = findViewById(R.id.toolbar);
                toolbar.setTitle("Messages");
                startNewFragmentActivity(new MessagesFragment());
            }
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

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Share Rideshare with friends , URL";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));

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
        fragmentTransaction.commitAllowingStateLoss(); //very imp, signals OS to perform above operation whenever possible
    }


    void signout() {
        App_Wide_Static_Vars.switch_to_rideShared_fragment = false;
        FirebaseAuth.getInstance().signOut();
        //take back to login/signup screen
        Intent welcomeActivityIntetn = new Intent(NavigationDrawer.this, WelcomeActivity.class);
        startActivity(welcomeActivityIntetn);
        finish();
        return;
    }


    @Override
    public void triggerMenuSwitchToHomeFragment() {

        //Take care of Title
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Home");

        //check home item
        navigationView.getMenu().getItem(0).setChecked(true);

        //Start Home Fragment
        startNewFragmentActivity(new HomeFragment());
    }
}
