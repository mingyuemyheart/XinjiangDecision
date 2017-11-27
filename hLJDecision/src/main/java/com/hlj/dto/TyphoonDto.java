package com.hlj.dto;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.hlj.utils.WeatherUtil;

import java.util.GregorianCalendar;

import shawn.cxwl.com.hlj.decision.R;

public class TyphoonDto {

	public int yearly;//哪年的台风
	public String name;//台风名称
	public String id;//台风id
	public String code;//台风code
	public String enName;//台风应为名称
	public String status;//台风状态,stop、start
	public double lat;
	public double lng;
	public int year; 
	public int month;
	public int day;
	public int hour;
	public String pressure;//气压
	public String max_wind_speed;//最大风速
	public String move_speed;//移动速度
	public String wind_dir;//移动方向
	public String type;//台风类型
	public String radius_7;
	public String radius_10;
	public String en_radius_7;
	public String es_radius_7;
	public String wn_radius_7;
	public String ws_radius_7;
	public String en_radius_10;
	public String es_radius_10;
	public String wn_radius_10;
	public String ws_radius_10;
	public String time;
	public boolean isFactPoint = true;//true为实况点，false为预报点
	public String strength;//台风强度
	
	public String content(Context context) {
		StringBuffer buffer = new StringBuffer();
		GregorianCalendar calendar = new GregorianCalendar(year, month-1, day, hour,0);

		if(TextUtils.isEmpty(type)){
			String time = DateFormat.format("yyyy/MM/dd kk"+context.getString(R.string.chart_hour), calendar.getTime()).toString();
			buffer.append(time).append(context.getString(R.string.chart_china_forecast));
		}else{
			String time = DateFormat.format(context.getString(R.string.chart_time1), calendar.getTime()).toString();
			buffer.append(context.getString(R.string.chart_time11111)).append(time).append("\n");
		}

		if(!TextUtils.isEmpty(max_wind_speed)){
			if (!TextUtils.isEmpty(type)) {
				if (TextUtils.equals(type, "1")) {
					strength = context.getString(R.string.typhoon_level1);
				}else if (TextUtils.equals(type, "2")) {
					strength = context.getString(R.string.typhoon_level2);
				}else if (TextUtils.equals(type, "3")) {
					strength = context.getString(R.string.typhoon_level3);
				}else if (TextUtils.equals(type, "4")) {
					strength = context.getString(R.string.typhoon_level4);
				}else if (TextUtils.equals(type, "5")) {
					strength = context.getString(R.string.typhoon_level5);
				}else if (TextUtils.equals(type, "6")) {
					strength = context.getString(R.string.typhoon_level6);
				}
				buffer.append("中心风力："+ WeatherUtil.getHourWindForce(Float.parseFloat(max_wind_speed))+"("+strength+")"+"\n");
			}
			buffer.append(context.getString(R.string.chart_biggest_speed)).append(max_wind_speed).append(context.getString(R.string.chart_speed1));
		}
		if(!TextUtils.isEmpty(pressure)){
			buffer.append(context.getString(R.string.chart_center_pressure)).append(pressure).append(context.getString(R.string.chart_baipa));
		}
		if(!TextUtils.isEmpty(wind_dir)){
			buffer.append(context.getString(R.string.chart_yidong_direct)).append(wind_dir).append("\n");
		}
		if(!TextUtils.isEmpty(move_speed)){
			buffer.append(context.getString(R.string.chart_yidong_speed)).append(move_speed).append(context.getString(R.string.chart_speed2));
		}
		if(!TextUtils.isEmpty(radius_7) && !TextUtils.equals("999999", radius_10)){
			buffer.append(context.getString(R.string.chart_radius1)).append(radius_7).append(context.getString(R.string.chart_kilometer));
		}
		if(!TextUtils.isEmpty(radius_10) && !TextUtils.equals("999999", radius_10)){
			buffer.append(context.getString(R.string.chart_radius2)).append(radius_10).append(context.getString(R.string.chart_kilometer));
		}
				
		return buffer.toString();
	}
	
	public int icon() {
		int power = TextUtils.isEmpty(type) ? -1: Integer.parseInt(type);
		if(power == 1){
			return R.drawable.typhoon_level1;
		}else if(power == 2){
			return R.drawable.typhoon_level2;
		}else if(power == 3){
			return R.drawable.typhoon_level3;
		}else if(power == 4){
			return R.drawable.typhoon_level4;
		}else if(power == 5){
			return R.drawable.typhoon_level5;
		}else if(power == 6){
			return R.drawable.typhoon_level6;
		}
		return R.drawable.typhoon_yb;
	}
	
}
