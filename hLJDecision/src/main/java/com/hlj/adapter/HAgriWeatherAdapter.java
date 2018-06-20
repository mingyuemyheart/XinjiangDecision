package com.hlj.adapter;

/**
 * 农业气象、人工影响天气
 */

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hlj.dto.AgriDto;

import net.tsz.afinal.FinalBitmap;

import java.util.List;

import shawn.cxwl.com.hlj.R;

public class HAgriWeatherAdapter extends BaseAdapter{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<AgriDto> mArrayList;
	
	private final class ViewHolder{
		TextView tvName;
		ImageView imageView;
	}
	
	private ViewHolder mHolder = null;
	
	public HAgriWeatherAdapter(Context context, List<AgriDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.hadapter_agri_weather, null);
			mHolder = new ViewHolder();
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		AgriDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.name)) {
			mHolder.tvName.setText(dto.name);
		}

		if (!TextUtils.isEmpty(dto.icon)) {
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(mHolder.imageView, dto.icon, null, 0);
		}

		return convertView;
	}

}
