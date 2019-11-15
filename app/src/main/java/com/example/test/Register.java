package com.example.test;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

import static com.example.test.MainActivity.JSON;

public class Register extends AppCompatActivity {
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏actionbar
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_register);

        setStatusBarColor(Register.this);
        LinearLayout goback=findViewById(R.id.goback);
        LinearLayout register=findViewById(R.id.register);

        //返回按钮
        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(Register.this,MainActivity.class);
                startActivity(intent);
            }
        });
        //注册按钮
        /*
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
         */
        //获取输入内容
        final EditText phone_number=findViewById(R.id.phone_number);
        final EditText password=findViewById(R.id.password);
        final EditText confirm_password=findViewById(R.id.confirm_password);
        final EditText username=findViewById(R.id.username);
        final EditText job=findViewById(R.id.job);
        AppCompatSpinner question=findViewById(R.id.chose_question);
        final int[] question_id = {0};
        final EditText answer=findViewById(R.id.answer);

        //选择问题监听
        question.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                question_id[0] =i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //注册按钮监听
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String phnoe_number_text=phone_number.getText().toString().trim();
                final String password_text=password.getText().toString().trim();
                final String confirm_password_text=confirm_password.getText().toString().trim();
                final String username_text=username.getText().toString().trim();
                final String job_text=job.getText().toString().trim();
                final String [] question_array=getResources().getStringArray(R.array.question);
                final String question_text=question_array[question_id[0]];
                final String answer_text=answer.getText().toString().trim();

                if (!phnoe_number_text.equals("")&&!password_text.equals("")&&!confirm_password_text.equals("")&&!username_text.equals("")&&!job_text.equals("")
                        &&!question_text.equals("")&&!answer_text.equals("")){
                    if (phnoe_number_text.length()!=11){

                        AlertDialog dialog=new AlertDialog.Builder(Register.this).setMessage("请输入正确的手机号码").create();
                        dialog.show();
                    }
                    else if (!password_text.equals(confirm_password_text)){
                        AlertDialog dialog=new AlertDialog.Builder(Register.this).setMessage("密码不一致请重新输入").create();
                        dialog.show();
                    }
                    else {
                        //request请求
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String model = android.os.Build.MODEL; // 手机型号
                                //this.release = android.os.Build.VERSION.RELEASE; // android系统版本号
                                String mac = getNewMac();
                                RequestBody requestBody = new FormBody.Builder()
                                        .add("username",phnoe_number_text)
                                        .add("password",password_text)
                                        .add("nickname",username_text)
                                        .add("company","")
                                        .add("duty",job_text)
                                        .add("question",question_text)
                                        .add("answer",answer_text)
                                        .add("signature","")
                                        .add("email","")
                                        .add("headimg","")
                                        .add("macaddress",mac)
                                        .add("model",model)
                                        .build();
                                String url = getResources().getString(R.string.ip)+getResources().getString(R.string.register);
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
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(Register.this,"注册失败",Toast.LENGTH_SHORT).show();
                                                //AlertDialog dialog=new AlertDialog.Builder(Register.this).setMessage("注册失败").create();
                                                //dialog.show();
                                            }
                                        });

                                    }
                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String result = response.body().string();
                                        Log.e("网络请求","请求成功");
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(Register.this,"注册成功",Toast.LENGTH_SHORT).show();
                                                //AlertDialog dialog=new AlertDialog.Builder(Register.this).setMessage("注册成功").create();
                                                //dialog.show();
                                            }
                                        });
                                        finish();
                                        //Intent intent=new Intent(Register.this,MainActivity.class);
                                        //startActivity(intent);
                                    }
                                });
                            }
                        }).start();

                    }
                }
            }
        });
    }
    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.M)
    public void setStatusBarColor(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            //取消设置Window半透明的Flag
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //添加Flag把状态栏设为可绘制模式
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏为蓝色
            window.setStatusBarColor(Color.TRANSPARENT);
            //window.setStatusBarColor(getResources().getColor(R.color.white));
        }
    }
}
