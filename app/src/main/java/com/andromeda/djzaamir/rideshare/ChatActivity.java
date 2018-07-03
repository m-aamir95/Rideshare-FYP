package com.andromeda.djzaamir.rideshare;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class ChatActivity extends AppCompatActivity {

    private String u_id, other_u_id; //Other u_ud_id can be a driver or a customer
    private String unique_chat_id; //Will be used for pushing messages to a unique chat_history Node
    private ImageView other_person_image;
    private TextView other_person_name;
    private Button function_button; //Depending on Driver Or Customer, it can be REQUEST , ACCEPT REQUEST
    private EditText chat_message_edittextview;

    private LinearLayout chats_container;
    private ScrollView scrollView;


    private DatabaseReference chats_node_reference;
    ValueEventListener chats_listener;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Init Gui references
        other_person_image        = findViewById(R.id.other_person_image);
        other_person_name         = findViewById(R.id.other_person_name);
        function_button           = findViewById(R.id.function_button);
        chats_container           = findViewById(R.id.chats_container);
        chat_message_edittextview = findViewById(R.id.chat_message_edittextview);
        scrollView                = findViewById(R.id.chatS_scroll_view);


        other_u_id = getIntent().getExtras().getString("other_person_id");
        unique_chat_id = getIntent().getExtras().getString("unique_chat_id");

        u_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        /*
        * Fetch other person's
        * */
        fetchOtherPersonData();


        //Attach listeners for loading chat
         chats_node_reference =  FirebaseDatabase.getInstance().getReference()
                .child("chats")
                .child(unique_chat_id);


          chats_listener = chats_node_reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.getValue() != null){

                            chats_container.removeAllViews();

                            for (DataSnapshot chat_msg :
                                    dataSnapshot.getChildren()) {
                                push_chat_message_to_gui(chat_msg);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


    }

    private void fetchOtherPersonData() {

        //Get Image and Name
        FirebaseDatabase.getInstance().getReference().child("Users").child(other_u_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    String name = dataSnapshot.child("name").getValue().toString();
                    other_person_name.setText(name);

                    if (dataSnapshot.child("driver_image").getValue() != null) {
                        String image_url = dataSnapshot.child("driver_image").getValue().toString();
                        Glide.with(getApplicationContext()).load(image_url).into(other_person_image);
                    } else {
                        //Load default image
                        Drawable default_icon = getResources().getDrawable(R.drawable.rideshare_logo_final_new);
                        Glide.with(getApplicationContext()).load(default_icon).into(other_person_image);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    //Send button/Image for chats
    public void send_chat_message(View view) {
        if (chat_message_edittextview.getText().toString().trim().length() > 0){
            String chat_msg  = chat_message_edittextview.getText().toString();

            long timestamp =  Calendar.getInstance().getTimeInMillis();
            chat_wrapper new_chat_wrapper =  new chat_wrapper(chat_msg , u_id , timestamp);

            //init push
            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(unique_chat_id)
                    .push()
                    .setValue(new_chat_wrapper);

            chat_message_edittextview.setText("");
        }
    }


    private void push_chat_message_to_gui(DataSnapshot new_chat_msg){

        //Parse data
        String msg =  new_chat_msg.child("msg").getValue().toString();
        String msg_usr_id =  new_chat_msg.child("user_id").getValue().toString();
        String timestamp =  new_chat_msg.child("timestamp").getValue().toString();




        //Prepare Msg TextView
       TextView new_chat_message_textview = new TextView(getApplicationContext());
       new_chat_message_textview.setText(msg);
       new_chat_message_textview.setTextColor(Color.BLACK);
       new_chat_message_textview.setTextSize(16);




       //Prepare time_stamp
       TextView new_chat_message_time = new TextView(getApplicationContext());

       parseTimestampAndSetupTextView(Long.parseLong(timestamp),new_chat_message_time);

       new_chat_message_time.setTextColor(Color.BLACK);
       new_chat_message_time.setTextSize(12);


       //Decide Placement, depending on who was the sender this_user or other_user
        if (msg_usr_id.equals(u_id)) { //if this user
            LinearLayout.LayoutParams params =  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup                        .LayoutParams.WRAP_CONTENT);
            params.gravity =  Gravity.START;

            new_chat_message_textview.setLayoutParams(params);
            new_chat_message_textview.setGravity(Gravity.START);
            new_chat_message_time.setLayoutParams(params);
            new_chat_message_time.setGravity(Gravity.START);

        }else{
            LinearLayout.LayoutParams params =  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup                        .LayoutParams.WRAP_CONTENT);
            params.gravity =  Gravity.END;

            new_chat_message_textview.setLayoutParams(params);
            new_chat_message_textview.setGravity(Gravity.END);
            new_chat_message_time.setLayoutParams(params);
            new_chat_message_time.setGravity(Gravity.END);
        }



        //Append to parent
       chats_container.addView(new_chat_message_time);
       chats_container.addView(new_chat_message_textview);

       scrollView.fullScroll(View.FOCUS_DOWN);
    }

   private void parseTimestampAndSetupTextView(long timestamp, TextView target_textview){
        Calendar c =  Calendar.getInstance();
        c.setTimeInMillis(timestamp);

        int day   = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year  = c.get(Calendar.YEAR);

        int hour  = c.get(Calendar.HOUR);
        int min   = c.get(Calendar.MINUTE);
        int isAm  = c.get(Calendar.AM_PM);

        String time_of_day = isAm == Calendar.AM ? "AM":"PM";

        //Date Representation day + "-" + (month+1) + "-" + year + ", " +
        String str_representation = hour + ":" + formatMinutes(min) + " " + time_of_day;

        target_textview.setText(str_representation);
    }

    private String formatMinutes(int min) {
        if (min < 10){
            return "0" + String.valueOf(min);
        }
        return String.valueOf(min);
    }

    @Override
    protected void onStop() {
        super.onStop();
        chats_node_reference.removeEventListener(chats_listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
       chats_node_reference.removeEventListener(chats_listener);
    }



    @Override
    protected void onStart() {
        super.onStart();
        chats_node_reference.addValueEventListener(chats_listener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        chats_node_reference.addValueEventListener(chats_listener);
    }

    //Modal Class for chat messages
    class chat_wrapper {
        public String msg, user_id;
        public long timestamp;

        public chat_wrapper(String msg, String user_id ,long timestamp) {
            this.msg = msg;
            this.user_id = user_id;
            this.timestamp =  timestamp;
        }
    }
}
