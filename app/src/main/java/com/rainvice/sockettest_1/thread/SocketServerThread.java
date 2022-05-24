package com.rainvice.sockettest_1.thread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.rainvice.sockettest_1.constant.Status;
import com.rainvice.sockettest_1.protocol.MsgType;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.protocol.RvResponseProtocol;
import com.rainvice.sockettest_1.utils.DataUtil;
import com.rainvice.sockettest_1.utils.IpScanUtil;
import com.rainvice.sockettest_1.utils.LogUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
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
                        LogUtil.d(TAG, "客户端:" + socket.getInetAddress().getHostAddress() + " 已连接到服务器");
                        //将消息发送到统一消息处理中心处理
                        messageManage(socket);
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

    private void messageManage(Socket socket) throws IOException {
        socket.setSoTimeout(1000);
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //读取客户端发送来的消息
        String mess = br.readLine();
        Gson gson = new Gson();
        if (Objects.nonNull(mess)){
            RvRequestProtocol requestProtocol = null;
            LogUtil.d("接收到消息", mess);
            try {
                //转换为 bean 类
                requestProtocol = gson.fromJson(mess, RvRequestProtocol.class);
            } catch (Exception e) {
                LogUtil.d("转换Bean类失败",e.getMessage());
            }

            if (Objects.nonNull(requestProtocol)) {
                //获取用户名
                if (MsgType.GET_NAME.equals(requestProtocol.getType())) {
                    RvResponseProtocol<String> responseProtocol = new RvResponseProtocol<>(MsgType.RECEIPT, RvResponseProtocol.OK, DataUtil.getUsername());
                    String s = gson.toJson(responseProtocol);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    bw.write(s);
                    bw.flush();
                }else {
                    Message message = new Message();
                    message.what = Status.SUCCESS;
                    message.obj = requestProtocol;
                    mHandler.sendMessage(message);
                }
            }else {
                fail(socket, gson,"数据接收失败，转换 Bean 类失败");
            }
        }else {
            fail(socket, gson,"数据接收失败,接收到空数据");
        }
    }

    /**
     * 接受数据失败
     * @param socket
     * @param gson
     * @throws IOException
     */
    private void fail(Socket socket, Gson gson,String msg) throws IOException {
        Message message = new Message();
        message.what = Status.ERROR;
        mHandler.sendMessage(message);
        RvResponseProtocol<String> responseProtocol = new RvResponseProtocol<>(MsgType.RECEIPT, RvResponseProtocol.FAIL, msg);
        String s = gson.toJson(responseProtocol);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        bw.write(s);
        bw.flush();
    }


}
