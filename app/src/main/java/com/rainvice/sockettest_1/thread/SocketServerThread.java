package com.rainvice.sockettest_1.thread;

import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.rainvice.sockettest_1.protocol.MsgType;
import com.rainvice.sockettest_1.protocol.RainviceProtocol;
import com.rainvice.sockettest_1.service.SocketServerService;
import com.rainvice.sockettest_1.utils.IpScanUtil;
import com.rainvice.sockettest_1.utils.LogUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;

/**
 * Scoket 线程
 */

public class SocketServerThread extends Thread{

    private Handler mHandler;

    public SocketServerThread(Handler handler){
        this.mHandler = handler;
    }
    public SocketServerThread(){
    }


    private final String TAG = this.getClass().getSimpleName();

    private boolean isOpen = true;

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(6898);
            Log.d(TAG, "onCreate: Socket服务已启动");
            while (isOpen) {
                Socket socket = serverSocket.accept();

                //业务处理代码
                new Thread(() -> {
                    try {
                        LogUtil.d(TAG, "onCreate: 客户端:" + socket.getInetAddress().getHostAddress() + "已连接到服务器");
                        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        //读取客户端发送来的消息
                        String mess = br.readLine();
                        LogUtil.d("接受消息",mess);

                        Gson gson = new Gson();
                        RainviceProtocol rainviceProtocol = null;
                        try {
                            rainviceProtocol = gson.fromJson(mess, RainviceProtocol.class);
                        }catch (Exception e){

                        }

                        //返回数据
                        if (Objects.nonNull(rainviceProtocol) && MsgType.GET_NAME.equals(rainviceProtocol.getType())) {
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                            bw.write(IpScanUtil.getHostIp());
                            bw.flush();
                            LogUtil.d(TAG,"返回数据");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
