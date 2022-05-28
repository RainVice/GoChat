package com.rainvice.sockettest_1.fragment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rainvice.sockettest_1.Adapter.RvAdapter;
import com.rainvice.sockettest_1.R;
import com.rainvice.sockettest_1.activity.ChatActivity;
import com.rainvice.sockettest_1.bean.DialogueRecordBean;
import com.rainvice.sockettest_1.constant.IntentConstant;
import com.rainvice.sockettest_1.constant.Status;
import com.rainvice.sockettest_1.event.BusToMessageEvent;
import com.rainvice.sockettest_1.event.BusToNearbyEvent;
import com.rainvice.sockettest_1.protocol.MsgType;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.protocol.RvResponseProtocol;
import com.rainvice.sockettest_1.server.SendMessageServer;
import com.rainvice.sockettest_1.service.SocketServerService;
import com.rainvice.sockettest_1.utils.DataUtil;
import com.rainvice.sockettest_1.utils.IpUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

public class MessageFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();
    private Context mContext;

    //服务中的类，用于调用服务中的方法
    private SocketServerService.CommunicateBinder mCommunicateBinder;
    private Intent mIntent;
    private RecyclerView mRecyclerView;
    private RvAdapter<DialogueRecordBean> mRvAdapter;
    private Map<String, DialogueRecordBean> mMessageMap;
    private ImageView mImageView;
    private TextView mIpView;
    private NetworkChangeReceiver mNetworkChangeReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //开启服务
        mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        //创建IntentFilter实例，并添加action
        IntentFilter intentFilter = new IntentFilter();
        //当网络发生变化时，系统发出的广播为android.net.conn.CONNECTIVITY_CHANGE
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        //创建NetworkChangeReceiver实例
        mNetworkChangeReceiver = new NetworkChangeReceiver();
        //调用registerReceiver（）方法进行注册
        getContext().registerReceiver(mNetworkChangeReceiver, intentFilter);


        //初始化View
        initView(view);

        //初始化点击事件
        initListener();

        mIntent = new Intent(mContext, SocketServerService.class);

        //启动服务
        mContext.startService(mIntent);

        //绑定服务
        mContext.bindService(mIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        //初始化消息列表
        initRecyclerView();
    }

    private void initListener() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_message, null, false);
        TextView ipView = view.findViewById(R.id.ip);
        TextView text = view.findViewById(R.id.text);
        AlertDialog alertDialog = builder.setView(view)
                .setTitle("添加聊天")
                .setNegativeButton("取消",null)
                .setPositiveButton("确定", null)
                .create();
        mImageView.setOnClickListener(v -> {
            alertDialog.show();
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                String ip = ipView.getText().toString();
                SendMessageServer sendMessageServer = new SendMessageServer(ip, new RvRequestProtocol<>(MsgType.GET_NAME, "我要名称"));
                sendMessageServer.sendMsg(new SendMessageServer.Callback() {
                    @Override
                    public void success(RvResponseProtocol<String> result) {
                        String data = result.getData();
                        if (data == null){
                            text.setVisibility(View.VISIBLE);
                        }
                        DataUtil.getNameMap().put(ip,data);
                        Intent intent = new Intent(getContext(), ChatActivity.class);
                        intent.putExtra(IntentConstant.IP, ip);
                        intent.putExtra(IntentConstant.USERNAME, data);
                        startActivity(intent);
                        alertDialog.cancel();
                    }

                    @Override
                    public void error(RvResponseProtocol<String> result) {
                        text.setVisibility(View.VISIBLE);
                    }
                });
            });
        });

    }

    private void initView(View view) {
        mRecyclerView = view.findViewById(R.id.message_recyclerview);
        mImageView = view.findViewById(R.id.add);
        mIpView = view.findViewById(R.id.ip);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onResume() {
        super.onResume();
        notifyData();
    }


    /**
     * 刷新数据
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void notifyData() {
        ArrayList<DialogueRecordBean> dialogueRecordBeans = new ArrayList<>(mMessageMap.values());
        dialogueRecordBeans.sort(Comparator.comparing(DialogueRecordBean::getTimes).reversed());
        mRvAdapter.notifyData(dialogueRecordBeans);
    }

    /**
     * 初始化列表
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initRecyclerView() {
        mMessageMap = DataUtil.getMessageMap();
        ArrayList<DialogueRecordBean> dialogueRecordBeans = new ArrayList<>(mMessageMap.values());
        dialogueRecordBeans.sort(Comparator.comparing(DialogueRecordBean::getTimes).reversed());
        mRvAdapter = new RvAdapter<>(dialogueRecordBeans, R.layout.item_message_list, (itemView, position, dialogueRecordBean) -> {
            long count = dialogueRecordBean.getDialogs().stream().filter(dialogBean -> !dialogBean.isRead()).count();
            CardView msgNumView = itemView.findViewById(R.id.msg_num);
            TextView usernameView = itemView.findViewById(R.id.username);
            TextView ipView = itemView.findViewById(R.id.ip);
            TextView numView = itemView.findViewById(R.id.num);
            String ip = dialogueRecordBean.getIp();
            String username = DataUtil.getNameMap().get(ip);
            ipView.setText(ip);
            if (count == 0) {
                msgNumView.setVisibility(View.GONE);
            } else {
                numView.setText(String.valueOf(count));
                msgNumView.setVisibility(View.VISIBLE);
            }
            if (Objects.isNull(username)) {
                RvRequestProtocol<String> protocol = new RvRequestProtocol<>(MsgType.GET_NAME, "给我名称");
                SendMessageServer sendMessageServer = new SendMessageServer(ip, protocol);
                sendMessageServer.sendMsg(new SendMessageServer.Callback() {
                    @Override
                    public void success(RvResponseProtocol<String> result) {
                        String data = result.getData();
                        DataUtil.getNameMap().put(ip, data);
                        dialogueRecordBean.setUsername(data);
                        usernameView.setText(data);
                    }

                    @Override
                    public void error(RvResponseProtocol<String> result) {
                        String s = dialogueRecordBean.getUsername();
                        if (s == null) {
                            usernameView.setText("未知用户");
                            dialogueRecordBean.setUsername("未知用户");
                        } else {
                            usernameView.setText(s);
                        }
                    }
                });
            } else {
                usernameView.setText(username);
            }

            itemView.setOnClickListener(view -> {
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra(IntentConstant.IP, ip);
                String name = DataUtil.getNameMap().get(ip);
                if (name == null){
                    intent.putExtra(IntentConstant.USERNAME, usernameView.getText().toString());
                }else {
                    intent.putExtra(IntentConstant.USERNAME, name);
                }
                startActivity(intent);
            });

        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mRvAdapter);
    }

    //拿到服务中的类，用于调用服务中的方法
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mCommunicateBinder = (SocketServerService.CommunicateBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Subscribe
    public void onEvent(BusToMessageEvent event) {
        if (event.getStatus() == Status.SUCCESS) {
            notifyData();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Subscribe
    public void onEvent(BusToNearbyEvent event) {
        if (event.getStatus() == Status.SUCCESS) {
            notifyData();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        getContext().unregisterReceiver(mNetworkChangeReceiver);
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 如果相等的话就说明网络状态发生了变化
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                String hostIp = IpUtil.getHostIp();
                DataUtil.setIp(hostIp);
                mIpView.setText(hostIp);
            }
        }
    }


}