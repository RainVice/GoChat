package com.rainvice.sockettest_1.utils;

import android.app.Application;
import android.content.Context;


public class DataUtil extends Application {

    private static Context context;

    private static String username = "匿名用户";

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
