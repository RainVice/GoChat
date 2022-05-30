package com.rainvice.sockettest_1.server;

import android.os.Handler;

import com.rainvice.sockettest_1.constant.Status;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.protocol.RvResponseProtocol;
import com.rainvice.sockettest_1.thread.TCPSendMsgThread;
import com.rainvice.sockettest_1.thread.UDPSendMsgThread;

public class SendMessageServer {


    private String mIp;
    private RvRequestProtocol<String> mProtocol;
    private Handler mHandler;

    public SendMessageServer(String ip, RvRequestProtocol<String> protocol){

        this.mIp = ip;
        this.mProtocol = protocol;

    }

    public SendMessageServer(RvRequestProtocol<String> protocol){
        this.mProtocol = protocol;
    }


    public void sendTCPMsg(Callback callback){
        this.mHandler = new Handler(message -> {
            switch (message.what){
                case Status.SUCCESS:
                    callback.success((RvResponseProtocol<String>) message.obj);
                    break;
                case Status.ERROR:
                    callback.error((RvResponseProtocol<String>) message.obj);
                    break;
            }
            return true;
        });
        TCPSendMsgThread tcpSendMsgThread = new TCPSendMsgThread(mIp, mProtocol, mHandler);
        tcpSendMsgThread.start();
    }

    public void sendUDPMsg(Callback callback){
        this.mHandler = new Handler(message -> {
            switch (message.what){
                case Status.SUCCESS:
                    callback.success((RvResponseProtocol<String>) message.obj);
                    break;
                case Status.ERROR:
                    callback.error((RvResponseProtocol<String>) message.obj);
                    break;
            }
            return true;
        });
        UDPSendMsgThread udpSendMsgThread = new UDPSendMsgThread(mProtocol, mHandler);
        udpSendMsgThread.start();
    }



    public interface Callback{
        /**
         * 发送对方已收到
         * @param result
         */
        void success(RvResponseProtocol<String> result);

        /**
         * 发送失败或者对方掉线
         * @param result
         */
        void error(RvResponseProtocol<String> result);
    }


}
