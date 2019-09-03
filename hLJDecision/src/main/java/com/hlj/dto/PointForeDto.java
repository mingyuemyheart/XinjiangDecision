package com.hlj.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 个点预报
 */
public class PointForeDto {

    public String legendUrl;
    public String imgUrl;
    public String time;

    public List<PointForeDto> tems = new ArrayList<>();
    public List<PointForeDto> humiditys = new ArrayList<>();
    public List<PointForeDto> winds = new ArrayList<>();
    public List<PointForeDto> clouds = new ArrayList<>();

}
