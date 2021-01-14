package com.hlj.dto;

import java.util.ArrayList;
import java.util.List;

public class StationDto {

	public String date;//时间戳
	public String balltemp;//温度
	public String datatime;//时次
	public String humidity;//湿度
	public String precipitation1h;//逐小时降水量
	public String stationid;//站点号
	public String lat;//
	public String lng;//
	public String winddir;//风向
	public String windspeed;//风速
	public String rainfall3;//3小时降水量
	public String rainfall6;//6小时降水量
	public String rainfall24;//24小时降水量
	public String maxtemperature;//最高温
	public String meantemperature;//平均温度
	public String mintemperature;//最低温
	public String temperature02;//02时温度
	public String temperature08;//08时温度
	public String temperature14;//14时温度
	public String temperature20;//20时温度
	public List<StationDto> list = new ArrayList<StationDto>();//4Hlist
}
