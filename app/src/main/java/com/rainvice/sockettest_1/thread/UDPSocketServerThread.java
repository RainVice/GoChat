package com.rainvice.sockettest_1.thread;

import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.utils.DataUtil;
import com.rainvice.sockettest_1.utils.LogUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class UDPSocketServerThread extends Thread {

    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void run() {
        try {
            DatagramSocket serverSocket = new DatagramSocket(DataUtil.getUDPPort());
            byte[] arr = new byte[1024];
            DatagramPacket packet = new DatagramPacket(arr, arr.length);
            LogUtil.d(TAG, "UDP服务启动中");
            Gson gson = new Gson();
            while (true) {
                serverSocket.receive(packet);
                new Thread(() -> {
                    //3 当程序运行起来之后,receive方法会一直处于监听状态
                    String hostAddress = packet.getAddress().getHostAddress();
                    String json = Arrays.toString(packet.getData());
                    RvRequestProtocol<String> rvRequestProtocol = gson.fromJson(json, RvRequestProtocol.class);
                    LogUtil.d("接收到",hostAddress + " 的数据 " + rvRequestProtocol.getData());
                    //接收到UDP连接
                    DataUtil.getNameMap().put(hostAddress, rvRequestProtocol.getData());
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
