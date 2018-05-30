package com.andromeda.djzaamir.rideshare;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.andromeda.djzaamir.rideshare.utils.InputUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DriverDetailActivity extends AppCompatActivity {


    //region Vars
    //Spinner car colors
    private final String[] colors = new String[]{
      "Choose a color","Black","White","Silver","Red","Brown","Orange","Yellow","Blue","Purple"
    };

    //gui referrences
    private EditText vehicle_no, cnic;
    private Spinner color_spinner;
    private ImageView profile_image;

    private Button profile_image_button,submit_data_button;

    private String selected_color,vehicle_number,cnic_no;

    private final int RESULT_FROM_IMAGE_INTENT = 2;

    private Uri imageData;

    //Boolean to let the activity know that data uploading is finished and it may close now
    private boolean image_upload_complete = false, other_data_upload_complete = false;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_detail);

        //Init gui references
        vehicle_no    =  findViewById(R.id.vehicle_no);
        cnic          =  findViewById(R.id.cnic);
        color_spinner =  findViewById(R.id.vehicle_color_spinner);
        profile_image = findViewById(R.id.user_image);
        profile_image_button = findViewById(R.id.profile_image_button);
        submit_data_button = findViewById(R.id.submit_Data_button);

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

    public void submit_driver_details_btn(View view) throws IOException {
        if (dataValidationGood()){

            //Disable input controls
            InputUtils.disableInputControls(vehicle_no,cnic,profile_image,profile_image_button,submit_data_button);


          //Push data to firebase
            String u_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final DatabaseReference ref =  FirebaseDatabase.getInstance().getReference().child("Driver_vehicle_info").child(u_id);
            final DatabaseReference user_data_ref =  FirebaseDatabase.getInstance().getReference().child("Users").child(u_id);
            //Prepare a data model object to be pushed
             DriverDetailsContainer data_model = new DriverDetailsContainer(vehicle_number,selected_color,cnic_no);

            //Push text data
            ref.setValue(data_model).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                  //Start the Activity to get the driver route/jouney info
                    //and make sure that on pressing back user doesnt fall back on this activity
                     Intent shareMyRideActivityIntent =  new Intent(getApplicationContext(), com.andromeda.djzaamir                                        .rideshare.shareMyRide.class);
                     startActivity(shareMyRideActivityIntent);

                     other_data_upload_complete = true;

                     //Only finish when all data upload is completed
                     if (image_upload_complete && other_data_upload_complete){
                         finish();
                     }
                }
            });

            StorageReference image_storage_ref = FirebaseStorage.getInstance().getReference().child("profile_pics").child(u_id);
            //Push image data
            Bitmap image_bitmap = null;

            //We are uploading this way because this will allow us to compress the image before uploading
            //We could have directly Uploaded the image using Uri object , FirebaseStorage Supports that
            image_bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),imageData);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image_bitmap.compress(Bitmap.CompressFormat.JPEG,20,byteArrayOutputStream);

            byte[] image_byte_data =  byteArrayOutputStream.toByteArray();

            UploadTask image_upload_task = image_storage_ref.putBytes(image_byte_data);
            image_upload_task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri uploaded_image_url = taskSnapshot.getDownloadUrl();

                    //Prepare to upload downlaod url in driver vehicle information
                    Map driver_profile_image_url = new HashMap();
                    driver_profile_image_url.put("driver_image" , uploaded_image_url.toString());

                    user_data_ref.updateChildren(driver_profile_image_url);

                    image_upload_complete = true;
                       //Only finish when all data upload is completed
                     if (image_upload_complete && other_data_upload_complete){
                         finish();
                     }
                }
            });
        }
    }

    private boolean dataValidationGood() {

        String v_no = vehicle_no.getText().toString().trim();
        String c_no = cnic.getText().toString().trim();


         if (imageData != null){
           profile_image_button.setError(null);
        }else{
          profile_image_button.setError("Profile Image Required!");
          return false;
        }

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
        Intent imageGrabIntent =  new Intent(Intent.ACTION_PICK);
        imageGrabIntent.setType("image/*");
        startActivityForResult(imageGrabIntent ,  RESULT_FROM_IMAGE_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case RESULT_FROM_IMAGE_INTENT:
                if (resultCode == Activity.RESULT_OK){
                    final Uri image_data =  data.getData();
                    imageData = image_data; //GLobal Image data will be used for uploading it to Geofire

                    //update image
                    profile_image.setImageURI(image_data);

                }
                break;
            default:
                    break;
        }
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
