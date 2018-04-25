package com.zzlys.dumphierarchy.presenter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.zzlys.dumphierarchy.Constants;
import com.zzlys.dumphierarchy.R;
import com.zzlys.dumphierarchy.view.FileListActivity;
import com.zzlys.dumphierarchy.view.HierarchyViewerActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zzlys.dumphierarchy.Constants.EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE;
import static com.zzlys.dumphierarchy.Constants.FILE_PATH;

/**
 * Created by ziliang.z on 2017/4/11.
 */

public class FileListActivityPresenter {
    private static final String TAG= "FileListActivityPresenter";
    private FileListActivity _activity = null;
    private String _nowPath = Constants.DUMP_FOLDER_DIR;

    private FileListActivityPresenter(){}

    public FileListActivityPresenter(FileListActivity activity) {
        _activity = activity;
        requestPermission();
        loadList();
    }

    public void requestPermission() {
        if(Build.VERSION.SDK_INT > 23 ) {
            try {
                int hasWriteExternalStoragePermission = _activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);//权限检查
                if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                    _activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(_activity, "Permission error.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //if return true, super.onBackPressed will called, otherwise not.
    public boolean onBackPressed() {
        if(_nowPath.equals(Constants.DUMP_FOLDER_DIR)) {
            return true;
        } else {
            //go up diretory
            _nowPath = _nowPath.substring(0,_nowPath.lastIndexOf(File.separator));
            loadList();
            return false;
        }
    }

    public void loadList() {
        _activity.setListAdapter(getItemsAdapter(_nowPath));
    }

    public SimpleAdapter getItemsAdapter(String filepath) {
        File file = new File(filepath);
        if(file != null && file.exists() && file.isDirectory()) {
            String [] fileList = getUixFileNames(file);
            String [] subDirs = getSubDirNames(file);
            if(subDirs==null || fileList==null) {
                return null;
            }
            List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
            if(!filepath.equals(Constants.DUMP_FOLDER_DIR)) {
                Map<String, Object> item = new HashMap<String, Object>();
                item.put(Constants.FILE_IMG, R.drawable.ic_back);
                item.put(Constants.FILE_NAME, ". . ." + filepath.substring(filepath.lastIndexOf(File.separator)));
                listItems.add(item);
            }
            for(String s:subDirs) {
                //Logger.d(TAG, s);
                Map<String, Object> item = new HashMap<String, Object>();
                item.put(Constants.FILE_IMG, R.drawable.ic_folder);
                item.put(Constants.FILE_NAME, s);
                listItems.add(item);
            }
            for(String s:fileList) {
                //Logger.d(TAG, s);
                Map<String, Object> item = new HashMap<String, Object>();
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 16;
                Bitmap bmp = BitmapFactory.decodeFile(filepath + File.separator + s.replace(".uix", ".png"), opts);
                if(bmp != null) {
                    item.put(Constants.FILE_IMG, bmp);
                } else {
                    item.put(Constants.FILE_IMG, R.drawable.ic_img_error);
                }
                item.put(Constants.FILE_NAME, s);
                listItems.add(item);
            }
            SimpleAdapter simpleAdapter = new SimpleAdapter(_activity, listItems, R.layout.list_item,
                    new String[] {Constants.FILE_IMG, Constants.FILE_NAME}, new int[] {R.id.fileImg, R.id.fileName});
            simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if(view instanceof ImageView && data instanceof Bitmap) {
                        ImageView iv = (ImageView) view;
                        iv.setImageBitmap((Bitmap) data);
                        return true;
                    }
                    return false;
                }
            });
            return simpleAdapter;
        }
        return null;
    }
    String [] getUixFileNames(File file) {
        String [] jsonList = null;
        if(file != null && file.exists() && file.isDirectory()) {
            jsonList = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    File f = new File(dir.getPath() + File.separator + name);
                    if (f != null && f.exists() && f.isFile() && name.endsWith(".uix"))
                        return true;
                    return false;
                }
            });
        }
        return jsonList;
    }
    String [] getSubDirNames(File file) {
        String [] subDirs = null;
        if(file != null && file.exists() && file.isDirectory()) {
            subDirs = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    File f = new File(dir.getPath() + File.separator + name);
                    if (f != null && f.exists() && f.isDirectory())
                        return true;
                    return false;
                }
            });
        }
        return subDirs;
    }

    public void onListItemClicked(ListView l, View v, int position, long id){
        if(!_nowPath.equals(Constants.DUMP_FOLDER_DIR))
            id--;
        if(id == -1) {
            //go up diretory
            _nowPath = _nowPath.substring(0,_nowPath.lastIndexOf(File.separator));
            loadList();
            return;
        }
        File file = new File(_nowPath);
        String [] subDirs = getSubDirNames(file);
        String [] uixList = getUixFileNames(file);
        if(id < subDirs.length) {
            //directory clicked
            _nowPath = _nowPath + File.separator + subDirs[(int)id];
            loadList();
            return;
        }else{
            id -= subDirs.length;
            if(id < uixList.length) {
                //json file clicked
                String filePath = _nowPath + File.separator + uixList[(int)id];
                //runScriptFile(filePath);
                Intent it = new Intent();
                it.putExtra(FILE_PATH, filePath);
                it.setClass(_activity, HierarchyViewerActivity.class);
                _activity.startActivity(it);
            }
        }
        return;
    }
}
