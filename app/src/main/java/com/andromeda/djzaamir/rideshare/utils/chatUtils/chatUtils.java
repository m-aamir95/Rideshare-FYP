package com.andromeda.djzaamir.rideshare.utils.chatUtils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by djzaamir on 6/29/2018.
 * Responsible for Checking If a Chat History Exist Between Two Users
 * If Not then this class will Initiate a new one
 */

public class chatUtils {


    private static boolean chat_history_exist;

    /*
        * IF a previous chat history exist between the two persons
        * */
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
                                                    if (snapShot_A.getValue().toString().equals(snapShot_B.getValue().toString())){
                                                       chat_history_exist = true;
                                                       break;
                                                    }

                                                }

                                                if (chat_history_exist){
                                                    break;
                                                }
                                            }
                                        }
                                     }

                                     @Override
                                     public void onCancelled(DatabaseError databaseError) {

                                     }
                                 });
                         //endregion

                     }
                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               });
    }


     /*
     * Will Only Create new Chat Nodes if previous chat nodes does'nt exist between the users
     * */
     public static void initChatDbSchemeForBothPersonsIfNotExist(String other_user_id) {
       if (!chat_history_exist){

           //Make a new Chat History entry
            DatabaseReference new_chat_ref = FirebaseDatabase.getInstance().getReference().child("chats").push();
            new_chat_ref.setValue(true);
            String new_char_history_ID = new_chat_ref.getKey();


            //Make chat history entry for this user
            String this_user_id  = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(this_user_id)
                    .child("chat_history")
                    .child(new_char_history_ID).setValue(true); //New Chat Entry In User Node

            //Make chat history entry for the other user
            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(other_user_id)
                    .child("chat_history")
                    .child(new_char_history_ID).setValue(true); //New Chat Entry In User Node

           //Reset
           chat_history_exist = false;
       }
    }
}
