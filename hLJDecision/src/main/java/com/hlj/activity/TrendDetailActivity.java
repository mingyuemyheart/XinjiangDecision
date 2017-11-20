package com.hlj.activity;

/**
 * 趋势详情
 */

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hlj.utils.CommonUtil;
import com.hlj.dto.RangeDto;
import com.hlj.dto.TrendDto;
import com.hlj.view.HumidityView;
import com.hlj.view.PressureView;
import com.hlj.view.RainFallView;
import com.hlj.view.TemperatureView;
import com.hlj.view.WindSpeedView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants.Language;
import cn.com.weather.listener.AsyncResponseHandler;
import shawn.cxwl.com.hlj.decision.R;

public class TrendDetailActivity extends BaseActivity implements OnClickListener{

	private Context mContext = null;
	private LinearLayout llBack = null;//返回按钮
	private TextView tvTitle = null;
	private LinearLayout llContainer1 = null;
	private LinearLayout llContainer2 = null;
	private LinearLayout llContainer3 = null;
	private LinearLayout llContainer4 = null;
	private LinearLayout llContainer5 = null;
	private int width = 0;
	private float density = 0;
	private LinearLayout llMain = null;
	private RangeDto data = null;
	private TextView tvException1 = null;
	private TextView tvException2 = null;
	private TextView tvException3 = null;
	private TextView tvException4 = null;
	private TextView tvException5 = null;
	private HorizontalScrollView hScroll1 = null;
	private HorizontalScrollView hScroll2 = null;
	private HorizontalScrollView hScroll3 = null;
	private HorizontalScrollView hScroll4 = null;
	private HorizontalScrollView hScroll5 = null;
	private LinearLayout llContent = null;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("HH");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hactivity_trend_detail);
		mContext = this;
		showDialog();
		initWidget();
	}

	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		llContainer1 = (LinearLayout) findViewById(R.id.llContainer1);
		llContainer2 = (LinearLayout) findViewById(R.id.llContainer2);
		llContainer3 = (LinearLayout) findViewById(R.id.llContainer3);
		llContainer4 = (LinearLayout) findViewById(R.id.llContainer4);
		llContainer5 = (LinearLayout) findViewById(R.id.llContainer5);
		llMain = (LinearLayout) findViewById(R.id.llMain);
		tvException1 = (TextView) findViewById(R.id.tvException1);
		tvException2 = (TextView) findViewById(R.id.tvException2);
		tvException3 = (TextView) findViewById(R.id.tvException3);
		tvException4 = (TextView) findViewById(R.id.tvException4);
		tvException5 = (TextView) findViewById(R.id.tvException5);
		hScroll1 = (HorizontalScrollView) findViewById(R.id.hScroll1);
		hScroll2 = (HorizontalScrollView) findViewById(R.id.hScroll2);
		hScroll3 = (HorizontalScrollView) findViewById(R.id.hScroll3);
		hScroll4 = (HorizontalScrollView) findViewById(R.id.hScroll4);
		hScroll5 = (HorizontalScrollView) findViewById(R.id.hScroll5);
		llContent = (LinearLayout) findViewById(R.id.llContent);

		int hour = Integer.valueOf(sdf1.format(new Date()));
		if (hour >= 5 && hour < 18) {
			llContent.setBackgroundResource(R.drawable.bg_forecast_day_big);
		}else {
			llContent.setBackgroundResource(R.drawable.bg_forecast_night_big);
		}
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		density = dm.density;
		
		data = getIntent().getExtras().getParcelable("data");
		if (data != null) {
			if (!TextUtils.isEmpty(data.cityName)) {
				tvTitle.setText(data.cityName + "-" + data.areaName);
			}else {
				tvTitle.setText(data.areaName);
			}
			getWeatherInfo(data.cityId);
		}

	}
	
	/**
	 * 获取天气数据
	 */
	private void getWeatherInfo(String cityId) {
		if (cityId != null) {
			WeatherAPI.getWeather2(mContext, cityId, Language.ZH_CN, new AsyncResponseHandler() {
				@Override
				public void onComplete(Weather content) {
					super.onComplete(content);
					if (content != null) {
						//实况信息
						JSONObject object = content.getWeatherFactInfo();
						try {
							int time = 0;//发布时间
							if (!object.isNull("l7")) {
								String[] temp = object.getString("l7").split(":");
								time = Integer.valueOf(temp[0]);
							}
							
							TrendDto dto = new TrendDto();
							if (!object.isNull("l1")) {
								String[] temperatures = object.getString("l1").split("\\|");
								for (int i = 0; i < temperatures.length; i++) {
									TrendDto h = new TrendDto();
									h.temp = Integer.valueOf(temperatures[i]);
									dto.tempList.add(h);
								}
								
								TemperatureView temperatureView = new TemperatureView(mContext);
								temperatureView.setData(dto.tempList, time);
								llContainer1.addView(temperatureView, (int)(CommonUtil.dip2px(mContext, width/density*2)), (int)(CommonUtil.dip2px(mContext, 160)));
								new Handler().post(new Runnable() {
									@Override
									public void run() {
										hScroll1.scrollTo((int)(CommonUtil.dip2px(mContext, width/density*2)), 0);
									}
								});
							}
							if (!object.isNull("l2")) {
								String[] humiditys = object.getString("l2").split("\\|");
								for (int i = 0; i < humiditys.length; i++) {
									TrendDto h = new TrendDto();
									h.humidity = Integer.valueOf(humiditys[i]);
									dto.humidityList.add(h);
								}
								
								HumidityView humidityView = new HumidityView(mContext);
								humidityView.setData(dto.humidityList, time);
								llContainer2.addView(humidityView, (int)(CommonUtil.dip2px(mContext, width/density*2)), (int)(CommonUtil.dip2px(mContext, 160)));
								new Handler().post(new Runnable() {
									@Override
									public void run() {
										hScroll2.scrollTo((int)(CommonUtil.dip2px(mContext, width/density*2)), 0);
									}
								});
							}
							if (!object.isNull("l6")) {
								String[] rainFalls = object.getString("l6").split("\\|");
								for (int i = 0; i < rainFalls.length; i++) {
									TrendDto r = new TrendDto();
									r.rainFall = Float.valueOf(rainFalls[i]);
									dto.rainFallList.add(r);
								}
								
								RainFallView rainFallView = new RainFallView(mContext);
								rainFallView.setData(dto.rainFallList, time);
								llContainer3.addView(rainFallView, (int)(CommonUtil.dip2px(mContext, width/density*2)), (int)(CommonUtil.dip2px(mContext, 160)));
								new Handler().post(new Runnable() {
									@Override
									public void run() {
										hScroll3.scrollTo((int)(CommonUtil.dip2px(mContext, width/density*2)), 0);
									}
								});
							}
							if (!object.isNull("l11")) {
								String[] windSpeeds = object.getString("l11").split("\\|");
								String[] windDir = object.getString("l4").split("\\|");
								for (int i = 0; i < windSpeeds.length; i++) {
									TrendDto w = new TrendDto();
									w.windSpeed = Float.valueOf(windSpeeds[i]);
									w.windDir = Integer.valueOf(windDir[i]);
									dto.windSpeedList.add(w);
								}
								
								WindSpeedView windSpeedView = new WindSpeedView(mContext);
								windSpeedView.setData(dto.windSpeedList, time);
								llContainer4.addView(windSpeedView, (int)(CommonUtil.dip2px(mContext, width/density*2)), (int)(CommonUtil.dip2px(mContext, 160)));
								new Handler().post(new Runnable() {
									@Override
									public void run() {
										hScroll4.scrollTo((int)(CommonUtil.dip2px(mContext, width/density*2)), 0);
									}
								});
							}
							if (!object.isNull("l10")) {
								String[] pressures = object.getString("l10").split("\\|");
								for (int i = 0; i < pressures.length; i++) {
									TrendDto p = new TrendDto();
									p.pressure = Integer.valueOf(pressures[i]);
									dto.pressureList.add(p);
								}
								
								PressureView pressureView = new PressureView(mContext);
								pressureView.setData(dto.pressureList, time);
								llContainer5.addView(pressureView, (int)(CommonUtil.dip2px(mContext, width/density*2)), (int)(CommonUtil.dip2px(mContext, 160)));
								new Handler().post(new Runnable() {
									@Override
									public void run() {
										hScroll5.scrollTo((int)(CommonUtil.dip2px(mContext, width/density*2)), 0);
									}
								});
							}
							
							llMain.setVisibility(View.VISIBLE);
							
						} catch (JSONException e) {
							e.printStackTrace();
						}
						
						cancelDialog();
						llMain.setVisibility(View.VISIBLE);
					}
				}
				
				@Override
				public void onError(Throwable error, String content) {
					super.onError(error, content);
					cancelDialog();
					Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.llBack) {
			finish();
		}
	}
}
