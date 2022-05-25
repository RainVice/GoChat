package com.rainvice.sockettest_1.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.rainvice.sockettest_1.bean.DialogBean;
import com.rainvice.sockettest_1.bean.DialogueRecordBean;
import com.rainvice.sockettest_1.bean.InputMsgBean;
import com.rainvice.sockettest_1.constant.DataType;
import com.rainvice.sockettest_1.constant.Status;
import com.rainvice.sockettest_1.protocol.MsgType;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.thread.SocketServerThread;
import com.rainvice.sockettest_1.utils.DataUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class SocketServerService extends Service {
    String TAG = this.getClass().getSimpleName();


    Handler mHandler = new Handler(message -> {
        int what = message.what;
        InputMsgBean obj = (InputMsgBean) message.obj;
        switch (what){
            case Status.SUCCESS:
                //发送者发送的消息
                RvRequestProtocol<String> protocol = obj.getProtocol();
                //获取发送者的 ip
                String ip = obj.getIp();
                //获取消息类型
                String type = protocol.getType();
                //获取当前时间
                String format = new SimpleDateFormat("HH:mm:ss").format(new Date());
                //判断消息类型
                if (type.equals(MsgType.MESSAGE)) {
                    //处理文字消息
                    manageWord(ip, protocol, format);
                }else if (type.equals(MsgType.IMAGE)){



                }
                break;
            case Status.ERROR:
                Toast.makeText(this, "接收消息失败", Toast.LENGTH_SHORT).show();
                break;
            case Status.FINISH:
                Toast.makeText(this, "已完成返回", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    });

    /**
     * 处理文字消息
     * @param obj 消息信息
     * @param protocol
     * @param format
     */
    private void manageWord(String ip, RvRequestProtocol<String> protocol, String format) {
        //消息内容
        String data = protocol.getData();
        //获取发送者的昵称
        String username = DataUtil.getNameMap().get("ip");
        //保存消息
        Map<String, DialogueRecordBean> messageMap = DataUtil.getMessageMap();
        DialogBean dialogBean = new DialogBean(false, format,DataType.WORD,data);
        DialogueRecordBean dialogueRecordBean = messageMap.get(ip);
        //判断是否有消息记录
        if (dialogueRecordBean != null) {
            //添加消息
            dialogueRecordBean.setUsername(username);
            dialogueRecordBean.getDialogs().add(dialogBean);
        }else {
            //添加消息记录
            dialogueRecordBean = new DialogueRecordBean(username,ip);
            dialogueRecordBean.setUsername(username);
            //添加消息
            dialogueRecordBean.getDialogs().add(dialogBean);
            messageMap.put(ip,dialogueRecordBean);
        }
        Toast.makeText(this, "接收消息成功，内容是："+data, Toast.LENGTH_SHORT).show();
    }


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