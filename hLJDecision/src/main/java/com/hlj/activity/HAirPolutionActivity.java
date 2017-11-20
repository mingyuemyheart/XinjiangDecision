package com.hlj.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.hlj.common.CONST;
import com.hlj.dto.AirPolutionDto;
import com.hlj.dto.AqiDto;
import com.hlj.manager.RainManager;
import com.hlj.manager.XiangJiManager;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.OkHttpUtil;
import com.hlj.view.AqiQualityView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

/**
 * 空气污染
 * @author shawn_sun
 *
 */

@SuppressLint("SimpleDateFormat")
public class HAirPolutionActivity extends BaseActivity implements OnClickListener, OnMarkerClickListener,
        OnMapClickListener, OnCameraChangeListener, AMapLocationListener {
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private MapView mMapView = null;
	private AMap aMap = null;
	private List<AirPolutionDto> provinceList = new ArrayList<>();//省级
	private List<AirPolutionDto> cityList = new ArrayList<>();//市级
	private List<AirPolutionDto> districtList = new ArrayList<>();//县级
	private TextView tvName = null;
	private TextView tvTime = null;
	private TextView tvAqiCount = null;
	private TextView tvAqi = null;
	private TextView tvPrompt = null;
	private RelativeLayout reRank = null;
	private TextView tvRank = null;
	private RelativeLayout rePm2_5 = null;
	private TextView tvPm2_5 = null;
	private RelativeLayout rePm10 = null;
	private TextView tvPm10 = null;
	private LinearLayout llContent = null;
	private RelativeLayout reLegend = null;
	private LinearLayout llTop = null;
	private LinearLayout llCity = null;
	private TextView tvCity = null;
	public final static String SANX_DATA_99 = "sanx_data_99";//加密秘钥名称
	public final static String APPID = "f63d329270a44900";//机密需要用到的AppId
	private float zoom = 5.5f;
	private boolean isClick = false;//判断是否点击
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHH");
	private HorizontalScrollView hScrollView = null;
	private LinearLayout llContainer = null;
	private List<AqiDto> aqiList = new ArrayList<>();
	private List<AqiDto> factAqiList = new ArrayList<>();//实况aqi数据
	private List<AqiDto> foreAqiList = new ArrayList<>();//预报aqi数据
	private int maxAqi = 0, minAqi = 0;
	private String aqiDate = null;
	private ImageView ivExpand = null;
	private Configuration configuration = null;
	private List<Marker> markerList = new ArrayList<>();
	private LatLng leftlatlng = null;
	private LatLng rightLatlng = null;
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private String locationCityId = "101050101";//默认为哈尔滨
	private boolean isFirstEnter = true;//是否为第一次进入

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hactivity_air_polution);
		mContext = this;
		showDialog();
		initMap(savedInstanceState);
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvName = (TextView) findViewById(R.id.tvName);
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvAqiCount = (TextView) findViewById(R.id.tvAqiCount);
		tvAqi = (TextView) findViewById(R.id.tvAqi);
		tvPrompt = (TextView) findViewById(R.id.tvPrompt);
		reRank = (RelativeLayout) findViewById(R.id.reRank);
		tvRank = (TextView) findViewById(R.id.tvRank);
		rePm2_5 = (RelativeLayout) findViewById(R.id.rePm2_5);
		tvPm2_5 = (TextView) findViewById(R.id.tvPm2_5);
		rePm10 = (RelativeLayout) findViewById(R.id.rePm10);
		tvPm10 = (TextView) findViewById(R.id.tvPm10);
		llContent = (LinearLayout) findViewById(R.id.llContent);
		llTop = (LinearLayout) findViewById(R.id.llTop);
		llContainer = (LinearLayout) findViewById(R.id.llContainer);
		reLegend = (RelativeLayout) findViewById(R.id.reLegend);
		llCity = (LinearLayout) findViewById(R.id.llCity);
		tvCity = (TextView) findViewById(R.id.tvCity);
		hScrollView = (HorizontalScrollView) findViewById(R.id.hScrollView);
		ivExpand = (ImageView) findViewById(R.id.ivExpand);
		ivExpand.setOnClickListener(this);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}
		
		configuration = getResources().getConfiguration();

		startLocation();
	}

	/**
	 * 开始定位
	 */
	private void startLocation() {
		mLocationOption = new AMapLocationClientOption();//初始化定位参数
		mLocationClient = new AMapLocationClient(mContext);//初始化定位
		mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
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
			getCityId(amapLocation.getLongitude(), amapLocation.getLatitude());
		}
	}

	private void getCityId(double lng, double lat) {
		WeatherAPI.getGeo(mContext, lng+"", lat+"", new AsyncResponseHandler() {
			@Override
			public void onComplete(JSONObject content) {
				super.onComplete(content);
				if (!content.isNull("geo")) {
					try {
						JSONObject geo = content.getJSONObject("geo");
						if (!geo.isNull("id")) {
							locationCityId = geo.getString("id");
							if (!locationCityId.startsWith("10105")) {
								locationCityId = "101050101";
							}
							OkHttpRank();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	/**
	 * 初始化地图
	 */
	private void initMap(Bundle bundle) {
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mMapView.getMap();
		}
		LatLng guizhouLatLng = new LatLng(46.102915,128.121040);
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(guizhouLatLng, zoom));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMarkerClickListener(this);
		aMap.setOnMapClickListener(this);
		aMap.setOnCameraChangeListener(this);

		CommonUtil.drawHLJJson(mContext, aMap);
		CommonUtil.drawJGDQJson(mContext, aMap);
	}
	
	@Override
	public void onCameraChange(CameraPosition arg0) {
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Point leftPoint = new Point(0, dm.heightPixels);
		Point rightPoint = new Point(dm.widthPixels, 0);
		leftlatlng = aMap.getProjection().fromScreenLocation(leftPoint);
		rightLatlng = aMap.getProjection().fromScreenLocation(rightPoint);
		
		if (zoom == arg0.zoom && isClick == true) {//如果是地图缩放级别不变，并且点击就不做处理
			isClick = false;
			return;
		}
		
		zoom = arg0.zoom;
		removeMarkers();
		if (arg0.zoom <= 7.0f) {
			addMarker(provinceList);
			addMarker(cityList);
		}if (arg0.zoom > 7.0f) {
			addMarker(provinceList);
			addMarker(cityList);
			addMarker(districtList);
		}
	}
	
	/**
	 * 加密请求字符串
	 * @return
	 */
	private String getSecretUrl() {
		String URL = "http://scapi.weather.com.cn/weather/getaqiobserve";//空气污染
		String sysdate = RainManager.getDate(Calendar.getInstance(), "yyyyMMddHHmm");//系统时间
		StringBuffer buffer = new StringBuffer();
		buffer.append(URL);
		buffer.append("?");
		buffer.append("date=").append(sysdate);
		buffer.append("&");
		buffer.append("appid=").append(APPID);
		
		String key = RainManager.getKey(SANX_DATA_99, buffer.toString());
		buffer.delete(buffer.lastIndexOf("&"), buffer.length());
		
		buffer.append("&");
		buffer.append("appid=").append(APPID.substring(0, 6));
		buffer.append("&");
		buffer.append("key=").append(key.substring(0, key.length() - 3));
		String result = buffer.toString();
		return result;
	}
	
	/**
	 * 获取空气质量排行
	 */
	private void OkHttpRank() {
		OkHttpUtil.enqueue(new Request.Builder().url(getSecretUrl()).build(), new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {

			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					return;
				}
				String result = response.body().string();
				if (result != null) {
					parseStationInfo(result, "level1", provinceList);
					parseStationInfo(result, "level2", cityList);
					parseStationInfo(result, "level3", districtList);

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							addMarker(provinceList);
							addMarker(cityList);
							cancelDialog();

							isFirstEnter = true;
							clickMarker(locationCityId);
						}
					});
				}
			}
		});
	}
	
	/**
	 * 解析数据
	 */
	private void parseStationInfo(String result, String level, List<AirPolutionDto> list) {
		list.clear();
		try {
			JSONObject obj = new JSONObject(result.toString());
			if (!obj.isNull("data")) {
				String time = obj.getString("time");
				JSONObject dataObj = obj.getJSONObject("data");
				if (!dataObj.isNull(level)) {
					JSONArray array = dataObj.getJSONArray(level);
					for (int i = 0; i < array.length(); i++) {
						AirPolutionDto dto = new AirPolutionDto();
						JSONObject itemObj = array.getJSONObject(i);
						if (!itemObj.isNull("name")) {
							dto.name = itemObj.getString("name");
						}
						if (!itemObj.isNull("level")) {
							dto.level = itemObj.getString("level");
						}
						if (!itemObj.isNull("areaid")) {
							dto.areaId = itemObj.getString("areaid");
						}
						if (!itemObj.isNull("lat")) {
							dto.latitude = itemObj.getString("lat");
						}
						if (!itemObj.isNull("lon")) {
							dto.longitude = itemObj.getString("lon");
						}
						if (!itemObj.isNull("aqi")) {
							dto.aqi = itemObj.getString("aqi");
						}
						if (!itemObj.isNull("pm10")) {
							dto.pm10 = itemObj.getString("pm10");
						}
						if (!itemObj.isNull("pm2_5")) {
							dto.pm2_5 = itemObj.getString("pm2_5");
						}
						if (!itemObj.isNull("rank")) {
							dto.rank = itemObj.getInt("rank");
						}
						dto.time = time;

						if (dto.areaId.startsWith("10105")) {
							list.add(dto);
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 给marker添加文字
	 * @param name 城市名称
	 * @return
	 */
	private View getTextBitmap(String name, String aqi) {      
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.airpolution_item, null);
		if (view == null) {
			return null;
		}
		TextView tvName = (TextView) view.findViewById(R.id.tvName);
		ImageView icon = (ImageView) view.findViewById(R.id.icon);
		if (!TextUtils.isEmpty(name) && name.length() > 2) {
			name = name.substring(0, 2)+"\n"+name.substring(2, name.length());
		}
		tvName.setText(name);
		int value = Integer.valueOf(aqi);
		icon.setImageResource(getMarker(value));
		return view;
	}
	
	/**
	 * 根据aqi数据获取相对应的marker图标
	 * @param value
	 * @return
	 */
	private int getMarker(int value) {
		int drawable = -1;
		if (value >= 0 && value <= 50) {
			drawable = R.drawable.iv_air1;
		}else if (value >= 51 && value < 100) {
			drawable = R.drawable.iv_air2;
		}else if (value >= 101 && value < 150) {
			drawable = R.drawable.iv_air3;
		}else if (value >= 151 && value < 200) {
			drawable = R.drawable.iv_air4;
		}else if (value >= 201 && value < 300) {
			drawable = R.drawable.iv_air5;
		}else if (value >= 301) {
			drawable = R.drawable.iv_air6;
		}
		return drawable;
	}
	
	private void markerExpandAnimation(Marker marker) {
		ScaleAnimation animation = new ScaleAnimation(0,1,0,1);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(300);
		marker.setAnimation(animation);
		marker.startAnimation();
	}
	
	private void markerColloseAnimation(Marker marker) {
		ScaleAnimation animation = new ScaleAnimation(1,0,1,0);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(300);
		marker.setAnimation(animation);
		marker.startAnimation();
	}
	
	private void removeMarkers() {
		for (int i = 0; i < markerList.size(); i++) {
			Marker marker = markerList.get(i);
			markerColloseAnimation(marker);
			marker.remove();
		}
		markerList.clear();
	}
	
	/**
	 * 添加marker
	 */
	private void addMarker(List<AirPolutionDto> list) {
		if (list.isEmpty()) {
			return;
		}
		
		for (int i = 0; i < list.size(); i++) {
			AirPolutionDto dto = list.get(i);
			double lat = Double.valueOf(dto.latitude);
			double lng = Double.valueOf(dto.longitude);
			if (leftlatlng == null || rightLatlng == null) {
				MarkerOptions options = new MarkerOptions();
				options.title(list.get(i).areaId);
				options.anchor(0.5f, 0.5f);
				options.position(new LatLng(lat, lng));
				options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(dto.name, dto.aqi)));
				Marker marker = aMap.addMarker(options);
				markerList.add(marker);
				markerExpandAnimation(marker);
			}else {
				if (lat > leftlatlng.latitude && lat < rightLatlng.latitude && lng > leftlatlng.longitude && lng < rightLatlng.longitude) {
					MarkerOptions options = new MarkerOptions();
					options.title(list.get(i).areaId);
					options.anchor(0.5f, 0.5f);
					options.position(new LatLng(lat, lng));
					options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(dto.name, dto.aqi)));
					Marker marker = aMap.addMarker(options);
					markerList.add(marker);
					markerExpandAnimation(marker);
				}
			}
		}
	}
	
	@Override
	public void onMapClick(LatLng arg0) {
		hideAnimation(llContent);
	}
	
	/**
	 * 根据aqi值获取aqi的描述（优、良等）
	 * @param value
	 * @return
	 */
	private String getAqiDes(int value) {
		String aqi = null;
		if (value >= 0 && value <= 50) {
			aqi = getString(R.string.aqi_level1);
		}else if (value >= 51 && value < 100) {
			aqi = getString(R.string.aqi_level2);
		}else if (value >= 101 && value < 150) {
			aqi = getString(R.string.aqi_level3);
		}else if (value >= 151 && value < 200) {
			aqi = getString(R.string.aqi_level4);
		}else if (value >= 201 && value < 300) {
			aqi = getString(R.string.aqi_level5);
		}else if (value >= 301) {
			aqi = getString(R.string.aqi_level6);
		}
		return aqi;
	}
	
	/**
	 * 根据aqi值获取aqi的提示信息
	 * @param value
	 * @return
	 */
	private String getPrompt(int value) {
		String aqi = null;
		if (value >= 0 && value <= 50) {
			aqi = getString(R.string.aqi1_text);
		}else if (value >= 51 && value < 100) {
			aqi = getString(R.string.aqi2_text);
		}else if (value >= 101 && value < 150) {
			aqi = getString(R.string.aqi3_text);
		}else if (value >= 151 && value < 200) {
			aqi = getString(R.string.aqi4_text);
		}else if (value >= 201 && value < 300) {
			aqi = getString(R.string.aqi5_text);
		}else if (value >= 301) {
			aqi = getString(R.string.aqi6_text);
		}
		return aqi;
	}
	
	/**
	 * 根据aqi数据获取相对应的背景图标
	 * @param value
	 * @return
	 */
	private int getCicleBackground(int value) {
		int drawable = -1;
		if (value >= 0 && value <= 50) {
			drawable = R.drawable.circle_aqi_one;
		}else if (value >= 51 && value < 100) {
			drawable = R.drawable.circle_aqi_two;
		}else if (value >= 101 && value < 150) {
			drawable = R.drawable.circle_aqi_three;
		}else if (value >= 151 && value < 200) {
			drawable = R.drawable.circle_aqi_four;
		}else if (value >= 201 && value < 300) {
			drawable = R.drawable.circle_aqi_five;
		}else if (value >= 301) {
			drawable = R.drawable.circle_aqi_six;
		}
		return drawable;
	}
	
	/**
	 * 根据aqi数据获取相对应的背景图标
	 * @param value
	 * @return
	 */
	private int getCornerBackground(int value) {
		int drawable = -1;
		if (value >= 0 && value <= 50) {
			drawable = R.drawable.corner_aqi_one;
		}else if (value >= 51 && value < 100) {
			drawable = R.drawable.corner_aqi_two;
		}else if (value >= 101 && value < 150) {
			drawable = R.drawable.corner_aqi_three;
		}else if (value >= 151 && value < 200) {
			drawable = R.drawable.corner_aqi_four;
		}else if (value >= 201 && value < 300) {
			drawable = R.drawable.corner_aqi_five;
		}else if (value >= 301) {
			drawable = R.drawable.corner_aqi_six;
		}
		return drawable;
	}
	
	private void setValue(String areaId, List<AirPolutionDto> list) {
		for (int i = 0; i < list.size(); i++) {
			if (TextUtils.equals(areaId, list.get(i).areaId)) {
				tvName.setText(list.get(i).name);
				tvCity.setText(list.get(i).name+"空气质量指数（AQI）");
				tvAqiCount.setText(list.get(i).aqi);
				int value = Integer.valueOf(list.get(i).aqi);
				tvAqi.setText(getAqiDes(value));
				tvAqi.setBackgroundResource(getCornerBackground(value));
				if (value > 150) {
					tvAqi.setTextColor(getResources().getColor(R.color.white));
				}else {
					tvAqi.setTextColor(getResources().getColor(R.color.black));
				}
				tvPrompt.setText("温馨提示："+getPrompt(value));
				reRank.setBackgroundResource(getCicleBackground(value));
				tvRank.setText(list.get(i).rank+"");
				rePm2_5.setBackgroundResource(getCicleBackground(value));
				tvPm2_5.setText(list.get(i).pm2_5);
				rePm10.setBackgroundResource(getCicleBackground(value));
				tvPm10.setText(list.get(i).pm10);
				if (!TextUtils.isEmpty(list.get(i).time)) {
					try {
						tvTime.setText(sdf2.format(sdf1.parse(list.get(i).time)) + getString(R.string.update));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				break;
			}
		}
	} 
	
	/**
	 * 向上弹出动画
	 * @param layout
	 */
	private void showAnimation(final View layout) {
		if (layout.getVisibility() == View.VISIBLE) {
			return;
		}
		TranslateAnimation animation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 1f, 
				TranslateAnimation.RELATIVE_TO_SELF, 0);
		animation.setDuration(300);
		layout.startAnimation(animation);
		layout.setVisibility(View.VISIBLE);
		if (isFirstEnter) {
			ivExpand.setVisibility(View.GONE);
		}else {
			ivExpand.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * 向下隐藏动画
	 * @param layout
	 */
	private void hideAnimation(final View layout) {
		if (layout.getVisibility() == View.GONE) {
			return;
		}
		TranslateAnimation animation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 0, 
				TranslateAnimation.RELATIVE_TO_SELF, 1f);
		animation.setDuration(300);
		layout.startAnimation(animation);
		layout.setVisibility(View.GONE);
		ivExpand.setVisibility(View.GONE);
	}
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		isFirstEnter = false;
		clickMarker(marker.getTitle());
		return true;
	}

	private void clickMarker(String cityId) {
		showAnimation(llContent);
		isClick = true;

		setValue(cityId, provinceList);
		setValue(cityId, cityList);
		setValue(cityId, districtList);

		llContainer.removeAllViews();
		getWeatherInfo(cityId);
	}
	
	/**
	 * 获取实况信息、预报信息
	 */
	private void getWeatherInfo(String cityId) {
		if (TextUtils.isEmpty(cityId)) {
			return;
		}
		WeatherAPI.getWeather2(mContext, cityId, Language.ZH_CN, new AsyncResponseHandler() {
			@Override
			public void onComplete(Weather content) {
				super.onComplete(content);
				aqiList.clear();
				if (content != null) {
					//空气质量
					try {
						JSONObject obj = content.getAirQualityInfo();
						if (!obj.isNull("k3")) {
							String[] array = obj.getString("k3").split("\\|");
							factAqiList.clear();
							for (int i = 0; i < array.length; i++) {
								AqiDto data = new AqiDto();
								if (!TextUtils.isEmpty(array[i]) && !TextUtils.equals(array[i], "?")) {
									if (i == array.length-1) {
										data.aqi = tvAqiCount.getText().toString();
									}else {
										data.aqi = array[i];
									}
									factAqiList.add(data);
								}
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}

					//位置信息
					JSONObject city = content.getCityInfo();
					try {
						double lat = 0, lng = 0;
						if (!city.isNull("c14")) {
							lat = Double.valueOf(city.getString("c14"));
						}
						if (!city.isNull("c13")) {
							lng = Double.valueOf(city.getString("c13"));
						}
						OkHttpXiangJiAqi(lat, lng);
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
	 * 请求象辑aqi
	 */
	private void OkHttpXiangJiAqi(double lat, double lng) {
    	long timestamp = new Date().getTime();
    	String start1 = sdf3.format(timestamp);
    	String end1 = sdf3.format(timestamp+1000*60*60*24);
		String url = XiangJiManager.getXJSecretUrl(lng, lat, start1, end1, timestamp);
		OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {

			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					return;
				}
				String result = response.body().string();
				if (result != null) {
					try {
						JSONObject obj = new JSONObject(result.toString());
						if (!obj.isNull("reqTime")) {
							aqiDate = obj.getString("reqTime");
						}

						if (!obj.isNull("series")) {
							aqiList.clear();
							JSONArray array = obj.getJSONArray("series");
							foreAqiList.clear();
							for (int i = 0; i < array.length(); i++) {
								AqiDto data = new AqiDto();
								data.aqi = String.valueOf(array.get(i));
								foreAqiList.add(data);
							}
							aqiList.addAll(factAqiList);
							aqiList.addAll(foreAqiList);
						}

						if (aqiList.size() > 0) {
							try {
								if (!TextUtils.isEmpty(aqiList.get(0).aqi)) {
									maxAqi = Integer.valueOf(aqiList.get(0).aqi);
									minAqi = Integer.valueOf(aqiList.get(0).aqi);
									for (int i = 0; i < aqiList.size(); i++) {
										if (!TextUtils.isEmpty(aqiList.get(i).aqi)) {
											if (maxAqi <= Integer.valueOf(aqiList.get(i).aqi)) {
												maxAqi = Integer.valueOf(aqiList.get(i).aqi);
											}
											if (minAqi >= Integer.valueOf(aqiList.get(i).aqi)) {
												minAqi = Integer.valueOf(aqiList.get(i).aqi);
											}
										}
									}
									maxAqi = maxAqi + (50 - maxAqi%50);

									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											if (isFirstEnter) {
												hScrollView.setVisibility(View.GONE);
												ivExpand.setVisibility(View.GONE);
											}else {
												if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
													setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
													showPortrait();
													ivExpand.setImageResource(R.drawable.iv_expand_black);
												}else {
													setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
													showLandscape();
													ivExpand.setImageResource(R.drawable.iv_collose_black);
												}
											}
										}
									});

								}
							} catch (ArrayIndexOutOfBoundsException e) {
								e.printStackTrace();
							}
						}
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
	}
	
	private void showPortrait() {
		hScrollView.setVisibility(View.VISIBLE);
		ivExpand.setVisibility(View.VISIBLE);
		llTop.setVisibility(View.VISIBLE);
		llCity.setVisibility(View.GONE);
		AqiQualityView aqiView = new AqiQualityView(mContext);
		aqiView.setData(aqiList, aqiDate);
		int viewHeight = (int)(CommonUtil.dip2px(mContext, 180));
//		if (maxAqi <= 100) {
//			viewHeight = (int)(CommonUtil.dip2px(mContext, 150));
//		}else if (maxAqi > 100 && maxAqi <= 150) {
//			viewHeight = (int)(CommonUtil.dip2px(mContext, 200));
//		}else if (maxAqi > 150) {
//			viewHeight = (int)(CommonUtil.dip2px(mContext, 250));
//		}
		final DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		llContainer.removeAllViews();
		llContainer.addView(aqiView, dm.widthPixels*4, viewHeight);
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				hScrollView.scrollTo(dm.widthPixels*3/2, hScrollView.getHeight());
			}
		});
	}
	
	private void showLandscape() {
		hScrollView.setVisibility(View.VISIBLE);
		ivExpand.setVisibility(View.VISIBLE);
		llTop.setVisibility(View.GONE);
		llCity.setVisibility(View.VISIBLE);
		AqiQualityView aqiView = new AqiQualityView(mContext);
		aqiView.setData(aqiList, aqiDate);
		final DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		llContainer.removeAllViews();
		llContainer.addView(aqiView, dm.widthPixels*2, LinearLayout.LayoutParams.MATCH_PARENT);
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				hScrollView.scrollTo(dm.widthPixels*2/4, hScrollView.getHeight());
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (configuration == null) {
				return false;
			}
			if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
				if (llContent.getVisibility() == View.VISIBLE) {
					hideAnimation(llContent);
					llCity.setVisibility(View.GONE);
					return false;
				} else {
					finish();
				}
			}else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				showPortrait();
				ivExpand.setImageResource(R.drawable.iv_expand_black);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			if (configuration == null) {
				return;
			}
			if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
				if (llContent.getVisibility() == View.VISIBLE) {
					hideAnimation(llContent);
					llCity.setVisibility(View.GONE);
				} else {
					finish();
				}
			}else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				showPortrait();
				ivExpand.setImageResource(R.drawable.iv_expand_black);
			}
			break;
		case R.id.ivExpand:
			if (configuration == null) {
				return;
			}
			if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				showLandscape();
				ivExpand.setImageResource(R.drawable.iv_collose_black);
			}else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				showPortrait();
				ivExpand.setImageResource(R.drawable.iv_expand_black);
			}
			break;

		default:
			break;
		}
	}
	
	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (mMapView != null) {
			mMapView.onResume();
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if (mMapView != null) {
			mMapView.onPause();
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mMapView != null) {
			mMapView.onSaveInstanceState(outState);
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mMapView != null) {
			mMapView.onDestroy();
		}
	}

}
