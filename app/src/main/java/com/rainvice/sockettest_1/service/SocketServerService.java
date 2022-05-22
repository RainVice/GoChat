package com.rainvice.sockettest_1.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.rainvice.sockettest_1.thread.SocketServerThread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServerService extends Service {
    String TAG = this.getClass().getSimpleName();


    Handler mHandler = new Handler(message -> {
        int what = message.what;

        return true;
    });


    public SocketServerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new CommunicateBinder();
    }


    @Override
    public void onCreate() {
        //初始化Socket
        initSocketServer();

    }

    private void initSocketServer() {
        //启动服务
        SocketServerThread socketServerThread = new SocketServerThread(mHandler);
        socketServerThread.start();
    }


    public class CommunicateBinder extends Binder{

        public void sendMsg(){
            new Thread(() -> {
                try {
                    Socket socket = new Socket("127.0.0.1", 6898);

                    //构建IO
                    InputStream is = socket.getInputStream();
                    OutputStream os = socket.getOutputStream();

                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                    //向服务器端发送一条消息
                    bw.write("测试客户端和服务器通信，服务器接收到消息返回到客户端\n");
                    bw.flush();

                    //读取服务器返回的消息
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String mess = br.readLine();
                    System.out.println("服务器："+mess);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        }

    }


}