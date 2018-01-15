package com.hlj.activity;

/**
 * 天气详情
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.hlj.activity.HCityActivity;
import com.hlj.activity.HMinuteFallActivity;
import com.hlj.activity.HHeadWarningActivity;
import com.hlj.adapter.WeeklyForecastAdapter;
import com.hlj.common.CONST;
import com.hlj.dto.CityDto;
import com.hlj.dto.WarningDto;
import com.hlj.dto.WeatherDto;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.OkHttpUtil;
import com.hlj.utils.WeatherUtil;
import com.hlj.view.CubicView;
import com.hlj.view.MinuteFallView;
import com.hlj.view.RefreshLayout;
import com.hlj.view.RefreshLayout.OnRefreshListener;
import com.hlj.view.WeeklyView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants.Language;
import cn.com.weather.listener.AsyncResponseHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

public class HWeatherDetailActivity extends BaseActivity implements OnClickListener{

	private Context mContext = null;
	private RelativeLayout reTitle = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView tvPosition = null;//定位地点
	private TextView tvTime = null;//更新时间
	private TextView tvRain = null;//下雨、下雪信息
	private TextView tvTemp = null;//实况温度
	private ImageView ivPhe = null;
	private TextView tvPhe = null;//天气显现对应的图标
	private ImageView ivAqi = null;
	private TextView tvAqi = null;
	private ImageView ivWind = null;
	private TextView tvWind = null;//风向
	private LinearLayout llContainer1 = null;//加载逐小时预报曲线容器
	private ImageView ivSwitcher = null;//列表和趋势开关
	private ListView mListView = null;//一周预报列表listview
	private WeeklyForecastAdapter mAdapter = null;
	private HorizontalScrollView hScrollView2 = null;
	private LinearLayout llContainer2 = null;//一周预报曲线容器
	private RelativeLayout reMain = null;//全屏
	private int width = 0;
	private List<WeatherDto> weeklyList = new ArrayList<>();
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private SimpleDateFormat sdf1 = new SimpleDateFormat("HH");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private int hour = 0;

	private TextView tvDay1 = null;
	private ImageView ivPhe1 = null;
	private TextView tvTemp1 = null;
	private TextView tvPhe1 = null;
	private TextView tvDay2 = null;
	private ImageView ivPhe2 = null;
	private TextView tvTemp2 = null;
	private TextView tvPhe2 = null;
	private TextView tvMinetePrompt = null;
	private TextView tvTime2 = null;
	private ImageView ivClose, ivClose2, ivClose3;
	private LinearLayout llContainer3 = null;
	private RelativeLayout reMinuteTitle = null;
	private RelativeLayout reMinuteContent = null;
	private RelativeLayout reLocation = null;
	private LinearLayout llFact = null;
	private LinearLayout llDay1, llDay2;
	private ImageView ivWarning = null;
	private List<WarningDto> warningList = new ArrayList<>();
	private LinearLayout llHourlyTitle = null;
	private HorizontalScrollView hScrollView = null;
	private RelativeLayout reFifteenTitle = null;
	private LinearLayout llFifteenContent = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hactivity_weather_detail);
		mContext = this;
		initRefreshLayout();
		initWidget();
		initListView();
	}

	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
		refreshLayout.setColor(com.hlj.common.CONST.color1, com.hlj.common.CONST.color2, com.hlj.common.CONST.color3, com.hlj.common.CONST.color4);
		refreshLayout.setMode(RefreshLayout.Mode.PULL_FROM_START);
		refreshLayout.setLoadNoFull(false);
		refreshLayout.setRefreshing(true);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
	}

	/**
	 * 初始化控件
	 */
	private void initWidget() {
		reTitle = (RelativeLayout) findViewById(R.id.reTitle);
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		//解决scrollView嵌套listview，动态计算listview高度后，自动滚动到屏幕底部
		tvPosition = (TextView) findViewById(R.id.tvPosition);
		tvPosition.setFocusable(true);
		tvPosition.setFocusableInTouchMode(true);
		tvPosition.requestFocus();
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvRain = (TextView) findViewById(R.id.tvRain);
		tvRain.setOnClickListener(this);
		ivSwitcher = (ImageView) findViewById(R.id.ivSwitcher);
		ivSwitcher.setOnClickListener(this);
		tvTemp = (TextView) findViewById(R.id.tvTemp);
		ivPhe = (ImageView) findViewById(R.id.ivPhe);
		tvPhe = (TextView) findViewById(R.id.tvPhe);
		ivAqi = (ImageView) findViewById(R.id.ivAqi);
		tvAqi = (TextView) findViewById(R.id.tvAqi);
		ivWind = (ImageView) findViewById(R.id.ivWind);
		tvWind = (TextView) findViewById(R.id.tvWind);
		llContainer1 = (LinearLayout) findViewById(R.id.llContainer1);
		hScrollView2 = (HorizontalScrollView) findViewById(R.id.hScrollView2);
		llContainer2 = (LinearLayout) findViewById(R.id.llContainer2);
		reMain = (RelativeLayout) findViewById(R.id.reMain);
		tvDay1 = (TextView) findViewById(R.id.tvDay1);
		ivPhe1 = (ImageView) findViewById(R.id.ivPhe1);
		tvTemp1 = (TextView) findViewById(R.id.tvTemp1);
		tvPhe1 = (TextView) findViewById(R.id.tvPhe1);
		tvDay2 = (TextView) findViewById(R.id.tvDay2);
		ivPhe2 = (ImageView) findViewById(R.id.ivPhe2);
		tvTemp2 = (TextView) findViewById(R.id.tvTemp2);
		tvPhe2 = (TextView) findViewById(R.id.tvPhe2);
		tvMinetePrompt = (TextView) findViewById(R.id.tvMinetePrompt);
		llContainer3 = (LinearLayout) findViewById(R.id.llContainer3);
		ivClose = (ImageView) findViewById(R.id.ivClose);
		ivClose2 = (ImageView) findViewById(R.id.ivClose2);
		ivClose3 = (ImageView) findViewById(R.id.ivClose3);
		tvTime2 = (TextView) findViewById(R.id.tvTime2);
		reMinuteTitle = (RelativeLayout) findViewById(R.id.reMinuteTitle);
		reMinuteTitle.setOnClickListener(this);
		reMinuteContent = (RelativeLayout) findViewById(R.id.reMinuteContent);
		reLocation = (RelativeLayout) findViewById(R.id.reLocation);
		llFact = (LinearLayout) findViewById(R.id.llFact);
		llDay1 = (LinearLayout) findViewById(R.id.llDay1);
		llDay2 = (LinearLayout) findViewById(R.id.llDay2);
		ivWarning = (ImageView) findViewById(R.id.ivWarning);
		ivWarning.setOnClickListener(this);
		llHourlyTitle = (LinearLayout) findViewById(R.id.llHourlyTitle);
		llHourlyTitle.setOnClickListener(this);
		hScrollView = (HorizontalScrollView) findViewById(R.id.hScrollView);
		reFifteenTitle = (RelativeLayout) findViewById(R.id.reFifteenTitle);
		reFifteenTitle.setOnClickListener(this);
		llFifteenContent = (LinearLayout) findViewById(R.id.llFifteenContent);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;

//		int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//		int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//		reTitle.measure(w, h);
//		reLocation.measure(w, h);
//		llFact.measure(w, h);
//		RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) llFact.getLayoutParams();
//		params2.setMargins(0, dm.heightPixels-reTitle.getMeasuredHeight()-reLocation.getMeasuredHeight()-llFact.getMeasuredHeight(), 0, 0);
//		llFact.setLayoutParams(params2);

		hour = Integer.parseInt(sdf1.format(new Date()));
		if (hour >= 5 && hour < 18) {
			refreshLayout.setBackgroundResource(R.drawable.bg_forecast_day_big);
			tvAqi.setBackgroundColor(0x60338fb7);
			tvWind.setBackgroundColor(0x60338fb7);
			llDay1.setBackgroundColor(0xff338fb7);
			llDay2.setBackgroundColor(0xff338fb7);
		}else {
			refreshLayout.setBackgroundResource(R.drawable.bg_forecast_night_big);
			tvAqi.setBackgroundColor(0x602867ad);
			tvWind.setBackgroundColor(0x602867ad);
			llDay1.setBackgroundColor(0xff2867ad);
			llDay2.setBackgroundColor(0xff2867ad);
		}

		refresh();
	}

	private void refresh() {
		if (getIntent().hasExtra("data")) {
			CityDto data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				tvTitle.setText(data.areaName);
				tvPosition.setText(data.areaName);
				getWeatherInfo(data.cityId);
			}
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
						JSONObject city = content.getCityInfo();
						try {
							double lat = Double.valueOf(city.getString("c14"));
							double lng = Double.valueOf(city.getString("c13"));
							OkHttpHourRain(lng, lat);
						} catch (JSONException e) {
							e.printStackTrace();
						}

						//实况信息
						JSONObject object = content.getWeatherFactInfo();
						try {
							if (!object.isNull("l7")) {
								String time = object.getString("l7");
								if (time != null) {
									tvTime.setText(time + getString(R.string.update));
								}
							}
							if (!object.isNull("l5")) {
								String weatherCode = WeatherUtil.lastValue(object.getString("l5"));
								tvPhe.setText(getString(WeatherUtil.getWeatherId(Integer.valueOf(weatherCode))));
								Drawable drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
								drawable.setLevel(Integer.valueOf(weatherCode));
								ivPhe.setBackground(drawable);
							}
							if (!object.isNull("l1")) {
								String factTemp = WeatherUtil.lastValue(object.getString("l1"));
								tvTemp.setText(factTemp);
							}

							if (!object.isNull("l4")) {
								String windDir = WeatherUtil.lastValue(object.getString("l4"));
								String dir = getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir)));
								if (!object.isNull("l3")) {
									String windForce = WeatherUtil.lastValue(object.getString("l3"));
									String force = WeatherUtil.getFactWindForce(Integer.valueOf(windForce));
									tvWind.setText(dir+" "+force);
									if (TextUtils.equals(dir, "北风")) {
										ivWind.setRotation(0);
									}else if (TextUtils.equals(dir, "东北风")) {
										ivWind.setRotation(45);
									}else if (TextUtils.equals(dir, "东风")) {
										ivWind.setRotation(90);
									}else if (TextUtils.equals(dir, "东南风")) {
										ivWind.setRotation(135);
									}else if (TextUtils.equals(dir, "南风")) {
										ivWind.setRotation(180);
									}else if (TextUtils.equals(dir, "西南风")) {
										ivWind.setRotation(225);
									}else if (TextUtils.equals(dir, "西风")) {
										ivWind.setRotation(270);
									}else if (TextUtils.equals(dir, "西北风")) {
										ivWind.setRotation(315);
									}
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

						//空气质量
						JSONObject aqiObj = content.getAirQualityInfo();
						try {
							if (!aqiObj.isNull("k3")) {
								String aqi = WeatherUtil.lastValue(aqiObj.getString("k3"));
								if (!TextUtils.isEmpty(aqi)) {
									tvAqi.setText(aqi+" "+WeatherUtil.getAqi(mContext, Integer.valueOf(aqi)));
									ivAqi.setImageResource(WeatherUtil.getAqiIcon(Integer.valueOf(aqi)));
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

						//预警
						try {
							ivWarning.setVisibility(View.GONE);
							warningList.clear();
							JSONArray warningArray = content.getWarningInfo();
							if (warningArray != null && warningArray.length() > 0) {
								for (int j = 0; j < warningArray.length(); j++) {
									JSONObject warningObj = warningArray.getJSONObject(j);
									if (!warningObj.isNull("w11")) {
										WarningDto dto = new WarningDto();
										String html = warningObj.getString("w11");
										dto.html = html;
										if (!TextUtils.isEmpty(html) && html.contains("content2")) {
											dto.html = html.substring(html.indexOf("content2/")+"content2/".length(), html.length());
											String[] array = dto.html.split("-");
											String item0 = array[0];
											String item1 = array[1];
											String item2 = array[2];

											dto.item0 = item0;
											dto.provinceId = item0.substring(0, 2);
											dto.type = item2.substring(0, 5);
											dto.color = item2.substring(5, 7);
											dto.time = item1;
											String w1 = warningObj.getString("w1");
											String w3 = warningObj.getString("w3");
											String w5 = warningObj.getString("w5");
											String w7 = warningObj.getString("w7");
											dto.name = w1+w3+"发布"+w5+w7+"预警";
											warningList.add(dto);

											if (j == 0) {
												Bitmap bitmap = CommonUtil.getImageFromAssetsFile(mContext,"warning/"+dto.type+CONST.blue[1]+CONST.imageSuffix);
												if (bitmap != null) {
													ivWarning.setImageBitmap(bitmap);
													ivWarning.setVisibility(View.VISIBLE);
												}
											}
										}
									}
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

						//逐小时预报信息
						JSONArray hourlyArray = content.getHourlyFineForecast2();
						try {
							List<WeatherDto> hourlyList = new ArrayList<>();
							for (int i = 0; i < hourlyArray.length(); i++) {
								JSONObject itemObj = hourlyArray.getJSONObject(i);
								WeatherDto dto = new WeatherDto();
								dto.hourlyCode = Integer.valueOf(itemObj.getString("ja"));
								dto.hourlyTemp = Integer.valueOf(itemObj.getString("jb"));
								dto.hourlyTime = itemObj.getString("jf");
								dto.hourlyWindDirCode = Integer.valueOf(itemObj.getString("jc"));
								dto.hourlyWindForceCode = Integer.valueOf(itemObj.getString("jd"));
								hourlyList.add(dto);
							}

							//逐小时预报信息
							CubicView cubicView = new CubicView(mContext);
							cubicView.setData(hourlyList);
							llContainer1.removeAllViews();
							llContainer1.addView(cubicView, width*2, (int)(CommonUtil.dip2px(mContext, 300)));
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (NullPointerException e) {
							e.printStackTrace();
						}

						//一周预报信息
						try {
							weeklyList.clear();
							//这里只去一周预报，默认为15天，所以遍历7次
							for (int i = 1; i <= 15; i++) {
								WeatherDto dto = new WeatherDto();

								JSONArray weeklyArray = content.getWeatherForecastInfo(i);
								JSONObject weeklyObj = weeklyArray.getJSONObject(0);

								//晚上
								dto.lowPheCode = Integer.valueOf(weeklyObj.getString("fb"));
								dto.lowPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fb"))));
								dto.lowTemp = Integer.valueOf(weeklyObj.getString("fd"));

								//白天
								dto.highPheCode = Integer.valueOf(weeklyObj.getString("fa"));
								dto.highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fa"))));
								dto.highTemp = Integer.valueOf(weeklyObj.getString("fc"));

								if (hour >= 5 && hour < 18) {
									dto.windDir = Integer.valueOf(weeklyObj.getString("fe"));
									dto.windForce = Integer.valueOf(weeklyObj.getString("fg"));
									dto.windForceString = WeatherUtil.getDayWindForce(Integer.valueOf(dto.windForce));
								}else {
									dto.windDir = Integer.valueOf(weeklyObj.getString("ff"));
									dto.windForce = Integer.valueOf(weeklyObj.getString("fh"));
									dto.windForceString = WeatherUtil.getDayWindForce(Integer.valueOf(dto.windForce));
								}

								JSONArray timeArray =  content.getTimeInfo(i);
								JSONObject timeObj = timeArray.getJSONObject(0);
								dto.week = timeObj.getString("t4");//星期几
								dto.date = timeObj.getString("t1");//日期

								weeklyList.add(dto);

								if (i == 1) {
									tvDay1.setText("今天");
									Drawable drawable;
									if (hour >= 5 && hour < 18) {
										drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
										drawable.setLevel(dto.highPheCode);
										tvPhe1.setText(getString(WeatherUtil.getWeatherId(dto.highPheCode)));
									}else {
										drawable = getResources().getDrawable(R.drawable.phenomenon_drawable_night);
										drawable.setLevel(dto.lowPheCode);
										tvPhe1.setText(getString(WeatherUtil.getWeatherId(dto.lowPheCode)));
									}
									if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
										ivPhe1.setBackground(drawable);
									}else {
										ivPhe1.setBackgroundDrawable(drawable);
									}
									tvTemp1.setText(dto.highTemp+"/"+dto.lowTemp);
								}
								if (i == 2) {
									tvDay2.setText("明天");
									Drawable drawable;
									if (hour >= 5 && hour < 18) {
										drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
										drawable.setLevel(dto.highPheCode);
										tvPhe2.setText(getString(WeatherUtil.getWeatherId(dto.highPheCode)));
									}else {
										drawable = getResources().getDrawable(R.drawable.phenomenon_drawable_night);
										drawable.setLevel(dto.lowPheCode);
										tvPhe2.setText(getString(WeatherUtil.getWeatherId(dto.lowPheCode)));
									}
									if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
										ivPhe2.setBackground(drawable);
									}else {
										ivPhe2.setBackgroundDrawable(drawable);
									}
									tvTemp2.setText(dto.highTemp+"/"+dto.lowTemp);
								}
							}

							//一周预报列表
							if (mAdapter != null) {
								mAdapter.notifyDataSetChanged();
								CommonUtil.setListViewHeightBasedOnChildren(mListView);
							}

							//一周预报曲线
							WeeklyView weeklyView = new WeeklyView(mContext);
							weeklyView.setData(weeklyList);
							llContainer2.removeAllViews();
							llContainer2.addView(weeklyView, width*2, (int) CommonUtil.dip2px(mContext, 320));
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (NullPointerException e) {
							e.printStackTrace();
						}

						reMain.setVisibility(View.VISIBLE);
						refreshLayout.setRefreshing(false);
					}
				}

				@Override
				public void onError(Throwable error, String content) {
					super.onError(error, content);
				}
			});
		}
	}

	/**
	 * 初始化listview
	 */
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new WeeklyForecastAdapter(mContext, weeklyList);
		mListView.setAdapter(mAdapter);
		CommonUtil.setListViewHeightBasedOnChildren(mListView);
	}

	/**
	 * 异步加载一小时内降雨、或降雪信息
	 * @param lng
	 * @param lat
	 */
	private void OkHttpHourRain(double lng, double lat) {
		String url = "http://api.caiyunapp.com/v2/HyTVV5YAkoxlQ3Zd/"+lng+","+lat+"/forecast";
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
						if (!TextUtils.isEmpty(result)) {
							try {
								JSONObject object = new JSONObject(result);
								if (!object.isNull("server_time")) {
									long t = object.getLong("server_time");
									Date date = new Date(t*1000);
									tvTime2.setText(sdf2.format(date)+"发布");
								}
								if (!object.isNull("result")) {
									JSONObject obj = object.getJSONObject("result");
									if (!obj.isNull("minutely")) {
										JSONObject objMin = obj.getJSONObject("minutely");
										if (!objMin.isNull("description")) {
											String rain = objMin.getString("description");
											if (!TextUtils.isEmpty(rain)) {
												tvRain.setText(rain.replace(getString(R.string.little_caiyun), ""));
												if (tvRain.getText().toString().contains("雪")) {
													tvMinetePrompt.setText("1小时分钟降雪预报");
												}else {
													tvMinetePrompt.setText("1小时分钟降水预报");
												}

												if (reMinuteContent.getVisibility() == View.VISIBLE) {
													tvRain.setVisibility(View.VISIBLE);
												}else {
													tvRain.setVisibility(View.GONE);
												}
											}else {
												tvRain.setVisibility(View.GONE);
											}
										}
										if (!objMin.isNull("precipitation_2h")) {
											JSONArray array = objMin.getJSONArray("precipitation_2h");
											int size = array.length();
											List<WeatherDto> minuteList = new ArrayList<>();
											for (int i = 0; i < size; i++) {
												WeatherDto dto = new WeatherDto();
												dto.minuteFall = (float) array.getDouble(i);
//										dto.minuteFall = new Random().nextFloat();
												minuteList.add(dto);
											}

											MinuteFallView minuteFallView = new MinuteFallView(mContext);
											minuteFallView.setData(minuteList, tvRain.getText().toString());
											llContainer3.removeAllViews();
											llContainer3.addView(minuteFallView, width, (int)(CommonUtil.dip2px(mContext, 150)));
										}
									}
								}
							} catch (JSONException e1) {
								e1.printStackTrace();
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
			case R.id.ivSwitcher:
				if (mListView.getVisibility() == View.VISIBLE) {
					ivSwitcher.setImageResource(R.drawable.iv_trend);
					mListView.setVisibility(View.GONE);
					hScrollView2.setVisibility(View.VISIBLE);
				}else {
					ivSwitcher.setImageResource(R.drawable.iv_list);
					mListView.setVisibility(View.VISIBLE);
					hScrollView2.setVisibility(View.GONE);
				}
				CommonUtil.setListViewHeightBasedOnChildren(mListView);
				break;
			case R.id.tvRain:
				Intent intent = new Intent(mContext, HMinuteFallActivity.class);
				intent.putExtra(CONST.ACTIVITY_NAME, getString(R.string.minute_fall));
				startActivity(intent);
				break;
			case R.id.reMinuteTitle:
				if (reMinuteContent.getVisibility() == View.VISIBLE) {
					tvRain.setVisibility(View.GONE);
					reMinuteContent.setVisibility(View.GONE);
					ivClose.setImageResource(R.drawable.iv_open);
				}else {
					tvRain.setVisibility(View.VISIBLE);
					reMinuteContent.setVisibility(View.VISIBLE);
					ivClose.setImageResource(R.drawable.iv_close);
				}
				break;
			case R.id.llHourlyTitle:
				if (hScrollView.getVisibility() == View.VISIBLE) {
					hScrollView.setVisibility(View.GONE);
					ivClose2.setImageResource(R.drawable.iv_open);
				}else {
					hScrollView.setVisibility(View.VISIBLE);
					ivClose2.setImageResource(R.drawable.iv_close);
				}
				break;
			case R.id.reFifteenTitle:
				if (llFifteenContent.getVisibility() == View.VISIBLE) {
					llFifteenContent.setVisibility(View.GONE);
					ivClose3.setImageResource(R.drawable.iv_open);
					ivSwitcher.setVisibility(View.INVISIBLE);
				}else {
					llFifteenContent.setVisibility(View.VISIBLE);
					ivClose3.setImageResource(R.drawable.iv_close);
					ivSwitcher.setVisibility(View.VISIBLE);
				}
				break;
			case R.id.ivWarning:
				intent = new Intent(mContext, HHeadWarningActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("warningList", (ArrayList<? extends Parcelable>) warningList);
				intent.putExtras(bundle);
				startActivity(intent);
				break;

			default:
				break;
		}
	}

}