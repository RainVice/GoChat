package com.rainvice.sockettest_1.event;

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
