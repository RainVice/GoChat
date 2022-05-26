package com.rainvice.sockettest_1.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainvice.sockettest_1.Adapter.RvAdapter;
import com.rainvice.sockettest_1.R;
import com.rainvice.sockettest_1.activity.ChatActivity;
import com.rainvice.sockettest_1.bean.DialogueRecordBean;
import com.rainvice.sockettest_1.constant.IntentConstant;
import com.rainvice.sockettest_1.constant.Status;
import com.rainvice.sockettest_1.event.BusToChatEvent;
import com.rainvice.sockettest_1.event.BusToMessageEvent;
import com.rainvice.sockettest_1.protocol.MsgType;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.protocol.RvResponseProtocol;
import com.rainvice.sockettest_1.server.SendMessageServer;
import com.rainvice.sockettest_1.service.SocketServerService;
import com.rainvice.sockettest_1.utils.DataUtil;

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
        //初始化View
        initView(view);

        mIntent = new Intent(mContext, SocketServerService.class);

        //启动服务
        mContext.startService(mIntent);

        //绑定服务
        mContext.bindService(mIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        //初始化消息列表
        initRecyclerView();
    }

    private void initView(View view) {
        mRecyclerView = view.findViewById(R.id.message_recyclerview);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onResume() {
        super.onResume();
        notifyData();
    }




    @RequiresApi(api = Build.VERSION_CODES.N)
    private void notifyData() {
        ArrayList<DialogueRecordBean> dialogueRecordBeans = new ArrayList<>(mMessageMap.values());
        dialogueRecordBeans.sort(Comparator.comparing(DialogueRecordBean::getTimes).reversed());
        mRvAdapter.notifyData(dialogueRecordBeans);
    }

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
            String username = dialogueRecordBean.getUsername();
            String ip = dialogueRecordBean.getIp();
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
                        usernameView.setText(data);
                    }

                    @Override
                    public void error(RvResponseProtocol<String> result) {
                        usernameView.setText("未知用户");
                    }
                });
            } else {
                usernameView.setText(username);
            }

            itemView.setOnClickListener(view -> {
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra(IntentConstant.IP, ip);
                String name = DataUtil.getNameMap().get(ip);
                intent.putExtra(IntentConstant.USERNAME, username == null ? name == null?"未知用户": name : username);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}