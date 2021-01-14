package com.hlj.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hlj.dto.StreamFactDto;

import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 强对流天气实况列表
 */
public class ShawnStreamFactListAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private List<StreamFactDto> mArrayList;
	public String columnName = "";

	private final class ViewHolder{
		TextView tvStationName,tvProvince,tvStationId,tvValue;
	}

	public ShawnStreamFactListAdapter(Context context, List<StreamFactDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.shawn_adapter_stream_fact_list, null);
			mHolder = new ViewHolder();
			mHolder.tvStationName = convertView.findViewById(R.id.tvStationName);
			mHolder.tvProvince = convertView.findViewById(R.id.tvProvince);
			mHolder.tvStationId = convertView.findViewById(R.id.tvStationId);
			mHolder.tvValue = convertView.findViewById(R.id.tvValue);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		StreamFactDto dto = mArrayList.get(position);

		if (!TextUtils.isEmpty(columnName)) {
			if (columnName.contains("闪电")){
				mHolder.tvStationName.setText(dto.province);
				mHolder.tvProvince.setText(dto.city);
				mHolder.tvStationId.setText(dto.dis);
				if (!TextUtils.isEmpty(dto.lighting)) {
					mHolder.tvValue.setText(dto.lighting);
				}
			}else {
				if (columnName.contains("降水")) {
					if (!TextUtils.isEmpty(dto.pre1h)) {
						mHolder.tvValue.setText(dto.pre1h);
					}
				}else if (columnName.contains("大风")) {
					if (!TextUtils.isEmpty(dto.windS)) {
						float fx = Float.parseFloat(dto.windD);
						String wind_dir;
						if(fx >= 22.5 && fx < 67.5){
							wind_dir = "东北风";
						}else if(fx >= 67.5 && fx < 112.5){
							wind_dir = "东风";
						}else if(fx >= 112.5 && fx < 157.5){
							wind_dir = "东南风";
						}else if(fx >= 157.5 && fx < 202.5){
							wind_dir = "南风";
						}else if(fx >= 202.5 && fx < 247.5){
							wind_dir = "西南风";
						}else if(fx >= 247.5 && fx < 292.5){
							wind_dir = "西风";
						}else if(fx >= 292.5 && fx < 337.5){
							wind_dir = "西北风";
						}else {
							wind_dir = "北风";
						}
						mHolder.tvValue.setText(wind_dir+" "+dto.windS);
					}
				}else if (columnName.contains("冰雹")) {
					if (!TextUtils.isEmpty(dto.hail)) {
						mHolder.tvValue.setText(dto.hail);
					}
				}

				mHolder.tvStationName.setText(dto.stationName);
				mHolder.tvProvince.setText(dto.province+dto.city+dto.dis);
				mHolder.tvStationId.setText(dto.stationId);
			}

		}

		if (position%2 == 0) {
			convertView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
		}else {
			convertView.setBackgroundColor(mContext.getResources().getColor(R.color.light_gray));
		}

		return convertView;
	}

}
