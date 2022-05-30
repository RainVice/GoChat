package com.rainvice.gochat.event;

public class BusToNearbyEvent {

    private int status;

    public BusToNearbyEvent(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


}
