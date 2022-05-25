package com.rainvice.sockettest_1.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.rainvice.sockettest_1.Adapter.RvAdapter;
import com.rainvice.sockettest_1.R;
import com.rainvice.sockettest_1.activity.ChatActivity;
import com.rainvice.sockettest_1.bean.NearbyBean;
import com.rainvice.sockettest_1.constant.IntentConstant;
import com.rainvice.sockettest_1.protocol.MsgType;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.protocol.RvResponseProtocol;
import com.rainvice.sockettest_1.server.SendMessageServer;
import com.rainvice.sockettest_1.utils.DataUtil;
import com.rainvice.sockettest_1.utils.IpScanUtil;
import com.rainvice.sockettest_1.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NonConstantResourceId")
public class NearbyFragment extends Fragment {


    private RecyclerView mRecyclerView;
    private TextView mTextView;
    private ImageView mImageView;

    private final String TAG = this.getClass().getSimpleName();
    private final List<NearbyBean> ips = new ArrayList<>();
    private IpScanUtil mIpScanUtil;
    private View.OnClickListener mScanIp;
    private View.OnClickListener mDisScanIp;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nearby, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initView(view);

        mTextView.setText(DataUtil.getUsername() + "(点击设置用户名)");

        initListener();
        mIpScanUtil = new IpScanUtil();
        scanNearby(mIpScanUtil);
    }

    /**
     * 事件监听
     */
    private void initListener() {
        mScanIp = view -> {
            Toast.makeText(getContext(), "开始扫描", Toast.LENGTH_SHORT).show();
            mImageView.setOnClickListener(mDisScanIp);
            ips.clear();
            scanNearby(mIpScanUtil);
        };

        mTextView.setOnClickListener(view -> {
            final EditText inputServer = new EditText(getActivity());
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("设置用户名").setView(inputServer)
                    .setNegativeButton("取消", null);
            builder.setPositiveButton("确定", (dialog, which) -> {
                String s = inputServer.getText().toString();
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
     * @param ipScanUtil
     */
    private void scanNearby(@NonNull IpScanUtil ipScanUtil) {
        ipScanUtil.scanIp(new IpScanUtil.Callback() {
            /**
             * 找到一个设备
             * @param ip 找到的设备IP
             */
            @Override
            public void onFind(String ip) {
                LogUtil.d(TAG,"成功连接：" + ip);
                //发送消息，获取名称
                NearbyBean nearbyBean = new NearbyBean(ip);
                ips.add(nearbyBean);
                RvRequestProtocol<String> protocol = new RvRequestProtocol<>(MsgType.GET_NAME,"我想要名称");
                SendMessageServer sendMessageServer = new SendMessageServer(ip, protocol);
                sendMessageServer.sendMsg(new SendMessageServer.Callback() {
                    @Override
                    public void success(RvResponseProtocol<String> result) {
                        String data = result.getData();
                        if (data == null){
                            return;
                        }
                        //设置保存用户名
                        DataUtil.getNameMap().put(ip,data);
                        nearbyBean.setName(data);
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
            @Override
            public void onFinish() {
                Toast.makeText(getContext(), "扫描完成", Toast.LENGTH_SHORT).show();
                mImageView.setOnClickListener(mScanIp);
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
        RvAdapter<NearbyBean> adapter = new RvAdapter<>(ips, R.layout.item_nearby_list, new RvAdapter.Callback<NearbyBean>() {
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
        mRecyclerView.setAdapter(adapter);
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



}