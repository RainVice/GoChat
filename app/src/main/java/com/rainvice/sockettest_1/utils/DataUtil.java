package com.rainvice.sockettest_1.utils;

import android.app.Application;
import android.content.Context;


public class DataUtil extends Application {

    private static Context context;


    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}
