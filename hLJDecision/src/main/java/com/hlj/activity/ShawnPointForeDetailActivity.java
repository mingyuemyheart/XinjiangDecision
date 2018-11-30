package com.hlj.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.hlj.dto.StationMonitorDto;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.OkHttpUtil;
import com.hlj.view.pointfore.ForeCloudView;
import com.hlj.view.pointfore.ForeHumidityView;
import com.hlj.view.pointfore.ForeTempView;
import com.hlj.view.pointfore.ForeWindView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

/**
 * 格点预报详情
 */
public class ShawnPointForeDetailActivity extends BaseActivity implements View.OnClickListener, GeocodeSearch.OnGeocodeSearchListener {

    private Context mContext;
    private TextView tvName,tvPublishTime;
    private LinearLayout llContainerTemp,llContainerHumidity,llContainerWind,llContainerCloud;
    private GeocodeSearch geocoderSearch;
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH时", Locale.CHINA);
    private SimpleDateFormat sdf3 = new SimpleDateFormat("HH时", Locale.CHINA);
    private int width;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shawn_activity_point_fore_detail);
        mContext = this;
        showDialog();
        initWidget();
    }

    private void initWidget() {
        LinearLayout llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("格点预报详情");
        tvName = findViewById(R.id.tvName);
        tvName.setText("未知位置");
        tvPublishTime = findViewById(R.id.tvPublishTime);
        llContainerTemp = findViewById(R.id.llContainerTemp);
        llContainerHumidity = findViewById(R.id.llContainerHumidity);
        llContainerWind = findViewById(R.id.llContainerWind);
        llContainerCloud = findViewById(R.id.llContainerCloud);
        scrollView = findViewById(R.id.scrollView);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;

        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);

        double lat = getIntent().getDoubleExtra("lat", 0);
        double lng = getIntent().getDoubleExtra("lng", 0);
        searchAddrByLatLng(lat, lng);
        OkHttpList(lat, lng);
    }

    /**
     * 通过经纬度获取地理位置信息
     * @param lat
     * @param lng
     */
    private void searchAddrByLatLng(double lat, double lng) {
        //latLonPoint参数表示一个Latlng，第二参数表示范围多少米，GeocodeSearch.AMAP表示是国测局坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(lat, lng), 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
    }

    @Override
    public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
    }
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == 1000) {
            if (result != null && result.getRegeocodeAddress() != null && result.getRegeocodeAddress().getFormatAddress() != null) {
                tvName.setText(result.getRegeocodeAddress().getFormatAddress());
            }else {
                tvName.setText("未知位置");
            }
        }else {
            tvName.setText("未知位置");
        }
    }

    private void OkHttpList(final double lat, final double lng) {
        final String url = String.format("http://scapi.weather.com.cn/weather/getqggdyb?type=EDA10,R03,ECT,TMP,RRH&lonlat=%s,%s&tier=24&test=ncg", lng, lat);
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            return;
                        }
                        final String result = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(result)) {
                                    try {
                                        JSONObject obj = new JSONObject(result);
                                        if (!obj.isNull("Time")) {
                                            String publishTime = obj.getString("Time");
                                            if (!TextUtils.isEmpty(publishTime)) {
                                                try {
                                                    tvPublishTime.setText("北纬:"+lng+"°"+" "+"东经："+lat+"°"+"   "+"中央气象台"+sdf2.format(sdf1.parse(publishTime))+"发布");
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }

                                                if (!obj.isNull("TMP")) {
                                                    JSONArray array = obj.getJSONArray("TMP");
                                                    List<StationMonitorDto> list = new ArrayList<>();
                                                    for (int i = 0; i < array.length(); i++) {
                                                        StationMonitorDto dto = new StationMonitorDto();
                                                        dto.pointTemp = array.getString(i);
                                                        try {
                                                            long time = sdf1.parse(publishTime).getTime()+1000*60*60*i*3;
                                                            dto.time = sdf3.format(time);

                                                            long currentTime = new Date().getTime();
                                                            if (currentTime <= time) {
                                                                list.add(dto);
                                                            }

                                                        } catch (ParseException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                    llContainerTemp.removeAllViews();
                                                    ForeTempView view = new ForeTempView(mContext);
                                                    view.setData(list);
                                                    llContainerTemp.addView(view, width, (int)CommonUtil.dip2px(mContext, 150));
                                                }

                                                if (!obj.isNull("RRH")) {
                                                    JSONArray array = obj.getJSONArray("RRH");
                                                    List<StationMonitorDto> list = new ArrayList<>();
                                                    for (int i = 0; i < array.length(); i++) {
                                                        StationMonitorDto dto = new StationMonitorDto();
                                                        dto.humidity = array.getString(i);
                                                        try {
                                                            long time = sdf1.parse(publishTime).getTime()+1000*60*60*i*3;
                                                            dto.time = sdf3.format(time);

                                                            long currentTime = new Date().getTime();
                                                            if (currentTime <= time) {
                                                                list.add(dto);
                                                            }

                                                        } catch (ParseException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                    llContainerHumidity.removeAllViews();
                                                    ForeHumidityView view = new ForeHumidityView(mContext);
                                                    view.setData(list);
                                                    llContainerHumidity.addView(view, width, (int)CommonUtil.dip2px(mContext, 150));
                                                }

                                                if (!obj.isNull("WINS")) {
                                                    JSONArray array = obj.getJSONArray("WINS");
                                                    JSONArray array2 = obj.getJSONArray("WIND");
                                                    List<StationMonitorDto> list = new ArrayList<>();
                                                    for (int i = 0; i < array.length(); i++) {
                                                        StationMonitorDto dto = new StationMonitorDto();
                                                        dto.windSpeed = array.getString(i);
                                                        dto.windDir = array2.getString(i);
                                                        try {
                                                            long time = sdf1.parse(publishTime).getTime()+1000*60*60*i*3;
                                                            dto.time = sdf3.format(time);

                                                            long currentTime = new Date().getTime();
                                                            if (currentTime <= time) {
                                                                list.add(dto);
                                                            }

                                                        } catch (ParseException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                    llContainerWind.removeAllViews();
                                                    ForeWindView view = new ForeWindView(mContext);
                                                    view.setData(list);
                                                    llContainerWind.addView(view, width, (int)CommonUtil.dip2px(mContext, 150));
                                                }

                                                if (!obj.isNull("ECT")) {
                                                    JSONArray array = obj.getJSONArray("ECT");
                                                    List<StationMonitorDto> list = new ArrayList<>();
                                                    for (int i = 0; i < array.length(); i++) {
                                                        StationMonitorDto dto = new StationMonitorDto();
                                                        dto.cloud = array.getString(i);
                                                        try {
                                                            long time = sdf1.parse(publishTime).getTime()+1000*60*60*i*3;
                                                            dto.time = sdf3.format(time);

                                                            long currentTime = new Date().getTime();
                                                            if (currentTime <= time) {
                                                                list.add(dto);
                                                            }

                                                        } catch (ParseException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                    llContainerCloud.removeAllViews();
                                                    ForeCloudView view = new ForeCloudView(mContext);
                                                    view.setData(list);
                                                    llContainerCloud.addView(view, width, (int)CommonUtil.dip2px(mContext, 150));
                                                }
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }

                                scrollView.setVisibility(View.VISIBLE);
                                cancelDialog();
                            }
                        });
                    }
                });
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
        }
    }

}
