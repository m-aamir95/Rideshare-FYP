package com.andromeda.djzaamir.rideshare;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatActivity extends AppCompatActivity {

    String u_id , other_u_id; //Other u_ud_id can be a driver or a customer
    ImageView other_person_image;
    TextView other_person_name;
    Button function_button;
    LinearLayout chats_container;
    EditText chat_message_edittextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Init Gui references
        other_person_image =  findViewById(R.id.other_person_image);
        other_person_name = findViewById(R.id.other_person_name);
        function_button =  findViewById(R.id.function_button);
        chats_container =  findViewById(R.id.chats_container);
        chat_message_edittextview =  findViewById(R.id.chat_message_edittextview);


        other_u_id =  getIntent().getExtras().getString("other_person_id");
        u_id  = FirebaseAuth.getInstance().getCurrentUser().getUid();

        /*
        * Fetch other person's
        * */
        fetchOtherPersonData();




    }

    private void fetchOtherPersonData() {

        //Get Image and Name
        FirebaseDatabase.getInstance().getReference().child("Users").child(other_u_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null){

                    String name = dataSnapshot.child("name").getValue().toString();
                    other_person_name.setText(name);

                    if (dataSnapshot.child("driver_image").getValue() != null){
                        String image_url = dataSnapshot.child("driver_image").getValue().toString();
                        Glide.with(getApplicationContext()).load(image_url).into(other_person_image);
                    }else{
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


    }
}
