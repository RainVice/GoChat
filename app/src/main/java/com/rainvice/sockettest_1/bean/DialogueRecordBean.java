package com.rainvice.sockettest_1.bean;

import java.util.ArrayList;
import java.util.List;

public class DialogueRecordBean {

    private String username;

    private String ip;

    private long times;

    private List<DialogBean> dialogs = new ArrayList<>();

    public DialogueRecordBean() {
    }

    public DialogueRecordBean(String username, String ip) {
        this.username = username;
        this.ip = ip;
    }

    public long getTimes() {
        return times;
    }

    public void setTimes(long times) {
        this.times = times;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public List<DialogBean> getDialogs() {
        return dialogs;
    }

    public void setDialogs(List<DialogBean> dialogs) {
        this.dialogs = dialogs;
    }
}
