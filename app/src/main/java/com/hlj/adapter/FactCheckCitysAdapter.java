package com.hlj.adapter;

/**
 * 实况查询城市列表
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

import shawn.cxwl.com.hlj.R;

public class FactCheckCitysAdapter extends BaseAdapter{

	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<FactDto> mArrayList = new ArrayList<>();

	private final class ViewHolder{
		TextView tvArea;
	}

	private ViewHolder mHolder = null;

	public FactCheckCitysAdapter(Context context, List<FactDto> mArrayList) {
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_fact_check_citys, null);
			mHolder = new ViewHolder();
			mHolder.tvArea = (TextView) convertView.findViewById(R.id.tvArea);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		FactDto dto = mArrayList.get(position);
		
		if (!TextUtils.isEmpty(dto.area)) {
			mHolder.tvArea.setText(dto.area);
		}
		return convertView;
	}

}
