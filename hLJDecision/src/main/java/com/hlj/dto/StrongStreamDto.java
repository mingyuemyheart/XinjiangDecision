package com.hlj.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.amap.api.maps.model.LatLng;

/**
 * Created by shawn on 2017/8/18.
 */

public class StrongStreamDto implements Parcelable{

    public String path;// 下载到本地存放的路径

    public String imgUrl;//
    public String imgPath;// 下载到本地存放的路径
    public String num;//雷电次数
    public String type;//0代表 地基 1代表路基
    public String startTime;
    public double lat;
    public double lng;
    public String tag;//每条数据增加一个tag，为了绘制myseekbar时间所用
    public LatLng leftLatLng,rightLatLng;
    public int thunderCount;//闪电发生次数
    public String thunderTime;//闪电发生时间
    public float x = 0;//x轴坐标点
    public float y = 0;//y轴坐标点
    public String dataUrl;
    public boolean isCurrentTime;//是否为当前时刻

    //加油站、景点、公园
    public String stationName;
    public String pro,city;

    //雷电上传
    public String eventId;
    public String eventType;//事件类型
    public String eventContent;//图片对应内容

    //消息
    public String time;
    public String msgType;//消息类型
    public String content;//消息内容
    public String link;//跳转链接

    //审核
    public String status;//审核状态
    public String addr;//上传地点

    public StrongStreamDto() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
        dest.writeString(this.imgUrl);
        dest.writeString(this.imgPath);
        dest.writeString(this.num);
        dest.writeString(this.type);
        dest.writeString(this.startTime);
        dest.writeDouble(this.lat);
        dest.writeDouble(this.lng);
        dest.writeString(this.tag);
        dest.writeParcelable(this.leftLatLng, flags);
        dest.writeParcelable(this.rightLatLng, flags);
        dest.writeInt(this.thunderCount);
        dest.writeString(this.thunderTime);
        dest.writeFloat(this.x);
        dest.writeFloat(this.y);
        dest.writeString(this.dataUrl);
        dest.writeByte(this.isCurrentTime ? (byte) 1 : (byte) 0);
        dest.writeString(this.stationName);
        dest.writeString(this.pro);
        dest.writeString(this.city);
        dest.writeString(this.eventId);
        dest.writeString(this.eventType);
        dest.writeString(this.eventContent);
        dest.writeString(this.time);
        dest.writeString(this.msgType);
        dest.writeString(this.content);
        dest.writeString(this.link);
        dest.writeString(this.status);
        dest.writeString(this.addr);
    }

    protected StrongStreamDto(Parcel in) {
        this.path = in.readString();
        this.imgUrl = in.readString();
        this.imgPath = in.readString();
        this.num = in.readString();
        this.type = in.readString();
        this.startTime = in.readString();
        this.lat = in.readDouble();
        this.lng = in.readDouble();
        this.tag = in.readString();
        this.leftLatLng = in.readParcelable(LatLng.class.getClassLoader());
        this.rightLatLng = in.readParcelable(LatLng.class.getClassLoader());
        this.thunderCount = in.readInt();
        this.thunderTime = in.readString();
        this.x = in.readFloat();
        this.y = in.readFloat();
        this.dataUrl = in.readString();
        this.isCurrentTime = in.readByte() != 0;
        this.stationName = in.readString();
        this.pro = in.readString();
        this.city = in.readString();
        this.eventId = in.readString();
        this.eventType = in.readString();
        this.eventContent = in.readString();
        this.time = in.readString();
        this.msgType = in.readString();
        this.content = in.readString();
        this.link = in.readString();
        this.status = in.readString();
        this.addr = in.readString();
    }

    public static final Creator<StrongStreamDto> CREATOR = new Creator<StrongStreamDto>() {
        @Override
        public StrongStreamDto createFromParcel(Parcel source) {
            return new StrongStreamDto(source);
        }

        @Override
        public StrongStreamDto[] newArray(int size) {
            return new StrongStreamDto[size];
        }
    };
}
