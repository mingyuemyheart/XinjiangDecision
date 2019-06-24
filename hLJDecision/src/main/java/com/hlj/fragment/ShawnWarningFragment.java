package com.hlj.fragment;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.hlj.activity.HWarningDetailActivity;
import com.hlj.activity.HWarningListActivity;
import com.hlj.activity.ShawnWarningStatisticActivity;
import com.hlj.adapter.HWarningAdapter;
import com.hlj.adapter.ShawnWarningStatisticAdapter;
import com.hlj.common.CONST;
import com.hlj.dto.WarningDto;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.OkHttpUtil;
import com.hlj.view.ArcMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

/**
 * 天气预警
 */
public class ShawnWarningFragment extends Fragment implements OnClickListener, OnMapClickListener,
OnMarkerClickListener, InfoWindowAdapter {
	
	private TextView tvWarningStatistic;
	private RelativeLayout reWarningStatistic;
	private MapView mapView;//高德地图
	private AMap aMap;//高德地图
	private float zoom = 6.0f;
	private ArcMenu arcMenu;
	private boolean blue = true, yellow = true, orange = true, red = true;
	private List<WarningDto> warningList = new ArrayList<>();
	private List<Marker> markers = new ArrayList<>();
	private ImageView ivList,ivStatistic,ivArrow;
	private Marker selectMarker;
	private boolean isShowPrompt = true;
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

	//预警统计列表
	private ListView listView;
	private ShawnWarningStatisticAdapter statisticAdapter;
	private List<WarningDto> statisticList = new ArrayList<>();

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.shawn_fragment_warning, null);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initAmap(view, savedInstanceState);
		initWidget(view);
		initListView(view);
	}

	/**
	 * 初始化高德地图
	 */
	private void initAmap(View view, Bundle bundle) {
		mapView = view.findViewById(R.id.mapView);
		mapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mapView.getMap();
		}

		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), zoom));
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setOnMapClickListener(this);
		aMap.setOnMarkerClickListener(this);
		aMap.setInfoWindowAdapter(this);

		TextView tvMapNumber = view.findViewById(R.id.tvMapNumber);
		tvMapNumber.setText(aMap.getMapContentApprovalNumber());

		CommonUtil.drawHLJJson(getActivity(), aMap);
	}

	/**
	 * 初始化控件
	 */
	private void initWidget(View view) {
		tvWarningStatistic = view.findViewById(R.id.tvWarningStatistic);
		arcMenu = view.findViewById(R.id.arcMenu);
		arcMenu.setOnMenuItemClickListener(arcMenuListener);
		ImageView ivRefresh = view.findViewById(R.id.ivRefresh);
		ivRefresh.setOnClickListener(this);
		ivList = view.findViewById(R.id.ivList);
		ivList.setOnClickListener(this);
		ivStatistic = view.findViewById(R.id.ivStatistic);
		ivStatistic.setOnClickListener(this);
		reWarningStatistic = view.findViewById(R.id.reWarningStatistic);
		reWarningStatistic.setOnClickListener(this);
		ivArrow = view.findViewById(R.id.ivArrow);
		ivArrow.setOnClickListener(this);

		refresh();
    }
	
	private void refresh() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpWarning();
			}
		}).start();
	}
	
	/**
	 * 获取预警信息
	 */
	private void OkHttpWarning() {
		final String url = "https://decision-admin.tianqi.cn/Home/work2019/getwarns?areaid=23";
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
								warningList.clear();
								JSONObject object = new JSONObject(result);
								if (!object.isNull("data")) {
									JSONArray jsonArray = object.getJSONArray("data");
									for (int i = 0; i < jsonArray.length(); i++) {
										JSONArray tempArray = jsonArray.getJSONArray(i);
										WarningDto dto = new WarningDto();
										dto.html = tempArray.getString(1);
										String[] array = dto.html.split("-");
										String item0 = array[0];
										String item1 = array[1];
										String item2 = array[2];

										dto.item0 = item0;
										dto.provinceId = item0.substring(0, 2);
										dto.type = item2.substring(0, 5);
										dto.color = item2.substring(5, 7);
										dto.time = item1;
										dto.lng = tempArray.getDouble(2);
										dto.lat = tempArray.getDouble(3);
										dto.name = tempArray.getString(0);

										if (!dto.name.contains("解除")) {
											warningList.add(dto);
										}

									}

									addWarningMarkers();

									try {
										String count = warningList.size()+"";
										if (TextUtils.equals(count, "0")) {
											String time = "";
											if (!object.isNull("time")) {
												long t = object.getLong("time");
												time = sdf3.format(new Date(t*1000));
											}
											tvWarningStatistic.setText(time+", "+"当前生效预警"+count+"条");
											ivList.setVisibility(View.GONE);
											ivStatistic.setVisibility(View.GONE);
											arcMenu.setVisibility(View.GONE);
											reWarningStatistic.setVisibility(View.VISIBLE);
											return;
										}

										String time = "";
										if (!object.isNull("time")) {
											long t = object.getLong("time");
											time = sdf3.format(new Date(t*1000));
										}
										String str1 = time+", "+"当前生效预警";
										String str2 = "条";
										String warningInfo = str1+count+str2;
										SpannableStringBuilder builder = new SpannableStringBuilder(warningInfo);
										ForegroundColorSpan builderSpan1 = new ForegroundColorSpan(getResources().getColor(R.color.text_color3));
										ForegroundColorSpan builderSpan2 = new ForegroundColorSpan(getResources().getColor(R.color.red));
										ForegroundColorSpan builderSpan3 = new ForegroundColorSpan(getResources().getColor(R.color.text_color3));
										builder.setSpan(builderSpan1, 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
										builder.setSpan(builderSpan2, str1.length(), str1.length()+count.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
										builder.setSpan(builderSpan3, str1.length()+count.length(), str1.length()+count.length()+str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
										tvWarningStatistic.setText(builder);
										ivList.setVisibility(View.VISIBLE);
										ivStatistic.setVisibility(View.VISIBLE);
										arcMenu.setVisibility(View.VISIBLE);
										reWarningStatistic.setVisibility(View.VISIBLE);

										//计算统计列表信息
										int rnation = 0;int rpro = 0;int rcity = 0;int rdis = 0;
										int onation = 0;int opro = 0;int ocity = 0;int odis = 0;
										int ynation = 0;int ypro = 0;int ycity = 0;int ydis = 0;
										int bnation = 0;int bpro = 0;int bcity = 0;int bdis = 0;
										int wnation = 0;int wpro = 0;int wcity = 0;int wdis = 0;
										for (int i = 0; i < warningList.size(); i++) {
											WarningDto dto = warningList.get(i);
											if (TextUtils.equals(dto.color, "04")) {
												if (TextUtils.equals(dto.item0, "000000")) {
													rnation += 1;
												}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-4, dto.item0.length()), "0000")) {
													rpro += 1;
												}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-2, dto.item0.length()), "00")) {
													rcity += 1;
												}else {
													rdis += 1;
												}
											}else if (TextUtils.equals(dto.color, "03")) {
												if (TextUtils.equals(dto.item0, "000000")) {
													onation += 1;
												}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-4, dto.item0.length()), "0000")) {
													opro += 1;
												}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-2, dto.item0.length()), "00")) {
													ocity += 1;
												}else {
													odis += 1;
												}
											}else if (TextUtils.equals(dto.color, "02")) {
												if (TextUtils.equals(dto.item0, "000000")) {
													ynation += 1;
												}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-4, dto.item0.length()), "0000")) {
													ypro += 1;
												}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-2, dto.item0.length()), "00")) {
													ycity += 1;
												}else {
													ydis += 1;
												}
											}else if (TextUtils.equals(dto.color, "01")) {
												if (TextUtils.equals(dto.item0, "000000")) {
													bnation += 1;
												}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-4, dto.item0.length()), "0000")) {
													bpro += 1;
												}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-2, dto.item0.length()), "00")) {
													bcity += 1;
												}else {
													bdis += 1;
												}
											}else if (TextUtils.equals(dto.color, "05")) {
												if (TextUtils.equals(dto.item0, "000000")) {
													wnation += 1;
												}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-4, dto.item0.length()), "0000")) {
													wpro += 1;
												}else if (TextUtils.equals(dto.item0.substring(dto.item0.length()-2, dto.item0.length()), "00")) {
													wcity += 1;
												}else {
													wdis += 1;
												}
											}
										}

										statisticList.clear();
										WarningDto wDto = new WarningDto();
										wDto.colorName = "预警"+warningList.size();
										wDto.nationCount = "国家级"+(rnation+onation+ynation+bnation+wnation);
										wDto.proCount = "省级"+(rpro+opro+ypro+bpro+wpro);
										wDto.cityCount = "市级"+(rcity+ocity+ycity+bcity+wcity);
										wDto.disCount = "县级"+(rdis+odis+ydis+bdis+wdis);
										statisticList.add(wDto);

										wDto = new WarningDto();
										wDto.colorName = "红"+(rnation+rpro+rcity+rdis);
										wDto.nationCount = rnation+"";
										wDto.proCount = rpro+"";
										wDto.cityCount = rcity+"";
										wDto.disCount = rdis+"";
										statisticList.add(wDto);

										wDto = new WarningDto();
										wDto.colorName = "橙"+(onation+opro+ocity+odis);
										wDto.nationCount = onation+"";
										wDto.proCount = opro+"";
										wDto.cityCount = ocity+"";
										wDto.disCount = odis+"";
										statisticList.add(wDto);

										wDto = new WarningDto();
										wDto.colorName = "黄"+(ynation+ypro+ycity+ydis);
										wDto.nationCount = ynation+"";
										wDto.proCount = ypro+"";
										wDto.cityCount = ycity+"";
										wDto.disCount = ydis+"";
										statisticList.add(wDto);

										wDto = new WarningDto();
										wDto.colorName = "蓝"+(bnation+bpro+bcity+bdis);
										wDto.nationCount = bnation+"";
										wDto.proCount = bpro+"";
										wDto.cityCount = bcity+"";
										wDto.disCount = bdis+"";
										statisticList.add(wDto);

										wDto = new WarningDto();
										wDto.colorName = "未知"+(wnation+wpro+wcity+wdis);
										wDto.nationCount = wnation+"";
										wDto.proCount = wpro+"";
										wDto.cityCount = wcity+"";
										wDto.disCount = wdis+"";
										statisticList.add(wDto);

										if (statisticAdapter != null) {
											statisticAdapter.notifyDataSetChanged();
										}
									} catch (Exception e) {
										e.printStackTrace();
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

	/**
	 * 在地图上添加marker
	 */
	private void addWarningMarkers() {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (WarningDto dto : warningList) {
			MarkerOptions optionsTemp = new MarkerOptions();
			optionsTemp.title(dto.lat+","+dto.lng+","+dto.item0+","+dto.color);
			optionsTemp.snippet(dto.color);
			optionsTemp.anchor(0.5f, 0.5f);
			optionsTemp.position(new LatLng(dto.lat, dto.lng));
			View mView = inflater.inflate(R.layout.shawn_warning_marker_icon, null);
			ImageView ivMarker = mView.findViewById(R.id.ivMarker);
			Bitmap bitmap = null;
			if (dto.color.equals(CONST.blue[0])) {
				bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+dto.type+CONST.blue[1]+CONST.imageSuffix);
			}else if (dto.color.equals(CONST.yellow[0])) {
				bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+dto.type+CONST.yellow[1]+CONST.imageSuffix);
			}else if (dto.color.equals(CONST.orange[0])) {
				bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+dto.type+CONST.orange[1]+CONST.imageSuffix);
			}else if (dto.color.equals(CONST.red[0])) {
				bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+dto.type+CONST.red[1]+CONST.imageSuffix);
			}
			if (bitmap == null) {
				bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.imageSuffix);
			}
			ivMarker.setImageBitmap(bitmap);
			optionsTemp.icon(BitmapDescriptorFactory.fromView(mView));
			Marker marker;
			if (TextUtils.equals(dto.color, "01") && blue) {
				marker = aMap.addMarker(optionsTemp);
			}else if (TextUtils.equals(dto.color, "02") && yellow) {
				marker = aMap.addMarker(optionsTemp);
			}else if (TextUtils.equals(dto.color, "03") && orange) {
				marker = aMap.addMarker(optionsTemp);
			}else if (TextUtils.equals(dto.color, "04") && red) {
				marker = aMap.addMarker(optionsTemp);
			}else {
				marker = aMap.addMarker(optionsTemp);
			}
			markers.add(marker);
		}
	}

	private void switchMarkers() {
		for (Marker marker : markers) {
			if (TextUtils.equals(marker.getSnippet(), "01")) {
				marker.setVisible(blue);
			}else if (TextUtils.equals(marker.getSnippet(), "02")){
				marker.setVisible(yellow);
			}else if (TextUtils.equals(marker.getSnippet(), "03")){
				marker.setVisible(orange);
			}else if (TextUtils.equals(marker.getSnippet(), "04")){
				marker.setVisible(red);
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
		if (marker != null) {
			selectMarker = marker;
			if (selectMarker.isInfoWindowShown()) {
				selectMarker.hideInfoWindow();
			}else {
				selectMarker.showInfoWindow();
			}
		}
		return true;
	}
	
	@Override
	public View getInfoContents(final Marker marker) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mView = inflater.inflate(R.layout.shawn_warning_marker_icon_info, null);
		final List<WarningDto> infoList = addInfoList(marker);
		ListView mListView = mView.findViewById(R.id.listView);
		HWarningAdapter mAdapter = new HWarningAdapter(getActivity(), infoList, true);
		mListView.setAdapter(mAdapter);
		LayoutParams params = mListView.getLayoutParams();
		if (infoList.size() == 1) {
			params.height = (int) CommonUtil.dip2px(getActivity(), 50);
		}else if (infoList.size() == 2) {
			params.height = (int) CommonUtil.dip2px(getActivity(), 100);
		}else if (infoList.size() > 2){
			params.height = (int) CommonUtil.dip2px(getActivity(), 150);
		}
		mListView.setLayoutParams(params);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				intentDetail(infoList.get(arg2));
			}
		});
		return mView;
	}

	private List<WarningDto> addInfoList(Marker marker) {
		List<WarningDto> infoList = new ArrayList<>();
		for (WarningDto dto : warningList) {
			String[] latLng = marker.getTitle().split(",");
			if (TextUtils.equals(latLng[0], dto.lat+"") && TextUtils.equals(latLng[1], dto.lng+"")) {
				infoList.add(dto);
			}
		}
		return infoList;
	}
	
	private void intentDetail(WarningDto data) {
		Intent intentDetail = new Intent(getActivity(), HWarningDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable("data", data);
		intentDetail.putExtras(bundle);
		startActivity(intentDetail);
	}
	
	@Override
	public View getInfoWindow(Marker arg0) {
		return null;
	}
	
	private ArcMenu.OnMenuItemClickListener arcMenuListener = new ArcMenu.OnMenuItemClickListener() {
		@Override
		public void onClick(View view, int pos) {
			if (pos == 0) {
				blue = !blue;
				if (!blue) {
					((ImageView)view).setImageResource(R.drawable.shawn_icon_arc_blue_press);
				}else {
					((ImageView)view).setImageResource(R.drawable.shawn_icon_arc_blue);
				}
				switchMarkers();
			}else if (pos == 1) {
				yellow = !yellow;
				if (!yellow) {
					((ImageView)view).setImageResource(R.drawable.shawn_icon_arc_yellow_press);
				}else {
					((ImageView)view).setImageResource(R.drawable.shawn_icon_arc_yellow);
				}
				switchMarkers();
			}else if (pos == 2) {
				orange = !orange;
				if (!orange) {
					((ImageView)view).setImageResource(R.drawable.shawn_icon_arc_orange_press);
				}else {
					((ImageView)view).setImageResource(R.drawable.shawn_icon_arc_orange);
				}
				switchMarkers();
			}else if (pos == 3) {
				red = !red;
				if (!red) {
					((ImageView)view).setImageResource(R.drawable.shawn_icon_arc_red_press);
				}else {
					((ImageView)view).setImageResource(R.drawable.shawn_icon_arc_red);
				}
				switchMarkers();
			}
		}
	};
	
	/**
	 * 初始化预警统计列表
	 */
	private void initListView(View view) {
		listView = view.findViewById(R.id.listView);
		statisticAdapter = new ShawnWarningStatisticAdapter(getActivity(), statisticList);
		listView.setAdapter(statisticAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				clickPromptWarning();
			}
		});
	}
	
	/**
     * 隐藏或显示ListView的动画 
     */  
    public void hideOrShowListViewAnimator(final View view, final int startValue,final int endValue){  
        //1.设置属性的初始值和结束值  
        ValueAnimator mAnimator = ValueAnimator.ofInt(0,100);
        //2.为目标对象的属性变化设置监听器  
        mAnimator.addUpdateListener(new AnimatorUpdateListener() {  
            @Override  
            public void onAnimationUpdate(ValueAnimator animation) {  
                int animatorValue = (Integer) animation.getAnimatedValue();  
                float fraction = animatorValue/100f;  
                IntEvaluator mEvaluator = new IntEvaluator();  
                //3.使用IntEvaluator计算属性值并赋值给ListView的高  
                view.getLayoutParams().height = mEvaluator.evaluate(fraction, startValue, endValue);  
                view.requestLayout();  
            }  
        });  
        //4.为ValueAnimator设置LinearInterpolator  
        mAnimator.setInterpolator(new LinearInterpolator());  
        //5.设置动画的持续时间  
        mAnimator.setDuration(200);  
        //6.为ValueAnimator设置目标对象并开始执行动画  
        mAnimator.setTarget(view);  
        mAnimator.start();  
    } 
    
    private void clickPromptWarning() {
    	int height = CommonUtil.getListViewHeightBasedOnChildren(listView);
		isShowPrompt = !isShowPrompt;
		if (!isShowPrompt) {
			ivArrow.setImageResource(R.drawable.shawn_icon_arrow_up_black);
			hideOrShowListViewAnimator(listView, 0, height);
		}else {
			ivArrow.setImageResource(R.drawable.shawn_icon_arrow_down_black);
			hideOrShowListViewAnimator(listView, height, 0);
		}
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.reWarningStatistic:
		case R.id.ivArrow:
			clickPromptWarning();
			break;
		case R.id.ivRefresh:
			refresh();
			break;
		case R.id.ivList:
			Intent intent = new Intent(getActivity(), HWarningListActivity.class);
			intent.putExtra("isVisible", true);
			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList("warningList", (ArrayList<? extends Parcelable>) warningList);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivStatistic:
			startActivity(new Intent(getActivity(), ShawnWarningStatisticActivity.class));
			break;


		default:
			break;
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onResume() {
		super.onResume();
		if (mapView != null) {
			mapView.onResume();
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onPause() {
		super.onPause();
		if (mapView != null) {
			mapView.onPause();
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mapView != null) {
			mapView.onSaveInstanceState(outState);
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mapView != null) {
			mapView.onDestroy();
		}
	}

}
