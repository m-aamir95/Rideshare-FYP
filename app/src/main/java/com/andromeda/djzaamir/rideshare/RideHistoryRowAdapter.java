package com.andromeda.djzaamir.rideshare;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class RideHistoryRowAdapter extends ArrayAdapter<RideHistoryDataModel> {

    private ArrayList<RideHistoryDataModel> ride_histories;
    private Context context;
    private String u_id;


    public RideHistoryRowAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public RideHistoryRowAdapter(ArrayList<RideHistoryDataModel> ride_history, Context context) {
        super(context, R.layout.message_row, ride_history);
        this.ride_histories = ride_history;
        this.context = context;

        u_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View rowView = inflater.inflate(R.layout.ride_history_row, parent, false);


        //Grab different childern views of this Row
        final ImageView image = rowView.findViewById(R.id.image);
        final TextView name = rowView.findViewById(R.id.driver_name);
        EditText from = (EditText) rowView.findViewById(R.id.driver_pickup_address);
        EditText to = (EditText) rowView.findViewById(R.id.driver_destination_address);
        final TextView date_of_msg = rowView.findViewById(R.id.date_of_msg);

        //Begin Loading data from model to fields
        final RideHistoryDataModel dataModel = ride_histories.get(position);


        //Begin fetching data

        //Try to load name and image for other party
        String other_person_id = dataModel.driver_id.equals(u_id) ? dataModel.customer_id : dataModel.driver_id;

        FirebaseDatabase.getInstance().getReference()
                .child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    String name_str = dataSnapshot.child("name").getValue().toString();
                    String image_url = dataSnapshot.child("driver_image").getValue() != null ?
                            dataSnapshot.child("driver_image").getValue().toString() : null;
                    name.setText(name_str);
                    fetchAndLoadImage(rowView, image, image_url);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Start Translating and loading Latlng to addresses
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            String s_address = geocoder.getFromLocation(dataModel.start_lat, dataModel.start_lng, 1).get(0).getAddressLine(0);
            from.setText(s_address);

             String e_address = geocoder.getFromLocation(dataModel.end_lat, dataModel.end_lng, 1).get(0).getAddressLine(0);
            to.setText(s_address);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return rowView;
    }

    private void fetchAndLoadImage(View rowView, ImageView imageView, String image_url) {
        if (image_url == null) {
            Glide.with(rowView.getContext()).load(image_url).into(imageView);
        } else {
            Drawable default_icon = rowView.getResources().getDrawable(R.drawable.rideshare_logo_final_new);
            Glide.with(rowView.getContext()).load(default_icon).into(imageView);
        }
    }
}
