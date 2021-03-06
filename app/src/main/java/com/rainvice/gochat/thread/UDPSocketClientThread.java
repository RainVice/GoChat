package com.rainvice.gochat.thread;

import com.google.gson.Gson;
import com.rainvice.gochat.protocol.MsgType;
import com.rainvice.gochat.protocol.RvRequestProtocol;
import com.rainvice.gochat.utils.DataUtil;
import com.rainvice.gochat.utils.LogUtil;
import com.rainvice.gochat.utils.StrZipUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
                String json = gson.toJson(requestProtocol);
                byte[] bytes = StrZipUtil.compress(json);
                bytes = StrZipUtil.compress(bytes);
                byte[] compress = StrZipUtil.compress(bytes);
                DatagramPacket dp = new DatagramPacket(compress,compress.length, adds, DataUtil.getUDPPort());
                ds.send(dp);
                LogUtil.d(TAG,"发送消息");
                sleep(5 * 1000);
            } while (true);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
