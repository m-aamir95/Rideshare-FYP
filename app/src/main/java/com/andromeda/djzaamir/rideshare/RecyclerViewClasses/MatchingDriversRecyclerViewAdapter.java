package com.andromeda.djzaamir.rideshare.RecyclerViewClasses;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andromeda.djzaamir.rideshare.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.List;

public class MatchingDriversRecyclerViewAdapter extends RecyclerView.Adapter<MatchingDriversRecyclerViewAdapter.MatchingDriversViewHolder> {


    private LayoutInflater inflater;
    private List<DriverDataModel> DriverData =  Collections.emptyList();
    public MatchingDriversRecyclerViewAdapter(Context context, List<DriverDataModel> driverData){
      inflater = LayoutInflater.from(context);
      this.DriverData = driverData;
    }

    @Override
    public MatchingDriversViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //Inflate our custom row
        //And pass it  to the view holder which will manage it and also cache it
        View view =  inflater.inflate(R.layout.matching_driver_row,parent,false);
        MatchingDriversViewHolder holder =  new MatchingDriversViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MatchingDriversViewHolder holder, int position) {
       DriverDataModel current_data =  DriverData.get(position);

       //Assign the image here
        holder.driver_pickup_address.setText(current_data.pickup_loc_name);
        holder.driver_destination_address.setText(current_data.destination_name);

        //Now we have to query for name based on id
        DatabaseReference name_node_ref = FirebaseDatabase.getInstance().getReference().child("Users").child(current_data.id).child("name");
        name_node_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.driver_name.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return DriverData.size();
    }


    //Internal Class for view holder
    /*
    * RecyclerView.ViewHolder allows use to create a row in the RecyclerView
    * and then whenever it moves out of screen, RecyclerView.ViewHolder will cache it
    * hence reducing calls to findViewById
    * */
    class MatchingDriversViewHolder extends RecyclerView.ViewHolder {

        public TextView driver_name , driver_pickup_address , driver_destination_address;
        public ImageView driver_image;

        //Here the root of out Matching_driver_row will be passed in
        public MatchingDriversViewHolder(View itemView) {
            super(itemView);

            //Grab References to gui
            driver_image =  (ImageView) itemView.findViewById(R.id.driver_image);
            driver_name  =  (TextView)  itemView.findViewById(R.id.driver_name);
            driver_pickup_address = (TextView) itemView.findViewById(R.id.driver_pickup_address);
            driver_destination_address = (TextView) itemView.findViewById(R.id.driver_destination_address);

        }
    }


}
