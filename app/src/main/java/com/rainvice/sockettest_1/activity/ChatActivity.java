package com.rainvice.sockettest_1.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rainvice.sockettest_1.R;
import com.rainvice.sockettest_1.constant.IntentConstant;
import com.rainvice.sockettest_1.protocol.MsgType;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.protocol.RvResponseProtocol;
import com.rainvice.sockettest_1.server.SendMessageServer;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.back)
    ImageView mImageView;

    @BindView(R.id.username)
    TextView mTextView;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    @BindView(R.id.inputView)
    EditText mEditText;

    @BindView(R.id.send)
    Button mButton;

    private String mIp = "127.0.0.1";
    private String mUsername = "用户名";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        //初始化数据
        initData();
        //初始化视图
        initView();
        //初始化事件监听
        initListener();


    }

    private void initListener() {
        mImageView.setOnClickListener(view -> finish());

        mButton.setOnClickListener(view -> {
            //发送消息
            String msg = mEditText.getText().toString();
            sendMessage(msg);
        });
    }

    private void sendMessage(String msg) {
        if (Objects.nonNull(msg) && !"".equals(msg)){
            RvRequestProtocol<String> requestProtocol = new RvRequestProtocol<>(MsgType.MESSAGE, msg);
            SendMessageServer sendMessageServer = new SendMessageServer(mIp, requestProtocol);
            sendMessageServer.sendMsg(new SendMessageServer.Callback() {
                /**
                 * 发送对方已收到
                 * @param result
                 */
                @Override
                public void success(RvResponseProtocol<String> result) {
                    Toast.makeText(ChatActivity.this, "回复状态" + result.getStatus(), Toast.LENGTH_SHORT).show();
                }
                /**
                 * 发送失败或者对方掉线
                 * @param result
                 */
                @Override
                public void error(RvResponseProtocol<String> result) {

                }
            });
        }
    }

    private void initView() {
        mTextView.setText(mUsername);
    }

    private void initData() {
        Intent intent = getIntent();
        mIp = intent.getStringExtra(IntentConstant.IP);
        mUsername = intent.getStringExtra(IntentConstant.USERNAME);
    }
}