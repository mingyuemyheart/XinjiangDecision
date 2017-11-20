package com.hlj.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class WarningDto implements Parcelable {

	public String id;
	public String name;// 预警全名
	public String html;// 详情需要用到的html
	public String time;// 预警发布时间
	public String lat;// 纬度
	public String lng;// 经度
	public String type;//预警类型，如11B09
	public String color;// 预警颜色,红橙黄蓝，id的后两位
	public String provinceId;//省份id
	public String item0;
	public int count;
	public List<WarningDto> itemList = new ArrayList<WarningDto>();

	public WarningDto() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.id);
		dest.writeString(this.name);
		dest.writeString(this.html);
		dest.writeString(this.time);
		dest.writeString(this.lat);
		dest.writeString(this.lng);
		dest.writeString(this.type);
		dest.writeString(this.color);
		dest.writeString(this.provinceId);
		dest.writeString(this.item0);
		dest.writeInt(this.count);
		dest.writeTypedList(this.itemList);
	}

	protected WarningDto(Parcel in) {
		this.id = in.readString();
		this.name = in.readString();
		this.html = in.readString();
		this.time = in.readString();
		this.lat = in.readString();
		this.lng = in.readString();
		this.type = in.readString();
		this.color = in.readString();
		this.provinceId = in.readString();
		this.item0 = in.readString();
		this.count = in.readInt();
		this.itemList = in.createTypedArrayList(WarningDto.CREATOR);
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
