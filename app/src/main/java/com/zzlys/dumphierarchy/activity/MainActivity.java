package com.zzlys.dumphierarchy.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zzlys.dumphierarchy.R;
import com.zzlys.dumphierarchy.Utils;
import com.zzlys.dumphierarchy.service.MainService;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 123;
    Button _btnToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _btnToggle = (Button)findViewById(R.id.button_service_toggle);
        final Intent it = new Intent(MainActivity.this, MainService.class);
        _btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Utils.isServiceWork(getApplicationContext(), "com.zzlys.dumphierarchy.service.MainService")) {
                    Log.d(TAG, "start service");
                    startService(it);

                } else {
//                    Log.d(TAG, "stop service");
//                    stopService(it);
//                    _btnToggle.setText("START");
                }
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
//        if(Utils.isServiceWork(getApplicationContext(), "com.zzlys.dumphierarchy.service.MainService"))
//            _btnToggle.setText("STOP");

        //request external storage write permission
        try {
            int hasWriteExternalStoragePermission =this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);//权限检查
            if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "权限异常", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(getApplicationContext(), "获取写权限失败，将无法保存dump文件供UIAUTOMATOR使用.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
