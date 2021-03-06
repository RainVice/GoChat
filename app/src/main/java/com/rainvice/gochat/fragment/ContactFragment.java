package com.rainvice.gochat.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import com.rainvice.gochat.Adapter.RvAdapter;
import com.rainvice.gochat.R;
import com.rainvice.gochat.activity.PhotoViewActivity;
import com.rainvice.gochat.bean.DialogBean;
import com.rainvice.gochat.bean.DialogueRecordBean;
import com.rainvice.gochat.bean.GroupChatBean;
import com.rainvice.gochat.constant.DataType;
import com.rainvice.gochat.constant.Status;
import com.rainvice.gochat.event.BusToGroupEvent;
import com.rainvice.gochat.protocol.MsgType;
import com.rainvice.gochat.protocol.RvRequestProtocol;
import com.rainvice.gochat.protocol.RvResponseProtocol;
import com.rainvice.gochat.server.SendMessageServer;
import com.rainvice.gochat.utils.DataUtil;
import com.rainvice.gochat.utils.LogUtil;
import com.rainvice.gochat.utils.compress.CompressHelper;
import com.rainvice.gochat.utils.compress.FileUtil;


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
                try {
//                    Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);

                    File file = FileUtil.getTempFile(getContext(), uri);
                    Bitmap bitmap = new CompressHelper.Builder(getContext())
                            .setQuality(1)
                            .build().compressToBitmap(file);

                    //2.????????????,????????????????????????????????????????????????100??????????????????
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 1, bos);

                    //3.???????????????????????????base64??????
                    byte[] bytes = bos.toByteArray();
                    String s = Base64.encodeToString(bytes, Base64.DEFAULT);
                    //4.???base64???????????????bitmap????????????imageview????????????
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
            //????????????
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
                //Toast.makeText(ChatActivity.this, "????????????" + result.getStatus(), Toast.LENGTH_SHORT).show();
                //??????????????????
                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                DialogBean dialogBean = null;
                if (dataType == DataType.WORD) {
                    dialogBean = new DialogBean(true, DataUtil.getIp() + "???" + groupChatBean.getUsername() + "???" + time, dataType, msg, true);
                } else {
                    dialogBean = new DialogBean(true, DataUtil.getIp() + "???" + groupChatBean.getUsername() + "???" + time, dataType, bitmap, true);
                }
                //???????????????????????????
                if (mDialogueRecordBean == null) {
                    //????????????????????????
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
                Toast.makeText(getContext(), "????????????", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onResume() {
        super.onResume();
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

                    if (dialogBean.getDataType() == DataType.WORD) {
                        imageView.setVisibility(View.GONE);
                        time.setText(dialogBean.getTime());
                        String text = (String) dialogBean.getContent();
                        content.setText(text);
                        content.setOnLongClickListener(view -> {
                            ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText(text, text);
                            clipboardManager.setPrimaryClip(clipData);
                            Toast.makeText(getContext(), "?????????", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getContext(), "????????????", Toast.LENGTH_SHORT).show();
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
     * ????????????
     */
    private void checkPermission() {
        //1 ????????????????????????
        // context ????????????????????????
        int isPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        //????????????????????? ??????PERMISSION_GRANTED ??????PERMISSION_DENIED
        if (isPermission == PackageManager.PERMISSION_GRANTED) {
            //2 ???????????????????????????
            getImage();
        } else {
            //3 ??????????????????????????????????????????
            //???????????????????????? ?????????
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_EXTERNAL_STORAGE_CODE) {
            //????????? ????????????0 ?????????????????????
            if (grantResults.length > 0) {
                //PackageManager.PERMISSION_GRANTED ??????
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //?????????
                    //????????????
                    getImage();
                } else {
                    //????????????????????????????????????????????????????????????
                    Toast.makeText(getContext(), "????????????????????????????????????", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    public void getImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mLauncher.launch(intent);
    }

    /**
     * ??????bitmap?????????
     *
     * @param bitmap Bitmap
     */
    public void saveBitmap(Bitmap bitmap) {
        new Thread(() -> {
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File image = new File(file, System.currentTimeMillis() + ".jpg");
            try {
                //???????????????
                FileOutputStream fileOutputStream = new FileOutputStream(image);
                //??????????????????????????????png?????????Bitmap.CompressFormat.PNG????????????jpg??????Bitmap.CompressFormat.JPEG,?????????100%??????????????????
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                //?????????????????????????????????????????????
                fileOutputStream.flush();
                //????????????????????????
                fileOutputStream.close();
                //???????????????
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "???????????????" + image.getPath(), Toast.LENGTH_SHORT).show());

            } catch (IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "??????????????????", Toast.LENGTH_SHORT).show());
            }

        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Subscribe
    public void onEvent(BusToGroupEvent event) {
        int status = event.getStatus();
        LogUtil.d("?????????????????????", String.valueOf(status));
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