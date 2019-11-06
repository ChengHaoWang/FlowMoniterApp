package com.example.test;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import static com.example.test.ApkTool.REQUEST_READ_PHONE_STATE;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 3;
    public final static int REQUEST_ACTION_USAGE_ACCESS_SETTINGS = 1;
    private SharedPreferences sp;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏actionbar
        //getSupportActionBar().hide();
        //定义全屏参数
        int flag=WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //获得当前窗体对象
        Window window=MainActivity.this.getWindow();
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);
        setContentView(R.layout.activity_main);
        //获取权限
        //hasPermissionToReadNetworkStats();
        //动态申请权限
        int permissionCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            //TODO
        }
        if (!hasEnablePermission(this)) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            //ActivityCompat.requestPermissions(this, new String[]{Settings.ACTION_USAGE_ACCESS_SETTINGS}, REQUEST_ACTION_USAGE_ACCESS_SETTINGS);
        }
        //获取
        final EditText account=findViewById(R.id.accountNumber);
        final EditText password=findViewById(R.id.password);
        final ImageView account_clear=findViewById(R.id.account_clear);
        final ImageView password_clear=findViewById(R.id.password_clear);
        final ImageView password_visible=findViewById(R.id.password_visible);
        final LinearLayout confirm=findViewById(R.id.confirm);
        TextView forget_password=findViewById(R.id.forgetpassword);
        TextView register=findViewById(R.id.register);
        TextView service_text=findViewById(R.id.service_text);

        //添加事件
        account.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                account_clear.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                account_clear.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (account.length()!=0){
                    account_clear.setVisibility(View.VISIBLE);
                }
                else {
                    account_clear.setVisibility(View.INVISIBLE);
                }
                if (!account.getText().toString().trim().equals("")&&!password.getText().toString().trim().equals("")){
                    confirm.setBackgroundResource(R.drawable.shape_back_orange);
                    confirm.setTag("clickable");
                }
                else {
                    confirm.setBackgroundResource(R.drawable.shape_back_orange_unclick);
                    confirm.setTag("unclickable");
                }
            }
        });
        account_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                account.setText("");
                account_clear.setVisibility(View.INVISIBLE);
            }
        });

        //添加事件
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                password_clear.setVisibility(View.INVISIBLE);
                password_visible.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                password_clear.setVisibility(View.VISIBLE);
                password_visible.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (password.length()!=0){
                    password_clear.setVisibility(View.VISIBLE);
                    password_visible.setVisibility(View.VISIBLE);
                }
                else {
                    password_clear.setVisibility(View.INVISIBLE);
                    password_visible.setVisibility(View.INVISIBLE);
                }
                if (!account.getText().toString().trim().equals("")&&!password.getText().toString().trim().equals("")){
                    confirm.setBackgroundResource(R.drawable.shape_back_orange);
                    confirm.setTag("clickable");
                }
                else {
                    confirm.setBackgroundResource(R.drawable.shape_back_orange_unclick);
                    confirm.setTag("unclickable");
                }
            }
        });

        password_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password.setText("");
                password_clear.setVisibility(View.INVISIBLE);
            }
        });
        password_visible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (password_visible.getVisibility()==View.VISIBLE){
                    if (password_visible.getTag().equals("select")){
                        password_visible.setTag("unSelect");
                        password_visible.setImageResource(R.drawable.eye_open);
                        password.setInputType(128);//显示
                    }
                    else {
                        password_visible.setTag("select");
                        password_visible.setImageResource(R.drawable.eye_close);
                        password.setInputType(129);//隐藏
                    }
                }
            }
        });

        //登录点击事件
        sp = getSharedPreferences("userInfo", 0);
        String name=sp.getString("USER_NAME", "");
        String pass =sp.getString("PASSWORD", "");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //向服务器发送账号密码数据，验证登录请求
                if (confirm.getTag().equals("clickable")){
                    String accout_text=account.getText().toString();
                    String password_text=password.getText().toString();
                    //request

                    //Intent intent=new Intent(MainActivity.this,MainInterface.class);
                    //startActivity(intent);
                    SharedPreferences.Editor editor =sp.edit();
                    editor.putString("USER_NAME", accout_text);
                    editor.putString("PASSWORD", password_text);
                    editor.commit();
                }

                //测试用，测完删
                Intent intent=new Intent(MainActivity.this,BottomNavigation.class);
                startActivity(intent);

            }
        });
        //找回密码事件
        forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //向服务器发送请求，一般用手机验证码登录，挺麻烦的
                Intent findpassword=new Intent(MainActivity.this,ForgetPassword.class);
                startActivity(findpassword);
            }
        });
        //用户注册事件
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转
                Intent register=new Intent(MainActivity.this,Register.class);
                startActivity(register);
            }
        });
        //服务条款点击事件
        service_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent userprotocol=new Intent(MainActivity.this,UserProtocol.class);
                startActivity(userprotocol);
            }
        });
    }


    private boolean hasPermissionToReadNetworkStats() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        final AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }

        requestReadNetworkStats();
        return false;
    }
    // 打开“有权查看使用情况的应用”页面
    private void requestReadNetworkStats() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }
    //重写该方法处理用户对申请权限的返回结果
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                    Log.e("Steven", "权限申请成功");
                }
                else {
                    Log.e("Steven", "权限申请失败");
                }
                break;

            default:
                break;
        }
    }
/**
 *  判断当前应用是否有查看应用使用情况的权限（针对于android5.0以上的系统）
 * @return
 */
    @SuppressLint("NewApi")
    public static  boolean hasEnablePermission(Context context){

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){   // 如果大于等于5.0 再做判断
            long ts = System.currentTimeMillis();
            UsageStatsManager usageStatsManager=(UsageStatsManager)context.getSystemService(Service.USAGE_STATS_SERVICE);
            List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, 0, ts);
            if (queryUsageStats == null || queryUsageStats.isEmpty()) {
                return false;
            }
        }
        return true;
    }


}
