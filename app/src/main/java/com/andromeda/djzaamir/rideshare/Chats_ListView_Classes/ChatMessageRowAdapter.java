package com.andromeda.djzaamir.rideshare.Chats_ListView_Classes;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.andromeda.djzaamir.rideshare.ChatActivity;
import com.andromeda.djzaamir.rideshare.R;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class ChatMessageRowAdapter extends ArrayAdapter<ChatMessageDataModel> implements View.OnClickListener{


    private ArrayList<ChatMessageDataModel> chat_messages;
    private Context context;

    public ChatMessageRowAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public ChatMessageRowAdapter(ArrayList<ChatMessageDataModel> chat_messages , Context context){
        super(context, R.layout.message_row , chat_messages);
        this.chat_messages  = chat_messages;
        this.context =  context;
    }

    @Override
    public void onClick(View view) {


    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View rowView =  inflater.inflate(R.layout.message_row, parent, false);

        //Put event listener
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 TextView hidden_unique_chat_key      = (TextView)view.findViewById(R.id.hidden_id_field_unique_chat_msg);
                TextView hidden_other_user_id        = (TextView)view.findViewById(R.id.hidden_id_field_other_user_id);
                String hidden_unique_chat_key_str    =  hidden_unique_chat_key.getText().toString();
                String hidden_user_id                =  hidden_other_user_id.getText().toString();

                Intent chatActivityIntent =  new Intent(getContext(), ChatActivity.class);
                chatActivityIntent.putExtra("other_person_id" , hidden_user_id);
                chatActivityIntent.putExtra("unique_chat_id" , hidden_unique_chat_key_str);
                getContext().startActivity(chatActivityIntent);
            }
        });

        //Grab different childern views of this Row
        final ImageView imageView = rowView.findViewById(R.id.chat_msg_image);
        final TextView sender_name = rowView.findViewById(R.id.driver_name);
        final TextView chat_msg = rowView.findViewById(R.id.last_chat_msg);
        TextView hidden_unique_chat_id =  rowView.findViewById(R.id.hidden_id_field_unique_chat_msg);
        TextView hidden_other_user_id  = rowView.findViewById(R.id.hidden_id_field_other_user_id);
        final TextView date_of_msg = rowView.findViewById(R.id.date_of_msg);

        //Begin Loading data from model to fields
        final ChatMessageDataModel dataModel = chat_messages.get(position);

        //Load data in hidden fields
        hidden_unique_chat_id.setText(dataModel.msg_id);
        hidden_other_user_id.setText(dataModel.msg_sender_id);



        //Load data related to user
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(dataModel.msg_sender_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null){
                    String name =  dataSnapshot.child("name").getValue().toString();
                    sender_name.setText(name);

                    if (dataSnapshot.child("driver_image").getValue() != null){
                         String image_url = dataSnapshot.child("driver_image").getValue().toString();
                         Glide.with(rowView.getContext()).load(image_url).into(imageView);
                    }else{
                        Drawable default_icon =  rowView.getResources().getDrawable(R.drawable.rideshare_logo_final_new);
                        Glide.with(rowView.getContext()).load(default_icon).into(imageView);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //Load data related to chat-msg
        FirebaseDatabase.getInstance().getReference()
                .child("chats")
                .child(dataModel.msg_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                boolean new_msg_recieved = false;

                if (dataSnapshot != null && dataSnapshot.getValue() != null){
                    for (DataSnapshot chat_data_node :
                            dataSnapshot.getChildren()) {

                        String msg = chat_data_node.child("msg").getValue().toString();
                        String timestamp =chat_data_node.child("timestamp").getValue().toString();
                        parseDateAndSetupTextView(Long.parseLong(timestamp),date_of_msg);
                        chat_msg.setText(msg);

                        new_msg_recieved = true;
                    }
                }

                if (!new_msg_recieved){
                    chat_msg.setText("No New Message");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



      return rowView;
    }


    private void parseDateAndSetupTextView(long timestamp, TextView target_textview){
        Calendar c =  Calendar.getInstance();
        c.setTimeInMillis(timestamp);

        int day   = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year  = c.get(Calendar.YEAR);

        String _date_str  =  day + "-" + getMonthName(month+1) + "-" + year;

        target_textview.setText(_date_str);
    }

    private String getMonthName(int i) {
       String month_name = "";
       switch (i){
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

}
