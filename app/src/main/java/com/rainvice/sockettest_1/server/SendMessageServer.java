package com.rainvice.sockettest_1.server;

import android.os.Handler;

import com.rainvice.sockettest_1.constant.Status;
import com.rainvice.sockettest_1.protocol.RainviceProtocol;
import com.rainvice.sockettest_1.thread.SendMsgThread;

public class SendMessageServer {


    private String mIp;
    private RainviceProtocol<String> mProtocol;
    private Handler mHandler;

    public SendMessageServer(String ip, RainviceProtocol<String> protocol){

        this.mIp = ip;
        this.mProtocol = protocol;

    }


    public void sendMsg(Callback callback){
        this.mHandler = new Handler(message -> {
            switch (message.what){
                case Status.SUCCESS:
                    callback.callback((RainviceProtocol<String>) message.obj);
                    break;
            }
            return true;
        });
        SendMsgThread sendMsgThread = new SendMsgThread(mIp, mProtocol, mHandler);
        sendMsgThread.start();
    }



    public interface Callback{
        void callback(RainviceProtocol<String> result);
    }


}
