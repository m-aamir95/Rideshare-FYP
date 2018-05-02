package com.andromeda.djzaamir.rideshare;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
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

    //Date Memebers
    //Will let us know if we are reading Start DateAndTime Or End DateAndTime
    private boolean startDateTimeSelection = true;

    private String start_year = null, start_month= null, start_day= null, start_hour= null, start_minutes= null;
    private boolean start_isAM;
    private String end_year= null, end_month= null, end_day= null, end_hour= null, end_minutes= null;
    private boolean end_isAm;
    private String monthsName[] = {"JAN","FEB","MAR","APRIL","MAY","JUNE","JULY","AUG","SEP","OCT","NOV","DEC"};

    private final int start_loc_intent = 1;
    private final int end_loc_intent   = 2;

    //Start and ending locations
    private LatLng start_loc_point = null, end_loc_point = null;


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


        //region Different onClick Listeners On Different Widgets
        //On Click Listener for CheckBox
        roundTrip_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (roundTrip_checkbox.isChecked()){
                    end_date_time_edittext.setVisibility(View.VISIBLE);
                }else{
                    end_date_time_edittext.setVisibility(View.INVISIBLE);
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
                setDate(year,month,day);

                //To make sure Time Dialogue will come up after selecting a date
              timePickerDialog.show();

            }
        },Year,Month,Day);

        datePickerDialog.show();
    }
    //helper functions to update outside scope vars with the vars of date and time callbacks
    //More details in the getDateAndTime()
    void setDate(int year , int month , int day){

        if (startDateTimeSelection){
            this.start_year = String.valueOf(year);
            this.start_month = monthsName[month];
            this.start_day = String.valueOf(day);
            //Update gui
            start_date_time_edittext.setText(start_day+"-"+start_month+"-"+start_year);
        }else{
            this.end_year = String.valueOf(year);
            this.end_month = monthsName[month];
            this.end_day = String.valueOf(day);
            end_date_time_edittext.setText(end_day+"-"+end_month+"-"+end_year);
        }
    }
    void setTime(int hour, int minutes){
        //Update Start Date and Time
        if (startDateTimeSelection){
        //Perform Military to normal time conversion
        if (hour <= 12){ //AM time range
           start_isAM = true;
           this.start_hour = String.valueOf(hour);
        }else {
            start_isAM = false;
            //Perform Military to normal time conversion
            this.start_hour =  String.valueOf(hour - 12);
        }
        this.start_minutes = String.valueOf(minutes);

        //update gui
        String current_gui_date = start_date_time_edittext.getText().toString();
        start_date_time_edittext.setText(current_gui_date + ", " +start_hour + ":" + start_minutes);
       }
       //Update return/time
       else{
         //Perform Military to normal time conversion
        if (hour <= 12){ //AM time range
           end_isAm = true;
           this.end_hour = String.valueOf(hour);
        }else {
            end_isAm = false;
            //Perform Military to normal time conversion
            this.end_hour =  String.valueOf(hour - 12);
        }
        this.end_minutes = String.valueOf(minutes);
        //update gui
        String current_gui_date = end_date_time_edittext.getText().toString();
        end_date_time_edittext.setText(current_gui_date + ", " +end_hour + ":" + end_minutes);
       }
    }


    //Share my Ride Button Click Listener
    public void shareMyRide_onClick(View view) {
       //Before Submitting data to firebase validate everything
       if (validateData()){
           //Submit data to firebase
           Available_Driver new_ride_entry =
                   new Available_Driver(start_loc_point,end_loc_point,new Time(start_day,start_month,start_year,start_hour,                                                       start_minutes,start_isAM),new Time(end_day,end_month,end_year,end_hour,end_minutes,                                                       end_isAm));

           String u_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
           DatabaseReference available_drivers = FirebaseDatabase.getInstance().getReference().child("available_drivers");

           //Push above data at this Db reference againt current user's ID
           available_drivers.child(u_id).setValue(new_ride_entry);
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
        if (!validateDateAndTime(start_day,start_minutes,start_date_time_edittext)){
            return false;
        }


        //If the user has provided return route info
        if (roundTrip_checkbox.isChecked()){
          if (!validateDateAndTime(end_day,end_minutes,end_date_time_edittext)){
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
    private boolean validateDateAndTime(String day, String minutes, EditText target_widget){
         /*
        * Because Dates and time are comprised of multiple parts
        * such as year,month,day,hour,minutes
        * We are going to take smart approach here
        * Only going to check
        *  Day in the date section
        *    Because if the user fills in a Day, then automatically it means that they have selected year and month as well
        *
        * Minutes in the time section
            Same rule as above applies if the user selects then it also means they have selected hours toi
        *
        * */
        if (day == null){
            target_widget.setError("Invalid Date");
            return false;
        }else{
            target_widget.setError(null);
        }
        if (minutes == null){
            target_widget.setError("Invalid Time");
            return false;
        }else{
            target_widget.setError(null);
        }

        //if everything good return true
        return true;
    }
    //endregion

    //Internal Class Data Container For Available-Driver data
    class Available_Driver{
        public LatLng start_point , end_point;
        public Time start_time , end_time;


        private Available_Driver(){}//Disable creation without params
        public Available_Driver(LatLng start_point, LatLng end_point, Time start_time, Time end_time) {
            this.start_point = start_point;
            this.end_point = end_point;
            this.start_time = start_time;
            this.end_time = end_time;
        }


    }
       //Internal class for saving pickup and drop times
        class Time{
           public String day,month,year,hour,minutes;
           public String timeOfDay;

           //Cant be instaniated without these params
            private Time(){}//Make the default constructor private
            public Time(String day, String month, String year, String hour, String minutes,boolean isAm) {
                this.day = day;
                this.month = month;
                this.year = year;
                this.hour = hour;
                this.minutes = minutes;
                this.timeOfDay = isAm ?  "Am":"Pm";
            }
        }
}
