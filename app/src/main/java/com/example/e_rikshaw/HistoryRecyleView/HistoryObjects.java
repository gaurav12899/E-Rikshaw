package com.example.e_rikshaw.HistoryRecyleView;

public class HistoryObjects {
    private String rideId;
    private String time;

    public HistoryObjects(String rideId,String time){
        this.time=time;
        this.rideId=rideId;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
