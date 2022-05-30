package com.rainvice.gochat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.rainvice.gochat.Adapter.Vp2Adapter;
import com.rainvice.gochat.fragment.ContactFragment;
import com.rainvice.gochat.fragment.MessageFragment;
import com.rainvice.gochat.fragment.NearbyFragment;
import com.rainvice.gochat.service.UDPSocketService;
import com.rainvice.gochat.utils.DataUtil;
import com.rainvice.gochat.utils.IpUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressLint("NonConstantResourceId")
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.bottom_menu)
    BottomNavigationView mBottomNavigationView;

    @BindView(R.id.main_view_pager)
    ViewPager2 mViewPager2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //启动 UDP 服务
        Intent intent = new Intent(this, UDPSocketService.class);

        //启动服务
        startService(intent);

        //设置设备名称
        String bluetoothName = Settings.Secure.getString(getContentResolver(), "bluetooth_name");
        DataUtil.setUsername(bluetoothName);
        String hostIp = IpUtil.getHostIp();
        DataUtil.setIp(hostIp);


        //将三个 fragment 添加到 ViewPager2 中
        MessageFragment messageFragment = new MessageFragment();
        ContactFragment contactFragment = new ContactFragment();
        NearbyFragment nearbyFragment = new NearbyFragment();

        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(messageFragment);
        fragments.add(contactFragment);
        fragments.add(nearbyFragment);
        //设置适配器
        mViewPager2.setAdapter(new Vp2Adapter(getSupportFragmentManager(),getLifecycle(),fragments));

        //设置翻页时底部导航栏动作
        mViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                final int[] ids = {R.id.message,R.id.contact,R.id.nearby};
                mBottomNavigationView.setSelectedItemId(ids[position]);
            }
        });

        //设置点击底部导航栏翻页
        mBottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            switch (itemId){
                case R.id.message:
                    mViewPager2.setCurrentItem(0);
                    break;
                case R.id.contact:
                    mViewPager2.setCurrentItem(1);
                    break;
                case R.id.nearby:
                    mViewPager2.setCurrentItem(2);
                    break;
            }
            return true;
        });
    }
}