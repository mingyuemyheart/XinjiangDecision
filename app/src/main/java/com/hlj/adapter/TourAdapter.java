package com.hlj.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hlj.common.ColumnData;
import com.squareup.picasso.Picasso;

import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 旅游气象
 */
public class TourAdapter extends BaseAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private List<ColumnData> mArrayList;

	private final class ViewHolder {
		ImageView imageView;
		TextView tvName;
	}

	public TourAdapter(Context context, List<ColumnData> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_tour, null);
			mHolder = new ViewHolder();
			mHolder.imageView = convertView.findViewById(R.id.imageView);
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		ColumnData dto = mArrayList.get(position);

		if (dto.name != null) {
			mHolder.tvName.setText(dto.name);
		}

		if (!TextUtils.isEmpty(dto.icon)) {
			Picasso.get().load(dto.icon).into(mHolder.imageView);
		} else {
			mHolder.imageView.setImageResource(R.drawable.icon_no_bitmap);
		}

		return convertView;
	}

}
