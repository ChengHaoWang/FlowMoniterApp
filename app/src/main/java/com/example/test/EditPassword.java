package com.example.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
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

public class EditPassword extends AppCompatActivity {
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);
        LinearLayout goback=findViewById(R.id.goback);
        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                //Intent intent =new Intent(EditPassword.this,BottomNavigation.class);
                //startActivity(intent);
            }
        });

        sp = getSharedPreferences("userInfo", 0);
        final EditText old_password=findViewById(R.id.old_password);
        final EditText new_password=findViewById(R.id.new_password);
        final EditText confirm_new_password=findViewById(R.id.confirm_new_password);
        final LinearLayout confirm_edit_info=findViewById(R.id.confirm_edit_info);
        confirm_edit_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //非空
                if (!old_password.getText().toString().equals("")&&!new_password.getText().toString().equals("")&&!confirm_new_password.getText().toString().equals("")){
                    //判断新密码是否相等
                    if (!new_password.getText().toString().equals(confirm_new_password.getText().toString())){
                        Toast.makeText(EditPassword.this,"两次密码不匹配",Toast.LENGTH_SHORT).show();
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                final String username=sp.getString("username", "");
                                RequestBody requestBody = new FormBody.Builder()
                                        .add("username",username)
                                        .add("oldpassword",old_password.getText().toString())
                                        .add("newpassword",new_password.getText().toString())
                                        .build();

                                String url = getResources().getString(R.string.ip)+getResources().getString(R.string.editpassword);
                                OkHttpClient okHttpClient = new OkHttpClient();
                                final Request request = new Request.Builder()
                                        .url(url)
                                        .post(requestBody)
                                        .build();
                                Call call = okHttpClient.newCall(request);
                                call.enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(EditPassword.this,"网络无法连接",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        String respose_text = response.body().string();
                                        JSONObject jsonObject = null;
                                        try {
                                            jsonObject = new JSONObject(respose_text);
                                            final String result = jsonObject.optString("result", null);
                                            if (result.equals("success")){
                                                SharedPreferences.Editor editor =sp.edit();
                                                editor.putString("loginstatus","false");
                                                editor.commit();
                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(EditPassword.this,"修改成功",Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }else {
                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(EditPassword.this,result,Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                });
                            }
                        }).start();
                    }
                }else {
                    Toast.makeText(EditPassword.this,"输入不能为空",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
