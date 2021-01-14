package com.hlj.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 强对流天气实况
 */
public class StreamFactDto implements Parcelable {

    public double lat;
    public double lng;
    public String stationId;
    public String stationName;
    public String province,city,dis;
    public String windD;//风向
    public String windS;//风速
    public String pre1h;//短时强降水
    public String lighting;//闪电
    public String hail;//冰雹
    public int lightingType;//1、2、3、4、5、6分别对应每10分钟，6为最新

    public StreamFactDto() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.lat);
        dest.writeDouble(this.lng);
        dest.writeString(this.stationId);
        dest.writeString(this.stationName);
        dest.writeString(this.province);
        dest.writeString(this.city);
        dest.writeString(this.dis);
        dest.writeString(this.windD);
        dest.writeString(this.windS);
        dest.writeString(this.pre1h);
        dest.writeString(this.lighting);
        dest.writeString(this.hail);
        dest.writeInt(this.lightingType);
    }

    protected StreamFactDto(Parcel in) {
        this.lat = in.readDouble();
        this.lng = in.readDouble();
        this.stationId = in.readString();
        this.stationName = in.readString();
        this.province = in.readString();
        this.city = in.readString();
        this.dis = in.readString();
        this.windD = in.readString();
        this.windS = in.readString();
        this.pre1h = in.readString();
        this.lighting = in.readString();
        this.hail = in.readString();
        this.lightingType = in.readInt();
    }

    public static final Creator<StreamFactDto> CREATOR = new Creator<StreamFactDto>() {
        @Override
        public StreamFactDto createFromParcel(Parcel source) {
            return new StreamFactDto(source);
        }

        @Override
        public StreamFactDto[] newArray(int size) {
            return new StreamFactDto[size];
        }
    };
}
