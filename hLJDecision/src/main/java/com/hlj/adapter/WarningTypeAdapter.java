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
 * 预警列表
 */
public class WarningTypeAdapter extends BaseAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private List<WarningDto> mArrayList;

	private final class ViewHolder {
		TextView tvName;
	}

	public WarningTypeAdapter(Context context, List<WarningDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_warning_type, null);
			mHolder = new ViewHolder();
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		WarningDto dto = mArrayList.get(position);

		if (!TextUtils.isEmpty(dto.name)) {
			mHolder.tvName.setText(dto.name);
		}
		if (dto.isSelected) {
			mHolder.tvName.setTextColor(Color.WHITE);
			mHolder.tvName.setBackgroundResource(R.drawable.bg_warning_type_press);
		} else {
			mHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.blue));
			mHolder.tvName.setBackgroundResource(R.drawable.bg_warning_type);
		}

		return convertView;
	}

}
