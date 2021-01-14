package com.hlj.adapter;

/**
 * 6小时降水量
 */

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hlj.stickygridheaders.StickyGridHeadersSimpleAdapter;
import shawn.cxwl.com.hlj.R;
import com.hlj.dto.RangeDto;

public class SixHourRainfallAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter {

	private Context mContext = null;
	private List<RangeDto> mArrayList = null;
	private LayoutInflater mInflater = null;

	public SixHourRainfallAdapter(Context context, List<RangeDto> mArrayList) {
		this.mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	private ViewHolder mHolder = null;
	
	private class ViewHolder {
		TextView tvAreaName;
		TextView tvRainfll;
	}

	@Override
	public int getCount() {
		return mArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return mArrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			mHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.six_hour_item_content, null);
			mHolder.tvAreaName = (TextView) convertView.findViewById(R.id.tvAreaName);
			mHolder.tvRainfll = (TextView) convertView.findViewById(R.id.tvRainfll);
			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		RangeDto dto = mArrayList.get(position);
		mHolder.tvAreaName.setText(dto.areaName+" ( "+dto.stationId+" ) ");
		if (!TextUtils.isEmpty(dto.sixRain)) {
			mHolder.tvRainfll.setText(dto.sixRain+mContext.getString(R.string.unit_mm));
		}
		return convertView;
	}
	
	private HeaderViewHolder mHeaderHolder = null;

	private class HeaderViewHolder {
		TextView tvCityName;
	}

	@Override
	public long getHeaderId(int position) {
		return mArrayList.get(position).section;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			mHeaderHolder = new HeaderViewHolder();
			convertView = mInflater.inflate(R.layout.six_hour_item_header, null);
			mHeaderHolder.tvCityName = (TextView) convertView.findViewById(R.id.tvCityName);
			convertView.setTag(mHeaderHolder);
		} else {
			mHeaderHolder = (HeaderViewHolder) convertView.getTag();
		}
		
		RangeDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.cityName)) {
			mHeaderHolder.tvCityName.setText(dto.cityName);
		}

		return convertView;
	}

}
