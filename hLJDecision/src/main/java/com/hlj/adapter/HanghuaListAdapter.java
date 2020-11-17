package com.hlj.adapter;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hlj.dto.StationMonitorDto;
import com.hlj.utils.OkHttpUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

/**
 * 航华列表
 */
public class HanghuaListAdapter extends BaseAdapter{

	private LayoutInflater mInflater;
	private List<StationMonitorDto> mArrayList;
	private Handler mUIHandler = new Handler();

	private final class ViewHolder {
		TextView tvTitle,tvTime,tvValue;
	}

	public HanghuaListAdapter(Context context, List<StationMonitorDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_hanghua_list, null);
			mHolder = new ViewHolder();
			mHolder.tvTitle = convertView.findViewById(R.id.tvTitle);
			mHolder.tvTime = convertView.findViewById(R.id.tvTime);
			mHolder.tvValue = convertView.findViewById(R.id.tvValue);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		StationMonitorDto dto = mArrayList.get(position);
		
		if (!TextUtils.isEmpty(dto.name)) {
			mHolder.tvTitle.setText(dto.name);
		}

		if (!TextUtils.isEmpty(dto.time)) {
			mHolder.tvTime.setText(dto.time);
		}
		okHttpValue(dto.lat, dto.lng, mHolder.tvValue);

		return convertView;
	}

	private void okHttpValue(final double lat, final double lng, final TextView textView) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String url = String.format("http://decision-admin.tianqi.cn/Home/work2019/hlj_gethanghuoData?lat=%s&lon=%s", lat, lng);
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(@NotNull Call call, @NotNull IOException e) {
					}
					@Override
					public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						mUIHandler.post(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("msg")) {
											String msg = obj.getString("msg");
											if (!TextUtils.isEmpty(msg)) {
												textView.setText(msg);
											}
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
						});
					}
				});
			}
		}).start();
	}

}
