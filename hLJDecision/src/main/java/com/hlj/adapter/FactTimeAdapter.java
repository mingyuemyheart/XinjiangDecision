package com.hlj.adapter;

/**
 * 实况时间选择
 */

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.hlj.dto.FactDto;

import java.util.ArrayList;
import java.util.List;

import shawn.cxwl.com.hlj.decision.R;

public class FactTimeAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<FactDto> mArrayList = new ArrayList<>();
	
	private final class ViewHolder{
		TextView tvTime;
	}
	
	private ViewHolder mHolder = null;
	
	public FactTimeAdapter(Context context, List<FactDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_fact_time, null);
			mHolder = new ViewHolder();
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		FactDto dto = mArrayList.get(position);
		
		if (!TextUtils.isEmpty(dto.timeString)) {
			mHolder.tvTime.setText(dto.timeString);
		}
		
		return convertView;
	}

}
