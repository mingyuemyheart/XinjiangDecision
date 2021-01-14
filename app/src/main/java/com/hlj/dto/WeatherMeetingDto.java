package com.hlj.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 天气会商
 * @author shawn_sun
 *
 */

public class WeatherMeetingDto implements Parcelable{

	public String liveName;
	public String liveStart;
	public String liveEnd;
	public String hlsAddress;
	public String columnName;
	public String columnUrl;
	public int section;
	public String startTime;
	public String endTime;
	public String title;
	public String date;
	public int state = 0;//0为无状态，1为直播，2为点播

	//点播
	public String videoImgs;
	public String videoTime;

	public WeatherMeetingDto() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.liveName);
		dest.writeString(this.liveStart);
		dest.writeString(this.liveEnd);
		dest.writeString(this.hlsAddress);
		dest.writeString(this.columnName);
		dest.writeString(this.columnUrl);
		dest.writeInt(this.section);
		dest.writeString(this.startTime);
		dest.writeString(this.endTime);
		dest.writeString(this.title);
		dest.writeString(this.date);
		dest.writeInt(this.state);
		dest.writeString(this.videoImgs);
		dest.writeString(this.videoTime);
	}

	protected WeatherMeetingDto(Parcel in) {
		this.liveName = in.readString();
		this.liveStart = in.readString();
		this.liveEnd = in.readString();
		this.hlsAddress = in.readString();
		this.columnName = in.readString();
		this.columnUrl = in.readString();
		this.section = in.readInt();
		this.startTime = in.readString();
		this.endTime = in.readString();
		this.title = in.readString();
		this.date = in.readString();
		this.state = in.readInt();
		this.videoImgs = in.readString();
		this.videoTime = in.readString();
	}

	public static final Creator<WeatherMeetingDto> CREATOR = new Creator<WeatherMeetingDto>() {
		@Override
		public WeatherMeetingDto createFromParcel(Parcel source) {
			return new WeatherMeetingDto(source);
		}

		@Override
		public WeatherMeetingDto[] newArray(int size) {
			return new WeatherMeetingDto[size];
		}
	};
}
