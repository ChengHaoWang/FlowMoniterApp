package com.example.test.ui.dashboard;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.test.AppItem;
import com.example.test.CustomPopWindow;
import com.example.test.R;
import com.example.test.SpeedItem;
import com.example.test.SpeedPopWindow;
import com.example.test.SpeedService;
import com.example.test.ui.home.AppAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.example.test.ApkTool.getInitiSpeedList;
import static com.example.test.ApkTool.scanLocalInstallAppInfoList;

public class DashboardFragment extends Fragment {

    private SpeedService service = null;
    private Intent speedServiceIntent;
    private DashboardViewModel dashboardViewModel;
    //private List<SpeedItem> speedItemArrayList=new ArrayList<SpeedItem>();
    private List<AppItem> appList=new ArrayList<AppItem>();
    private SpeedAdapter speedAdapter;
    private SpeedPopWindow speedPopWindow;
    private List<AppItem> sortAppList=new ArrayList<AppItem>();

    //定时器
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){//处理消息
            switch (msg.what){
                case 0:
                    //更新UI
                    //Log.e("Speed","UI计时器循环");
                    //Log.e("speed", String.valueOf(speedItemArrayList.size()));
                    if(service!=null){
                        List<AppItem> mspeedItemArrayList= service.getSpeedList();

                        //Log.e("速度界面","元大小"+appList.size());
                        appList.clear();
                        appList.addAll(mspeedItemArrayList);
                        speedAdapter.notifyDataSetChanged();
                        //Log.e("速度界面","界面更新"+mspeedItemArrayList.size()+appList.size());
                        /*
                        appList.clear();
                        appList.addAll(mspeedItemArrayList);
                        speedAdapter.notifyDataSetChanged();
                         */
                    }
                    break;
            }
        }
    };
    //为弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            speedPopWindow.dismiss();
            switch (v.getId()) {

            }
        }

    };
    private ServiceConnection conn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e("Kathy", "链接服务成功");
            SpeedService.MyBinder myBinder = (SpeedService.MyBinder) iBinder;
            service = myBinder.getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("Kathy", "服务断开");
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        EventBus.getDefault().register(this);//用了粘性，这个位置无所谓
        //Log.e("传消息","已注册");

        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);

        dashboardViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });


        if(savedInstanceState!= null){
            String FRAGMENTS_TAG = "android:support:fragments";
            savedInstanceState.remove(FRAGMENTS_TAG);
        }
        speedServiceIntent=new Intent(getActivity(), SpeedService.class);
        getActivity().bindService(speedServiceIntent,conn, BIND_AUTO_CREATE);
        if (appList.size()!=0){
            speedAdapter=new SpeedAdapter(getContext(),R.layout.speed_item,appList);
            ListView speedlistview=root.findViewById(R.id.speedlistview);
            speedlistview.setAdapter(speedAdapter);
        }else {
            Toast.makeText(getActivity(),"请先加载APP流量列表",Toast.LENGTH_SHORT).show();
        }

        Timer timer = new Timer();
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 0;
                msg.obj = 0;
                handler.sendMessage(msg);//定时器超时，发送消息
            }
        };
        timer.schedule(timerTask,0,5000);

        //右上角菜单
        final TextView title=getActivity().findViewById(R.id.title);
        if (title.getText().equals("实时速度")){
            final ImageView menu=getActivity().findViewById(R.id.menu);
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    speedPopWindow = new SpeedPopWindow(getActivity(), itemsOnClick);

                    final CheckBox positiveOrderItem = speedPopWindow.view.findViewById(R.id.positiveOrder);
                    final CheckBox reverseOrderItem = speedPopWindow.view.findViewById(R.id.reverseOrder);
                    final CheckBox nameOrderItem = speedPopWindow.view.findViewById(R.id.nameOrder);

                    speedPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            speedPopWindow.backgroundAlpha(getActivity(), 1f);
                            //判断是否存在排序需求
                            //速度正序
                            if (positiveOrderItem.isChecked()){
                                if (sortAppList.size()==0){
                                    sortAppList.addAll(appList);
                                }
                                Collections.sort(sortAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o1.getUnconvertedSpeed()).compareTo(Long.valueOf(o2.getUnconvertedSpeed()));
                                    }
                                });
                                List<AppItem> tempAppList=new ArrayList<AppItem>();
                                tempAppList.addAll(sortAppList);
                                appList.clear();
                                appList.addAll(tempAppList);

                                speedAdapter.notifyDataSetChanged();
                                //通知Service数组变更
                                final List<AppItem> tappList=new ArrayList<AppItem>();
                                tappList.removeAll(tappList);
                                tappList.addAll(appList);
                                EventBus.getDefault().postSticky(tappList);
                            }
                            //速度倒序
                            if (reverseOrderItem.isChecked()){
                                if (sortAppList.size()==0){
                                    sortAppList.addAll(appList);
                                }
                                Collections.sort(sortAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        //int i = (int)(o1.getTotalFlowLong() - o2.getTotalFlowLong());
                                        return Long.valueOf(o2.getUnconvertedSpeed()).compareTo(Long.valueOf(o1.getUnconvertedSpeed()));
                                    }
                                });
                                List<AppItem> tempAppList=new ArrayList<AppItem>();
                                tempAppList.addAll(sortAppList);
                                appList.clear();
                                appList.addAll(tempAppList);

                                speedAdapter.notifyDataSetChanged();
                                //通知Service数组变更
                                final List<AppItem> tappList=new ArrayList<AppItem>();
                                tappList.removeAll(tappList);
                                tappList.addAll(appList);
                                EventBus.getDefault().postSticky(tappList);
                            }
                            //名称排序
                            if (nameOrderItem.isChecked()){
                                if (sortAppList.size()==0){
                                    sortAppList.addAll(appList);
                                }
                                Collections.sort(sortAppList, new Comparator<AppItem>() {
                                    @Override
                                    public int compare(AppItem o1, AppItem o2) {
                                        Comparator<Object> compare = Collator.getInstance(java.util.Locale.CHINA);
                                        return compare.compare(o1.getAppName(), o2.getAppName());
                                    }
                                });
                                List<AppItem> tempAppList=new ArrayList<AppItem>();
                                tempAppList.addAll(sortAppList);
                                appList.clear();
                                appList.addAll(tempAppList);

                                speedAdapter.notifyDataSetChanged();
                                //通知Service数组变更
                                final List<AppItem> tappList=new ArrayList<AppItem>();
                                tappList.removeAll(tappList);
                                tappList.addAll(appList);
                                EventBus.getDefault().postSticky(tappList);
                            }

                        }
                    });
                    //mPopwindow.setAnimationStyle(R.style.popwin_anim_style);//动画执行必须在AtLocation之前
                    //获取toolbar的高度
                    Toolbar toolbar=getActivity().findViewById(R.id.toolbar);
                    int toolbarheight=toolbar.getHeight();
                    int menuheight=menu.getHeight();
                    int menuwidth=menu.getWidth();
                    speedPopWindow.showAtLocation(view, Gravity.RIGHT|Gravity.TOP, menuwidth/2, toolbarheight+(menuheight*3/4));
                    //弹出菜单监听
                    positiveOrderItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //positiveOrderItem.setChecked(!positiveOrderItem.isChecked());
                            if (positiveOrderItem.isChecked()){
                                reverseOrderItem.setClickable(false);
                                nameOrderItem.setClickable(false);
                            }else {
                                reverseOrderItem.setClickable(true);
                                nameOrderItem.setClickable(true);
                            }
                        }
                    });
                    reverseOrderItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (reverseOrderItem.isChecked()){
                                positiveOrderItem.setClickable(false);
                                nameOrderItem.setClickable(false);
                            }else {
                                positiveOrderItem.setClickable(true);
                                nameOrderItem.setClickable(true);
                            }
                        }
                    });
                    nameOrderItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (nameOrderItem.isChecked()){
                                positiveOrderItem.setClickable(false);
                                reverseOrderItem.setClickable(false);
                            }else {
                                positiveOrderItem.setClickable(true);
                                reverseOrderItem.setClickable(true);
                            }
                        }
                    });
                }
            });
        }

        return root;
    }
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unbindService(conn);
    }
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onEvent(List<AppItem> passAppList) {
        Log.e("传递消息","消息已接受");
        //int timeflag=passtimeflag;
        //Log.e("时间flag为", String.valueOf(timeflag));
        if (passAppList.size()!=0){
            appList.clear();
            appList.addAll(passAppList);
        }
        else
            Toast.makeText(getContext(),"无法接收消息",Toast.LENGTH_SHORT).show();
        //EventBus.getDefault().removeStickyEvent(appList.getClass());
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
    //解除注册
    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }
}