package com.hlj.adapter;

/**
 * 天气雷达详情
 */

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hlj.dto.RadarDto;

import shawn.cxwl.com.hlj.decision.R;

public class WeatherRadarDetailAdpater extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<RadarDto> mArrayList = new ArrayList<>();
	private String selected = "1";//选中
	private String unselected = "0";//未选中
	
	private final class ViewHolder{
		TextView tvTime;
	}
	
	private ViewHolder mHolder = null;
	
	public WeatherRadarDetailAdpater(Context context, List<RadarDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_weather_radar_detail, null);
			mHolder = new ViewHolder();
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		RadarDto dto = mArrayList.get(position);
		mHolder.tvTime.setText(dto.time);
		if (dto.isSelect.equals(selected)) {
			mHolder.tvTime.setTextColor(mContext.getResources().getColor(R.color.title_bg));
		}else if (dto.isSelect.equals(unselected)){
			mHolder.tvTime.setTextColor(mContext.getResources().getColor(R.color.text_color2));
		}
		
		return convertView;
	}

}
