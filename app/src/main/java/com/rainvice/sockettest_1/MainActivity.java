package com.rainvice.sockettest_1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.rainvice.sockettest_1.Adapter.Vp2Adapter;
import com.rainvice.sockettest_1.fragment.ContactFragment;
import com.rainvice.sockettest_1.fragment.MessageFragment;
import com.rainvice.sockettest_1.fragment.NearbyFragment;
import com.rainvice.sockettest_1.utils.DataUtil;
import com.rainvice.sockettest_1.utils.IpScanUtil;

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

        //设置设备名称
        String bluetoothName = Settings.Secure.getString(getContentResolver(), "bluetooth_name");
        DataUtil.setUsername(bluetoothName);
        String hostIp = IpScanUtil.getHostIp();
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