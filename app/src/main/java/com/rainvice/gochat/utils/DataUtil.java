package com.rainvice.gochat.utils;

import android.app.Application;
import android.content.Context;

import com.rainvice.gochat.bean.DialogueRecordBean;

import java.util.HashMap;
import java.util.Map;


public class DataUtil extends Application {

    private static Context context;

    private static int TCPPort = 6898;

    private static int UDPPort = 6899;

    //本机的用户名
    private static String username = "匿名用户";
    //消息记录
    private static Map<String, DialogueRecordBean> messageMap = new HashMap<>();

    private static Map<String,String> nameMap = new HashMap<>();

    public static int getUDPPort() {
        return UDPPort;
    }

    public static void setUDPPort(int UDPPort) {
        DataUtil.UDPPort = UDPPort;
    }

    public static int getTCPPort() {
        return TCPPort;
    }

    public static void setTCPPort(int TCPPort) {
        DataUtil.TCPPort = TCPPort;
    }

    public static Map<String, String> getNameMap() {
        return nameMap;
    }

    public static String ip = IpUtil.getHostIp();


    public static String getIp() {
        return ip;
    }

    public static void setIp(String ip) {
        DataUtil.ip = ip;
    }

    public static void setNameMap(Map<String, String> nameMap) {
        DataUtil.nameMap = nameMap;
    }

    public static Map<String, DialogueRecordBean> getMessageMap() {
        return messageMap;
    }

    public static void setMessageMap(Map<String, DialogueRecordBean> messageMap) {
        DataUtil.messageMap = messageMap;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        DataUtil.username = username;
    }

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}
