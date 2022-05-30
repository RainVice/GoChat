package com.rainvice.sockettest_1.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.rainvice.sockettest_1.bean.DialogBean;
import com.rainvice.sockettest_1.bean.DialogueRecordBean;
import com.rainvice.sockettest_1.bean.GroupChatBean;
import com.rainvice.sockettest_1.bean.InputMsgBean;
import com.rainvice.sockettest_1.constant.DataType;
import com.rainvice.sockettest_1.constant.Status;
import com.rainvice.sockettest_1.event.BusToGroupEvent;
import com.rainvice.sockettest_1.event.BusToNearbyEvent;
import com.rainvice.sockettest_1.protocol.MsgType;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.thread.UDPSocketClientThread;
import com.rainvice.sockettest_1.thread.UDPSocketServerThread;
import com.rainvice.sockettest_1.utils.DataUtil;
import com.rainvice.sockettest_1.utils.LogUtil;
import com.rainvice.sockettest_1.utils.StrZipUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.S)
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
            case Status.GROUP_SUCCESS:
                InputMsgBean obj = (InputMsgBean) message.obj;
                //发送者发送的消息
                RvRequestProtocol<String> protocol = obj.getProtocol();
                //获取发送者的 ip
                String ip = obj.getIp();
                //获取当前时间
                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

                //消息内容
                String data = protocol.getData();
                Gson gson = new Gson();
                GroupChatBean groupChatBean = gson.fromJson(data, GroupChatBean.class);
                //获取消息类型
                String type = groupChatBean.getMsgType();

                DataUtil.getNameMap().put(ip,groupChatBean.getUsername());

                //判断消息类型
                if (type.equals(MsgType.MESSAGE)) {
                    //处理文字消息
                    manageWord(groupChatBean, time);
                } else if (type.equals(MsgType.IMAGE)) {
                    manageImage(groupChatBean,time);
                }
                EventBus.getDefault().post(new BusToGroupEvent(Status.SUCCESS));
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


    /**
     * 处理文字消息
     *  @param groupChatBean 消息内容
     * @param time     时间
     */
    private void manageWord(GroupChatBean groupChatBean, String time) {
        //保存消息
        DialogBean dialogBean = new DialogBean(false, time + groupChatBean.getUsername() + "，" + DataUtil.getIp(), DataType.WORD, groupChatBean.getMessage());
        saveDialog(MsgType.GROUP_MESSAGE, dialogBean);
    }


    /**
     * 处理图片消息
     *  @param groupChatBean 消息内容
     * @param time     时间
     */
    private void manageImage(GroupChatBean groupChatBean, String time) {

        String message = groupChatBean.getMessage();
        //String uncompress = StrZipUtil.uncompress(message);
        //4.将base64解码，转为bitmap并显示在imageview中看效果
        //System.out.println("解压" + uncompress);
        byte[] decodedString = Base64.decode(message, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        //创建消息
        DialogBean dialogBean = new DialogBean(false, time + groupChatBean.getUsername() + "，" + DataUtil.getIp() , DataType.IMAGE,bitmap);
        saveDialog(MsgType.GROUP_MESSAGE, dialogBean);

    }

    /**
     * 保存消息
     * @param ip 发送者的 ip 地址
     * @param dialogBean 消息内容
     */
    @NonNull
    private void saveDialog(String ip, DialogBean dialogBean) {
        //消息列表
        Map<String, DialogueRecordBean> messageMap = DataUtil.getMessageMap();
        //获取发送者的昵称
        String username = DataUtil.getNameMap().get(ip);

        DialogueRecordBean dialogueRecordBean = messageMap.get(ip);
        //判断是否有消息记录
        if (dialogueRecordBean != null) {
            //添加消息
            dialogueRecordBean.setUsername(username);
            dialogueRecordBean.getDialogs().add(dialogBean);
        } else {
            //添加消息记录
            dialogueRecordBean = new DialogueRecordBean(username, ip);
            dialogueRecordBean.setUsername(username);
            //添加消息
            dialogueRecordBean.getDialogs().add(dialogBean);
            messageMap.put(ip, dialogueRecordBean);
        }
        dialogueRecordBean.setTimes(System.currentTimeMillis());
        //Toast.makeText(this, "接收消息成功，内容是：" + data, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

}