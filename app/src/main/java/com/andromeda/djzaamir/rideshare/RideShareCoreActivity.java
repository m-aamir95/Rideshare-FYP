package com.andromeda.djzaamir.rideshare;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RideShareCoreActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_share_core);


        //Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        //Navigation Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Actaul Navigation Menu
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        //fetch User Information from firebase
        fillUpUserInfo();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //Otherwise perform the normal back button operation
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ride_share_core, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        //Home
        if (id == R.id.home_item){

            //TODO, very imp Inline Issue
            //The problem is, i can't put a intent here which will pointing back to this activity
            //Also i need to figure a way to share a single Navigation Drawer with Multilple Activities
            //Such as
            //Rides History
            //Settings

        }

        //Settings
        if (id == R.id.settings_item){
            //Handle
        }

        //Rides History
        if (id == R.id.pastRides_item){
            //Handle
        }

        //Signout
        if (id == R.id.signout_item) {
          signOut();
        }

        //Share
        if (id == R.id.nav_share_item) {
            //Handle
        }

        //Feedback
        if (id == R.id.feedback_item) {
            //Handle
        }

        //close drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }




    private void fillUpUserInfo(){

      String u_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
      DatabaseReference usr_data_ref =  FirebaseDatabase.getInstance().getReference().child("Users").child(u_id);

      //Put data-modified listener on above reference
       usr_data_ref.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               if (dataSnapshot != null){
                   String name  = dataSnapshot.child("name").getValue().toString();
                   String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                   //Grab references to gui
                   TextView textView_name  =  findViewById(R.id.textview_customerName);
                   TextView textView_email =  findViewById(R.id.textview_customerEmail);

                   textView_name.setText(name);
                   textView_email.setText(email);
               }
           }
           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });


    }


    public void signOut() {
        FirebaseAuth.getInstance().signOut();

        //Take back to WelcomeActivity
        Intent welcomeActivityIntent =  new Intent(RideShareCoreActivity.this,WelcomeActivity           .class);
        startActivity(welcomeActivityIntent);

        //Dispose off current activity
        finish();
        return;
    }
}
