package com.hlj.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hlj.dto.WarningDto;

import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 预警统计
 */
public class ShawnWarningStatisticAdapter extends BaseAdapter{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<WarningDto> mArrayList;
	
	private final class ViewHolder {
		TextView tvWarning,tvNation,tvPro,tvCity,tvDis;
	}
	
	public ShawnWarningStatisticAdapter(Context context, List<WarningDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.shawn_adapter_warning_statistic, null);
			mHolder = new ViewHolder();
			mHolder.tvWarning = convertView.findViewById(R.id.tvWarning);
			mHolder.tvNation = convertView.findViewById(R.id.tvNation);
			mHolder.tvPro = convertView.findViewById(R.id.tvPro);
			mHolder.tvCity = convertView.findViewById(R.id.tvCity);
			mHolder.tvDis = convertView.findViewById(R.id.tvDis);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		WarningDto dto = mArrayList.get(position);
		mHolder.tvWarning.setText(dto.colorName);
		mHolder.tvNation.setText(dto.nationCount);
		mHolder.tvNation.setVisibility(View.GONE);
		mHolder.tvPro.setText(dto.proCount);
		mHolder.tvCity.setText(dto.cityCount);
		mHolder.tvDis.setText(dto.disCount);

        if (dto.colorName.contains("共")) {
			mHolder.tvWarning.setTextColor(mContext.getResources().getColor(R.color.text_color4));
			mHolder.tvWarning.setBackgroundColor(0x80f0eff5);
			mHolder.tvNation.setBackgroundColor(0x80f0eff5);
			mHolder.tvPro.setBackgroundColor(0x80f0eff5);
			mHolder.tvCity.setBackgroundColor(0x80f0eff5);
			mHolder.tvDis.setBackgroundColor(0x80f0eff5);
        }else if (dto.colorName.contains("红")) {
			mHolder.tvWarning.setTextColor(Color.WHITE);
			mHolder.tvWarning.setBackgroundColor(0xfffe2624);
			mHolder.tvNation.setBackgroundColor(0x10fe2624);
			mHolder.tvPro.setBackgroundColor(0x10fe2624);
			mHolder.tvCity.setBackgroundColor(0x10fe2624);
			mHolder.tvDis.setBackgroundColor(0x10fe2624);
		}else if (dto.colorName.contains("橙")) {
			mHolder.tvWarning.setTextColor(Color.WHITE);
			mHolder.tvWarning.setBackgroundColor(0xfffea228);
			mHolder.tvNation.setBackgroundColor(0x10fea228);
			mHolder.tvPro.setBackgroundColor(0x10fea228);
			mHolder.tvCity.setBackgroundColor(0x10fea228);
			mHolder.tvDis.setBackgroundColor(0x10fea228);
		}else if (dto.colorName.contains("黄")) {
			mHolder.tvWarning.setTextColor(Color.WHITE);
			mHolder.tvWarning.setBackgroundColor(0xffecdf04);
			mHolder.tvNation.setBackgroundColor(0x10ecdf04);
			mHolder.tvPro.setBackgroundColor(0x10ecdf04);
			mHolder.tvCity.setBackgroundColor(0x10ecdf04);
			mHolder.tvDis.setBackgroundColor(0x10ecdf04);
		}else if (dto.colorName.contains("蓝")) {
			mHolder.tvWarning.setTextColor(Color.WHITE);
			mHolder.tvWarning.setBackgroundColor(0xff2f82db);
			mHolder.tvNation.setBackgroundColor(0x102f82db);
			mHolder.tvPro.setBackgroundColor(0x102f82db);
			mHolder.tvCity.setBackgroundColor(0x102f82db);
			mHolder.tvDis.setBackgroundColor(0x102f82db);
		}else if (dto.colorName.contains("未知")) {
			mHolder.tvWarning.setTextColor(Color.WHITE);
			mHolder.tvWarning.setBackgroundColor(0xff999999);
			mHolder.tvNation.setBackgroundColor(0x20999999);
			mHolder.tvPro.setBackgroundColor(0x20999999);
			mHolder.tvCity.setBackgroundColor(0x20999999);
			mHolder.tvDis.setBackgroundColor(0x20999999);
		}
		
		return convertView;
	}

}
