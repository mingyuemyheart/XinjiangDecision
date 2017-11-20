package com.hlj.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class TrendDto implements Parcelable{

	public int section;
	public String areaName;//区名称
	public String streetName;//街道名称
	public float x = 0;//x轴坐标点
	public float y = 0;//y轴坐标点
	public int temp;//温度
	public int humidity;//相对湿度
	public float rainFall;//降水量
	public float windSpeed;//风速
	public int windDir;//风向
	public int pressure;//气压
	
	public List<TrendDto> tempList = new ArrayList<TrendDto>();//温度list
	public List<TrendDto> humidityList = new ArrayList<TrendDto>();//相对湿度list
	public List<TrendDto> rainFallList = new ArrayList<TrendDto>();//降水量list
	public List<TrendDto> windSpeedList = new ArrayList<TrendDto>();//风速list
	public List<TrendDto> pressureList = new ArrayList<TrendDto>();//气压list

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.section);
		dest.writeString(this.areaName);
		dest.writeString(this.streetName);
		dest.writeFloat(this.x);
		dest.writeFloat(this.y);
		dest.writeInt(this.temp);
		dest.writeInt(this.humidity);
		dest.writeFloat(this.rainFall);
		dest.writeFloat(this.windSpeed);
		dest.writeInt(this.windDir);
		dest.writeInt(this.pressure);
		dest.writeTypedList(this.tempList);
		dest.writeTypedList(this.humidityList);
		dest.writeTypedList(this.rainFallList);
		dest.writeTypedList(this.windSpeedList);
		dest.writeTypedList(this.pressureList);
	}

	public TrendDto() {
	}

	protected TrendDto(Parcel in) {
		this.section = in.readInt();
		this.areaName = in.readString();
		this.streetName = in.readString();
		this.x = in.readFloat();
		this.y = in.readFloat();
		this.temp = in.readInt();
		this.humidity = in.readInt();
		this.rainFall = in.readFloat();
		this.windSpeed = in.readFloat();
		this.windDir = in.readInt();
		this.pressure = in.readInt();
		this.tempList = in.createTypedArrayList(TrendDto.CREATOR);
		this.humidityList = in.createTypedArrayList(TrendDto.CREATOR);
		this.rainFallList = in.createTypedArrayList(TrendDto.CREATOR);
		this.windSpeedList = in.createTypedArrayList(TrendDto.CREATOR);
		this.pressureList = in.createTypedArrayList(TrendDto.CREATOR);
	}

	public static final Creator<TrendDto> CREATOR = new Creator<TrendDto>() {
		@Override
		public TrendDto createFromParcel(Parcel source) {
			return new TrendDto(source);
		}

		@Override
		public TrendDto[] newArray(int size) {
			return new TrendDto[size];
		}
	};
}
