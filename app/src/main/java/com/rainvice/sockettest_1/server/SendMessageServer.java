package com.rainvice.sockettest_1.server;

import android.os.Handler;

import com.rainvice.sockettest_1.constant.Status;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.protocol.RvResponseProtocol;
import com.rainvice.sockettest_1.thread.SendMsgThread;

public class SendMessageServer {


    private String mIp;
    private RvRequestProtocol<String> mProtocol;
    private Handler mHandler;

    public SendMessageServer(String ip, RvRequestProtocol<String> protocol){

        this.mIp = ip;
        this.mProtocol = protocol;

    }


    public void sendMsg(Callback callback){
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
        SendMsgThread sendMsgThread = new SendMsgThread(mIp, mProtocol, mHandler);
        sendMsgThread.start();
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
