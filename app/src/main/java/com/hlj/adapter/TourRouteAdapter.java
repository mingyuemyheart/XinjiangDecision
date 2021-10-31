package com.hlj.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hlj.dto.NewsDto;
import com.hlj.utils.CommonUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 旅游气象-旅游路线
 */
public class TourRouteAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater mInflater;
	private List<NewsDto> mArrayList;

	private final class ViewHolder {
		ImageView imageView,ivRoute;
		TextView tvTitle,tvLength,tvPlayTime,tvRoute,divider,tvStartEnd;
	}

	public TourRouteAdapter(Context context, List<NewsDto> mArrayList) {
		this.context = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			convertView = mInflater.inflate(R.layout.adapter_tour_route, null);
			mHolder = new ViewHolder();
			mHolder.imageView = convertView.findViewById(R.id.imageView);
			mHolder.ivRoute = convertView.findViewById(R.id.ivRoute);
			mHolder.tvTitle = convertView.findViewById(R.id.tvTitle);
			mHolder.tvLength = convertView.findViewById(R.id.tvLength);
			mHolder.tvPlayTime = convertView.findViewById(R.id.tvPlayTime);
			mHolder.tvRoute = convertView.findViewById(R.id.tvRoute);
			mHolder.divider = convertView.findViewById(R.id.divider);
			mHolder.tvStartEnd = convertView.findViewById(R.id.tvStartEnd);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		NewsDto dto = mArrayList.get(position);
		
		if (dto.title != null) {
			mHolder.tvTitle.setText(dto.title);
		}
		
		if (dto.length != null) {
			mHolder.tvLength.setText(dto.length);
			mHolder.tvLength.setBackgroundResource(R.drawable.corner_left_right_gray);
		}

		if (dto.playTime != null) {
			mHolder.tvPlayTime.setText("适合游玩时间："+dto.playTime);
			mHolder.tvPlayTime.setBackgroundResource(R.drawable.corner_left_right_green_line);
		}

		mHolder.ivRoute.setImageResource(R.drawable.icon_route);
		mHolder.tvRoute.setText("起-终");
		mHolder.divider.setBackgroundColor(context.getResources().getColor(R.color.gray));

		if (dto.startEnd != null) {
			mHolder.tvStartEnd.setText(dto.startEnd);
		}
		
		if (!TextUtils.isEmpty(dto.imgUrl)) {
			Picasso.get().load(dto.imgUrl).into(mHolder.imageView);
		} else {
			mHolder.imageView.setImageResource(R.drawable.icon_no_bitmap);
		}

		ViewGroup.LayoutParams params = mHolder.imageView.getLayoutParams();
		params.width = (int)(CommonUtil.widthPixels(context)-CommonUtil.dip2px(context, 20f));
		params.height = (int)(CommonUtil.widthPixels(context)-CommonUtil.dip2px(context, 20f))*26/71;
		mHolder.imageView.setLayoutParams(params);
		
		return convertView;
	}

}
