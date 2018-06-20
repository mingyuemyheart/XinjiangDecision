package com.hlj.adapter;

/**
 * 天气预报、天气实况、电力气象服务、铁路气象服务
 */

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hlj.dto.AgriDto;

import net.tsz.afinal.FinalBitmap;

import java.util.List;

import shawn.cxwl.com.hlj.R;

public class HWeatherForecastFragmentAdapter extends BaseAdapter{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<AgriDto> mArrayList;
	private int width = 0;
	
	private final class ViewHolder{
		ImageView imageView;
		TextView tvName;
	}
	
	private ViewHolder mHolder = null;
	
	public HWeatherForecastFragmentAdapter(Context context, List<AgriDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
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
			convertView = mInflater.inflate(R.layout.hadapter_fragment_weather_forecast, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		AgriDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.name)) {
			mHolder.tvName.setText(dto.name);
		}

		if (TextUtils.isEmpty(dto.icon2)) {
			mHolder.imageView.setImageResource(R.drawable.iv_hlj2);
		}else {
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(mHolder.imageView, dto.icon2, null, 0);
		}

		AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT);
		params.width = width/3;
		params.height = width/3-30;
		convertView.setLayoutParams(params);
		
		return convertView;
	}

}
