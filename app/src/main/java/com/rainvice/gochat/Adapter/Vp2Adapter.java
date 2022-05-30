package com.rainvice.gochat.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.List;


/**
 * ViewPager2 中添加 Fragment 的适配器
 */
public class Vp2Adapter extends FragmentStateAdapter {

    private List<Fragment> mFragments;

    /**
     * 构造方法
     * @param fragmentManager 碎片管理器
     * @param lifecycle 生命周期
     * @param fragments fragment列表
     */
    public Vp2Adapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, List<Fragment> fragments) {
        super(fragmentManager, lifecycle);
        this.mFragments = fragments;
    }



    /**
     *
     * @param position 当前创建 fragment 索引值
     * @return 返回当前创建的 fragment
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mFragments.get(position);
    }

    /**
     * 获取 fragment 的长度
     * @return fragment 的长度
     */
    @Override
    public int getItemCount() {
        return mFragments.size();
    }


}
