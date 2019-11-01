package com.example.test;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ForgetPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏actionbar
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_forget_password);
        setStatusBarColor(ForgetPassword.this);

        ImageView goback=findViewById(R.id.goback);
        LinearLayout confirm_forgetpassword=findViewById(R.id.confirm_forgetpassword);
        final EditText answer=findViewById(R.id.answer_forgetpassword);

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
                    String answer_text=answer.getText().toString();
                    //request
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
