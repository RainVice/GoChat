package com.rainvice.sockettest_1.thread;

import android.os.Handler;
import android.os.Message;

import com.rainvice.sockettest_1.constant.Status;
import com.rainvice.sockettest_1.utils.DataUtil;
import com.rainvice.sockettest_1.utils.LogUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScannerIpThread extends Thread {


    private final String TAG = this.getClass().getSimpleName();

    private String mHostIp;
    private String mIps;


    private Handler mHandler;

    public ScannerIpThread(String hostIp, String ips, Handler handler) {
        this.mHostIp = hostIp;
        this.mIps = ips;
        mHandler = handler;
    }

    @Override
    public void run() {
        //创建长度为 50 的线程池
        ExecutorService executorService = Executors.newFixedThreadPool(254);
        final CountDownLatch latch = new CountDownLatch(254);

        LogUtil.d(TAG, "scanIp: 开始扫描 " + mIps + " 网段");

        //扫描局域网内的设备
        for (int i = 1; i <= 255; i++) {
            String ip = mIps + i;
            if (ip.equals(mHostIp)) {
                continue;
            }
            executorService.execute(() -> {
                Socket socket = new Socket();
                InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, DataUtil.getTCPPort());

                //消息对象，给主线程发送消息
                Message message = new Message();
                message.obj = ip;

                try {
                    socket.connect(inetSocketAddress,200);
                    message.what = Status.SUCCESS;
                } catch (IOException e) {
                    message.what = Status.ERROR;
                } finally {
                    mHandler.sendMessage(message);
                    try {
                        socket.close();
                        latch.countDown();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            });
        }

        try {
            latch.await();
            executorService.shutdown();
            //完成
            LogUtil.d(TAG, "完成");
            Message message = new Message();
            message.what = Status.FINISH;
            mHandler.sendMessage(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
