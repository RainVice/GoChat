package com.rainvice.sockettest_1.service;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.aware.DiscoverySession;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.rainvice.sockettest_1.bean.InputMsgBean;
import com.rainvice.sockettest_1.constant.Status;
import com.rainvice.sockettest_1.event.BusToChatEvent;
import com.rainvice.sockettest_1.event.BusToNearbyEvent;
import com.rainvice.sockettest_1.protocol.MsgType;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.thread.UDPSocketClientThread;
import com.rainvice.sockettest_1.thread.UDPSocketServerThread;
import com.rainvice.sockettest_1.utils.DataUtil;
import com.rainvice.sockettest_1.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPSocketService extends Service {

    private final String TAG = this.getClass().getSimpleName();
    Handler mHandler = new Handler(message -> {

        int what = message.what;
        switch (what) {
            case Status.SUCCESS:
                EventBus.getDefault().post(new BusToNearbyEvent(Status.SUCCESS));
                break;
            case Status.ERROR:

                break;
            case Status.FINISH:

                break;
        }

        return true;
    });

    public UDPSocketService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate() {
        super.onCreate();

        //初始化 UDP 服务
        initUDPSocketServer();


        new Thread(() -> {
            while (true){
                try {
                    Thread.sleep(60 * 1000);
                    LogUtil.d(TAG, "开始检测设备");
                    Map<String, String> nameMap = DataUtil.getNameMap();
                    ArrayList<String> keys = new ArrayList<>();
                    nameMap.forEach((key,val) -> {
                        try {
                            Thread.sleep(200);
                            Socket socket = new Socket();
                            InetSocketAddress inetSocketAddress = new InetSocketAddress(key, DataUtil.getTCPPort());
                            try {
                                socket.connect(inetSocketAddress,200);
                            } catch (IOException e) {
                                LogUtil.d(TAG,"设备掉线：" + key);
                                keys.add(key);
                            } finally {
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    keys.forEach(nameMap::remove);
                    Message message = new Message();
                    message.what = Status.SUCCESS;
                    mHandler.sendMessage(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initUDPSocketServer() {

        new UDPSocketServerThread(mHandler).start();
        new UDPSocketClientThread().start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

}