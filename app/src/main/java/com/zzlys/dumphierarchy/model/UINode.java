package com.zzlys.dumphierarchy.model;


import android.provider.Contacts;
import android.text.TextUtils;

import com.zzlys.dumphierarchy.Logger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by ziliang.z on 2017/4/10.
 */

public class UINode {
    private static final String TAG = "UINode";
    private List<UINode> mChildren = new ArrayList<>();
    private String mClassName = "";
    private String mPackageName = "";
    private int mIndex = 0;
    private String mText = "";
    private String mResourceId = "";
    private String mContentDesc = "";
    private boolean mCheckable = false;
    private boolean mChecked = false;
    private boolean mClickable = false;
    private boolean mEnabled = false;
    private boolean mFocusable = false;
    private boolean mFocused = false;
    private boolean mScrollable = false;
    private boolean mLongClickable = false;
    private boolean mPassword = false;
    private boolean mSelected = false;
    private int mBoundsLeft = 0;
    private int mBoundsTop = 0;
    private int mBoundsRight = 0;
    private int mBoundsBottom = 0;



    private static final String NODE = "node";
    private static final String HIERARCHY = "hierarchy";
    private static final String INDEX = "index";
    private static final String TEXT = "text";
    private static final String RESOURCE_ID = "resource-id";
    private static final String CLASS = "class";
    private static final String PACKAGE = "package";
    private static final String CONTENT_DESC = "content-desc";
    private static final String CHECKABLE = "checkable";
    private static final String CHECKED = "checked";
    private static final String CLICKABLE = "clickable";
    private static final String ENABLED = "enabled";
    private static final String FOCUSABLE = "focusable";
    private static final String FOCUSED = "focused";
    private static final String SCROLLABLE = "scrollable";
    private static final String LONG_CLICKABLE = "long-clickable";
    private static final String PASSWORD = "password";
    private static final String SELECTED = "selected";
    private static final String BOUNDS = "bounds";


    public String getClassName() {
        return mClassName;
    }

    public void setClassName(String mClassName) {
        this.mClassName = mClassName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public int getIndex() {
        return mIndex;
    }

    public String getText() {
        return mText;
    }

    public String getResourceId() {
        return mResourceId;
    }

    public String getContentDesc() {
        return mContentDesc;
    }

    public boolean isCheckable() {
        return mCheckable;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public boolean isClickable() {
        return mClickable;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public boolean isFocusable() {
        return mFocusable;
    }

    public boolean isFocused() {
        return mFocused;
    }

    public boolean isScrollable() {
        return mScrollable;
    }

    public boolean isLongClickable() {
        return mLongClickable;
    }

    public boolean isPassword() {
        return mPassword;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public int getBoundsLeft() {
        return mBoundsLeft;
    }

    public int getBoundsTop() {
        return mBoundsTop;
    }

    public int getBoundsRight() {
        return mBoundsRight;
    }

    public int getBoundsBottom() {
        return mBoundsBottom;
    }

    public void setPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }

    public void setIndex(int mIndex) {
        this.mIndex = mIndex;
    }

    public void setText(String mText) {
        this.mText = mText;
    }

    public void setResourceId(String mResourceId) {
        this.mResourceId = mResourceId;
    }

    public void setContentDesc(String mContentDesc) {
        this.mContentDesc = mContentDesc;
    }

    public void setCheckable(boolean mCheckable) {
        this.mCheckable = mCheckable;
    }

    public void setChecked(boolean mChecked) {
        this.mChecked = mChecked;
    }

    public void setClickable(boolean mClickable) {
        this.mClickable = mClickable;
    }

    public void setEnabled(boolean mEnabled) {
        this.mEnabled = mEnabled;
    }

    public void setFocusable(boolean mFocusable) {
        this.mFocusable = mFocusable;
    }

    public void setFocused(boolean mFocused) {
        this.mFocused = mFocused;
    }

    public void setScrollable(boolean mScrollable) {
        this.mScrollable = mScrollable;
    }

    public void setLongClickable(boolean mLongClickable) {
        this.mLongClickable = mLongClickable;
    }

    public void setPassword(boolean mPassword) {
        this.mPassword = mPassword;
    }

    public void setSelected(boolean mSelected) {
        this.mSelected = mSelected;
    }

    public void setBoundsLeft(int mBoundsLeft) {
        this.mBoundsLeft = mBoundsLeft;
    }

    public void setBoundsTop(int mBoundsTop) {
        this.mBoundsTop = mBoundsTop;
    }

    public void setBoundsRight(int mBoundsRight) {
        this.mBoundsRight = mBoundsRight;
    }

    public void setBoundsBottom(int mBoundsBottom) {
        this.mBoundsBottom = mBoundsBottom;
    }

    public List<UINode> getChildren() {
        return mChildren;
    }

    public static UINode fromXML(String xmlStr) throws XmlPullParserException, IOException {
        UINode root = new UINode();

        Stack<UINode> stack = new Stack<>();
        stack.push(root);

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput( new StringReader( xmlStr) );
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.START_DOCUMENT) {
                logd("Start document");
            } else if(eventType == XmlPullParser.START_TAG) {
                logd("Start tag "+xpp.getName());
                switch (xpp.getName()) {
                    case NODE:
                        UINode node = new UINode();
                        node.setClassName(xpp.getAttributeValue(null, CLASS));
                        node.setPackageName(xpp.getAttributeValue(null, PACKAGE));
                        node.setIndex(Integer.parseInt(xpp.getAttributeValue(null, INDEX)));
                        node.setText(xpp.getAttributeValue(null, TEXT));
                        node.setResourceId(xpp.getAttributeValue(null, RESOURCE_ID));
                        node.setContentDesc(xpp.getAttributeValue(null, CONTENT_DESC));
                        node.setCheckable(Boolean.parseBoolean(xpp.getAttributeValue(null, CHECKABLE)));
                        node.setChecked(Boolean.parseBoolean(xpp.getAttributeValue(null, CHECKED)));
                        node.setClickable(Boolean.parseBoolean(xpp.getAttributeValue(null, CLICKABLE)));
                        node.setEnabled(Boolean.parseBoolean(xpp.getAttributeValue(null, ENABLED)));
                        node.setFocusable(Boolean.parseBoolean(xpp.getAttributeValue(null, FOCUSABLE)));
                        node.setFocused(Boolean.parseBoolean(xpp.getAttributeValue(null, FOCUSED)));
                        node.setScrollable(Boolean.parseBoolean(xpp.getAttributeValue(null, SCROLLABLE)));
                        node.setLongClickable(Boolean.parseBoolean(xpp.getAttributeValue(null, LONG_CLICKABLE)));
                        node.setPassword(Boolean.parseBoolean(xpp.getAttributeValue(null, PASSWORD)));
                        node.setSelected(Boolean.parseBoolean(xpp.getAttributeValue(null, SELECTED)));
                        String bounds = xpp.getAttributeValue(null,BOUNDS);
                        node.setBoundsLeft(Integer.parseInt(bounds.substring(bounds.indexOf('[')+1, bounds.indexOf(','))));
                        node.setBoundsTop(Integer.parseInt(bounds.substring(bounds.indexOf(',')+1, bounds.indexOf(']'))));
                        node.setBoundsRight(Integer.parseInt(bounds.substring(bounds.lastIndexOf('[')+1, bounds.lastIndexOf(','))));
                        node.setBoundsBottom(Integer.parseInt(bounds.substring(bounds.lastIndexOf(',')+1, bounds.lastIndexOf(']'))));
                        stack.get(stack.size()-1).getChildren().add(node);
                        stack.push(node);
                        break;
                    case HIERARCHY:

                        break;
                }

            } else if(eventType == XmlPullParser.END_TAG) {
                logd("End tag "+xpp.getName());
                switch (xpp.getName()) {
                    case NODE:
                        root = stack.pop();
                        break;
                    case HIERARCHY:

                        break;
                }
            } else if(eventType == XmlPullParser.TEXT) {
                logd("Text "+xpp.getText());
            }
            eventType = xpp.next();
        }
        logd("End document");
        return root;
    }

    @Override
    public String toString() {
        String str = ""
                + " text=\"" + (TextUtils.isEmpty(getText())?"":getText().toString().replace("<","&lt;").replace(">","&gt;").replace("&","&amp;").replace("\"","&quot;").replace("\'","&apos;"))

                + "\"\n resource-id=\"" + (getResourceId()==null?"":getResourceId())

                + "\"\n class=\"" + (getClassName()==null?"":getClassName())

                + "\"\n package=\"" + (getPackageName()==null?"":getPackageName())

                + "\"\n content-desc=\"" + (getContentDesc()==null?"":getContentDesc().toString().replace("<","&lt;").replace(">","&gt;").replace("&","&amp;").replace("\"","&quot;").replace("\'","&apos;"))

                + "\"\n checkable=\"" + isCheckable()

                + "\"\n checked=\"" + isChecked()

                + "\"\n clickable=\"" + isClickable()

                + "\"\n enabled=\"" + isEnabled()

                + "\"\n focusable=\"" + isFocusable()

                + "\"\n focused=\"" + isFocused()

                + "\"\n scrollable=\"" + isScrollable()

                + "\"\n long-clickable=\"" + isLongClickable()

                + "\"\n password=\"" + isPassword()

                + "\"\n selected=\"" + isSelected()

                + "\"\n bounds=\"[" + getBoundsLeft() + "," + getBoundsTop() + "][" + getBoundsRight() + "," + getBoundsBottom() + "]\""

                + "";
        return str;
    }

    public UINode getChild(int[] path) {
        if(path.length == 1)
            return this;
        int[] subp = new int[path.length-1];
        System.arraycopy(path, 1, subp, 0, path.length-1);
        return mChildren.get(path[1]).getChild(subp);
    }

    public UINode getChildByPos(float x, float y) {
        UINode rslNode = null;

        if(x>mBoundsRight || x<mBoundsLeft || y>mBoundsBottom || y<mBoundsTop) {
            //out of bounds
            return null;
        }

        rslNode = this;

        for(UINode child : mChildren) {
            UINode childRsl = child.getChildByPos(x,y);
            if(childRsl != null
                    && rslNode.getBoundsLeft()<=childRsl.getBoundsLeft()
                    && rslNode.getBoundsTop()<=childRsl.getBoundsTop()
                    && rslNode.getBoundsRight()>=childRsl.getBoundsRight()
                    && rslNode.getBoundsBottom()>=childRsl.getBoundsBottom()) {
                rslNode = childRsl;
            }
        }

        return rslNode;
    }

    public static UINode copyWithoutChildren(UINode from) {
        UINode node = new UINode();
        node.setClassName(from.getClassName());
        node.setPackageName(from.getPackageName());
        node.setIndex(0);
        node.setText(from.getText());
        node.setResourceId(from.getResourceId());
        node.setContentDesc(from.getContentDesc());
        node.setCheckable(from.isCheckable());
        node.setChecked(from.isChecked());
        node.setClickable(from.isClickable());
        node.setEnabled(from.isEnabled());
        node.setFocusable(from.isFocusable());
        node.setFocused(from.isFocused());
        node.setScrollable(from.isScrollable());
        node.setLongClickable(from.isLongClickable());
        node.setPassword(from.isPassword());
        node.setSelected(from.isSelected());
        node.setBoundsLeft(from.getBoundsLeft());
        node.setBoundsTop(from.getBoundsTop());
        node.setBoundsRight(from.getBoundsRight());
        node.setBoundsBottom(from.getBoundsBottom());
        return node;
    }

    private static void logd(String str) {
        Logger.d(TAG, str);
    }
}
