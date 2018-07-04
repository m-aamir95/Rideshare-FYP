package com.andromeda.djzaamir.rideshare.utils.chatUtils;

/**
 * Created by djzaamir on 6/29/2018.
 * Responsible for Triggering Background Event's
 */

public interface IChatUtilsEventListeners {

    void onBackgroundChatCheckComplete();

    void onInitDbSchemaComplete(String unique_chat_id);
}
