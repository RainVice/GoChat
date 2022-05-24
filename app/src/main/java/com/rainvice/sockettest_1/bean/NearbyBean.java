package com.rainvice.sockettest_1.bean;

/**
 * 附近设备列表
 */
public class NearbyBean {

    private String ip;

    private String name;

    public NearbyBean() {
    }

    public NearbyBean(String ip) {
        this.ip = ip;
    }

    public NearbyBean(String ip, String name) {
        this.ip = ip;
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
