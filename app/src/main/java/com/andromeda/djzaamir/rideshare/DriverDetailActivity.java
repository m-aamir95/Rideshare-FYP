package com.andromeda.djzaamir.rideshare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class DriverDetailActivity extends AppCompatActivity {


    //region Vars
    //Spinner car colors
    private final String[] colors = new String[]{
      "Choose a color","Black","White","Silver","Red","Brown","Orange","Yellow","Blue","Purple"
    };

    //gui referrences
    private EditText vehicle_no, cnic;
    private Spinner color_spinner;

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

    }
}
