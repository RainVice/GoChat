package com.rainvice.sockettest_1.fragment;

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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rainvice.sockettest_1.Adapter.RvAdapter;
import com.rainvice.sockettest_1.R;
import com.rainvice.sockettest_1.activity.ChatActivity;
import com.rainvice.sockettest_1.activity.PhotoViewActivity;
import com.rainvice.sockettest_1.bean.DialogBean;
import com.rainvice.sockettest_1.bean.DialogueRecordBean;
import com.rainvice.sockettest_1.bean.GroupChatBean;
import com.rainvice.sockettest_1.constant.DataType;
import com.rainvice.sockettest_1.constant.IntentConstant;
import com.rainvice.sockettest_1.constant.Status;
import com.rainvice.sockettest_1.event.BusToChatEvent;
import com.rainvice.sockettest_1.event.BusToGroupEvent;
import com.rainvice.sockettest_1.protocol.MsgType;
import com.rainvice.sockettest_1.protocol.RvRequestProtocol;
import com.rainvice.sockettest_1.protocol.RvResponseProtocol;
import com.rainvice.sockettest_1.server.SendMessageServer;
import com.rainvice.sockettest_1.utils.DataUtil;
import com.rainvice.sockettest_1.utils.LogUtil;
import com.rainvice.sockettest_1.utils.StrZipUtil;

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

public class ContactFragment extends Fragment {

    private EditText mEditText;
    private Button mButton;
    private ImageView mImageView;
    private RecyclerView mRecyclerView;
    private ActivityResultLauncher<Intent> mLauncher;
    private Map<String, DialogueRecordBean> mMessageMap;
    private DialogueRecordBean mDialogueRecordBean;
    private RvAdapter<DialogBean> mRvAdapter;

    public ContactFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        initView(view);
        initData();
        initListener();

        mLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                assert data != null;
                Uri uri = data.getData();
                ContentResolver resolver = getContext().getContentResolver();
                Bitmap bitmap = null;
                try {
                    //1.将相册返回的uri转为Bitmap
                    bitmap = MediaStore.Images.Media.getBitmap(resolver, uri);



                    //2.压缩图片,第二个入参表示图片压缩率，如果是100就表示不压缩
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 1, bos);

                    //3.将压缩后的图片进行base64编码
                    byte[] bytes = bos.toByteArray();
                    String s = Base64.encodeToString(bytes, Base64.DEFAULT);
                    //4.将base64解码，转为bitmap并显示在imageview中看效果
//                    byte[] decodedString = Base64.decode(s, Base64.DEFAULT);
//                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    sendMessage(DataType.IMAGE, s, bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private void initData() {
        mMessageMap = DataUtil.getMessageMap();
        mDialogueRecordBean = mMessageMap.get(MsgType.GROUP_MESSAGE);
    }

    private void initView(View view) {

        mEditText = view.findViewById(R.id.inputView);
        mButton = view.findViewById(R.id.send);
        mImageView = view.findViewById(R.id.image);
        mRecyclerView = view.findViewById(R.id.recyclerview);

    }

    private void initListener() {

        mButton.setOnClickListener(view -> {
            //发送消息
            String msg = mEditText.getText().toString();
            sendMessage(DataType.WORD, msg, null);
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

    private void sendMessage(DataType dataType, String msg, Bitmap bitmap) {
        GroupChatBean groupChatBean = new GroupChatBean(DataUtil.getUsername(), msg, dataType == DataType.WORD ? MsgType.MESSAGE : MsgType.IMAGE);
        Gson gson = new Gson();
        String json = gson.toJson(groupChatBean);
        SendMessageServer sendMessageServer = new SendMessageServer(new RvRequestProtocol<>(MsgType.GROUP_MESSAGE, json));
        sendMessageServer.sendUDPMsg(new SendMessageServer.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void success(RvResponseProtocol<String> result) {
                mEditText.setText("");
                //Toast.makeText(ChatActivity.this, "回复状态" + result.getStatus(), Toast.LENGTH_SHORT).show();
                //获取消息记录
                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                DialogBean dialogBean = null;
                if (dataType == DataType.WORD) {
                    dialogBean = new DialogBean(true, DataUtil.getIp() + "，" + groupChatBean.getUsername() + "，" + time, dataType, msg, true);
                } else {
                    dialogBean = new DialogBean(true, DataUtil.getIp() + "，" + groupChatBean.getUsername() + "，" + time, dataType, bitmap, true);
                }
                //判断是否有消息记录
                if (mDialogueRecordBean == null) {
                    //添加相关消息记录
                    mDialogueRecordBean = new DialogueRecordBean(MsgType.GROUP_MESSAGE, MsgType.GROUP_MESSAGE);
                    mDialogueRecordBean.getDialogs().add(dialogBean);
                    mMessageMap.put(MsgType.GROUP_MESSAGE, mDialogueRecordBean);
                    initRecyclerView();
                } else {
                    List<DialogBean> dialogs = mDialogueRecordBean.getDialogs();
                    int position = dialogs.size();
                    dialogs.add(dialogBean);
                    if (mRvAdapter == null){
                        initRecyclerView();
                    }else {
                        mRvAdapter.notifyItemRangeInserted(position, 1);
                        mRecyclerView.scrollToPosition(position);
                    }
                }
                mDialogueRecordBean.setTimes(System.currentTimeMillis());
            }

            @Override
            public void error(RvResponseProtocol<String> result) {
                Toast.makeText(getContext(), "发送失败", Toast.LENGTH_SHORT).show();
            }
        });
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

                    if (dialogBean.getDataType() == DataType.WORD) {
                        imageView.setVisibility(View.GONE);
                        time.setText(dialogBean.getTime());
                        String text = (String) dialogBean.getContent();
                        content.setText(text);
                        content.setOnLongClickListener(view -> {
                            ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText(text, text);
                            clipboardManager.setPrimaryClip(clipData);
                            Toast.makeText(getContext(), "已复制", Toast.LENGTH_SHORT).show();
                            return true;
                        });
                    } else {
                        content.setVisibility(View.GONE);
                        time.setText(dialogBean.getTime());
                        Bitmap bit = (Bitmap) dialogBean.getContent();
                        imageView.setImageBitmap(bit);

                        imageView.setOnClickListener(view -> {
                            Intent intent = new Intent(getContext(), PhotoViewActivity.class);
                            intent.putExtra("ip", MsgType.GROUP_MESSAGE);
                            intent.putExtra("position", position);
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

                    if (dialogBean.getDataType() == DataType.WORD) {
                        imageView.setVisibility(View.GONE);
                        time.setText(dialogBean.getTime());
                        String text = (String) dialogBean.getContent();
                        content.setText(text);
                        content.setOnLongClickListener(view -> {
                            ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText(MsgType.GROUP_MESSAGE, text);
                            clipboardManager.setPrimaryClip(clipData);
                            Toast.makeText(getContext(), "复制成功", Toast.LENGTH_SHORT).show();
                            return true;
                        });
                    } else {
                        content.setVisibility(View.GONE);
                        time.setText(dialogBean.getTime());
                        Bitmap bit = (Bitmap) dialogBean.getContent();
                        imageView.setImageBitmap(bit);

                        imageView.setOnClickListener(view -> {
                            Intent intent = new Intent(getContext(), PhotoViewActivity.class);
                            intent.putExtra("ip", MsgType.GROUP_MESSAGE);
                            intent.putExtra("position", position);
                            startActivity(intent);
                        });

                        imageView.setOnLongClickListener(v -> {
                            saveBitmap(bit);
                            return true;
                        });

                    }
                }
            });
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mRecyclerView.setAdapter(mRvAdapter);
            mRecyclerView.scrollToPosition(mRvAdapter.getItemCount() - 1);

        }

    }


    private static final int READ_EXTERNAL_STORAGE_CODE = 0;

    /**
     * 检查权限
     */
    private void checkPermission() {
        //1 先判断有没有权限
        // context 检查权限的字符串
        int isPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        //返回值只有两个 授权PERMISSION_GRANTED 拒绝PERMISSION_DENIED
        if (isPermission == PackageManager.PERMISSION_GRANTED) {
            //2 如果有这个直接拨打
            getImage();
        } else {
            //3 如果每一那么我们就需要去申请
            //申请的字符串数组 请求码
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_CODE);
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
                    Toast.makeText(getContext(), "权限被拒绝，无法获取图片", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    public void getImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mLauncher.launch(intent);
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
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "图片保存到" + image.getPath(), Toast.LENGTH_SHORT).show());

            } catch (IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "图片保存失败", Toast.LENGTH_SHORT).show());
            }

        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Subscribe
    public void onEvent(BusToGroupEvent event) {
        int status = event.getStatus();
        LogUtil.d("接收到刷新指令", String.valueOf(status));
        switch (status) {
            case Status.SUCCESS:
                if (mDialogueRecordBean == null) {
                    mDialogueRecordBean = mMessageMap.get(MsgType.GROUP_MESSAGE);
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}