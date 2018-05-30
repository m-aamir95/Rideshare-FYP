package com.andromeda.djzaamir.rideshare.utils;

import android.view.View;

import java.lang.reflect.Array;

public class InputUtils {

    private static View inputComponents_internal_buffer[];

    //Following function takes variable arguments
    /*
    * Stores different Views into its internal buffer
    * disable's them and then
    * Once required re-enable's them
    * */
    public static void  disableInputControls(View ... inputs_vararg){
        inputComponents_internal_buffer =  new View[inputs_vararg.length];

        //Copy all the components to internal buffer, and disable them at the same time
        for (int i = 0; i < inputs_vararg.length; i++) {
            inputComponents_internal_buffer[i] =  inputs_vararg[i];

            //Disable
            inputs_vararg[i].setEnabled(false);
        }
    }


    //Re-enable all the control in the internal buffer
    public static void enableInputControls(){
        for (int i = 0; i < inputComponents_internal_buffer.length; i++) {
            //Enable
            inputComponents_internal_buffer[i].setEnabled(true);
        }
    }
}
