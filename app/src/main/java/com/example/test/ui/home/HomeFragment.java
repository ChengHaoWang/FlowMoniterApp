package com.example.test.ui.home;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.example.test.AppItem;
import com.example.test.Appinfo;
import com.example.test.BottomNavigation;
import com.example.test.MainActivity;
import com.example.test.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.test.ApkTool.scanLocalInstallAppInfoList;
import static com.example.test.BottomNavigation.homePopWindow;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private List<AppItem> appList=new ArrayList<AppItem>();
    //private CustomPopWindow mPopwindow;
    private AppAdapter appAdapter;
    private long startTime;
    private List<AppItem> dayAppList=new ArrayList<AppItem>();
    private List<AppItem> weekAppList=new ArrayList<AppItem>();
    private List<AppItem> monthAppList=new ArrayList<AppItem>();
    private int timeFlag=0;//0=month;1=week;2=day
    final long firstdayOfmonth =getTimesMonthMorning();
    final long firstdayOfweek=getTimesWeekmorning();
    final long fisttimeOfday=getTimesmorning();
    private ImageView menu;
    private WaveSwipeRefreshLayout refreshLayout;
    private ListView applistview;
    private ProgressDialog progressDialog;
    private SharedPreferences sp;
    private String username;
    //protected WeakReference<View> mRootView;//缓存view
    //为弹出窗口实现监听类
    /*
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            mPopwindow.dismiss();
            switch (v.getId()) {

            }
        }

    };
     */
    private View.OnClickListener menuOnclick=new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            //mPopwindow.setAnimationStyle(R.style.popwin_anim_style);//动画执行必须在AtLocation之前
        }
    };

    private void showDialog(){
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("信息加载中，请等待...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        //窗口不可点击
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
    //获取本月第一天0点的时间
    public static long getTimesMonthMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return cal.getTimeInMillis();
    }
    //获得本周周日0点时间，周日是一周的开始
    public static long getTimesWeekmorning(){
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0,0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return cal.getTimeInMillis();
    }
    //获得当天0点时间
    public static long getTimesmorning(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    //当天0点的日期格式
    public Date getTimesmorning2() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    /**
     * 通过网络接口取mac
     * @return
     */
    private static String getNewMac() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0"))
                    continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return null;
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        /*
        final TextView textView = root.findViewById(R.id.text_home);

        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
*/
        //Date a=getTimesmorning2();
        //设置走马灯被选中
        final TextView rolltext=root.findViewById(R.id.rolltext);
        rolltext.setSelected(true);

        sp = getActivity().getSharedPreferences("userInfo", 0);
        username=sp.getString("username", "");

        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestBody requestBody = new FormBody.Builder()
                        .add("username",username)
                        .build();

                String url = getResources().getString(R.string.ip)+getResources().getString(R.string.queryinfo);
                OkHttpClient okHttpClient = new OkHttpClient();
                final Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();
                Call call = okHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("网络请求","请求失败");
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String respose_text = response.body().string();
                        //JSONObject jsonObject1 = null;
                        JSONObject jsonObject = null;
                        Log.e("网络请求",respose_text);
                        try {
                            //jsonObject1 = new JSONObject(respose_text);
                            //String result1 = jsonObject1.optString("result", null);
                            jsonObject=new JSONObject(respose_text);
                            final String iannouncement=jsonObject.optString("announcement",null);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                if (!iannouncement.equals("")){
                                    rolltext.setText(iannouncement);
                                }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        }).start();
        //获取流量信息
        new Thread(new Runnable() {
            @Override
            public void run() {
                appList.clear();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        showDialog();
                    }
                });
                appList=scanLocalInstallAppInfoList(getContext().getPackageManager(),getContext(),startTime);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                    //更新UI，回归主线程
                    appAdapter=new AppAdapter(getContext(),R.layout.app_item,appList);
                    applistview=root.findViewById(R.id.applistview);
                    applistview.setAdapter(appAdapter);
                    //进度框消失
                    progressDialog.dismiss();
                    //窗口恢复点击
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    //ListView的Item点击事件
                        applistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Intent intent =  new Intent();
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                if (Build.VERSION.SDK_INT >= 9) {
                                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                    intent.setData(Uri.fromParts("package", appList.get(i).getAppPackageName(), null));
                                } else if (Build.VERSION.SDK_INT <= 8) {
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                                    intent.putExtra("com.android.settings.ApplicationPkgName", appList.get(i).getAppPackageName());
                                }
                                startActivity(intent);
                            }
                        });
                    //广播数据
                    final List<AppItem> tappList=new ArrayList<AppItem>();
                    tappList.removeAll(tappList);
                    tappList.addAll(appList);
                    EventBus.getDefault().postSticky(tappList);
                    //EventBus.getDefault().postSticky(timeFlag);
                    //Log.e("传递消息","消息已发送");
                    }
                });
                transToServer();
            }
        }).start();
        startTime=firstdayOfmonth;
        menu=getActivity().findViewById(R.id.menu);

        //右上角菜单
        /*
        final TextView title=getActivity().findViewById(R.id.title);
        if (title.getText().equals("流量")){
            menu.setOnClickListener(menuOnclick);
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
       */

        homePopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDismiss() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                homePopWindow.backgroundAlpha(getActivity(), 1f);
                            }
                        });
                        final CheckBox positiveOrderItem = homePopWindow.view.findViewById(R.id.positiveOrder);
                        final CheckBox reverseOrderItem = homePopWindow.view.findViewById(R.id.reverseOrder);
                        final CheckBox dayOrderItem = homePopWindow.view.findViewById(R.id.dayOrder);
                        final CheckBox weekOrderItem = homePopWindow.view.findViewById(R.id.weekOrder);
                        final CheckBox monthOrderItem=homePopWindow.view.findViewById(R.id.monothOrder);
                        //临时列表
                        //tempAppList.addAll(appList);
                        //按天升序、降序排序
                        List<AppItem> tempAppList=new ArrayList<AppItem>();
                        if (positiveOrderItem.isChecked()&&dayOrderItem.isChecked()){
                            if (dayAppList.size()==0){
                                if (timeFlag==2&&appList.size()!=0){
                                    tempAppList.addAll(appList);
                                }
                                else {
                                    startTime=fisttimeOfday;
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showDialog();
                                        }
                                    });
                                    tempAppList=scanLocalInstallAppInfoList(getContext().getPackageManager(),getContext(),startTime);
                                    //进度框消失
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            //窗口恢复点击
                                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        }
                                    });
                                    transToServer();
                                }
                                Collections.sort(tempAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o1.getTotalFlowLong()).compareTo(Long.valueOf(o2.getTotalFlowLong()));
                                    }
                                });

                                dayAppList.addAll(tempAppList);
                            }
                            else {
                                Collections.sort(dayAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o1.getTotalFlowLong()).compareTo(Long.valueOf(o2.getTotalFlowLong()));
                                    }
                                });

                                dayAppList.addAll(tempAppList);
                            }
                            appList.clear();
                            appList.addAll(dayAppList);
                            timeFlag=2;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    appAdapter.notifyDataSetChanged();
                                }
                            });

                        }
                        else if(reverseOrderItem.isChecked()&&dayOrderItem.isChecked()){
                            if (dayAppList.size()==0){
                                if (timeFlag==2&&appList.size()!=0){
                                    tempAppList.addAll(appList);
                                }
                                else {
                                    startTime=fisttimeOfday;
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showDialog();
                                        }
                                    });
                                    tempAppList=scanLocalInstallAppInfoList(getContext().getPackageManager(),getContext(),startTime);
                                    //进度框消失
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            //窗口恢复点击
                                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        }
                                    });
                                    transToServer();
                                }
                                Collections.sort(tempAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o2.getTotalFlowLong()).compareTo(Long.valueOf(o1.getTotalFlowLong()));
                                    }
                                });
                                //List<AppItem> tempAppList=new ArrayList<AppItem>();
                                dayAppList.addAll(tempAppList);
                            }
                            else {
                                Collections.sort(dayAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o2.getTotalFlowLong()).compareTo(Long.valueOf(o1.getTotalFlowLong()));
                                    }
                                });
                            }
                            appList.clear();
                            appList.addAll(dayAppList);
                            timeFlag=2;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    appAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                        //按周升序、降序排序
                        else if (positiveOrderItem.isChecked()&&weekOrderItem.isChecked()){
                            if (weekAppList.size()==0){
                                if (timeFlag==1&&appList.size()!=0){
                                    tempAppList.addAll(appList);
                                }
                                else {
                                    startTime=firstdayOfweek;
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showDialog();
                                        }
                                    });
                                    tempAppList=scanLocalInstallAppInfoList(getContext().getPackageManager(),getContext(),startTime);
                                    //进度框消失
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            //窗口恢复点击
                                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        }
                                    });
                                    transToServer();
                                }
                                Collections.sort(tempAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o1.getTotalFlowLong()).compareTo(Long.valueOf(o2.getTotalFlowLong()));
                                    }
                                });
                                //List<AppItem> tempAppList=new ArrayList<AppItem>();
                                weekAppList.addAll(tempAppList);
                            }
                            else {
                                Collections.sort(weekAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o1.getTotalFlowLong()).compareTo(Long.valueOf(o2.getTotalFlowLong()));
                                    }
                                });
                            }
                            appList.clear();
                            appList.addAll(weekAppList);
                            timeFlag=1;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    appAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                        else if (reverseOrderItem.isChecked()&&weekOrderItem.isChecked()){
                            if (weekAppList.size()==0){
                                if (timeFlag==1&&appList.size()!=0){
                                    tempAppList.addAll(appList);
                                }
                                else {
                                    startTime=firstdayOfweek;
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showDialog();
                                        }
                                    });
                                    tempAppList=scanLocalInstallAppInfoList(getContext().getPackageManager(),getContext(),startTime);
                                    //进度框消失
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            //窗口恢复点击
                                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        }
                                    });
                                    transToServer();
                                }
                                Collections.sort(tempAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o2.getTotalFlowLong()).compareTo(Long.valueOf(o1.getTotalFlowLong()));
                                    }
                                });
                                //List<AppItem> tempAppList=new ArrayList<AppItem>();
                                weekAppList.addAll(tempAppList);
                            }
                            else {
                                Collections.sort(weekAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o2.getTotalFlowLong()).compareTo(Long.valueOf(o1.getTotalFlowLong()));
                                    }
                                });
                            }
                            appList.clear();
                            appList.addAll(weekAppList);
                            timeFlag=1;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    appAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                        //按月升序、降序排序
                        else if(positiveOrderItem.isChecked()&&monthOrderItem.isChecked()){
                            if (monthAppList.size()==0){
                                if (timeFlag==0&&appList.size()!=0){
                                    tempAppList.addAll(appList);
                                }
                                else {
                                    startTime=firstdayOfmonth;
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showDialog();
                                        }
                                    });
                                    tempAppList=scanLocalInstallAppInfoList(getContext().getPackageManager(),getContext(),startTime);
                                    //进度框消失
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            //窗口恢复点击
                                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        }
                                    });
                                    transToServer();
                                }
                                Collections.sort(tempAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o1.getTotalFlowLong()).compareTo(Long.valueOf(o2.getTotalFlowLong()));
                                    }
                                });
                                //List<AppItem> tempAppList=new ArrayList<AppItem>();
                                monthAppList.addAll(tempAppList);
                            }
                            else {
                                Collections.sort(monthAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o1.getTotalFlowLong()).compareTo(Long.valueOf(o2.getTotalFlowLong()));
                                    }
                                });
                            }
                            appList.clear();
                            appList.addAll(monthAppList);
                            timeFlag=0;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    appAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                        else if(reverseOrderItem.isChecked()&&monthOrderItem.isChecked()){
                            if (monthAppList.size()==0){
                                if (timeFlag==0&&appList.size()!=0){
                                    tempAppList.addAll(appList);
                                }
                                else {
                                    startTime=firstdayOfmonth;
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showDialog();
                                        }
                                    });
                                    tempAppList=scanLocalInstallAppInfoList(getContext().getPackageManager(),getContext(),startTime);
                                    //进度框消失
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            //窗口恢复点击
                                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        }
                                    });
                                    transToServer();
                                }
                                Collections.sort(tempAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o2.getTotalFlowLong()).compareTo(Long.valueOf(o1.getTotalFlowLong()));
                                    }
                                });
                                //List<AppItem> tempAppList=new ArrayList<AppItem>();
                                monthAppList.addAll(tempAppList);
                            }
                            else {
                                Collections.sort(monthAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o2.getTotalFlowLong()).compareTo(Long.valueOf(o1.getTotalFlowLong()));
                                    }
                                });
                            }
                            appList.clear();
                            appList.addAll(monthAppList);
                            timeFlag=0;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    appAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }).start();


            }
        });
        refreshLayout = (WaveSwipeRefreshLayout)root.findViewById(R.id.refreshLayout);
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.white));
        refreshLayout.setWaveColor(getResources().getColor(R.color.colorAccent));
        //Toolbar toolbar=getActivity().findViewById(R.id.toolbar);
        //int toolbarheight=toolbar.getHeight();
        //refreshLayout.setMaxDropHeight(toolbarheight);
        refreshLayout.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() {
                // Do work to refresh the list here.
                //new Task().execute();
                //refreshLayout.setRefreshing(false);
                new Task().execute();
            }
        });
        ScrollView scrollView=root.findViewById(R.id.scrollView);
        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                if (refreshLayout!=null){
                    refreshLayout.setEnabled(i1==0);
                }
            }
        });

        return root;
    }

    private void transToServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (appList.size()!=0){
                    List<Appinfo> appinfoList=new ArrayList<>();
                    switch (timeFlag){
                        case 0:
                            for (int i=0;i<appList.size();i++){
                                Appinfo appinfo=new Appinfo();
                                appinfo.setAppIcon("");
                                appinfo.setAppId(String.valueOf(appList.get(i).getAppId()));
                                appinfo.setAppName(appList.get(i).getAppName());
                                appinfo.setDescription("");
                                appinfo.setMacaddress(getNewMac());
                                appinfo.setMonthTotalString(appList.get(i).getTotalFlowString());
                                appinfo.setWeekTotalFlowString("");
                                appinfo.setDayTotalFlowString("");
                                appinfo.setTotalTime(appList.get(i).getTotalTime());
                                appinfo.setUsername(username);
                                appinfoList.add(appinfo);
                            }
                            break;
                        case 1:
                            for (int i=0;i<appList.size();i++){
                                Appinfo appinfo=new Appinfo();
                                appinfo.setAppIcon("");
                                appinfo.setAppId(String.valueOf(appList.get(i).getAppId()));
                                appinfo.setAppName(appList.get(i).getAppName());
                                appinfo.setDescription("");
                                appinfo.setMacaddress(getNewMac());
                                appinfo.setMonthTotalString("");
                                appinfo.setWeekTotalFlowString(appList.get(i).getTotalFlowString());
                                appinfo.setDayTotalFlowString("");
                                appinfo.setTotalTime(appList.get(i).getTotalTime());
                                appinfo.setUsername(username);
                                appinfoList.add(appinfo);
                            }
                            break;
                        case 2:
                            for (int i=0;i<appList.size();i++){
                                Appinfo appinfo=new Appinfo();
                                appinfo.setAppIcon("");
                                appinfo.setAppId(String.valueOf(appList.get(i).getAppId()));
                                appinfo.setAppName(appList.get(i).getAppName());
                                appinfo.setDescription("");
                                appinfo.setMacaddress(getNewMac());
                                appinfo.setMonthTotalString("");
                                appinfo.setWeekTotalFlowString("");
                                appinfo.setDayTotalFlowString(appList.get(i).getTotalFlowString());
                                appinfo.setTotalTime(appList.get(i).getTotalTime());
                                appinfo.setUsername(username);
                                appinfoList.add(appinfo);
                            }
                            break;
                        default:
                            break;
                    }

                    Gson gson = new Gson();
                    //String jsonString = gson.toJson(appinfoList);
                    String jsonArray = gson.toJson(appinfoList, new TypeToken<List<Appinfo>>() {}.getType());
                    RequestBody requestBody = new FormBody.Builder()
                            .add("applist",jsonArray)
                            .build();

                    String url = getResources().getString(R.string.ip)+getResources().getString(R.string.addappinfo);
                    OkHttpClient okHttpClient = new OkHttpClient();
                    final Request request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();
                    Call call = okHttpClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("网络请求","请求失败");
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String respose_text = response.body().string();
                            Log.e("网络请求",respose_text);

                        }
                    });
                }
            }
        }).start();
    }

    private class Task extends AsyncTask<Void, Void, String[]> {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected String[] doInBackground(Void... voids) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    //窗口不可点击
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            });

            String[] test=new String[1];
            timeFlag=0;
            appList.clear();
            dayAppList.clear();
            weekAppList.clear();
            monthAppList.clear();
            appList=scanLocalInstallAppInfoList(getContext().getPackageManager(),getContext(),startTime);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    appAdapter.notifyDataSetChanged();
                }
            });
            transToServer();
            test[0]="true";
            return test;
        }
        @Override
        protected void onPostExecute(String[] result) {
            // Call setRefreshing(false) when the list has been refreshed.
            Log.e("刷新","刷新已完成");
            refreshLayout.setRefreshing(false);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    //窗口恢复点击
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            });
            super.onPostExecute(result);
        }
    }
}

