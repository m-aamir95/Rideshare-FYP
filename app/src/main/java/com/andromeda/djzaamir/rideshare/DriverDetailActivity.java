package com.andromeda.djzaamir.rideshare;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverDetailActivity extends AppCompatActivity {


    //region Vars
    //Spinner car colors
    private final String[] colors = new String[]{
      "Choose a color","Black","White","Silver","Red","Brown","Orange","Yellow","Blue","Purple"
    };

    //gui referrences
    private EditText vehicle_no, cnic;
    private Spinner color_spinner;

    private String selected_color,vehicle_number,cnic_no;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_detail);

        //Init gui references
        vehicle_no    =  findViewById(R.id.vehicle_no);
        cnic          =  findViewById(R.id.cnic);
        color_spinner =  findViewById(R.id.vehicle_color_spinner);

        //Fill up spinner with Array Adapter
        ArrayAdapter<String> colors_adapter =  new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,colors);

       colors_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       color_spinner.setAdapter(colors_adapter);
       color_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
             if (position > 0){
                 //Then simply grab the color the at that position from colors array
                 selected_color = colors[position];
             }
         }

         @Override
         public void onNothingSelected(AdapterView<?> adapterView) {

         }
     });

    }

    public void submit_driver_details_btn(View view) {
        if (dataValidationGood()){
          //Push data to firebase
            String u_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref =  FirebaseDatabase.getInstance().getReference().child("Driver_vehicle_info").child(u_id);

            //Prepare a data model object to be pushed
            DriverDetailsContainer data_model = new DriverDetailsContainer(vehicle_number,selected_color,cnic_no);

            //Push
            ref.setValue(data_model).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                  //Start the Activity to get the driver route/jouney info
                    //and make sure that on pressing back user doesnt fall back on this activity
                     Intent shareMyRideActivityIntent =  new Intent(getApplicationContext(), com.andromeda.djzaamir                                        .rideshare.shareMyRide.class);
                     startActivity(shareMyRideActivityIntent);
                     finish();
                }
            });
        }
    }

    private boolean dataValidationGood() {

        String v_no = vehicle_no.getText().toString().trim();
        String c_no = cnic.getText().toString().trim();

        if (v_no.length() > 0){
            vehicle_number = v_no;
            vehicle_no.setError(null);
        }else{
            vehicle_no.setError("Invalid Vehicle no");
            return false;
        }

        if (c_no.length() == 13){
            cnic_no = c_no;
            cnic.setError(null);
        }else{
            cnic.setError("Invalid CNIC");
            return false;
        }
        return true;
    }

    public void image_upload(View view) {
    }
}


//Data Model class
class DriverDetailsContainer{
    public String vehicle_no , vehicle_color,driver_cnic;
    public DriverDetailsContainer(String v_no ,String v_color,String dri_cnic){
        vehicle_no     = v_no;
        vehicle_color  = v_color;
        driver_cnic    = dri_cnic;
    }
}
