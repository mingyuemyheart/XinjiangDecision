package com.hlj.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hlj.dto.StationMonitorDto;

import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 航华列表
 */
public class HanghuaListAdapter extends BaseAdapter{

	private LayoutInflater mInflater;
	private List<StationMonitorDto> mArrayList;

	private final class ViewHolder {
		TextView tvTitle,tvTime;
	}

	public HanghuaListAdapter(Context context, List<StationMonitorDto> mArrayList) {
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			convertView = mInflater.inflate(R.layout.adapter_hanghua_list, null);
			mHolder = new ViewHolder();
			mHolder.tvTitle = convertView.findViewById(R.id.tvTitle);
			mHolder.tvTime = convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		StationMonitorDto dto = mArrayList.get(position);
		
		if (!TextUtils.isEmpty(dto.name)) {
			mHolder.tvTitle.setText(dto.name);
		}
		
		if (!TextUtils.isEmpty(dto.time)) {
			mHolder.tvTime.setText(dto.time);
		}

		return convertView;
	}

}
