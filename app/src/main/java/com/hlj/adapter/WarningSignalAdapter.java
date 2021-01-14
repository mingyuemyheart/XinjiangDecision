package com.hlj.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hlj.common.CONST;
import com.hlj.dto.AgriDto;
import com.hlj.activity.WebviewCssActivity;
import shawn.cxwl.com.hlj.R;

public class WarningSignalAdapter extends BaseAdapter implements OnClickListener{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<AgriDto> mArrayList = new ArrayList<AgriDto>();
	
	private final class ViewHolder{
		TextView tvName;
		TextView tvBlue;
		TextView tvYellow;
		TextView tvOrange;
		TextView tvRed;
	}
	
	private ViewHolder mHolder = null;
	
	public WarningSignalAdapter(Context context, List<AgriDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.warning_signal_cell, null);
			mHolder = new ViewHolder();
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			mHolder.tvBlue = (TextView) convertView.findViewById(R.id.tvBlue);
			mHolder.tvBlue.setOnClickListener(this);
			mHolder.tvYellow = (TextView) convertView.findViewById(R.id.tvYellow);
			mHolder.tvYellow.setOnClickListener(this);
			mHolder.tvOrange = (TextView) convertView.findViewById(R.id.tvOrange);
			mHolder.tvOrange.setOnClickListener(this);
			mHolder.tvRed = (TextView) convertView.findViewById(R.id.tvRed);
			mHolder.tvRed.setOnClickListener(this);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		mHolder.tvBlue.setTag(position);
		mHolder.tvYellow.setTag(position);
		mHolder.tvOrange.setTag(position);
		mHolder.tvRed.setTag(position);
		
		AgriDto dto = mArrayList.get(position);
		mHolder.tvName.setText(dto.name);
		mHolder.tvBlue.setText(dto.blue);
		mHolder.tvYellow.setText(dto.yellow);
		mHolder.tvOrange.setText(dto.orange);
		mHolder.tvRed.setText(dto.red);
		
		return convertView;
	}

	@Override
	public void onClick(View v) {
		int index = (Integer) v.getTag();
		AgriDto data = mArrayList.get(index);
		String baseUrl = data.dataUrl.substring(0, data.dataUrl.length()-9);
		Intent intent = new Intent(mContext, WebviewCssActivity.class);
		switch (v.getId()) {
		case R.id.tvBlue:
			if (!TextUtils.isEmpty(data.blue)) {
				intent.putExtra(CONST.ACTIVITY_NAME, data.title);
				baseUrl = baseUrl + data.warningType + data.blueCode + ".html";
				intent.putExtra(CONST.WEB_URL, baseUrl);
				mContext.startActivity(intent);
			}
			break;
		case R.id.tvYellow:
			if (!TextUtils.isEmpty(data.blue)) {
				intent.putExtra(CONST.ACTIVITY_NAME, data.title);
				baseUrl = baseUrl + data.warningType + data.yellowCode + ".html";
				intent.putExtra(CONST.WEB_URL, baseUrl);
				mContext.startActivity(intent);
			}
			break;
		case R.id.tvOrange:
			if (!TextUtils.isEmpty(data.blue)) {
				intent.putExtra(CONST.ACTIVITY_NAME, data.title);
				baseUrl = baseUrl + data.warningType + data.orangeCode + ".html";
				intent.putExtra(CONST.WEB_URL, baseUrl);
				mContext.startActivity(intent);
			}
			break;
		case R.id.tvRed:
			if (!TextUtils.isEmpty(data.blue)) {
				intent.putExtra(CONST.ACTIVITY_NAME, data.title);
				baseUrl = baseUrl + data.warningType + data.redCode + ".html";
				intent.putExtra(CONST.WEB_URL, baseUrl);
				mContext.startActivity(intent);
			}
		break;

		default:
			break;
		}
	}

}
