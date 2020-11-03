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
public class HanghuaDetailAdapter extends BaseAdapter{

	private LayoutInflater mInflater;
	private List<StationMonitorDto> mArrayList;

	private final class ViewHolder {
		TextView tvContent,tvDesc;
	}

	public HanghuaDetailAdapter(Context context, List<StationMonitorDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_hanghua_detail, null);
			mHolder = new ViewHolder();
			mHolder.tvContent = convertView.findViewById(R.id.tvContent);
			mHolder.tvDesc = convertView.findViewById(R.id.tvDesc);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		StationMonitorDto dto = mArrayList.get(position);
		
		if (!TextUtils.isEmpty(dto.name)) {
			mHolder.tvContent.setText(dto.name);
		}
		
		if (!TextUtils.isEmpty(dto.value)) {
			mHolder.tvDesc.setText(dto.value);
		}

		return convertView;
	}

}
