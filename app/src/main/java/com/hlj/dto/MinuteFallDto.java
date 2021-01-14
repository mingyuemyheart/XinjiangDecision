package com.hlj.dto;

public class MinuteFallDto {

	private String imgUrl;// 网络图片的下载地�?
	public String path;// 下载到本地存放的路径
	private long time;
	private double p3;
	private double p2;
	private double p1;
	private double p4;

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public double getP3() {
		return p3;
	}

	public void setP3(double p3) {
		this.p3 = p3;
	}

	public double getP2() {
		return p2;
	}

	public void setP2(double p2) {
		this.p2 = p2;
	}

	public double getP1() {
		return p1;
	}

	public void setP1(double p1) {
		this.p1 = p1;
	}

	public double getP4() {
		return p4;
	}

	public void setP4(double p4) {
		this.p4 = p4;
	}

}
