package com.example.test.ui.home;

import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.test.AppItem;
import com.example.test.BottomNavigation;
import com.example.test.CustomPopWindow;
import com.example.test.MainActivity;
import com.example.test.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static android.content.Context.NETWORK_STATS_SERVICE;
import static com.example.test.ApkTool.scanLocalInstallAppInfoList;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private List<AppItem> appList=new ArrayList<AppItem>();
    private CustomPopWindow mPopwindow;
    private AppAdapter appAdapter;
    private long startTime;
    private List<AppItem> dayAppList=new ArrayList<AppItem>();
    private List<AppItem> weekAppList=new ArrayList<AppItem>();
    private List<AppItem> monthAppList=new ArrayList<AppItem>();
    private int timeFlag=0;//0=month;1=week;2=day
    //protected WeakReference<View> mRootView;//缓存view
    //为弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            mPopwindow.dismiss();
            switch (v.getId()) {

            }
        }

    };
    private View.OnClickListener menuOnclick=new View.OnClickListener(){
        @Override
        public void onClick(View view) {

        }
    };

    //获取本月第一天0点的时间
    public static long getTimesMonthMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return cal.getTimeInMillis();
    }
    //获得本周一0点时间
    public static long getTimesWeekmorning(){
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0,0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
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
    public static Date getTimesmorning2() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);

        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        final long firstdayOfmonth =getTimesMonthMorning();
        final long firstdayOfweek=getTimesWeekmorning();
        final long fisttimeOfday=getTimesmorning();
        startTime=firstdayOfmonth;
        //Date a=getTimesmorning2();
        //设置走马灯被选中
        TextView rolltext=root.findViewById(R.id.rolltext);
        rolltext.setSelected(true);

        //获取流量信息
        new Thread(new Runnable() {
            @Override
            public void run() {
                appList.clear();
                appList=scanLocalInstallAppInfoList(getContext().getPackageManager(),getContext(),startTime);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                    //更新UI，回归主线程
                    appAdapter=new AppAdapter(getContext(),R.layout.app_item,appList);
                    ListView applistview=root.findViewById(R.id.applistview);
                    applistview.setAdapter(appAdapter);

                    final List<AppItem> tappList=new ArrayList<AppItem>();
                    tappList.removeAll(tappList);
                    tappList.addAll(appList);
                    EventBus.getDefault().postSticky(tappList);
                    //EventBus.getDefault().postSticky(timeFlag);
                    //Log.e("传递消息","消息已发送");
                    }
                });
            }
        }).start();
        //右上角菜单
        final TextView title=getActivity().findViewById(R.id.title);
        if (title.getText().equals("流量")){
            final ImageView menu=getActivity().findViewById(R.id.menu);
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPopwindow = new CustomPopWindow(getActivity(), itemsOnClick);
                    final CheckBox positiveOrderItem = mPopwindow.view.findViewById(R.id.positiveOrder);
                    final CheckBox reverseOrderItem = mPopwindow.view.findViewById(R.id.reverseOrder);
                    final CheckBox dayOrderItem = mPopwindow.view.findViewById(R.id.dayOrder);
                    final CheckBox weekOrderItem = mPopwindow.view.findViewById(R.id.weekOrder);
                    final CheckBox monthOrderItem=mPopwindow.view.findViewById(R.id.monothOrder);

                    mPopwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            mPopwindow.backgroundAlpha(getActivity(), 1f);
                            //临时列表
                            //tempAppList.addAll(appList);
                            //按天升序、降序排序
                            if (positiveOrderItem.isChecked()&&dayOrderItem.isChecked()){
                                if (dayAppList.size()==0){
                                    if (timeFlag==2&&appList.size()!=0){
                                        dayAppList.addAll(appList);
                                    }
                                    else {
                                        startTime=fisttimeOfday;
                                        dayAppList=scanLocalInstallAppInfoList(getContext().getPackageManager(),getContext(),startTime);
                                    }
                                }
                                Collections.sort(dayAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o1.getTotalFlowLong()).compareTo(Long.valueOf(o2.getTotalFlowLong()));
                                    }
                                });
                                List<AppItem> tempAppList=new ArrayList<AppItem>();
                                tempAppList.addAll(dayAppList);
                                appList.clear();
                                appList.addAll(tempAppList);
                                timeFlag=2;
                                appAdapter.notifyDataSetChanged();
                            }
                            else if(reverseOrderItem.isChecked()&&dayOrderItem.isChecked()){
                                if (dayAppList.size()==0){
                                    if (timeFlag==2&&appList.size()!=0){
                                        dayAppList.addAll(appList);
                                    }
                                    else {
                                        startTime=fisttimeOfday;
                                        dayAppList=scanLocalInstallAppInfoList(getContext().getPackageManager(),getContext(),startTime);
                                    }
                                }
                                Collections.sort(dayAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o2.getTotalFlowLong()).compareTo(Long.valueOf(o1.getTotalFlowLong()));
                                    }
                                });
                                List<AppItem> tempAppList=new ArrayList<AppItem>();
                                tempAppList.addAll(dayAppList);
                                appList.clear();
                                appList.addAll(tempAppList);
                                timeFlag=2;
                                appAdapter.notifyDataSetChanged();
                            }
                            //按周升序、降序排序
                            else if (positiveOrderItem.isChecked()&&weekOrderItem.isChecked()){
                                if (weekAppList.size()==0){
                                    if (timeFlag==1&&appList.size()!=0){
                                        weekAppList.addAll(appList);
                                    }
                                    else {
                                        startTime=firstdayOfweek;
                                        weekAppList=scanLocalInstallAppInfoList(getContext().getPackageManager(),getContext(),startTime);
                                    }
                                }
                                Collections.sort(weekAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o1.getTotalFlowLong()).compareTo(Long.valueOf(o2.getTotalFlowLong()));
                                    }
                                });
                                List<AppItem> tempAppList=new ArrayList<AppItem>();
                                tempAppList.addAll(weekAppList);
                                appList.clear();
                                appList.addAll(tempAppList);
                                timeFlag=1;
                                appAdapter.notifyDataSetChanged();
                            }
                            else if (reverseOrderItem.isChecked()&&weekOrderItem.isChecked()){
                                if (weekAppList.size()==0){
                                    if (timeFlag==1&&appList.size()!=0){
                                        weekAppList.addAll(appList);
                                    }
                                    else {
                                        startTime=firstdayOfweek;
                                        weekAppList=scanLocalInstallAppInfoList(getContext().getPackageManager(),getContext(),startTime);
                                    }
                                }
                                Collections.sort(weekAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o2.getTotalFlowLong()).compareTo(Long.valueOf(o1.getTotalFlowLong()));
                                    }
                                });
                                List<AppItem> tempAppList=new ArrayList<AppItem>();
                                tempAppList.addAll(weekAppList);
                                appList.clear();
                                appList.addAll(tempAppList);
                                timeFlag=1;
                                appAdapter.notifyDataSetChanged();
                            }
                            //按月升序、降序排序
                            else if(positiveOrderItem.isChecked()&&monthOrderItem.isChecked()){
                                if (monthAppList.size()==0){
                                    if (timeFlag==0&&appList.size()!=0){
                                        monthAppList.addAll(appList);
                                    }
                                    else {
                                        startTime=firstdayOfmonth;
                                        monthAppList=scanLocalInstallAppInfoList(getContext().getPackageManager(),getContext(),startTime);
                                    }
                                }
                                Collections.sort(monthAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o1.getTotalFlowLong()).compareTo(Long.valueOf(o2.getTotalFlowLong()));
                                    }
                                });
                                List<AppItem> tempAppList=new ArrayList<AppItem>();
                                tempAppList.addAll(monthAppList);
                                appList.clear();
                                appList.addAll(tempAppList);
                                timeFlag=0;
                                appAdapter.notifyDataSetChanged();
                            }
                            else if(reverseOrderItem.isChecked()&&monthOrderItem.isChecked()){
                                if (monthAppList.size()==0){
                                    if (timeFlag==0&&appList.size()!=0){
                                        monthAppList.addAll(appList);
                                    }
                                    else {
                                        startTime=firstdayOfmonth;
                                        monthAppList=scanLocalInstallAppInfoList(getContext().getPackageManager(),getContext(),startTime);
                                    }
                                }
                                Collections.sort(monthAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o2.getTotalFlowLong()).compareTo(Long.valueOf(o1.getTotalFlowLong()));
                                    }
                                });
                                List<AppItem> tempAppList=new ArrayList<AppItem>();
                                tempAppList.addAll(monthAppList);
                                appList.clear();
                                appList.addAll(tempAppList);
                                timeFlag=0;
                                appAdapter.notifyDataSetChanged();
                            }

                        }
                    });
                    //mPopwindow.setAnimationStyle(R.style.popwin_anim_style);//动画执行必须在AtLocation之前
                    //获取toolbar的高度
                    Toolbar toolbar=getActivity().findViewById(R.id.toolbar);
                    int toolbarheight=toolbar.getHeight();
                    int menuheight=menu.getHeight();
                    int menuwidth=menu.getWidth();
                    mPopwindow.showAtLocation(view, Gravity.RIGHT|Gravity.TOP, menuwidth/2, toolbarheight+(menuheight*3/4));
                    //弹出菜单监听
                    positiveOrderItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //positiveOrderItem.setChecked(!positiveOrderItem.isChecked());
                            if (positiveOrderItem.isChecked()){
                                reverseOrderItem.setClickable(false);
                            }else {
                                reverseOrderItem.setClickable(true);
                            }
                        }
                    });
                    reverseOrderItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (reverseOrderItem.isChecked()){
                                positiveOrderItem.setClickable(false);
                            }else {
                                positiveOrderItem.setClickable(true);
                            }
                        }
                    });
                    dayOrderItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (dayOrderItem.isChecked()){
                                weekOrderItem.setClickable(false);
                                monthOrderItem.setClickable(false);
                            }else {
                                weekOrderItem.setClickable(true);
                                monthOrderItem.setClickable(true);
                            }
                        }
                    });
                    weekOrderItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (weekOrderItem.isChecked()){
                                dayOrderItem.setClickable(false);
                                monthOrderItem.setClickable(false);
                            }else {
                                dayOrderItem.setClickable(true);
                                monthOrderItem.setClickable(true);
                            }
                        }
                    });
                    monthOrderItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (monthOrderItem.isChecked()){
                                dayOrderItem.setClickable(false);
                                weekOrderItem.setClickable(false);
                            }else {
                                dayOrderItem.setClickable(true);
                                weekOrderItem.setClickable(true);
                            }
                        }
                    });
                }
            });
        }

        return root;
    }

}
