package com.hlj.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlj.activity.ShawnWarningStatisticActivity;
import com.hlj.dto.WarningDto;

import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 预警统计
 */
public class ShawnWarningStatisticGroupAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<WarningDto> groupList;
    private List<List<WarningDto>> childList;
    private LayoutInflater mInflater;
    private ExpandableListView listView;
    private String startTime, endTime;

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public ShawnWarningStatisticGroupAdapter(Context context, List<WarningDto> groupList, List<List<WarningDto>> childList, ExpandableListView listView){
        mContext = context;
        this.groupList = groupList;
        this.childList = childList;
        this.listView = listView;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    class GroupHolder{
        TextView tvAreaName,tvShortName,tvCount,tvRed,tvOrange,tvYellow,tvBlue;
        LinearLayout llAll;
        ImageView ivArrow;
    }

    class ChildHolder{
        TextView tvAreaName,tvShortName,tvCount,tvRed,tvOrange,tvYellow,tvBlue;
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childList.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder groupHolder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.shawn_adapter_warning_statistic_group, null);
            groupHolder = new GroupHolder();
            groupHolder.tvAreaName = convertView.findViewById(R.id.tvAreaName);
            groupHolder.llAll = convertView.findViewById(R.id.llAll);
            groupHolder.tvShortName = convertView.findViewById(R.id.tvShortName);
            groupHolder.ivArrow = convertView.findViewById(R.id.ivArrow);
            groupHolder.tvCount = convertView.findViewById(R.id.tvCount);
            groupHolder.tvRed = convertView.findViewById(R.id.tvRed);
            groupHolder.tvOrange = convertView.findViewById(R.id.tvOrange);
            groupHolder.tvYellow = convertView.findViewById(R.id.tvYellow);
            groupHolder.tvBlue = convertView.findViewById(R.id.tvBlue);
            convertView.setTag(groupHolder);
        }else{
            groupHolder = (GroupHolder) convertView.getTag();
        }

        //判断是否已经打开列表
        if(isExpanded){
            groupHolder.ivArrow.setImageResource(R.drawable.statistic_arrow_top);
        }else{
            groupHolder.ivArrow.setImageResource(R.drawable.statistic_arrow_bottom);
        }

        final WarningDto dto = groupList.get(groupPosition);
        if (!TextUtils.isEmpty(dto.areaName)) {
//            String areaName = dto.areaName;
//            if (areaName.contains("省")) {
//                areaName = areaName.replace("省", "");
//            }
//            if (areaName.startsWith("内蒙古")) {
//                areaName = "内蒙古";
//            }else if (areaName.startsWith("广西")) {
//                areaName = "广西";
//            }else if (areaName.startsWith("西藏")) {
//                areaName = "西藏";
//            }else if (areaName.startsWith("宁夏")) {
//                areaName = "宁夏";
//            }else if (areaName.startsWith("新疆")) {
//                areaName = "新疆";
//            }
//            groupHolder.tvAreaName.setText(areaName);
            groupHolder.tvAreaName.setText(dto.areaName);
        }else {
            groupHolder.tvAreaName.setText("总计");
        }
        if (!TextUtils.isEmpty(dto.areaKey) && !TextUtils.equals(dto.areaKey, "all") && !dto.areaKey.contains("00") && dto.areaKey.length() != 6) {
            if (!TextUtils.isEmpty(dto.areaName)) {
                SpannableString ss = new SpannableString(dto.areaName);
                ss.setSpan(new UnderlineSpan(), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                groupHolder.tvAreaName.setText(ss);
            }
        }
        if (!TextUtils.isEmpty(dto.shortName)) {
            groupHolder.tvShortName.setText(dto.shortName);
        }else {
            groupHolder.tvShortName.setText("全部");
        }
        groupHolder.tvCount.setText(dto.warningCount);
        groupHolder.tvRed.setText(dto.redCount);
        groupHolder.tvOrange.setText(dto.orangeCount);
        groupHolder.tvYellow.setText(dto.yellowCount);
        groupHolder.tvBlue.setText(dto.blueCount);

        groupHolder.tvAreaName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.equals(dto.areaKey, "all") && !dto.areaKey.contains("00") && dto.areaKey.length() != 6) {
                    Intent intent = new Intent(mContext, ShawnWarningStatisticActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("data", dto);
                    intent.putExtras(bundle);
                    intent.putExtra("startTime", startTime);
                    intent.putExtra("endTime", endTime);
                    mContext.startActivity(intent);
                }
            }
        });

        groupHolder.llAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listView.isGroupExpanded(groupPosition)) {
                    listView.collapseGroup(groupPosition);
                }else {
                    listView.expandGroup(groupPosition, true);
                }
            }
        });

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder childHolder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.shawn_adapter_warning_statistic_child, null);
            childHolder = new ChildHolder();
            childHolder.tvAreaName = convertView.findViewById(R.id.tvAreaName);
            childHolder.tvShortName = convertView.findViewById(R.id.tvShortName);
            childHolder.tvCount = convertView.findViewById(R.id.tvCount);
            childHolder.tvRed = convertView.findViewById(R.id.tvRed);
            childHolder.tvOrange = convertView.findViewById(R.id.tvOrange);
            childHolder.tvYellow = convertView.findViewById(R.id.tvYellow);
            childHolder.tvBlue = convertView.findViewById(R.id.tvBlue);
            convertView.setTag(childHolder);
        }else{
            childHolder = (ChildHolder) convertView.getTag();
        }

        WarningDto dto = childList.get(groupPosition).get(childPosition);
        if (!TextUtils.isEmpty(dto.shortName)) {
//            String shortName = dto.shortName;
//            if (shortName.contains("事件")) {
//                shortName = shortName.replaceAll("事件", "");
//            }
            childHolder.tvShortName.setText(dto.shortName);
        }
        childHolder.tvCount.setText(dto.warningCount);
        childHolder.tvRed.setText(dto.redCount);
        childHolder.tvOrange.setText(dto.orangeCount);
        childHolder.tvYellow.setText(dto.yellowCount);
        childHolder.tvBlue.setText(dto.blueCount);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
