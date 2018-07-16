package com.andromeda.djzaamir.rideshare.utils;

import android.graphics.Color;
import android.widget.Button;


public class ButtonUtils {

    private static Button target_button = null;
    private static String button_text_buff;


    public static void disableAndChangeText(Button button,String text,String backgroundColor,String textColor){
       button.setBackgroundColor(Color.parseColor(backgroundColor));
       button.setTextColor(Color.parseColor(textColor));
       disableAndChangeText(button, text);
    }

    public static void disableAndChangeText(Button button,String text){
     target_button = button;
     button_text_buff =  target_button.getText().toString();
     target_button.setEnabled(false);
     target_button.setText(text);
    }

    public static void enableButtonRestoreTitle(){
        target_button.setText(button_text_buff);
        target_button.setEnabled(true);
    }
}
