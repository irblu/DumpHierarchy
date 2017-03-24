package com.zzlys.dumphierarchy.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Toast;


import com.zzlys.dumphierarchy.ScreenShotListenManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * Created by ziliang.z on 2017/3/2.
 */

public class DumpService extends AccessibilityService {
    private static final String TAG = "DumpHierarchy";
    Handler _handler;
    String _outputString;
    ScreenShotListenManager _screenShotListener;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("DumpEvent", event.toString());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                dump(true);
                return false;
            }
        });
        _screenShotListener = ScreenShotListenManager.newInstance(getApplicationContext());

        _screenShotListener.setListener(
            new ScreenShotListenManager.OnScreenShotListener() {
            public void onShot(String imagePath) {
                Log.d(TAG, "Screen Capture detected! Dump UI Hierarchy in 1s");
                _handler.sendEmptyMessageDelayed(1,1000);
            }
        }
        );

        _screenShotListener.startListen();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _screenShotListener.stopListen();
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
            Toast.makeText(getApplicationContext(),"Get root view failed, cannot dump UI hierarchy...", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getApplicationContext(),"Dumping UI Hierarchy...", Toast.LENGTH_SHORT).show();
        dumpHierarchy(root);
        if (dump2file && root!= null) {
            logNode2Xml(root);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddhhmmss");
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String time = formatter.format(curDate);
            String dir = Environment.getExternalStorageDirectory() + "/DumpHierarchy";
            File file = new File(dir);
            if(!file.exists()) {
                file.mkdir();
            }
            String name = file.getPath() + "/dump_" + time;
            write2file(name + ".uix", _outputString);
            copyLastestScreenshot(name + ".png");
        }
        Toast.makeText(getApplicationContext(),"Dump UI Hierarchy completed.", Toast.LENGTH_SHORT).show();
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
                + " text=\"" + node.getText()
                + "\" resource-id=\"" + node.getViewIdResourceName()
                + "\" class=\"" + node.getClassName()
                + "\" package=\"" + node.getPackageName()
                + "\" content-desc=\"" + node.getContentDescription()
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
        String PATH = Environment.getExternalStorageDirectory() + "/";
        File screenshotDir = new File(PATH + "/DCIM/Screenshots");
        if (!screenshotDir.exists()) {
            Toast.makeText(getApplicationContext(), "未找到/DCIM/Screenshots文件夹", Toast.LENGTH_SHORT).show();
            return;
        }
        final File[] files = screenshotDir.listFiles();
        File lastFile = null;
        for(File file : files) {
            lastFile = file;
        }
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
}
