package com.example.test;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import me.nereo.multi_image_selector.MultiImageSelector;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditInfo extends AppCompatActivity {

    private static final int REQUEST_IMAGE = 2;
    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    protected static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;
    private ArrayList<String> mSelectPath;
    private SharedPreferences sp;
    //控件
    private EditText sign;
    private EditText nickname;
    private Spinner sex;
    private EditText company;
    private EditText duty;
    private EditText email;
    private ImageView head_image_upload;
    private final int[] sex_id = {0};
    private String [] sex_array;
    //初始变量
    private String isign="";
    private String inickname="";
    private String isex="";
    private String icompany="";
    private String iduty="";
    private String iemail="";
    private String iheadsrc="";

    private int picChangeOrNot=0;//判断头像是否发生变化
    private Bitmap headbitmap;
    private File f;

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

    /** 保存方法 */
    public void saveBitmap(Bitmap bmp) {
        //Log.e("保存图片", "保存图片");
        //生成路径
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        String dirName = "erweima16";
        File appDir = new File(root , dirName);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        f = new File(appDir, "head.jpg");
        Log.i("图片路径", f.getPath().toString());
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i("保存图片", "已经保存");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
                //Intent intent =new Intent(EditInfo.this,BottomNavigation.class);
                //startActivity(intent);
                finish();
            }
        });
        //初始化界面
        sex_array=getResources().getStringArray(R.array.sex);
        sp = getSharedPreferences("userInfo", 0);
        final String username=sp.getString("username", "");

        head_image_upload=findViewById(R.id.head_image_upload);
        sign=findViewById(R.id.sign);
        nickname=findViewById(R.id.nickname);
        sex=findViewById(R.id.sex);
        company=findViewById(R.id.company);
        duty=findViewById(R.id.duty);
        email=findViewById(R.id.email);
        sex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sex_id[0] =i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //String ipicsrc=head_image_upload.get

        //初始化请求
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
                            isign=jsonObject2.optString("signature",null);
                            inickname=jsonObject2.optString("nickname",null);
                            isex=jsonObject2.optString("sex",null);
                            //icompany=jsonObject2.optString("company",null);
                            iduty=jsonObject2.optString("duty",null);
                            iemail=jsonObject2.optString("email",null);
                            iheadsrc=jsonObject2.optString("headimg",null);

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    //head_image_upload=findViewById(R.id.head_image_upload);
                                    sign.setText(isign);
                                    nickname.setText(inickname);
                                    if (isex.equals("男")){
                                        sex.setSelection(0);
                                    }else {
                                        sex.setSelection(1);
                                    }
                                    company.setText(icompany);
                                    duty.setText(iduty);
                                    email.setText(iemail);
                                }
                            });


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        }).start();

        //图片选择
        checkPermision();//检查权限

        PhotoOrCropUtil.getInstance().setContext(this);
        PhotoOrCropUtil.getInstance().setPhotoOrCropListener(new PhotoOrCropUtil.PhotoOrCropListener() {
            @Override
            public void uploadAvatar(Bitmap bitmap) {
                head_image_upload.setImageBitmap(bitmap);
                picChangeOrNot=1;
                headbitmap=bitmap;
                saveBitmap(bitmap);
            }
        });
        head_image_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhotoOrCropUtil.getInstance().gallery();
            }
        });
        //传输数据

        LinearLayout confirm_edit_info=findViewById(R.id.confirm_edit_info);
        confirm_edit_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //网络请求
                final String psign=sign.getText().toString();
                final String pnickname=nickname.getText().toString();
                final String psex=sex_array[sex_id[0]].toString();
                final String pcompany=company.getText().toString();
                final String pduty=duty.getText().toString();
                final String pemail=email.getText().toString();
                if (!psign.equals(isign)||!pnickname.equals(inickname)||!psex.equals(isex)||!pcompany.equals(icompany)||!pduty.equals(iduty)||!pemail.equals(iemail)||picChangeOrNot==1){

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            MediaType type=MediaType.parse("image/jpeg");
                            File file=new File(f.getPath());
                            RequestBody fileBody=RequestBody.create(file,type);

                            RequestBody multipartBody = new MultipartBody.Builder()
                                    .setType(MultipartBody.ALTERNATIVE)
                                    .addFormDataPart("username",username)
                                    .addFormDataPart("password","")
                                    .addFormDataPart("question","")
                                    .addFormDataPart("answer","")
                                    .addFormDataPart("signature",psign)
                                    .addFormDataPart("nickname",pnickname)
                                    .addFormDataPart("sex",psex)
                                    .addFormDataPart("company",pcompany)
                                    .addFormDataPart("duty",pduty)
                                    .addFormDataPart("email",pemail)
                                    .addFormDataPart("headimg",f.getName(),fileBody)
                                    .build();
                            String url = getResources().getString(R.string.ip)+getResources().getString(R.string.editinfo);
                            OkHttpClient okHttpClient = new OkHttpClient();
                            final Request request = new Request.Builder()
                                    .url(url)
                                    .post(multipartBody)
                                    .build();
                            Call call = okHttpClient.newCall(request);
                            call.enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Log.e("网络请求","请求失败");
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            //android.app.AlertDialog dialog=new android.app.AlertDialog.Builder(Register.this).setMessage("注册失败").create();
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
                                            //android.app.AlertDialog dialog=new android.app.AlertDialog.Builder(Register.this).setMessage("注册成功").create();
                                            //dialog.show();
                                        }
                                    });

                                    //finish();
                                    //Intent intent=new Intent(Register.this,MainActivity.class);
                                    //startActivity(intent);
                                }
                            });
                        }
                    }).start();

                }
            }
        });
    }
}
