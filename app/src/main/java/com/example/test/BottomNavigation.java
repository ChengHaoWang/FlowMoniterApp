package com.example.test;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

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

import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import static android.widget.LinearLayout.*;
import static com.example.test.ApkTool.REQUEST_READ_PHONE_STATE;

public class BottomNavigation extends FragmentActivity{
    private static final String TAG = "MainActivity";
    private CustomPopWindow mPopwindow;
    private String sdkNum=null;//sdk号码
    private String model=null; //手机型号
    private String release =null; //android系统版本号
    private String mac = null;//mac地址
    private Fragment currentFragment=new Fragment();
    private FragmentManager manager;

    //为弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            mPopwindow.dismiss();
            switch (v.getId()) {

            }
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
        final ImageView menu=findViewById(R.id.menu);
        //导航栏点击监听事件
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //int id=menuItem.getItemId();
                if (menuItem.getTitle().toString().equals(getResources().getString(R.string.title_home))){
                    title.setText(getResources().getString(R.string.title_home));
                    showFragment(finalHomeFragment);
                    //menu.setOnClickListener(HomeFragment.menuOnclick);
                    //Log.e("导航栏1", String.valueOf(id));
                }
                else if (menuItem.getTitle().toString().equals(getResources().getString(R.string.title_dashboard))){
                    title.setText(getResources().getString(R.string.title_dashboard));
                    showFragment(dashboardFragment);
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
        View drawer=findViewById(R.id.drawer_page);
        LinearLayout change_color_container=drawer.findViewById(R.id.color_change_container);
        //将颜色按钮填充进布局
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        //ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.activity_bottom_navigation, null);
        FrameLayout parent2 = (FrameLayout) rightLowerButton.getParent();
        //ConstraintLayout parent1 = (ConstraintLayout) ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
        parent2.removeView(rightLowerButton);
        change_color_container.addView(rightLowerButton);
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
        TextView workspace=drawer.findViewById(R.id.worksapce);
        TextView position=drawer.findViewById(R.id.position);
        TextView contactway=drawer.findViewById(R.id.contact_way);
        TextView personaldes=drawer.findViewById(R.id.personal_des);
        GetPhoneInfo();
        if (!model.equals("")){
            phonetype.setText(model);
        }
        /*
        if (!mac.equals("")){
            macaddress.setText(mac);
        }

         */

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
        this.sdkNum = android.os.Build.VERSION.SDK; // SDK号
        this.model = android.os.Build.MODEL; // 手机型号
        this.release = android.os.Build.VERSION.RELEASE; // android系统版本号
        this.mac = getNewMac();
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
}
