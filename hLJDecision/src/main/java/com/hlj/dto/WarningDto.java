package com.hlj.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class WarningDto implements Parcelable {

	public String name;// 预警全名
	public String html;// 详情需要用到的html
	public String time;// 预警发布时间
	public double lat;// 纬度
	public double lng;// 经度
	public String type;//预警类型，如11B09
	public String color;// 预警颜色,红橙黄蓝，id的后两位
	public String provinceId;//省份id
	public String item0;
	public int count;
	public String colorName;
	public String nationCount;
	public String proCount;
	public String cityCount;
	public String disCount;

	//预警统计
	public String areaName;//省、市、县（区）对应的名称
	public String areaId;
	public String shortName;//预警类型，大风、冰雹等汉子
	public String warningCount;//每个areaName对应预警总数
	public String redCount;
	public String orangeCount;
	public String yellowCount;
	public String blueCount;
	public boolean isShowItem = false;
	public String areaKey;
	public String time2;//预警解除时间
	public String content;//预警详情内容
	public String unit;//发布单位

	public WarningDto() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.name);
		dest.writeString(this.html);
		dest.writeString(this.time);
		dest.writeDouble(this.lat);
		dest.writeDouble(this.lng);
		dest.writeString(this.type);
		dest.writeString(this.color);
		dest.writeString(this.provinceId);
		dest.writeString(this.item0);
		dest.writeInt(this.count);
		dest.writeString(this.colorName);
		dest.writeString(this.nationCount);
		dest.writeString(this.proCount);
		dest.writeString(this.cityCount);
		dest.writeString(this.disCount);
		dest.writeString(this.areaName);
		dest.writeString(this.areaId);
		dest.writeString(this.shortName);
		dest.writeString(this.warningCount);
		dest.writeString(this.redCount);
		dest.writeString(this.orangeCount);
		dest.writeString(this.yellowCount);
		dest.writeString(this.blueCount);
		dest.writeByte(this.isShowItem ? (byte) 1 : (byte) 0);
		dest.writeString(this.areaKey);
		dest.writeString(this.time2);
		dest.writeString(this.content);
		dest.writeString(this.unit);
	}

	protected WarningDto(Parcel in) {
		this.name = in.readString();
		this.html = in.readString();
		this.time = in.readString();
		this.lat = in.readDouble();
		this.lng = in.readDouble();
		this.type = in.readString();
		this.color = in.readString();
		this.provinceId = in.readString();
		this.item0 = in.readString();
		this.count = in.readInt();
		this.colorName = in.readString();
		this.nationCount = in.readString();
		this.proCount = in.readString();
		this.cityCount = in.readString();
		this.disCount = in.readString();
		this.areaName = in.readString();
		this.areaId = in.readString();
		this.shortName = in.readString();
		this.warningCount = in.readString();
		this.redCount = in.readString();
		this.orangeCount = in.readString();
		this.yellowCount = in.readString();
		this.blueCount = in.readString();
		this.isShowItem = in.readByte() != 0;
		this.areaKey = in.readString();
		this.time2 = in.readString();
		this.content = in.readString();
		this.unit = in.readString();
	}

	public static final Creator<WarningDto> CREATOR = new Creator<WarningDto>() {
		@Override
		public WarningDto createFromParcel(Parcel source) {
			return new WarningDto(source);
		}

		@Override
		public WarningDto[] newArray(int size) {
			return new WarningDto[size];
		}
	};
}
