package com.hlj.dto;

import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * 装载请求接口返回的数据
 * 
 * @author shawn_sun
 */
public class WindData {

	public int width = 0;
	public int height = 0;
	public double x0 = 0;
	public double y0 = 0;
	public double x1 = 0;
	public double y1 = 0;
	public String filetime = null;
	public List<WindDto> dataList = new ArrayList<>();
	public LatLng latLngStart = null;
	public LatLng latLngEnd = null;
}
