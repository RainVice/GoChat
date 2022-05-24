package com.rainvice.sockettest_1.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rainvice.sockettest_1.Adapter.RvAdapter;
import com.rainvice.sockettest_1.R;
import com.rainvice.sockettest_1.bean.NearbyBean;
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

    private void initListener() {

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

        mImageView.setOnClickListener(view -> {
            ips.clear();
            scanNearby(mIpScanUtil);
        });



    }

    private void scanNearby(IpScanUtil ipScanUtil) {
        ipScanUtil.scanIp(new IpScanUtil.Callback() {
            @Override
            public void onFind(String ip) {
                LogUtil.d(TAG,"成功连接：" + ip);
                NearbyBean nearbyBean = new NearbyBean(ip);
                ips.add(nearbyBean);
                RvRequestProtocol<String> protocol = new RvRequestProtocol<>(MsgType.GET_NAME,"我想要名称");
                SendMessageServer sendMessageServer = new SendMessageServer(ip, protocol);
                sendMessageServer.sendMsg(new SendMessageServer.Callback() {
                    @Override
                    public void callback(RvResponseProtocol<String> result) {
                        String data = result.getData();
                        if (data == null){
                            return;
                        }
                        nearbyBean.setName(data);
//                        Toast.makeText(getContext(), data,Toast.LENGTH_SHORT).show();
                        LogUtil.d(TAG, data);
                    }
                });

            }

            @Override
            public void onFinish() {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                RvAdapter<NearbyBean> adapter = new RvAdapter<>(ips, R.layout.item_nearby_list, new RvAdapter.Callback<NearbyBean>() {
                    @Override
                    public void callback(View itemView, int position, NearbyBean nearbyBean) {
                        TextView ip = itemView.findViewById(R.id.ip);
                        TextView username = itemView.findViewById(R.id.username);
                        ip.setText(nearbyBean.getIp());
                        username.setText(nearbyBean.getName());
                    }
                });
                mRecyclerView.setAdapter(adapter);

            }
        });
    }

    private void initView(View view) {
        mRecyclerView = view.findViewById(R.id.nearby_recycler_view);
        mTextView = view.findViewById(R.id.device_name);
        mImageView = view.findViewById(R.id.refresh);
    }



}