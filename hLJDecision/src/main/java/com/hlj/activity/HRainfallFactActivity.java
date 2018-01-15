package com.hlj.activity;

/**
 * 降水实况
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.hlj.dto.AgriDto;
import com.hlj.adapter.HFactTableAdapter;
import com.hlj.stickygridheaders.StickyGridHeadersGridView;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.CustomHttpClient;
import com.hlj.adapter.HWeatherFactAdapter;
import com.hlj.dto.RangeDto;
import com.hlj.dto.StationDto;
import com.hlj.manager.DBManager;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shawn.cxwl.com.hlj.R;

@SuppressLint("SimpleDateFormat")
public class HRainfallFactActivity extends BaseActivity implements OnClickListener, OnCameraChangeListener{

	private Context mContext = null;
	private TextView tvTitle = null;
	private ImageView ivArrow = null;
	private LinearLayout llBack = null;
	private ListView tableListView = null;
	private HFactTableAdapter tableAdapter = null;
	private List<AgriDto> tableList = new ArrayList<>();
	private RelativeLayout reContainer = null;
	private MapView mapView = null;//高德地图
	private AMap aMap = null;//高德地图
	private LinearLayout llContainer = null;
	private ImageView ivExpand = null;
	private ProgressBar progressBar = null;
	private List<Marker> markers = new ArrayList<>();
	private List<StationDto> dataList = new ArrayList<>();//站点信息
	private String rainFact = "降水实况";
	private String rainFact1 = "逐小时降水实况";
	private String rainFact24 = "24小时降水实况";
	private String tempFact = "气温实况";
	private String tempFact1 = "逐小时气温实况";
	private String tempFact24H = "24小时最高气温实况";
	private String tempFact24L = "24小时最低气温实况";
	private String tempFact24A = "24小时平均气温实况";
	private String tempFact24V = "24小时变温实况";
	private String windFact = "风向风速实况";
	private String humidityFact = "相对湿度分析";
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHH");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("dd日HH时");
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");
	private SimpleDateFormat sdf4 = new SimpleDateFormat("dd日");
	private Map<String, StationDto> stationMap = new HashMap<>();//保存站点信息列表
	private String date = sdf3.format(new Date());
	private float zoom = 5.5f;
	
	private LinearLayout llGridView = null;
	private StickyGridHeadersGridView mGridView = null;
	private HWeatherFactAdapter mAdapter = null;
	private List<RangeDto> mList = new ArrayList<>();
	private int section = 1;
	private HashMap<String, Integer> sectionMap = new HashMap<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hactivity_rainfall_fact);
		mContext = this;
		initWidget();
		initAmap(savedInstanceState);
		initTableListView();
		initGridView();
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
		
		aMap.showMapText(false);
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(com.hlj.common.CONST.guizhouLatLng, zoom));
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.setOnCameraChangeListener(this);
		
		LatLngBounds bounds = new LatLngBounds.Builder()
		.include(new LatLng(1, 66))
		.include(new LatLng(60, 153))
		.build();
		aMap.addGroundOverlay(new GroundOverlayOptions()
			.anchor(0.5f, 0.5f)
			.positionFromBounds(bounds)
			.image(BitmapDescriptorFactory.fromResource(R.drawable.empty))
			.transparency(0.0f));
		aMap.runOnDrawFrame();
		
		CommonUtil.drawDistrict(mContext, aMap);//回执区域
	}
	
	@Override
	public void onCameraChange(CameraPosition arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		zoom = arg0.zoom;
		if (llGridView.getVisibility() == View.VISIBLE) {
			if (arg0.zoom < 5.0f) {
				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(com.hlj.common.CONST.guizhouLatLng, 5.5f));
			}
		}else {
			if (arg0.zoom < 5.0f) {
				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(com.hlj.common.CONST.guizhouLatLng, 6.0f));
			}
		}
	}
	
	/**
	 * 初始化数据库
	 */
	private Map<String, StationDto> getStationInfo() {
		HashMap<String, StationDto> map = new HashMap<>();
		DBManager dbManager = new DBManager(mContext);
		dbManager.openDateBase();
		dbManager.closeDatabase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		Cursor cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME1 + " where Pro like "+"\"%"+"黑龙江"+"%\"", null);
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			String lat = cursor.getString(cursor.getColumnIndex("LAT"));
			String lng = cursor.getString(cursor.getColumnIndex("LON"));
			String sid = cursor.getString(cursor.getColumnIndex("SID"));
			StationDto dto = new StationDto();
			dto.lat = lat;
			dto.lng = lng;
			dto.stationid = sid;
			if (!TextUtils.isEmpty(sid)) {
				map.put(sid, dto);
			}
		}
		return map;
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setOnClickListener(this);
		ivArrow = (ImageView) findViewById(R.id.ivArrow);
		ivArrow.setOnClickListener(this);
		reContainer = (RelativeLayout) findViewById(R.id.reContainer);
		llContainer = (LinearLayout) findViewById(R.id.llContainer);
		ivExpand = (ImageView) findViewById(R.id.ivExpand);
		ivExpand.setOnClickListener(this);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		llGridView = (LinearLayout) findViewById(R.id.llGridView);
		
		stationMap.clear();
		stationMap.putAll(getStationInfo());
		
		String type = getIntent().getStringExtra("type");
		if (TextUtils.equals(type, "rain")) {
			tvTitle.setText(rainFact);
			ivArrow.setVisibility(View.VISIBLE);
			tableList.clear();
			AgriDto dto = new AgriDto();
			dto.title = rainFact1;
			tableList.add(dto);
			dto = new AgriDto();
			dto.title = rainFact24;
			tableList.add(dto);
		}else if (TextUtils.equals(type, "temp")) {
			tvTitle.setText(tempFact);
			ivArrow.setVisibility(View.VISIBLE);
			tableList.clear();
			AgriDto dto = new AgriDto();
			dto.title = tempFact1;
			tableList.add(dto);
			dto = new AgriDto();
			dto.title = tempFact24H;
			tableList.add(dto);
			dto = new AgriDto();
			dto.title = tempFact24L;
			tableList.add(dto);
			dto = new AgriDto();
			dto.title = tempFact24A;
			tableList.add(dto);
			dto = new AgriDto();
			dto.title = tempFact24V;
			tableList.add(dto);
		}else if (TextUtils.equals(type, "wind")) {
			tvTitle.setText(windFact);
			ivArrow.setVisibility(View.GONE);
		}else if (TextUtils.equals(type, "humidity")) {
			tvTitle.setText(humidityFact);
			ivArrow.setVisibility(View.GONE);
		}
		
		asyncQuery("http://scapi.weather.com.cn/weather/rgwst?test=ncg");
	}
	
	/**
	 * 获取详情
	 */
	private void asyncQuery(String requestUrl) {
		progressBar.setVisibility(View.VISIBLE);
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(requestUrl);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private String method = "GET";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTask() {
		}
		
		@Override
		protected String doInBackground(String... url) {
			String result = null;
			if (method.equalsIgnoreCase("POST")) {
				result = CustomHttpClient.post(url[0], nvpList);
			} else if (method.equalsIgnoreCase("GET")) {
				result = CustomHttpClient.get(url[0]);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String requestResult) {
			super.onPostExecute(requestResult);
			progressBar.setVisibility(View.GONE);
			if (requestResult != null) {
				try {
					dataList.clear();
					JSONObject obj = new JSONObject(requestResult);
					if (!obj.isNull("date")) {
						date = obj.getString("date");
					}
					if (!obj.isNull("list")) {
						JSONArray listArray = obj.getJSONArray("list");
						for (int i = 0; i < listArray.length(); i++) {
							StationDto dto = new StationDto();
							JSONObject itemObj = listArray.getJSONObject(i);
							if (!itemObj.isNull("stationid")) {
								dto.stationid = itemObj.getString("stationid");
							}
							
							if (!itemObj.isNull("rainfall3")) {
								dto.rainfall3 = itemObj.getString("rainfall3");
							}
							if (!itemObj.isNull("rainfall6")) {
								dto.rainfall6 = itemObj.getString("rainfall6");
							}
							if (!itemObj.isNull("rainfall24")) {
								dto.rainfall24 = itemObj.getString("rainfall24");
							}
							
							if (!itemObj.isNull("temperature02")) {
								dto.temperature02 = itemObj.getString("temperature02");
							}
							if (!itemObj.isNull("temperature08")) {
								dto.temperature08 = itemObj.getString("temperature08");
							}
							if (!itemObj.isNull("temperature14")) {
								dto.temperature14 = itemObj.getString("temperature14");
							}
							if (!itemObj.isNull("temperature20")) {
								dto.temperature20 = itemObj.getString("temperature20");
							}
							
							if (!itemObj.isNull("statistics")) {
								JSONObject statisObj = itemObj.getJSONObject("statistics");
								if (!statisObj.isNull("maxtemperature")) {
									dto.maxtemperature = statisObj.getString("maxtemperature");
								}
								if (!statisObj.isNull("meantemperature")) {
									dto.meantemperature = statisObj.getString("meantemperature");
								}
								if (!statisObj.isNull("mintemperature")) {
									dto.mintemperature = statisObj.getString("mintemperature");
								}
							}
							
							if (!itemObj.isNull("4H")) {
								List<StationDto> tempList = new ArrayList<StationDto>();
								tempList.clear();
								JSONArray itemArray = itemObj.getJSONArray("4H");
								for (int j = 0; j < itemArray.length(); j++) {
									StationDto data = new StationDto();
									JSONObject object = itemArray.getJSONObject(j);
									if (!object.isNull("balltemp")) {
										data.balltemp = object.getString("balltemp");
									}
									if (!object.isNull("datatime")) {
										data.datatime = object.getString("datatime");
									}
									if (!object.isNull("humidity")) {
										data.humidity = object.getString("humidity");
									}
									if (!object.isNull("precipitation1h")) {
										data.precipitation1h = object.getString("precipitation1h");
									}
									if (!object.isNull("winddir")) {
										data.winddir = object.getString("winddir");
									}
									if (!object.isNull("windspeed")) {
										data.windspeed = object.getString("windspeed");
									}
									tempList.add(data);
								}
								dto.list.addAll(tempList);
							}
							
							if (dto.stationid != null && dto.stationid.length() <= 5) {
								dataList.add(dto);
							}
						}
						
						if (dataList.size() > 0) {
							int childSize = dataList.get(0).list.size();
							for (int k = 0; k < childSize; k++) {
								StationDto kDto = dataList.get(0).list.get(k);
								if (!TextUtils.isEmpty(kDto.datatime)) {
									try {
										String time = sdf2.format(sdf1.parse(kDto.datatime));
										if (!TextUtils.isEmpty(time)) {
											final TextView tvTime = new TextView(mContext);
											tvTime.setPadding(20, 0, 20, 0);
											tvTime.setTextSize(CommonUtil.dip2px(mContext, 5));
											tvTime.setTextColor(getResources().getColor(R.color.white));
											tvTime.setText(time);
											tvTime.setTag(k);
											tvTime.setGravity(Gravity.CENTER);
											tvTime.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));  
											tvTime.setOnClickListener(new OnClickListener() {
												@Override
												public void onClick(View arg0) {
													for (int m = 0; m < llContainer.getChildCount(); m++) {
														TextView textView = (TextView) llContainer.getChildAt(m);
														if (textView != null) {
															if (m == Integer.valueOf(String.valueOf(arg0.getTag()))) {
																textView.setTextColor(getResources().getColor(R.color.title_bg));
																if (tableList.size() > 0) {
																	String title = tableList.get(0).title;
																	tvTitle.setText(title);
																}
																addMarkers(tvTitle.getText().toString(), m);
															}else {
																textView.setTextColor(getResources().getColor(R.color.white));
															}
														}
													}
												}
											});
											if (k == 0) {
												tvTime.setTextColor(getResources().getColor(R.color.title_bg));
												if (tableList.size() > 0) {
													String title = tableList.get(0).title;
													tvTitle.setText(title);
												}
												addMarkers(tvTitle.getText().toString(), k);
											}
											llContainer.addView(tvTime, k);
											
											if (llContainer.getChildCount() == 0) {
												llContainer.setVisibility(View.GONE);
											}else {
												llContainer.setVisibility(View.VISIBLE);
											}
										}
									} catch (ParseException e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		@SuppressWarnings("unused")
		private void setParams(NameValuePair nvp) {
			nvpList.add(nvp);
		}

		private void setMethod(String method) {
			this.method = method;
		}

		private void setTimeOut(int timeOut) {
			CustomHttpClient.TIME_OUT = timeOut;
		}

		/**
		 * 取消当前task
		 */
		@SuppressWarnings("unused")
		private void cancelTask() {
			CustomHttpClient.shuttdownRequest();
			this.cancel(true);
		}
	}
	
	/**
	 * 添加markers
	 * @param title 根据标题来区分添加marker的种类
	 * @param index 为添加marker的时次
	 */
	private void addMarkers(String title, int index) {
		removeMarkers();
		for (int i = 0; i < dataList.size(); i++) {
			StationDto dto = dataList.get(i);
			if (!TextUtils.isEmpty(dto.stationid)) {
				StationDto data = stationMap.get(dto.stationid);
				if (data != null) {
					if (data.lat != null && data.lng != null) {
						LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						View view = inflater.inflate(R.layout.rainfall_fact_marker_view, null);
						TextView tvValue = (TextView) view.findViewById(R.id.tvValue);
						ImageView ivWind = (ImageView) view.findViewById(R.id.ivWind);
						String value = null;
						String colorType = "";
						if (TextUtils.equals(title, rainFact) || TextUtils.equals(title, rainFact1)) {
							value = dto.list.get(index).precipitation1h;
							colorType = "jiangshui";
							tvValue.setVisibility(View.VISIBLE);
							ivWind.setVisibility(View.GONE);
						}else if (TextUtils.equals(title, rainFact24)) {
							value = dto.rainfall24;
							colorType = "jiangshui";
							tvValue.setVisibility(View.VISIBLE);
							ivWind.setVisibility(View.GONE);
						}else if (TextUtils.equals(title, tempFact) || TextUtils.equals(title, tempFact1)) {
							value = dto.list.get(index).balltemp;
							colorType = "wendu";
							tvValue.setVisibility(View.VISIBLE);
							ivWind.setVisibility(View.GONE);
						}else if (TextUtils.equals(title, tempFact24H)) {
							value = dto.maxtemperature;
							colorType = "wendu";
							tvValue.setVisibility(View.VISIBLE);
							ivWind.setVisibility(View.GONE);
						}else if (TextUtils.equals(title, tempFact24L)) {
							value = dto.mintemperature;
							colorType = "wendu";
							tvValue.setVisibility(View.VISIBLE);
							ivWind.setVisibility(View.GONE);
						}else if (TextUtils.equals(title, tempFact24A)) {
							value = dto.meantemperature;
							colorType = "wendu";
							tvValue.setVisibility(View.VISIBLE);
							ivWind.setVisibility(View.GONE);
						}else if (TextUtils.equals(title, tempFact24V) && index == 0) {
							value = dto.temperature20;
							colorType = "bianwen";
							tvValue.setVisibility(View.VISIBLE);
							ivWind.setVisibility(View.GONE);
						}else if (TextUtils.equals(title, tempFact24V) && index == 1) {
							value = dto.temperature14;
							colorType = "bianwen";
							tvValue.setVisibility(View.VISIBLE);
							ivWind.setVisibility(View.GONE);
						}else if (TextUtils.equals(title, tempFact24V) && index == 2) {
							value = dto.temperature08;
							colorType = "bianwen";
							tvValue.setVisibility(View.VISIBLE);
							ivWind.setVisibility(View.GONE);
						}else if (TextUtils.equals(title, tempFact24V) && index == 3) {
							value = dto.temperature02;
							colorType = "bianwen";
							tvValue.setVisibility(View.VISIBLE);
							ivWind.setVisibility(View.GONE);
						}else if (TextUtils.equals(title, windFact)) {
							value = dto.list.get(index).windspeed;
							String rotation = dto.list.get(index).winddir;
							tvValue.setVisibility(View.GONE);
							ivWind.setVisibility(View.VISIBLE);
							
							if (!TextUtils.isEmpty(value)) {
								Bitmap b = CommonUtil.getWindMarker(mContext, Float.valueOf(value));
								if (b != null) {
									Matrix matrix = new Matrix();
									matrix.postScale(1, 1);
									if (!TextUtils.isEmpty(rotation)) {
										matrix.postRotate(Float.valueOf(rotation));
									}
									Bitmap bitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
									if (bitmap != null) {
										ivWind.setImageBitmap(bitmap);
									}
								}
							}
						}else if (TextUtils.equals(title, humidityFact)) {
							value = dto.list.get(index).humidity;
							colorType = "shidu";
							tvValue.setVisibility(View.VISIBLE);
							ivWind.setVisibility(View.GONE);
						}
						
						if (!TextUtils.isEmpty(value)) {
							if (tvValue.getVisibility() == View.VISIBLE) {
								tvValue.setText(value);
								tvValue.setTextColor(CommonUtil.colorForValue(colorType, Float.valueOf(value)));
							}
							MarkerOptions options = new MarkerOptions();
							options.anchor(0.5f, 0.5f);
							options.position(new LatLng(Double.valueOf(data.lat), Double.valueOf(data.lng)));
							options.icon(BitmapDescriptorFactory.fromView(view));
							Marker marker = aMap.addMarker(options);
							markers.add(marker);
						}
						
					}
				}
			}
		}
	}
	
	private void initTableListView() {
		tableListView = (ListView) findViewById(R.id.tableListView);
		tableAdapter = new HFactTableAdapter(mContext, tableList);
		tableListView.setAdapter(tableAdapter);
		tableListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				switchData();
				llContainer.removeAllViews();
				String title = tableList.get(arg2).title;
				tvTitle.setText(title);
				addMarkers(tvTitle.getText().toString(), 0);
				
				if (TextUtils.equals(title, rainFact) || TextUtils.equals(title, rainFact1)
						|| TextUtils.equals(title, tempFact) || TextUtils.equals(title, tempFact1)
						|| TextUtils.equals(title, windFact)
						|| TextUtils.equals(title, humidityFact)) {
					if (dataList.size() > 0) {
						int childSize = dataList.get(0).list.size();
						for (int k = 0; k < childSize; k++) {
							StationDto kDto = dataList.get(0).list.get(k);
							if (!TextUtils.isEmpty(kDto.datatime)) {
								try {
									String time = sdf2.format(sdf1.parse(kDto.datatime));
									if (!TextUtils.isEmpty(time)) {
										final TextView tvTime = new TextView(mContext);
										tvTime.setPadding(20, 0, 20, 0);
										tvTime.setTextSize(CommonUtil.dip2px(mContext, 5));
										tvTime.setTextColor(getResources().getColor(R.color.white));
										tvTime.setText(time);
										tvTime.setTag(k);
										tvTime.setGravity(Gravity.CENTER);
										tvTime.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));  
										tvTime.setOnClickListener(new OnClickListener() {
											@Override
											public void onClick(View arg0) {
												for (int m = 0; m < llContainer.getChildCount(); m++) {
													TextView textView = (TextView) llContainer.getChildAt(m);
													if (textView != null) {
														if (m == Integer.valueOf(String.valueOf(arg0.getTag()))) {
															textView.setTextColor(getResources().getColor(R.color.title_bg));
															addMarkers(tvTitle.getText().toString(), m);
														}else {
															textView.setTextColor(getResources().getColor(R.color.white));
														}
													}
												}
											}
										});
										if (k == 0) {
											tvTime.setTextColor(getResources().getColor(R.color.title_bg));
											addMarkers(tvTitle.getText().toString(), k);
										}
										llContainer.addView(tvTime, k);
									}
								} catch (ParseException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}else if (TextUtils.equals(title, tempFact24V)) {
					StationDto dto = dataList.get(0);
					List<String> list = new ArrayList<String>();
					list.clear();
					list.add(dto.temperature20);
					list.add(dto.temperature14);
					list.add(dto.temperature08);
					list.add(dto.temperature02);
					for (int n = 0; n < list.size(); n++) {
						String time = null;
						try {
							if (n == 0) {
								time = sdf4.format(sdf3.parse(date))+"02时";
							}else if (n == 1) {
								time = sdf4.format(sdf3.parse(date))+"08时";
							}else if (n == 2) {
								time = sdf4.format(sdf3.parse(date))+"14时";
							}else if (n == 3) {
								time = sdf4.format(sdf3.parse(date))+"20时";
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
						if (!TextUtils.isEmpty(list.get(n))) {
							if (!TextUtils.isEmpty(time)) {
								final TextView tvTime = new TextView(mContext);
								tvTime.setPadding(20, 0, 20, 0);
								tvTime.setTextSize(CommonUtil.dip2px(mContext, 5));
								tvTime.setTextColor(getResources().getColor(R.color.white));
								tvTime.setText(time);
								tvTime.setTag(n);
								tvTime.setGravity(Gravity.CENTER);
								tvTime.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));  
								tvTime.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View arg0) {
										for (int m = 0; m < llContainer.getChildCount(); m++) {
											TextView textView = (TextView) llContainer.getChildAt(m);
											if (textView != null) {
												if (m == Integer.valueOf(String.valueOf(arg0.getTag()))) {
													textView.setTextColor(getResources().getColor(R.color.title_bg));
													addMarkers(tvTitle.getText().toString(), m);
												}else {
													textView.setTextColor(getResources().getColor(R.color.white));
												}
											}
										}
									}
								});
								if (n == 0) {
									tvTime.setTextColor(getResources().getColor(R.color.title_bg));
									addMarkers(tvTitle.getText().toString(), n);
								}
								llContainer.addView(tvTime, n);
							}
						}
						
						if (llContainer.getChildCount() > 0) {
							llContainer.setVisibility(View.VISIBLE);
						}else {
							llContainer.setVisibility(View.GONE);
						}
					}
				}
				
				if (llContainer.getChildCount() == 0) {
					llContainer.setVisibility(View.GONE);
				}else {
					llContainer.setVisibility(View.VISIBLE);
				}
			}
		});
	}
	
	private void removeMarkers() {
		for (int i = 0; i < markers.size(); i++) {
			Marker marker = markers.get(i);
			marker.remove();
		}
		markers.clear();
	}
	
	/**
	 * 切换数据
	 */
	private void switchData() {
		if (reContainer.getVisibility() == View.GONE) {
			startAnimation(false, reContainer);
			reContainer.setVisibility(View.VISIBLE);
			ivArrow.setImageResource(R.drawable.iv_arrow_up);
		}else {
			startAnimation(true, reContainer);
			reContainer.setVisibility(View.GONE);
			ivArrow.setImageResource(R.drawable.iv_arrow_down);
		}
	}
	
	/**
	 * @param flag false为显示map，true为显示list
	 */
	private void startAnimation(final boolean flag, final RelativeLayout llContainer) {
		//列表动画
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation animation = null;
		if (flag == false) {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,-1.0f,
					Animation.RELATIVE_TO_SELF,0f);
		}else {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,-1.0f);
		}
		animation.setDuration(400);
		animationSet.addAnimation(animation);
		animationSet.setFillAfter(true);
		llContainer.startAnimation(animationSet);
		animationSet.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				llContainer.clearAnimation();
			}
		});
	}
	
//	/**
//	 * 获取详情
//	 */
//	private void asyncQuery(String requestUrl) {
//		progressBar.setVisibility(View.VISIBLE);
//		HttpAsyncTask task = new HttpAsyncTask();
//		task.setMethod("GET");
//		task.setTimeOut(CustomHttpClient.TIME_OUT);
//		task.execute(requestUrl);
//	}
//	
//	/**
//	 * 异步请求方法
//	 * @author dell
//	 *
//	 */
//	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
//		private String method = "GET";
//		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
//		
//		public HttpAsyncTask() {
//		}
//		
//		@Override
//		protected String doInBackground(String... url) {
//			String result = null;
//			if (method.equalsIgnoreCase("POST")) {
//				result = CustomHttpClient.post(url[0], nvpList);
//			} else if (method.equalsIgnoreCase("GET")) {
//				result = CustomHttpClient.get(url[0]);
//			}
//			return result;
//		}
//
//		@Override
//		protected void onPostExecute(String requestResult) {
//			super.onPostExecute(requestResult);
//			progressBar.setVisibility(View.GONE);
//			if (requestResult != null) {
//				try {
//					JSONObject obj = new JSONObject(requestResult);
//					if (!obj.isNull("color")) {
//						colorType = obj.getString("color");
//					}
//					if (!obj.isNull("items")) {
//						rainList.clear();
//						llContainer.removeAllViews();
//						JSONArray array = obj.getJSONArray("items");
//						for (int i = 0; i < array.length(); i++) {
//							JSONObject itemObj = array.getJSONObject(i);
//							AgriDto dto = new AgriDto();
//							dto.dataUrl = itemObj.getString("src");
//							dto.time = itemObj.getString("text");
//							rainList.add(dto);
//							
//							final TextView tvTime = new TextView(mContext);
//							tvTime.setPadding(20, 0, 20, 0);
//							tvTime.setTextSize(CommonUtil.dip2px(mContext, 5));
//							tvTime.setTextColor(getResources().getColor(R.color.white));
//							tvTime.setText(dto.time);
//							tvTime.setTag(i);
//							tvTime.setOnClickListener(new OnClickListener() {
//								@Override
//								public void onClick(View arg0) {
//									for (int m = 0; m < llContainer.getChildCount(); m++) {
//										TextView textView = (TextView) llContainer.getChildAt(m);
//										if (textView != null) {
//											if (m == Integer.valueOf(String.valueOf(arg0.getTag()))) {
//												textView.setTextColor(getResources().getColor(R.color.title_bg));
//												asyncQueryJson(rainList.get(m).dataUrl);
//											}else {
//												textView.setTextColor(getResources().getColor(R.color.white));
//											}
//										}
//									}
//								}
//							});
//							llContainer.addView(tvTime, i);
//							
//							if (i == 0) {
//								tvTime.setTextColor(getResources().getColor(R.color.title_bg));
//								if (!TextUtils.isEmpty(dto.dataUrl)) {
//									asyncQueryJson(dto.dataUrl);
//								}
//							}
//						}
//						
//						hScrollView.setVisibility(View.VISIBLE);
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//
//		@SuppressWarnings("unused")
//		private void setParams(NameValuePair nvp) {
//			nvpList.add(nvp);
//		}
//
//		private void setMethod(String method) {
//			this.method = method;
//		}
//
//		private void setTimeOut(int timeOut) {
//			CustomHttpClient.TIME_OUT = timeOut;
//		}
//
//		/**
//		 * 取消当前task
//		 */
//		@SuppressWarnings("unused")
//		private void cancelTask() {
//			CustomHttpClient.shuttdownRequest();
//			this.cancel(true);
//		}
//	}
//	
//	/**
//	 * 获取详情
//	 */
//	private void asyncQueryJson(String requestUrl) {
//		progressBar.setVisibility(View.VISIBLE);
//		HttpAsyncTaskJson task = new HttpAsyncTaskJson();
//		task.setMethod("GET");
//		task.setTimeOut(CustomHttpClient.TIME_OUT);
//		task.execute(requestUrl);
//	}
//	
//	/**
//	 * 异步请求方法
//	 * @author dell
//	 *
//	 */
//	private class HttpAsyncTaskJson extends AsyncTask<String, Void, String> {
//		private String method = "GET";
//		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
//		
//		public HttpAsyncTaskJson() {
//		}
//		
//		@Override
//		protected String doInBackground(String... url) {
//			String result = null;
//			if (method.equalsIgnoreCase("POST")) {
//				result = CustomHttpClient.post(url[0], nvpList);
//			} else if (method.equalsIgnoreCase("GET")) {
//				result = CustomHttpClient.get(url[0]);
//			}
//			return result;
//		}
//
//		@Override
//		protected void onPostExecute(String requestResult) {
//			super.onPostExecute(requestResult);
//			progressBar.setVisibility(View.GONE);
//			if (requestResult != null) {
//				try {
//					JSONObject obj = new JSONObject(requestResult);
//					if (!obj.isNull("features")) {
//						for (int i = 0; i < markers.size(); i++) {
//							Marker marker = markers.get(i);
//							marker.remove();
//						}
//						markers.clear();
//						JSONArray array = obj.getJSONArray("features");
//						for (int i = 0; i < array.length(); i++) {
//							JSONObject itemObj = array.getJSONObject(i);
//							JSONObject properties = itemObj.getJSONObject("properties");
//							String prov_name = properties.getString("prov_name");
//							if (TextUtils.equals(prov_name, "hei_long_jiang")) {
//								String value = properties.getString("value");
//								
//								JSONObject geometry = itemObj.getJSONObject("geometry");
//								JSONArray coordinates = geometry.getJSONArray("coordinates");
//								for (int j = 0; j < coordinates.length(); j++) {
//									double lng = coordinates.getDouble(0);
//									double lat = coordinates.getDouble(1);
//									
//									LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//									View view = inflater.inflate(R.layout.rainfall_fact_marker_view, null);
//									TextView tvValue = (TextView) view.findViewById(R.id.tvValue);
//									if (!TextUtils.isEmpty(value)) {
//										tvValue.setText(value);
//										if (colorType != null) {
//											tvValue.setTextColor(CommonUtil.colorForValue(colorType, Float.valueOf(value)));
//										}
//									}
//									MarkerOptions options = new MarkerOptions();
//									options.anchor(0.5f, 0.5f);
//									options.position(new LatLng(lat, lng));
//									options.icon(BitmapDescriptorFactory.fromView(view));
//									Marker marker = aMap.addMarker(options);
//									markers.add(marker);
//								}
//							}
//						}
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//
//		@SuppressWarnings("unused")
//		private void setParams(NameValuePair nvp) {
//			nvpList.add(nvp);
//		}
//
//		private void setMethod(String method) {
//			this.method = method;
//		}
//
//		private void setTimeOut(int timeOut) {
//			CustomHttpClient.TIME_OUT = timeOut;
//		}
//
//		/**
//		 * 取消当前task
//		 */
//		@SuppressWarnings("unused")
//		private void cancelTask() {
//			CustomHttpClient.shuttdownRequest();
//			this.cancel(true);
//		}
//	}
	
	/**
	 * 初始化initGridView
	 */
	private void initGridView() {
		String[] stations = getResources().getStringArray(R.array.guizhou_stations);
		for (int i = 0; i < stations.length; i++) {
			String[] value = stations[i].split(",");
			RangeDto dto = new RangeDto();
			dto.cityId = value[0];
			dto.areaName = value[1];
			dto.cityName = value[2];
			mList.add(dto);
		}

		for (int i = 0; i < mList.size(); i++) {
			RangeDto sectionDto = mList.get(i);
			if (!sectionMap.containsKey(sectionDto.cityName)) {
				sectionDto.section = section;
				sectionMap.put(sectionDto.cityName, section);
				section++;
			}else {
				sectionDto.section = sectionMap.get(sectionDto.cityName);
			}
			mList.set(i, sectionDto);
		}
		
		mGridView = (StickyGridHeadersGridView) findViewById(R.id.stickyGridView);
		mAdapter = new HWeatherFactAdapter(mContext, mList);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				RangeDto dto = mList.get(arg2);
				Intent intent = new Intent(mContext, TrendDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
	
	private void translateAnimation(final boolean flag, final LinearLayout llGridView) {
		AnimationSet animup = new AnimationSet(true);
		TranslateAnimation mytranslateanimup0 = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,1.0f);
		mytranslateanimup0.setDuration(400);
		TranslateAnimation mytranslateanimup1 = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,1.f,
				Animation.RELATIVE_TO_SELF,0f);
		mytranslateanimup1.setDuration(400);
		mytranslateanimup1.setStartOffset(200);
		if (flag) {
			animup.addAnimation(mytranslateanimup0);
		}else {
			animup.addAnimation(mytranslateanimup1);
		}
		animup.setFillAfter(true);
		llGridView.startAnimation(animup);
		animup.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				llGridView.clearAnimation();
				if (flag) {
					ivExpand.setImageResource(R.drawable.iv_collose1);
				}else {
					ivExpand.setImageResource(R.drawable.iv_expand1);
					llGridView.setVisibility(View.VISIBLE);
				}
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (reContainer.getVisibility() == View.GONE) {
				finish();
			}else {
				startAnimation(true, reContainer);
				reContainer.setVisibility(View.GONE);
				ivArrow.setImageResource(R.drawable.iv_arrow_down);
			}
		}
		return false;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			if (reContainer.getVisibility() == View.GONE) {
				finish();
			}else {
				startAnimation(true, reContainer);
				reContainer.setVisibility(View.GONE);
				ivArrow.setImageResource(R.drawable.iv_arrow_down);
			}
			break;
		case R.id.tvTitle:
		case R.id.ivArrow:
			String type = getIntent().getStringExtra("type");
			if (TextUtils.equals(type, "rain") || TextUtils.equals(type, "temp")) {
				switchData();
			}
			break;
		case R.id.ivExpand:
			if (mGridView != null) {
				if (llGridView.getVisibility() == View.VISIBLE) {
					translateAnimation(true, llGridView);
					llGridView.setVisibility(View.GONE);
					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(com.hlj.common.CONST.guizhouLatLng, 6.0f));
				}else {
					translateAnimation(false, llGridView);
					aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(com.hlj.common.CONST.guizhouLatLng, 5.5f));
				}
			}
			break;
			
		default:
			break;
		}
	}

}
