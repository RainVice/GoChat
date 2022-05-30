package com.rainvice.gochat.event;

public class BusToMessageEvent {

    private int status;

    public BusToMessageEvent(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
