package com.hlj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hlj.dto.WarningDto;
import com.hlj.stickygridheaders.StickyGridHeadersSimpleAdapter;

import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 预警列表-筛选
 */
public class WarningListScreenAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter {

	private Context mContext;
	private List<WarningDto> mArrayList;
	private LayoutInflater mInflater;

	public WarningListScreenAdapter(Context context, List<WarningDto> mArrayList) {
		this.mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	private class ViewHolder {
		TextView tvCityName;
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
		ViewHolder mHolder;
		if (convertView == null) {
			mHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.hadapter_city_content, null);
			mHolder.tvCityName = convertView.findViewById(R.id.tvCityName);
			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		WarningDto dto = mArrayList.get(position);
		mHolder.tvCityName.setText(dto.areaName);

		return convertView;
	}


	private class HeaderViewHolder {
		TextView tvName;
	}

	@Override
	public long getHeaderId(int position) {
		return mArrayList.get(position).section;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeaderViewHolder mHeaderHolder;
		if (convertView == null) {
			mHeaderHolder = new HeaderViewHolder();
			convertView = mInflater.inflate(R.layout.hadapter_city_header, null);
			mHeaderHolder.tvName = convertView.findViewById(R.id.tvName);
			convertView.setTag(mHeaderHolder);
		} else {
			mHeaderHolder = (HeaderViewHolder) convertView.getTag();
		}

		WarningDto dto = mArrayList.get(position);
		mHeaderHolder.tvName.setText(dto.sectionName);

		return convertView;
	}

}
