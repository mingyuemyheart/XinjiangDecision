package com.hlj.dto;

import android.os.Parcel;
import android.os.Parcelable;


public class RadarDto implements Parcelable{

	public String radarName;//雷达名称
	public String radarCode;//雷达站号
	public String url;//图片名称
	public String time;//发布时间
	public String isSelect;//是否选中(0未选中，1选中)

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.radarName);
		dest.writeString(this.radarCode);
		dest.writeString(this.url);
		dest.writeString(this.time);
		dest.writeString(this.isSelect);
	}

	public RadarDto() {
	}

	protected RadarDto(Parcel in) {
		this.radarName = in.readString();
		this.radarCode = in.readString();
		this.url = in.readString();
		this.time = in.readString();
		this.isSelect = in.readString();
	}

	public static final Creator<RadarDto> CREATOR = new Creator<RadarDto>() {
		@Override
		public RadarDto createFromParcel(Parcel source) {
			return new RadarDto(source);
		}

		@Override
		public RadarDto[] newArray(int size) {
			return new RadarDto[size];
		}
	};
}
