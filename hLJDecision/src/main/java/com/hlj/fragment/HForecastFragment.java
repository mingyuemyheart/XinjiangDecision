package com.hlj.fragment;

/**
 * 天气预报
 */

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
import com.hlj.activity.HAirPolutionActivity;
import com.hlj.activity.HCityActivity;
import com.hlj.activity.HHeadWarningActivity;
import com.hlj.activity.HMinuteFallActivity;
import com.hlj.adapter.WeeklyForecastAdapter;
import com.hlj.common.CONST;
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
import shawn.cxwl.com.hlj.decision.R;

public class HForecastFragment extends Fragment implements OnClickListener, AMapLocationListener{
	
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
	private LinearLayout llPosition = null;
	private ListView mListView = null;//一周预报列表listview
	private WeeklyForecastAdapter mAdapter = null;
	private HorizontalScrollView hScrollView2 = null;
	private LinearLayout llContainer2 = null;//一周预报曲线容器
	private RelativeLayout reMain = null;//全屏
	private int width = 0;
	private List<WeatherDto> weeklyList = new ArrayList<>();
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.hfragment_forecast, container, false);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initRefreshLayout(view);
		initWidget(view);
		initListView(view);
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout(View view) {
		refreshLayout = (RefreshLayout) view.findViewById(R.id.refreshLayout);
		refreshLayout.setColor(com.hlj.common.CONST.color1, com.hlj.common.CONST.color2, com.hlj.common.CONST.color3, com.hlj.common.CONST.color4);
		refreshLayout.setMode(RefreshLayout.Mode.PULL_FROM_START);
		refreshLayout.setLoadNoFull(false);
		refreshLayout.setRefreshing(true);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				startLocation();
			}
		});
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget(View view) {
		//解决scrollView嵌套listview，动态计算listview高度后，自动滚动到屏幕底部
		tvPosition = (TextView) view.findViewById(R.id.tvPosition);
		tvPosition.setFocusable(true);
		tvPosition.setFocusableInTouchMode(true);
		tvPosition.requestFocus();
		tvTime = (TextView) view.findViewById(R.id.tvTime);
		tvRain = (TextView) view.findViewById(R.id.tvRain);
		tvRain.setOnClickListener(this);
		ivSwitcher = (ImageView) view.findViewById(R.id.ivSwitcher);
		ivSwitcher.setOnClickListener(this);
		tvTemp = (TextView) view.findViewById(R.id.tvTemp);
		ivPhe = (ImageView) view.findViewById(R.id.ivPhe);
		tvPhe = (TextView) view.findViewById(R.id.tvPhe);
		ivAqi = (ImageView) view.findViewById(R.id.ivAqi);
		ivAqi.setOnClickListener(this);
		tvAqi = (TextView) view.findViewById(R.id.tvAqi);
		tvAqi.setOnClickListener(this);
		ivWind = (ImageView) view.findViewById(R.id.ivWind);
		tvWind = (TextView) view.findViewById(R.id.tvWind);
		llContainer1 = (LinearLayout) view.findViewById(R.id.llContainer1);
		hScrollView2 = (HorizontalScrollView) view.findViewById(R.id.hScrollView2);
		llContainer2 = (LinearLayout) view.findViewById(R.id.llContainer2);
		reMain = (RelativeLayout) view.findViewById(R.id.reMain);
		llPosition = (LinearLayout) view.findViewById(R.id.llPosition);
		llPosition.setOnClickListener(this);
		tvDay1 = (TextView) view.findViewById(R.id.tvDay1);
		ivPhe1 = (ImageView) view.findViewById(R.id.ivPhe1);
		tvTemp1 = (TextView) view.findViewById(R.id.tvTemp1);
		tvPhe1 = (TextView) view.findViewById(R.id.tvPhe1);
		tvDay2 = (TextView) view.findViewById(R.id.tvDay2);
		ivPhe2 = (ImageView) view.findViewById(R.id.ivPhe2);
		tvTemp2 = (TextView) view.findViewById(R.id.tvTemp2);
		tvPhe2 = (TextView) view.findViewById(R.id.tvPhe2);
		tvMinetePrompt = (TextView) view.findViewById(R.id.tvMinetePrompt);
		llContainer3 = (LinearLayout) view.findViewById(R.id.llContainer3);
		ivClose = (ImageView) view.findViewById(R.id.ivClose);
		ivClose2 = (ImageView) view.findViewById(R.id.ivClose2);
		ivClose3 = (ImageView) view.findViewById(R.id.ivClose3);
		tvTime2 = (TextView) view.findViewById(R.id.tvTime2);
		reMinuteTitle = (RelativeLayout) view.findViewById(R.id.reMinuteTitle);
		reMinuteTitle.setOnClickListener(this);
		reMinuteContent = (RelativeLayout) view.findViewById(R.id.reMinuteContent);
		reMinuteContent.setOnClickListener(this);
		reLocation = (RelativeLayout) view.findViewById(R.id.reLocation);
		llFact = (LinearLayout) view.findViewById(R.id.llFact);
		llDay1 = (LinearLayout) view.findViewById(R.id.llDay1);
		llDay2 = (LinearLayout) view.findViewById(R.id.llDay2);
		ivWarning = (ImageView) view.findViewById(R.id.ivWarning);
		ivWarning.setOnClickListener(this);
		llHourlyTitle = (LinearLayout) view.findViewById(R.id.llHourlyTitle);
		llHourlyTitle.setOnClickListener(this);
		hScrollView = (HorizontalScrollView) view.findViewById(R.id.hScrollView);
		reFifteenTitle = (RelativeLayout) view.findViewById(R.id.reFifteenTitle);
		reFifteenTitle.setOnClickListener(this);
		llFifteenContent = (LinearLayout) view.findViewById(R.id.llFifteenContent);

		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;

		int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		reLocation.measure(w, h);
		llFact.measure(w, h);
		RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) llFact.getLayoutParams();
		params2.setMargins(0, dm.heightPixels-(int)(120*dm.density)-reLocation.getMeasuredHeight()-llFact.getMeasuredHeight(), 0, 0);
		llFact.setLayoutParams(params2);

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

		startLocation();
	}
	
	/**
	 * 开始定位
	 */
	private void startLocation() {
        mLocationOption = new AMapLocationClientOption();//初始化定位参数
        mLocationClient = new AMapLocationClient(getActivity());//初始化定位
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setOnceLocation(true);//设置是否只定位一次,默认为false
        mLocationOption.setWifiActiveScan(true);//设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setMockEnable(false);//设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setInterval(2000);//设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
        mLocationClient.setLocationListener(this);
        mLocationClient.startLocation();//启动定位
	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (amapLocation != null && amapLocation.getErrorCode() == 0) {
        	String district = amapLocation.getDistrict();
        	String street = amapLocation.getStreet()+amapLocation.getStreetNum();
			tvPosition.setText(district+street);

			OkHttpHourRain(amapLocation.getLongitude(), amapLocation.getLatitude());
        	getWeatherInfo(amapLocation.getLongitude(), amapLocation.getLatitude());
        }
	}
	
	/**
	 * 获取天气数据
	 */
	private void getWeatherInfo(double lng, double lat) {
		WeatherAPI.getGeo(getActivity(),String.valueOf(lng), String.valueOf(lat), new AsyncResponseHandler(){
			@Override
			public void onComplete(JSONObject content) {
				super.onComplete(content);
				if (!content.isNull("geo")) {
					try {
						JSONObject geoObj = content.getJSONObject("geo");
						if (!geoObj.isNull("id")) {
							String cityId = geoObj.getString("id");
							if (cityId != null) {
								WeatherAPI.getWeather2(getActivity(), cityId, Language.ZH_CN, new AsyncResponseHandler() {
									@Override
									public void onComplete(Weather content) {
										super.onComplete(content);
										if (content != null) {
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
													Drawable drawable;
													if (hour >= 5 && hour < 18) {
														drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
													}else {
														drawable = getResources().getDrawable(R.drawable.phenomenon_drawable_night);
													}
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
														tvAqi.setText(aqi+" "+WeatherUtil.getAqi(getActivity(), Integer.valueOf(aqi)));
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
																	Bitmap bitmap = null;
																	if (dto.color.equals(CONST.blue[0])) {
																		bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+dto.type+CONST.blue[1]+CONST.imageSuffix);
																		if (bitmap == null) {
																			bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.blue[1]+CONST.imageSuffix);
																		}
																	}else if (dto.color.equals(CONST.yellow[0])) {
																		bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+dto.type+CONST.yellow[1]+CONST.imageSuffix);
																		if (bitmap == null) {
																			bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.yellow[1]+CONST.imageSuffix);
																		}
																	}else if (dto.color.equals(CONST.orange[0])) {
																		bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+dto.type+CONST.orange[1]+CONST.imageSuffix);
																		if (bitmap == null) {
																			bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.orange[1]+CONST.imageSuffix);
																		}
																	}else if (dto.color.equals(CONST.red[0])) {
																		bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+dto.type+CONST.red[1]+CONST.imageSuffix);
																		if (bitmap == null) {
																			bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.red[1]+CONST.imageSuffix);
																		}
																	}
																	ivWarning.setImageBitmap(bitmap);
																	ivWarning.setVisibility(View.VISIBLE);
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
												CubicView cubicView = new CubicView(getActivity());
												cubicView.setData(hourlyList);
												llContainer1.removeAllViews();
												llContainer1.addView(cubicView, width*2, (int)(CommonUtil.dip2px(getActivity(), 300)));
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
												WeeklyView weeklyView = new WeeklyView(getActivity());
												weeklyView.setData(weeklyList);
												llContainer2.removeAllViews();
												llContainer2.addView(weeklyView, width*2, (int) CommonUtil.dip2px(getActivity(), 320));
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
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public void onError(Throwable error, String content) {
				super.onError(error, content);
			}
		});
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView(View view) {
		mListView = (ListView) view.findViewById(R.id.listView);
		mAdapter = new WeeklyForecastAdapter(getActivity(), weeklyList);
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
				getActivity().runOnUiThread(new Runnable() {
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
													tvMinetePrompt.setText("2小时分钟降雪预报");
												}else {
													tvMinetePrompt.setText("2小时分钟降水预报");
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

											MinuteFallView minuteFallView = new MinuteFallView(getActivity());
											minuteFallView.setData(minuteList, tvRain.getText().toString());
											llContainer3.removeAllViews();
											llContainer3.addView(minuteFallView, width, (int)(CommonUtil.dip2px(getActivity(), 150)));
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
		case R.id.llPosition:
			startActivity(new Intent(getActivity(), HCityActivity.class));
			break;
		case R.id.ivAqi:
		case R.id.tvAqi:
			Intent intent = new Intent(getActivity(), HAirPolutionActivity.class);
			intent.putExtra(CONST.ACTIVITY_NAME, "空气质量");
			startActivity(intent);
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
			intent = new Intent(getActivity(), HMinuteFallActivity.class);
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
//			if (llFifteenContent.getVisibility() == View.VISIBLE) {
//				llFifteenContent.setVisibility(View.GONE);
//				ivClose3.setImageResource(R.drawable.iv_open);
//				ivSwitcher.setVisibility(View.INVISIBLE);
//			}else {
//				llFifteenContent.setVisibility(View.VISIBLE);
//				ivClose3.setImageResource(R.drawable.iv_close);
//				ivSwitcher.setVisibility(View.VISIBLE);
//			}
			break;
		case R.id.ivWarning:
			intent = new Intent(getActivity(), HHeadWarningActivity.class);
			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList("warningList", (ArrayList<? extends Parcelable>) warningList);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.reMinuteContent:
			intent = new Intent(getActivity(), HMinuteFallActivity.class);
			intent.putExtra(CONST.ACTIVITY_NAME, getString(R.string.minute_fall));
			startActivity(intent);
			break;

		default:
			break;
		}
	}

}