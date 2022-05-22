package com.rainvice.sockettest_1.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.rainvice.sockettest_1.R;
import com.rainvice.sockettest_1.service.SocketServerService;

import butterknife.OnClick;
import butterknife.OnItemClick;

public class MessageFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();
    private Context mContext;

    //服务中的类，用于调用服务中的方法
    private SocketServerService.CommunicateBinder mCommunicateBinder;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Intent intent = new Intent(mContext, SocketServerService.class);

        //启动服务
        mContext.startService(intent);

        //绑定服务
        mContext.bindService(intent,mServiceConnection,Context.BIND_AUTO_CREATE);


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

}