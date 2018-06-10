package com.andromeda.djzaamir.rideshare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andromeda.djzaamir.rideshare.utils.InputUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatActivity extends AppCompatActivity {

    private String driver_id , u_id;
    private ImageView driver_image,send_button;
    private TextView driver_name;
    private EditText chat_message_Edittextview;
    private Button ride_request_button;
    private LinearLayout chats_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Get Gui references
        driver_image = findViewById(R.id.driver_image);
        send_button =  findViewById(R.id.send_msg_button_imageview);
        driver_name =  findViewById(R.id.driver_name);
        ride_request_button =  findViewById(R.id.ride_request_button);
        chat_message_Edittextview =  findViewById(R.id.chat_message_edittextview);





        //Grab driver id from intent
        driver_id =  getIntent().getStringExtra("driver_id");
        u_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    //Send button/Image for chats
    public void send_chat_message(View view) {

        if (chat_message_Edittextview.getText().toString().trim().length() > 0 == false){
            return; //Quite Method
        }

        //Disable msg button for timebeing
        InputUtils.disableInputControls(send_button);


        //Grab and send message to firebase
        String msg =  chat_message_Edittextview.getText().toString();

        //Every chat has a unique id which is comprised of   chat_id = driver_id + customer_id;
        DatabaseReference chat_node = FirebaseDatabase.getInstance().getReference().child("chats_history").child(driver_id + u_id);
        /*
        * Push will make a random ID entry inside this node
        * Against this random ID we will insert this new Chat Message
        * */
        chat_node.push().setValue(msg);

        //clear message-type area
        chat_message_Edittextview.setText("");

        //Enable button
        InputUtils.enableInputControls();
    }
}
