package com.andromeda.djzaamir.rideshare;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.andromeda.djzaamir.rideshare.utils.ButtonUtils;
import com.andromeda.djzaamir.rideshare.utils.InputUtils;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class shareMyRide extends AppCompatActivity {

    //First of All, the Date and time Code is written in a very messed up way
    //you can understand it eventually because its heavily commented


    //region Vars
    //Gui references
    private EditText start_point_edittext, end_point_edittext, start_date_time_edittext,end_date_time_edittext;
    private CheckBox roundTrip_checkbox;
    private ProgressBar loading_spinner;
    private TextInputLayout textInputLayout;

    //Date Members
    //Will let us know if we are reading Start DateAndTime Or End DateAndTime
    private boolean startDateTimeSelection = true;

    private Calendar start_date_and_time = null, end_date_and_time = null;
    private boolean start_date_and_time_good = false, end_date_and_time_good = false; //Will be used in error validation

    //TO discrimnite between activityResults in order to Assign latlng to Start Destination or Ending destination
    private final int start_loc_intent = 1;
    private final int end_loc_intent   = 2;
    //Start and ending locations
    private LatLng start_loc_point = null, end_loc_point = null;

    //Bools to let know if data sent to Firebase about starting and ending location good
    boolean start_loc_data_send_good = false , end_loc_data_send_good = false , date_and_time_send_state_good = false;



    //To convert Latlng, to Addresses
    Geocoder geocoder;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_my_ride);

        //init geo-coder
        geocoder  = new Geocoder(this, Locale.getDefault());

        //init gui references
        start_point_edittext     =   findViewById(R.id.start_point_txtview);
        end_point_edittext       =   findViewById(R.id.end_point_txtview);
        start_date_time_edittext =   findViewById(R.id.start_date_time_textview);
        end_date_time_edittext   =   findViewById(R.id.return_date_time_textview);

        roundTrip_checkbox       =   findViewById(R.id.roundTrip_checkbox);

        loading_spinner =  findViewById(R.id.loading_spinner);

        textInputLayout = findViewById(R.id.textInputLayout_wrapper_for_return_date_time);

        //select both address field, so they may scroll
        start_point_edittext.setSelected(true);
        end_point_edittext.setSelected(true);


        //region Different onClick Listeners On Different Widgets
        //On Click Listener for CheckBox
        roundTrip_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (roundTrip_checkbox.isChecked()){
                    textInputLayout.setVisibility(View.VISIBLE);
                }else{
                    textInputLayout.setVisibility(View.INVISIBLE);
                }
            }
        });

        //On Click Listener for start-point edittextbox
        start_point_edittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //start Another Activity to get Start location using Google Maps API and Google Places API
                Intent grabLocationActivity =  new Intent(shareMyRide.this,grabLocationMapsActivity.class);
                startActivityForResult(grabLocationActivity ,start_loc_intent); //lat,lng will be returned
            }
        });

         //On Click Listener for end-point edittextbox
        end_point_edittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //start Another Activity to get Start location using Google Maps API and Google Places API
                Intent grabLocationActivity =  new Intent(shareMyRide.this,grabLocationMapsActivity.class);
                startActivityForResult(grabLocationActivity ,end_loc_intent); //lat,lng will be returned
            }
        });

         //On Click Listener for start-date-time edittextbox
        start_date_time_edittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDateTimeSelection = true;
                getDateAndTime();
            }
        });

        //On Click Listener for end/return-date-time edittextbox
        end_date_time_edittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDateTimeSelection = false;//Means currently working on roundtrip date and time
                getDateAndTime();
            }
        });
        //endregion

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //TODO, can be refactored

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

    private void getDateAndTime(){

        //This Function is pretty Weirdly written
        //I am combining two mostly similar but different things here
        //Getting Date and time via Dialogue boxes
        /*
        *  The problem:
        *  If i write this code in linear Fashion without mixing in date and time dialogue code's
        *  it will cause both date and time dialogue's to be shown at the same time
        *  Because both date and time uses callbacks which are executed after a slight delay
        *  And after setting up those callbacks, java continues to execute code in a linear Fashion
        *  Hence ending up showing both date and time dialogue's
        *
        *  My Solution:
        *  To call the time dialogue show() method in the OnDateSet Callback of the Date Dialogue
        *  and you want to understand following code, and Look up DatePickerDialouge and TimePickerDialouge Separately on internet
        * */

        //To get curent date and time,Because we need to pass something into Date and time callbacks,
        //In the following code
        final Calendar c = Calendar.getInstance();


         //Get time
         int Hour , Minutes;
         Hour      = c.get(Calendar.HOUR);
         Minutes   = c.get(Calendar.MINUTE);


        final  TimePickerDialog timePickerDialog =  new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minutes) {
              //Can't format time to be returned in here
              //Because only Final variables can be accessed in here

                //Setup time, we are passing these vars into this helper function
                //Because we need to update outside scoped variables
                //They cant be used here, because they need to be final for that
                //This helper function will take these vars and update those outside scopped vars
                setTime(hour,minutes);
            }
        },Hour,Minutes,false);

        //Get Date
        int Year,Month,Day;
        Year = c.get(Calendar.YEAR);
        Month = c.get(Calendar.MONTH);
        Day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog =  new DatePickerDialog(shareMyRide.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
              //Can't format date to be returned in here
              //Because only Final variables can be accessed in here

                //Setup date, we are passing these vars into this helper function
                //Because we need to update outside scoped variables
                //They cant be used here, because they need to be final for that
                //This helper function will take these vars and update those outside scopped vars

                setDate(year,month,day);  //month + 1, because DataPickerDialouge is starting months from 0 =  Jan insead of 1 =  Jan

                //To make sure Time Dialogue will come up after selecting a date
              timePickerDialog.show();

            }
        },Year,Month,Day);

        //handle minimum date to be shown,depending upon start and end time
        if (startDateTimeSelection){
           datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        }else{
            datePickerDialog.getDatePicker().setMinDate(start_date_and_time.getTimeInMillis());
        }

        datePickerDialog.show();
    }
    //helper functions to update outside scope vars with the vars of date and time callbacks
    //More details in the getDateAndTime()
    void setDate(int year , int month , int day){

        if (startDateTimeSelection){

          //update calender obj for start date and time
            start_date_and_time =  Calendar.getInstance();
            start_date_and_time.set(year,month,day);

            //Update gui
            start_date_time_edittext.setText(day+"-"+(month+1)+"-"+year);
        }else{

            //update calender obj for start date and time
            end_date_and_time =  Calendar.getInstance();
            end_date_and_time.set(year,month,day);


            end_date_time_edittext.setText(day+"-"+(month+1)+"-"+year);
        }
    }
    void setTime(int hour, int minutes){


        //Update Start Date and Time
        if (startDateTimeSelection){

         start_date_and_time_good = true;

         //Also Enable CheckBox for Return date and time
         roundTrip_checkbox.setEnabled(true);

        //Update start calendar
        start_date_and_time.set(Calendar.HOUR_OF_DAY ,hour); //Hour_Of_Day, Indicating a 24 Hour clock
        start_date_and_time.set(Calendar.MINUTE,minutes);
        int isAm = -1;
        isAm =  start_date_and_time.get(Calendar.AM_PM);
        String am_pm_decided_str = isAm == Calendar.AM ? "AM":"PM";

        //Perform Military to normal time conversion
        if (hour <= 12){ //AM time range
        }else {
            //Perform Military to normal time conversion
            hour =  hour - 12;
        }

        //update gui
        String current_gui_date = start_date_time_edittext.getText().toString(); //To Concatinate Date and time in GUi
        start_date_time_edittext.setText(current_gui_date + ", " +hour + ":" + FormatMinutes(minutes) + " " + am_pm_decided_str );
       }
       //Update return/time
       else{

            end_date_and_time_good = true;

            //Update start calendar
           end_date_and_time.set(Calendar.HOUR_OF_DAY ,hour); //Hour_Of_Day, Indicating a 24 Hour clock
           end_date_and_time.set(Calendar.MINUTE,minutes);
           int isAm = -1;
           isAm = end_date_and_time.get(Calendar.AM_PM);
           String am_pm_decided_str = isAm == Calendar.AM ? "AM":"PM";


        //Perform Military to normal time conversion
        if (hour <= 12){ //AM time range
        }else {
            //Perform Military to normal time conversion
            hour =  hour - 12;
        }


        //update gui
        String current_gui_date = end_date_time_edittext.getText().toString();
        end_date_time_edittext.setText(current_gui_date + ", " +hour + ":" + FormatMinutes(minutes) + " " + am_pm_decided_str);
       }
    }
    String FormatMinutes(int minutes){
      String tr;
      if (minutes < 10){
          tr = "0" + String.valueOf(minutes);
      }else{
          tr = String.valueOf(minutes);
      }
      return tr;
    }

    //Share my Ride Button Click Listener
    public void shareMyRide_onClick(View view) {

       //Before Submitting data to firebase validate everything
       if (validateData()){

           final Button shareMyRide_button = findViewById(R.id.shareMyRide_button);
           ButtonUtils.disableAndChangeText(shareMyRide_button,"Sharing Ride...");

           //disable all inputs
           InputUtils.disableInputControls(start_point_edittext,end_point_edittext,start_date_time_edittext,roundTrip_checkbox , end_date_time_edittext);

           //Init loading spinner
           loading_spinner.setVisibility(View.VISIBLE);


           /*
           * Get Different Ref's to db because we have to place data in different points
           * Due to using Geofire For Match making between drivers and customers
           * Otherwise this could be packaged in seperate container and then pushed to DB
           * */
           String u_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

           //Submit data to firebase Of start point
           DatabaseReference available_drivers_start_point = FirebaseDatabase.getInstance().getReference().child                                                                                                                                    ("available_drivers_start_point");

           GeoFire start_loc_geofire_ref =  new GeoFire(available_drivers_start_point);
           start_loc_geofire_ref.setLocation(u_id, new GeoLocation(start_loc_point.latitude, start_loc_point.longitude), new GeoFire             .CompletionListener() {
               @Override
               public void onComplete(String key, DatabaseError error) {
                   //TODO , No error Handling Incase of Rejection/Failure from firebase
                 if (error == null)
                      setStartLocationDataSendState(true);
                      tryToSwitchToRideSharedFragment();
               }
           });




           //Submit data to firebase Of End point
           DatabaseReference available_drivers_end_point = FirebaseDatabase.getInstance().getReference().child                                   ("available_drivers_end_point");
           GeoFire end_loc_geofire_ref =  new GeoFire(available_drivers_end_point);
           end_loc_geofire_ref.setLocation(u_id, new GeoLocation(end_loc_point.latitude, end_loc_point.longitude), new GeoFire                    .CompletionListener() {
               @Override
               public void onComplete(String key, DatabaseError error) {
                   if (error == null)
                      setEndLocationDataSendState(true);
                      tryToSwitchToRideSharedFragment();
               }
           });



           //Submit data to firebase Of Start and End time Info if any
           DatabaseReference available_drivers_time_info = FirebaseDatabase.getInstance().getReference().child                                   ("available_drivers_time_info").child(u_id);

           Start_end_time times = null;
           if (roundTrip_checkbox.isChecked()){
               times = new Start_end_time(start_date_and_time.getTimeInMillis(),end_date_and_time.getTimeInMillis());
           }else{
              times = new Start_end_time(start_date_and_time.getTimeInMillis());
           }

           available_drivers_time_info.setValue(times).addOnSuccessListener(new OnSuccessListener<Void>() {
               @Override
               public void onSuccess(Void aVoid) {
                   setDateAndTimeSendState(true);
                   tryToSwitchToRideSharedFragment();
               }
           }).addOnFailureListener(new OnFailureListener() {
               @Override
               public void onFailure(@NonNull Exception e) {
                 loading_spinner.setVisibility(View.GONE);
                 InputUtils.enableInputControls();
                 Toast.makeText(shareMyRide.this,"Something went wrong\nUnable to share Ride data",Toast.LENGTH_LONG).show();
               }
           });
       }
    }
    void setStartLocationDataSendState(boolean state){
        start_loc_data_send_good = state;
    }
    void setEndLocationDataSendState(boolean state){
        end_loc_data_send_good = state;
    }
    void setDateAndTimeSendState(boolean state){
        date_and_time_send_state_good = state;
    }
    void tryToSwitchToRideSharedFragment(){
        if (start_loc_data_send_good && end_loc_data_send_good && date_and_time_send_state_good){
             loading_spinner.setVisibility(View.GONE);

            //Take to RideSharedActivity
            finish();
        }
    }

    //region Data Validation Functions Before Sending data to firebase
    //Beautifully written and refactored Validation function
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


        /*
        * Validating date and time beautifully,
        * Pls if you have some time read the function documentation
        * */

        //All of the Error Displaying part to the user, has been taken care within the function
        if (!validateDateAndTime(start_date_and_time_good,start_date_time_edittext)){
            return false;
        }


        //If the user has provided return route info
        if (roundTrip_checkbox.isChecked()){
          if (!validateDateAndTime(end_date_and_time_good,end_date_time_edittext)){
              return  false;
          }
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
    private boolean validateDateAndTime(boolean date_time_state, EditText target_widget){
        if (date_time_state == false){
            target_widget.setError("Invalid Date or time");
            return false;
        }else{
            target_widget.setError(null);
        }
        //if everything good return true
        return true;
    }
    //endregion



    //Data-Model Class To Contain Available Driver Time info i-e start and end time
    class Start_end_time {
        public long start_time_stamp, end_time_stamp;

        private Start_end_time(){}//Disable Default constructor
        public Start_end_time(long start_time_stamp){
            this.start_time_stamp = start_time_stamp;
        }
        public Start_end_time(long start_time_stamp, long end_time_stamp) {
            this.start_time_stamp = start_time_stamp;
            this.end_time_stamp = end_time_stamp;
        }
    }

}
