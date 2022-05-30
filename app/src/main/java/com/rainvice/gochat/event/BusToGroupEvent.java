package com.rainvice.gochat.event;

public class BusToGroupEvent {
    private int status;

    public BusToGroupEvent(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
