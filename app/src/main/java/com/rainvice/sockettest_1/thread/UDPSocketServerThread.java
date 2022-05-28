package com.rainvice.sockettest_1.thread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.rainvice.sockettest_1.constant.Status;
import com.rainvice.sockettest_1.event.BusToNearbyEvent;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.utils.DataUtil;
import com.rainvice.sockettest_1.utils.LogUtil;
import com.rainvice.sockettest_1.utils.StringUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class UDPSocketServerThread extends Thread {

    private final String TAG = this.getClass().getSimpleName();
    private final Handler mHander;

    public UDPSocketServerThread(Handler handler) {
        this.mHander = handler;
    }

    @Override
    public void run() {
        try {
            byte[] buf = new byte[1024];//存储发来的消息
            Gson gson = new Gson();
            //绑定端口的
            DatagramSocket ds = new DatagramSocket(DataUtil.getUDPPort());
            LogUtil.d(TAG,"UDP服务端已启动");
            while (true){
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                ds.receive(dp);
                if (Objects.equals(dp.getAddress().getHostAddress(), DataUtil.getIp())){
                    continue;
                }
                new Thread(() -> {
                    try {
                        String json = StringUtil.byteToStr(dp.getData());
                        LogUtil.d("接收到",json);
                        RvRequestProtocol<String> rvRequestProtocol = gson.fromJson(json, RvRequestProtocol.class);
                        String hostAddress = dp.getAddress().getHostAddress();
                        String data = rvRequestProtocol.getData();
                        DataUtil.getNameMap().put(hostAddress, data);
                        Message message = new Message();
                        message.what = Status.SUCCESS;
                        mHander.sendMessage(message);
                        LogUtil.d(TAG,"接收到来自 " + hostAddress + " 的数据：" + data);
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }


                }).start();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
