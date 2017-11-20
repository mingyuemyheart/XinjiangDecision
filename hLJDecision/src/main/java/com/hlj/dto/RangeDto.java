package com.hlj.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class RangeDto implements Parcelable{

	public int section;
	public String areaName;//区名称
	public String cityName = null;//城市名称
	public String stationId = null;//站点id
	public String value;//值
	public String cityId = null;//城市id
	public String maxTemp;//最高温度
	public String minTemp;//最低温度
	public String oneRain;//一小时降水量
	public String sixRain;//一小时降水量
	public String tfRain;//24小时降水量
	public String maxHumidity;//最大湿度
	public String minHumidity;//最小湿度
	public String windSpeed;//风速
	public String visible;//能见度

	public List<RangeDto> maxTempList = new ArrayList<RangeDto>();
	public List<RangeDto> minTempList = new ArrayList<RangeDto>();
	public List<RangeDto> oneRainList = new ArrayList<RangeDto>();
	public List<RangeDto> tfRainList = new ArrayList<RangeDto>();
	public List<RangeDto> maxHumidityList = new ArrayList<RangeDto>();
	public List<RangeDto> minHumidityList = new ArrayList<RangeDto>();
	public List<RangeDto> windSpeedList = new ArrayList<RangeDto>();
	public List<RangeDto> visibleList = new ArrayList<RangeDto>();


	public RangeDto() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.section);
		dest.writeString(this.areaName);
		dest.writeString(this.cityName);
		dest.writeString(this.stationId);
		dest.writeString(this.value);
		dest.writeString(this.cityId);
		dest.writeString(this.maxTemp);
		dest.writeString(this.minTemp);
		dest.writeString(this.oneRain);
		dest.writeString(this.sixRain);
		dest.writeString(this.tfRain);
		dest.writeString(this.maxHumidity);
		dest.writeString(this.minHumidity);
		dest.writeString(this.windSpeed);
		dest.writeString(this.visible);
		dest.writeTypedList(this.maxTempList);
		dest.writeTypedList(this.minTempList);
		dest.writeTypedList(this.oneRainList);
		dest.writeTypedList(this.tfRainList);
		dest.writeTypedList(this.maxHumidityList);
		dest.writeTypedList(this.minHumidityList);
		dest.writeTypedList(this.windSpeedList);
		dest.writeTypedList(this.visibleList);
	}

	protected RangeDto(Parcel in) {
		this.section = in.readInt();
		this.areaName = in.readString();
		this.cityName = in.readString();
		this.stationId = in.readString();
		this.value = in.readString();
		this.cityId = in.readString();
		this.maxTemp = in.readString();
		this.minTemp = in.readString();
		this.oneRain = in.readString();
		this.sixRain = in.readString();
		this.tfRain = in.readString();
		this.maxHumidity = in.readString();
		this.minHumidity = in.readString();
		this.windSpeed = in.readString();
		this.visible = in.readString();
		this.maxTempList = in.createTypedArrayList(RangeDto.CREATOR);
		this.minTempList = in.createTypedArrayList(RangeDto.CREATOR);
		this.oneRainList = in.createTypedArrayList(RangeDto.CREATOR);
		this.tfRainList = in.createTypedArrayList(RangeDto.CREATOR);
		this.maxHumidityList = in.createTypedArrayList(RangeDto.CREATOR);
		this.minHumidityList = in.createTypedArrayList(RangeDto.CREATOR);
		this.windSpeedList = in.createTypedArrayList(RangeDto.CREATOR);
		this.visibleList = in.createTypedArrayList(RangeDto.CREATOR);
	}

	public static final Creator<RangeDto> CREATOR = new Creator<RangeDto>() {
		@Override
		public RangeDto createFromParcel(Parcel source) {
			return new RangeDto(source);
		}

		@Override
		public RangeDto[] newArray(int size) {
			return new RangeDto[size];
		}
	};
}
