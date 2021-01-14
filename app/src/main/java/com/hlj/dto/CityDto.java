package com.hlj.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class CityDto implements Parcelable {

	public String alpha = null;//首字母
	public String cityName = null;//城市名称
	public String cityId = null;//城市id
	public String spellName = null;//全拼名称
	public String provinceName = null;//省份名称
	public double lng = 0;//经度
	public double lat = 0;//维度
	public String level;
	public int section;
	public String sectionName = null;
	public int lowPheCode;
	public int highPheCode;
	public String lowTemp;
	public String highTemp;
	public String areaId;//行政区划id
	public String areaName;//行政区划name
	public String warningId;//预警id
	public List<WarningDto> warningList = new ArrayList<>();

	public CityDto() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.alpha);
		dest.writeString(this.cityName);
		dest.writeString(this.cityId);
		dest.writeString(this.spellName);
		dest.writeString(this.provinceName);
		dest.writeDouble(this.lng);
		dest.writeDouble(this.lat);
		dest.writeString(this.level);
		dest.writeInt(this.section);
		dest.writeString(this.sectionName);
		dest.writeInt(this.lowPheCode);
		dest.writeInt(this.highPheCode);
		dest.writeString(this.lowTemp);
		dest.writeString(this.highTemp);
		dest.writeString(this.areaId);
		dest.writeString(this.areaName);
		dest.writeString(this.warningId);
		dest.writeTypedList(this.warningList);
	}

	protected CityDto(Parcel in) {
		this.alpha = in.readString();
		this.cityName = in.readString();
		this.cityId = in.readString();
		this.spellName = in.readString();
		this.provinceName = in.readString();
		this.lng = in.readDouble();
		this.lat = in.readDouble();
		this.level = in.readString();
		this.section = in.readInt();
		this.sectionName = in.readString();
		this.lowPheCode = in.readInt();
		this.highPheCode = in.readInt();
		this.lowTemp = in.readString();
		this.highTemp = in.readString();
		this.areaId = in.readString();
		this.areaName = in.readString();
		this.warningId = in.readString();
		this.warningList = in.createTypedArrayList(WarningDto.CREATOR);
	}

	public static final Creator<CityDto> CREATOR = new Creator<CityDto>() {
		@Override
		public CityDto createFromParcel(Parcel source) {
			return new CityDto(source);
		}

		@Override
		public CityDto[] newArray(int size) {
			return new CityDto[size];
		}
	};
}
