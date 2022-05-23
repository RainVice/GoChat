package com.rainvice.sockettest_1.fragment;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainvice.sockettest_1.Adapter.RvAdapter;
import com.rainvice.sockettest_1.R;
import com.rainvice.sockettest_1.protocol.MsgType;
import com.rainvice.sockettest_1.protocol.RainviceProtocol;
import com.rainvice.sockettest_1.server.SendMessageServer;
import com.rainvice.sockettest_1.utils.IpScanUtil;
import com.rainvice.sockettest_1.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressLint("NonConstantResourceId")
public class NearbyFragment extends Fragment {


    private RecyclerView mRecyclerView;

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
        ipScanUtil.scanIp(new IpScanUtil.Callback() {
            @Override
            public void onFind(String ip) {
                LogUtil.d(TAG,"成功连接：" + ip);
                ips.add(ip);

                RainviceProtocol<String> protocol = new RainviceProtocol<>(MsgType.GET_NAME);
                SendMessageServer sendMessageServer = new SendMessageServer(ip, protocol);
                sendMessageServer.sendMsg(new SendMessageServer.Callback() {
                    @Override
                    public void callback(RainviceProtocol<String> result) {
                        LogUtil.d(TAG,result.getData());
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

                        itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                RainviceProtocol<String> protocol = new RainviceProtocol<>(MsgType.GET_NAME);
                                SendMessageServer sendMessageServer = new SendMessageServer("192.168.1.213", protocol);
                                sendMessageServer.sendMsg(new SendMessageServer.Callback() {
                                    @Override
                                    public void callback(RainviceProtocol<String> result) {
                                        LogUtil.d(TAG,result.getData());
                                    }
                                });
                            }
                        });

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