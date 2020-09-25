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

		switch (dto.type) {
			case "11B03" :
				mHolder.name.setBackgroundColor(Color.parseColor("#4383AE"));
				break;
			case "11B04" :
				mHolder.name.setBackgroundColor(Color.parseColor("#8E59B2"));
				break;
			case "11B05" :
				mHolder.name.setBackgroundColor(Color.parseColor("#554EAD"));
				break;
			case "11B06" :
				mHolder.name.setBackgroundColor(Color.parseColor("#C77B2D"));
				break;
			case "11B15" :
				mHolder.name.setBackgroundColor(Color.parseColor("#7CC0A7"));
				break;
			case "11B17" :
				mHolder.name.setBackgroundColor(Color.parseColor("#6B6C6D"));
				break;
			case "11B19" :
				mHolder.name.setBackgroundColor(Color.parseColor("#814E4F"));
				break;
			case "11B20" :
				mHolder.name.setBackgroundColor(Color.parseColor("#9DC093"));
				break;
			case "11B21" :
				mHolder.name.setBackgroundColor(Color.parseColor("#D3B2B3"));
				break;
			case "11B25" :
				mHolder.name.setBackgroundColor(Color.parseColor("#C32E30"));
				break;
			case "11B14" :
				mHolder.name.setBackgroundColor(Color.parseColor("#D4765E"));
				break;
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
		mHolder.tvName.setText(dto.name+"\n("+dto.count+")");

		return convertView;
	}

}
