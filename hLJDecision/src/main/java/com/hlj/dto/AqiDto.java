package com.hlj.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class AqiDto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public float x = 0;//x轴坐标点
	public float y = 0;//y轴坐标点
	public String date = null;//时间
	public String aqi = null;//空气质量
	public String pm2_5 = null;
	public String pm10 = null;
	public String NO2 = null;
	public String SO2 = null;
	public String O3 = null;
	public String CO = null;
	public List<AqiDto> aqiList = new ArrayList<AqiDto>();
    
}
