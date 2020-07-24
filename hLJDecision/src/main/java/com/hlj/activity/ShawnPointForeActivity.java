package com.hlj.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.GroundOverlay;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.hlj.common.CONST;
import com.hlj.dto.PointForeDto;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.OkHttpUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

/**
 * 格点预报
 */
public class ShawnPointForeActivity extends BaseActivity implements OnClickListener, AMapLocationListener, AMap.OnCameraChangeListener,
        AMap.OnMapClickListener{
	
	private Context mContext;
	private TextView tvName,tvDataSource,tvTime;
	private TextView tvTemp,tvHumidity,tvWind,tvCloud;
	private ImageView ivTemp,ivHumidity,ivWind,ivCloud,ivSwitch,ivDataSource,ivPlay,ivLegend;
	private MapView mMapView;
	private AMap aMap;
	private float zoom = 5.5f;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("dd日HH时", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHH", Locale.CHINA);
	private int dataType = 1;//1温度、2湿度、3风速、4能见度、5云量
	private double locationLat = 46.102915, locationLng = 128.121040;
	private int mapType = AMap.MAP_TYPE_NORMAL;
	private LatLng start,end;
	private Bundle savedInstanceState;
	private SeekBar seekBar;
	private RadarThread mRadarThread;
	private Marker locationMarker;
	private PointForeDto pointForeDto = new PointForeDto();
	private int currentIndex = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_point_fore);
		mContext = this;
		this.savedInstanceState = savedInstanceState;
		showDialog();
		init();
	}

	private void init() {
		initMap();
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvName = findViewById(R.id.tvName);
		ivTemp = findViewById(R.id.ivTemp);
		ivTemp.setOnClickListener(this);
		ivHumidity = findViewById(R.id.ivHumidity);
		ivHumidity.setOnClickListener(this);
		ivWind = findViewById(R.id.ivWind);
		ivWind.setOnClickListener(this);
		ivCloud = findViewById(R.id.ivCloud);
		ivCloud.setOnClickListener(this);
		tvTemp = findViewById(R.id.tvTemp);
		tvTemp.setOnClickListener(this);
		tvHumidity = findViewById(R.id.tvHumidity);
		tvHumidity.setOnClickListener(this);
		tvWind = findViewById(R.id.tvWind);
		tvWind.setOnClickListener(this);
		tvCloud = findViewById(R.id.tvCloud);
		tvCloud.setOnClickListener(this);
		ImageView ivLocation = findViewById(R.id.ivLocation);
		ivLocation.setOnClickListener(this);
		ivSwitch = findViewById(R.id.ivSwitch);
		ivSwitch.setOnClickListener(this);
		tvDataSource = findViewById(R.id.tvDataSource);
		tvDataSource.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
		tvDataSource.setOnClickListener(this);
		ivDataSource = findViewById(R.id.ivDataSource);
		ivDataSource.setOnClickListener(this);
		ivPlay = findViewById(R.id.ivPlay);
		ivPlay.setOnClickListener(this);
		tvTime = findViewById(R.id.tvTime);
		seekBar = findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(seekbarListener);
		ImageView ivLegendPrompt = findViewById(R.id.ivLegendPrompt);
		ivLegendPrompt.setOnClickListener(this);
		ivLegend = findViewById(R.id.ivLegend);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}

		startLocation();
		OkHttpList();

		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);

	}

	/**
	 * 开始定位
	 */
	private void startLocation() {
		AMapLocationClientOption mLocationOption = new AMapLocationClientOption();//初始化定位参数
		AMapLocationClient mLocationClient = new AMapLocationClient(mContext);//初始化定位
		mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
		mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
		mLocationOption.setOnceLocation(true);//设置是否只定位一次,默认为false
		mLocationOption.setMockEnable(false);//设置是否允许模拟位置,默认为false，不允许模拟位置
		mLocationOption.setInterval(2000);//设置定位间隔,单位毫秒,默认为2000ms
		mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
		mLocationClient.setLocationListener(this);
		mLocationClient.startLocation();//启动定位
	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (amapLocation != null && amapLocation.getErrorCode() == 0) {
			locationLat = amapLocation.getLatitude();
			locationLng = amapLocation.getLongitude();
			locationComplete();
		}
	}

	private void locationComplete() {
		if (locationMarker != null) {
			locationMarker.remove();
		}
		LatLng latLng = new LatLng(locationLat, locationLng);
		MarkerOptions options = new MarkerOptions();
		options.anchor(0.5f, 0.5f);
		Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.iv_map_location),
				(int) (CommonUtil.dip2px(mContext, 15)), (int) (CommonUtil.dip2px(mContext, 15)));
		if (bitmap != null) {
			options.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
		} else {
			options.icon(BitmapDescriptorFactory.fromResource(R.drawable.iv_map_location));
		}
		options.position(latLng);
		locationMarker = aMap.addMarker(options);
		locationMarker.setClickable(false);
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		Log.e("zoom", arg0.zoom+"");
		zoom = arg0.zoom;
	}

    @Override
    public void onMapClick(LatLng latLng) {
		locationLat = latLng.latitude;
		locationLng = latLng.longitude;
		locationComplete();
        Intent intent = new Intent(mContext, ShawnPointForeDetailActivity.class);
        intent.putExtra("lat", latLng.latitude);
        intent.putExtra("lng", latLng.longitude);
        startActivity(intent);
    }

	private SeekBar.OnSeekBarChangeListener seekbarListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			if (mRadarThread != null) {
				mRadarThread.setCurrent(seekBar.getProgress());
				mRadarThread.stopTracking();
			}
		}
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			if (mRadarThread != null) {
				mRadarThread.startTracking();
			}
		}
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		}
	};

	/**
	 * 初始化地图
	 */
	private void initMap() {
		mMapView = findViewById(R.id.mapView);
		mMapView.onCreate(savedInstanceState);
		if (aMap == null) {
			aMap = mMapView.getMap();
		}
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(46.102915,128.121040), zoom));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnCameraChangeListener(this);
		aMap.setOnMapClickListener(this);

		TextView tvMapNumber = findViewById(R.id.tvMapNumber);
		tvMapNumber.setText(aMap.getMapContentApprovalNumber());

		CommonUtil.drawHLJJson(mContext, aMap);
	}

	private void OkHttpList() {
		final String url = "http://decision-admin.tianqi.cn/Home/extra/decision_gdsk_yb_images";
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
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject obj = new JSONObject(result);

										start = new LatLng(obj.getDouble("minlat"), obj.getDouble("minlon"));
										end = new LatLng(obj.getDouble("maxlat"), obj.getDouble("maxlon"));

										if (!obj.isNull("tem")) {
											JSONObject tem = obj.getJSONObject("tem");
											pointForeDto.tems.clear();
											String tuliurl = tem.getString("tuliurl");
											JSONArray array = tem.getJSONArray("list");

											String cHour = sdf2.format(new Date());
											for (int i = 0; i < array.length(); i++) {
												PointForeDto dto = new PointForeDto();
												JSONObject itemObj = array.getJSONObject(i);
												dto.legendUrl = tuliurl;
												dto.imgUrl = itemObj.getString("imgurl");
												dto.time = itemObj.getString("time");
												pointForeDto.tems.add(dto);
												if (TextUtils.equals(cHour, dto.time)) {
													currentIndex = i;
												}
											}
										}

										if (!obj.isNull("humidity")) {
											JSONObject humidity = obj.getJSONObject("humidity");
											pointForeDto.humiditys.clear();
											String tuliurl = humidity.getString("tuliurl");
											JSONArray array = humidity.getJSONArray("list");
											for (int i = 0; i < array.length(); i++) {
												PointForeDto dto = new PointForeDto();
												JSONObject itemObj = array.getJSONObject(i);
												dto.legendUrl = tuliurl;
												dto.imgUrl = itemObj.getString("imgurl");
												dto.time = itemObj.getString("time");
												pointForeDto.humiditys.add(dto);
											}
										}

										if (!obj.isNull("wind")) {
											JSONObject wind = obj.getJSONObject("wind");
											pointForeDto.winds.clear();
											String tuliurl = wind.getString("tuliurl");
											JSONArray array = wind.getJSONArray("list");
											for (int i = 0; i < array.length(); i++) {
												PointForeDto dto = new PointForeDto();
												JSONObject itemObj = array.getJSONObject(i);
												dto.legendUrl = tuliurl;
												dto.imgUrl = itemObj.getString("imgurl");
												dto.time = itemObj.getString("time");
												pointForeDto.winds.add(dto);
											}
										}

										if (!obj.isNull("cloud")) {
											JSONObject cloud = obj.getJSONObject("cloud");
											pointForeDto.clouds.clear();
											String tuliurl = cloud.getString("tuliurl");
											JSONArray array = cloud.getJSONArray("list");
											for (int i = 0; i < array.length(); i++) {
												PointForeDto dto = new PointForeDto();
												JSONObject itemObj = array.getJSONObject(i);
												dto.legendUrl = tuliurl;
												dto.imgUrl = itemObj.getString("imgurl");
												dto.time = itemObj.getString("time");
												pointForeDto.clouds.add(dto);
											}
										}

										switchElement();

									} catch (JSONException e) {
										e.printStackTrace();
									}
								}

								cancelDialog();
							}
						});
					}
				});
			}
		}).start();
	}

	/**
	 * 切换要素
	 */
	private void switchElement() {
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
		mRadarThread = new RadarThread();

		PointForeDto data = null;
		try {
			if (dataType == 1) {
				mRadarThread.setDataList(pointForeDto.tems);
				data = pointForeDto.tems.get(currentIndex);
				changeProgress(data.time, currentIndex, pointForeDto.tems.size()-1);
			}else if (dataType == 2) {
				mRadarThread.setDataList(pointForeDto.humiditys);
				data = pointForeDto.humiditys.get(currentIndex);
				changeProgress(data.time, currentIndex, pointForeDto.humiditys.size()-1);
			}else if (dataType == 3) {
				mRadarThread.setDataList(pointForeDto.winds);
				data = pointForeDto.winds.get(currentIndex);
				changeProgress(data.time, currentIndex, pointForeDto.winds.size()-1);
			}else if (dataType == 5) {
				mRadarThread.setDataList(pointForeDto.clouds);
				data = pointForeDto.clouds.get(currentIndex);
				changeProgress(data.time, currentIndex, pointForeDto.clouds.size()-1);
			}
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}

		if (data != null) {
			OkHttpBitmap(data.imgUrl);
			Picasso.get().load(data.legendUrl).into(ivLegend);
			mRadarThread.start();
		}
	}

	private void OkHttpBitmap(final String url) {
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
						final byte[] bytes = response.body().bytes();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
								if (bitmap != null) {
									drawFactBitmap(bitmap);
								}
							}
						});
					}
				});
			}
		}).start();
	}

	private GroundOverlay factOverlay;
	/**
	 * 绘制实况图
	 */
	private void drawFactBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			return;
		}
		BitmapDescriptor fromView = BitmapDescriptorFactory.fromBitmap(bitmap);
		LatLngBounds bounds = new LatLngBounds.Builder()
				.include(start)
				.include(end)
				.build();

//        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

		if (factOverlay == null) {
			factOverlay = aMap.addGroundOverlay(new GroundOverlayOptions()
					.anchor(0.5f, 0.5f)
					.positionFromBounds(bounds)
					.image(fromView)
					.transparency(0.2f));
		} else {
			factOverlay.setImage(null);
			factOverlay.setPositionFromBounds(bounds);
			factOverlay.setImage(fromView);
		}
	}

	/**
	 * 清除实况图
	 */
	private void removeFactLayer() {
		if (factOverlay != null) {
			factOverlay.remove();
			factOverlay = null;
		}
	}

    private class RadarThread extends Thread {

		static final int STATE_NONE = 0;
		static final int STATE_PLAYING = 1;
		static final int STATE_PAUSE = 2;
		static final int STATE_CANCEL = 3;
		private List<PointForeDto> itemList;
		private int state;
		private int index;
		private int count;
		private boolean isTracking;

		private RadarThread() {
			this.index = currentIndex;
			this.state = STATE_PAUSE;
			this.isTracking = false;
		}

		private void setDataList(List<PointForeDto> itemList) {
			this.itemList = itemList;
			this.count = itemList.size();
		}

		private int getCurrentState() {
			return state;
		}

		@Override
		public void run() {
			super.run();
			while (true) {
				if (state == STATE_CANCEL) {
					break;
				}
				if (state == STATE_PAUSE) {
					continue;
				}
				if (isTracking) {
					continue;
				}
				sendRadar();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private void sendRadar() {
			if (index >= count || index < 0) {
				index = 0;
			}else {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						PointForeDto dto = itemList.get(index);
						OkHttpBitmap(dto.imgUrl);
						changeProgress(dto.time, index++, count-1);
						currentIndex = index;
					}
				});
			}
		}

		public void cancel() {
			this.state = STATE_CANCEL;
		}
		public void pause() {
			this.state = STATE_PAUSE;
		}
		public void play() {
			this.state = STATE_PLAYING;
		}

		public void setCurrent(int index) {
			this.index = index;
		}

		public void startTracking() {
			isTracking = true;
		}

		public void stopTracking() {
			isTracking = false;
			if (this.state == STATE_PAUSE) {
				sendRadar();
			}
		}
	}

	@SuppressLint("SimpleDateFormat")
	private void changeProgress(String time, int progress, int max) {
		if (seekBar != null) {
			seekBar.setMax(max);
			seekBar.setProgress(progress);
		}
		if (!TextUtils.isEmpty(time)) {
			try {
				tvTime.setText(sdf1.format(sdf2.parse(time)));
				String ttTime = tvTime.getText().toString();
				if (dataType == 1) {
					tvName.setText(ttTime+"格点温度预报[单位:"+getString(R.string.unit_degree)+"]");
				}else if (dataType == 2) {
					tvName.setText(ttTime+"格点相对湿度预报[单位:"+getString(R.string.unit_percent)+"]");
				}else if (dataType == 3) {
					tvName.setText(ttTime+"格点风速预报[单位:"+getString(R.string.unit_speed)+"]");
				}else if (dataType == 5) {
					tvName.setText(ttTime+"格点云量预报[单位:"+getString(R.string.unit_percent)+"]");
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				finish();
				break;
			case R.id.tvTemp:
			case R.id.ivTemp:
				ivTemp.setImageResource(R.drawable.com_temp_press);
				ivHumidity.setImageResource(R.drawable.com_humidity);
				ivWind.setImageResource(R.drawable.fzj_butn_windoff);
				ivCloud.setImageResource(R.drawable.com_cloud);
				tvTemp.setBackgroundResource(R.drawable.bg_map_btn_press);
				tvHumidity.setBackgroundResource(R.drawable.bg_map_btn);
				tvWind.setBackgroundResource(R.drawable.bg_map_btn);
				tvCloud.setBackgroundResource(R.drawable.bg_map_btn);
				tvTemp.setTextColor(Color.WHITE);
				tvHumidity.setTextColor(getResources().getColor(R.color.text_color4));
				tvWind.setTextColor(getResources().getColor(R.color.text_color4));
				tvCloud.setTextColor(getResources().getColor(R.color.text_color4));

				dataType = 1;
				switchElement();
				break;
			case R.id.tvHumidity:
			case R.id.ivHumidity:
				ivTemp.setImageResource(R.drawable.com_temp);
				ivHumidity.setImageResource(R.drawable.com_humidity_press);
				ivWind.setImageResource(R.drawable.fzj_butn_windoff);
				ivCloud.setImageResource(R.drawable.com_cloud);
				tvTemp.setBackgroundResource(R.drawable.bg_map_btn);
				tvHumidity.setBackgroundResource(R.drawable.bg_map_btn_press);
				tvWind.setBackgroundResource(R.drawable.bg_map_btn);
				tvCloud.setBackgroundResource(R.drawable.bg_map_btn);
				tvTemp.setTextColor(getResources().getColor(R.color.text_color4));
				tvHumidity.setTextColor(Color.WHITE);
				tvWind.setTextColor(getResources().getColor(R.color.text_color4));
				tvCloud.setTextColor(getResources().getColor(R.color.text_color4));

				dataType = 2;
				switchElement();
				break;
			case R.id.tvWind:
			case R.id.ivWind:
				ivTemp.setImageResource(R.drawable.com_temp);
				ivHumidity.setImageResource(R.drawable.com_humidity);
				ivWind.setImageResource(R.drawable.fzj_butn_wind);
				ivCloud.setImageResource(R.drawable.com_cloud);
				tvTemp.setBackgroundResource(R.drawable.bg_map_btn);
				tvHumidity.setBackgroundResource(R.drawable.bg_map_btn);
				tvWind.setBackgroundResource(R.drawable.bg_map_btn_press);
				tvCloud.setBackgroundResource(R.drawable.bg_map_btn);
				tvTemp.setTextColor(getResources().getColor(R.color.text_color4));
				tvHumidity.setTextColor(getResources().getColor(R.color.text_color4));
				tvWind.setTextColor(Color.WHITE);
				tvCloud.setTextColor(getResources().getColor(R.color.text_color4));

				dataType = 3;
				switchElement();
				break;
			case R.id.tvCloud:
			case R.id.ivCloud:
				ivTemp.setImageResource(R.drawable.com_temp);
				ivHumidity.setImageResource(R.drawable.com_humidity);
				ivWind.setImageResource(R.drawable.fzj_butn_windoff);
				ivCloud.setImageResource(R.drawable.com_cloud_press);
				tvTemp.setBackgroundResource(R.drawable.bg_map_btn);
				tvHumidity.setBackgroundResource(R.drawable.bg_map_btn);
				tvWind.setBackgroundResource(R.drawable.bg_map_btn);
				tvCloud.setBackgroundResource(R.drawable.bg_map_btn_press);
				tvTemp.setTextColor(getResources().getColor(R.color.text_color4));
				tvHumidity.setTextColor(getResources().getColor(R.color.text_color4));
				tvWind.setTextColor(getResources().getColor(R.color.text_color4));
				tvCloud.setTextColor(Color.WHITE);

				dataType = 5;
				switchElement();
				break;
			case R.id.ivSwitch:
				if (mapType == AMap.MAP_TYPE_NORMAL) {
					mapType = AMap.MAP_TYPE_SATELLITE;
					ivSwitch.setImageResource(R.drawable.com_switch_map_press);
				}else if (mapType == AMap.MAP_TYPE_SATELLITE) {
					mapType = AMap.MAP_TYPE_NORMAL;
					ivSwitch.setImageResource(R.drawable.com_switch_map);
				}
				if (aMap != null) {
					aMap.setMapType(mapType);
				}
				break;
			case R.id.ivDataSource:
				if (tvDataSource.getVisibility() == View.VISIBLE) {
					tvDataSource.setVisibility(View.GONE);
					ivDataSource.setImageResource(R.drawable.com_data_source);
				}else {
					tvDataSource.setVisibility(View.VISIBLE);
					ivDataSource.setImageResource(R.drawable.com_data_source_press);
				}
				break;
			case R.id.tvDataSource:
				Intent intent = new Intent(mContext, HUrlActivity.class);
				intent.putExtra(CONST.ACTIVITY_NAME, "中央气象台智能网格预报产品");
				intent.putExtra(CONST.WEB_URL, "http://www.cma.gov.cn/2011xzt/2017zt/2017qmt/20170728/");
				startActivity(intent);
				break;
			case R.id.ivLocation:
				if (zoom >= 12.f) {
					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationLat, locationLng), 3.5f));
				}else {
					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationLat, locationLng), 12.0f));
				}
				break;
			case R.id.ivLegendPrompt:
				if (ivLegend.getVisibility() == View.VISIBLE) {
					ivLegend.setVisibility(View.INVISIBLE);
				}else {
					ivLegend.setVisibility(View.VISIBLE);
				}
				break;
			case R.id.ivPlay:
				if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PLAYING) {
					mRadarThread.pause();
					ivPlay.setImageResource(R.drawable.icon_play);
				} else if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PAUSE) {
					mRadarThread.play();
					ivPlay.setImageResource(R.drawable.icon_pause);
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
