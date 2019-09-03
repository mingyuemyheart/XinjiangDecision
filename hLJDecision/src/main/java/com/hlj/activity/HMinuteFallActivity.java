package com.hlj.activity;

/**
 * 分钟级降水
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.GroundOverlay;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.hlj.common.CONST;
import com.hlj.dto.MinuteFallDto;
import com.hlj.dto.WeatherDto;
import com.hlj.manager.CaiyunManager;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.OkHttpUtil;
import com.hlj.view.MinuteFallView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

public class HMinuteFallActivity extends BaseActivity implements View.OnClickListener, CaiyunManager.RadarListener,
		AMap.OnMapClickListener, GeocodeSearch.OnGeocodeSearchListener, AMapLocationListener {

	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private MapView mMapView = null;
	private AMap aMap = null;
	private List<MinuteFallDto> mList = new ArrayList<>();
	private List<MinuteFallDto> images = new ArrayList<>();
	private String dataUrl = "http://api.tianqi.cn:8070/v1/img.py";//彩云接口数据
	private GroundOverlay mOverlay = null;
	private CaiyunManager mRadarManager;
	private RadarThread mRadarThread;
	private static final int HANDLER_SHOW_RADAR = 1;
	private static final int HANDLER_PROGRESS = 2;
	private static final int HANDLER_LOAD_FINISHED = 3;
	private static final int HANDLER_PAUSE = 4;
	private LinearLayout llSeekBar = null;
	private ImageView ivPlay = null;
	private SeekBar seekBar = null;
	private TextView tvTime = null;
	private Marker clickMarker = null;
	private GeocodeSearch geocoderSearch = null;
	private TextView tvAddr = null;//地址信息
	private TextView tvRain = null;//降雨信息
	private LinearLayout llContainer3 = null;
	private int width = 0;
	private LinearLayout llLegend = null;
	private ImageView ivRank = null;
	private ImageView ivLegend = null;
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hactivity_minute_fall);
		mContext = this;
		showDialog();
		initMap(savedInstanceState);
		initWidget();
	}

	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		ivPlay = (ImageView) findViewById(R.id.ivPlay);
		ivPlay.setOnClickListener(this);
		ivRank = (ImageView) findViewById(R.id.ivRank);
		ivRank.setOnClickListener(this);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(seekbarListener);
		tvTime = (TextView) findViewById(R.id.tvTime);
		llSeekBar = (LinearLayout) findViewById(R.id.llSeekBar);
		tvAddr = (TextView) findViewById(R.id.tvAddr);
		tvRain = (TextView) findViewById(R.id.tvRain);
		llLegend = (LinearLayout) findViewById(R.id.llLegend);
		ivLegend = (ImageView) findViewById(R.id.ivLegend);
		llContainer3 = (LinearLayout) findViewById(R.id.llContainer3);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;

		CommonUtil.drawHLJJson(mContext, aMap);

		geocoderSearch = new GeocodeSearch(mContext);
		geocoderSearch.setOnGeocodeSearchListener(this);

		mRadarManager = new CaiyunManager(mContext);

		startLocation();

		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
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
			if (amapLocation.getLongitude() != 0 && amapLocation.getLatitude() != 0) {
				LatLng latLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
				aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8.0f));
				addMarkerToMap(latLng);

				queryMinute(amapLocation.getLongitude(), amapLocation.getLatitude());
				asyncImages(dataUrl);
			}
		}
	}

	/**
	 * 异步加载一小时内降雨、或降雪信息
	 * @param lng
	 * @param lat
	 */
	private void queryMinute(double lng, double lat) {
		final String url = "http://api.caiyunapp.com/v2/HyTVV5YAkoxlQ3Zd/"+lng+","+lat+"/forecast";
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
										JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("result")) {
												JSONObject obj = object.getJSONObject("result");
												if (!obj.isNull("minutely")) {
													JSONObject objMin = obj.getJSONObject("minutely");
													if (!objMin.isNull("description")) {
														String rain = objMin.getString("description");
														if (!TextUtils.isEmpty(rain)) {
															tvRain.setText(rain.replace("小彩云", ""));
															tvRain.setVisibility(View.VISIBLE);
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
														llContainer3.addView(minuteFallView, width, (int)(CommonUtil.dip2px(mContext, 120)));
													}
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
		}).start();
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

	private void initMap(Bundle bundle) {
		mMapView = (MapView) findViewById(R.id.mapView);
		mMapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mMapView.getMap();
		}
		aMap.moveCamera(CameraUpdateFactory.zoomTo(8.0f));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMapClickListener(this);

		TextView tvMapNumber = findViewById(R.id.tvMapNumber);
		tvMapNumber.setText(aMap.getMapContentApprovalNumber());
	}

	private void addMarkerToMap(LatLng latLng) {
		MarkerOptions options = new MarkerOptions();
		options.position(latLng);
		options.anchor(0.5f, 0.5f);
		Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.iv_map_location),
				(int)(CommonUtil.dip2px(mContext, 15)), (int)(CommonUtil.dip2px(mContext, 15)));
		if (bitmap != null) {
			options.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
		}else {
			options.icon(BitmapDescriptorFactory.fromResource(R.drawable.iv_map_location));
		}
		clickMarker = aMap.addMarker(options);
		query(latLng.longitude, latLng.latitude);
		searchAddrByLatLng(latLng.latitude, latLng.longitude);
	}

	@Override
	public void onMapClick(LatLng arg0) {
		if (clickMarker != null) {
			clickMarker.remove();
		}
		tvAddr.setText("");
		tvRain.setText("");
		addMarkerToMap(arg0);
		queryMinute(arg0.longitude, arg0.latitude);
	}

	/**
	 * 通过经纬度获取地理位置信息
	 * @param lat
	 * @param lng
	 */
	private void searchAddrByLatLng(double lat, double lng) {
		//latLonPoint参数表示一个Latlng，第二参数表示范围多少米，GeocodeSearch.AMAP表示是国测局坐标系还是GPS原生坐标系   
		RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(lat, lng), 200, GeocodeSearch.AMAP);
		geocoderSearch.getFromLocationAsyn(query);
	}

	@Override
	public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
		// TODO Auto-generated method stub
	}
	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		if (rCode == 1000) {
			if (result != null && result.getRegeocodeAddress() != null && result.getRegeocodeAddress().getFormatAddress() != null) {
				String addr = result.getRegeocodeAddress().getFormatAddress();
				if (!TextUtils.isEmpty(addr)) {
					tvAddr.setText(addr);
				}
			}
		}
	}

	/**
	 * 异步加载一小时内降雨、或降雪信息
	 * @param lng
	 * @param lat
	 */
	private void query(double lng, double lat) {
		final String url = "http://api.caiyunapp.com/v2/HyTVV5YAkoxlQ3Zd/"+lng+","+lat+"/forecast";
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
										JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("result")) {
												JSONObject objResult = object.getJSONObject("result");
												if (!objResult.isNull("minutely")) {
													JSONObject objMin = objResult.getJSONObject("minutely");
													if (!objMin.isNull("description")) {
														String rain = objMin.getString("description");
														if (!TextUtils.isEmpty(rain)) {
															tvRain.setText(rain.replace("小彩云", ""));
															tvRain.setVisibility(View.VISIBLE);
														}else {
															tvRain.setVisibility(View.GONE);
														}
													}
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
		}).start();
	}

	private void asyncImages(final String url) {
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
										if (!obj.isNull("status")) {
											if (obj.getString("status").equals("ok")) {//鎴愬姛
												if (!obj.isNull("radar_img")) {
													mList.clear();
													JSONArray array = new JSONArray(obj.getString("radar_img"));
													for (int i = 0; i < array.length(); i++) {
														JSONArray array0 = array.getJSONArray(i);
														MinuteFallDto dto = new MinuteFallDto();
														dto.setImgUrl(array0.optString(0));
														dto.setTime(array0.optLong(1));
														JSONArray itemArray = array0.getJSONArray(2);
														dto.setP1(itemArray.optDouble(0));
														dto.setP2(itemArray.optDouble(1));
														dto.setP3(itemArray.optDouble(2));
														dto.setP4(itemArray.optDouble(3));
														mList.add(dto);
													}
													if (mList.size() > 0) {
														startDownLoadImgs(mList);
													}
												}
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

	private void startDownLoadImgs(List<MinuteFallDto> list) {
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
		mRadarManager.loadImagesAsyn(list, this);
	}

	@Override
	public void onResult(int result, List<MinuteFallDto> images) {
		mHandler.sendEmptyMessage(HANDLER_LOAD_FINISHED);
		if (result == CaiyunManager.RadarListener.RESULT_SUCCESSED) {
//			if (mRadarThread != null) {
//				mRadarThread.cancel();
//				mRadarThread = null;
//			}
//			mRadarThread = new RadarThread(images);
//			mRadarThread.start();

			this.images.clear();
			this.images.addAll(images);

			//把最新的一张降雨图片覆盖在地图上
			MinuteFallDto radar = images.get(images.size()-1);
			Message message = mHandler.obtainMessage();
			message.what = HANDLER_SHOW_RADAR;
			message.obj = radar;
			message.arg1 = 100;
			message.arg2 = 100;
			mHandler.sendMessage(message);
		}
	}

	@Override
	public void onProgress(String url, int progress) {
		Message msg = new Message();
		msg.obj = progress;
		msg.what = HANDLER_PROGRESS;
		mHandler.sendMessage(msg);
	}

	private void showRadar(Bitmap bitmap, double p1, double p2, double p3, double p4) {
		BitmapDescriptor fromView = BitmapDescriptorFactory.fromBitmap(bitmap);
		LatLngBounds bounds = new LatLngBounds.Builder()
				.include(new LatLng(p3, p2))
				.include(new LatLng(p1, p4))
				.build();

		if (mOverlay == null) {
			mOverlay = aMap.addGroundOverlay(new GroundOverlayOptions()
					.anchor(0.5f, 0.5f)
					.positionFromBounds(bounds)
					.image(fromView)
					.transparency(0.0f));
		} else {
			mOverlay.setImage(null);
			mOverlay.setPositionFromBounds(bounds);
			mOverlay.setImage(fromView);
		}
		aMap.runOnDrawFrame();
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
				case HANDLER_SHOW_RADAR:
					if (msg.obj != null) {
						MinuteFallDto dto = (MinuteFallDto) msg.obj;
						if (dto.getPath() != null) {
							Bitmap bitmap = BitmapFactory.decodeFile(dto.getPath());
							if (bitmap != null) {
								showRadar(bitmap, dto.getP1(), dto.getP2(), dto.getP3(), dto.getP4());
							}
						}
						changeProgress(dto.getTime(), msg.arg2, msg.arg1);
					}
					break;
				case HANDLER_PROGRESS:
//				if (mDialog != null) {
//					if (msg.obj != null) {
//						int progress = (Integer) msg.obj;
//						mDialog.setPercent(progress);
//					}
//				}
					break;
				case HANDLER_LOAD_FINISHED:
				cancelDialog();
				llSeekBar.setVisibility(View.VISIBLE);
				llLegend.setVisibility(View.VISIBLE);
					break;
				case HANDLER_PAUSE:
					if (ivPlay != null) {
						ivPlay.setImageResource(R.drawable.iv_play2);
					}
					break;
				default:
					break;
			}

		};
	};

	private class RadarThread extends Thread {
		static final int STATE_NONE = 0;
		static final int STATE_PLAYING = 1;
		static final int STATE_PAUSE = 2;
		static final int STATE_CANCEL = 3;
		private List<MinuteFallDto> images;
		private int state;
		private int index;
		private int count;
		private boolean isTracking = false;

		public RadarThread(List<MinuteFallDto> images) {
			this.images = images;
			this.count = images.size();
			this.index = 0;
			this.state = STATE_NONE;
			this.isTracking = false;
		}

		public int getCurrentState() {
			return state;
		}

		@Override
		public void run() {
			super.run();
			this.state = STATE_PLAYING;
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
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private void sendRadar() {
			if (index >= count || index < 0) {
				index = 0;

//				if (mRadarThread != null) {
//					mRadarThread.pause();
//					
//					Message message = mHandler.obtainMessage();
//					message.what = HANDLER_PAUSE;
//					mHandler.sendMessage(message);
//					if (seekBar != null) {
//						seekBar.setProgress(100);
//					}
//				}
			}else {
				MinuteFallDto radar = images.get(index);
				Message message = mHandler.obtainMessage();
				message.what = HANDLER_SHOW_RADAR;
				message.obj = radar;
				message.arg1 = count - 1;
				message.arg2 = index ++;
				mHandler.sendMessage(message);
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
	private void changeProgress(long time, int progress, int max) {
		if (seekBar != null) {
			seekBar.setMax(max);
			seekBar.setProgress(progress);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		String value = time + "000";
		Date date = new Date(Long.valueOf(value));
		tvTime.setText(sdf.format(date));
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.llBack) {
			finish();
		}else if (v.getId() == R.id.ivPlay) {
			if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PLAYING) {
				mRadarThread.pause();
				ivPlay.setImageResource(R.drawable.iv_play2);
			} else if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PAUSE) {
				mRadarThread.play();
				ivPlay.setImageResource(R.drawable.iv_pause2);
			} else if (mRadarThread == null) {
				ivPlay.setImageResource(R.drawable.iv_pause2);
				if (mRadarThread != null) {
					mRadarThread.cancel();
					mRadarThread = null;
				}
				if (!images.isEmpty()) {
					mRadarThread = new RadarThread(images);
					mRadarThread.start();
				}
			}
		}else if (v.getId() == R.id.ivRank) {
			if (ivLegend.getVisibility() == View.VISIBLE) {
				ivLegend.setVisibility(View.INVISIBLE);
			}else {
				ivLegend.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mMapView != null) {
			mMapView.onDestroy();
		}
		if (mRadarManager != null) {
			mRadarManager.onDestory();
		}
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
	}

}
