package com.rainvice.sockettest_1.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rainvice.sockettest_1.Adapter.RvAdapter;
import com.rainvice.sockettest_1.R;
import com.rainvice.sockettest_1.constant.Status;
import com.rainvice.sockettest_1.event.BusToChatEvent;
import com.rainvice.sockettest_1.bean.DialogBean;
import com.rainvice.sockettest_1.bean.DialogueRecordBean;
import com.rainvice.sockettest_1.constant.DataType;
import com.rainvice.sockettest_1.constant.IntentConstant;
import com.rainvice.sockettest_1.protocol.MsgType;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.protocol.RvResponseProtocol;
import com.rainvice.sockettest_1.server.SendMessageServer;
import com.rainvice.sockettest_1.utils.DataUtil;
import com.rainvice.sockettest_1.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.back)
    LinearLayout mLinearLayout;

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
    private DialogueRecordBean mDialogueRecordBean;
    private Map<String, DialogueRecordBean> mMessageMap;
    private RvAdapter<DialogBean> mRvAdapter;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        //初始化数据
        initData();
        //初始化视图
        initView();
        //初始化事件监听
        initListener();
    }

    private void initListener() {
        mLinearLayout.setOnClickListener(view -> finish());

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
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void success(RvResponseProtocol<String> result) {
                    Toast.makeText(ChatActivity.this, "回复状态" + result.getStatus(), Toast.LENGTH_SHORT).show();
                    //获取消息记录
                    String format = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    DialogBean dialogBean = new DialogBean(true, format, DataType.WORD, msg,true);
                    //判断是否有消息记录
                    if (mDialogueRecordBean == null){
                        //添加相关消息记录
                        mDialogueRecordBean = new DialogueRecordBean(mUsername,mIp);
                        mDialogueRecordBean.getDialogs().add(dialogBean);
                        mMessageMap.put(mIp,mDialogueRecordBean);
                        initRecyclerView();
                    }else{
                        List<DialogBean> dialogs = mDialogueRecordBean.getDialogs();
                        int position = dialogs.size();
                        dialogs.add(dialogBean);
                        mRvAdapter.notifyItemRangeInserted(position,1);
//                        mRvAdapter.notifyDataSetChanged();
                        mRecyclerView.scrollToPosition(position);
                    }
                    mDialogueRecordBean.setTimes(System.currentTimeMillis());
                }
                /**
                 * 发送失败或者对方掉线
                 * @param result
                 */
                @Override
                public void error(RvResponseProtocol<String> result) {
                    Toast.makeText(ChatActivity.this, "发送失败或者对方掉线", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Subscribe
    public void onEvent(BusToChatEvent event) {
        int status = event.getStatus();
        LogUtil.d("接收到刷新指令", String.valueOf(status));
        switch (status){
            case Status.SUCCESS:
                if (mDialogueRecordBean == null){
                    mDialogueRecordBean = mMessageMap.get(mIp);
                    initRecyclerView();
                }else {
                    List<DialogBean> dialogs = mDialogueRecordBean.getDialogs();
                    int position = dialogs.size() - 1;
                    mRvAdapter.notifyItemRangeInserted(position,1);
//                    mRvAdapter.notifyDataSetChanged();
                    mRecyclerView.scrollToPosition(position);
                }
                break;

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initView() {
        mTextView.setText(mUsername);

        initRecyclerView();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initRecyclerView() {

        if (mDialogueRecordBean != null){

            List<DialogBean> dialogs = mDialogueRecordBean.getDialogs();
            dialogs.forEach(dialog -> dialog.setRead(true));
            mRvAdapter = new RvAdapter<>(dialogs, R.layout.item_chat_list, (itemView, position, dialogBean) -> {
                LinearLayout left = itemView.findViewById(R.id.left_message);
                LinearLayout right = itemView.findViewById(R.id.right_message);
                if(dialogBean.isMine()){
                    left.setVisibility(View.GONE);

                    TextView time = itemView.findViewById(R.id.right_time);
                    TextView content = itemView.findViewById(R.id.right_content);

                    time.setText(dialogBean.getTime());
                    content.setText((String)dialogBean.getContent());
                }else{
                    right.setVisibility(View.GONE);

                    TextView time = itemView.findViewById(R.id.left_time);
                    TextView content = itemView.findViewById(R.id.left_content);

                    time.setText(dialogBean.getTime());
                    content.setText((String)dialogBean.getContent());
                }

            });
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(mRvAdapter);
            mRecyclerView.scrollToPosition(mRvAdapter.getItemCount()-1);

        }

    }

    private void initData() {
        Intent intent = getIntent();
        mIp = intent.getStringExtra(IntentConstant.IP);
        mUsername = intent.getStringExtra(IntentConstant.USERNAME);

        mMessageMap = DataUtil.getMessageMap();
        mDialogueRecordBean = mMessageMap.get(mIp);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}