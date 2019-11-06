package com.example.test;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import me.nereo.multi_image_selector.MultiImageSelector;

public class EditInfo extends AppCompatActivity {

    private static final int REQUEST_IMAGE = 2;
    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    protected static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;
    private ArrayList<String> mSelectPath;

    private void checkPermision() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.mis_permission_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        }else {
            /*
            boolean showCamera = true;
            MultiImageSelector selector = MultiImageSelector.create(EditInfo.this);
            selector.showCamera(showCamera);
            selector.count(1);
            selector.single();
            selector.origin(mSelectPath);
            selector.start(EditInfo.this, REQUEST_IMAGE);

             */
        }
    }
    private void requestPermission(final String permission, String rationale, final int requestCode){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){
            new AlertDialog.Builder(this)
                    .setTitle(R.string.mis_permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.mis_permission_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(EditInfo.this, new String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.mis_permission_dialog_cancel, null)
                    .create().show();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_STORAGE_READ_ACCESS_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(EditInfo.this,"权限赋予成功",Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PhotoOrCropUtil.getInstance().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);
        //解决Caused by: android.os.FileUriExposedException
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        //返回按钮
        ImageView goback=findViewById(R.id.goback);
        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(EditInfo.this,BottomNavigation.class);
                startActivity(intent);
            }
        });
        //图片选择
        checkPermision();//检查权限
        final ImageView head_image_upload=findViewById(R.id.head_image_upload);
        PhotoOrCropUtil.getInstance().setContext(this);
        PhotoOrCropUtil.getInstance().setPhotoOrCropListener(new PhotoOrCropUtil.PhotoOrCropListener() {
            @Override
            public void uploadAvatar(Bitmap bitmap) {
                head_image_upload.setImageBitmap(bitmap);
            }
        });
        head_image_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhotoOrCropUtil.getInstance().gallery();
            }
        });
        //传输数据
        EditText sign=findViewById(R.id.sign);
        EditText nickname=findViewById(R.id.nickname);
        Spinner sex=findViewById(R.id.sex);
        EditText company=findViewById(R.id.company);
        EditText duty=findViewById(R.id.duty);
        EditText email=findViewById(R.id.email);
        LinearLayout confirm_edit_info=findViewById(R.id.confirm_edit_info);
        confirm_edit_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //网络请求
            }
        });
    }
}
