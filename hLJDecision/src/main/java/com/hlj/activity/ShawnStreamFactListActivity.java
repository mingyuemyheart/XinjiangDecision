package com.hlj.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hlj.adapter.ShawnStreamFactListAdapter;
import com.hlj.dto.StreamFactDto;
import com.hlj.utils.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

/**
 * 强对流天气实况列表
 * @author shawn_sun
 */
public class ShawnStreamFactListActivity extends BaseActivity implements OnClickListener {
	
	private Context mContext;
	private ShawnStreamFactListAdapter mAdapter;
	private List<StreamFactDto> dataList = new ArrayList<>();//listview
	private List<StreamFactDto> dataList1 = new ArrayList<>();//短时强降水
	private List<StreamFactDto> dataList2 = new ArrayList<>();//大风
	private List<StreamFactDto> dataList3 = new ArrayList<>();//冰雹
	private List<StreamFactDto> dataList4 = new ArrayList<>();//闪电
	private SimpleDateFormat sdf1 = new SimpleDateFormat("HH", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHH", Locale.CHINA);
	private TextView tvListName,tvStationName,tvProvince,tvStationId,tvValue,tvUnit,tvPrompt;
	private ImageView ivRank;
	private boolean isDesc = true;//是否为降序排序
	private TextView tvRain1,tvRain2,tvRain3,tvWind1,tvWind2,tvWind3,tvLighting1,tvLighting2,tvLighting3,tvHail1,tvHail2,tvHail3;
	private String start,end;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_stream_fact_list);
		mContext = this;
		showDialog();
		initWidget();
		initListView();
	}
	
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("强对流天气实况");
		RelativeLayout reRain = findViewById(R.id.reRain);
		reRain.setOnClickListener(this);
		RelativeLayout reWind = findViewById(R.id.reWind);
		reWind.setOnClickListener(this);
		RelativeLayout reLighting = findViewById(R.id.reLighting);
		reLighting.setOnClickListener(this);
		RelativeLayout reHail = findViewById(R.id.reHail);
		reHail.setOnClickListener(this);
		tvRain1 = findViewById(R.id.tvRain1);
		tvRain2 = findViewById(R.id.tvRain2);
		tvRain3 = findViewById(R.id.tvRain3);
		tvWind1 = findViewById(R.id.tvWind1);
		tvWind2 = findViewById(R.id.tvWind2);
		tvWind3 = findViewById(R.id.tvWind3);
		tvLighting1 = findViewById(R.id.tvLighting1);
		tvLighting2 = findViewById(R.id.tvLighting2);
		tvLighting3 = findViewById(R.id.tvLighting3);
		tvHail1 = findViewById(R.id.tvHail1);
		tvHail2 = findViewById(R.id.tvHail2);
		tvHail3 = findViewById(R.id.tvHail3);
		tvListName = findViewById(R.id.tvListName);
		tvStationName = findViewById(R.id.tvStationName);
		tvProvince = findViewById(R.id.tvProvince);
		tvStationId = findViewById(R.id.tvStationId);
		tvValue = findViewById(R.id.tvValue);
		tvUnit = findViewById(R.id.tvUnit);
		tvPrompt = findViewById(R.id.tvPrompt);
		ivRank = findViewById(R.id.ivRank);
		LinearLayout llRank = findViewById(R.id.llRank);
		llRank.setOnClickListener(this);

		end = sdf1.format(new Date())+"时";
		start = sdf1.format(new Date().getTime()-1000*60*60)+"时";
		tvListName.setText(String.format("1小时%s实况(%s-%s)", tvRain1.getText().toString(), start, end));
		tvValue.setText(tvRain1.getText().toString());
		tvUnit.setText("("+getString(R.string.unit_mm)+")");

		OkHttpList();
	}

	private void initListView() {
		ListView listView = findViewById(R.id.listView);
		mAdapter = new ShawnStreamFactListAdapter(mContext, dataList);
		listView.setAdapter(mAdapter);
	}

	/**
	 * 获取数据
	 */
	private void OkHttpList() {
		final String url = String.format("http://scapi.weather.com.cn/weather/getServerWeather?time=%s&test=ncg", sdf2.format(new Date()));
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
										if (!obj.isNull("PRE")) {
											JSONObject object = obj.getJSONObject("PRE");
											if (!object.isNull("data")) {
												dataList1.clear();
												JSONArray array = object.getJSONArray("data");
												for (int i = 0; i < array.length(); i++) {
													StreamFactDto dto = new StreamFactDto();
													JSONObject itemObj = array.getJSONObject(i);
													if (!itemObj.isNull("Lat")) {
														dto.lat = itemObj.getDouble("Lat");
													}
													if (!itemObj.isNull("Lon")) {
														dto.lng = itemObj.getDouble("Lon");
													}
													if (!itemObj.isNull("Station_ID_C")) {
														dto.stationId = itemObj.getString("Station_ID_C");
													}
													if (!itemObj.isNull("Station_Name")) {
														dto.stationName = itemObj.getString("Station_Name");
													}
													if (!itemObj.isNull("Province")) {
														dto.province = itemObj.getString("Province");
													}
													if (!itemObj.isNull("City")) {
														dto.city = itemObj.getString("City");
													}
													if (!itemObj.isNull("Cnty")) {
														dto.dis = itemObj.getString("Cnty");
													}
													if (!itemObj.isNull("PRE_1h")) {
														dto.pre1h = itemObj.getString("PRE_1h");
													}
													if (!dto.pre1h.contains("99999") && !TextUtils.isEmpty(dto.province) && dto.province.startsWith("黑龙江")) {
														double pre1h = Double.parseDouble(dto.pre1h);
														if (pre1h <= 300) {//过滤掉300mm以上
															dataList1.add(dto);
														}
													}
												}

												if (dataList1.size() > 0) {
													tvRain3.setText(dataList1.size()+"");
													tvRain3.setVisibility(View.VISIBLE);
												}

												dataList.addAll(dataList1);
												if (mAdapter != null) {
													mAdapter.columnName = "短时强降水";
													mAdapter.notifyDataSetChanged();
												}
												if (dataList.size() <= 0) {
													tvPrompt.setVisibility(View.VISIBLE);
												}
											}
										}

										if (!obj.isNull("WIN")) {
											JSONObject object = obj.getJSONObject("WIN");
											if (!object.isNull("data")) {
												dataList2.clear();
												JSONArray array = object.getJSONArray("data");
												for (int i = 0; i < array.length(); i++) {
													StreamFactDto dto = new StreamFactDto();
													JSONObject itemObj = array.getJSONObject(i);
													if (!itemObj.isNull("Lat")) {
														dto.lat = itemObj.getDouble("Lat");
													}
													if (!itemObj.isNull("Lon")) {
														dto.lng = itemObj.getDouble("Lon");
													}
													if (!itemObj.isNull("Station_ID_C")) {
														dto.stationId = itemObj.getString("Station_ID_C");
													}
													if (!itemObj.isNull("Station_Name")) {
														dto.stationName = itemObj.getString("Station_Name");
													}
													if (!itemObj.isNull("Province")) {
														dto.province = itemObj.getString("Province");
													}
													if (!itemObj.isNull("City")) {
														dto.city = itemObj.getString("City");
													}
													if (!itemObj.isNull("Cnty")) {
														dto.dis = itemObj.getString("Cnty");
													}
													if (!itemObj.isNull("WIN_S_Max")) {
														dto.windS = itemObj.getString("WIN_S_Max");
													}
													if (!itemObj.isNull("WIN_D_S_Max")) {
														dto.windD = itemObj.getString("WIN_D_S_Max");
													}
													if (!dto.windS.contains("99999") && !TextUtils.isEmpty(dto.province) && dto.province.startsWith("黑龙江")) {
														double windS = Double.parseDouble(dto.windS);
														if (windS > 17 && windS < 60) {//过滤掉17m/s以下、60m/s以上
															dataList2.add(dto);
														}
													}
												}

												if (dataList2.size() > 0) {
													tvWind3.setText(dataList2.size()+"");
													tvWind3.setVisibility(View.VISIBLE);
												}

											}
										}

										if (!obj.isNull("Lit")) {
											JSONObject object = obj.getJSONObject("Lit");
											dataList3.clear();
											if (!object.isNull("data_1")) {
												JSONArray array = object.getJSONArray("data_1");
												for (int i = 0; i < array.length(); i++) {
													StreamFactDto dto = new StreamFactDto();
													JSONObject itemObj = array.getJSONObject(i);
													if (!itemObj.isNull("Lat")) {
														dto.lat = itemObj.getDouble("Lat");
													}
													if (!itemObj.isNull("Lon")) {
														dto.lng = itemObj.getDouble("Lon");
													}
													if (!itemObj.isNull("Station_ID_C")) {
														dto.stationId = itemObj.getString("Station_ID_C");
													}
													if (!itemObj.isNull("Station_Name")) {
														dto.stationName = itemObj.getString("Station_Name");
													}
													if (!itemObj.isNull("Lit_Prov")) {
														dto.province = itemObj.getString("Lit_Prov");
													}
													if (!itemObj.isNull("Lit_City")) {
														dto.city = itemObj.getString("Lit_City");
													}
													if (!itemObj.isNull("Lit_Cnty")) {
														dto.dis = itemObj.getString("Lit_Cnty");
													}
													if (!itemObj.isNull("Lit_Current")) {
														dto.lighting = itemObj.getString("Lit_Current");
													}
													dto.lightingType = 1;
													if (!dto.lighting.contains("99999") && !TextUtils.isEmpty(dto.province) && dto.province.startsWith("黑龙江")) {
														dataList3.add(dto);
													}
												}
											}
											if (!object.isNull("data_2")) {
												JSONArray array = object.getJSONArray("data_2");
												for (int i = 0; i < array.length(); i++) {
													StreamFactDto dto = new StreamFactDto();
													JSONObject itemObj = array.getJSONObject(i);
													if (!itemObj.isNull("Lat")) {
														dto.lat = itemObj.getDouble("Lat");
													}
													if (!itemObj.isNull("Lon")) {
														dto.lng = itemObj.getDouble("Lon");
													}
													if (!itemObj.isNull("Station_ID_C")) {
														dto.stationId = itemObj.getString("Station_ID_C");
													}
													if (!itemObj.isNull("Station_Name")) {
														dto.stationName = itemObj.getString("Station_Name");
													}
													if (!itemObj.isNull("Lit_Prov")) {
														dto.province = itemObj.getString("Lit_Prov");
													}
													if (!itemObj.isNull("Lit_City")) {
														dto.city = itemObj.getString("Lit_City");
													}
													if (!itemObj.isNull("Lit_Cnty")) {
														dto.dis = itemObj.getString("Lit_Cnty");
													}
													if (!itemObj.isNull("Lit_Current")) {
														dto.lighting = itemObj.getString("Lit_Current");
													}
													dto.lightingType = 2;
													if (!dto.lighting.contains("99999") && !TextUtils.isEmpty(dto.province) && dto.province.startsWith("黑龙江")) {
														dataList3.add(dto);
													}
												}
											}
											if (!object.isNull("data_3")) {
												JSONArray array = object.getJSONArray("data_3");
												for (int i = 0; i < array.length(); i++) {
													StreamFactDto dto = new StreamFactDto();
													JSONObject itemObj = array.getJSONObject(i);
													if (!itemObj.isNull("Lat")) {
														dto.lat = itemObj.getDouble("Lat");
													}
													if (!itemObj.isNull("Lon")) {
														dto.lng = itemObj.getDouble("Lon");
													}
													if (!itemObj.isNull("Station_ID_C")) {
														dto.stationId = itemObj.getString("Station_ID_C");
													}
													if (!itemObj.isNull("Station_Name")) {
														dto.stationName = itemObj.getString("Station_Name");
													}
													if (!itemObj.isNull("Lit_Prov")) {
														dto.province = itemObj.getString("Lit_Prov");
													}
													if (!itemObj.isNull("Lit_City")) {
														dto.city = itemObj.getString("Lit_City");
													}
													if (!itemObj.isNull("Lit_Cnty")) {
														dto.dis = itemObj.getString("Lit_Cnty");
													}
													if (!itemObj.isNull("Lit_Current")) {
														dto.lighting = itemObj.getString("Lit_Current");
													}
													dto.lightingType = 3;
													if (!dto.lighting.contains("99999") && !TextUtils.isEmpty(dto.province) && dto.province.startsWith("黑龙江")) {
														dataList3.add(dto);
													}
												}
											}
											if (!object.isNull("data_4")) {
												JSONArray array = object.getJSONArray("data_4");
												for (int i = 0; i < array.length(); i++) {
													StreamFactDto dto = new StreamFactDto();
													JSONObject itemObj = array.getJSONObject(i);
													if (!itemObj.isNull("Lat")) {
														dto.lat = itemObj.getDouble("Lat");
													}
													if (!itemObj.isNull("Lon")) {
														dto.lng = itemObj.getDouble("Lon");
													}
													if (!itemObj.isNull("Station_ID_C")) {
														dto.stationId = itemObj.getString("Station_ID_C");
													}
													if (!itemObj.isNull("Station_Name")) {
														dto.stationName = itemObj.getString("Station_Name");
													}
													if (!itemObj.isNull("Lit_Prov")) {
														dto.province = itemObj.getString("Lit_Prov");
													}
													if (!itemObj.isNull("Lit_City")) {
														dto.city = itemObj.getString("Lit_City");
													}
													if (!itemObj.isNull("Lit_Cnty")) {
														dto.dis = itemObj.getString("Lit_Cnty");
													}
													if (!itemObj.isNull("Lit_Current")) {
														dto.lighting = itemObj.getString("Lit_Current");
													}
													dto.lightingType = 4;
													if (!dto.lighting.contains("99999") && !TextUtils.isEmpty(dto.province) && dto.province.startsWith("黑龙江")) {
														dataList3.add(dto);
													}
												}
											}
											if (!object.isNull("data_5")) {
												JSONArray array = object.getJSONArray("data_5");
												for (int i = 0; i < array.length(); i++) {
													StreamFactDto dto = new StreamFactDto();
													JSONObject itemObj = array.getJSONObject(i);
													if (!itemObj.isNull("Lat")) {
														dto.lat = itemObj.getDouble("Lat");
													}
													if (!itemObj.isNull("Lon")) {
														dto.lng = itemObj.getDouble("Lon");
													}
													if (!itemObj.isNull("Station_ID_C")) {
														dto.stationId = itemObj.getString("Station_ID_C");
													}
													if (!itemObj.isNull("Station_Name")) {
														dto.stationName = itemObj.getString("Station_Name");
													}
													if (!itemObj.isNull("Lit_Prov")) {
														dto.province = itemObj.getString("Lit_Prov");
													}
													if (!itemObj.isNull("Lit_City")) {
														dto.city = itemObj.getString("Lit_City");
													}
													if (!itemObj.isNull("Lit_Cnty")) {
														dto.dis = itemObj.getString("Lit_Cnty");
													}
													if (!itemObj.isNull("Lit_Current")) {
														dto.lighting = itemObj.getString("Lit_Current");
													}
													dto.lightingType = 5;
													if (!dto.lighting.contains("99999") && !TextUtils.isEmpty(dto.province) && dto.province.startsWith("黑龙江")) {
														dataList3.add(dto);
													}
												}
											}
											if (!object.isNull("data_6")) {
												JSONArray array = object.getJSONArray("data_6");
												for (int i = 0; i < array.length(); i++) {
													StreamFactDto dto = new StreamFactDto();
													JSONObject itemObj = array.getJSONObject(i);
													if (!itemObj.isNull("Lat")) {
														dto.lat = itemObj.getDouble("Lat");
													}
													if (!itemObj.isNull("Lon")) {
														dto.lng = itemObj.getDouble("Lon");
													}
													if (!itemObj.isNull("Station_ID_C")) {
														dto.stationId = itemObj.getString("Station_ID_C");
													}
													if (!itemObj.isNull("Station_Name")) {
														dto.stationName = itemObj.getString("Station_Name");
													}
													if (!itemObj.isNull("Lit_Prov")) {
														dto.province = itemObj.getString("Lit_Prov");
													}
													if (!itemObj.isNull("Lit_City")) {
														dto.city = itemObj.getString("Lit_City");
													}
													if (!itemObj.isNull("Lit_Cnty")) {
														dto.dis = itemObj.getString("Lit_Cnty");
													}
													if (!itemObj.isNull("Lit_Current")) {
														dto.lighting = itemObj.getString("Lit_Current");
													}
													dto.lightingType = 6;
													if (!dto.lighting.contains("99999") && !TextUtils.isEmpty(dto.province) && dto.province.startsWith("黑龙江")) {
														dataList3.add(dto);
													}
												}
											}

											if (dataList3.size() > 0) {
												tvLighting3.setText(dataList3.size()+"");
												tvLighting3.setVisibility(View.VISIBLE);
											}

										}

										if (!obj.isNull("HAIL")) {
											JSONObject object = obj.getJSONObject("HAIL");
											if (!object.isNull("data")) {
												dataList4.clear();
												JSONArray array = object.getJSONArray("data");
												for (int i = 0; i < array.length(); i++) {
													StreamFactDto dto = new StreamFactDto();
													JSONObject itemObj = array.getJSONObject(i);
													if (!itemObj.isNull("Lat")) {
														dto.lat = itemObj.getDouble("Lat");
													}
													if (!itemObj.isNull("Lon")) {
														dto.lng = itemObj.getDouble("Lon");
													}
													if (!itemObj.isNull("Station_ID_C")) {
														dto.stationId = itemObj.getString("Station_ID_C");
													}
													if (!itemObj.isNull("Station_Name")) {
														dto.stationName = itemObj.getString("Station_Name");
													}
													if (!itemObj.isNull("Province")) {
														dto.province = itemObj.getString("Province");
													}
													if (!itemObj.isNull("City")) {
														dto.city = itemObj.getString("City");
													}
													if (!itemObj.isNull("Cnty")) {
														dto.dis = itemObj.getString("Cnty");
													}
													if (!itemObj.isNull("HAIL_Diam_Max")) {
														dto.hail = itemObj.getString("HAIL_Diam_Max");
													}
													if (!dto.hail.contains("99999") && !TextUtils.isEmpty(dto.province) && dto.province.startsWith("黑龙江")) {
														dataList4.add(dto);
													}
												}

												if (dataList4.size() > 0) {
													tvHail3.setText(dataList4.size()+"");
													tvHail3.setVisibility(View.VISIBLE);
												}

											}
										}

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
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				finish();
				break;
			case R.id.reRain:
				tvRain1.setTextColor(getResources().getColor(R.color.blue));
				tvRain2.setBackgroundColor(getResources().getColor(R.color.blue));
				tvWind1.setTextColor(getResources().getColor(R.color.text_color3));
				tvWind2.setBackgroundColor(getResources().getColor(R.color.transparent));
				tvLighting1.setTextColor(getResources().getColor(R.color.text_color3));
				tvLighting2.setBackgroundColor(getResources().getColor(R.color.transparent));
				tvHail1.setTextColor(getResources().getColor(R.color.text_color3));
				tvHail2.setBackgroundColor(getResources().getColor(R.color.transparent));

				tvListName.setText(String.format("1小时%s实况(%s-%s)", tvRain1.getText().toString(), start, end));
				tvStationName.setText("站名");
				tvProvince.setText("行政区划");
				tvStationId.setText("站号");
				tvUnit.setText("("+getString(R.string.unit_mm)+")");
				tvValue.setText(tvRain1.getText().toString());

				tvPrompt.setVisibility(View.GONE);
				dataList.clear();
				dataList.addAll(dataList1);
				if (dataList.size() <= 0) {
					tvPrompt.setVisibility(View.VISIBLE);
				}
				if (mAdapter != null) {
					mAdapter.columnName = tvRain1.getText().toString();
					mAdapter.notifyDataSetChanged();
				}
				break;
			case R.id.reWind:
				tvRain1.setTextColor(getResources().getColor(R.color.text_color3));
				tvRain2.setBackgroundColor(getResources().getColor(R.color.transparent));
				tvWind1.setTextColor(getResources().getColor(R.color.blue));
				tvWind2.setBackgroundColor(getResources().getColor(R.color.blue));
				tvLighting1.setTextColor(getResources().getColor(R.color.text_color3));
				tvLighting2.setBackgroundColor(getResources().getColor(R.color.transparent));
				tvHail1.setTextColor(getResources().getColor(R.color.text_color3));
				tvHail2.setBackgroundColor(getResources().getColor(R.color.transparent));

				tvListName.setText(String.format("1小时%s实况(%s-%s)", tvWind1.getText().toString(), start, end));
				tvStationName.setText("站名");
				tvProvince.setText("行政区划");
				tvStationId.setText("站号");
				tvUnit.setText("("+getString(R.string.unit_speed)+")");
				tvValue.setText(tvWind1.getText().toString());

				tvPrompt.setVisibility(View.GONE);
				dataList.clear();
				dataList.addAll(dataList2);
				if (dataList.size() <= 0) {
					tvPrompt.setVisibility(View.VISIBLE);
				}
				if (mAdapter != null) {
					mAdapter.columnName = tvWind1.getText().toString();
					mAdapter.notifyDataSetChanged();
				}
				break;
			case R.id.reLighting:
				tvRain1.setTextColor(getResources().getColor(R.color.text_color3));
				tvRain2.setBackgroundColor(getResources().getColor(R.color.transparent));
				tvWind1.setTextColor(getResources().getColor(R.color.text_color3));
				tvWind2.setBackgroundColor(getResources().getColor(R.color.transparent));
				tvLighting1.setTextColor(getResources().getColor(R.color.blue));
				tvLighting2.setBackgroundColor(getResources().getColor(R.color.blue));
				tvHail1.setTextColor(getResources().getColor(R.color.text_color3));
				tvHail2.setBackgroundColor(getResources().getColor(R.color.transparent));

				tvListName.setText(String.format("1小时%s实况(%s-%s)", tvLighting1.getText().toString(), start, end));
				tvStationName.setText("省");
				tvProvince.setText("市/地区");
				tvStationId.setText("县");
				tvUnit.setText("(10KA)");
				tvValue.setText(tvLighting1.getText().toString());

				tvPrompt.setVisibility(View.GONE);
				dataList.clear();
				dataList.addAll(dataList3);
				if (dataList.size() <= 0) {
					tvPrompt.setVisibility(View.VISIBLE);
				}
				if (mAdapter != null) {
					mAdapter.columnName = tvLighting1.getText().toString();
					mAdapter.notifyDataSetChanged();
				}
				break;
			case R.id.reHail:
				tvRain1.setTextColor(getResources().getColor(R.color.text_color3));
				tvRain2.setBackgroundColor(getResources().getColor(R.color.transparent));
				tvWind1.setTextColor(getResources().getColor(R.color.text_color3));
				tvWind2.setBackgroundColor(getResources().getColor(R.color.transparent));
				tvLighting1.setTextColor(getResources().getColor(R.color.text_color3));
				tvLighting2.setBackgroundColor(getResources().getColor(R.color.transparent));
				tvHail1.setTextColor(getResources().getColor(R.color.blue));
				tvHail2.setBackgroundColor(getResources().getColor(R.color.blue));

				tvListName.setText(String.format("1小时%s实况(%s-%s)", tvHail1.getText().toString(), start, end));
				tvStationName.setText("站名");
				tvProvince.setText("行政区划");
				tvStationId.setText("站号");
				tvUnit.setText("("+getString(R.string.unit_mm)+")");
				tvValue.setText(tvHail1.getText().toString());

				tvPrompt.setVisibility(View.GONE);
				dataList.clear();
				dataList.addAll(dataList4);
				if (dataList.size() <= 0) {
					tvPrompt.setVisibility(View.VISIBLE);
				}
				if (mAdapter != null) {
					mAdapter.columnName = tvHail1.getText().toString();
					mAdapter.notifyDataSetChanged();
				}
				break;
			case R.id.llRank:
				String columnName = tvListName.getText().toString();
				if (isDesc) {
					ivRank.setImageResource(R.drawable.icon_range_up);
					if (columnName.contains("降水")) {
						Collections.sort(dataList, new Comparator<StreamFactDto>() {
							@Override
							public int compare(StreamFactDto arg0, StreamFactDto arg1) {
								if (TextUtils.isEmpty(arg0.pre1h) || TextUtils.isEmpty(arg1.pre1h)) {
									return 0;
								}else {
									return Double.valueOf(arg0.pre1h).compareTo(Double.valueOf(arg1.pre1h));
								}
							}
						});
					}else if (columnName.contains("大风")) {
						Collections.sort(dataList, new Comparator<StreamFactDto>() {
							@Override
							public int compare(StreamFactDto arg0, StreamFactDto arg1) {
								if (TextUtils.isEmpty(arg0.windS) || TextUtils.isEmpty(arg1.windS)) {
									return 0;
								}else {
									return Double.valueOf(arg0.windS).compareTo(Double.valueOf(arg1.windS));
								}
							}
						});
					}else if (columnName.contains("冰雹")) {
						Collections.sort(dataList, new Comparator<StreamFactDto>() {
							@Override
							public int compare(StreamFactDto arg0, StreamFactDto arg1) {
								if (TextUtils.isEmpty(arg0.hail) || TextUtils.isEmpty(arg1.hail)) {
									return 0;
								}else {
									return Double.valueOf(arg0.hail).compareTo(Double.valueOf(arg1.hail));
								}
							}
						});
					}else if (columnName.contains("闪电")) {
						Collections.sort(dataList, new Comparator<StreamFactDto>() {
							@Override
							public int compare(StreamFactDto arg0, StreamFactDto arg1) {
								if (TextUtils.isEmpty(arg0.lighting) || TextUtils.isEmpty(arg1.lighting)) {
									return 0;
								}else {
									return Double.valueOf(arg0.lighting).compareTo(Double.valueOf(arg1.lighting));
								}
							}
						});
					}
				}else {
					ivRank.setImageResource(R.drawable.icon_range_down);
					if (columnName.contains("降水")) {
						Collections.sort(dataList, new Comparator<StreamFactDto>() {
							@Override
							public int compare(StreamFactDto arg0, StreamFactDto arg1) {
								if (TextUtils.isEmpty(arg0.pre1h) || TextUtils.isEmpty(arg1.pre1h)) {
									return 0;
								}else {
									return Double.valueOf(arg1.pre1h).compareTo(Double.valueOf(arg0.pre1h));
								}
							}
						});
					}else if (columnName.contains("大风")) {
						Collections.sort(dataList, new Comparator<StreamFactDto>() {
							@Override
							public int compare(StreamFactDto arg0, StreamFactDto arg1) {
								if (TextUtils.isEmpty(arg0.windS) || TextUtils.isEmpty(arg1.windS)) {
									return 0;
								}else {
									return Double.valueOf(arg1.windS).compareTo(Double.valueOf(arg0.windS));
								}
							}
						});
					}else if (columnName.contains("冰雹")) {
						Collections.sort(dataList, new Comparator<StreamFactDto>() {
							@Override
							public int compare(StreamFactDto arg0, StreamFactDto arg1) {
								if (TextUtils.isEmpty(arg0.hail) || TextUtils.isEmpty(arg1.hail)) {
									return 0;
								}else {
									return Double.valueOf(arg1.hail).compareTo(Double.valueOf(arg0.hail));
								}
							}
						});
					}else if (columnName.contains("闪电")) {
						Collections.sort(dataList, new Comparator<StreamFactDto>() {
							@Override
							public int compare(StreamFactDto arg0, StreamFactDto arg1) {
								if (TextUtils.isEmpty(arg0.lighting) || TextUtils.isEmpty(arg1.lighting)) {
									return 0;
								}else {
									return Double.valueOf(arg1.lighting).compareTo(Double.valueOf(arg0.lighting));
								}
							}
						});
					}
				}
				isDesc = !isDesc;

				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}

				break;

		default:
			break;
		}
	}

}
