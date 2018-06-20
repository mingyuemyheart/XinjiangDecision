package com.hlj.adapter;

/**
 * 气温预报、雾霾预报、降温大风沙尘预报
 */

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hlj.dto.AgriDto;
import com.hlj.utils.OkHttpUtil;

import net.tsz.afinal.FinalBitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

public class HTemperatureForecastAdapter extends BaseAdapter{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<AgriDto> mArrayList;
	private Activity activity;
	
	private final class ViewHolder{
		TextView tvName;
		ImageView imageView;
	}
	
	private ViewHolder mHolder = null;
	
	public HTemperatureForecastAdapter(Context context, List<AgriDto> mArrayList, Activity activity) {
		this.activity = activity;
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
			convertView = mInflater.inflate(R.layout.hadapter_temperature_forecast, null);
			mHolder = new ViewHolder();
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		AgriDto dto = mArrayList.get(position);
		mHolder.tvName.setText(dto.name);
		OkHttpImage(dto.dataUrl, mHolder.imageView);
		Log.d("name", dto.name);
		
		return convertView;
	}
	
	/**
	 * 获取详情
	 */
	private void OkHttpImage(final String url, final ImageView imageView) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("imgs")) {
											JSONArray array = obj.getJSONArray("imgs");
											JSONObject itemObj = array.getJSONObject(0);
											String icon = itemObj.getString("i");
											if (!TextUtils.isEmpty(icon)) {
												FinalBitmap finalBitmap = FinalBitmap.create(mContext);
												finalBitmap.display(imageView, icon, null, 0);
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
