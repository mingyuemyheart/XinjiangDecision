package com.hlj.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import shawn.cxwl.com.hlj.R;
import com.hlj.dto.RangeDto;

public class TrendRankAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<RangeDto> mArrayList = new ArrayList<RangeDto>();
	
	private final class ViewHolder{
		TextView tvNum;
		TextView tvName;
		TextView tvValue;
		LinearLayout llCell;
	}
	
	private ViewHolder mHolder = null;
	
	public TrendRankAdapter(Context context, List<RangeDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.trend_rank_cell, null);
			mHolder = new ViewHolder();
			mHolder.tvNum = (TextView) convertView.findViewById(R.id.tvNum);
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			mHolder.tvValue = (TextView) convertView.findViewById(R.id.tvValue);
			mHolder.llCell = (LinearLayout) convertView.findViewById(R.id.llCell);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		RangeDto dto = mArrayList.get(position);
		mHolder.tvNum.setText(String.valueOf(position+1));
		if (!TextUtils.isEmpty(dto.cityName)) {
			mHolder.tvName.setText(dto.cityName+"："+dto.areaName);
		}else {
			mHolder.tvName.setText(dto.areaName);
		}
		mHolder.tvValue.setText(dto.value);
		if (position % 2 == 0) {//偶数
			mHolder.llCell.setBackgroundColor(0xffF1F5F8);
		}else {//奇数
			mHolder.llCell.setBackgroundColor(0xffDCE7ED);
		}
		
		return convertView;
	}

}
