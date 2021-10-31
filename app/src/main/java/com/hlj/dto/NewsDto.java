package com.hlj.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class NewsDto implements Parcelable {

	public String id = null;//数组的下标0-9对应没个镇的id
	public String imgUrl = null;//图片地址
	public String title = null;//标题
	public String time = null;//时间
	public String detailUrl = null;//详情页地址
	public String showType;//显示类型
	public boolean isSelected = false;//是否被选中
	public String playTime;
	public String length;
	public String startEnd;
	public double lat;
	public double lng;
	public String cityId;
	public String warningId;
	public String level;
	public String addr;
	public String type;

	public NewsDto() {
	}

	protected NewsDto(Parcel in) {
		id = in.readString();
		imgUrl = in.readString();
		title = in.readString();
		time = in.readString();
		detailUrl = in.readString();
		showType = in.readString();
		isSelected = in.readByte() != 0;
		playTime = in.readString();
		length = in.readString();
		startEnd = in.readString();
		lat = in.readDouble();
		lng = in.readDouble();
		cityId = in.readString();
		warningId = in.readString();
		level = in.readString();
		addr = in.readString();
		type = in.readString();
	}

	public static final Creator<NewsDto> CREATOR = new Creator<NewsDto>() {
		@Override
		public NewsDto createFromParcel(Parcel in) {
			return new NewsDto(in);
		}

		@Override
		public NewsDto[] newArray(int size) {
			return new NewsDto[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(imgUrl);
		dest.writeString(title);
		dest.writeString(time);
		dest.writeString(detailUrl);
		dest.writeString(showType);
		dest.writeByte((byte) (isSelected ? 1 : 0));
		dest.writeString(playTime);
		dest.writeString(length);
		dest.writeString(startEnd);
		dest.writeDouble(lat);
		dest.writeDouble(lng);
		dest.writeString(cityId);
		dest.writeString(warningId);
		dest.writeString(level);
		dest.writeString(addr);
		dest.writeString(type);
	}
}
