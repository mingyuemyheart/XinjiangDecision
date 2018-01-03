package com.hlj.activity;

/**
 * 设置界面
 */

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.hlj.common.CONST;
import com.hlj.manager.DataCleanManager;
import com.hlj.utils.AutoUpdateUtil;
import com.hlj.utils.OkHttpUtil;
import com.hlj.utils.WeatherUtil;
import com.hlj.view.RefreshLayout;
import com.hlj.view.RefreshLayout.OnRefreshListener;
import com.hlj.common.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants.Language;
import cn.com.weather.listener.AsyncResponseHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.decision.R;

public class HSettingActivity extends BaseActivity implements OnClickListener, AMapLocationListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;//返回按钮
	private TextView tvTitle = null;
	private TextView tvPosition = null;//定位地点
	private TextView tvTime = null;//更新时间
	private TextView tvRain = null;//下雨、下雪信息
	private TextView tvTemp = null;//实况温度
	private ImageView ivPhe = null;
	private TextView tvPhe = null;//天气显现对应的图标
	private TextView tvHumidity = null;//相对湿度
	private TextView tvFall = null;//降水量
	private TextView tvWind = null;//风速
	private TextView tvPressure = null;//气压
	private LinearLayout llFeedBack = null;//意见反馈
	private LinearLayout llVersion = null;//版本检测
	private LinearLayout llClearCache = null;//清除缓存
	private LinearLayout llClearData = null;//清除数据
	private LinearLayout llBuild = null;//企业信息
	private TextView tvCache = null;
	private TextView tvData = null;
	private TextView tvUserName = null;
	private TextView tvVersion = null;//版本号
	private TextView tvLogout = null;//退出登录
	private RelativeLayout reWeather = null;
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private int hour = 0;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("HH");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hactivity_setting);
		mContext = this;
		initRefreshLayout();
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvPosition = (TextView) findViewById(R.id.tvPosition);
		tvPosition.setFocusable(true);
		tvPosition.setFocusableInTouchMode(true);
		tvPosition.requestFocus();
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvRain = (TextView) findViewById(R.id.tvRain);
		tvTemp = (TextView) findViewById(R.id.tvTemp);
		ivPhe = (ImageView) findViewById(R.id.ivPhe);
		tvPhe = (TextView) findViewById(R.id.tvPhe);
		tvHumidity = (TextView) findViewById(R.id.tvHumidity);
		tvFall = (TextView) findViewById(R.id.tvFall);
		tvWind = (TextView) findViewById(R.id.tvWind);
		tvPressure = (TextView) findViewById(R.id.tvPressure);
		llFeedBack = (LinearLayout) findViewById(R.id.llFeedBack);
		llFeedBack.setOnClickListener(this);
		llVersion = (LinearLayout) findViewById(R.id.llVersion);
		llVersion.setOnClickListener(this);
		llClearCache = (LinearLayout) findViewById(R.id.llClearCache);
		llClearCache.setOnClickListener(this);
		llClearData = (LinearLayout) findViewById(R.id.llClearData);
		llClearData.setOnClickListener(this);
		llBuild = (LinearLayout) findViewById(R.id.llBuild);
		llBuild.setOnClickListener(this);
		tvCache = (TextView) findViewById(R.id.tvCache);
		tvData = (TextView) findViewById(R.id.tvData);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.setting));
		tvLogout = (TextView) findViewById(R.id.tvLogout);
		tvLogout.setOnClickListener(this);
		tvVersion = (TextView) findViewById(R.id.tvVersion);
		tvVersion.setText(getVersion());
		reWeather = (RelativeLayout) findViewById(R.id.reWeather);
		tvUserName = (TextView) findViewById(R.id.tvUserName);

		hour = Integer.parseInt(sdf1.format(new Date()));
		
		startLocation();
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
		refreshLayout.setColor(com.hlj.common.CONST.color1, com.hlj.common.CONST.color2, com.hlj.common.CONST.color3, com.hlj.common.CONST.color4);
		refreshLayout.setMode(RefreshLayout.Mode.PULL_FROM_START);
		refreshLayout.setLoadNoFull(false);
		refreshLayout.setRefreshing(true);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				startLocation();
			}
		});
	}
	
	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	private String getVersion() {
	    try {
	        PackageManager manager = this.getPackageManager();
	        PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
	        return info.versionName;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "";
	    }
	}
	
	/**
	 * 开始定位
	 */
	private void startLocation() {
		mLocationOption = new AMapLocationClientOption();//初始化定位参数
        mLocationClient = new AMapLocationClient(mContext);//初始化定位
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
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
        	String district = amapLocation.getDistrict();
        	String street = amapLocation.getStreet()+amapLocation.getStreetNum();
        	tvPosition.setText(district + street);

    		SharedPreferences sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
    		String userName = sharedPreferences.getString(CONST.UserInfo.userName, null);
    		if (userName != null) {
    			tvUserName.setText(userName);
    		}
    		
    		try {
    			String cache = DataCleanManager.getCacheSize(mContext);
    			tvCache.setText(cache);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}

			OkHttpHourRain(amapLocation.getLongitude(), amapLocation.getLatitude());
        	getWeatherInfo(amapLocation.getLongitude(), amapLocation.getLatitude());
        }
	}
	
	/**
	 * 获取天气数据
	 */
	private void getWeatherInfo(double lng, double lat) {
		WeatherAPI.getGeo(mContext,String.valueOf(lng), String.valueOf(lat), new AsyncResponseHandler(){
			@Override
			public void onComplete(JSONObject content) {
				super.onComplete(content);
				if (!content.isNull("geo")) {
					try {
						JSONObject geoObj = content.getJSONObject("geo");
						if (!geoObj.isNull("id")) {
							String cityId = geoObj.getString("id");
							if (cityId != null) {
								WeatherAPI.getWeather2(mContext, cityId, Language.ZH_CN, new AsyncResponseHandler() {
									@Override
									public void onComplete(Weather content) {
										super.onComplete(content);
										if (content != null) {
											//实况信息
											JSONObject object = content.getWeatherFactInfo();
											try {
												if (!object.isNull("l7")) {
													String time = object.getString("l7");
													if (time != null) {
														tvTime.setText(time + getString(R.string.update));
													}
												}
												if (!object.isNull("l5")) {
													String weatherCode = WeatherUtil.lastValue(object.getString("l5"));
													tvPhe.setText(getString(WeatherUtil.getWeatherId(Integer.valueOf(weatherCode))));
													Drawable drawable;
													if (hour >= 5 && hour < 18) {
														drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
													}else {
														drawable = getResources().getDrawable(R.drawable.phenomenon_drawable_night);
													}
													drawable.setLevel(Integer.valueOf(weatherCode));
													ivPhe.setBackground(drawable);
												}
												if (!object.isNull("l1")) {
													String factTemp = WeatherUtil.lastValue(object.getString("l1"));
													tvTemp.setText(factTemp);
												}
												if (!object.isNull("l2")) {
													String humidity = WeatherUtil.lastValue(object.getString("l2"));
													tvHumidity.setText(humidity + getString(R.string.unit_percent));
												}
												if (!object.isNull("l6")) {
													String rainFall = WeatherUtil.lastValue(object.getString("l6"));
													tvFall.setText(rainFall + getString(R.string.unit_mm));
												}
												if (!object.isNull("l4")) {
													String windDir = WeatherUtil.lastValue(object.getString("l4"));
													String dir = getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir)));
													String windForce = WeatherUtil.lastValue(object.getString("l3"));
													String force = WeatherUtil.getFactWindForce(Integer.valueOf(windForce));
													tvWind.setText(dir+" "+force);
												}
												if (!object.isNull("l10")) {
													String pressure = WeatherUtil.lastValue(object.getString("l10"));
													tvPressure.setText(pressure + getString(R.string.unit_hPa));
												}
											} catch (JSONException e) {
												e.printStackTrace();
											}
											
											reWeather.setVisibility(View.VISIBLE);
											refreshLayout.setRefreshing(false);
										}
									}
									
									@Override
									public void onError(Throwable error, String content) {
										super.onError(error, content);
									}
								});
							}
						}
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
	 * 异步加载一小时内降雨、或降雪信息
	 * @param lng
	 * @param lat
	 */
	private void OkHttpHourRain(double lng, double lat) {
		String url = "http://api.caiyunapp.com/v2/HyTVV5YAkoxlQ3Zd/"+lng+","+lat+"/forecast";
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
						JSONObject object = new JSONObject(result);
						if (object != null) {
							if (!object.isNull("result")) {
								JSONObject objResult = object.getJSONObject("result");
								if (!objResult.isNull("minutely")) {
									JSONObject objMin = objResult.getJSONObject("minutely");
									if (!objMin.isNull("description")) {
										final String rain = objMin.getString("description");
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												if (!TextUtils.isEmpty(rain)) {
													tvRain.setText(rain);
													tvRain.setVisibility(View.VISIBLE);
												}else {
													tvRain.setVisibility(View.GONE);
												}
											}
										});
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
	
	/**
	 * 删除对话框
	 * @param message 标题
	 * @param content 内容
	 * @param flag 0删除本地存储，1删除缓存
	 */
	private void deleteDialog(final boolean flag, String message, String content, final TextView textView) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.delete_dialog, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvMessage.setText(message);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (flag) {
					DataCleanManager.clearCache(mContext);
					try {
						String cache = DataCleanManager.getCacheSize(mContext);
						if (cache != null) {
							textView.setText(cache);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else {
//					ChannelsManager.clearData(mContext);//清除保存在本地的频道数据
					DataCleanManager.clearLocalSave(mContext);
					try {
						String data = DataCleanManager.getLocalSaveSize(mContext);
						if (data != null) {
							textView.setText(data);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				dialog.dismiss();
			}
		});
	}
	
	/**
	 * 删除对话框
	 * @param message 标题
	 * @param content 内容
	 */
	private void logout(String message, String content) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.delete_dialog, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvMessage.setText(message);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				SharedPreferences sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
				Editor editor = sharedPreferences.edit();
				editor.clear();
				editor.commit();
				startActivity(new Intent(mContext, HLoginActivity.class));
				finish();
				MyApplication.destoryActivity(com.hlj.common.CONST.MainActivity);
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.llFeedBack:
			Intent intent = new Intent(mContext, HFeedbackActivity.class);
			intent.putExtra(CONST.ACTIVITY_NAME, getString(R.string.setting_feedback));
			intent.putExtra(CONST.INTENT_APPID, com.hlj.common.CONST.APPID);
			startActivity(intent);
			break;
		case R.id.llVersion:
			AutoUpdateUtil.checkUpdate(HSettingActivity.this, mContext, "41", getString(R.string.app_name), false);//黑龙江气象
//			AutoUpdateUtil.checkUpdate(HSettingActivity.this, mContext, "53", getString(R.string.app_name), false);//决策气象服务
			break;
		case R.id.llClearCache:
			deleteDialog(true, getString(R.string.delete_cache), getString(R.string.sure_delete_cache), tvCache);
			break;
		case R.id.llClearData:
			deleteDialog(false, getString(R.string.delete_data), getString(R.string.sure_delete_data), tvData);
			break;
		case R.id.llBuild:
			Intent intentBuild = new Intent(mContext, HUrlActivity.class);
			intentBuild.putExtra(CONST.ACTIVITY_NAME, getString(R.string.setting_build));
			intentBuild.putExtra(CONST.WEB_URL, com.hlj.common.CONST.BUILD_URL);
			startActivity(intentBuild);
			break;
		case R.id.tvLogout:
			logout(getString(R.string.logout), getString(R.string.sure_logout));
			break;

		default:
			break;
		}
	}
}
