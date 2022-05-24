package com.rainvice.sockettest_1.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rainvice.sockettest_1.Adapter.RvAdapter;
import com.rainvice.sockettest_1.R;
import com.rainvice.sockettest_1.protocol.MsgType;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.protocol.RvResponseProtocol;
import com.rainvice.sockettest_1.server.SendMessageServer;
import com.rainvice.sockettest_1.utils.IpScanUtil;
import com.rainvice.sockettest_1.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NonConstantResourceId")
public class NearbyFragment extends Fragment {


    private RecyclerView mRecyclerView;
    private TextView mTextView;

    private final String TAG = this.getClass().getSimpleName();
    private final List<String> ips = new ArrayList<>();


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



        IpScanUtil ipScanUtil = new IpScanUtil();
        scanNearby(ipScanUtil);
    }

    private void scanNearby(IpScanUtil ipScanUtil) {
        ipScanUtil.scanIp(new IpScanUtil.Callback() {
            @Override
            public void onFind(String ip) {
                LogUtil.d(TAG,"成功连接：" + ip);
                ips.add(ip);

                RvRequestProtocol<String> protocol = new RvRequestProtocol<>(MsgType.GET_NAME,"我想要名称");
                SendMessageServer sendMessageServer = new SendMessageServer(ip, protocol);
                sendMessageServer.sendMsg(new SendMessageServer.Callback() {
                    @Override
                    public void callback(RvResponseProtocol<String> result) {

                        String data = result.getData();
                        if (data == null){
                            return;
                        }
                        Toast.makeText(getContext(), data,Toast.LENGTH_SHORT).show();
                        LogUtil.d(TAG, data);
                    }
                });

            }

            @Override
            public void onFinish() {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                RvAdapter<String> adapter = new RvAdapter<>(ips, R.layout.item_nearby_list, new RvAdapter.Callback<String>() {
                    @Override
                    public void callback(View itemView, int position, String s) {
                        TextView textView = itemView.findViewById(R.id.ip);
                        textView.setText(s);
                    }
                });
                mRecyclerView.setAdapter(adapter);

            }
        });
    }

    private void initView(View view) {
        mRecyclerView = view.findViewById(R.id.nearby_recycler_view);
    }
}