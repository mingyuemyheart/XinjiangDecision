package com.hlj.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.hlj.activity.HHeadWarningActivity;
import com.hlj.activity.HWarningDetailActivity;
import com.hlj.adapter.HWarningAdapter;
import com.hlj.common.CONST;
import com.hlj.dto.WarningDto;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.OkHttpUtil;

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

/**
 * 预警
 * @author shawn_sun
 *
 */

public class HWeatherWarningFragment extends Fragment implements View.OnClickListener, AMap.OnMapClickListener,
		AMap.OnMarkerClickListener, AMap.InfoWindowAdapter, AMap.OnCameraChangeListener {

	private ImageView ivList = null;
	private ImageView ivNation = null;
	private ImageView ivRefresh = null;
	private boolean isShanxi = true;
	private TextView tvPrompt = null;//没有数据时提示
	private TextView tvPro = null;//省级预警
	private MapView mapView = null;//高德地图
	private AMap aMap = null;//高德地图
	private Marker selectMarker = null;
	private String warningUrl = "http://decision-admin.tianqi.cn/Home/extra/getwarns?order=0";//预警地址
	private List<WarningDto> warningList = new ArrayList<>();
	private List<WarningDto> nationList = new ArrayList<>();
	private List<WarningDto> proList = new ArrayList<>();
	private List<WarningDto> cityList = new ArrayList<>();
	private List<WarningDto> disList = new ArrayList<>();
	private List<Marker> proMarkers = new ArrayList<>();
	private List<Marker> cityMarkers = new ArrayList<>();
	private List<Marker> disMarkers = new ArrayList<>();
	private List<WarningDto> shanxiList = new ArrayList<>();
	private List<Marker> shanxiMarkers = new ArrayList<>();
	private int count1, count2, count3;
	private RelativeLayout reList = null;
	private ListView cityListView = null;
	private HWarningAdapter cityAdapter = null;
	private List<WarningDto> mList = new ArrayList<>();
	private EditText etSearch = null;
	private List<WarningDto> searchList = new ArrayList<>();
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private float zoom = 5.5f;
	private LatLng leftlatlng = null;
	private LatLng rightLatlng = null;
	private List<WarningDto> shanxiProWarning = new ArrayList<>();//山西省级预警

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.hfragment_weather_warning, null);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initAmap(savedInstanceState, view);
		initWidget(view);
		initListView(view);
	}

	/**
	 * 初始化控件
	 */
	private void initWidget(View view) {
		ivList = (ImageView) view.findViewById(R.id.ivList);
		ivList.setOnClickListener(this);
		ivNation = (ImageView) view.findViewById(R.id.ivNation);
		ivNation.setOnClickListener(this);
		ivRefresh = (ImageView) view.findViewById(R.id.ivRefresh);
		ivRefresh.setOnClickListener(this);
		etSearch = (EditText) view.findViewById(R.id.etSearch);
		etSearch.addTextChangedListener(watcher);
		tvPrompt = (TextView) view.findViewById(R.id.tvPrompt);
		tvPro = (TextView) view.findViewById(R.id.tvPro);
		tvPro.setOnClickListener(this);
		reList = (RelativeLayout) view.findViewById(R.id.reList);

		OkHttpWarning(warningUrl);
	}

	private TextWatcher watcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void afterTextChanged(Editable arg0) {
			if (arg0.toString() == null) {
				return;
			}
			searchList.clear();
			if (isShanxi == false) {
				if (!TextUtils.isEmpty(arg0.toString().trim())) {
					for (int i = 0; i < warningList.size(); i++) {
						WarningDto data = warningList.get(i);
						if (data.name.contains(arg0.toString().trim())) {
							searchList.add(data);
						}
					}
					mList.clear();
					mList.addAll(searchList);
					if (cityAdapter != null) {
						cityAdapter.notifyDataSetChanged();
					}
				}else {
					mList.clear();
					mList.addAll(warningList);
					if (cityAdapter != null) {
						cityAdapter.notifyDataSetChanged();
					}
				}
			}else {
				if (!TextUtils.isEmpty(arg0.toString().trim())) {
					for (int i = 0; i < shanxiList.size(); i++) {
						WarningDto data = shanxiList.get(i);
						if (data.name.contains(arg0.toString().trim())) {
							searchList.add(data);
						}
					}
					mList.clear();
					mList.addAll(searchList);
					if (cityAdapter != null) {
						cityAdapter.notifyDataSetChanged();
					}
				}else {
					mList.clear();
					mList.addAll(shanxiList);
					if (cityAdapter != null) {
						cityAdapter.notifyDataSetChanged();
					}
				}
			}
		}
	};

	/**
	 * 初始化listview
	 */
	private void initListView(View view) {
		cityListView = (ListView) view.findViewById(R.id.listView);
		cityAdapter = new HWarningAdapter(getActivity(), mList, false);
		cityListView.setAdapter(cityAdapter);
		cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				WarningDto data = mList.get(arg2);
				Intent intentDetail = new Intent(getActivity(), HWarningDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", data);
				intentDetail.putExtras(bundle);
				startActivity(intentDetail);
			}
		});
	}

	/**
	 * 初始化高德地图
	 */
	private void initAmap(Bundle bundle, View view) {
		mapView = (MapView) view.findViewById(R.id.mapView);
		mapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mapView.getMap();
		}

		LatLng guizhouLatLng = new LatLng(48.102915,128.121040);
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(guizhouLatLng, zoom));
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMapClickListener(this);
		aMap.setOnMarkerClickListener(this);
		aMap.setInfoWindowAdapter(this);
		aMap.setOnCameraChangeListener(this);

		CommonUtil.drawHLJJson(getActivity(), aMap);
	}

	/**
	 */
	private void OkHttpWarning(final String url) {
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
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("data")) {
												warningList.clear();
												nationList.clear();
												proList.clear();
												cityList.clear();
												disList.clear();
												shanxiList.clear();
												count1 = 0;count2 = 0;count3 = 0;
												JSONArray jsonArray = object.getJSONArray("data");
												for (int i = 0; i < jsonArray.length(); i++) {
													JSONArray tempArray = jsonArray.getJSONArray(i);
													WarningDto dto = new WarningDto();
													dto.html = tempArray.optString(1);
													String[] array = dto.html.split("-");
													String item0 = array[0];
													String item1 = array[1];
													String item2 = array[2];

													dto.item0 = item0;
													dto.provinceId = item0.substring(0, 2);
													dto.type = item2.substring(0, 5);
													dto.color = item2.substring(5, 7);
													dto.time = item1;
													dto.lng = tempArray.optString(2);
													dto.lat = tempArray.optString(3);
													dto.name = tempArray.optString(0);

													if (!dto.name.contains("解除")) {
														warningList.add(dto);
														if (TextUtils.equals(item0, "000000")) {
															nationList.add(dto);
														}else if (TextUtils.equals(item0.substring(item0.length()-4, item0.length()), "0000")) {
															proList.add(dto);
														}else if (TextUtils.equals(item0.substring(item0.length()-2, item0.length()), "00")) {
															cityList.add(dto);
														}else {
															disList.add(dto);
														}

														if (TextUtils.equals(dto.provinceId, "23")) {//黑龙江省预警
															shanxiList.add(dto);
															if (TextUtils.equals(item0.substring(item0.length()-4, item0.length()), "0000")) {
																count1++;
																shanxiProWarning.add(dto);
															}else if (TextUtils.equals(item0.substring(item0.length()-2, item0.length()), "00")) {
																count2++;
															}else {
																count3++;
															}
														}
													}
												}

												if (isShanxi == false) {
													if (warningList.size() > 0) {

														String str1 = sdf.format(new Date())+"更新"+"\n"+"全国共有";
														String str2 = "条预警，国家级预警";
														String str3 = "条，省级预警";
														String str4 = "条，市级预警";
														String str5 = "条，区县级预警";
														String str6 = "条。";
														String c1 = warningList.size()+"";
														String c2 = nationList.size()+"";
														String c3 = proList.size()+"";
														String c4 = cityList.size()+"";
														String c5 = disList.size()+"";
														String warningInfo = str1+c1+str2+c2+str3+c3+str4+c4+str5+c5+str6;
														SpannableStringBuilder builder = new SpannableStringBuilder(warningInfo);
														ForegroundColorSpan builderSpan1 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
														ForegroundColorSpan builderSpan2 = new ForegroundColorSpan(getResources().getColor(R.color.red));
														ForegroundColorSpan builderSpan3 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
														ForegroundColorSpan builderSpan4 = new ForegroundColorSpan(getResources().getColor(R.color.red));
														ForegroundColorSpan builderSpan5 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
														ForegroundColorSpan builderSpan6 = new ForegroundColorSpan(getResources().getColor(R.color.red));
														ForegroundColorSpan builderSpan7 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
														ForegroundColorSpan builderSpan8 = new ForegroundColorSpan(getResources().getColor(R.color.red));
														ForegroundColorSpan builderSpan9 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
														ForegroundColorSpan builderSpan10 = new ForegroundColorSpan(getResources().getColor(R.color.red));
														ForegroundColorSpan builderSpan11 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
														builder.setSpan(builderSpan1, 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														builder.setSpan(builderSpan2, str1.length(), str1.length()+c1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														builder.setSpan(builderSpan3, str1.length()+c1.length(), str1.length()+c1.length()+str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														builder.setSpan(builderSpan4, str1.length()+c1.length()+str2.length(), str1.length()+c1.length()+str2.length()+c2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														builder.setSpan(builderSpan5, str1.length()+c1.length()+str2.length()+c2.length(), str1.length()+c1.length()+str2.length()+c2.length()+str3.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														builder.setSpan(builderSpan6, str1.length()+c1.length()+str2.length()+c2.length()+str3.length(),
																str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														builder.setSpan(builderSpan7, str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length(),
																str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														builder.setSpan(builderSpan8, str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length(),
																str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														builder.setSpan(builderSpan9, str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length(),
																str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length()+str5.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														builder.setSpan(builderSpan10, str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length()+str5.length(),
																str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length()+str5.length()+c5.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														builder.setSpan(builderSpan11, str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length()+str5.length()+c5.length(),
																str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length()+str5.length()+c5.length()+str6.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														tvPrompt.setText(builder);

														tvPro.setVisibility(View.GONE);
														mList.clear();
														mList.addAll(warningList);
														if (cityAdapter != null) {
															cityAdapter.notifyDataSetChanged();
														}
														removeMarkers(proMarkers);
														removeMarkers(cityMarkers);
														removeMarkers(disMarkers);
														if (zoom <= 6.0f) {
															addMarkersToMap(proList, proMarkers);
														}else if (zoom > 6.0f && zoom <= 8.0f) {
															addMarkersToMap(proList, proMarkers);
															addMarkersToMap(cityList, cityMarkers);
														}else if (zoom > 8.0f) {
															addMarkersToMap(proList, proMarkers);
															addMarkersToMap(cityList, cityMarkers);
															addMarkersToMap(disList, disMarkers);
														}
													}else {
														tvPrompt.setText(sdf.format(new Date())+"更新"+"\n"
																+"全国暂无预警信息发布");
													}
												}else {
													if (shanxiList.size() > 0) {

														String str1 = sdf.format(new Date())+"更新"+"\n"+"黑龙江全省共有";
														String str2 = "条预警，省级预警";
														String str3 = "条，市级预警";
														String str4 = "条，区县级预警";
														String str5 = "条。";
														String c1 = shanxiList.size()+"";
														String c2 = count1+"";
														String c3 = count2+"";
														String c4 = count3+"";
														String warningInfo = str1+c1+str2+c2+str3+c3+str4+c4+str5;
														SpannableStringBuilder builder = new SpannableStringBuilder(warningInfo);
														ForegroundColorSpan builderSpan1 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
														ForegroundColorSpan builderSpan2 = new ForegroundColorSpan(getResources().getColor(R.color.red));
														ForegroundColorSpan builderSpan3 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
														ForegroundColorSpan builderSpan4 = new ForegroundColorSpan(getResources().getColor(R.color.red));
														ForegroundColorSpan builderSpan5 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
														ForegroundColorSpan builderSpan6 = new ForegroundColorSpan(getResources().getColor(R.color.red));
														ForegroundColorSpan builderSpan7 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
														ForegroundColorSpan builderSpan8 = new ForegroundColorSpan(getResources().getColor(R.color.red));
														ForegroundColorSpan builderSpan9 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
														builder.setSpan(builderSpan1, 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														builder.setSpan(builderSpan2, str1.length(), str1.length()+c1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														builder.setSpan(builderSpan3, str1.length()+c1.length(), str1.length()+c1.length()+str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														builder.setSpan(builderSpan4, str1.length()+c1.length()+str2.length(), str1.length()+c1.length()+str2.length()+c2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														builder.setSpan(builderSpan5, str1.length()+c1.length()+str2.length()+c2.length(), str1.length()+c1.length()+str2.length()+c2.length()+str3.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														builder.setSpan(builderSpan6, str1.length()+c1.length()+str2.length()+c2.length()+str3.length(),
																str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														builder.setSpan(builderSpan7, str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length(),
																str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														builder.setSpan(builderSpan8, str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length(),
																str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														builder.setSpan(builderSpan9, str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length(),
																str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length()+str5.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
														tvPrompt.setText(builder);

														if (count1 > 0) {
															tvPro.setVisibility(View.VISIBLE);
														}else {
															tvPro.setVisibility(View.GONE);
														}
														mList.clear();
														mList.addAll(shanxiList);
														if (cityAdapter != null) {
															cityAdapter.notifyDataSetChanged();
														}
														removeMarkers(shanxiMarkers);
														addMarkersToMap(shanxiList, shanxiMarkers);
													}else {
														tvPrompt.setText(sdf.format(new Date())+"更新"+"\n"
																+"黑龙江全省暂无预警信息发布");
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

	/**
	 * 移除地图上指定marker
	 * @param markers
	 */
	private void removeMarkers(List<Marker> markers) {
		for (int i = 0; i < markers.size(); i++) {
			final Marker marker = markers.get(i);
			ScaleAnimation animation = new ScaleAnimation(1,0,1,0);
			animation.setInterpolator(new LinearInterpolator());
			animation.setDuration(300);
			marker.setAnimation(animation);
			marker.startAnimation();
			marker.setAnimationListener(new ScaleAnimation.AnimationListener() {
				@Override
				public void onAnimationStart() {
				}
				@Override
				public void onAnimationEnd() {
					marker.remove();
				}
			});
		}
		markers.clear();
	}

	/**
	 * 在地图上添加marker
	 */
	private void addMarkersToMap(List<WarningDto> list, List<Marker> markerList) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < list.size(); i++) {
			WarningDto dto = list.get(i);
			MarkerOptions optionsTemp = new MarkerOptions();
			optionsTemp.title(dto.lat);
			optionsTemp.snippet(dto.lng);
			optionsTemp.anchor(0.5f, 0.5f);
			if (!TextUtils.isEmpty(dto.lat) && !TextUtils.isEmpty(dto.lng)) {
				optionsTemp.position(new LatLng(Double.valueOf(dto.lat), Double.valueOf(dto.lng)));
			}

			View view = inflater.inflate(R.layout.warning_marker_view, null);
			ImageView ivMarker = (ImageView) view.findViewById(R.id.ivMarker);

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
			ivMarker.setImageBitmap(bitmap);
			optionsTemp.icon(BitmapDescriptorFactory.fromView(view));

			if (leftlatlng == null || rightLatlng == null) {
				Marker marker = aMap.addMarker(optionsTemp);
				markerList.add(marker);
				ScaleAnimation animation = new ScaleAnimation(0,1,0,1);
				animation.setInterpolator(new LinearInterpolator());
				animation.setDuration(300);
				marker.setAnimation(animation);
				marker.startAnimation();
			}else {
				if (Double.valueOf(dto.lat) > leftlatlng.latitude && Double.valueOf(dto.lat) < rightLatlng.latitude
						&& Double.valueOf(dto.lng) > leftlatlng.longitude && Double.valueOf(dto.lng) < rightLatlng.longitude) {
					Marker marker = aMap.addMarker(optionsTemp);
					markerList.add(marker);
					ScaleAnimation animation = new ScaleAnimation(0,1,0,1);
					animation.setInterpolator(new LinearInterpolator());
					animation.setDuration(300);
					marker.setAnimation(animation);
					marker.startAnimation();
				}
			}
		}
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
	}
	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		zoom = arg0.zoom;
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		Point leftPoint = new Point(0, dm.heightPixels);
		Point rightPoint = new Point(dm.widthPixels, 0);
		leftlatlng = aMap.getProjection().fromScreenLocation(leftPoint);
		rightLatlng = aMap.getProjection().fromScreenLocation(rightPoint);

		if (isShanxi == false) {
			removeMarkers(proMarkers);
			removeMarkers(cityMarkers);
			removeMarkers(disMarkers);
			if (zoom <= 6.0f) {
				addMarkersToMap(proList, proMarkers);
			}else if (zoom > 6.0f && zoom <= 8.0f) {
				addMarkersToMap(proList, proMarkers);
				addMarkersToMap(cityList, cityMarkers);
			}else if (zoom > 8.0f) {
				addMarkersToMap(proList, proMarkers);
				addMarkersToMap(cityList, cityMarkers);
				addMarkersToMap(disList, disMarkers);
			}
		}
	}

	@Override
	public void onMapClick(LatLng arg0) {
		if (selectMarker != null) {
			selectMarker.hideInfoWindow();
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		selectMarker = marker;
		marker.showInfoWindow();
		return true;
	}

	@Override
	public View getInfoContents(final Marker marker) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.warning_marker_info, null);
		ListView mListView = null;
		HWarningAdapter mAdapter = null;
		final List<WarningDto> infoList = new ArrayList<WarningDto>();

		addInfoList(warningList, marker, infoList);

		mListView = (ListView) view.findViewById(R.id.listView);
		mAdapter = new HWarningAdapter(getActivity(), infoList, true);
		mListView.setAdapter(mAdapter);
		ViewGroup.LayoutParams params = mListView.getLayoutParams();
		if (infoList.size() == 1) {
			params.height = (int) CommonUtil.dip2px(getActivity(), 50);
		}else if (infoList.size() == 2) {
			params.height = (int) CommonUtil.dip2px(getActivity(), 100);
		}else if (infoList.size() > 2){
			params.height = (int) CommonUtil.dip2px(getActivity(), 150);
		}
		mListView.setLayoutParams(params);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				intentDetail(infoList.get(arg2));
			}
		});
		return view;
	}

	private void intentDetail(WarningDto data) {
		Intent intentDetail = new Intent(getActivity(), HWarningDetailActivity.class);
		intentDetail.putExtra("data", data);
		startActivity(intentDetail);
	}

	private void addInfoList(List<WarningDto> list, Marker marker, List<WarningDto> infoList) {
		for (int i = 0; i < list.size(); i++) {
			WarningDto dto = list.get(i);
			if (TextUtils.equals(marker.getTitle(), dto.lat) && TextUtils.equals(marker.getSnippet(), dto.lng)) {
				infoList.add(dto);
			}
		}
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		return null;
	}

	/**
	 * @param flag false为显示map，true为显示list
	 */
	private void startAnimation(boolean flag, final RelativeLayout reList) {
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
		reList.startAnimation(animationSet);
		animationSet.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				reList.clearAnimation();
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.ivList:
				if (reList.getVisibility() == View.GONE) {
					startAnimation(false, reList);
					reList.setVisibility(View.VISIBLE);
					ivList.setImageResource(R.drawable.iv_warning_list_selected);
				}else {
					startAnimation(true, reList);
					reList.setVisibility(View.GONE);
					ivList.setImageResource(R.drawable.iv_warning_list_unselected);
				}
				break;
			case R.id.ivNation:
				if (isShanxi == false) {
					isShanxi = true;
					removeMarkers(proMarkers);
					removeMarkers(cityMarkers);
					removeMarkers(disMarkers);
					ivNation.setImageResource(R.drawable.iv_warning_nation_unselected);
					if (shanxiList.size() > 0) {

						String str1 = sdf.format(new Date())+"更新"+"\n"+"黑龙江全省共有";
						String str2 = "条预警，省级预警";
						String str3 = "条，市级预警";
						String str4 = "条，区县级预警";
						String str5 = "条。";
						String c1 = shanxiList.size()+"";
						String c2 = count1+"";
						String c3 = count2+"";
						String c4 = count3+"";
						String warningInfo = str1+c1+str2+c2+str3+c3+str4+c4+str5;
						SpannableStringBuilder builder = new SpannableStringBuilder(warningInfo);
						ForegroundColorSpan builderSpan1 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
						ForegroundColorSpan builderSpan2 = new ForegroundColorSpan(getResources().getColor(R.color.red));
						ForegroundColorSpan builderSpan3 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
						ForegroundColorSpan builderSpan4 = new ForegroundColorSpan(getResources().getColor(R.color.red));
						ForegroundColorSpan builderSpan5 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
						ForegroundColorSpan builderSpan6 = new ForegroundColorSpan(getResources().getColor(R.color.red));
						ForegroundColorSpan builderSpan7 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
						ForegroundColorSpan builderSpan8 = new ForegroundColorSpan(getResources().getColor(R.color.red));
						ForegroundColorSpan builderSpan9 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
						builder.setSpan(builderSpan1, 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan2, str1.length(), str1.length()+c1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan3, str1.length()+c1.length(), str1.length()+c1.length()+str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan4, str1.length()+c1.length()+str2.length(), str1.length()+c1.length()+str2.length()+c2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan5, str1.length()+c1.length()+str2.length()+c2.length(), str1.length()+c1.length()+str2.length()+c2.length()+str3.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan6, str1.length()+c1.length()+str2.length()+c2.length()+str3.length(),
								str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan7, str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length(),
								str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan8, str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length(),
								str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan9, str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length(),
								str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length()+str5.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						tvPrompt.setText(builder);

						if (count1 > 0) {
							tvPro.setVisibility(View.VISIBLE);
						}else {
							tvPro.setVisibility(View.GONE);
						}
						ivList.setVisibility(View.VISIBLE);
						ivNation.setVisibility(View.VISIBLE);
						mList.clear();
						mList.addAll(shanxiList);
						if (cityAdapter != null) {
							cityAdapter.notifyDataSetChanged();
						}
						removeMarkers(shanxiMarkers);
						addMarkersToMap(shanxiList, shanxiMarkers);
					}else {
						tvPrompt.setText(sdf.format(new Date())+"更新"+"\n"
								+"黑龙江全省暂无预警信息发布");
					}
				}else {
					isShanxi = false;
					removeMarkers(shanxiMarkers);
					ivNation.setImageResource(R.drawable.iv_warning_nation_selected);
					if (warningList.size() > 0) {

						String str1 = sdf.format(new Date())+"更新"+"\n"+"全国共有";
						String str2 = "条预警，国家级预警";
						String str3 = "条，省级预警";
						String str4 = "条，市级预警";
						String str5 = "条，区县级预警";
						String str6 = "条。";
						String c1 = warningList.size()+"";
						String c2 = nationList.size()+"";
						String c3 = proList.size()+"";
						String c4 = cityList.size()+"";
						String c5 = disList.size()+"";
						String warningInfo = str1+c1+str2+c2+str3+c3+str4+c4+str5+c5+str6;
						SpannableStringBuilder builder = new SpannableStringBuilder(warningInfo);
						ForegroundColorSpan builderSpan1 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
						ForegroundColorSpan builderSpan2 = new ForegroundColorSpan(getResources().getColor(R.color.red));
						ForegroundColorSpan builderSpan3 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
						ForegroundColorSpan builderSpan4 = new ForegroundColorSpan(getResources().getColor(R.color.red));
						ForegroundColorSpan builderSpan5 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
						ForegroundColorSpan builderSpan6 = new ForegroundColorSpan(getResources().getColor(R.color.red));
						ForegroundColorSpan builderSpan7 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
						ForegroundColorSpan builderSpan8 = new ForegroundColorSpan(getResources().getColor(R.color.red));
						ForegroundColorSpan builderSpan9 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
						ForegroundColorSpan builderSpan10 = new ForegroundColorSpan(getResources().getColor(R.color.red));
						ForegroundColorSpan builderSpan11 = new ForegroundColorSpan(getResources().getColor(R.color.text_color4));
						builder.setSpan(builderSpan1, 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan2, str1.length(), str1.length()+c1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan3, str1.length()+c1.length(), str1.length()+c1.length()+str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan4, str1.length()+c1.length()+str2.length(), str1.length()+c1.length()+str2.length()+c2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan5, str1.length()+c1.length()+str2.length()+c2.length(), str1.length()+c1.length()+str2.length()+c2.length()+str3.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan6, str1.length()+c1.length()+str2.length()+c2.length()+str3.length(),
								str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan7, str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length(),
								str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan8, str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length(),
								str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan9, str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length(),
								str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length()+str5.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan10, str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length()+str5.length(),
								str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length()+str5.length()+c5.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						builder.setSpan(builderSpan11, str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length()+str5.length()+c5.length(),
								str1.length()+c1.length()+str2.length()+c2.length()+str3.length()+c3.length()+str4.length()+c4.length()+str5.length()+c5.length()+str6.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						tvPrompt.setText(builder);
						
						tvPro.setVisibility(View.GONE);
						ivList.setVisibility(View.VISIBLE);
						ivNation.setVisibility(View.VISIBLE);
						mList.clear();
						mList.addAll(warningList);
						if (cityAdapter != null) {
							cityAdapter.notifyDataSetChanged();
						}
						removeMarkers(proMarkers);
						removeMarkers(cityMarkers);
						removeMarkers(disMarkers);
						if (zoom <= 6.0f) {
							addMarkersToMap(proList, proMarkers);
						}else if (zoom > 6.0f && zoom <= 8.0f) {
							addMarkersToMap(proList, proMarkers);
							addMarkersToMap(cityList, cityMarkers);
						}else if (zoom > 8.0f) {
							addMarkersToMap(proList, proMarkers);
							addMarkersToMap(cityList, cityMarkers);
							addMarkersToMap(disList, disMarkers);
						}
					}else {
						tvPrompt.setText(sdf.format(new Date())+"更新"+"\n"
								+"全国暂无预警信息发布");
					}
				}
				break;
			case R.id.ivRefresh:
				OkHttpWarning(warningUrl);
				break;
			case R.id.tvPro:
				Intent intent = new Intent(getActivity(), HHeadWarningActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("warningList", (ArrayList<? extends Parcelable>) shanxiProWarning);
				intent.putExtras(bundle);
				startActivity(intent);
				break;
		}

	}

}
