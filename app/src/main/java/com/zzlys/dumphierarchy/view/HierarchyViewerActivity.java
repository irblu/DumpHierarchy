package com.zzlys.dumphierarchy.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.zzlys.dumphierarchy.R;
import com.zzlys.dumphierarchy.presenter.HierarchyViewerPresenter;

/**
 * Created by ziliang.z on 2017/4/10.
 */

public class HierarchyViewerActivity extends Activity {
    private static final String TAG = "HierarchyViewerActivity";
    private ImageView _screenshotImgView = null;
    private EditText _searchText = null;

    private HierarchyViewerPresenter _presenter = new HierarchyViewerPresenter();

    public HierarchyViewerPresenter getPresenter() {
        return _presenter;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPresenter().setActivity(this);
    }

    public Point setSceenshotImage(Bitmap bmp) {
        if(_screenshotImgView == null)
            _screenshotImgView = (ImageView) findViewById(R.id.screenshot_img);
        _screenshotImgView.setImageBitmap(bmp);
        return new Point(_screenshotImgView.getWidth(),_screenshotImgView.getHeight());
    }

    public void setOnSearchTextChangeListener(TextWatcher tw) {
        if(_searchText == null) {
            _searchText = (EditText) findViewById(R.id.search_text);
        }
        _searchText.addTextChangedListener(tw);
    }

    public Point getImageViewSize() {
        return new Point(_screenshotImgView.getWidth(),_screenshotImgView.getHeight());
    }

    public void setScreenshotOnTouchListener(View.OnTouchListener listener) {
        if(_screenshotImgView == null)
            _screenshotImgView = (ImageView) findViewById(R.id.screenshot_img);
        _screenshotImgView.setOnTouchListener(listener);
    }


}
