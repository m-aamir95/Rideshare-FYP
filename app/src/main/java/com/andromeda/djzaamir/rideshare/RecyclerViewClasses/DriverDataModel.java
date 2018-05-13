package com.andromeda.djzaamir.rideshare.RecyclerViewClasses;

public class DriverDataModel {

    public String id;
    public String image_url;
    public String pickup_loc_name;
    public String destination_name;

    public DriverDataModel(String id, String image_url, String pickup_loc_name, String destination_name) {
        this.id = id;
        this.image_url = image_url;
        this.pickup_loc_name = pickup_loc_name;
        this.destination_name = destination_name;
    }


}
