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

import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 预警图例
 */
public class WarningLegendAdapter extends BaseAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private List<WarningDto> mArrayList;

	private final class ViewHolder {
		TextView name,tvName;
	}

	public WarningLegendAdapter(Context context, List<WarningDto> mArrayList) {
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
		ViewHolder mHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_warning_legend, null);
			mHolder = new ViewHolder();
			mHolder.name = convertView.findViewById(R.id.name);
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		WarningDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.color)) {
			mHolder.name.setBackgroundColor(Color.parseColor(dto.color));
		} else {
			switch (dto.type) {
				case "01" :
					mHolder.name.setBackgroundColor(Color.parseColor("#1D67C1"));
					break;
				case "02" :
					mHolder.name.setBackgroundColor(Color.parseColor("#F7BA34"));
					break;
				case "03" :
					mHolder.name.setBackgroundColor(Color.parseColor("#F98227"));
					break;
				case "04" :
					mHolder.name.setBackgroundColor(Color.parseColor("#D4292A"));
					break;
				default :
					mHolder.name.setBackgroundColor(Color.parseColor("#BEBEBE"));
					break;
			}
		}
		mHolder.tvName.setText(dto.name+"\n("+dto.count+")");

		return convertView;
	}

}
