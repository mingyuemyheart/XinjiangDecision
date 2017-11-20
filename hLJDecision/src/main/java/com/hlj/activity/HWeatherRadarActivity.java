package com.hlj.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import shawn.cxwl.com.hlj.decision.R;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.hlj.dto.AgriDto;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.CustomHttpClient;
import com.hlj.adapter.WeatherRadarAdapter;

/**
 * 天气雷达
 * @author shawn_sun
 *
 */
public class HWeatherRadarActivity extends BaseActivity implements OnClickListener, OnMarkerClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private MapView mapView = null;//高德地图
	private AMap aMap = null;//高德地图
	private AgriDto data = null;
	private ImageView ivExpand = null;
	private GridView mGridView = null;
	private WeatherRadarAdapter mAdapter = null;
	private List<AgriDto> mList = new ArrayList<>();
	private RelativeLayout llGridView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hactivity_weather_radar);
		mContext = this;
		initAmap(savedInstanceState);
		initWidget();
		initGridView();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		ivExpand = (ImageView) findViewById(R.id.ivExpand);
		ivExpand.setOnClickListener(this);
		llGridView = (RelativeLayout) findViewById(R.id.llGridView);
		
		data = getIntent().getExtras().getParcelable("data");
		if (data != null) {
			if (data.name != null) {
				tvTitle.setText(data.name);
			}
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
		
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(com.hlj.common.CONST.guizhouLatLng, 5.5f));
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.setOnMarkerClickListener(this);
		
		getRadarData("http://decision-admin.tianqi.cn/Home/extra/getHljRadarData");
		CommonUtil.drawHLJJson(mContext, aMap);
		CommonUtil.drawJGDQJson(mContext, aMap);
	}
	
	/**
	 * 获取雷达图片集信息
	 */
	private void getRadarData(String url) {
		mList.clear();
		AgriDto dto = new AgriDto();
		dto.name = "东北";
		dto.lat = 0;
		dto.lng = 0;
		dto.radarId = "JC_RADAR_DB_JB";
		mList.add(dto);
		
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(url);
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
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				try {
					JSONArray array = new JSONArray(result);
					for (int i = 0; i < array.length(); i++) {
						JSONObject itemObj = array.getJSONObject(i);
						AgriDto dto = new AgriDto();
						if (!itemObj.isNull("name")) {
							dto.name = itemObj.getString("name");
						}
						if (!itemObj.isNull("lat")) {
							dto.lat = Double.valueOf(itemObj.getString("lat"));
						}
						if (!itemObj.isNull("lon")) {
							dto.lng = Double.valueOf(itemObj.getString("lon"));
						}
						if (!itemObj.isNull("id")) {
							dto.radarId = itemObj.getString("id");
						}
						mList.add(dto);
					}
					
					if (mList.size() > 0 && mAdapter != null) {
						mAdapter.notifyDataSetChanged();
					}
					
					addMarkerToMap();
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
	
	private void addMarkerToMap() {
		for (int i = 1; i < mList.size(); i++) {
			AgriDto dto = mList.get(i);
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.wind_dir_speed_marker_view, null);
			ImageView ivWind = (ImageView) view.findViewById(R.id.ivWind);
			ivWind.setImageResource(R.drawable.iv_radar);
			MarkerOptions options = new MarkerOptions();
			options.title(dto.name);
			options.snippet(dto.radarId);
			options.anchor(0.5f, 0.5f);
			options.position(new LatLng(dto.lat, dto.lng));
			options.icon(BitmapDescriptorFactory.fromView(view));
			aMap.addMarker(options);
		}
	}
	
	private void initGridView() {
		mGridView = (GridView) findViewById(R.id.gridView);
		mAdapter = new WeatherRadarAdapter(mContext, mList);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				AgriDto dto = mList.get(arg2);
				Intent intent = new Intent(HWeatherRadarActivity.this, WeatherRadarDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		AgriDto dto = new AgriDto();
		dto.name = marker.getTitle();
		dto.radarId = marker.getSnippet();
		
		Intent intent = new Intent(HWeatherRadarActivity.this, WeatherRadarDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable("data", dto);
		intent.putExtras(bundle);
		startActivity(intent);
		return true;
	}
	
	private void translateAnimation(final boolean flag, final RelativeLayout llGridView) {
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
