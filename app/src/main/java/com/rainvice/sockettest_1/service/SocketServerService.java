package com.rainvice.sockettest_1.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.rainvice.sockettest_1.R;
import com.rainvice.sockettest_1.activity.ChatActivity;
import com.rainvice.sockettest_1.constant.IntentConstant;
import com.rainvice.sockettest_1.event.BusToChatEvent;
import com.rainvice.sockettest_1.bean.DialogBean;
import com.rainvice.sockettest_1.bean.DialogueRecordBean;
import com.rainvice.sockettest_1.bean.InputMsgBean;
import com.rainvice.sockettest_1.constant.DataType;
import com.rainvice.sockettest_1.constant.Status;
import com.rainvice.sockettest_1.event.BusToMessageEvent;
import com.rainvice.sockettest_1.protocol.MsgType;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.thread.TCPSocketServerThread;
import com.rainvice.sockettest_1.thread.UDPSocketClientThread;
import com.rainvice.sockettest_1.thread.UDPSocketServerThread;
import com.rainvice.sockettest_1.utils.DataUtil;
import com.rainvice.sockettest_1.utils.IpUtil;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

@RequiresApi(api = Build.VERSION_CODES.N)
public class SocketServerService extends Service {
    String TAG = this.getClass().getSimpleName();


    Handler mHandler = new Handler(message -> {
        int what = message.what;
        InputMsgBean obj = (InputMsgBean) message.obj;
        switch (what) {
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
                } else if (type.equals(MsgType.IMAGE)) {


                }
                EventBus.getDefault().post(new BusToChatEvent(Status.SUCCESS));
                break;
            case Status.ERROR:
//                Toast.makeText(this, "接收消息失败", Toast.LENGTH_SHORT).show();
                break;
            case Status.FINISH:
//                Toast.makeText(this, "已完成返回", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    });

    /**
     * 处理文字消息
     *
     * @param ip       发送者的 ip
     * @param protocol 消息内容
     * @param format   时间
     */
    private void manageWord(String ip, RvRequestProtocol<String> protocol, String format) {
        //消息内容
        String data = protocol.getData();
        //获取发送者的昵称
        String username = DataUtil.getNameMap().get(ip);
        //保存消息
        Map<String, DialogueRecordBean> messageMap = DataUtil.getMessageMap();
        DialogBean dialogBean = new DialogBean(false, format, DataType.WORD, data);
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
        EventBus.getDefault().post(new BusToMessageEvent(Status.SUCCESS));
        //Toast.makeText(this, "接收消息成功，内容是：" + data, Toast.LENGTH_SHORT).show();


        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(IntentConstant.IP, ip);
        intent.putExtra(IntentConstant.USERNAME, username);
        int noticeId = IpUtil.getIp(ip);
        PendingIntent pendingIntent =  PendingIntent.getActivity(this , noticeId , intent, PendingIntent.FLAG_MUTABLE);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "message";
            long count = dialogueRecordBean.getDialogs().stream().filter(dialog -> !dialog.isRead()).count();
            Notification notification = new Notification.Builder(this, channelId)
                    .setContentTitle(username + "发来一条消息")
                    .setContentText(data + (count == 1 ? "" : "\n" + count + "条未读"))
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))   //设置大图标
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            NotificationChannel channel = new NotificationChannel(channelId, getResources().getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(noticeId, notification);

        } else {
            // 创建通知(标题、内容、图标)
            long count = dialogueRecordBean.getDialogs().stream().filter(dialog -> !dialog.isRead()).count();
            Notification notification = new Notification.Builder(this)
                    .setContentTitle(username + "发来一条消息")
                    .setContentText(data + (count == 1 ? "" : "\n" + count + "条未读"))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            // 创建通知管理器
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // 发送通知
            manager.notify(noticeId, notification);
        }

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
        initTCPSocketServer();
    }


    private void initTCPSocketServer() {
        //启动服务
        TCPSocketServerThread socketServerThread = new TCPSocketServerThread(mHandler);
        socketServerThread.start();
    }


    public class CommunicateBinder extends Binder {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}