package com.andromeda.djzaamir.rideshare;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class shareMyRide extends AppCompatActivity {

    //First of All, the Date and time Code is written in a very messed up way
    //you can understand it eventually because its heavily commented


   //Gui references
    private EditText start_point_edittext, end_point_edittext, start_date_time_edittext,end_date_time_edittext;
    private CheckBox roundTrip_checkbox;

    //Date Memebers
    boolean startDateTimeSelection = true;

    String start_year, start_month, start_day, start_hour, start_minutes;
    boolean start_isAM;
    String end_year, end_month, end_day, end_hour, end_minutes;
    boolean end_isAm;
    String monthsName[] = {"JAN","FEB","MAR","APRIL","MAY","JUNE","JULY","AUG","SEP","OCT","NOV","DEC"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_my_ride);

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
            }
        });

         //On Click Listener for end-point edittextbox
        end_point_edittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //start Another Activity to get Start location using Google Maps API and Google Places API
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
        StringBuilder formattedDateAndTime =  new StringBuilder();
        formattedDateAndTime.append(String.format("%s-%s-%s , %s:%s", start_day, start_month, start_year, start_hour, start_minutes));
        Toast.makeText(this,formattedDateAndTime.toString(),Toast.LENGTH_SHORT).show();
    }

}
