package com.example.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class EditPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);
        ImageView goback=findViewById(R.id.goback);
        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(EditPassword.this,BottomNavigation.class);
                startActivity(intent);
            }
        });
        EditText old_password=findViewById(R.id.old_password);
        final EditText new_password=findViewById(R.id.new_password);
        final EditText confirm_new_password=findViewById(R.id.confirm_new_password);
        final LinearLayout confirm_edit_info=findViewById(R.id.confirm_edit_info);
        confirm_edit_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //网络请求，匹配原密码

                //判断新密码是否相等
                if (!new_password.getText().equals(confirm_new_password.getText())){
                    Toast.makeText(EditPassword.this,"两次密码不匹配",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
