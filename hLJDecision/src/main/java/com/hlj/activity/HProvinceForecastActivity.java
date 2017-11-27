package com.hlj.activity;

/**
 * 全省预报
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
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
import com.hlj.manager.RainManager;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.OkHttpUtil;
import com.hlj.utils.WeatherUtil;
import com.hlj.dto.CityDto;
import com.hlj.view.ExpandableTextView;

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
 * 天气预报
 * @author shawn_sun
 *
 */

@SuppressLint("SimpleDateFormat")
public class HProvinceForecastActivity extends BaseActivity implements OnClickListener, OnMarkerClickListener, OnMapClickListener,
        InfoWindowAdapter, OnCameraChangeListener {
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private MapView mapView = null;//高德地图
	private AMap aMap = null;//高德地图
	private float zoom = 5.5f;
	private Marker selectMarker = null;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM月dd日");
	private SimpleDateFormat sdf3 = new SimpleDateFormat("HH");
	private ExpandableTextView llContainer = null;

	private List<CityDto> cityList = new ArrayList<>();//市级
	private List<CityDto> districtList = new ArrayList<>();//县级
	private List<Marker> cityMarkers = new ArrayList<>();
	private List<Marker> disMarkers = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hactivity_province_forecast);
		mContext = this;
		showDialog();
		initAmap(savedInstanceState);
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		llContainer = (ExpandableTextView) findViewById(R.id.llContainer);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}

		//获取全省预报文字
		String url = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(url)) {
			OkHttpText(url);
		}

		//获取站点信息
		OkHttpRank();
    }
	
	/**
	 * 初始化高德地图
	 */
	private void initAmap(Bundle bundle) {
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mapView.getMap();
		}

		LatLng guizhouLatLng = new LatLng(49.302915,128.121040);
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(guizhouLatLng, zoom));
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMarkerClickListener(this);
		aMap.setOnMapClickListener(this);
		aMap.setInfoWindowAdapter(this);
		aMap.setOnCameraChangeListener(this);

		CommonUtil.drawHLJJson(mContext, aMap);
	}
	
	/**
	 * 异步请求
	 */
	private void OkHttpText(String url) {
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
				if (!TextUtils.isEmpty(result)) {
					try {
						JSONObject obj = new JSONObject(result);
						String c1 = obj.getString("c1");
						c1 = c1.replace("<p>", "");
						c1 = c1.replace("</p>", "")+"    ";
						String c2 = obj.getString("c2");
						c2 = c2.replace("<p>", "");
						c2 = c2.replace("</p>", "")+"\n";
						String c3 = obj.getString("c3");
						c3 = c3.replace("<p>", "");
						c3 = c3.replace("</p>", "").trim()+"\n";

						final String content = c1+c3+c2;

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								llContainer.setText(content);
								llContainer.setVisibility(View.VISIBLE);
							}
						});

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * 加密请求字符串
	 * @return
	 */
	private String getSecretUrl() {
		String SANX_DATA_99 = "sanx_data_99";//加密秘钥名称
		String APPID = "f63d329270a44900";//机密需要用到的AppId
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
					cityList.clear();
					parseStationInfo(result, "level1", cityList);
					parseStationInfo(result, "level2", cityList);
					districtList.clear();
					parseStationInfo(result, "level3", districtList);

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							removeMarkers();

							for (int i = 0; i < cityList.size(); i++) {
								CityDto dto = cityList.get(i);
								getWeatherInfos(dto, cityMarkers, true);
							}

							for (int i = 0; i < districtList.size(); i++) {
								CityDto dto = districtList.get(i);
								getWeatherInfos(dto, disMarkers, false);
							}

							cancelDialog();
						}
					});
				}
			}
		});
	}

	/**
	 * 解析数据
	 */
	private void parseStationInfo(String result, String level, List<CityDto> list) {
		try {
			JSONObject obj = new JSONObject(result.toString());
			if (!obj.isNull("data")) {
				JSONObject dataObj = obj.getJSONObject("data");
				if (!dataObj.isNull(level)) {
					JSONArray array = new JSONArray(dataObj.getString(level));
					for (int i = 0; i < array.length(); i++) {
						CityDto dto = new CityDto();
						JSONObject itemObj = array.getJSONObject(i);
						if (!itemObj.isNull("name")) {
							dto.areaName = itemObj.getString("name");
						}
						if (!itemObj.isNull("level")) {
							dto.level = itemObj.getString("level");
						}
						if (!itemObj.isNull("areaid")) {
							dto.areaId = itemObj.getString("areaid");
						}
						if (!itemObj.isNull("lat")) {
							dto.lat = Double.valueOf(itemObj.getString("lat"));
						}
						if (!itemObj.isNull("lon")) {
							dto.lng = Double.valueOf(itemObj.getString("lon"));
						}

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
	 * 获取多个城市天气信息
	 */
	private void getWeatherInfos(final CityDto dto, final List<Marker> markers, final boolean isVisible) {
		WeatherAPI.getWeather2(mContext, dto.areaId, Language.ZH_CN, new AsyncResponseHandler() {
			@Override
			public void onComplete(Weather content) {
				super.onComplete(content);
				try {
					//获取明天预报信息
					JSONArray weeklyArray = content.getWeatherForecastInfo(2);
					JSONObject weeklyObj = weeklyArray.getJSONObject(0);

					dto.lowPheCode = Integer.valueOf(weeklyObj.getString("fb"));
					dto.highPheCode = Integer.valueOf(weeklyObj.getString("fa"));
					dto.lowTemp = weeklyObj.getString("fd");
					dto.highTemp = weeklyObj.getString("fc");

					addMarker(dto, markers, isVisible);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable error, String content) {
				super.onError(error, content);
			}
		});
	}

	private void removeMarkers() {
		for (int i = 0; i < cityMarkers.size(); i++) {
			Marker m = cityMarkers.get(i);
			markerColloseAnimation(m);
			m.remove();
		}
		cityMarkers.clear();

		for (int i = 0; i < disMarkers.size(); i++) {
			Marker m = disMarkers.get(i);
			markerColloseAnimation(m);
			m.remove();
		}
		disMarkers.clear();
	}
	
	private void addMarker(CityDto dto, List<Marker> markers, boolean isVisible) {
		MarkerOptions options = new MarkerOptions();
		options.title(dto.areaId);
		options.snippet(dto.areaName);
		options.anchor(0.5f, 0.5f);
		options.position(new LatLng(dto.lat, dto.lng));
		options.icon(BitmapDescriptorFactory.fromView(getTextBitmap1(dto)));
		Marker marker = aMap.addMarker(options);
		if (marker != null) {
			marker.setVisible(isVisible);
			markers.add(marker);
			markerExpandAnimation(marker);
		}
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
	
	/**
	 * 给marker添加文字
	 * @return
	 */
	private View getTextBitmap1(CityDto dto) {      
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.layout_weather1, null);
		if (view == null) {
			return null;
		}
		TextView tvName = (TextView) view.findViewById(R.id.tvName);
		ImageView ivPhe = (ImageView) view.findViewById(R.id.ivPhe);
		TextView tvTemp = (TextView) view.findViewById(R.id.tvTemp);
		
		tvName.setText(dto.areaName);
		Drawable drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
		try {
			long zao8 = sdf3.parse("06").getTime();
			long wan8 = sdf3.parse("20").getTime();
			long current = sdf3.parse(sdf3.format(new Date())).getTime();
			if (current >= zao8 && current < wan8) {
				drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
				drawable.setLevel(dto.highPheCode);
			}else {
				drawable = getResources().getDrawable(R.drawable.phenomenon_drawable_night);
				drawable.setLevel(dto.lowPheCode);
			}
			ivPhe.setBackground(drawable);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		tvTemp.setText(dto.highTemp+" | "+dto.lowTemp+"℃");
		
		return view;
	}
	
	@Override
	public void onMapClick(LatLng arg0) {
		if (selectMarker != null) {
			selectMarker.hideInfoWindow();
		}
	}
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		if (marker != null) {
			selectMarker = marker;
			selectMarker.showInfoWindow();
		}
		return true;
	}
	
	@Override
	public View getInfoContents(final Marker marker) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.weather_marker_info, null);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		TextView tvDetail = (TextView) view.findViewById(R.id.tvDetail);
		
		tvDetail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				CityDto data = new CityDto();
				data.areaName = marker.getSnippet();
				data.cityId = marker.getTitle();
				Intent intent = new Intent(mContext, HWeatherDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", data);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		tvContent.setText("");
		getWeatherInfo(marker.getTitle(), marker.getSnippet(), tvContent, progressBar, tvDetail);
		return view;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		return null;
	}
	
	@Override
	public void onCameraChange(CameraPosition arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		if (zoom == arg0.zoom) {//如果是地图缩放级别不变，并且点击就不做处理
			return;
		}

		zoom = arg0.zoom;
		if (arg0.zoom <= 8.0f) {
			for (int i = 0; i < cityMarkers.size(); i++) {
				Marker m = cityMarkers.get(i);
				m.setVisible(true);
				markerExpandAnimation(m);
			}
			for (int i = 0; i < disMarkers.size(); i++) {
				Marker m = disMarkers.get(i);
				m.setVisible(false);
				markerColloseAnimation(m);
			}
		}if (arg0.zoom > 8.0f) {
			for (int i = 0; i < cityMarkers.size(); i++) {
				Marker m = cityMarkers.get(i);
				m.setVisible(true);
				markerExpandAnimation(m);
			}
			for (int i = 0; i < disMarkers.size(); i++) {
				Marker m = disMarkers.get(i);
				m.setVisible(true);
				markerExpandAnimation(m);
			}
		}

	}
	
	/**
	 * 获取天气数据
	 */
	private void getWeatherInfo(String cityId, final String cityName, final TextView tvContent, final ProgressBar progressBar, final TextView tvDetail) {
		WeatherAPI.getWeather2(mContext, cityId, Language.ZH_CN, new AsyncResponseHandler() {
			@Override
			public void onComplete(Weather content) {
				super.onComplete(content);
				if (content != null) {
					//实况信息
					String factContent = cityName+"预报";
					
					try {
						JSONObject object = content.getWeatherFactInfo();
						if (!object.isNull("l7")) {
							String time = object.getString("l7");
							if (time != null) {
								factContent = factContent + "（"+time+"）"+"发布：\n";
							}
						}
						
						//获取明天预报信息
						JSONArray weeklyArray = content.getWeatherForecastInfo(2);
						JSONObject weeklyObj = weeklyArray.getJSONObject(0);

						JSONArray timeArray =  content.getTimeInfo(2);
						JSONObject timeObj = timeArray.getJSONObject(0);
						String week = timeObj.getString("t4");//星期几
						String date = timeObj.getString("t1");//日期
						if (week != null && date != null) {
							try {
								factContent = factContent + sdf2.format(sdf1.parse(date)) + "（"+week+"），";
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
						
						String lowPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fb"))));
						String highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fa"))));
						if (highPhe != null && lowPhe != null) {
							String phe = lowPhe;
							if (!TextUtils.equals(highPhe, lowPhe)) {
								phe = lowPhe + "转" + highPhe;
							}else {
								phe = lowPhe;
							}
							factContent = factContent + phe + "，";
						}
						
						String lowTemp = weeklyObj.getString("fd");
						String highTemp = weeklyObj.getString("fc");
						if (lowTemp != null && highTemp != null) {
							factContent = factContent + lowTemp + " ~ " + highTemp + "℃，";
						}
						
						String lowDir = getString(WeatherUtil.getWindDirection(Integer.valueOf(weeklyObj.getString("ff"))));
						String lowForce = WeatherUtil.getDayWindForce(Integer.valueOf(weeklyObj.getString("fh")));
						String highDir = getString(WeatherUtil.getWindDirection(Integer.valueOf(weeklyObj.getString("fe"))));
						String highForce = WeatherUtil.getDayWindForce(Integer.valueOf(weeklyObj.getString("fg")));
						if (!TextUtils.equals(lowDir+lowForce, highDir+highForce)) {
							factContent = factContent + lowDir + lowForce + "转" + highDir + highForce;
						}else {
							factContent = factContent + lowDir + lowForce;
						}
						
						tvContent.setText(factContent);
						tvDetail.setVisibility(View.VISIBLE);
						progressBar.setVisibility(View.GONE);
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (NullPointerException e) {
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
