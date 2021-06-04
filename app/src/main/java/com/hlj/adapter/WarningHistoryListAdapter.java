package com.hlj.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hlj.common.CONST;
import com.hlj.dto.WarningDto;
import com.hlj.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 历史预警-预警列表
 */
public class WarningHistoryListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<WarningDto> groupList;
    private List<List<WarningDto>> childList;
    private LayoutInflater mInflater;
    private List<WarningDto> typeList = new ArrayList<>();

    public WarningHistoryListAdapter(Context context, List<WarningDto> groupList, List<List<WarningDto>> childList){
        mContext = context;
        this.groupList = groupList;
        this.childList = childList;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        typeList.clear();
        String[] array1 = mContext.getResources().getStringArray(R.array.warningType);
        for (String result : array1) {
            String[] value = result.split(",");
            WarningDto dto = new WarningDto();
            dto.name = value[1];
            dto.type = value[0];
            typeList.add(dto);
        }
    }

    class GroupHolder{
        ImageView imageView;
        TextView tvName,tvTime1,tvTime2;
    }

    class ChildHolder{
        TextView tvTime1,tvTime2,tvColor,tvType,tvContent,tvUnit;
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
            convertView = mInflater.inflate(R.layout.adapter_warning_history_list_group, null);
            groupHolder = new GroupHolder();
            groupHolder.imageView = convertView.findViewById(R.id.imageView);
            groupHolder.tvName = convertView.findViewById(R.id.tvName);
            groupHolder.tvTime1 = convertView.findViewById(R.id.tvTime1);
            groupHolder.tvTime2 = convertView.findViewById(R.id.tvTime2);
            convertView.setTag(groupHolder);
        }else{
            groupHolder = (GroupHolder) convertView.getTag();
        }

        WarningDto dto = groupList.get(groupPosition);

        Bitmap bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+"_"+dto.color.toLowerCase()+CONST.imageSuffix);
        if (bitmap == null) {
            bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+"default"+ CONST.imageSuffix);
        }
        groupHolder.imageView.setImageBitmap(bitmap);

        if (!TextUtils.isEmpty(dto.name)) {
            groupHolder.tvName.setText(dto.name);
        }
        if (!TextUtils.isEmpty(dto.time)) {
            groupHolder.tvTime1.setText("发布时间："+dto.time);
        }
        if (!TextUtils.isEmpty(dto.time2)) {
            groupHolder.tvTime2.setText("解除时间："+dto.time2);
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder childHolder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.adapter_warning_history_list_child, null);
            childHolder = new ChildHolder();
            childHolder.tvTime1 = convertView.findViewById(R.id.tvTime1);
            childHolder.tvTime2 = convertView.findViewById(R.id.tvTime2);
            childHolder.tvColor = convertView.findViewById(R.id.tvColor);
            childHolder.tvType = convertView.findViewById(R.id.tvType);
            childHolder.tvContent = convertView.findViewById(R.id.tvContent);
            childHolder.tvUnit = convertView.findViewById(R.id.tvUnit);
            convertView.setTag(childHolder);
        }else{
            childHolder = (ChildHolder) convertView.getTag();
        }

        WarningDto dto = childList.get(groupPosition).get(childPosition);
        if (!TextUtils.isEmpty(dto.time)) {
            childHolder.tvTime1.setText("发布时间："+dto.time);
        }
        if (!TextUtils.isEmpty(dto.time2)) {
            childHolder.tvTime2.setText("解除时间："+dto.time2);
        }
        if (!TextUtils.isEmpty(dto.color)) {
            String color = null;
            if (TextUtils.equals(dto.color, "Red")) {
                color = "红色";
            }else if (TextUtils.equals(dto.color, "Orange")) {
                color = "橙色";
            }else if (TextUtils.equals(dto.color, "Yellow")) {
                color = "黄色";
            }else if (TextUtils.equals(dto.color, "Blue")) {
                color = "蓝色";
            }
            if (!TextUtils.isEmpty(color)) {
                childHolder.tvColor.setText("预警级别："+color);
            }
        }

//        String type = null;
//        if (!TextUtils.isEmpty(dto.type)) {
//            for (int i = 0; i < typeList.size(); i++) {
//                if (TextUtils.equals(typeList.get(i).type, dto.type)) {
//                    type = typeList.get(i).name;
//                    break;
//                }
//            }
//        }
        if (!TextUtils.isEmpty(dto.eventTypeCn)) {
            childHolder.tvType.setText("预警种类："+dto.eventTypeCn);
        }else {
            childHolder.tvType.setText("预警种类："+"未知类型");
        }

        if (!TextUtils.isEmpty(dto.content)) {
            childHolder.tvContent.setText(dto.content);
        }
        if (!TextUtils.isEmpty(dto.name)) {
            String heading = null;
            if (dto.name.contains("发布")) {
                heading = dto.name.split("发布")[0];
            }else if (dto.name.contains("解除")) {
                heading = dto.name.split("解除")[0];
            }
            if (!TextUtils.isEmpty(heading)) {
                childHolder.tvUnit.setText("发布单位："+heading);
            }
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
