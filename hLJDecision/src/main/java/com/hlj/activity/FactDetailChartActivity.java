package com.hlj.activity;

/**
 * 实况详情图标
 */

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.hlj.dto.FactDto;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.OkHttpUtil;
import com.hlj.view.FactRainView;
import com.hlj.view.FactTempView;
import com.hlj.view.FactWindView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

public class FactDetailChartActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private List<FactDto> rainList = new ArrayList<>();
	private List<FactDto> tempList = new ArrayList<>();
	private List<FactDto> windList = new ArrayList<>();
	private LinearLayout llContainer1,llContainer2,llContainer3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fact_detail_chart);
		mContext = this;
		showDialog();
		initWidget();
	}
	
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		llContainer1 = findViewById(R.id.llContainer1);
		llContainer2 = findViewById(R.id.llContainer2);
		llContainer3 = findViewById(R.id.llContainer3);

		FactDto data = getIntent().getParcelableExtra("data");
		if (data != null) {
			if (!TextUtils.isEmpty(data.stationName)) {
				tvTitle.setText(data.stationName);
			}
			if (!TextUtils.isEmpty(data.stationCode)) {
				OkHttpStationInfo("http://decision-171.tianqi.cn/api/heilj/dates/getone48?id="+data.stationCode);
			}
		}
	}
	
	/**
	 * 获取站点数据
	 */
	private void OkHttpStationInfo(String url) {
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
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						cancelDialog();
						if (!TextUtils.isEmpty(result)) {
							try {
								JSONObject obj = new JSONObject(result);
								if (!obj.isNull("list")) {
									JSONArray array = obj.getJSONArray("list");
									rainList.clear();
									tempList.clear();
									windList.clear();
									for (int i = 0; i < array.length(); i++) {
										FactDto dto = new FactDto();
										JSONObject itemObj = array.getJSONObject(i);
										if (!itemObj.isNull("Datetime")) {
											dto.factTime = itemObj.getString("Datetime");
										}
										String value = "";
										if (!itemObj.isNull("JS")) {
											value = itemObj.getString("JS");
											dto.factRain = Float.parseFloat(value);
										}
										if (!value.contains("99999")) {
											rainList.add(dto);
										}

										value = "";
										if (!itemObj.isNull("WD")) {
											value = itemObj.getString("WD");
											dto.factTemp = Float.parseFloat(value);
										}
										if (!value.contains("99999")) {
											tempList.add(dto);
										}

										value = "";
										if (!itemObj.isNull("FS")) {
											value = itemObj.getString("FS");
											dto.factWind = Float.parseFloat(value);
										}
										if (!value.contains("99999")) {
											windList.add(dto);
										}
									}

									FactRainView rainView = new FactRainView(mContext);
									rainView.setData(rainList);
									llContainer1.removeAllViews();
									int viewWidth1 = rainList.size() <= 25 ? CommonUtil.widthPixels(mContext)*2 : CommonUtil.widthPixels(mContext)*4;
									llContainer1.addView(rainView, viewWidth1, CommonUtil.heightPixels(mContext)/3);

									FactTempView tempView = new FactTempView(mContext);
									tempView.setData(tempList);
									llContainer2.removeAllViews();
									int viewWidth2 = rainList.size() <= 25 ? CommonUtil.widthPixels(mContext)*2 : CommonUtil.widthPixels(mContext)*4;
									llContainer2.addView(tempView, viewWidth2, CommonUtil.heightPixels(mContext)/3);

									FactWindView windView = new FactWindView(mContext);
									windView.setData(windList);
									llContainer3.removeAllViews();
									int viewWidth3 = windList.size() <= 25 ? CommonUtil.widthPixels(mContext)*2 : CommonUtil.widthPixels(mContext)*4;
									llContainer3.addView(windView, viewWidth3, CommonUtil.heightPixels(mContext)/3);
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}
	
}
