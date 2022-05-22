package com.rainvice.sockettest_1.utils;

import android.util.Log;

/**
 * @author Administrator
 * 用法：例如需要打印VERBOSE以上的日志：
 * 则public static final int LEVEL=VERBOSE;
 * 打印DEBUG以上的日志：
 * 则public static final int LEVEL=DEBUG;
 * .....
 * 如果想屏蔽日志：
 * 则public static final int LEVEL=NOTHING;
 * <p>
 * LogUtil.d("TAG","debug log");
 */
public class LogUtil {

    public static final boolean isLog = true;

    public static void v(String tag, String msg) {
        if (isLog){
            if (tag==null)tag = "null";
            if (msg == null)msg = "null";
            Log.v(tag, msg);
        }
    }

    @SuppressWarnings("unused")
    public static void d(String tag, String msg) {
        if (isLog){
            if (tag==null)tag = "null";
            if (msg == null)msg = "null";
            Log.d(tag, msg);
        }
    }

    @SuppressWarnings("unused")
    public static void i(String tag, String msg) {
        if (isLog){
            if (tag==null)tag = "null";
            if (msg == null)msg = "null";
            Log.i(tag, msg);
        }
    }

    @SuppressWarnings("unused")
    public static void w(String tag, String msg) {
        if (isLog){
            if (tag==null)tag = "null";
            if (msg == null)msg = "null";
            Log.w(tag, msg);
        }
    }

    @SuppressWarnings("unused")
    public static void e(String tag, String msg) {
        if (isLog){
            if (tag==null)tag = "null";
            if (msg == null)msg = "null";
            Log.e(tag, msg);
        }
    }

}

