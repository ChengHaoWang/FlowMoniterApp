package com.example.test;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.test.ui.dashboard.DashboardFragment;
import com.example.test.ui.home.HomeFragment;
import com.example.test.ui.notifications.NotificationsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.makeramen.roundedimageview.RoundedImageView;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.widget.LinearLayout.*;
import static com.example.test.ApkTool.REQUEST_READ_PHONE_STATE;

public class BottomNavigation extends FragmentActivity{
    private static final String TAG = "MainActivity";
    public static CustomPopWindow homePopWindow;
    public static SpeedPopWindow speedPopWindow;
    private String sdkNum=null;//sdk号码
    private String model=null; //手机型号
    private String release =null; //android系统版本号
    private String mac = null;//mac地址
    private Fragment currentFragment=new Fragment();
    private FragmentManager manager;
    private ImageView menu;
    private SharedPreferences sp;

    //为弹出窗口实现监听类
    private View.OnClickListener homeItemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            homePopWindow.dismiss();
            switch (v.getId()) {

            }
        }

    };
    private View.OnClickListener speedItemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            speedPopWindow.dismiss();
            switch (v.getId()) {

            }
        }

    };
    private OnClickListener homeMenuOnClick=new OnClickListener() {
        @Override
        public void onClick(View view) {
            final CheckBox positiveOrderItem = homePopWindow.view.findViewById(R.id.positiveOrder);
            final CheckBox reverseOrderItem = homePopWindow.view.findViewById(R.id.reverseOrder);
            final CheckBox dayOrderItem = homePopWindow.view.findViewById(R.id.dayOrder);
            final CheckBox weekOrderItem = homePopWindow.view.findViewById(R.id.weekOrder);
            final CheckBox monthOrderItem=homePopWindow.view.findViewById(R.id.monothOrder);
            positiveOrderItem.setChecked(false);
            reverseOrderItem.setChecked(false);
            dayOrderItem.setChecked(false);
            weekOrderItem.setChecked(false);
            monthOrderItem.setChecked(false);
            //获取toolbar的高度
            Toolbar toolbar=findViewById(R.id.toolbar);
            int toolbarheight=toolbar.getHeight();
            int menuheight=menu.getHeight();
            int menuwidth=menu.getWidth();

            homePopWindow.showAtLocation(view, Gravity.RIGHT|Gravity.TOP, menuwidth/2, toolbarheight+(menuheight*3/4));

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
    };
    private OnClickListener speedMenuOnClick=new OnClickListener() {
        @Override
        public void onClick(View view) {
            final CheckBox positiveOrderItem = speedPopWindow.view.findViewById(R.id.positiveOrder);
            final CheckBox reverseOrderItem = speedPopWindow.view.findViewById(R.id.reverseOrder);
            final CheckBox nameOrderItem = speedPopWindow.view.findViewById(R.id.nameOrder);
            positiveOrderItem.setChecked(false);
            reverseOrderItem.setChecked(false);
            nameOrderItem.setChecked(false);
            //获取toolbar的高度
            Toolbar toolbar=findViewById(R.id.toolbar);
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
    };
    private void showFragment(Fragment fragment) {
        /*
        FragmentTransaction transaction = manager.beginTransaction();
        if (!currentFragment.isAdded()){
            transaction.add(R.id.nav_host_fragment, currentFragment);
        }
        if (!toFragment.isAdded()){
            //FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.nav_host_fragment, toFragment);
        }
        if (currentFragment!=toFragment){
            //FragmentTransaction transaction = manager.beginTransaction();
            transaction.hide(currentFragment).show(toFragment);
        }else {
            transaction.show(toFragment);
        }
        transaction.commit();
         */

        if (currentFragment!=fragment) {
            FragmentTransaction transaction = manager.beginTransaction();
            Log.e("Fragment数量", String.valueOf(manager.getFragments().size()));
            if (!currentFragment.isAdded()){
                transaction.add(R.id.nav_host_fragment, currentFragment);
            }
            transaction.hide(currentFragment);
            currentFragment = fragment;
            if (!fragment.isAdded()) {
                transaction.add(R.id.nav_host_fragment, fragment).show(fragment).commit();
            } else {
                transaction.show(fragment).commit();
            }
/*
            Log.e("Fragment数量", String.valueOf(manager.getFragments().size()));
            if (fragment.isVisible()){
                Log.e("Fragment以显示", String.valueOf(manager.getFragments().size()));
            }
            if (fragment.isHidden()){
                Log.e("Fragment已隐藏", String.valueOf(manager.getFragments().size()));
            }
            Log.e("现在的Fragment", String.valueOf(currentFragment.getId()));
 */
        }
     }
    @SuppressLint("RestrictedApi")
    public Fragment getCurrentFragment() {
        manager = getSupportFragmentManager();
        List<Fragment> fragments = manager.getFragments();
        for(int i = 0; i < fragments.size(); i++) {
            Fragment fragment = fragments.get(i);
            if(fragment!=null && fragment.isAdded()&&fragment.isMenuVisible()) {
                return fragment;
            }
        }
        return null;
    }
    //为了避免崩溃重启后fragment重叠问题
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_bottom_navigation);
        setStatusBarColor(BottomNavigation.this);
        final BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        //Toolbar mToolbar=findViewById(R.id.toolbar);
        //setSupportActionBar(mToolbar);//利用Toolbar代替ActionBar
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        homePopWindow = new CustomPopWindow(BottomNavigation.this, homeItemsOnClick);
        speedPopWindow = new SpeedPopWindow(BottomNavigation.this, speedItemsOnClick);
        menu=findViewById(R.id.menu);
        Fragment homeFragment=new Fragment();
        final DashboardFragment dashboardFragment=new DashboardFragment();
        final NotificationsFragment notificationsFragment=new NotificationsFragment();
        //获取当前Fragment
        manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        //Fragment current = getCurrentFragment();
        if (manager.getFragments().size()==0){
            Fragment current=manager.findFragmentById(R.id.nav_host_fragment);
            if (current != null ){
                currentFragment=current;
                homeFragment=current;
                //transaction.hide(current).commit();
            }
        }
        final Fragment finalHomeFragment = homeFragment;
        final TextView title=findViewById(R.id.title);
        menu.setOnClickListener(homeMenuOnClick);
        //导航栏点击监听事件
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //int id=menuItem.getItemId();
                if (menuItem.getTitle().toString().equals(getResources().getString(R.string.title_home))){
                    title.setText(getResources().getString(R.string.title_home));
                    showFragment(finalHomeFragment);
                    menu.setOnClickListener(homeMenuOnClick);
                    //Log.e("导航栏1", String.valueOf(id));
                }
                else if (menuItem.getTitle().toString().equals(getResources().getString(R.string.title_dashboard))){
                    title.setText(getResources().getString(R.string.title_dashboard));
                    showFragment(dashboardFragment);
                    menu.setOnClickListener(speedMenuOnClick);
                    //Log.e("导航栏2", String.valueOf(id));
                }
                else if (menuItem.getTitle().toString().equals(getResources().getString(R.string.title_notifications))){
                    title.setText(getResources().getString(R.string.title_notifications));
                    showFragment(notificationsFragment);
                    //Log.e("导航栏3", String.valueOf(id));

                }
                /*
                switch (id){
                    case 2131296413:
                        //title.setText("流量");
                        showFragment(finalHomeFragment);
                        Log.e("导航栏1", String.valueOf(id));
                        break;
                    case 2131296411:
                        //title.setText("实时速度");
                        showFragment(dashboardFragment);
                        Log.e("导航栏2", String.valueOf(id));
                        break;
                    case 2131296414:
                        //title.setText("未知");
                        showFragment(notificationsFragment);
                        Log.e("导航栏3", String.valueOf(id));
                        break;
                     default:
                         break;
                }
                 */
                return true;
            }
        });

        //显示侧边栏
        RoundedImageView nav_head=findViewById(R.id.nav_head);
        final DrawerLayout drawerLayout=findViewById(R.id.drawer_left);
        nav_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(Gravity.LEFT);
                setStatusBarColor(BottomNavigation.this);
            }
        });
        /*
        //右上角菜单
        final ImageView menu=findViewById(R.id.menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopwindow = new CustomPopWindow(BottomNavigation.this, itemsOnClick);

                mPopwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        mPopwindow.backgroundAlpha(BottomNavigation.this, 1f);
                    }
                });
                //mPopwindow.setAnimationStyle(R.style.popwin_anim_style);//动画执行必须在AtLocation之前
                //获取toolbar的高度
                Toolbar toolbar=findViewById(R.id.toolbar);
                int toolbarheight=toolbar.getHeight();
                int menuheight=menu.getHeight();
                int menuwidth=menu.getWidth();
                mPopwindow.showAtLocation(view, Gravity.RIGHT|Gravity.TOP, menuwidth/2, toolbarheight+(menuheight*3/4));
            }
        });

         */
        //改变颜色
        final ImageView fabIconNew = new ImageView(BottomNavigation.this);
        //fabIconNew.setImageDrawable(getResources().getDrawable(R.drawable.color_change));
        FloatingActionButton.LayoutParams starParams = new FloatingActionButton.LayoutParams(80, 80);
        final FloatingActionButton rightLowerButton = new FloatingActionButton.Builder(BottomNavigation.this).setContentView(fabIconNew).setBackgroundDrawable(R.drawable.color_change).
                setLayoutParams(starParams)
                .build();

        SubActionButton.Builder rLSubBuilder = new SubActionButton.Builder(BottomNavigation.this);
        ImageView rlIcon1 = new ImageView(BottomNavigation.this);
        ImageView rlIcon2 = new ImageView(BottomNavigation.this);
        ImageView rlIcon3 = new ImageView(BottomNavigation.this);
        ImageView rlIcon4 = new ImageView(BottomNavigation.this);
        rlIcon1.setImageDrawable(getResources().getDrawable(R.drawable.black));
        rlIcon2.setImageDrawable(getResources().getDrawable(R.drawable.orange));
        rlIcon3.setImageDrawable(getResources().getDrawable(R.drawable.green));

        // FloatingActionMenu通过attachTo(rightLowerButton)附着到FloatingActionButton
        final FloatingActionMenu rightLowerMenu = new FloatingActionMenu.Builder(BottomNavigation.this)
                .addSubActionView(rLSubBuilder.setContentView(rlIcon1).setBackgroundDrawable(getResources().getDrawable(R.drawable.white)).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon2).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon3).build())
                .setStartAngle(45).setEndAngle(-45).setRadius(150).attachTo(rightLowerButton).build();

        // Listen menu open and close events to animate the button content view
        rightLowerMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu menu) {
                // 增加按钮中的+号图标顺时针旋转45度
                // Rotate the icon of rightLowerButton 45 degrees clockw
                fabIconNew.setRotation(0);
                PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 45);
                ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(fabIconNew, pvhR);
                animation.start();
            }

            @Override
            public void onMenuClosed(FloatingActionMenu menu) {
                // 增加按钮中的+号图标逆时针旋转45度
                // Rotate the icon of rightLowerButton 45 degrees
                // counter-clockwise
                fabIconNew.setRotation(45);
                PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 0);
                ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(fabIconNew, pvhR);
                animation.start();
            }
        });
        final View drawer=findViewById(R.id.drawer_page);
        LinearLayout change_color_container=drawer.findViewById(R.id.color_change_container);
        //将颜色按钮填充进布局
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        //ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.activity_bottom_navigation, null);
        FrameLayout parent2 = (FrameLayout) rightLowerButton.getParent();
        //ConstraintLayout parent1 = (ConstraintLayout) ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
        parent2.removeView(rightLowerButton);
        change_color_container.addView(rightLowerButton);
        //监听颜色按钮点击
        rlIcon1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(BottomNavigation.this,"点击颜色按钮1",Toast.LENGTH_SHORT).show();
            }
        });
        rlIcon2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        rlIcon3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.green));
            }
        });
        //监听抽屉关闭事件
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                rightLowerMenu.close(false);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                rightLowerMenu.close(false);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                rightLowerMenu.close(false);
            }
        });
        //抽屉关闭按钮
        ImageView close_drawer=drawer.findViewById(R.id.close_drawer);
        close_drawer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.closeDrawers();
            }
        });
        //填充抽屉内容
        TextView phonetype=drawer.findViewById(R.id.phone_type);
        TextView macaddress=drawer.findViewById(R.id.macaddress);
        final TextView workspace=drawer.findViewById(R.id.worksapce);
        final TextView position=drawer.findViewById(R.id.position);
        final TextView contactway=drawer.findViewById(R.id.contact_way);
        final TextView personaldes=drawer.findViewById(R.id.personal_des);

        final RoundedImageView drawerheadimage=drawer.findViewById(R.id.headimage);//导航栏头像
        final RoundedImageView mainheadimage=findViewById(R.id.nav_head);
        final TextView nickname=drawer.findViewById(R.id.nickname);
        TextView phonenumber=drawer.findViewById(R.id.phonenumber);

        GetPhoneInfo();
        if (!model.equals("")){
            phonetype.setText(model);
        }

        if (!mac.equals("")){
            macaddress.setText(mac);
        }

        //初始化请求
        sp = getSharedPreferences("userInfo", 0);
        final String username=sp.getString("username", "");
        phonenumber.setText(username);
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
                        JSONObject jsonObject1 = null;
                        JSONObject jsonObject2 = null;
                        Log.e("网络请求",respose_text);
                        try {
                            jsonObject1 = new JSONObject(respose_text);
                            String result1 = jsonObject1.optString("result", null);
                            jsonObject2=new JSONObject(result1);

                            final String icompany=jsonObject2.optString("company",null);
                            final String iduty=jsonObject2.optString("duty",null);
                            final String iemail=jsonObject2.optString("email",null);
                            final String isign=jsonObject2.optString("signature",null);
                            final String iheadsrc=jsonObject2.optString("headimg",null);
                            final String inickname=jsonObject2.optString("nickname",null);

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    //head_image_upload=findViewById(R.id.head_image_upload);
                                    if (!icompany.equals("")){
                                        workspace.setText(icompany);
                                    }
                                    if (!iduty.equals("")){
                                        position.setText(iduty);
                                    }
                                    if (!iemail.equals("")){
                                        contactway.setText(iemail);
                                    }
                                    if (!isign.equals("")){
                                        personaldes.setText(isign);
                                    }
                                    //String url1 = getResources().getString(R.string.ip)+getResources().getString(R.string.testpic);
                                    if (!iheadsrc.equals("")){
                                        Glide.with(BottomNavigation.this)
                                                .load(iheadsrc)
                                                .centerCrop()
                                                .placeholder(R.drawable.default_head)
                                                .into(drawerheadimage);
                                        Glide.with(BottomNavigation.this)
                                                .load(iheadsrc)
                                                .centerCrop()
                                                .placeholder(R.drawable.default_head)
                                                .into(mainheadimage);
                                    }
                                    if (!inickname.equals("")){
                                        nickname.setText(inickname);
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
        //设置抽屉中按钮的监听
        LinearLayout editinfo=findViewById(R.id.editinfo);
        LinearLayout editpassword=findViewById(R.id.editpassword);
        ImageView change_account=findViewById(R.id.change_account);
        editinfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(BottomNavigation.this,EditInfo.class);
                startActivity(intent);
            }
        });
        editpassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(BottomNavigation.this,EditPassword.class);
                startActivity(intent);
            }
        });

        change_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor =sp.edit();
                editor.putString("username", "");
                editor.putString("password", "");
                editor.putString("loginstatus","false");
                editor.putLong("starttime",0);
                editor.commit();
                Intent intent=new Intent(BottomNavigation.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }


    /**
     * 解决Toolbar中Menu无法同时显示图标和文字的问题
     * */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.M)
    public void setStatusBarColor(Activity activity) {
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //取消设置Window半透明的Flag
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //添加Flag把状态栏设为可绘制模式
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏为蓝色
            //window.setStatusBarColor(Color.parseColor("#5CACEE"));
            //window.setStatusBarColor(getResources().getColor(R.color.actionbar));
            window.setStatusBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
                localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    DrawerLayout drawerLayout=findViewById(R.id.drawer_left);
                    //将侧边栏顶部延伸至status bar
                    drawerLayout.setFitsSystemWindows(true);
                    //将主页面顶部延伸至status bar;虽默认为false,但经测试,DrawerLayout需显示设置
                    drawerLayout.setClipToPadding(false);
                }
            }

            if(Build.VERSION.SDK_INT >= 21) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.setStatusBarColor(Color.TRANSPARENT);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void GetPhoneInfo() {
        //this.sdkNum = android.os.Build.VERSION.SDK; // SDK号
        this.model = android.os.Build.MODEL; // 手机型号
        //this.release = android.os.Build.VERSION.RELEASE; // android系统版本号
        this.mac = getNewMac();
    }

    /**
     * 通过网络接口取mac
     * @return
     */
    public static String getNewMac() {
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
}
