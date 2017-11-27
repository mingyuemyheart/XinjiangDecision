package com.hlj.dto;

import com.amap.api.maps.model.LatLng;

public class WindDto {

	public float initX;//网格横坐标
	public float initY;//网格纵坐标
	public float x = 0;// 当前粒子横坐标
	public float y = 0;// 当前粒子纵坐标
	public float oldX = -1;// 当前粒子横坐标
	public float oldY = -1;// 当前粒子纵坐标
	public int life = 0;// 生命周期，范围暂时设定在0-50
	public LatLng latLng = null;// 对应手机屏幕上点的经纬度
	public float vx = 0;// x轴速度
	public float vy = 0;// y轴速度
}
