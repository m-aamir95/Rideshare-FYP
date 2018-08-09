package com.andromeda.djzaamir.rideshare;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.andromeda.djzaamir.rideshare.utils.InputUtils;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class ChatActivity extends AppCompatActivity {

    //region VARS
    private String u_id, other_u_id; //Other u_ud_id can be a driver or a customer
    private String unique_chat_id; //Will be used for pushing messages to a unique chat_history Node
    private ImageView other_person_image;
    private TextView other_person_name;
    private Button function_button, driver_info_button; //Depending on Driver Or Customer, it can be REQUEST , ACCEPT REQUEST
    private EditText chat_message_edittextview;
    private ProgressBar data_load_progressbar;

    private LinearLayout chats_container;
    private ScrollView scrollView;


    private DatabaseReference chats_node_reference;
    private ValueEventListener chats_listener;
    private String date_str = "";
    private boolean is_driver, is_driver_status_check_complete = false;
    private boolean is_request_exist = false, is_request_exist_status_check_complete = false;

    private Latlng_wrapper start_position = null, end_position = null;
    private Start_End_Timestamps_Wrapper start_end_timestamps = null;
    private boolean do_not_disable_buttons_Until_One_2_One_Chat = true;
    //endregion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initGuiReferences();

        other_u_id = getIntent().getExtras().getString("other_person_id");
        unique_chat_id = getIntent().getExtras().getString("unique_chat_id");

        u_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        InputUtils.disableInputControls(chats_container, chat_message_edittextview, scrollView);
        data_load_progressbar.setVisibility(View.VISIBLE);

        /*
        * Fetch other person's
        * */
        fetchOtherPersonData();


        //region Firebase Data Grab For Is_Driver , Is_Request_exist , and for chat data
        //Attach listeners for loading chat
        chats_node_reference = FirebaseDatabase.getInstance().getReference()
                .child("chats")
                .child(unique_chat_id);
        chats_listener = chats_node_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    chats_container.removeAllViews();

                    for (DataSnapshot data_node :
                            dataSnapshot.getChildren()) {

                        if (data_node.getKey().equals("REQUESTED_BY")) { //Customer has made a Rideshare Request
                            is_request_exist = true;
                        }
                        //Handle as a normal Chat Message
                        else {
                            push_chat_message_to_gui(data_node);
                        }
                    }

                    is_request_exist_status_check_complete = true;
                    tryToInvokeFunctionButtonPostProcessing();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //Attach one time listener to see, if the driver is sharing Ride info
        FirebaseDatabase.getInstance().getReference()
                .child("available_drivers_start_point")
                .child(other_u_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    driver_info_button.setVisibility(View.VISIBLE);
                    is_driver = false;
                } else {
                    is_driver = true;
                }

                is_driver_status_check_complete = true;
                tryToInvokeFunctionButtonPostProcessing();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //endregion


        //region Button Event Listeners
        driver_info_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startDriverDetailsActivity = new Intent(getApplicationContext(), DisplayDriverDetails.class);
                startDriverDetailsActivity.putExtra("id", other_u_id);
                startActivity(startDriverDetailsActivity);
            }
        });


        function_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is_driver) {
                    InputUtils.disableInputControls(chats_container, chat_message_edittextview, scrollView);
                    data_load_progressbar.setVisibility(View.VISIBLE);
                    initRideScheduleProcedure();
                } else {
                    //Init RideShare Request
                    function_button.setEnabled(false);
                    FirebaseDatabase.getInstance().getReference()
                            .child("chats")
                            .child(unique_chat_id)
                            .child("REQUESTED_BY").setValue(u_id);
                }
            }
        });
        //endregion


        chats_container.requestFocus();
    }

    private void initRideScheduleProcedure() {
        DatabaseReference unique_RideScheduled_Node = FirebaseDatabase
                .getInstance().getReference().child("scheduled_rides").push();
        final String unique_rideScheduled_id = unique_RideScheduled_Node.getKey();


        //region Make entries for new unique scheduled ride both for driver and customer , also set Driver status to booked

        //For Driver
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(u_id)//U_id because only a driver can call `initRideScheduleProcedure`
                .child("scheduled_ride_id").setValue(unique_rideScheduled_id);

        //set Driver status to Booked
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(u_id)
                .child("is_booked").setValue(true);

        //For customer
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(other_u_id) //Always points to customer inside `initRideScheduleProcedure` method
                .child("scheduled_ride_id").setValue(unique_rideScheduled_id);
        //endregion

        //region Move data from Ride-Shared Nodes to Ride-Scheduled Nodes

        //Start position data grab and deletion from available_drivers_start_point
        FirebaseDatabase.getInstance().getReference()
                .child("available_drivers_start_point")
                .child(u_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    double lat = Double.parseDouble(dataSnapshot.child("l").child("0").getValue().toString());
                    double lng = Double.parseDouble(dataSnapshot.child("l").child("1").getValue().toString());
                    start_position = new Latlng_wrapper(lat, lng);

                    //Data deletion
                    FirebaseDatabase.getInstance().getReference()
                            .child("available_drivers_start_point")
                            .child(u_id).removeValue();

                    tryToInvokeRideScheduledDataPush(unique_rideScheduled_id);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //End position data grab and deletion from available_drivers_start_point
        FirebaseDatabase.getInstance().getReference()
                .child("available_drivers_end_point")
                .child(u_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    double lat = Double.parseDouble(dataSnapshot.child("l").child("0").getValue().toString());
                    double lng = Double.parseDouble(dataSnapshot.child("l").child("1").getValue().toString());
                    end_position = new Latlng_wrapper(lat, lng);

                    //Data deletion
                    FirebaseDatabase.getInstance().getReference()
                            .child("available_drivers_end_point")
                            .child(u_id).removeValue();

                    tryToInvokeRideScheduledDataPush(unique_rideScheduled_id);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //End position data grab and deletion from available_drivers_start_point
        FirebaseDatabase.getInstance().getReference()
                .child("available_drivers_time_info")
                .child(u_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    long start_timestamp = Long.parseLong(dataSnapshot.child("start_time_stamp").getValue().toString());
                    long end_timestamp = Long.parseLong(dataSnapshot.child("end_time_stamp").getValue().toString());
                    start_end_timestamps = new Start_End_Timestamps_Wrapper(start_timestamp, end_timestamp);

                    //Data deletion
                    FirebaseDatabase.getInstance().getReference()
                            .child("available_drivers_time_info")
                            .child(u_id).removeValue();

                    tryToInvokeRideScheduledDataPush(unique_rideScheduled_id);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //endregion


    }

    //Go-No-Go Pattern
    private void tryToInvokeRideScheduledDataPush(String unique_rideSchedueld_id) {
        if (start_position != null && end_position != null && start_end_timestamps != null) {
            //Initiate Data Push with Scheduled Ride info
            DatabaseReference unique_scheduled_ride_node = FirebaseDatabase.getInstance().getReference()
                    .child("scheduled_rides")
                    .child(unique_rideSchedueld_id);
            unique_scheduled_ride_node.child("start_point").setValue(start_position);
            unique_scheduled_ride_node.child("end_point").setValue(end_position);
            unique_scheduled_ride_node.child("start_end_timestamps").setValue(start_end_timestamps);
            unique_scheduled_ride_node.child("driver_id").setValue(u_id);
            unique_scheduled_ride_node.child("customer_id").setValue(other_u_id);


            //region Make a unique entry for Rides History both for the driver and the customer
            DatabaseReference unique_ride_history_node_ref =
                    FirebaseDatabase.getInstance().getReference()
                            .child("Rides_History").push();

            //Push this unique_Ride_history_under_both_driver and customer
            DatabaseReference driver_rides_history_ref = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(u_id) //U_id represents Driver id
                    .child("rides_history")
                    .push();
            driver_rides_history_ref.child("unique_ride_history_id").setValue(unique_ride_history_node_ref.getKey());

            //For customer
            DatabaseReference customer_rides_history_ref = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(other_u_id) //other_u_id represents Customer id
                    .child("rides_history")
                    .push();
            customer_rides_history_ref.child("unique_ride_history_id").setValue(unique_ride_history_node_ref.getKey());


            //Starting filling data for new unique_ride_history
            unique_ride_history_node_ref.child("driver_id").setValue(u_id);
            unique_ride_history_node_ref.child("customer_id").setValue(other_u_id);
            unique_ride_history_node_ref.child("start_position").setValue(start_position);
            unique_ride_history_node_ref.child("end_position").setValue(end_position);
            unique_ride_history_node_ref.child("start_end_timestamps").setValue(start_end_timestamps).
                    addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //Finish this Activity
                            finish();
                        }
                    });
            //endregion

        }
    }

    private void initGuiReferences() {
        other_person_image = findViewById(R.id.other_person_image);
        other_person_name = findViewById(R.id.other_person_name);
        function_button = findViewById(R.id.function_button);
        driver_info_button = findViewById(R.id.display_driver_info);
        chats_container = findViewById(R.id.chats_container);
        chat_message_edittextview = findViewById(R.id.chat_message_edittextview);
        scrollView = findViewById(R.id.chatS_scroll_view);
        data_load_progressbar = findViewById(R.id.data_load_progressbar);
    }

    //region Methods Related to Function_Button Functionality and Ride-Scheduling
    //Go-No-Go pattern , waits for results from Multiple Async calls
    private void tryToInvokeFunctionButtonPostProcessing() {
        if (is_driver_status_check_complete && is_request_exist_status_check_complete) {
            if (is_driver) {
                if (is_request_exist) {
                    function_button.setVisibility(View.VISIBLE);
                    function_button.setText("Accept Request");
                    function_button.setBackgroundColor(Color.GREEN);
                }
            } else {
                if (is_request_exist) {
                    function_button.setVisibility(View.VISIBLE);
                    function_button.setText("REQUESTED");
                    function_button.setBackgroundColor(Color.GREEN);
                    function_button.setEnabled(false);
                } else {
                    function_button.setVisibility(View.VISIBLE);
                    function_button.setText("Request");
                }
            }
            InputUtils.enableInputControls();
            data_load_progressbar.setVisibility(View.GONE);
        }
    }
    //endregion

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
        if (chat_message_edittextview.getText().toString().trim().length() > 0) {
            String chat_msg = chat_message_edittextview.getText().toString();

            long timestamp = Calendar.getInstance().getTimeInMillis();
            chat_wrapper new_chat_wrapper = new chat_wrapper(chat_msg, u_id, timestamp);

            //init push
            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(unique_chat_id)
                    .push()
                    .setValue(new_chat_wrapper);

            //Update TimeStamps for both users chat history so, when i fetch Chat List in ChatListView I can display the most recent
            // chats

            //for this user
            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(u_id)
                    .child("chat_history")
                    .child(unique_chat_id)
                    .child("server_timestamp").setValue(ServerValue.TIMESTAMP);


            //for other user
            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(other_u_id)
                    .child("chat_history")
                    .child(unique_chat_id)
                    .child("server_timestamp").setValue(ServerValue.TIMESTAMP);


            chat_message_edittextview.setText("");
        }
    }

    private void push_chat_message_to_gui(DataSnapshot new_chat_msg) {

        if (new_chat_msg.child("msg").getValue() != null) {
            //Parse data
            String msg = new_chat_msg.child("msg").getValue().toString();
            String msg_usr_id = new_chat_msg.child("sender_id").getValue().toString();
            String timestamp = new_chat_msg.child("timestamp").getValue().toString();

            //Prepare Msg TextView
            TextView new_chat_message_textview = new TextView(getApplicationContext());
            new_chat_message_textview.setText(msg);
            new_chat_message_textview.setTextColor(Color.BLACK);
            new_chat_message_textview.setTextSize(18);
            new_chat_message_textview.setPadding(2, 2, 2, 2);

            //Prepare time_stamp
            TextView new_chat_message_time = new TextView(getApplicationContext());

            String _date_str = parseTimestampAndSetupTextView(Long.parseLong(timestamp), new_chat_message_time);

            new_chat_message_time.setTextColor(Color.BLACK);
            new_chat_message_time.setTextSize(12);


            LinearLayout txt_time_container = new LinearLayout(getApplicationContext());
            txt_time_container.setOrientation(LinearLayout.VERTICAL);

            //Decide Placement, depending on who was the sender this_user or other_user
            if (msg_usr_id.equals(u_id)) { //if this user
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.START;
                params.setMargins(0, 0, 0, 5);

                new_chat_message_textview.setGravity(Gravity.START);
                new_chat_message_textview.setLayoutParams(params);
                new_chat_message_time.setGravity(Gravity.START);
                new_chat_message_time.setLayoutParams(params);

                txt_time_container.setLayoutParams(params);
                txt_time_container.setBackgroundColor(Color.parseColor("#cbf4dd"));
            } else {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.END;
                params.setMargins(0, 0, 0, 5);

                new_chat_message_textview.setGravity(Gravity.END);
                new_chat_message_textview.setLayoutParams(params);
                new_chat_message_time.setGravity(Gravity.END);
                new_chat_message_time.setLayoutParams(params);

                txt_time_container.setLayoutParams(params);
            }


            //Date append
            if (date_str.equals(_date_str) == false) {
                TextView new_date_textview = new TextView(getApplicationContext());
                new_date_textview.setText(_date_str);
                new_date_textview.setTextSize(14);
                new_date_textview.setTextColor(Color.BLACK);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    new_date_textview.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                    chats_container.addView(new_date_textview);
                }
                date_str = _date_str;
            }

            //Append Text and time textview to linearlayout
            txt_time_container.addView(new_chat_message_time);
            txt_time_container.addView(new_chat_message_textview);

            //Append to parent
            chats_container.addView(txt_time_container);

            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }

    //region Date and time Processors and helper methods
    private String parseTimestampAndSetupTextView(long timestamp, TextView target_textview) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);

        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        int hour = c.get(Calendar.HOUR);
        int min = c.get(Calendar.MINUTE);
        int isAm = c.get(Calendar.AM_PM);

        String time_of_day = isAm == Calendar.AM ? "AM" : "PM";

        String _date_str = day + "-" + getMonthName(month + 1) + "-" + year;

        String str_representation = hour + ":" + formatMinutes(min) + " " + time_of_day;

        target_textview.setText(str_representation);

        return _date_str;
    }

    private String getMonthName(int i) {
        String month_name = "";
        switch (i) {
            case 1:
                month_name = "January";
                break;
            case 2:
                month_name = "February";
                break;
            case 3:
                month_name = "March";
                break;
            case 4:
                month_name = "April";
                break;
            case 5:
                month_name = "May";
                break;
            case 6:
                month_name = "June";
                break;
            case 7:
                month_name = "July";
                break;
            case 8:
                month_name = "August";
                break;
            case 9:
                month_name = "September";
                break;
            case 10:
                month_name = "October";
                break;
            case 11:
                month_name = "November";
                break;
            case 12:
                month_name = "December";
                break;
        }
        return month_name;
    }

    private String formatMinutes(int min) {
        if (min < 10) {
            return "0" + String.valueOf(min);
        }
        return String.valueOf(min);
    }
    //endregion

    //region Activity Life-Cycles Overrides
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
    //endregion

    //Modal Class for chat messages
    class chat_wrapper {
        public String msg, sender_id;
        public long timestamp;

        public chat_wrapper(String msg, String user_id, long timestamp) {
            this.msg = msg;
            this.sender_id = user_id;
            this.timestamp = timestamp;
        }
    }

    class Latlng_wrapper {
        public double lat;
        public double lng;

        public Latlng_wrapper(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }

    class Start_End_Timestamps_Wrapper {
        public long start_timestamp;
        public long end_timestamp;

        public Start_End_Timestamps_Wrapper(long start_timestamp, long end_timestamp) {
            this.start_timestamp = start_timestamp;
            this.end_timestamp = end_timestamp;
        }
    }
}
