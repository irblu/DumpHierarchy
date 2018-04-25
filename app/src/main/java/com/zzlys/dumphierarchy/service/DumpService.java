package com.zzlys.dumphierarchy.service;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;


import com.zzlys.dumphierarchy.Constants;
import com.zzlys.dumphierarchy.R;
import com.zzlys.dumphierarchy.ScreenShotListenManager;
import com.zzlys.dumphierarchy.view.FileListActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.app.Notification.FLAG_ONGOING_EVENT;
import static com.zzlys.dumphierarchy.Constants.DUMP_FOLDER_DIR;
import static com.zzlys.dumphierarchy.Constants.EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE;


/**
 * Created by ziliang.z on 2017/3/2.
 */

public class DumpService extends AccessibilityService {
    private static final String TAG = "DumpHierarchy";
    Handler _handler;
    String _outputString;
    ScreenShotListenManager _screenShotListener;

    private Notification _notification;
    private NotificationManager _notiManager;
    private WindowManager _windowManager;
    private TextView _textView;
    private BroadcastReceiver _receiver;
    private String _activityName = "UNKNOWN";
    private String _appName = "UNKNOWN";
    private String _versionCode = "";

    private static final int MSG_CHECK_PERMISSION = 81;
    private static final int MSG_SCREENSHOT_DETACTED = 346;
    private static final int MSG_CANCLE_MSG = 482;

    private boolean _showActivityToast = true;
    private String _screenshotPath = null;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("DumpEvent", event.toString());
        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null && event.getClassName() != null) {
                ComponentName componentName = new ComponentName(
                        event.getPackageName().toString(),
                        event.getClassName().toString()
                );

                ActivityInfo activityInfo = tryGetActivity(componentName);
                boolean isActivity = activityInfo != null;
                if (isActivity) {
                    if(_showActivityToast)
                        showMsg(componentName.flattenToShortString(), 3000);
                    _activityName = event.getClassName().toString();
                    _activityName = _activityName.substring(_activityName.lastIndexOf('.') + 1);
                    _appName = tryGetAppName(event.getPackageName().toString());
                    _versionCode = tryGetAppVersionCodeString(event.getPackageName().toString());
                }
            }
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private String tryGetAppName(String packageName) {
        try {
            return getPackageManager().getPackageInfo(packageName, 0).applicationInfo.loadLabel(getPackageManager()).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    private String tryGetAppVersionCodeString(String packageName) {
        try {
            return getPackageManager().getPackageInfo(packageName, 0).versionCode + "";
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_CHECK_PERMISSION:
                        if(!checkPermission() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            disableSelf();
                        break;
                    case MSG_SCREENSHOT_DETACTED:
                        dump(true);
                        break;
                    case MSG_CANCLE_MSG:
                        dismissMsg();
                        break;
                }
                return false;
            }
        });
        _screenShotListener = ScreenShotListenManager.newInstance(getApplicationContext());

        _screenShotListener.setListener(
            new ScreenShotListenManager.OnScreenShotListener() {
            public void onShot(String imagePath) {
                _screenshotPath = imagePath;
                SharedPreferences sp = getSharedPreferences(Constants.DELAY_TIME_MS, Context.MODE_PRIVATE);
                int delayTime = sp.getInt(Constants.DELAY_TIME_MS, 2000);
                String msg = String.format(getString(R.string.SCREEN_CAPTURE_DETECTED_DUMP_UI_HIERARCHY_IN), delayTime/1000.0);
                showMsg(msg);
                Log.d(TAG, "Screen Capture detected! Dump UI Hierarchy in " + delayTime/1000.0 + "s");
                _handler.sendEmptyMessageDelayed(MSG_SCREENSHOT_DETACTED,delayTime);
            }
        }
        );

        _screenShotListener.startListen();

        createNoti();
        _handler.sendEmptyMessageDelayed(MSG_CHECK_PERMISSION,100);

        _receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                _showActivityToast = intent.getBooleanExtra(Constants.SHOW_ACTIVITY_TOAST, false);
            }
        };
        registerReceiver(_receiver, new IntentFilter(Constants.SHOW_ACTIVITY_TOAST));
        SharedPreferences spShowActivityToast = getSharedPreferences(Constants.SHOW_ACTIVITY_TOAST, Context.MODE_PRIVATE);
        _showActivityToast = spShowActivityToast.getBoolean(Constants.SHOW_ACTIVITY_TOAST, false);

        _windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    private void createNoti() {
        _notiManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), FileListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),2,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        _notification= new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.DUMPHIERARCHY_RUNNING))
                .setContentText(getString(R.string.TAKE_A_SCREENSHOT_TO_CAPTURE_UI_HIERARCHY))
                .setSmallIcon(R.drawable.ic_stat_dh)
                .setContentIntent(pendingIntent)
                //.setLargeIcon(Icon.createWithResource(getApplicationContext(),R.drawable.ic_action_dh))
                .setOngoing(true)
                .build();
        _notiManager.notify(0,_notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _screenShotListener.stopListen();
        _notiManager.cancelAll();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");
        //收回下拉的状态栏
        Intent it = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        sendBroadcast(it);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dump(false);


        return super.onStartCommand(intent, flags, startId);
    }

    private void dump(boolean dump2file) {

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null)
            root = getRootInTopWindow();
        if (root == null) {
            String msg = getString(R.string.GET_ROOT_VIEW_FAILED_PLEASE_CHECK_YOUR_ACCESSIBILITY_SETTING);
            showMsg(msg, 2000);
            return;
        }
        String msg = "Dumping UI Hierarchy...";
        showMsg(msg);

        dumpHierarchy(root);
        if (dump2file && root!= null) {
            logNode2Xml(root);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String time = formatter.format(curDate);
            String dir = DUMP_FOLDER_DIR;
            File file = new File(dir);
            if(!file.exists()) {
                file.mkdir();
            }
            dir = dir + File.separator + _appName + _versionCode;
            file = new File(dir);
            if(!file.exists()) {
                file.mkdir();
            }
            String name = file.getPath() + File.separator + _activityName + "_" + time;
            write2file(name + ".uix", _outputString);
            copyLastestScreenshot(name + ".png");
        }

        msg = getString(R.string.DUMP_UI_HIERARCHY_COMPLETED);
        showMsg(msg, 2000);
    }

    @Override
    public void onInterrupt() {

    }

    public void dumpHierarchy(AccessibilityNodeInfo root) {
        Log.d(TAG, "================ dump hierarchy start ================");
        dumpHierarchy(root, "[0]");
        Log.d(TAG, "================ dump hierarchy end ================");
    }

    public void dumpHierarchy(AccessibilityNodeInfo root, String prefix) {
        if(root == null) {
            Log.d(TAG, prefix + " is null");
            return;
        }
        Log.d(TAG, prefix + logNodeInfo(root) + " childs(" + root.getChildCount() + ")");
        String spaces = "";
        for(int i = 0; i<prefix.length(); i++)
            spaces = spaces + " ";
        if (root.getChildCount() > 0) {
            Log.d(TAG, spaces + "{");
            for(int i = 0; i<root.getChildCount(); i++) {
                dumpHierarchy(root.getChild(i), prefix + "[" + i + "]");
                if (root.getChild(i) != null)
                    root.getChild(i).recycle();
            }
            Log.d(TAG, spaces + "}");
        }
    }

    private boolean DEBUG_WINDOW = true;
    public AccessibilityNodeInfo getRootInTopWindow() {
        Log.d(TAG, "getRootInTopWindow");
        AccessibilityNodeInfo topRoot = null;
        List<AccessibilityWindowInfo> windowInfoList = getWindows();
        int windowInfoSize = windowInfoList.size();
        if (windowInfoSize > 0) {
            Collections.reverse(windowInfoList);
            // find focus window
            for (int i = 0; i < windowInfoSize; i++) {
                AccessibilityWindowInfo windowInfo = windowInfoList.get(i);
                if (DEBUG_WINDOW) {
                    Log.d(TAG, "getRootInTopWindow, i: " + i + ", windowInfo: " + windowInfo);
                }
                if (windowInfo.getType() == AccessibilityWindowInfo.TYPE_APPLICATION
                        && windowInfo.isActive()) {
                    topRoot = windowInfo.getRoot();
                    if (topRoot == null) {
                        Log.d(TAG, "getRootInTopWindow, getRoot is null. i: " + i
                                + ", windowInfo: " + windowInfo);
                        break;
                    }
                    String pkgName = (String) topRoot.getPackageName();
                    if (DEBUG_WINDOW) {
                        Log.d(TAG, "getRootInTopWindow, active i: " + i
                                + ", pkgName: " + pkgName
                                + ", windowInfo: " + windowInfo);
                    }
                    if (i + i < windowInfoSize) {
                        for (int topIdx = i + 1; topIdx < windowInfoSize; topIdx++) {
                            AccessibilityWindowInfo topWindowInfo = windowInfoList.get(topIdx);
                            if (DEBUG_WINDOW) {
                                Log.d(TAG, "getRootInTopWindow, candi topIdx: " + topIdx
                                        + ", topWindowInfo: " + topWindowInfo);
                            }
                            if (topWindowInfo.getType() == AccessibilityWindowInfo.TYPE_APPLICATION) {
                                AccessibilityNodeInfo candiRoot = topWindowInfo.getRoot();
                                if (candiRoot == null) {
                                    Log.d(TAG, "getRootInTopWindow, candiRoot is null");
                                    continue;
                                }
                                if (pkgName.equals(candiRoot.getPackageName())) {
                                    topRoot = topWindowInfo.getRoot();
                                    if (DEBUG_WINDOW) {
                                        Log.d(TAG, "getRootInTopWindow, found topRoot: " + topRoot);
                                    }
                                }
                            } else {
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }

        if (DEBUG_WINDOW) {
            Log.d(TAG, "getRootInTopWindow, ret topRoot: " + topRoot);
        }
        return topRoot;
    }

    @Override
    public List<AccessibilityWindowInfo> getWindows() {
        List<AccessibilityWindowInfo> wInfoList = super.getWindows();
        Log.d(TAG, "getWindows, wInfoList size: " + wInfoList.size());

        if (DEBUG_WINDOW) {
            for (AccessibilityWindowInfo windowInfo : wInfoList) {
                Log.d(TAG, "getWindows, ret windowInfo: " + windowInfo);
            }
        }

        return wInfoList;
    }

    public String logNodeInfo(AccessibilityNodeInfo node) {
        String rsl = "";
        Rect rc = new Rect();
        node.getBoundsInScreen(rc);
        rsl = ""
                + " text=\"" + (TextUtils.isEmpty(node.getText())?"":node.getText().toString().replace("<","&lt;").replace(">","&gt;").replace("&","&amp;").replace("\"","&quot;").replace("\'","&apos;"))
                + "\" resource-id=\"" + (node.getViewIdResourceName()==null?"":node.getViewIdResourceName())
                + "\" class=\"" + (node.getClassName()==null?"":node.getClassName())
                + "\" package=\"" + (node.getPackageName()==null?"":node.getPackageName())
                + "\" content-desc=\"" + (node.getContentDescription()==null?"":node.getContentDescription().toString().replace("<","&lt;").replace(">","&gt;").replace("&","&amp;").replace("\"","&quot;").replace("\'","&apos;"))
                + "\" checkable=\"" + node.isCheckable()
                + "\" checked=\"" + node.isChecked()
                + "\" clickable=\"" + node.isClickable()
                + "\" enabled=\"" + node.isEnabled()
                + "\" focusable=\"" + node.isFocusable()
                + "\" focused=\"" + node.isFocused()
                + "\" scrollable=\"" + node.isScrollable()
                + "\" long-clickable=\"" + node.isLongClickable()
                + "\" password=\"" + node.isPassword()
                + "\" selected=\"" + node.isSelected()
                + "\" bounds=\"[" + rc.left + "," + rc.top + "][" + rc.right + "," + rc.bottom + "]\""
                + "";
        return rsl;
    }

    private void logNode2Xml(AccessibilityNodeInfo node) {
        _outputString = "<?xml version='1.0' encoding='utf-8' standalone='yes'?><hierarchy rotation=\"0\">";
        logNode2Xml(node, 0);
        _outputString += "</hierarchy>";
    }

    private void logNode2Xml(AccessibilityNodeInfo node, int index) {
        if (node == null)
            return;
        if (node.getChildCount()>0) {
            _outputString += "<node " + "index=\"" + index + "\"" +logNodeInfo(node) + ">";
            for(int i = 0; i<node.getChildCount(); i++) {
                logNode2Xml(node.getChild(i), i);
            }
            _outputString += "</node>";
        }else{
            _outputString += "<node " + "index=\"" + index + "\"" +logNodeInfo(node) + "/>";
        }
    }

    private void write2file(String filename, String content){
        try{

            File file = new File(filename);
            if(!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fout = new FileOutputStream(file);

            byte[] bytes = content.getBytes();

            fout.write(bytes);

            fout.close();
            Toast.makeText(getApplicationContext(),"file saved at " + filename, Toast.LENGTH_SHORT);
        }

        catch(Exception e){
            Log.d(TAG, e.toString());
        }
    }

    private void copyLastestScreenshot(String filename) {
        File lastFile = new File(_screenshotPath);
        if(lastFile == null)
            return;
        try {
            int bytesum = 0;
            int byteread = 0;
            String oldPath = lastFile.getPath();
            String newPath = filename;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        }
        catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }
    }

    public boolean checkPermission() {
        if(Build.VERSION.SDK_INT > 23 ) {
            try {
                int hasWriteExternalStoragePermission = getApplication().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);//权限检查
                if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), getString(R.string.PERMISSION_CHECK_FAILED_PLEASE_CHECK_WRITE_EXTERNAL_STORAGE_PERMISSION), Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Permission error.", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    public void showMsg(String msg, int time) {
        showMsg(msg);
        _handler.sendEmptyMessageDelayed(MSG_CANCLE_MSG, time);
    }

    public void showMsg(String msg) {
        dismissMsg();
        if (Build.VERSION.SDK_INT>=23 && Settings.canDrawOverlays(getApplicationContext())) {
            _textView = new TextView(getApplicationContext());
            _textView.setText(msg);
            _textView.setBackgroundColor(Color.YELLOW);
            _textView.setTextColor(Color.BLUE);

            WindowManager.LayoutParams mLP = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    // Allows the view to be on top of the StatusBar
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    // Keeps the button presses from going to the background window
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            // Enables the notification to recieve touch events
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            // Draws over status bar
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    , PixelFormat.TRANSLUCENT);

            mLP.gravity = Gravity.TOP | Gravity.CENTER;
            _windowManager.addView(_textView, mLP);
        } else {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    public void dismissMsg() {
        if(_textView != null) {
            _windowManager.removeView(_textView);
            _handler.removeMessages(MSG_CANCLE_MSG);
            _textView = null;
        }
    }

}
