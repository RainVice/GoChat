package com.rainvice.sockettest_1.thread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.rainvice.sockettest_1.bean.GroupChatBean;
import com.rainvice.sockettest_1.constant.Status;
import com.rainvice.sockettest_1.protocol.MsgType;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.utils.DataUtil;
import com.rainvice.sockettest_1.utils.LogUtil;
import com.rainvice.sockettest_1.utils.StrZipUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class UDPSendMsgThread extends Thread {

    private Handler mHandler;
    private RvRequestProtocol<String> mMsg;
    private final Gson gson = new Gson();

    public UDPSendMsgThread() {
    }

    public UDPSendMsgThread(RvRequestProtocol<String> msg, Handler handler) {
        this.mMsg = msg;
        this.mHandler = handler;
    }

    @Override
    public void run() {
        try {
            InetAddress adds = InetAddress.getByName("255.255.255.255");
            DatagramSocket ds = new DatagramSocket();
            String json = gson.toJson(mMsg);


            byte[] bytes = StrZipUtil.compress(json);
            bytes = StrZipUtil.compress(bytes);
            byte[] compress = StrZipUtil.compress(bytes);

            DatagramPacket dp = new DatagramPacket(compress, compress.length, adds, DataUtil.getUDPPort());

            ds.send(dp);
            ds.close();

            System.out.println("发送: " + json.getBytes().length + ","+ compress.length);

            Message message = new Message();
            message.what = Status.SUCCESS;
            mHandler.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();

            Message message = new Message();
            message.what = Status.ERROR;
            mHandler.sendMessage(message);
        }
    }


    public Handler getHandler() {
        return mHandler;
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public RvRequestProtocol<String> getMsg() {
        return mMsg;
    }

    public void setMsg(RvRequestProtocol<String> msg) {
        mMsg = msg;
    }
}
