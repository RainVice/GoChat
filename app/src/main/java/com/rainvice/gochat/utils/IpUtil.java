package com.rainvice.gochat.utils;

import android.os.Handler;

import androidx.annotation.NonNull;

import com.rainvice.gochat.constant.Status;
import com.rainvice.gochat.thread.ScannerIpThread;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Objects;

public class IpUtil {

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
     * 将 ip 字符串转为 int 值 id
     * @param ip
     * @return
     */
    public static int getIp(@NonNull String ip){
        String[] split = ip.split("\\.");
        String s = split[2] + split[3];
        return Integer.parseInt(s);
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
