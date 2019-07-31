package com.hlj.dto;

/**
 * Created by shawn on 2017/8/18.
 */

public class StrongStreamDto {

    public String imgUrl;// 网络图片的下载地�?
    public String path;// 下载到本地存放的路径
    public String time;

    public String startTime;
    public String endTime;
    public double lat;
    public double lng;
    public String tag;//每条数据增加一个tag，为了绘制myseekbar时间所用
}
