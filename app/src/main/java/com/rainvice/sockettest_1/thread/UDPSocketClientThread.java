package com.rainvice.sockettest_1.thread;

import com.google.gson.Gson;
import com.rainvice.sockettest_1.protocol.MsgType;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.utils.DataUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPSocketClientThread extends Thread{

    @Override
    public void run() {
        try {
            //1.创建对象
            //构造数据报套接字并将其绑定到本地主机上任何可用的端口。
            DatagramSocket socket = new DatagramSocket();
            //2.打包数据
            RvRequestProtocol<String> requestProtocol = new RvRequestProtocol<>(MsgType.MESSAGE, DataUtil.getUsername());
            Gson gson = new Gson();
            byte[] arr = gson.toJson(requestProtocol).getBytes();
            //四个参数: 包的数据 包的长度 主机对象 端口号
            DatagramPacket packet = new DatagramPacket(arr, arr.length, InetAddress.getByName("255.255.255.255") , DataUtil.getTCPPort());
            while (true) {
                sleep(5000);
                //3.发送
                socket.send(packet);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
