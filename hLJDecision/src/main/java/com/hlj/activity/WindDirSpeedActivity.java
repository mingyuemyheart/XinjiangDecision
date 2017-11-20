package com.hlj.activity;

/**
 * 风向风速实况
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import shawn.cxwl.com.hlj.decision.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.hlj.dto.AgriDto;
import com.hlj.stickygridheaders.StickyGridHeadersGridView;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.CustomHttpClient;
import com.hlj.adapter.HWeatherFactAdapter;
import com.hlj.dto.RangeDto;

public class WindDirSpeedActivity extends BaseActivity implements OnClickListener{

	private Context mContext = null;
	private TextView tvTitle = null;
	private LinearLayout llBack = null;
	private AgriDto data = null;
	private List<AgriDto> rainList = new ArrayList<AgriDto>();
	private MapView mapView = null;//高德地图
	private AMap aMap = null;//高德地图
	private HorizontalScrollView hScrollView = null;
	private LinearLayout llContainer = null;
	private ImageView ivExpand = null;
	private ProgressBar progressBar = null;
	private List<Marker> markers = new ArrayList<Marker>();
	
	private LinearLayout llGridView = null;
	private StickyGridHeadersGridView mGridView = null;
	private HWeatherFactAdapter mAdapter = null;
	private List<RangeDto> mList = new ArrayList<RangeDto>();
	private int section = 1;
	private HashMap<String, Integer> sectionMap = new HashMap<String, Integer>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wind_dir_speed);
		mContext = this;
		initWidget();
		initAmap(savedInstanceState);
		initGridView();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setOnClickListener(this);
		hScrollView = (HorizontalScrollView) findViewById(R.id.hScrollView);
		llContainer = (LinearLayout) findViewById(R.id.llContainer);
		ivExpand = (ImageView) findViewById(R.id.ivExpand);
		ivExpand.setOnClickListener(this);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		llGridView = (LinearLayout) findViewById(R.id.llGridView);
		
		data = getIntent().getExtras().getParcelable("data");
		if (data != null) {
			tvTitle.setText(data.name);
			asyncQuery(data.dataUrl);
		}
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
		aMap.setMapType(AMap.MAP_TYPE_NIGHT);
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(com.hlj.common.CONST.guizhouLatLng, 5.5f));
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.setOnCameraChangeListener(new OnCameraChangeListener() {
			@Override
			public void onCameraChangeFinish(CameraPosition arg0) {
//				handler.removeMessages(0);
//				Message msg = new Message();
//				msg.what = 0;
//				msg.obj = arg0.zoom;
//				handler.sendMessageDelayed(msg, 1000);
			}
			
			@Override
			public void onCameraChange(CameraPosition arg0) {
			}
		});
		
		CommonUtil.drawDistrict(mContext, aMap);//回执区域
	}
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(com.hlj.common.CONST.guizhouLatLng, (Float) msg.obj));
				break;

			default:
				break;
			}
		};
	};
	
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
					JSONObject obj = new JSONObject(requestResult);
					if (!obj.isNull("items")) {
						rainList.clear();
						llContainer.removeAllViews();
						JSONArray array = obj.getJSONArray("items");
						for (int i = 0; i < array.length(); i++) {
							JSONObject itemObj = array.getJSONObject(i);
							AgriDto dto = new AgriDto();
							dto.dataUrl = itemObj.getString("src");
							dto.time = itemObj.getString("text");
							rainList.add(dto);
							
							final TextView tvTime = new TextView(mContext);
							tvTime.setPadding(20, 0, 20, 0);
							tvTime.setTextSize(CommonUtil.dip2px(mContext, 5));
							tvTime.setTextColor(getResources().getColor(R.color.white));
							tvTime.setText(dto.time);
							tvTime.setTag(i);
							tvTime.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View arg0) {
									for (int m = 0; m < llContainer.getChildCount(); m++) {
										TextView textView = (TextView) llContainer.getChildAt(m);
										if (textView != null) {
											if (m == Integer.valueOf(String.valueOf(arg0.getTag()))) {
												textView.setTextColor(getResources().getColor(R.color.title_bg));
												asyncQueryJson(rainList.get(m).dataUrl);
											}else {
												textView.setTextColor(getResources().getColor(R.color.white));
											}
										}
									}
								}
							});
							llContainer.addView(tvTime, i);
							
							if (i == 0) {
								tvTime.setTextColor(getResources().getColor(R.color.title_bg));
								if (!TextUtils.isEmpty(dto.dataUrl)) {
									asyncQueryJson(dto.dataUrl);
								}
							}
						}
						
						hScrollView.setVisibility(View.VISIBLE);
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
	 * 获取详情
	 */
	private void asyncQueryJson(String requestUrl) {
		progressBar.setVisibility(View.VISIBLE);
		HttpAsyncTaskJson task = new HttpAsyncTaskJson();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(requestUrl);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTaskJson extends AsyncTask<String, Void, String> {
		private String method = "GET";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTaskJson() {
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
					JSONObject obj = new JSONObject(requestResult);
					if (!obj.isNull("features")) {
						for (int i = 0; i < markers.size(); i++) {
							Marker marker = markers.get(i);
							marker.remove();
						}
						markers.clear();
						JSONArray array = obj.getJSONArray("features");
						for (int i = 0; i < array.length(); i++) {
							JSONObject itemObj = array.getJSONObject(i);
							JSONObject properties = itemObj.getJSONObject("properties");
							String prov_name = properties.getString("prov_name");
							if (TextUtils.equals(prov_name, "hei_long_jiang")) {
								String speed = properties.getString("speed");
								String rotation = properties.getString("rotation");
								
								JSONObject geometry = itemObj.getJSONObject("geometry");
								JSONArray coordinates = geometry.getJSONArray("coordinates");
								for (int j = 0; j < coordinates.length(); j++) {
									double lng = coordinates.getDouble(0);
									double lat = coordinates.getDouble(1);
									
									LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
									View view = inflater.inflate(R.layout.wind_dir_speed_marker_view, null);
									ImageView ivWind = (ImageView) view.findViewById(R.id.ivWind);
									if (!TextUtils.isEmpty(speed)) {
										Bitmap b = CommonUtil.getWindMarker(mContext, Integer.valueOf(speed));
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
									MarkerOptions options = new MarkerOptions();
									options.anchor(0.5f, 0.5f);
									options.position(new LatLng(lat, lng));
									options.icon(BitmapDescriptorFactory.fromView(view));
									Marker marker = aMap.addMarker(options);
									markers.add(marker);
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
					ivExpand.setImageResource(R.drawable.iv_collose);
				}else {
					ivExpand.setImageResource(R.drawable.iv_expand);
					llGridView.setVisibility(View.VISIBLE);
				}
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
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
