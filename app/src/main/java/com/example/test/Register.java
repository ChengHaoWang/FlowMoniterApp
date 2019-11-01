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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class Register extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏actionbar
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_register);

        setStatusBarColor(Register.this);
        ImageView goback=findViewById(R.id.goback);
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
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
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
                String phnoe_number_text=phone_number.getText().toString().trim();
                String password_text=password.getText().toString().trim();
                String confirm_password_text=confirm_password.getText().toString().trim();
                String username_text=username.getText().toString().trim();
                String job_text=job.getText().toString().trim();
                String [] question_array=getResources().getStringArray(R.array.question);
                String question_text=question_array[question_id[0]];
                String answer_text=answer.getText().toString().trim();

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

                        AlertDialog dialog=new AlertDialog.Builder(Register.this).setMessage("注册成功").create();
                        dialog.show();
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
