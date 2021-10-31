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

import net.tsz.afinal.FinalBitmap;

import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 旅游气象-智慧观景台
 */
public class TourObserveAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater mInflater;
	private List<NewsDto> mArrayList;

	private final class ViewHolder {
		ImageView imageView,ivRoute;
		TextView tvTitle,tvTime;
	}

	public TourObserveAdapter(Context context, List<NewsDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_tour_observe, null);
			mHolder = new ViewHolder();
			mHolder.imageView = convertView.findViewById(R.id.imageView);
			mHolder.ivRoute = convertView.findViewById(R.id.ivRoute);
			mHolder.tvTitle = convertView.findViewById(R.id.tvTitle);
			mHolder.tvTime = convertView.findViewById(R.id.tvTime);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		NewsDto dto = mArrayList.get(position);
		
		if (dto.title != null) {
			mHolder.tvTitle.setText(dto.title);
		}
		
		if (dto.time != null) {
			mHolder.tvTime.setText(dto.time+"更新");
		}

		mHolder.ivRoute.setImageResource(R.drawable.icon_observe);

		if (!TextUtils.isEmpty(dto.imgUrl)) {
			FinalBitmap finalBitmap = FinalBitmap.create(context);
			finalBitmap.display(mHolder.imageView, dto.imgUrl, null, (int)CommonUtil.dip2px(context, 5f));
		} else {
			mHolder.imageView.setImageResource(R.drawable.icon_no_bitmap);
		}

		ViewGroup.LayoutParams params = mHolder.imageView.getLayoutParams();
		params.width = (int)(CommonUtil.widthPixels(context)-CommonUtil.dip2px(context, 20f));
		params.height = (int)(CommonUtil.widthPixels(context)-CommonUtil.dip2px(context, 20f))*14/25;
		mHolder.imageView.setLayoutParams(params);
		
		return convertView;
	}

}
