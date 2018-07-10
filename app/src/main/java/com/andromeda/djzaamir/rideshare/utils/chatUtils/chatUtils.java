package com.andromeda.djzaamir.rideshare.utils.chatUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by djzaamir on 6/29/2018.
 * Responsible for Checking If a Chat History Exist Between Two Users
 * If Not then this class will Initiate a new one
 */

public class chatUtils {


    private static boolean chat_history_exist;
    private static IChatUtilsEventListeners _subscriber = null;

    private static String unique_chat_id;//Will be returned to chat_activity can use it for pushing messages

        /*
        * IF a previous chat history exist between the two persons
        * */

    public static void checkIFChatHistoryExist(final String other_user_id , IChatUtilsEventListeners subscriber){
        _subscriber = subscriber;
        checkIFChatHistoryExist(other_user_id);
    }

    public static void checkIFChatHistoryExist(final String other_user_id){
       /*
        Compare chat enteries of both persons
       * */
       String this_user_id  = FirebaseAuth.getInstance().getCurrentUser().getUid();
       FirebaseDatabase.getInstance().getReference().child("Users").child(this_user_id).child("chat_history")
               .addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(final DataSnapshot this_user_chat_history) {
                     if(this_user_chat_history != null && this_user_chat_history.getValue() != null){

                         //region Other Person Fetch Chat Histories And Compare with above history
                         FirebaseDatabase.getInstance().getReference().child("Users").child(other_user_id).child("chat_history")
                                 .addValueEventListener(new ValueEventListener() {
                                     @Override
                                     public void onDataChange(DataSnapshot other_user_chat_history) {
                                        if (other_user_chat_history != null && other_user_chat_history.getValue() != null){

                                            //Begin Match Making
                                            for (DataSnapshot snapShot_A :
                                                    this_user_chat_history.getChildren()) {


                                                for (DataSnapshot snapShot_B :
                                                        other_user_chat_history.getChildren()) {

                                                    //compare chat history ID's
                                                    if (snapShot_A.getKey().toString().equals(snapShot_B.getKey().toString())){
                                                       chat_history_exist = true;
                                                       unique_chat_id = snapShot_A.getKey().toString();
                                                       break;
                                                    }
                                                }

                                                if (chat_history_exist){
                                                    break;
                                                }
                                            }

                                            //Chat-History Check Complete, Trigger Event
                                            triggerChatCheckCompleteEvent();

                                        }else{ //No-chat Trigger Event
                                            triggerChatCheckCompleteEvent();
                                        }
                                     }

                                     @Override
                                     public void onCancelled(DatabaseError databaseError) {

                                     }
                                 });
                         //endregion

                     }
                     else{ //No chat-history , Trigger Event
                        triggerChatCheckCompleteEvent();
                     }
                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               });


    }

    private static void triggerChatCheckCompleteEvent(){
     if (_subscriber != null){
        _subscriber.onBackgroundChatCheckComplete();
        _subscriber = null;
     }
    }

     /*
     * Will Only Create new Chat Nodes if previous chat nodes does'nt exist between the users
     * */
     public static String initChatDbSchemeForBothPersonsIfNotExist(String other_user_id) {
       if (!chat_history_exist){

           //Make a new Chat History entry
            DatabaseReference new_chat_ref = FirebaseDatabase.getInstance().getReference().child("chats").push();
            new_chat_ref.setValue(true);
            String new_chat_history_ID = new_chat_ref.getKey();
            unique_chat_id = new_chat_history_ID;


            //Make chat history entry for this user
            String this_user_id  = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference ref1 =  FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(this_user_id)
                    .child("chat_history")
                    .child(new_chat_history_ID);
              ref1.child("other_user_id").setValue(other_user_id);
              ref1.child("server_timestamp").setValue(ServerValue.TIMESTAMP);

            //Make chat history entry for the other user
            DatabaseReference ref2  = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(other_user_id)
                    .child("chat_history")
                    .child(new_chat_history_ID);
            ref2.child("other_user_id").setValue(this_user_id);
            ref2.child("server_timestamp").setValue(ServerValue.TIMESTAMP);

       }


         //Trigger Events
         if (_subscriber != null){
            _subscriber.onInitDbSchemaComplete(unique_chat_id);
            _subscriber = null;
         }

         //Reset
         chat_history_exist = false;
       return unique_chat_id;
    }


}

