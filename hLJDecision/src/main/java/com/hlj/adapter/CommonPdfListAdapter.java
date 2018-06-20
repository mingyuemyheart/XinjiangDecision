package com.hlj.adapter;

/**
 * 电力气象服务（一周天气预报、短期天气预报、全省重大天气预报、省电力预报、旬月回顾与展望）
 * 铁路气象服务（站点预报、旬预报、一周天气预报、全省重大天气预报、短时预警预报）
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

public class CommonPdfListAdapter extends BaseAdapter{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<AgriDto> mArrayList;
	
	private final class ViewHolder{
		TextView tvTitle;
		ImageView imageView;
		TextView tvTime;
	}
	
	private ViewHolder mHolder = null;
	
	public CommonPdfListAdapter(Context context, List<AgriDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_common_pdf_list, null);
			mHolder = new ViewHolder();
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		AgriDto dto = mArrayList.get(position);
		
		if (!TextUtils.isEmpty(dto.title)) {
			mHolder.tvTitle.setText(dto.title);
		}
		
		if (!TextUtils.isEmpty(dto.time)) {
			mHolder.tvTime.setText(dto.time);
		}
		
		if (TextUtils.isEmpty(dto.icon)) {
			mHolder.imageView.setImageResource(R.drawable.iv_hlj1);
		}else {
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(mHolder.imageView, dto.icon, null, 0);
		}
		
		return convertView;
	}

}
