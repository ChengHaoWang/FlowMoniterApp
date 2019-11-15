package com.example.test;

import android.Manifest;
import android.app.Service;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author:Jack Tony
 * @tips  :实时获取当前网速的service
 * @date  :2019-9-22
 */
public class SpeedService extends Service {

    //client 可以通过Binder获取Service实例
    public class MyBinder extends Binder {
        public SpeedService getService() {
            return SpeedService.this;
        }
    }

    private MyBinder myBinder=new MyBinder();
    private long total_data = TrafficStats.getTotalRxBytes();
    private Handler mHandler=new Handler();//初始化很重要哦

    //几秒刷新一次
    private final int count = 10;
    //private Intent intent=new Intent(this, DashboardFragment.class);

    private static List<AppItem> speedItemArrayList=new ArrayList<AppItem>();
    private List<AppItem> tempList=new ArrayList<AppItem>();
    //private ExecutorService fixedThreadPool = Executors.newScheduledThreadPool(20);
    private ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(30, Integer.MAX_VALUE,1,TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
    //private int index=0;
    private static ThreadLocal<AppItem> threadLocalst=new ThreadLocal<AppItem>();
    /**
     * 定义线程周期性地获取网速
     */
    private Runnable mRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void run() {
            Log.e("Service","服务计时器");
            //更新网速
            //index=0;
            for(int i=0;i<speedItemArrayList.size();i++){
                final int index=i;

                Runnable threadsRunnable=new Runnable() {
                    @Override
                    public void run() {
                        //数据隔离
                        AppItem tappItem=speedItemArrayList.get(index);
                        threadLocalst.set(tappItem);
                        AppItem appItem=threadLocalst.get();

                        int appId = appItem.getAppId();
                        long totalFlow = appItem.getTotalFlowLong();
                        //long firstInstallTime = speedItemArrayList.get(index).getFirstInstallTime();
                        long startTime=appItem.getStartTime();

                        //计算现在的流量
                        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                            Log.e("Steven", "没有权限");
                        }
                        String subId = tm.getSubscriberId();//网络接口ID
                        NetworkStatsManager networkStatsManager = (NetworkStatsManager) getSystemService(NETWORK_STATS_SERVICE);
                        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String dateStr = dateformat.format(System.currentTimeMillis());
                        //获取方法二，这个APP好麻烦==
                        NetworkStats mobileFlowSummary = null;
                        try {
                            mobileFlowSummary = networkStatsManager.querySummary(ConnectivityManager.TYPE_MOBILE, subId, startTime, System.currentTimeMillis());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        NetworkStats.Bucket mobileFlowBucket = new NetworkStats.Bucket();
                        long mobileTrafficeByte = 0;//发送字节总数
                        long mobileReceiveByte = 0;//接收字节总数
                        long mobileTotalByte = 0;//总字节数
                        do {
                            mobileFlowSummary.getNextBucket(mobileFlowBucket);
                            if (appId == mobileFlowBucket.getUid()) {
                                mobileTrafficeByte = mobileTrafficeByte + mobileFlowBucket.getTxBytes();//接收的字节数
                                mobileReceiveByte = mobileReceiveByte + mobileFlowBucket.getRxBytes();//传输的字节数
                                mobileTotalByte = mobileTotalByte + mobileTrafficeByte + mobileReceiveByte;
                            }
                        } while (mobileFlowSummary.hasNextBucket());

                        NetworkStats wifiFlowSummary = null;
                        try {
                            wifiFlowSummary = networkStatsManager.querySummary(ConnectivityManager.TYPE_WIFI, subId, startTime, System.currentTimeMillis());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        NetworkStats.Bucket wifiFlowBucket = new NetworkStats.Bucket();
                        long wifiTrafficeByte = 0;//发送字节总数
                        long wifiReceiveByte = 0;//接收字节总数
                        long wifiTotalByte = 0;//总字节数
                        do {
                            wifiFlowSummary.getNextBucket(wifiFlowBucket);
                            if (appId == wifiFlowBucket.getUid()) {
                                wifiTrafficeByte = wifiTrafficeByte + wifiFlowBucket.getTxBytes();//接收的字节数
                                wifiReceiveByte = wifiReceiveByte + wifiFlowBucket.getRxBytes();//传输的字节数
                                wifiTotalByte = wifiTotalByte + wifiTrafficeByte + wifiReceiveByte;
                            }
                        } while (wifiFlowSummary.hasNextBucket());

                        long totalFlowNow = mobileTotalByte + wifiTotalByte;
                        appItem.setTotalFlowLong(totalFlowNow);
                        //网速
                        //时间差转int
                        long endTime=appItem.getEndTime();
                        int timeDifference=new Long(System.currentTimeMillis()-endTime).intValue()/1000;
                        long intervalFlow = totalFlowNow - totalFlow;
                        if (intervalFlow>0&&timeDifference>0){
                            int unconvertedSpeed=new Long(intervalFlow).intValue()/timeDifference;
                            appItem.setUnconvertedSpeed(unconvertedSpeed);
                            Log.e( "名称：" ,appItem.getAppName() + ",详情：" + String.valueOf(totalFlowNow) + "." + String.valueOf(totalFlow) + "差：" + String.valueOf(intervalFlow));

                            if (intervalFlow > 0 && intervalFlow < (1024 * timeDifference)) {
                                double speed = intervalFlow / timeDifference;
                                appItem.setAppSpeed(speed);
                                appItem.setSpeedUnit("B/s");
                                //Log.e("序号:", String.valueOf(index) + ",名称：" + speedItemArrayList.get(index).getAppName() + ",速度：" + String.valueOf(speed)+",时间差：" + String.valueOf(timeDifference));
                            } else if (intervalFlow >= (1024 * timeDifference) && intervalFlow < (1024 * 1024 * timeDifference)) {
                                double speed = intervalFlow / 1024 / timeDifference;
                                appItem.setAppSpeed(speed);
                                appItem.setSpeedUnit("K/s");
                                //Log.e("序号:", String.valueOf(index) + ",名称：" + speedItemArrayList.get(index).getAppName() + ",速度：" + String.valueOf(speed)+",时间差：" + String.valueOf(timeDifference));
                            } else if (intervalFlow >= (1024 * 1024 * timeDifference) && intervalFlow < (1024 * 1024 * 1024 * timeDifference)) {
                                double speed = intervalFlow / 1024 / 1024 / timeDifference;
                                appItem.setAppSpeed(speed);
                                appItem.setSpeedUnit("M/s");
                            } else {
                                double speed = intervalFlow / 1024 / 1024 / 1024 / timeDifference;
                                appItem.setAppSpeed(speed);
                                appItem.setSpeedUnit("G/s");
                            }
                        }

                        if (intervalFlow < 0 || intervalFlow == 0) {
                            appItem.setUnconvertedSpeed(0);
                            appItem.setAppSpeed(0.0);
                            appItem.setSpeedUnit("B/s");
                            //Log.e("序号:", String.valueOf(index) + ",名称：" + speedItemArrayList.get(index).getAppName() +",时间差：" + String.valueOf(timeDifference));
                        }
                        appItem.setEndTime(System.currentTimeMillis());
                        //SystemClock.sleep(1000);

/*
                        int appId = speedItemArrayList.get(index).getAppId();
                        long totalFlow = speedItemArrayList.get(index).getTotalFlowLong();
                        //long firstInstallTime = speedItemArrayList.get(index).getFirstInstallTime();
                        long startTime=speedItemArrayList.get(index).getStartTime();

                        //计算现在的流量
                        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                            Log.e("Steven", "没有权限");
                        }
                        String subId = tm.getSubscriberId();//网络接口ID
                        NetworkStatsManager networkStatsManager = (NetworkStatsManager) getSystemService(NETWORK_STATS_SERVICE);
                        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String dateStr = dateformat.format(System.currentTimeMillis());
                        //获取方法二，这个APP好麻烦==
                        NetworkStats mobileFlowSummary = null;
                        try {
                            mobileFlowSummary = networkStatsManager.querySummary(ConnectivityManager.TYPE_MOBILE, subId, startTime, System.currentTimeMillis());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        NetworkStats.Bucket mobileFlowBucket = new NetworkStats.Bucket();
                        long mobileTrafficeByte = 0;//发送字节总数
                        long mobileReceiveByte = 0;//接收字节总数
                        long mobileTotalByte = 0;//总字节数
                        do {
                            mobileFlowSummary.getNextBucket(mobileFlowBucket);
                            if (appId == mobileFlowBucket.getUid()) {
                                mobileTrafficeByte = mobileTrafficeByte + mobileFlowBucket.getTxBytes();//接收的字节数
                                mobileReceiveByte = mobileReceiveByte + mobileFlowBucket.getRxBytes();//传输的字节数
                                mobileTotalByte = mobileTotalByte + mobileTrafficeByte + mobileReceiveByte;
                            }
                        } while (mobileFlowSummary.hasNextBucket());

                        NetworkStats wifiFlowSummary = null;
                        try {
                            wifiFlowSummary = networkStatsManager.querySummary(ConnectivityManager.TYPE_WIFI, subId, startTime, System.currentTimeMillis());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        NetworkStats.Bucket wifiFlowBucket = new NetworkStats.Bucket();
                        long wifiTrafficeByte = 0;//发送字节总数
                        long wifiReceiveByte = 0;//接收字节总数
                        long wifiTotalByte = 0;//总字节数
                        do {
                            wifiFlowSummary.getNextBucket(wifiFlowBucket);
                            if (appId == wifiFlowBucket.getUid()) {
                                wifiTrafficeByte = wifiTrafficeByte + wifiFlowBucket.getTxBytes();//接收的字节数
                                wifiReceiveByte = wifiReceiveByte + wifiFlowBucket.getRxBytes();//传输的字节数
                                wifiTotalByte = wifiTotalByte + wifiTrafficeByte + wifiReceiveByte;
                            }
                        } while (wifiFlowSummary.hasNextBucket());

                        long totalFlowNow = mobileTotalByte + wifiTotalByte;
                        speedItemArrayList.get(index).setTotalFlowLong(totalFlowNow);
                        //网速
                        //时间差转int
                        long endTime=speedItemArrayList.get(index).getEndTime();
                        int timeDifference=new Long(System.currentTimeMillis()-endTime).intValue()/1000;
                        long intervalFlow = totalFlowNow - totalFlow;
                        if (intervalFlow>0&&timeDifference>0){
                            int unconvertedSpeed=new Long(intervalFlow).intValue()/timeDifference;
                            speedItemArrayList.get(index).setUnconvertedSpeed(unconvertedSpeed);
                            Log.e("序号:", String.valueOf(index) + ",名称：" + speedItemArrayList.get(index).getAppName() + ",详情：" + String.valueOf(totalFlowNow) + "." + String.valueOf(totalFlow) + "差：" + String.valueOf(intervalFlow));

                            if (intervalFlow > 0 && intervalFlow < (1024 * timeDifference)) {
                                double speed = intervalFlow / timeDifference;
                                speedItemArrayList.get(index).setAppSpeed(speed);
                                speedItemArrayList.get(index).setSpeedUnit("B/s");
                                //Log.e("序号:", String.valueOf(index) + ",名称：" + speedItemArrayList.get(index).getAppName() + ",速度：" + String.valueOf(speed)+",时间差：" + String.valueOf(timeDifference));
                            } else if (intervalFlow >= (1024 * timeDifference) && intervalFlow < (1024 * 1024 * timeDifference)) {
                                double speed = intervalFlow / 1024 / timeDifference;
                                speedItemArrayList.get(index).setAppSpeed(speed);
                                speedItemArrayList.get(index).setSpeedUnit("K/s");
                                //Log.e("序号:", String.valueOf(index) + ",名称：" + speedItemArrayList.get(index).getAppName() + ",速度：" + String.valueOf(speed)+",时间差：" + String.valueOf(timeDifference));
                            } else if (intervalFlow >= (1024 * 1024 * timeDifference) && intervalFlow < (1024 * 1024 * 1024 * timeDifference)) {
                                double speed = intervalFlow / 1024 / 1024 / timeDifference;
                                speedItemArrayList.get(index).setAppSpeed(speed);
                                speedItemArrayList.get(index).setSpeedUnit("M/s");
                            } else {
                                double speed = intervalFlow / 1024 / 1024 / 1024 / timeDifference;
                                speedItemArrayList.get(index).setAppSpeed(speed);
                                speedItemArrayList.get(index).setSpeedUnit("G/s");
                            }
                        }

                        if (intervalFlow < 0 || intervalFlow == 0) {
                            speedItemArrayList.get(index).setAppSpeed(0.0);
                            speedItemArrayList.get(index).setSpeedUnit("B/s");
                            //Log.e("序号:", String.valueOf(index) + ",名称：" + speedItemArrayList.get(index).getAppName() +",时间差：" + String.valueOf(timeDifference));
                        }
                        speedItemArrayList.get(index).setEndTime(System.currentTimeMillis());
                        //SystemClock.sleep(1000);
                        */
                    }

                };
                poolExecutor.execute(threadsRunnable);
            }
            //定时器
            mHandler.postDelayed(mRunnable, count * 1000);
            //Message msg = mHandler.obtainMessage();
            //msg.what = 1;
            //msg.arg1 = getNetSpeed();
            //mHandler.sendMessage(msg);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Steven", "进入Service");

        EventBus.getDefault().register(this);//用了粘性，这个位置无所谓

        mHandler.postDelayed(mRunnable, 0);

        }
        /*
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    //float real_data = (float)msg.arg1;
                    if(msg.arg1  > 1024 ){
                        System.out.println(msg.arg1 / 1024 + "kb/s");
                    }
                    else{
                        System.out.println(msg.arg1 + "b/s");
                    }
                }
            }
        };
        */


    /**
     * 启动服务时就开始启动线程获取网速
     */
    /*
    @Override
    public void onStart(Intent intent, int startId) {
        //获取初始速度列表
        mHandler.postDelayed(mRunnable, 0);
    };
     */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    /**
     * 在服务结束时删除消息队列
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
        EventBus.getDefault().unregister(this);
    }
    //暴露出去供client调用的公共方法
    public List<AppItem> getSpeedList() {
        return this.speedItemArrayList;
    }
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onEvent(List<AppItem> appList) {
        Log.e("Service消息","消息已接受:"+appList.size());
        if(appList.size()!=0){
            //speedItemArrayList=appList;
            //if (speedItemArrayList.size()==0) {
                speedItemArrayList.clear();
                speedItemArrayList.addAll(appList);
                Log.e("Service消息","数组已更新:"+appList.size());
            //}
        }
        else
            Toast.makeText(this,"无法接收消息",Toast.LENGTH_SHORT).show();
        //EventBus.getDefault().removeStickyEvent(appList.getClass());
    }

}