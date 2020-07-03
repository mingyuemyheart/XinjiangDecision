package com.hlj.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hlj.dto.WarningDto;

import java.util.HashMap;
import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 预警列表-筛选
 */
public class WarningListScreenAdapter extends BaseAdapter{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<WarningDto> mArrayList;
	public HashMap<Integer, Boolean> isSelected = new HashMap<>();
	private int totalCount = 0;
	private int flag;
	
	private final class ViewHolder {
		TextView tvName;//预警信息名称
	}
	
	public WarningListScreenAdapter(Context context, List<WarningDto> mArrayList, int flag) {
		mContext = context;
		this.flag = flag;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		for (int i = 0; i < mArrayList.size(); i++) {
			if (i == 0) {
				isSelected.put(i, true);
			}else {
				isSelected.put(i, false);
			}
			totalCount += mArrayList.get(i).count;
		}
	}

	@Override
	public int getCount() {
		return mArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder mHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_warning_list_screen, null);
			mHolder = new ViewHolder();
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		WarningDto dto = mArrayList.get(position);
		if (flag == 1) {
			if (!TextUtils.equals(dto.type, "999999")) {
				mHolder.tvName.setText(dto.name+"("+dto.count+")");
			}else {
				mHolder.tvName.setText(dto.name+"("+totalCount+")");
			}
		}else if (flag == 2) {
			if (!TextUtils.equals(dto.color, "999999")) {
				mHolder.tvName.setText(dto.name+"("+dto.count+")");
			}else {
				mHolder.tvName.setText(dto.name+"("+totalCount+")");
			}
		}else if (flag == 3) {
			if (!TextUtils.equals(dto.provinceId, "999999")) {
				mHolder.tvName.setText(dto.name+"("+dto.count+")");
			}else {
				mHolder.tvName.setText(dto.name+"("+totalCount+")");
			}
		}

		if (isSelected.get(position)) {
			mHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.white));
			mHolder.tvName.setBackgroundResource(R.drawable.shawn_bg_warning_selected);
		}else {
			mHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.text_color4));
			mHolder.tvName.setBackgroundColor(Color.TRANSPARENT);
		}
		
		return convertView;
	}

}
