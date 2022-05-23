package com.rainvice.sockettest_1.thread;

import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.rainvice.sockettest_1.constant.Status;
import com.rainvice.sockettest_1.protocol.RainviceProtocol;
import com.rainvice.sockettest_1.utils.LogUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Objects;

public class SendMsgThread extends Thread{

    private Handler mHandler;
    private InputStream mIs;
    private OutputStream mOs;
    private String mIp;
    private RainviceProtocol<String> mMsg;
    private Socket mSocket;

    /**
     * 传入 ip ，直接构建 socket
     * @param ip IP地址
     * @param msg 发送的消息
     */
    public SendMsgThread(String ip,RainviceProtocol<String> msg){
        this.mIp = ip;
        this.mMsg = msg;
    }

    /**
     * 传入 ip ，直接构建 socket
     * @param ip IP地址
     * @param msg 发送的消息
     * @param handler Handler，如果需要发送消息以后返回数据，那么就可以传入此对象
     */
    public SendMsgThread(String ip, RainviceProtocol<String> msg, Handler handler){
        this.mIp = ip;
        this.mMsg = msg;
        this.mHandler = handler;
    }

    @Override
    public void run() {
        try {
            mSocket = new Socket(mIp, 6898);
            //构建IO
            mOs = mSocket.getOutputStream();

            //将消息转换为 Json 字符串
            Gson gson = new Gson();
            String outMsg = gson.toJson(mMsg);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(mOs));
            //向服务器端发送一条消息
            bw.write(outMsg);
            bw.flush();
            LogUtil.d("发送消息" , outMsg);
            mSocket.shutdownOutput();

            if (Objects.nonNull(mHandler)){
                mIs = mSocket.getInputStream();
                //读取服务器返回的消息
                BufferedReader br = new BufferedReader(new InputStreamReader(mIs));
                String inMsg = br.readLine();
                LogUtil.d("接收到返回数据",inMsg);
                //将消息转换为 RainviceProtocol
                RainviceProtocol<String> protocol = new RainviceProtocol<>(inMsg);
//                RainviceProtocol<String> protocol = gson.fromJson(br, RainviceProtocol.class);

                //告诉主线程获取回执成功
                Message message = new Message();
                message.what = Status.SUCCESS;
                message.obj = protocol;
                mHandler.sendMessage(message);

                mIs.close();
            }
            mOs.close();
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
