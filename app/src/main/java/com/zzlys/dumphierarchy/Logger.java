package com.zzlys.dumphierarchy;

import android.util.Log;

/**
 * Created by ziliang.z on 2017/4/10.
 */

public class Logger {
    public static void d(String tag, String str) {
        Log.d("DumpHierarchy$" + tag, str);
    }
}
