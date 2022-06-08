package com.hlj.dto;


public class RouteWeatherDto {

	public String cityId;
	public double lat;
	public double lng;
	public String position;//地点名称
	public String temp;//实况温度
	public String code;//天气现象编号
	public String windDir;
	public String windForce;
	public int index = 0;
	public boolean start = false;
	public boolean end = false;
	
	public String name;// 预警全名
	public String html;// 详情需要用到的html
	public String time;// 预警发布时间
	public String type;//预警类型，如11B09
	public String color;// 预警颜色,红橙黄蓝，id的后两位
	public String provinceId;//省份id
	public String item0;
}
