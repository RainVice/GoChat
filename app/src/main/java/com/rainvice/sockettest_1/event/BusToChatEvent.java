package com.rainvice.sockettest_1.event;

public class BusToChatEvent {

    private int status;

    public BusToChatEvent(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
