package com.andromeda.djzaamir.rideshare;

/**
 * Created by djzaamir on 8/9/2018.
 */

public class RideHistoryDataModel {

    public String driver_id;
    public String customer_id;


    public double start_lat, start_lng;
    public double end_lat, end_lng;

    public long start_timestamp;
    public long end_timestamp;


    public RideHistoryDataModel(String driver_id, String customer_id, double start_lat, double start_lng, double end_lat, double end_lng, long start_timestamp, long end_timestamp) {
        this.driver_id = driver_id;
        this.customer_id = customer_id;
        this.start_lat = start_lat;
        this.start_lng = start_lng;
        this.end_lat = end_lat;
        this.end_lng = end_lng;
        this.start_timestamp = start_timestamp;
        this.end_timestamp = end_timestamp;
    }
}
