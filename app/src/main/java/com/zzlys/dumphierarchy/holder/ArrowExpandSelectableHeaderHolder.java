package com.zzlys.dumphierarchy.holder;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.github.johnkil.print.PrintView;
import com.unnamed.b.atv.model.TreeNode;
import com.zzlys.dumphierarchy.Logger;
import com.zzlys.dumphierarchy.R;


/**
 * Created by ziliang.z on 2017/4/11.
 */

public class ArrowExpandSelectableHeaderHolder extends TreeNode.BaseNodeViewHolder<ArrowExpandSelectableHeaderHolder.IconTreeItem> {
    private TextView tvValue;
    private PrintView arrowView;
    private CheckBox nodeSelector;
    private TreeNode node;
    private ColorStateList textColor = null;

    public ArrowExpandSelectableHeaderHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(final TreeNode node, IconTreeItem value) {
        this.node = node;
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.layout_selectable_header, null, false);

        tvValue = (TextView) view.findViewById(R.id.node_value);
        tvValue.setText(value.text);

        final PrintView iconView = (PrintView) view.findViewById(R.id.icon);
        iconView.setIconText(context.getResources().getString(value.icon));

        arrowView = (PrintView) view.findViewById(R.id.arrow_icon);
        arrowView.setPadding(20,10,10,10);
        if (node.isLeaf()) {
            arrowView.setIconText(R.string.ic_remove);
        }
        arrowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tView.toggleNode(node);
            }
        });


        nodeSelector = (CheckBox) view.findViewById(R.id.node_selector);
        nodeSelector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                node.setSelected(isChecked);
                for (TreeNode n : node.getChildren()) {
                    getTreeView().selectNode(n, isChecked);
                }
            }
        });
        nodeSelector.setChecked(node.isSelected());

        RelativeLayout container = (RelativeLayout)view.findViewById(R.id.container);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) container.getLayoutParams();
        params.leftMargin = value.tabs * 45;
        container.requestLayout();

        return view;
    }

    @Override
    public void toggle(boolean active) {
        arrowView.setIconText(context.getResources().getString(active ? R.string.ic_keyboard_arrow_down : R.string.ic_keyboard_arrow_right));
        if (node.isLeaf()) {
            arrowView.setIconText(R.string.ic_remove);
        }
        Logger.d("holder", "toggle:" + active);
    }

    @Override
    public void toggleSelectionMode(boolean editModeEnabled) {
        if(!editModeEnabled)
            mNode.setSelected(false);
        //nodeSelector.setVisibility(mNode.isSelected() ? View.VISIBLE : View.GONE);
        if(mNode.isSelected()) {
//            tvValue.setTextSize(16);
            tvValue.setBackgroundColor(Color.YELLOW);
            tvValue.setTextColor(Color.BLUE);
        } else {
//            tvValue.setTextSize(12);
            if(textColor == null)
                textColor = tvValue.getTextColors();
            tvValue.setBackgroundColor(Color.TRANSPARENT);
            tvValue.setTextColor(textColor);
        }
    }

    public static class IconTreeItem {
        public int icon;
        public String text;
        public int tabs;

        public IconTreeItem(int icon, String text, int tabs) {
            this.icon = icon;
            this.text = text;
            this.tabs = tabs;
        }
    }
}