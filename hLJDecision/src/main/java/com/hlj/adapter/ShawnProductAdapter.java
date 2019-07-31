package com.hlj.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlj.common.ColumnData;
import com.hlj.view.RoundTransform;
import com.squareup.picasso.Picasso;

import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 实况监测等模块
 */
public class ShawnProductAdapter extends BaseAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private List<ColumnData> mArrayList;
	private int width;
	private float density;
	
	private final class ViewHolder{
		TextView tvName;
		ImageView icon;
	}
	
	public ShawnProductAdapter(Context context, List<ColumnData> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		density = dm.density;
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
			convertView = mInflater.inflate(R.layout.shawn_adapter_product, null);
			mHolder = new ViewHolder();
			mHolder.tvName = convertView.findViewById(R.id.tvName);
			mHolder.icon = convertView.findViewById(R.id.icon);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		ColumnData dto = mArrayList.get(position);

		if (!TextUtils.isEmpty(dto.name)) {
			mHolder.tvName.setText(dto.name);
		}

		if (!TextUtils.isEmpty(dto.icon)) {
			Picasso.get().load(dto.icon).transform(new RoundTransform((int)(5*density))).into(mHolder.icon);
		}
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mHolder.icon.getLayoutParams();
		params.width = (width-(int)(30*density))/2;
		params.height = (width-(int)(30*density))/2*3/4;
		mHolder.icon.setLayoutParams(params);

		mHolder.icon.setBackgroundResource(R.drawable.shawn_bg_corner_border);
		mHolder.icon.setPadding(3,3,3,3);

		return convertView;
	}

}
