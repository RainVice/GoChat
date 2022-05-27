package com.rainvice.sockettest_1.utils;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.rainvice.sockettest_1.constant.Status;
import com.rainvice.sockettest_1.thread.ScannerIpThread;
import com.rainvice.sockettest_1.thread.ScannerIpThreadB;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IpScanUtil{

    private final String TAG = this.getClass().getSimpleName();

    private Callback mCallback;

    private Handler mHandler = new Handler(message -> {

        int what = message.what;
        String ip = (String) message.obj;
        switch (what) {

            case Status.SUCCESS:
                mCallback.onFind(ip);
                break;
            case Status.ERROR:
                break;
            case Status.FINISH:
                mCallback.onFinish();
                break;
        }

        return true;
    });


    public void scanIp(Callback callback) {
        mCallback = callback;

        String hostIp = getHostIp();
//        new ScannerIpThread(hostIp,getNetworkSegment(hostIp),mHandler).start();
        new ScannerIpThread(hostIp,getNetworkSegment(hostIp),mHandler).start();
    }


    /**
     * 获取本地 ip ，非 127.0.0.1
     *
     * @return
     */
    public static String getHostIp() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ip = addresses.nextElement();
                    if (ip instanceof Inet4Address
                            && !ip.isLoopbackAddress() //loopback地址即本机地址，IPv4的loopback范围是127.0.0.0 ~ 127.255.255.255
                            && !Objects.requireNonNull(ip.getHostAddress()).contains(":")) {
                        return ip.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据ip获取网段
     *
     * @param ip
     * @return
     */
    public static String getNetworkSegment(String ip) {
        int startIndex = ip.lastIndexOf(".");
        return ip.substring(0, startIndex + 1);
    }
    /**
     * 根据B类ip地址获取网段
     *
     * @param ip
     * @return
     */
    public static String getNetworkSegmentB(String ip) {
        String[] split = ip.split("\\.");
        String ipB = split[0] + "." + split[1] + ".";
        return ipB;
    }


    public interface Callback {
        /**
         * 找到一个设备
         * @param ip 找到的设备IP
         */
        void onFind(String ip);

        /**
         * 完成扫描
         */
        void onFinish();
    }
}
