package com.andromeda.djzaamir.rideshare;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.andromeda.djzaamir.rideshare.Chats_ListView_Classes.ChatMessageDataModel;
import com.andromeda.djzaamir.rideshare.Chats_ListView_Classes.ChatMessageRowAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MessagesFragment extends Fragment {

    private ListView chat_messages_listview;
    private ArrayList<ChatMessageDataModel> chat_messages;
    private String u_id;


    public MessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chat_messages = new ArrayList<>();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        u_id = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        return inflater.inflate(R.layout.fragment_messages,null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        chat_messages_listview = getView().findViewById(R.id.messages_listview);

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(u_id)
                .child("chat_history").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               if (dataSnapshot != null && dataSnapshot.getValue() != null ){

                    //Generate Chat-Messages Model
                   for (DataSnapshot chat_message:
                        dataSnapshot.getChildren()) {

                       String  msg_id = chat_message.getKey().toString();
                       String  other_usr_id = chat_message.getValue().toString();

                       ChatMessageDataModel new_chat_model  = new ChatMessageDataModel(other_usr_id, msg_id);

                       chat_messages.add(new_chat_model);

                   }

                   //Load it into ListView
                   ChatMessageRowAdapter adapter = new ChatMessageRowAdapter(chat_messages , getContext());
                   chat_messages_listview.setAdapter(adapter);
               }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
