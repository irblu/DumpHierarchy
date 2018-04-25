package com.zzlys.dumphierarchy.presenter;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;
import com.zzlys.dumphierarchy.Constants;
import com.zzlys.dumphierarchy.Logger;
import com.zzlys.dumphierarchy.R;
import com.zzlys.dumphierarchy.Utils;
import com.zzlys.dumphierarchy.holder.ArrowExpandSelectableHeaderHolder;
import com.zzlys.dumphierarchy.model.UINode;
import com.zzlys.dumphierarchy.view.HierarchyViewerActivity;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;

import static com.zzlys.dumphierarchy.Constants.DUMP_FOLDER_DIR;

/**
 * Created by ziliang.z on 2017/4/10.
 */

public class HierarchyViewerPresenter  implements TreeNode.TreeNodeClickListener, TreeNode.TreeNodeLongClickListener, View.OnTouchListener, TextWatcher{

    private static final String TAG = "HierarchyViewerPresenter";

    private HierarchyViewerActivity _activity;
    private AndroidTreeView _treeView;
    private UINode _uiNode;         //not in search mode
    private UINode _uiNode4Search;  //in search mode
    private Bitmap _bitmap;
    private Point _imgSize;
    private TreeNode _rootNode;         //not in search mode
    //private TreeNode _rootNode4Search;  //in search mode
    RelativeLayout _containerView;
    private String _searchKeyword;
    private HashMap<UINode,TreeNode> _nodeMapping;

    private boolean _isSearchMode = false;

    private int _maxHeight = 0;

    public void setActivity(HierarchyViewerActivity activity) {
        _activity = activity;
        initial();
    }

    private void initial() {
        setNoTitleBar();
        _activity.setContentView(R.layout.activity_hierarchy_detail);
        setFullScreen();

        String uixPath = _activity.getIntent().getStringExtra(Constants.FILE_PATH);
        try {
            _uiNode = _uiNode.fromXML(readFile(uixPath));
            String pngPath = uixPath.substring(0,uixPath.lastIndexOf(".")) + ".png";
            buildNodeTreeView(_uiNode);
            buildImageView(pngPath);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        _activity.setOnSearchTextChangeListener(this);

    }

    /**
     * must be called before setContentView()
     */
    private void setNoTitleBar() {
        _activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    private void setFullScreen() {
        _activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private TreeNode UINode2TreeNode(UINode uinode, int index, int tabs) {
        String className = uinode.getClassName();
        String nodeName = "(" + index + ")" + className.substring(className.lastIndexOf('.') + 1,className.length());
        TreeNode root;
        if(_isSearchMode && uinode.getText().equals("SearchResult:")) {
            root = new TreeNode(new ArrowExpandSelectableHeaderHolder.IconTreeItem(R.string.ic_remove_circle, nodeName + uinode.getText(), tabs));
        }else if(_isSearchMode && isSearchResultSubTitleNode(uinode)) {
            root = new TreeNode(new ArrowExpandSelectableHeaderHolder.IconTreeItem(R.string.ic_remove_circle, nodeName + uinode.getText() + "[" + uinode.getChildren().size() + "]", tabs));
        } else {
            root = new TreeNode(new ArrowExpandSelectableHeaderHolder.IconTreeItem(R.string.ic_remove_circle, nodeName + uinode.getText() + "[" + uinode.getBoundsLeft() + "," + uinode.getBoundsTop() + "][" + uinode.getBoundsRight() + "," + uinode.getBoundsBottom() + "]", tabs));
        }
        _nodeMapping.put(uinode,root);
        int id = 0;
        for(UINode sub:uinode.getChildren()) {
            root.addChild(UINode2TreeNode(sub, id, tabs + 1));
            id++;
        }
        return root;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        Logger.d(TAG,"afterTextChange:" + s);
        _searchKeyword = s.toString();
        if(TextUtils.isEmpty(_searchKeyword)) {
            switchMode(false);
        } else {
            switchMode(true);
        }
    }

    private void switchMode(Boolean searchMode) {
        _isSearchMode = searchMode;
        if(_isSearchMode) {
            _uiNode4Search = new UINode();
            _uiNode4Search.setText("SearchResult:");
            _uiNode4Search.setBoundsRight(_bitmap.getWidth());
            _uiNode4Search.setBoundsBottom(_bitmap.getHeight());
            buildChildrenById(_uiNode4Search);
            buildChildrenByText(_uiNode4Search);
            buildChildrenByClassName(_uiNode4Search);
            buildChildrenByDescription(_uiNode4Search);
            buildNodeTreeView(_uiNode4Search);
            _treeView.expandNode(_rootNode.getChildren().get(0));
        } else {
            buildNodeTreeView(_uiNode);
        }
    }

    private void buildChildrenByClassName(UINode root) {
        UINode node = new UINode();
        node.setText("ByClassName");
        node.setBoundsRight(_bitmap.getWidth());
        node.setBoundsBottom(_bitmap.getHeight());
        ArrayList<UINode> array = new ArrayList<>();
        array.add(_uiNode);
        while(array.size()>0) {
            UINode thisNode = array.get(0);
            array.remove(0);
            for(int i=0; i<thisNode.getChildren().size(); i++) {
                array.add(thisNode.getChildren().get(i));
            }
            if(thisNode.getClassName().toLowerCase().contains(_searchKeyword.toLowerCase())) {
                node.getChildren().add(thisNode);
            }
        }
        root.getChildren().add(node);
    }

    private void buildChildrenByText(UINode root) {
        UINode node = new UINode();
        node.setText("ByText");
        node.setBoundsRight(_bitmap.getWidth());
        node.setBoundsBottom(_bitmap.getHeight());
        ArrayList<UINode> array = new ArrayList<>();
        array.add(_uiNode);
        while(array.size()>0) {
            UINode thisNode = array.get(0);
            array.remove(0);
            for(int i=0; i<thisNode.getChildren().size(); i++) {
                array.add(thisNode.getChildren().get(i));
            }
            if(thisNode.getText().toLowerCase().contains(_searchKeyword.toLowerCase())) {
                node.getChildren().add(thisNode);
            }
        }
        root.getChildren().add(node);
    }

    private void buildChildrenById(UINode root) {
        UINode node = new UINode();
        node.setText("ById");
        node.setBoundsRight(_bitmap.getWidth());
        node.setBoundsBottom(_bitmap.getHeight());
        ArrayList<UINode> array = new ArrayList<>();
        array.add(_uiNode);
        while(array.size()>0) {
            UINode thisNode = array.get(0);
            array.remove(0);
            for(int i=0; i<thisNode.getChildren().size(); i++) {
                array.add(thisNode.getChildren().get(i));
            }
            if(thisNode.getResourceId().toLowerCase().contains(_searchKeyword.toLowerCase())) {
                node.getChildren().add(thisNode);
            }
        }
        root.getChildren().add(node);
    }

    private void buildChildrenByDescription(UINode root) {
        UINode node = new UINode();
        node.setText("ByDescription");
        node.setBoundsRight(_bitmap.getWidth());
        node.setBoundsBottom(_bitmap.getHeight());
        ArrayList<UINode> array = new ArrayList<>();
        array.add(_uiNode);
        while(array.size()>0) {
            UINode thisNode = array.get(0);
            array.remove(0);
            for(int i=0; i<thisNode.getChildren().size(); i++) {
                array.add(thisNode.getChildren().get(i));
            }
            if(thisNode.getContentDesc().toLowerCase().contains(_searchKeyword.toLowerCase())) {
                node.getChildren().add(thisNode);
            }
        }
        root.getChildren().add(node);
    }

    private void buildImageView(String filePath) {
        _bitmap = BitmapFactory.decodeFile(filePath);
        _imgSize = _activity.setSceenshotImage(_bitmap);
        _activity.setScreenshotOnTouchListener(this);
    }

    private Bitmap drawRect(int l, int t, int r, int b) {
        Bitmap bmp = _bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        int strokeWidth = 5;
        paint.setStrokeWidth(strokeWidth);
        canvas.drawRect(l,t,r,b,paint);
        paint.setColor(Color.BLUE);
        canvas.drawRect(l+strokeWidth,t+strokeWidth,r-strokeWidth,b-strokeWidth,paint);
        return bmp;
    }

    private void buildNodeTreeView(UINode node) {
        if(node == null) {
            return;
        }
        _nodeMapping = new HashMap<>();
        _rootNode = TreeNode.root();
        _rootNode.addChild(UINode2TreeNode(node, 0, 0));
        if(_containerView == null)
            _containerView = (RelativeLayout)_activity.findViewById(R.id.container_treenode);
        else
            _containerView.removeAllViews();
        _treeView = new AndroidTreeView(_activity, _rootNode);
        _treeView.setDefaultNodeClickListener(this);
        _treeView.setDefaultNodeLongClickListener(this);
        _treeView.setDefaultViewHolder(ArrowExpandSelectableHeaderHolder.class);


        _containerView.addView(_treeView.getView());

        ViewGroup.LayoutParams params = _containerView.getLayoutParams();

        if(params.height > _maxHeight)
            _maxHeight = params.height;
        else
            params.height = _maxHeight;
        _containerView.setLayoutParams(params);

        _treeView.setUseAutoToggle(false);
    }

    @Override
    public void onClick(TreeNode node, Object value) {
        UINode root = _isSearchMode?_uiNode4Search:_uiNode;

        if (!node.isSelected()) {
            UINode uiNode = root.getChild(Utils.formatPath2Ints(node.getPath()));
            Bitmap bmp = drawRect(uiNode.getBoundsLeft(), uiNode.getBoundsTop(), uiNode.getBoundsRight(), uiNode.getBoundsBottom());
            _imgSize = _activity.setSceenshotImage(bmp);

            _treeView.setSelectionModeEnabled(false);
            node.setSelected(true);
            _treeView.setSelectionModeEnabled(true);
        } else {
            //_treeView.toggleNode(node);
            onLongClick(node, null);
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        UINode root = _isSearchMode?_uiNode4Search:_uiNode;
        _imgSize = _activity.getImageViewSize();
        Point p = locationTransfer(event.getX(), event.getY(), _imgSize.x, _imgSize.y, _bitmap.getWidth(), _bitmap.getHeight());
        UINode nodeTouched = root.getChildByPos(p.x, p.y);
        if(isSearchResultTitleNode(nodeTouched))
            return false;
        TreeNode treeNode = _nodeMapping.get(nodeTouched);
        if(treeNode == null) {
            return false;
        }
        expandNode(treeNode);
        if(treeNode.isSelected()) {
            onLongClick(treeNode, null);
        } else {
            onClick(treeNode, null);
        }

        return false;
    }

    private void expandNode(TreeNode node) {
        if(node == null) {
            return;
        }
        if(!node.isExpanded())
            _treeView.expandNode(node);
        TreeNode parent = node.getParent();
        if(parent!=null)
            expandNode(parent);
    }


    @Override
    public boolean onLongClick(TreeNode node, Object value) {
        UINode root = _isSearchMode?_uiNode4Search:_uiNode;
        UINode uiNode = root.getChild(Utils.formatPath2Ints(node.getPath()));
        AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
        builder.setTitle("Path:" + Utils.formatPath2String(node.getPath()));
        builder.setMessage(uiNode.toString());
        AlertDialog dlg = builder.create();
        dlg.show();
        //Toast.makeText(_activity, uiNode.toString(), Toast.LENGTH_SHORT).show();
        return true;
    }

    public String readFile(String filePath) {
        File file = new File(filePath);
        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        String rawData = text.toString();
        return rawData;
    }

    private Point locationTransfer(float xSrc, float ySrc, int wSrc, int hSrc, int wDst, int hDst) {

        int xDst = 0;
        int yDst = 0;

        xDst = (int)(xSrc * wDst * 1.0 / wSrc);
        yDst = (int)(ySrc * hDst * 1.0 / hSrc);

        return new Point(xDst,yDst);
    }


    private boolean isSearchResultTitleNode(UINode uinode) {
        if(uinode == null)
            return false;
        if(uinode.getText().equals("SearchResult:") || isSearchResultSubTitleNode(uinode))
            return true;
        return false;
    }

    private boolean isSearchResultSubTitleNode(UINode uinode) {
        if(uinode == null)
            return false;
        if(uinode.getText().equals("ById") || uinode.getText().equals("ByText") || uinode.getText().equals("ByClassName") || uinode.getText().equals("ByDescription")) {
            return true;
        }
        return false;
    }
}
