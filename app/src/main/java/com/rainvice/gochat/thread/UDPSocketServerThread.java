package com.rainvice.gochat.thread;

import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.rainvice.gochat.bean.InputMsgBean;
import com.rainvice.gochat.constant.Status;
import com.rainvice.gochat.protocol.MsgType;
import com.rainvice.gochat.protocol.RvRequestProtocol;
import com.rainvice.gochat.utils.DataUtil;
import com.rainvice.gochat.utils.LogUtil;
import com.rainvice.gochat.utils.StrZipUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
            byte[] buf = new byte[102400];//存储发来的消息
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
                        byte[] bytes = dp.getData();
                        bytes = StrZipUtil.uncompressA(bytes);
                        bytes = StrZipUtil.uncompressA(bytes);
                        String json = StrZipUtil.uncompress(bytes);
                        LogUtil.d("接收到",json);
                        RvRequestProtocol<String> rvRequestProtocol = gson.fromJson(json, RvRequestProtocol.class);
                        String hostAddress = dp.getAddress().getHostAddress();
                        String data = rvRequestProtocol.getData();
                        String type = rvRequestProtocol.getType();
                        if (type.equals(MsgType.MESSAGE)){
                            DataUtil.getNameMap().put(hostAddress, data);
                            Message message = new Message();
                            message.what = Status.SUCCESS;
                            mHander.sendMessage(message);
                        }else if (type.equals(MsgType.GROUP_MESSAGE)){
                            Message message = new Message();
                            message.what = Status.GROUP_SUCCESS;
                            message.obj = new InputMsgBean(hostAddress,rvRequestProtocol);
                            mHander.sendMessage(message);
                        }
                        LogUtil.d(TAG,"接收到来自 " + hostAddress + " 的数据：" + data);
                    } catch (JsonSyntaxException | IOException e) {
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
