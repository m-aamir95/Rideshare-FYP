package com.andromeda.djzaamir.rideshare;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class PastRidesFragment extends Fragment {

    private ListView listView;
    private String u_id;
    private ArrayList<String> ride_histories;

    public PastRidesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        u_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //Fetch Ride Histories for this user


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_past_rides, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = getView().findViewById(R.id.past_rides_listview);
        ride_histories =  new ArrayList<>();

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(u_id)
                .child("rides_history")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    for (DataSnapshot ride_history_node :
                            dataSnapshot.getChildren()) {
                       String unique_ride_history_id  = ride_history_node.child("unique_ride_history_id").getValue().toString();
                       ride_histories.add(unique_ride_history_id);
                    }

                    if (ride_histories.size() > 0){
                        RideHistoryRowAdapter adapter = new RideHistoryRowAdapter(ride_histories,getContext());
                        listView.setAdapter(adapter);
                        listView.setVisibility(View.VISIBLE);
                    }else{
                        TextView no_past_rides_found =  getView().findViewById(R.id.no_past_rides_exist);
                        no_past_rides_found.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
