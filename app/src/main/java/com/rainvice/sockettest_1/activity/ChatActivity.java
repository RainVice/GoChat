package com.rainvice.sockettest_1.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.rainvice.sockettest_1.event.BusToNearbyEvent;
import com.rainvice.sockettest_1.protocol.MsgType;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.protocol.RvResponseProtocol;
import com.rainvice.sockettest_1.server.SendMessageServer;
import com.rainvice.sockettest_1.utils.DataUtil;
import com.rainvice.sockettest_1.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    @BindView(R.id.image)
    ImageView mImageView;

    private String mIp = "127.0.0.1";
    private String mUsername = "用户名";
    private DialogueRecordBean mDialogueRecordBean;
    private Map<String, DialogueRecordBean> mMessageMap;
    private RvAdapter<DialogBean> mRvAdapter;
    private ActivityResultLauncher<Intent> mLauncher;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        mLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                assert data != null;
                Uri uri = data.getData();
                ContentResolver resolver = getContentResolver();
                Bitmap mBitmap = null;
                try {
                    //1.将相册返回的uri转为Bitmap
                    mBitmap = MediaStore.Images.Media.getBitmap(resolver, uri);

                    //2.压缩图片,第二个入参表示图片压缩率，如果是100就表示不压缩
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);

                    //3.将压缩后的图片进行base64编码
                    byte[] bytes = bos.toByteArray();
                    String s = Base64.encodeToString(bytes, Base64.DEFAULT);

                    //4.将base64解码，转为bitmap并显示在imageview中看效果
//                    byte[] decodedString = Base64.decode(s, Base64.DEFAULT);
//                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    sendMessage(DataType.IMAGE,s,mBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

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
            sendMessage(DataType.WORD, msg,null);
        });

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = editable.length();
                if (length == 0) {
                    mImageView.setVisibility(View.VISIBLE);
                    mButton.setVisibility(View.GONE);
                } else {
                    mImageView.setVisibility(View.GONE);
                    mButton.setVisibility(View.VISIBLE);
                }
            }
        });

        mImageView.setOnClickListener(view -> checkPermission());

    }

    public void getImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mLauncher.launch(intent);
    }


    private static final int READ_EXTERNAL_STORAGE_CODE = 0;

    /**
     * 检查权限
     */
    private void checkPermission() {
        //1 先判断有没有权限
        // context 检查权限的字符串
        int isPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        //返回值只有两个 授权PERMISSION_GRANTED 拒绝PERMISSION_DENIED
        if (isPermission == PackageManager.PERMISSION_GRANTED) {
            //2 如果有这个直接拨打
            getImage();
        } else {
            //3 如果每一那么我们就需要去申请
            //申请的字符串数组 请求码
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_EXTERNAL_STORAGE_CODE) {
            //不为空 长度大于0 也就是存在这个
            if (grantResults.length > 0) {
                //PackageManager.PERMISSION_GRANTED 允许
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //授予了
                    //执行代码
                    getImage();
                } else {
                    //如果没有授权的话，可以给用户一个友好提示
                    Toast.makeText(this, "权限被拒绝，无法获取图片", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private void sendMessage(DataType dataType, String msg,Bitmap bitmap) {
        RvRequestProtocol<String> requestProtocol = null;
        if (Objects.nonNull(msg) && !"".equals(msg)) {
            if (dataType == DataType.WORD) {
                requestProtocol = new RvRequestProtocol<>(MsgType.MESSAGE, msg);
            } else {
                requestProtocol = new RvRequestProtocol<>(MsgType.IMAGE, msg);
            }
            SendMessageServer sendMessageServer = new SendMessageServer(mIp, requestProtocol);
            sendMessageServer.sendTCPMsg(new SendMessageServer.Callback() {
                /**
                 * 发送对方已收到
                 * @param result
                 */
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void success(RvResponseProtocol<String> result) {
                    mEditText.setText("");
                    mTextView.setText(mUsername);
                    //Toast.makeText(ChatActivity.this, "回复状态" + result.getStatus(), Toast.LENGTH_SHORT).show();
                    //获取消息记录
                    String format = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    DialogBean dialogBean = null;
                    if (dataType == DataType.WORD){
                        dialogBean = new DialogBean(true, format, dataType, msg, true);
                    }else {
                        dialogBean = new DialogBean(true,format,dataType,bitmap,true);
                    }
                    //判断是否有消息记录
                    if (mDialogueRecordBean == null) {
                        //添加相关消息记录
                        mDialogueRecordBean = new DialogueRecordBean(mUsername, mIp);
                        mDialogueRecordBean.getDialogs().add(dialogBean);
                        mMessageMap.put(mIp, mDialogueRecordBean);
                        initRecyclerView();
                    } else {
                        List<DialogBean> dialogs = mDialogueRecordBean.getDialogs();
                        int position = dialogs.size();
                        dialogs.add(dialogBean);
                        mRvAdapter.notifyItemRangeInserted(position, 1);
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
                    mTextView.setText(mUsername + "(对方可能已掉线)");
                    Toast.makeText(ChatActivity.this, "发送失败或者对方掉线", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Subscribe
    public void onEvent(BusToNearbyEvent event) {
        if (event.getStatus() == Status.SUCCESS) {
            String s = DataUtil.getNameMap().get(mIp);
            if (s == null) {
                mTextView.setText(mUsername + "(对方可能已掉线)");
            } else {
                mUsername = s;
                mTextView.setText(mUsername);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Subscribe
    public void onEvent(BusToChatEvent event) {
        int status = event.getStatus();
        LogUtil.d("接收到刷新指令", String.valueOf(status));
        switch (status) {
            case Status.SUCCESS:
                if (mDialogueRecordBean == null) {
                    mDialogueRecordBean = mMessageMap.get(mIp);
                    initRecyclerView();
                } else {
                    List<DialogBean> dialogs = mDialogueRecordBean.getDialogs();
                    int position = dialogs.size() - 1;
                    mRvAdapter.notifyItemRangeInserted(position, 1);
                    mRecyclerView.scrollToPosition(position);
                }
                break;

        }
    }

    ;

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initView() {
        mTextView.setText(mUsername);

        initRecyclerView();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initRecyclerView() {

        if (mDialogueRecordBean != null) {

            List<DialogBean> dialogs = mDialogueRecordBean.getDialogs();
            dialogs.forEach(dialog -> dialog.setRead(true));
            mRvAdapter = new RvAdapter<>(dialogs, R.layout.item_chat_list, (itemView, position, dialogBean) -> {
                    LinearLayout left = itemView.findViewById(R.id.left_message);
                    LinearLayout right = itemView.findViewById(R.id.right_message);
                    dialogBean.setRead(true);

                    if (dialogBean.isMine()) {
                        left.setVisibility(View.GONE);

                        TextView time = itemView.findViewById(R.id.right_time);
                        TextView content = itemView.findViewById(R.id.right_content);
                        ImageView imageView = itemView.findViewById(R.id.right_image);

                        if (dialogBean.getDataType() == DataType.WORD){
                            imageView.setVisibility(View.GONE);
                            time.setText(dialogBean.getTime());
                            String text = (String) dialogBean.getContent();
                            content.setText(text);
                            content.setOnLongClickListener(view -> {
                                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clipData = ClipData.newPlainText(text, text);
                                clipboardManager.setPrimaryClip(clipData);
                                Toast.makeText(ChatActivity.this, mIp, Toast.LENGTH_SHORT).show();
                                return true;
                            });
                        }else {
                            content.setVisibility(View.GONE);
                            time.setText(dialogBean.getTime());
                            Bitmap bit = (Bitmap) dialogBean.getContent();
                            imageView.setImageBitmap(bit);

                            imageView.setOnClickListener(view -> {
                                Intent intent = new Intent(this, PhotoViewActivity.class);
                                intent.putExtra("ip",mIp);
                                intent.putExtra("position",position);
                                startActivity(intent);
                            });

                            imageView.setOnLongClickListener(v -> {
                                saveBitmap(bit);
                                return true;
                            });

                        }


                    } else {
                        right.setVisibility(View.GONE);


                        TextView time = itemView.findViewById(R.id.left_time);
                        TextView content = itemView.findViewById(R.id.left_content);
                        ImageView imageView = itemView.findViewById(R.id.left_image);

                        if (dialogBean.getDataType() == DataType.WORD){
                            imageView.setVisibility(View.GONE);
                            time.setText(dialogBean.getTime());
                            String text = (String) dialogBean.getContent();
                            content.setText(text);
                            content.setOnLongClickListener(view -> {
                                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clipData = ClipData.newPlainText(mIp, text);
                                clipboardManager.setPrimaryClip(clipData);
                                Toast.makeText(ChatActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
                                return true;
                            });
                        }else {
                            content.setVisibility(View.GONE);
                            time.setText(dialogBean.getTime());
                            Bitmap bit = (Bitmap) dialogBean.getContent();
                            imageView.setImageBitmap(bit);

                            imageView.setOnClickListener(view -> {
                                Intent intent = new Intent(this, PhotoViewActivity.class);
                                intent.putExtra("ip",mIp);
                                intent.putExtra("position",position);
                                startActivity(intent);
                            });

                            imageView.setOnLongClickListener(v -> {
                                saveBitmap(bit);
                                return true;
                            });

                        }
                    }
            });
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(mRvAdapter);
            mRecyclerView.scrollToPosition(mRvAdapter.getItemCount() - 1);

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

    /**
     * 保存bitmap到本地
     *
     * @param bitmap Bitmap
     */
    public void saveBitmap(Bitmap bitmap) {
        new Thread(() -> {
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File image = new File(file, System.currentTimeMillis() + ".jpg");
            try {
                //文件输出流
                FileOutputStream fileOutputStream = new FileOutputStream(image);
                //压缩图片，如果要保存png，就用Bitmap.CompressFormat.PNG，要保存jpg就用Bitmap.CompressFormat.JPEG,质量是100%，表示不压缩
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                //写入，这里会卡顿，因为图片较大
                fileOutputStream.flush();
                //记得要关闭写入流
                fileOutputStream.close();
                //成功的提示
                runOnUiThread(()-> Toast.makeText(this, "图片保存到" + image.getPath(), Toast.LENGTH_SHORT).show());

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(()-> Toast.makeText(this, "图片保存失败", Toast.LENGTH_SHORT).show());
            }

        }).start();
    }


}