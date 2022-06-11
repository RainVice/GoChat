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

主要还是`RecyclerView`就不贴代码了，附上显示聊天记录的 item：`item_chat_list.xml`

### 二 、功能实现

#### 协议

作为一个通信功能，首先得自定义一个协议，用来承载信息，方便对信息的处理

1. 定义消息类型

   ```java
   public class MsgType {
       //文字消息
       public static final String MESSAGE = "message";
       //文字消息
       public static final String RECEIPT = "receipt";
       //请求昵称
       public static final String GET_NAME = "get_name";
       //图片消息
       public static final String IMAGE = "image";
       //群消息
       public static final String GROUP_MESSAGE = "group_message";
   }
   ```

2. 首先是请求协议 `RvResponseProtocol.java`

   ```java
   /**
    * 自定义协议
    * 以 json 形式呈现
    */
   public class RvRequestProtocol<T> {
       //消息类型
       private String type;
       //消息内容
       private T data;
   }
   ```

3. 响应协议`RvResponseProtocol.java`继承了请求协议，多了响应成功抓状态码功能

   ```java
   /**
    * 自定义协议
    * 以 json 形式呈现
    */
   public class RvResponseProtocol<T> extends RvRequestProtocol<T>{
       public static final Integer OK = 200;
       public static final Integer FAIL = 500;
       //状态码
       private Integer status;
   }
   ```

#### 广播实现

1. 需要`udp`服务端用于接收广播：`UDPSocketServerThread.java`

   > 服务端接收到广播后，判断接收到的消息类型，并通过`Handle`通知`UI`线程更新相关内容。并保存发送者的用户信息
   >
   > ```java
   > try {
   >     byte[] bytes = dp.getData();
   >     bytes = StrZipUtil.uncompressA(bytes);
   >     bytes = StrZipUtil.uncompressA(bytes);
   >     String json = StrZipUtil.uncompress(bytes);
   >     LogUtil.d("接收到",json);
   >     RvRequestProtocol<String> rvRequestProtocol = gson.fromJson(json, RvRequestProtocol.class);
   >     String hostAddress = dp.getAddress().getHostAddress();
   >     String data = rvRequestProtocol.getData();
   >     String type = rvRequestProtocol.getType();
   >     if (type.equals(MsgType.MESSAGE)){
   >         DataUtil.getNameMap().put(hostAddress, data);
   >         Message message = new Message();
   >         message.what = Status.SUCCESS;
   >         mHander.sendMessage(message);
   >     }else if (type.equals(MsgType.GROUP_MESSAGE)){
   >         Message message = new Message();
   >         message.what = Status.GROUP_SUCCESS;
   >         message.obj = new InputMsgBean(hostAddress,rvRequestProtocol);
   >         mHander.sendMessage(message);
   >     }
   >     LogUtil.d(TAG,"接收到来自 " + hostAddress + " 的数据：" + data);
   > } catch (JsonSyntaxException | IOException e) {
   >     e.printStackTrace();
   > }
   > ```

2. 需要`udp`客户端用户发送广播：`UDPSocketClientThread.java`

   > 在客户端中，每隔 5 秒发送一次广播，通知局域网设备在线，发送的内容为当前设备名称
   >
   > ```java
   > do {
   >     RvRequestProtocol<String> requestProtocol = new RvRequestProtocol<>(MsgType.MESSAGE, DataUtil.getUsername());
   >     String json = gson.toJson(requestProtocol);
   >     byte[] bytes = StrZipUtil.compress(json);
   >     bytes = StrZipUtil.compress(bytes);
   >     byte[] compress = StrZipUtil.compress(bytes);
   >     DatagramPacket dp = new DatagramPacket(compress,compress.length, adds, DataUtil.getUDPPort());
   >     ds.send(dp);
   >     LogUtil.d(TAG,"发送消息");
   >     sleep(5 * 1000);
   > } while (true);
   > ```

3. 需要一个类用户发送`UDP`消息：`UDPSendMsgThread.java`，用户发送群聊消息

4. 创建一个`UDP`服务：`UDPSocketService.java`,在启动时调用`UDPSocketServerThread`打开`UDP`服务端和服务端，同时创建一个线程，检查当前所有设备是否在线，每分钟检测一次。

   ```java
   new Thread(() -> {
       while (true){
           try {
               Thread.sleep(60 * 1000);
               LogUtil.d(TAG, "开始检测设备");
               Map<String, String> nameMap = DataUtil.getNameMap();
               ArrayList<String> keys = new ArrayList<>();
               nameMap.forEach((key,val) -> {
                   try {
                       Thread.sleep(200);
                       Socket socket = new Socket();
                       InetSocketAddress inetSocketAddress = new InetSocketAddress(key, DataUtil.getTCPPort());
                       try {
                           socket.connect(inetSocketAddress,200);
                       } catch (IOException e) {
                           LogUtil.d(TAG,"设备掉线：" + key);
                           keys.add(key);
                       } finally {
                           try {
                               socket.close();
                           } catch (IOException e) {
                               e.printStackTrace();
                           }
                       }
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
               });
               keys.forEach(nameMap::remove);
               Message message = new Message();
               message.what = Status.SUCCESS;
               mHandler.sendMessage(message);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }
   }).start();
   ```

5. 使用 `EventBus`通知`UI`线程更新显示

   ```java
   EventBus.getDefault().post(new BusToGroupEvent(Status.SUCCESS));
   ```

6. 更新`UI`代码见源码

7. 群聊（消息显示见私聊）

   - 发送文字消息，直接将文字消息封装成协议对象，发送。
   - 发送图片消息，因为`UDP`的限制，图片不能太大，我先将图片压缩，再转为 `base64`，将最后的协议内容压缩为二进制数据发送。接收端解析，并显示图片。

#### TCP 扫描与私聊

##### 服务端 

创建服务`SocketServerService.java`，启动服务端：`TCPSocketServerThread.java`。用于接收消息，判断消息类型，通知`UI`主线程。详情看代码

##### 扫描附近用户功能实现

`UDP TCP`结合，`UDP`接收到上线广播在扫描界面展示，`TCP`主动在局域网内扫描设备，核心代码：

```java
public void run() {
    //创建长度为 50 的线程池
    ExecutorService executorService = Executors.newFixedThreadPool(254);
    final CountDownLatch latch = new CountDownLatch(254);
    LogUtil.d(TAG, "scanIp: 开始扫描 " + mIps + " 网段");
    //扫描局域网内的设备
    for (int i = 1; i <= 255; i++) {
        String ip = mIps + i;
        if (ip.equals(mHostIp)) {
            continue;
        }
        executorService.execute(() -> {
            Socket socket = new Socket();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, DataUtil.getTCPPort());
            //消息对象，给主线程发送消息
            Message message = new Message();
            message.obj = ip;
            try {
                socket.connect(inetSocketAddress,200);
                message.what = Status.SUCCESS;
            } catch (IOException e) {
                message.what = Status.ERROR;
            } finally {
                mHandler.sendMessage(message);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            latch.countDown();
        });
    }
    try {
        latch.await();
        executorService.shutdown();
        //完成
        LogUtil.d(TAG, "完成");
        Message message = new Message();
        message.what = Status.FINISH;
        mHandler.sendMessage(message);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
```

两种方式的区别，若所处的网络环境稳定几乎不会出以下问题

`UDP`：能接收到在不同网段的消息，但缺点是不够稳定

`TCP`：稳定，但是想全网段扫描的话极其耗费资源，并且快速扫描会被路由器拦截

我将二者结合，使其在校园网内运行更稳定

##### 私聊

1. 发送文字消息时，调用`TCPSendMsgThread.java`内部的方法，封装成协议内容，发送消息。

2. 发送图片消息时，因为`TCP`对大数据的支持良好，所以可以直接将图片转为`base64`直接发送，直接解析

3. 接收到消息显示（群聊同理）：

   - 首先判断是否对方消息，来显示左右，
   - 再判断是否图片消息，来显示图片文字

   ```java
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
   ```

## 总结

很Ok
