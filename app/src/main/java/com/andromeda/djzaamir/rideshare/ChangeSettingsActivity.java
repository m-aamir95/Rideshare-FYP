package com.andromeda.djzaamir.rideshare;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.andromeda.djzaamir.rideshare.utils.ButtonUtils;

public class ChangeSettingsActivity extends AppCompatActivity {

    //region VARS
    private TextView content_header_textview ,  content_description_textview;
    private EditText user_input_editext;
    private Button submit_button;

    private String content_header_str;
    private String content_desc_str;
    private String user_input_str;
    private String intent_type;

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_settings);


        content_header_textview      =  findViewById(R.id.content_header_msg);
        content_description_textview =  findViewById(R.id.content_description);
        user_input_editext           =  findViewById(R.id.content_editextbox);
        submit_button                =  findViewById(R.id.content_submit_button);


        Intent incoming_intent =  getIntent();

        //Error-Check
        makeSureIntentHasValidData(incoming_intent);

        intent_type        =  incoming_intent.getStringExtra("intent_type");
        content_header_str =  incoming_intent.getStringExtra("content_header_title");
        content_desc_str   =  incoming_intent.getStringExtra("content_description");

        updateGUIAccordingToIntentType();

    }

    private void updateGUIAccordingToIntentType() {
        content_header_textview.setText(content_header_str);
        content_description_textview.setText(content_desc_str);

        //Put hint according to intent type
        if (intent_type.equals("NAME_CHANGE")){
          user_input_editext.setHint("Enter your Full Name");
        }else if (intent_type.equals("EMAIL_CHANGE")){
          user_input_editext.setHint("Enter your Email");
        }else if(intent_type.equals("CELL_CHANGE")){
          user_input_editext.setHint("Enter your Cell number");
        }

    }

    public void on_submit_data(View view) {
    }



    private void makeSureIntentHasValidData(Intent incoming_intent){
        if (incoming_intent.getStringExtra("intent_type") == null){
            showAlertDialog("Error", "Failure in changing Settings\nError Code: 1000", R.drawable.ic_error_black_24dp,                   "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
        }
        if (incoming_intent.getStringExtra("content_header_title") == null){
            showAlertDialog("Error", "Failure in changing Settings\nError Code: 1001", R.drawable.ic_error_black_24dp,                   "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
        }
         if (incoming_intent.getStringExtra("content_description") == null){
            showAlertDialog("Error", "Failure in changing Settings\nError Code: 1002", R.drawable.ic_error_black_24dp,                   "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
        }
    }
    private void showAlertDialog(String header_title, String msg, @DrawableRes int drawable_icon_id,String ok_button_msg,                                              DialogInterface.OnClickListener ok_callback){
        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(getApplicationContext());
         dialog_builder.setTitle(header_title)
                 .setMessage(msg)
                 .setIcon(drawable_icon_id)
                 .setPositiveButton(ok_button_msg,ok_callback).create().show();
    }
}
