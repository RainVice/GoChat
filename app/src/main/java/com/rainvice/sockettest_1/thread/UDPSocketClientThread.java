package com.rainvice.sockettest_1.thread;

import android.util.Log;

import com.google.gson.Gson;
import com.rainvice.sockettest_1.protocol.MsgType;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.utils.DataUtil;
import com.rainvice.sockettest_1.utils.LogUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class UDPSocketClientThread extends Thread{

    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void run() {
        try {
            InetAddress adds = InetAddress.getByName("255.255.255.255");
            Gson gson = new Gson();
            DatagramSocket ds = new DatagramSocket();
            LogUtil.d(TAG,"UDP 客户端已启动");
            do {
                RvRequestProtocol<String> requestProtocol = new RvRequestProtocol<>(MsgType.MESSAGE, DataUtil.getUsername());
                String json = gson.toJson(requestProtocol) + new String(new byte[100], StandardCharsets.UTF_8);
                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                DatagramPacket dp = new DatagramPacket(bytes,json.length(), adds, DataUtil.getUDPPort());
                ds.send(dp);
                LogUtil.d(TAG,"发送消息");
                sleep(5 * 1000);
            } while (true);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
