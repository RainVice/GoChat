package com.rainvice.sockettest_1.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.rainvice.sockettest_1.R;
import com.rainvice.sockettest_1.utils.DataUtil;
import com.rainvice.sockettest_1.view.PhotoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoViewActivity extends AppCompatActivity {

    @BindView(R.id.photo)
    PhotoView mPhotoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        ButterKnife.bind(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = getIntent();
        String ip = intent.getStringExtra("ip");
        int position = intent.getIntExtra("position",0);

        mPhotoView.enable();
        mPhotoView.enableRotate();
        Bitmap bitmap = (Bitmap) DataUtil.getMessageMap().get(ip).getDialogs().get(position).getContent();
        mPhotoView.setImageBitmap(bitmap);

        mPhotoView.setOnClickListener(view -> finish());

        mPhotoView.setOnLongClickListener(v -> {
            saveBitmap(bitmap);
            return true;
        });
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