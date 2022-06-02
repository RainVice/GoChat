# GoChat 安卓局域网聊天

## 简介

功能：实现在统一局域网下手机与手机之间的相互通信，实现指定用户发送文字以及图片消息。

## 功能

1. 每次用户打开app，可以将自己已上线的消息广播到局域网中
2. 同局域网之间用户可以互相搜索到，同时可以更新新上线的设备
3. 扫描到设备可以选择指定用户聊天，可以添加未扫描到的用户 IP 聊天
4. 用户可以自行更改用户名
5. 聊天界面可以发送文字消息，可以发送图片消息

## 实现

> 本人写代码的水平比较菜，不喜勿喷

### 一 、界面
#### 主界面实现

这是主界面，主要用于承载 Fragment

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MainActivity">
    <!-- ViewPager2，用户承载 Fragment 碎片，让主页面具有左右滑动效果 -->
    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/main_view_pager"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"/>
    <!-- BottomNavigationView 实现底部导航栏，也可自己实现 -->
    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottom_menu"
        app:menu="@menu/b_n_v_menu"
        app:labelVisibilityMode="selected"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>
</LinearLayout>
```

其中的 `b_n_v_menu` 文件为自定义的 `menu` 文件，用于显示底部导航栏按钮

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/message"
        android:title="聊天"
        android:icon="@drawable/ic_main_message_200"/>
    <item
        android:id="@+id/contact"
        android:title="群聊"
        android:icon="@drawable/ic_main_contact_200"/>
    <item
        android:id="@+id/nearby"
        android:title="附近"
        android:icon="@drawable/ic_main_nearby"/>
</menu>
```

这是子界面，负责显示：消息、群聊、附近设备页面。这里只放一个页面，需要详细信息请查看我的代码

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".fragment.MessageFragment">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:background="@color/salmon"
        android:gravity="center_vertical"
        android:paddingHorizontal="15dp">
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content">
            <ImageView
                android:layout_width="23dp"
                android:layout_height="23dp"
                android:src="@drawable/ic_main_message_200" />
            <TextView
                android:textStyle="bold"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_gravity="center_vertical"
                android:layout_marginStart="7dp"
                android:text="消息"
                android:textColor="@color/white" />
            <LinearLayout
                android:layout_weight="1"
                android:layout_width="0dp"
                android:layout_height="wrap_content"/>

            <ImageView
                android:id="@+id/add"
                android:layout_width="23dp"
                android:layout_height="23dp"
                android:src="@drawable/ic_messge_add_200" />
        </LinearLayout>
    </LinearLayout>
    <TextView
        android:id="@+id/ip"
        android:gravity="center"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="center_vertical"
        android:layout_margin="10dp"
        android:text="消息"
        android:textSize="10dp" />
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/message_recyclerview"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginHorizontal="10dp" />
</LinearLayout>
```

接下来是实现 ViewPager2 的适配器，让其可以加载 Fragment 碎片

```java
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
```

接下来在 `MainActivity.java`的 `onCreate`方法中加入如下代码给获取到的 `ViewPager2` 

```java
//1.将三个 fragment 添加到 ViewPager2 中
//1.1创建三个 Fragment
MessageFragment messageFragment = new MessageFragment();
ContactFragment contactFragment = new ContactFragment();
NearbyFragment nearbyFragment = new NearbyFragment();
//1.2将他们添加到集合中
ArrayList<Fragment> fragments = new ArrayList<>();
fragments.add(messageFragment);
fragments.add(contactFragment);
fragments.add(nearbyFragment);
//设置适配器
mViewPager2.setAdapter(new Vp2Adapter(getSupportFragmentManager(),getLifecycle(),fragments));
```

绑定 `ViewPager2` 和 `BottomNavigationView`，是其中一个翻页或点击就可以影响到另一个控件翻页或点击

```java
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
```

#### 聊天界面实现

主要还是`RecyclerView`就不贴代码了，附上显示聊天记录的`item`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:padding="10dp"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    <!-- 左边（对方消息） -->
    <LinearLayout
        android:padding="5dp"
        android:id="@+id/left_message"
        android:orientation="vertical"
        android:layout_width="match_parent"
        android:layout_marginRight="100dp"
        android:layout_height="wrap_content">
        <!-- 显示时间、用户名 -->
        <TextView
            android:id="@+id/left_time"
            android:layout_marginLeft="5dp"
            android:textSize="10sp"
            android:text="时间"
            android:textColor="@color/darkgray"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"/>
        <!-- 对话框 -->
        <androidx.cardview.widget.CardView
            android:layout_marginTop="5dp"
            app:cardCornerRadius="10dp"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content">
            <!-- 文字消息 -->
            <TextView
                android:id="@+id/left_content"
                android:layout_margin="10dp"
                android:textStyle="bold"
                android:text="内容"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"/>
            <!-- 图片消息 -->
            <ImageView
                android:maxWidth="200dp"
                android:adjustViewBounds="true"
                android:scaleType="fitXY"
                android:id="@+id/left_image"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"/>
        </androidx.cardview.widget.CardView>
    </LinearLayout>
    <LinearLayout
        android:id="@+id/right_message"
        android:orientation="vertical"
        android:layout_width="match_parent"
        android:layout_marginLeft="100dp"
        android:layout_height="wrap_content">
        <!-- 显示时间、用户名 -->
        <TextView
            android:id="@+id/right_time"
            android:gravity="right"
            android:textSize="10sp"
            android:layout_marginRight="5dp"
            android:text="时间"
            android:textColor="@color/darkgray"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"/>
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content">
            <LinearLayout
                android:layout_weight="1"
                android:layout_width="0dp"
                android:layout_height="wrap_content"/>
            <androidx.cardview.widget.CardView
                android:layout_marginTop="5dp"
                app:cardCornerRadius="10dp"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content">
            	<!-- 图片消息 -->
                <ImageView
                    android:maxWidth="200dp"
                    android:adjustViewBounds="true"
                    android:scaleType="fitXY"
                    android:id="@+id/right_image"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"/>
                <!-- 文字消息 -->
                <TextView
                    android:id="@+id/right_content"
                    android:layout_margin="10dp"
                    android:textStyle="bold"
                    android:text="内容"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"/>
            </androidx.cardview.widget.CardView>
        </LinearLayout>
    </LinearLayout>
</LinearLayout>
```

### 二 、功能实现

1. 打开 app 通知局域网，我已上线

   
