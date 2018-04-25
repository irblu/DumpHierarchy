package com.zzlys.dumphierarchy.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.johnkil.print.PrintView;
import com.zzlys.dumphierarchy.Constants;
import com.zzlys.dumphierarchy.Logger;
import com.zzlys.dumphierarchy.R;
import com.zzlys.dumphierarchy.view.FileListActivity;

import java.util.ArrayList;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 123;
    Button _btnToggle;
    Button _btnBrowser;
    EditText _edtDelayTimeInput;
    PrintView _btnInfo;
    ToggleButton _tgbShowActivityToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _btnToggle = (Button)findViewById(R.id.button_service_toggle);
        _btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
        _btnBrowser = (Button)findViewById(R.id.button_browser_files);
        _btnBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, FileListActivity.class);
                startActivity(intent);
            }
        });

        _edtDelayTimeInput = (EditText) findViewById(R.id.delay_time_input);
        _edtDelayTimeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(TextUtils.isEmpty(s))
                    return;
                SharedPreferences sp = getSharedPreferences(Constants.DELAY_TIME_MS, Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                edit.putInt(Constants.DELAY_TIME_MS, Integer.parseInt(s.toString()));
                edit.commit();
                Logger.d(TAG, "delay time edited:" + s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        SharedPreferences spTime = getSharedPreferences(Constants.DELAY_TIME_MS, Context.MODE_PRIVATE);
        int time = spTime.getInt(Constants.DELAY_TIME_MS, 2000);
        _edtDelayTimeInput.setText("" + time);

        _btnInfo = (PrintView) findViewById(R.id.info);
        _btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), getString(R.string.DELAY_TIME_HELP_INFO), Toast.LENGTH_LONG).show();
            }
        });

        _tgbShowActivityToast = (ToggleButton) findViewById(R.id.show_activity_toast);
        _tgbShowActivityToast.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SharedPreferences sp = getSharedPreferences(Constants.SHOW_ACTIVITY_TOAST, Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                edit.putBoolean(Constants.SHOW_ACTIVITY_TOAST, isChecked);
                edit.commit();
                Logger.d(TAG, "show activity toast: " + isChecked);

                Intent it = new Intent();
                it.setAction(Constants.SHOW_ACTIVITY_TOAST);
                it.putExtra(Constants.SHOW_ACTIVITY_TOAST, isChecked);
                sendBroadcast(it);
            }
        });
        SharedPreferences spShowActivityToast = getSharedPreferences(Constants.SHOW_ACTIVITY_TOAST, Context.MODE_PRIVATE);
        boolean showActivityToast = spShowActivityToast.getBoolean(Constants.SHOW_ACTIVITY_TOAST, false);
        _tgbShowActivityToast.setChecked(showActivityToast);


        //request external storage write permission
        if(Build.VERSION.SDK_INT >= 23 ) {
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), getString(R.string.PLEASE_PERMIT_DRAWING_OVER_OTHER_APPS), Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
            }
            try {
                int hasWriteExternalStoragePermission = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);//权限检查
                if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), getString(R.string.PERMISSION_ERROR), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.PERMISSION_CHECK_FAILED_PLEASE_CHECK_WRITE_EXTERNAL_STORAGE_PERMISSION), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
