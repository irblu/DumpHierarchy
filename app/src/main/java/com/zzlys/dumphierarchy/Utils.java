package com.zzlys.dumphierarchy;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Created by ziliang.z on 2017/3/2.
 */

public class Utils {

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName
     *            是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    public static int[] formatPath2Ints(String path) {
        String[] strs = path.split(":");
        int size = strs.length;
        int[] rsl = new int[size];
        for(int i = 0; i<size; i++) {
            rsl[i] = Integer.parseInt(strs[size - i - 1]) - 1;
        }
        return rsl;
    }

    public static String formatPath2String(String path) {
        String[] strs = path.split(":");
        int size = strs.length;
        String  rsl = "";
        for(int i = 0; i<size; i++) {
            rsl += (Integer.parseInt(strs[size - i - 1]) - 1) + "." ;
        }
        return rsl;
    }

}
