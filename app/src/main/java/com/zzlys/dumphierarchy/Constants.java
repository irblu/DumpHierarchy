package com.zzlys.dumphierarchy;

import android.os.Environment;

import java.io.File;

/**
 * Created by ziliang.z on 2017/4/11.
 */

public class Constants {
    public static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 946;

    public static final String DUMP_FOLDER_DIR = Environment.getExternalStorageDirectory() + File.separator + "DumpHierarchy";

    public static final String FILE_IMG = "file_img";
    public static final String FILE_NAME = "file_name";
    public static final String FILE_DESC = "file_desc";


    public static final String FILE_PATH = "file_path";

    public static final String DELAY_TIME_MS = "delayTimeMS";
    public static final String SHOW_ACTIVITY_TOAST = "showActivityToast";

}
