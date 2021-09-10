package com.hlj.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hlj.dto.WarningDto;

import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 预警统计
 */
public class WarningStatisticAdapter extends BaseAdapter {
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<WarningDto> mArrayList;
	
	private final class ViewHolder {
		TextView tvWarning,tvPro,tvCity,tvDis;
	}
	
	public WarningStatisticAdapter(Context context, List<WarningDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_warning_statistic, null);
			mHolder = new ViewHolder();
			mHolder.tvWarning = convertView.findViewById(R.id.tvWarning);
			mHolder.tvPro = convertView.findViewById(R.id.tvPro);
			mHolder.tvCity = convertView.findViewById(R.id.tvCity);
			mHolder.tvDis = convertView.findViewById(R.id.tvDis);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		WarningDto dto = mArrayList.get(position);
		mHolder.tvWarning.setText(dto.colorName);
		mHolder.tvPro.setText(dto.proCount);
		mHolder.tvCity.setText(dto.cityCount);
		mHolder.tvDis.setText(dto.disCount);

        if (dto.colorName.contains("共")) {
			mHolder.tvWarning.setTextColor(mContext.getResources().getColor(R.color.text_color3));
			mHolder.tvWarning.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			mHolder.tvPro.setTextColor(mContext.getResources().getColor(R.color.text_color3));
			mHolder.tvPro.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			mHolder.tvCity.setTextColor(mContext.getResources().getColor(R.color.text_color3));
			mHolder.tvCity.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			mHolder.tvDis.setTextColor(mContext.getResources().getColor(R.color.text_color3));
			mHolder.tvDis.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        }else if (dto.colorName.contains("红")) {
        	convertView.setBackgroundResource(R.drawable.bg_corner_warning_red_press);
			mHolder.tvWarning.setTextColor(Color.WHITE);
			if (TextUtils.equals(dto.colorName, "红0")) {
				mHolder.tvWarning.setBackgroundResource(R.drawable.bg_corner_warning_red_press);
			} else {
				mHolder.tvWarning.setBackgroundResource(R.drawable.bg_corner_warning_red);
			}
			if (TextUtils.equals(dto.proCount, "0")) {
				mHolder.tvPro.setTextColor(mContext.getResources().getColor(R.color.text_color2));
			} else {
				mHolder.tvPro.setBackgroundResource(R.drawable.bg_corner_warning_red_left);
			}
			if (TextUtils.equals(dto.cityCount, "0")) {
				mHolder.tvCity.setTextColor(mContext.getResources().getColor(R.color.text_color2));
			} else {
				mHolder.tvCity.setBackgroundResource(R.drawable.bg_corner_warning_red_middle);
			}
			if (TextUtils.equals(dto.disCount, "0")) {
				mHolder.tvDis.setTextColor(mContext.getResources().getColor(R.color.text_color2));
			} else {
				mHolder.tvDis.setBackgroundResource(R.drawable.bg_corner_warning_red_right);
			}
		}else if (dto.colorName.contains("橙")) {
			convertView.setBackgroundResource(R.drawable.bg_corner_warning_orange_press);
			mHolder.tvWarning.setTextColor(Color.WHITE);
			if (TextUtils.equals(dto.colorName, "橙0")) {
				mHolder.tvWarning.setBackgroundResource(R.drawable.bg_corner_warning_orange_press);
			} else {
				mHolder.tvWarning.setBackgroundResource(R.drawable.bg_corner_warning_orange);
			}
			if (TextUtils.equals(dto.proCount, "0")) {
				mHolder.tvPro.setTextColor(mContext.getResources().getColor(R.color.text_color2));
			} else {
				mHolder.tvPro.setBackgroundResource(R.drawable.bg_corner_warning_orange_left);
			}
			if (TextUtils.equals(dto.cityCount, "0")) {
				mHolder.tvCity.setTextColor(mContext.getResources().getColor(R.color.text_color2));
			} else {
				mHolder.tvCity.setBackgroundResource(R.drawable.bg_corner_warning_orange_middle);
			}
			if (TextUtils.equals(dto.disCount, "0")) {
				mHolder.tvDis.setTextColor(mContext.getResources().getColor(R.color.text_color2));
			} else {
				mHolder.tvDis.setBackgroundResource(R.drawable.bg_corner_warning_orange_right);
			}
		}else if (dto.colorName.contains("黄")) {
			convertView.setBackgroundResource(R.drawable.bg_corner_warning_yellow_press);
			mHolder.tvWarning.setTextColor(Color.WHITE);
			if (TextUtils.equals(dto.colorName, "黄0")) {
				mHolder.tvWarning.setBackgroundResource(R.drawable.bg_corner_warning_yellow_press);
			} else {
				mHolder.tvWarning.setBackgroundResource(R.drawable.bg_corner_warning_yellow);
			}
			if (TextUtils.equals(dto.proCount, "0")) {
				mHolder.tvPro.setTextColor(mContext.getResources().getColor(R.color.text_color2));
			} else {
				mHolder.tvPro.setBackgroundResource(R.drawable.bg_corner_warning_yellow_left);
			}
			if (TextUtils.equals(dto.cityCount, "0")) {
				mHolder.tvCity.setTextColor(mContext.getResources().getColor(R.color.text_color2));
			} else {
				mHolder.tvCity.setBackgroundResource(R.drawable.bg_corner_warning_yellow_middle);
			}
			if (TextUtils.equals(dto.disCount, "0")) {
				mHolder.tvDis.setTextColor(mContext.getResources().getColor(R.color.text_color2));
			} else {
				mHolder.tvDis.setBackgroundResource(R.drawable.bg_corner_warning_yellow_right);
			}
		}else if (dto.colorName.contains("蓝")) {
			convertView.setBackgroundResource(R.drawable.bg_corner_warning_blue_press);
			mHolder.tvWarning.setTextColor(Color.WHITE);
			if (TextUtils.equals(dto.colorName, "蓝0")) {
				mHolder.tvWarning.setBackgroundResource(R.drawable.bg_corner_warning_blue_press);
			} else {
				mHolder.tvWarning.setBackgroundResource(R.drawable.bg_corner_warning_blue);
			}
			if (TextUtils.equals(dto.proCount, "0")) {
				mHolder.tvPro.setTextColor(mContext.getResources().getColor(R.color.text_color2));
			} else {
				mHolder.tvPro.setBackgroundResource(R.drawable.bg_corner_warning_blue_left);
			}
			if (TextUtils.equals(dto.cityCount, "0")) {
				mHolder.tvCity.setTextColor(mContext.getResources().getColor(R.color.text_color2));
			} else {
				mHolder.tvCity.setBackgroundResource(R.drawable.bg_corner_warning_blue_middle);
			}
			if (TextUtils.equals(dto.disCount, "0")) {
				mHolder.tvDis.setTextColor(mContext.getResources().getColor(R.color.text_color2));
			} else {
				mHolder.tvDis.setBackgroundResource(R.drawable.bg_corner_warning_blue_right);
			}
		}else if (dto.colorName.contains("未知")) {
			convertView.setBackgroundResource(R.drawable.bg_corner_warning_gray_press);
			mHolder.tvWarning.setTextColor(Color.WHITE);
			if (TextUtils.equals(dto.colorName, "未知0")) {
				mHolder.tvWarning.setBackgroundResource(R.drawable.bg_corner_warning_gray_press);
			} else {
				mHolder.tvWarning.setBackgroundResource(R.drawable.bg_corner_warning_gray);
			}
			if (TextUtils.equals(dto.proCount, "0")) {
				mHolder.tvPro.setTextColor(mContext.getResources().getColor(R.color.text_color2));
			} else {
				mHolder.tvPro.setBackgroundResource(R.drawable.bg_corner_warning_gray_left);
			}
			if (TextUtils.equals(dto.cityCount, "0")) {
				mHolder.tvCity.setTextColor(mContext.getResources().getColor(R.color.text_color2));
			} else {
				mHolder.tvCity.setBackgroundResource(R.drawable.bg_corner_warning_gray_middle);
			}
			if (TextUtils.equals(dto.disCount, "0")) {
				mHolder.tvDis.setTextColor(mContext.getResources().getColor(R.color.text_color2));
			} else {
				mHolder.tvDis.setBackgroundResource(R.drawable.bg_corner_warning_gray_right);
			}
		}
		
		return convertView;
	}

}
