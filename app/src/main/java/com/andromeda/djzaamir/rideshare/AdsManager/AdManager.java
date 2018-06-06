package com.andromeda.djzaamir.rideshare.AdsManager;

/*
*  Responsible For Keeping track of
*  if the ad's need to be shown to the user
*  Will set a timer for N Duration after Showing an Ad to user
*  So that the user is not bombarded with Ad's
*  Everytime he/he tries to Use Contact Feature in order to communicate with the driver
*
* */
public class AdManager {

    //Vars
    private static boolean ad_already_shown = false;
    private static int seconds_before_ad_state_reset = 60 * 1; // 1-Minute = 60 Seconds = 60 Cycles Wait



    //Return Ad shown state
    public static boolean adShown(){
        return ad_already_shown;
    }

    /*
    * Set Ad State to shown, means the user has already seen a Ad
    * set off a timer inside a thread for N time
    * this will again set Ad state to Not-Shown after N time
    * */
    public static void  setAdShownStateToShown(){
        ad_already_shown = true;

        //Initiate Timer Inside Thread
        new Thread(new Runnable() {

            @Override
            public void run() {
               int count = 1;

               //Set-off timer here
               while(count <= seconds_before_ad_state_reset){

                   //1-second wait
                   try {
                       Thread.sleep(1000l);
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }

                   count++;
               }

               //Reset Ad shown state
                ad_already_shown = false;
            }
        }).run();
    }
}
