package com.rainvice.gochat.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rainvice.gochat.Adapter.RvAdapter;
import com.rainvice.gochat.R;
import com.rainvice.gochat.activity.ChatActivity;
import com.rainvice.gochat.bean.NearbyBean;
import com.rainvice.gochat.constant.IntentConstant;
import com.rainvice.gochat.event.BusToNearbyEvent;
import com.rainvice.gochat.protocol.MsgType;
import com.rainvice.gochat.protocol.RvRequestProtocol;
import com.rainvice.gochat.protocol.RvResponseProtocol;
import com.rainvice.gochat.server.SendMessageServer;
import com.rainvice.gochat.utils.DataUtil;
import com.rainvice.gochat.utils.IpUtil;
import com.rainvice.gochat.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressLint("NonConstantResourceId")
public class NearbyFragment extends Fragment {


    private RecyclerView mRecyclerView;
    private TextView mTextView;
    private ImageView mImageView;

    private final String TAG = this.getClass().getSimpleName();
    private final List<NearbyBean> ips = new ArrayList<>();
    private IpUtil mIpUtil;
    private View.OnClickListener mScanIp;
    private View.OnClickListener mDisScanIp;
    private RvAdapter<NearbyBean> mAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nearby, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        initView(view);

        mTextView.setText(DataUtil.getUsername() + "(点击设置用户名)");

        initListener();
        mIpUtil = new IpUtil();
        scanNearby(mIpUtil);
    }

    /**
     * 事件监听
     */
    private void initListener() {
        mScanIp = view -> {
            Toast.makeText(getContext(), "开始扫描", Toast.LENGTH_SHORT).show();
            mImageView.setOnClickListener(mDisScanIp);
            scanNearby(mIpUtil);
        };

        mTextView.setOnClickListener(view -> {
            final EditText inputServer = new EditText(getActivity());
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("设置用户名").setView(inputServer)
                    .setNegativeButton("取消", null);
            builder.setPositiveButton("确定", (dialog, which) -> {
                String s = inputServer.getText().toString();
                if (s.equals("")){
                    Toast.makeText(getContext(), "用户名不能为空", Toast.LENGTH_SHORT).show();
                }
                mTextView.setText(s);
                DataUtil.setUsername(s);
            });
            builder.show();
        });

        mDisScanIp = view -> Toast.makeText(getContext(), "等待扫描完成", Toast.LENGTH_SHORT).show();
        mImageView.setOnClickListener(mDisScanIp);
    }

    /**
     * 扫描附近设备
     * @param ipUtil
     */
    private void scanNearby(@NonNull IpUtil ipUtil) {
        ips.clear();
        ipUtil.scanIp(new IpUtil.Callback() {
            /**
             * 找到一个设备
             * @param ip 找到的设备IP
             */
            @Override
            public void onFind(String ip) {
                LogUtil.d(TAG,"成功连接：" + ip);
                //发送消息，获取名称
                RvRequestProtocol<String> protocol = new RvRequestProtocol<>(MsgType.GET_NAME,"我想要名称");
                SendMessageServer sendMessageServer = new SendMessageServer(ip, protocol);
                sendMessageServer.sendTCPMsg(new SendMessageServer.Callback() {
                    @Override
                    public void success(RvResponseProtocol<String> result) {
                        String data = result.getData();
                        if (data == null){
                            return;
                        }
                        //设置保存用户名
                        DataUtil.getNameMap().put(ip,data);
                        LogUtil.d(TAG, data);
                    }

                    @Override
                    public void error(RvResponseProtocol<String> result) {

                    }
                });

            }

            /**
             * 完成扫描
             */
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onFinish() {
                Toast.makeText(getContext(), "扫描完成", Toast.LENGTH_SHORT).show();
                mImageView.setOnClickListener(mScanIp);
                Map<String, String> nameMap = DataUtil.getNameMap();
                nameMap.forEach((key, val) -> {
                    ips.add(new NearbyBean(key,val));
                });
                //设置列表
                setRecyclerView();
            }
        });
    }

    /**
     * 设置列表
     */
    private void setRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (mAdapter == null){
            mAdapter = new RvAdapter<>(ips, R.layout.item_nearby_list, new RvAdapter.Callback<NearbyBean>() {
                @Override
                public void callback(View itemView, int position, NearbyBean nearbyBean) {
                    TextView ipView = itemView.findViewById(R.id.ip);
                    TextView usernameView = itemView.findViewById(R.id.username);
                    String ip = nearbyBean.getIp();
                    ipView.setText(ip);
                    String username = nearbyBean.getName();
                    usernameView.setText(username);

                    itemView.setOnClickListener(view -> {
                        Intent intent = new Intent(getContext(), ChatActivity.class);
                        intent.putExtra(IntentConstant.IP, ip);
                        intent.putExtra(IntentConstant.USERNAME,username);
                        startActivity(intent);
                    });

                }
            });
            mRecyclerView.setAdapter(mAdapter);
        }else {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 初始化 View
     * @param view
     */
    private void initView(View view) {
        mRecyclerView = view.findViewById(R.id.nearby_recycler_view);
        mTextView = view.findViewById(R.id.device_name);
        mImageView = view.findViewById(R.id.refresh);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null){
            ips.clear();
            Map<String, String> nameMap = DataUtil.getNameMap();
            nameMap.forEach((key, val) -> {
                ips.add(new NearbyBean(key, val));
            });
            //设置列表
            mAdapter.notifyData(ips);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Subscribe
    public void onEvent(BusToNearbyEvent event){
        if (mAdapter != null){
            ips.clear();
            Map<String, String> nameMap = DataUtil.getNameMap();
            nameMap.forEach((key, val) -> ips.add(new NearbyBean(key, val)));
            //设置列表
            mAdapter.notifyData(ips);
        }
    }

}