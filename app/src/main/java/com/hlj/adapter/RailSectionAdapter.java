package com.hlj.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hlj.dto.FactDto;

import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 专业服务-铁路气象服务-实况数据-铁路段
 */
public class RailSectionAdapter extends BaseAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private List<FactDto> mArrayList;

	private final class ViewHolder {
		TextView tvName;
	}

	public RailSectionAdapter(Context context, List<FactDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_rail_section, null);
			mHolder = new ViewHolder();
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		FactDto dto = mArrayList.get(position);

		if (dto.name != null) {
			mHolder.tvName.setText(dto.name);
			if (dto.name.length() > 5) {
				mHolder.tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
			} else if (dto.name.length() > 3) {
				mHolder.tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
			}
		}

		return convertView;
	}

}
