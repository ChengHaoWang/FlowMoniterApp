package com.example.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForgetPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏actionbar
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_forget_password);
        setStatusBarColor(ForgetPassword.this);

        LinearLayout goback=findViewById(R.id.goback);
        LinearLayout confirm_forgetpassword=findViewById(R.id.confirm_forgetpassword);
        final EditText username=findViewById(R.id.username);
        final EditText answer=findViewById(R.id.answer_forgetpassword);
        final String [] question_array=getResources().getStringArray(R.array.question);
        final AppCompatSpinner question=findViewById(R.id.chose_question);
        final int[] question_id = {0};
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
        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ForgetPassword.this,MainActivity.class);
                startActivity(intent);
            }
        });
        confirm_forgetpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (answer.getText().toString().trim().equals("")){
                    AlertDialog dialog=new AlertDialog.Builder(ForgetPassword.this).setMessage("请输入答案").create();
                    dialog.show();
                }
                else {
                    final String username_text=username.getText().toString();
                    final String answer_text=answer.getText().toString();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            RequestBody requestBody = new FormBody.Builder()
                                    .add("username",username_text)
                                    .add("question",question_array[question_id[0]])
                                    .add("answer",answer_text)
                                    .build();
                            String url = getResources().getString(R.string.ip)+getResources().getString(R.string.forgetpassword);
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
                                    Toast.makeText(ForgetPassword.this,"修改失败，请检查问题答案",Toast.LENGTH_SHORT).show();
                                    //AlertDialog dialog=new AlertDialog.Builder(ForgetPassword.this).setMessage("修改失败，请检查问题答案").create();
                                    //dialog.show();
                                }
                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String respose_text = response.body().string();
                                    //Log.e("网络请求","请求成功");
                                    JSONObject jsonObject = null;
                                    try {
                                        jsonObject = new JSONObject(respose_text);
                                        final String result = jsonObject.optString("result", null);
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ForgetPassword.this,result,Toast.LENGTH_SHORT).show();
                                                //AlertDialog dialog=new AlertDialog.Builder(ForgetPassword.this).setMessage(result).create();
                                                //dialog.show();
                                            }
                                        });
                                        Intent intent=new Intent(ForgetPassword.this,MainActivity.class);
                                        //startActivity(intent);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                        }
                    }).start();
                }
            }
        });
    }
    public void setStatusBarColor(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            //取消设置Window半透明的Flag
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //添加Flag把状态栏设为可绘制模式
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏为蓝色
            //window.setStatusBarColor(Color.parseColor("#5CACEE"));
            window.setStatusBarColor(getResources().getColor(R.color.actionbar));
        }
    }
}
