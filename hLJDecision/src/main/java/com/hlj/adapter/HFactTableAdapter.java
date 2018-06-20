package com.hlj.adapter;


/**
 * 降水实况、气温实况、风向风速实况、相对湿度分析列表
 */

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hlj.dto.AgriDto;

import java.util.List;

import shawn.cxwl.com.hlj.R;

public class HFactTableAdapter extends BaseAdapter{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<AgriDto> mArrayList;
	
	private final class ViewHolder{
		TextView tvTitle;
	}
	
	private ViewHolder mHolder = null;
	
	public HFactTableAdapter(Context context, List<AgriDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.hadapter_fact_table, null);
			mHolder = new ViewHolder();
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		AgriDto dto = mArrayList.get(position);
		mHolder.tvTitle.setText(dto.title);
		if (TextUtils.isEmpty(dto.showType)) {
			mHolder.tvTitle.setBackgroundColor(mContext.getResources().getColor(R.color.light_gray));
			mHolder.tvTitle.setTextColor(mContext.getResources().getColor(R.color.text_color3));
			mHolder.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
		}else {
			mHolder.tvTitle.setBackgroundResource(R.drawable.selector_detail);
			mHolder.tvTitle.setTextColor(mContext.getResources().getColor(R.color.text_color4));
			mHolder.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
		}
		
		return convertView;
	}

}
