package com.hlj.activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.GroundOverlay;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.hlj.common.CONST;
import com.hlj.dto.StrongStreamDto;
import com.hlj.manager.thunder.CloudManager;
import com.hlj.manager.thunder.LeibaoManager;
import com.hlj.manager.thunder.RadarManager;
import com.hlj.manager.thunder.RainManager;
import com.hlj.manager.thunder.YdgdManager;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.OkHttpUtil;
import com.hlj.utils.SecretUrlUtil;
import com.hlj.utils.WeatherUtil;
import com.hlj.view.SeekbarTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants;
import cn.com.weather.listener.AsyncResponseHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

/**
 * ????????????
 */
public class ThunderForeActivity extends BaseActivity implements View.OnClickListener, AMap.OnMapClickListener, AMap.OnMarkerClickListener, AMap.InfoWindowAdapter, AMap.OnCameraChangeListener, GeocodeSearch.OnGeocodeSearchListener {

    private Context mContext;
    private TextureMapView mapView;
    private AMap aMap;//????????????
    private int AMapType = AMap.MAP_TYPE_NORMAL;
    private float zoom = 7.0f;
    private AMapLocationClientOption mLocationOption;//??????mLocationOption??????
    private AMapLocationClient mLocationClient;//??????AMapLocationClient?????????
    private Marker locationMarker;
    private LatLng locationLatLng;
    private ImageView iv1,iv2,ivMore,ivPlay,ivLegend;
    private LinearLayout llTimeContainer,ll1,ll2,llMore;
    private TextView tvPosition,tvStreet,tvTime,tvFact,tvThunder,tv1,tv2,tvMore,tvSeekbarTime;
    private SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
    private SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.CHINA);
    private int width;
    private GeocodeSearch geocoderSearch;
    private boolean isShowThunderMarkers = true;//??????????????????markers

    //??????????????????
    private SeekBar seekBar;
    private float seekBarWidth,itemWidth,leftMargin,rightMargin,scrollX;
    private boolean isShowRadar = false;//?????????????????????
    private Map<String, Map<String, StrongStreamDto>> thunderDataMap = new LinkedHashMap<>();//??????????????????
    private List<Marker> thunderMarkers = new ArrayList<>();//??????markers??????
    private Map<String, StrongStreamDto> radarDataMap = new LinkedHashMap<>();//??????????????????,??????????????????????????????,linked??????????????????
    private List<StrongStreamDto> radarList = new ArrayList<>();//????????????
    private Map<String, List<List<StrongStreamDto>>> scwWindDataMap = new LinkedHashMap<>();//????????????????????????
    private Map<String, List<List<StrongStreamDto>>> scwRainDataMap = new LinkedHashMap<>();//???????????????????????????
    private Map<String, List<List<StrongStreamDto>>> scwHailDataMap = new LinkedHashMap<>();//???????????????????????????
    private Map<String, List<Polyline>> scwWindPolylineMap = new LinkedHashMap<>();//????????????polyline
    private Map<String, List<Polyline>> scwRainPolylineMap = new LinkedHashMap<>();//???????????????polyline
    private Map<String, List<Polyline>> scwHailPolylineMap = new LinkedHashMap<>();//???????????????polyline
    private GroundOverlay radarOverlay;//????????????
    private RadarManager radarManager;
    private RadarThread radarThread;

    //??????
    private boolean isShowRain = false;//?????????????????????
    private Map<String, StrongStreamDto> rainDataMap = new LinkedHashMap<>();//?????????????????????,????????????????????????,linked??????????????????
    private GroundOverlay rainOverlay;
    private RainManager rainManager;

    //????????????
    private boolean isShowYunding = false;//???????????????????????????
    private Map<String, StrongStreamDto> yundingDataMap = new LinkedHashMap<>();//???????????????????????????,????????????????????????,linked??????????????????
    private GroundOverlay yundingOverlay;
    private YdgdManager yundingManager;

    //????????????
    private boolean isShowVisible = false;

    //????????????
    private boolean isShowCloud = false;//??????????????????
    private Map<String, StrongStreamDto> cloudDataMap = new LinkedHashMap<>();//??????????????????,??????????????????????????????,linked??????????????????
    private GroundOverlay cloudOverlay;//??????
    private CloudManager cloudManager;

    //????????????
    private boolean isShowLeibao = false;//????????????????????????
    private Map<String, StrongStreamDto> leibaoDataMap = new LinkedHashMap<>();//?????????????????????,????????????????????????,linked??????????????????
    private GroundOverlay leibaoOverlay;
    private LeibaoManager leibaoManager;

    //??????
    private RelativeLayout reMore;
    private boolean isShowMore = false;//??????????????????layout
    private ImageView ivMore1,ivMore2,ivMore3,ivMore4,ivMore5,ivMore6;

    //??????
    private LinearLayout llSetting;
    private boolean isShowSetting = false;//??????????????????layout
    private boolean isShowPetrol = false, isShowScenic = false, isShowPark = false;
    private ImageView ivSetting,ivMapType1,ivMapType2,ivPetrol,ivScenic,ivPark;
    private String Petrol = "Petrol",Scenic = "Scenic",Park = "Park";//??????????????????
    private Map<String, StrongStreamDto> proStationMap = new LinkedHashMap<>();//???????????????key?????????map
    private List<Marker> proStationMarkers = new ArrayList<>();//???????????????markers
    private Map<String, List<StrongStreamDto>> petrolDataMap = new LinkedHashMap<>();//???????????????key??????????????????
    private List<StrongStreamDto> scenicList = new ArrayList<>();//????????????
    private List<StrongStreamDto> parkList = new ArrayList<>();//????????????
    private List<Marker> petrolMarkers = new ArrayList<>();//?????????markers
    private List<Marker> scenicMarkers = new ArrayList<>();//??????markers
    private List<Marker> parkMarkers = new ArrayList<>();//??????markers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thunder_fore);
        mContext = this;
        initWidget();
        initAmap(savedInstanceState);
    }

    private void initWidget() {
        showDialog();
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(getIntent().getStringExtra(CONST.ACTIVITY_NAME));
        LinearLayout llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        ImageView ivLocation = findViewById(R.id.ivLocation);
        ivLocation.setOnClickListener(this);
        ImageView ivLg = findViewById(R.id.ivLg);
        ivLg.setOnClickListener(this);
        ivLegend = findViewById(R.id.ivLegend);
        iv1 = findViewById(R.id.iv1);
        iv2 = findViewById(R.id.iv2);
        ivMore = findViewById(R.id.ivMore);
        tv1 = findViewById(R.id.tv1);
        tv2 = findViewById(R.id.tv2);
        tvMore = findViewById(R.id.tvMore);
        ll1 = findViewById(R.id.ll1);
        ll1.setOnClickListener(this);
        ll2 = findViewById(R.id.ll2);
        ll2.setOnClickListener(this);
        llMore = findViewById(R.id.llMore);
        llMore.setOnClickListener(this);
        LinearLayout llThunder = findViewById(R.id.llThunder);
        llThunder.setOnClickListener(this);
        tvPosition = findViewById(R.id.tvPosition);
        tvStreet = findViewById(R.id.tvStreet);
        tvTime = findViewById(R.id.tvTime);
        tvFact = findViewById(R.id.tvFact);
        tvThunder = findViewById(R.id.tvThunder);
        llTimeContainer = findViewById(R.id.llTimeContainer);
        tvSeekbarTime = findViewById(R.id.tvSeekbarTime);
        ivPlay = findViewById(R.id.ivPlay);
        ivPlay.setOnClickListener(this);
        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(seekbarChangeListener);

        //??????
        reMore = findViewById(R.id.reMore);
        ivMore1 = findViewById(R.id.ivMore1);
        ivMore1.setOnClickListener(this);
        ivMore2 = findViewById(R.id.ivMore2);
        ivMore2.setOnClickListener(this);
        ivMore3 = findViewById(R.id.ivMore3);
        ivMore3.setOnClickListener(this);
        ivMore4 = findViewById(R.id.ivMore4);
        ivMore4.setOnClickListener(this);
        ivMore5 = findViewById(R.id.ivMore5);
        ivMore5.setOnClickListener(this);
        ivMore6 = findViewById(R.id.ivMore6);
        ivMore6.setOnClickListener(this);

        //??????
        llSetting = findViewById(R.id.llSetting);
        ivSetting = findViewById(R.id.ivSetting);
        ivSetting.setOnClickListener(this);
        ivMapType1 = findViewById(R.id.ivMapType1);
        ivMapType1.setOnClickListener(this);
        ivMapType2 = findViewById(R.id.ivMapType2);
        ivMapType2.setOnClickListener(this);
        ivPetrol = findViewById(R.id.ivPetrol);
        ivPetrol.setOnClickListener(this);
        ivScenic = findViewById(R.id.ivScenic);
        ivScenic.setOnClickListener(this);
        ivPark = findViewById(R.id.ivPark);
        ivPark.setOnClickListener(this);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;

        //??????seebar???????????????
        leftMargin = CommonUtil.dip2px(mContext, 40);
        rightMargin = CommonUtil.dip2px(mContext, 20);
        seekBarWidth = width-leftMargin-rightMargin;
        scrollX = leftMargin;

        geocoderSearch = new GeocodeSearch(mContext);
        geocoderSearch.setOnGeocodeSearchListener(this);

        radarManager = new RadarManager(mContext);
        cloudManager = new CloudManager(mContext);
        rainManager = new RainManager(mContext);
        leibaoManager = new LeibaoManager(mContext);
        yundingManager = new YdgdManager(mContext);
    }

    /**
     * ?????????????????????
     */
    private void initAmap(Bundle savedInstanceState) {
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), zoom));
        aMap.getUiSettings().setMyLocationButtonEnabled(false);// ????????????????????????????????????
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setRotateGesturesEnabled(false);
        aMap.setMapType(AMapType);
        aMap.setOnMapClickListener(this);
        aMap.setOnMarkerClickListener(this);
        aMap.setInfoWindowAdapter(this);
//        aMap.setOnInfoWindowClickListener(this);
        aMap.setOnCameraChangeListener(this);
    }

    /**
     * ????????????????????????
     */
    private void mapLoaded() {
        if (CommonUtil.isLocationOpen(mContext)) {
            startLocation();
        }else {
            locationComplete("?????????", "?????????", "?????????", "2???", 39.904030, 116.407526);
        }
    }

    /**
     * ????????????
     */
    private void startLocation() {
        if (mLocationOption == null) {
            mLocationOption = new AMapLocationClientOption();//?????????????????????
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//???????????????????????????????????????Battery_Saving?????????????????????Device_Sensors??????????????????
            mLocationOption.setNeedAddress(true);//????????????????????????????????????????????????????????????
            mLocationOption.setOnceLocation(true);//???????????????????????????,?????????false
            mLocationOption.setMockEnable(false);//??????????????????????????????,?????????false????????????????????????
            mLocationOption.setInterval(2000);//??????????????????,????????????,?????????2000ms
        }
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(mContext);//???????????????
            mLocationClient.setLocationOption(mLocationOption);//??????????????????????????????????????????
        }
        mLocationClient.startLocation();//????????????
        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null && aMapLocation.getErrorCode() == AMapLocation.LOCATION_SUCCESS) {
                    locationComplete(aMapLocation.getCity(), aMapLocation.getDistrict(), aMapLocation.getStreet(),
                            aMapLocation.getStreetNum(), aMapLocation.getLatitude(), aMapLocation.getLongitude());
                }else {
                    locationComplete("?????????", "?????????", "?????????", "2???", 39.904030, 116.407526);
                }
            }
        });
    }

    private void locationComplete(String city, String dis, String street, String streetNum, double lat, double lng) {
        if (radarThread != null) {
            radarThread.pause();
        }
        tvPosition.setText(city+" | "+dis);
        tvStreet.setText(street+streetNum);
        locationLatLng = new LatLng(lat, lng);
        addLocationMarker();
        OkHttpThunderForecast(locationLatLng.longitude, locationLatLng.latitude);
        OkHttpThunder();
        OkHttpGeo(locationLatLng.longitude, locationLatLng.latitude);

        //??????????????????????????????
        if (isShowRain) {
            rainDataMap.clear();
            OkHttpRain();
        }
        if (isShowYunding) {
            yundingDataMap.clear();
            OkHttpYunding();
        }
        if (isShowCloud) {
            cloudDataMap.clear();
            OkHttpCloud();
        }
        if (isShowLeibao) {
            leibaoDataMap.clear();
            OkHttpLeibao();
        }
    }

    private void addLocationMarker() {
        if (locationLatLng == null) {
            return;
        }
        MarkerOptions options = new MarkerOptions();
        options.position(locationLatLng);
        options.anchor(0.5f, 1.0f);
        Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.shawn_iv_map_click_map),
                (int)(CommonUtil.dip2px(mContext, 21)), (int)(CommonUtil.dip2px(mContext, 32)));
        if (bitmap != null) {
            options.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }else {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.shawn_iv_map_click_map));
        }
        if (locationMarker != null) {
            locationMarker.remove();
        }
        locationMarker = aMap.addMarker(options);
        locationMarker.setClickable(false);
        aMap.animateCamera(CameraUpdateFactory.newLatLng(locationLatLng));
    }

    /**
     * ??????9?????????id
     */
    private void OkHttpGeo(final double lng, final double lat) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.enqueue(new Request.Builder().url(SecretUrlUtil.geo(lng, lat)).build(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            return;
                        }
                        String result = response.body().string();
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                JSONObject obj = new JSONObject(result);
                                if (!obj.isNull("geo")) {
                                    JSONObject geoObj = obj.getJSONObject("geo");
                                    if (!geoObj.isNull("id")) {
                                        String cityId = geoObj.getString("id");
                                        getWeatherInfo(cityId);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * ??????????????????
     * @param cityId
     */
    private void getWeatherInfo(final String cityId) {
        if (TextUtils.isEmpty(cityId)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                WeatherAPI.getWeather2(mContext, cityId, Constants.Language.ZH_CN, new AsyncResponseHandler() {
                    @Override
                    public void onComplete(final Weather content) {
                        super.onComplete(content);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String result = content.toString();
                                if (!TextUtils.isEmpty(result)) {
                                    try {
                                        JSONObject obj = new JSONObject(result);

                                        //????????????
                                        if (!obj.isNull("l")) {
                                            JSONObject l = obj.getJSONObject("l");
//                                            if (!l.isNull("l7")) {
//                                                String time = l.getString("l7");
//                                                if (time != null) {
//                                                    tvTime.setText(sdf1.format(new Date())+" "+time+"??????");
//                                                }
//                                            }
                                            String factTemp = "", humidity = "", wind = "";
                                            if (!l.isNull("l1")) {
                                                factTemp = "??????"+ WeatherUtil.lastValue(l.getString("l1"))+"??? | ";
                                            }
                                            if (!l.isNull("l2")) {
                                                humidity = WeatherUtil.lastValue(l.getString("l2"));
                                                if (TextUtils.isEmpty(humidity) || TextUtils.equals(humidity, "null")) {
                                                    humidity = "??????" + "-- | ";
                                                }else {
                                                    humidity = "??????"+humidity+"% | ";
                                                }
                                            }
                                            if (!l.isNull("l4")) {
                                                String windDir = WeatherUtil.lastValue(l.getString("l4"));
                                                if (!l.isNull("l3")) {
                                                    String windForce = WeatherUtil.lastValue(l.getString("l3"));
                                                    wind = getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir))) +
                                                            WeatherUtil.getFactWindForce(Integer.valueOf(windForce))+" | ";
                                                }
                                            }
                                            tvFact.setText(factTemp+humidity+wind);
                                        }

                                        //aqi??????
                                        if (!obj.isNull("k")) {
                                            JSONObject k = obj.getJSONObject("k");
                                            if (!k.isNull("k3")) {
                                                String aqi = WeatherUtil.lastValue(k.getString("k3"));
                                                tvFact.setText(tvFact.getText().toString()+" ????????????" + WeatherUtil.getAqi(mContext, Integer.valueOf(aqi)) + " " + aqi);
                                            }
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        });
                    }

                    @Override
                    public void onError(Throwable error, String content) {
                        super.onError(error, content);
                    }
                });
            }
        }).start();
    }

    /**
     * ????????????
     * @param lng
     * @param lat
     */
    private void OkHttpThunderForecast(double lng, double lat) {
        final String url = String.format("http://lightning.app.tianqi.cn/lightning/lhdata/ldzs?lonlat=%s,%s", lng, lat);
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
                                        if (!obj.isNull("markedwords")) {
                                            tvThunder.setText(obj.getString("markedwords"));
                                        }else {
                                            tvThunder.setText("????????????");
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }

    /**
     * ?????????????????????????????????????????????
     */
    private void OkHttpThunder() {
        showDialog();
        final String url = "http://lightning.app.tianqi.cn/lightning/lhdata/ldskyb";
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cancelDialog();
                            }
                        });
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cancelDialog();
                            }
                        });
                        if (!response.isSuccessful()) {
                            return;
                        }
                        String result = response.body().string();
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                JSONObject object = new JSONObject(result);

                                //????????????
                                thunderDataMap.clear();
                                if (!object.isNull("lightning")) {
                                    JSONObject obj = object.getJSONObject("lightning");
                                    if (!obj.isNull("observe")) {
                                        JSONArray array = obj.getJSONArray("observe");
                                        for (int i = 0; i < array.length(); i++) {
                                            JSONObject itemObj = array.getJSONObject(i);
                                            if (!itemObj.isNull("startTime")) {
                                                final String startTime = itemObj.getString("startTime");
                                                if (!itemObj.isNull("data")) {
                                                    JSONArray dataArray = itemObj.getJSONArray("data");
                                                    Map<String, StrongStreamDto> map = new HashMap<>();
                                                    Log.e("observe", dataArray.length()+"");
                                                    for (int j = 0; j < dataArray.length(); j++) {
                                                        JSONObject dataObj = dataArray.getJSONObject(j);
                                                        StrongStreamDto dto = new StrongStreamDto();
                                                        if (!dataObj.isNull("lat")) {
                                                            dto.lat = dataObj.getDouble("lat");
                                                        }
                                                        if (!dataObj.isNull("lon")) {
                                                            dto.lng = dataObj.getDouble("lon");
                                                        }
                                                        if (!dataObj.isNull("type")) {
                                                            dto.type = dataObj.getString("type");
                                                        }
                                                        if (!dataObj.isNull("num")) {
                                                            dto.num = dataObj.getString("num");
                                                        }
                                                        map.put(dto.lat+","+dto.lng, dto);
                                                    }
                                                    thunderDataMap.put(startTime, map);
                                                    Log.e("observe1", map.size()+"");
                                                }

                                                if (i == array.length()-1) {
                                                    drawMutiElement(startTime);
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            try {
                                                                tvTime.setText(sdf4.format(sdf3.parse(startTime))+"??????");
                                                            } catch (ParseException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }

                                    if (!obj.isNull("forecast")) {
                                        JSONArray array = obj.getJSONArray("forecast");
                                        for (int i = 0; i < array.length(); i++) {
                                            JSONObject itemObj = array.getJSONObject(i);
                                            if (!itemObj.isNull("startTime")) {
                                                String startTime = itemObj.getString("startTime");
                                                if (!itemObj.isNull("data")) {
                                                    JSONArray dataArray = itemObj.getJSONArray("data");
                                                    Map<String, StrongStreamDto> map = new HashMap<>();
                                                    Log.e("forecast", dataArray.length()+"");
                                                    for (int j = 0; j < dataArray.length(); j++) {
                                                        JSONObject dataObj = dataArray.getJSONObject(j);
                                                        StrongStreamDto dto = new StrongStreamDto();
                                                        if (!dataObj.isNull("lat")) {
                                                            dto.lat = dataObj.getDouble("lat");
                                                        }
                                                        if (!dataObj.isNull("lon")) {
                                                            dto.lng = dataObj.getDouble("lon");
                                                        }
                                                        if (!dataObj.isNull("type")) {
                                                            dto.type = dataObj.getString("type");
                                                        }
                                                        if (!dataObj.isNull("num")) {
                                                            dto.num = dataObj.getString("num");
                                                        }
                                                        map.put(dto.lat+","+dto.lng, dto);
                                                    }
                                                    thunderDataMap.put(startTime, map);
                                                }
                                            }
                                        }
                                    }

                                }

                                //?????????????????????
                                scwWindDataMap.clear();
                                scwRainDataMap.clear();
                                scwHailDataMap.clear();
                                if (!object.isNull("cn_scw")) {
                                    JSONObject obj = object.getJSONObject("cn_scw");

                                    //??????
                                    if (!obj.isNull("obs")) {
                                        JSONArray array = obj.getJSONArray("obs");
                                        for (int i = 0; i < array.length(); i++) {
                                            JSONObject itemObj = array.getJSONObject(i);
                                            if (!itemObj.isNull("startTime")) {
                                                String startTime = itemObj.getString("startTime");

                                                if (!itemObj.isNull("wind")  && !TextUtils.isEmpty(itemObj.getString("wind"))) {
                                                    List<List<StrongStreamDto>> list = new ArrayList<>();
                                                    JSONArray array1 = itemObj.getJSONArray("wind");
                                                    for (int j = 0; j < array1.length(); j++) {
                                                        JSONObject o = array1.getJSONObject(j);
                                                        if (!o.isNull("xy")) {
                                                            List<StrongStreamDto> itemList = new ArrayList<>();
                                                            JSONArray itemArray = o.getJSONArray("xy");
                                                            for (int m = 0; m < itemArray.length(); m+=2) {
                                                                StrongStreamDto dto = new StrongStreamDto();
                                                                dto.lat = itemArray.getDouble(m);
                                                                dto.lng = itemArray.getDouble(m+1);
                                                                itemList.add(dto);
                                                            }
                                                            list.add(itemList);
                                                        }
                                                    }
                                                    scwWindDataMap.put(startTime, list);
                                                }

                                                if (!itemObj.isNull("rain")  && !TextUtils.isEmpty(itemObj.getString("rain"))) {
                                                    List<List<StrongStreamDto>> list = new ArrayList<>();
                                                    JSONArray array1 = itemObj.getJSONArray("rain");
                                                    for (int j = 0; j < array1.length(); j++) {
                                                        JSONObject o = array1.getJSONObject(j);
                                                        if (!o.isNull("xy")) {
                                                            List<StrongStreamDto> itemList = new ArrayList<>();
                                                            JSONArray itemArray = o.getJSONArray("xy");
                                                            for (int m = 0; m < itemArray.length(); m+=2) {
                                                                StrongStreamDto dto = new StrongStreamDto();
                                                                dto.lat = itemArray.getDouble(m);
                                                                dto.lng = itemArray.getDouble(m+1);
                                                                itemList.add(dto);
                                                            }
                                                            list.add(itemList);
                                                        }
                                                    }
                                                    scwRainDataMap.put(startTime, list);
                                                }

                                                if (!itemObj.isNull("hail") && !TextUtils.isEmpty(itemObj.getString("hail"))) {
                                                    List<List<StrongStreamDto>> list = new ArrayList<>();
                                                    JSONArray array1 = itemObj.getJSONArray("hail");
                                                    for (int j = 0; j < array1.length(); j++) {
                                                        JSONObject o = array1.getJSONObject(j);
                                                        if (!o.isNull("xy")) {
                                                            List<StrongStreamDto> itemList = new ArrayList<>();
                                                            JSONArray itemArray = o.getJSONArray("xy");
                                                            for (int m = 0; m < itemArray.length(); m+=2) {
                                                                StrongStreamDto dto = new StrongStreamDto();
                                                                dto.lat = itemArray.getDouble(m);
                                                                dto.lng = itemArray.getDouble(m+1);
                                                                itemList.add(dto);
                                                            }
                                                            list.add(itemList);
                                                        }
                                                    }
                                                    scwHailDataMap.put(startTime, list);
                                                }

                                            }
                                        }
                                    }

                                    //??????
                                    if (!obj.isNull("forecast")) {
                                        JSONObject forecast = obj.getJSONObject("forecast");
                                        if (forecast.isNull("startTime")) {
                                            String startTime = forecast.getString("startTime");

                                            if (!forecast.isNull("wind")  && !TextUtils.isEmpty(forecast.getString("wind"))) {
                                                List<List<StrongStreamDto>> list = new ArrayList<>();
                                                JSONArray array1 = forecast.getJSONArray("wind");
                                                for (int j = 0; j < array1.length(); j++) {
                                                    JSONObject o = array1.getJSONObject(j);
                                                    if (!o.isNull("xy")) {
                                                        List<StrongStreamDto> itemList = new ArrayList<>();
                                                        JSONArray itemArray = o.getJSONArray("xy");
                                                        for (int m = 0; m < itemArray.length(); m+=2) {
                                                            StrongStreamDto dto = new StrongStreamDto();
                                                            dto.lat = itemArray.getDouble(m);
                                                            dto.lng = itemArray.getDouble(m+1);
                                                            itemList.add(dto);
                                                        }
                                                        list.add(itemList);
                                                    }
                                                }
                                                scwWindDataMap.put(startTime, list);
                                            }

                                            if (!forecast.isNull("rain")  && !TextUtils.isEmpty(forecast.getString("rain"))) {
                                                List<List<StrongStreamDto>> list = new ArrayList<>();
                                                JSONArray array1 = forecast.getJSONArray("rain");
                                                for (int j = 0; j < array1.length(); j++) {
                                                    JSONObject o = array1.getJSONObject(j);
                                                    if (!o.isNull("xy")) {
                                                        List<StrongStreamDto> itemList = new ArrayList<>();
                                                        JSONArray itemArray = o.getJSONArray("xy");
                                                        for (int m = 0; m < itemArray.length(); m+=2) {
                                                            StrongStreamDto dto = new StrongStreamDto();
                                                            dto.lat = itemArray.getDouble(m);
                                                            dto.lng = itemArray.getDouble(m+1);
                                                            itemList.add(dto);
                                                        }
                                                        list.add(itemList);
                                                    }
                                                }
                                                scwRainDataMap.put(startTime, list);
                                            }

                                            if (!forecast.isNull("hail") && !TextUtils.isEmpty(forecast.getString("hail"))) {
                                                List<List<StrongStreamDto>> list = new ArrayList<>();
                                                JSONArray array1 = forecast.getJSONArray("hail");
                                                for (int j = 0; j < array1.length(); j++) {
                                                    JSONObject o = array1.getJSONObject(j);
                                                    if (!o.isNull("xy")) {
                                                        List<StrongStreamDto> itemList = new ArrayList<>();
                                                        JSONArray itemArray = o.getJSONArray("xy");
                                                        for (int m = 0; m < itemArray.length(); m+=2) {
                                                            StrongStreamDto dto = new StrongStreamDto();
                                                            dto.lat = itemArray.getDouble(m);
                                                            dto.lng = itemArray.getDouble(m+1);
                                                            itemList.add(dto);
                                                        }
                                                        list.add(itemList);
                                                    }
                                                }
                                                scwHailDataMap.put(startTime, list);
                                            }

                                        }
                                    }

                                }

                                //??????????????????
                                radarDataMap.clear();
                                radarList.clear();
                                if (!object.isNull("radar")) {
                                    JSONObject obj = object.getJSONObject("radar");

                                    if (!obj.isNull("files_before")) {
                                        JSONArray array = obj.getJSONArray("files_before");
                                        for (int i = 0; i < array.length(); i++) {
                                            String itemUrl = array.getString(i);
                                            if (!TextUtils.isEmpty(itemUrl)) {
                                                StrongStreamDto dto = new StrongStreamDto();
                                                dto.imgUrl = "http://radar-qpfref.tianqi.cn/"+itemUrl;
                                                dto.startTime = itemUrl.substring(itemUrl.length()-16, itemUrl.length()-4);
                                                if (i == array.length()-1) {
                                                    dto.isCurrentTime = true;
                                                    dto.tag = dto.startTime;
//                                                    drawMutiElement(dto.startTime);
                                                }
                                                radarDataMap.put(dto.startTime, dto);
                                                radarList.add(dto);
                                            }
                                        }
                                    }

                                    if (!obj.isNull("files_after")) {
                                        JSONArray array = obj.getJSONArray("files_after");
                                        for (int i = 0; i < array.length(); i++) {
                                            if (i == array.length()-1) {
                                                for (int j = 0; j <= 12; j++) {
                                                    String itemUrl = array.getString(i);
                                                    StrongStreamDto dto = new StrongStreamDto();
                                                    dto.imgUrl = "http://radar-qpfref.tianqi.cn/"+itemUrl;
                                                    String time = itemUrl.substring(itemUrl.length()-16, itemUrl.length()-4);
                                                    try {
                                                        dto.startTime = sdf3.format(sdf3.parse(time).getTime()+1000*60*5*j);
                                                        radarDataMap.put(dto.startTime, dto);
                                                        radarList.add(dto);
                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                    } catch (ArrayIndexOutOfBoundsException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }else {
                                                String itemUrl = array.getString(i);
                                                StrongStreamDto dto = new StrongStreamDto();
                                                dto.imgUrl = "http://radar-qpfref.tianqi.cn/"+itemUrl;
                                                dto.startTime = itemUrl.substring(itemUrl.length()-16, itemUrl.length()-4);
                                                radarDataMap.put(dto.startTime, dto);
                                                radarList.add(dto);
                                            }
                                        }
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (radarList.size() <= 0) {
                                                return;
                                            }
                                            try {
                                                int currentIndex = 0;
                                                for (int i = 0; i < radarList.size(); i++) {
                                                    StrongStreamDto data = radarList.get(i);
                                                    //????????????????????????
                                                    if (data.isCurrentTime) {
                                                        currentIndex = i;
                                                    }
                                                }

                                                seekBar.setMax(radarList.size()-1);
                                                seekBar.setSecondaryProgress(currentIndex);
                                                seekBar.setProgress(currentIndex);
                                                itemWidth = seekBarWidth/radarList.size();

                                                if (!TextUtils.isEmpty(radarList.get(currentIndex).startTime)) {
                                                    try {
                                                        tvSeekbarTime.setText(sdf1.format(sdf3.parse(radarList.get(currentIndex).startTime)));
                                                        tvSeekbarTime.measure(0, 0);
                                                        TranslateAnimation anim = new TranslateAnimation(scrollX,leftMargin-tvSeekbarTime.getMeasuredWidth()+itemWidth*currentIndex,0,0);
                                                        anim.setDuration(50);
                                                        anim.setFillAfter(true);
                                                        tvSeekbarTime.setAnimation(anim);
                                                        scrollX = leftMargin-tvSeekbarTime.getMeasuredWidth()+itemWidth*currentIndex;
                                                    } catch (IndexOutOfBoundsException e) {
                                                        e.printStackTrace();
                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                    }
                                                }

                                                //?????????????????????
                                                llTimeContainer.removeAllViews();
                                                SeekbarTime seekbarTime = new SeekbarTime(mContext);
                                                seekbarTime.setData(radarList);
                                                llTimeContainer.addView(seekbarTime, width, (int)CommonUtil.dip2px(mContext, 15));

                                                if (radarThread == null) {
                                                    radarThread = new RadarThread(radarList);
                                                    radarThread.index = currentIndex;
                                                }

                                                ivPlay.setImageResource(R.drawable.icon_play);
                                                ivPlay.setVisibility(View.VISIBLE);
                                                tvSeekbarTime.setVisibility(View.VISIBLE);
                                                seekBar.setVisibility(View.VISIBLE);
                                                llTimeContainer.setVisibility(View.VISIBLE);
                                                cancelDialog();
                                            } catch (IndexOutOfBoundsException e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    });

                                    if (radarDataMap.size() > 0) {
                                        startDownloadRadarImgs();
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * seekba????????????
     */
    private SeekBar.OnSeekBarChangeListener seekbarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//            if (radarThread != null) {
//                radarThread.setCurrent(seekBar.getProgress());
//            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (radarThread != null) {
                radarThread.startTracking();
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (radarThread != null) {
                radarThread.setCurrent(seekBar.getProgress());
                radarThread.stopTracking();
            }
        }
    };

    private class RadarThread extends Thread {

        private static final int STATE_NONE = 0;
        private static final int STATE_PLAYING = 1;
        private static final int STATE_PAUSE = 2;
        private static final int STATE_CANCEL = 3;
        private List<StrongStreamDto> radars;
        private int state;
        private int index;
        private boolean isTracking;

        private RadarThread(List<StrongStreamDto> radars) {
            this.radars = radars;
            this.index = 0;
            this.state = STATE_NONE;
            this.isTracking = false;
        }

        private int getCurrentState() {
            return state;
        }

        @Override
        public void run() {
            super.run();
            this.state = STATE_PLAYING;
            while (true) {
                if (state == STATE_CANCEL) {
                    break;
                }
                if (state == STATE_PAUSE) {
                    continue;
                }
                if (isTracking) {
                    continue;
                }

                sendRadar();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendRadar() {
            if (index >= radars.size() || index < 0) {
                index = 0;
            }else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StrongStreamDto dto = radars.get(index);
                        if (seekBar != null && !TextUtils.isEmpty(dto.startTime)) {
                            try {
                                int currentIndex = index++;
                                Log.e("currentIndex",currentIndex+"");
                                seekBar.setProgress(currentIndex);
                                tvSeekbarTime.setText(sdf1.format(sdf3.parse(dto.startTime)));
                                tvSeekbarTime.measure(0, 0);
                                TranslateAnimation anim = new TranslateAnimation(scrollX,leftMargin-tvSeekbarTime.getMeasuredWidth()+itemWidth*currentIndex,0,0);
                                anim.setDuration(50);
                                anim.setFillAfter(true);
                                tvSeekbarTime.setAnimation(anim);
                                scrollX = leftMargin-tvSeekbarTime.getMeasuredWidth()+itemWidth*currentIndex;
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        drawMutiElement(dto.startTime);
                    }
                });
            }
        }

        private void cancel() {
            this.state = STATE_CANCEL;
        }
        private void pause() {
            this.state = STATE_PAUSE;
        }
        private void play() {
            this.state = STATE_PLAYING;
        }

        private void setCurrent(int index) {
            this.index = index;
            sendRadar();
        }

        private void startTracking() {
            isTracking = true;
        }

        private void stopTracking() {
            isTracking = false;
            if (this.state == STATE_PAUSE) {
                sendRadar();
            }
        }
    }

    /**
     * ??????????????????
     */
    private void drawMutiElement(final String startTime) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(startTime)) {
                    //??????????????????
                    if (thunderDataMap.containsKey(startTime)) {
                        drawThunderMarkers(startTime);
                    }

                    //???????????????????????????
                    drawScwLayer(startTime);
                }

                //??????????????????????????????
                if (isShowRadar) {
                    if (radarDataMap.containsKey(startTime)) {
                        StrongStreamDto dto = radarDataMap.get(startTime);
                        if (!TextUtils.isEmpty(dto.imgPath)) {
                            Bitmap bitmap = BitmapFactory.decodeFile(dto.imgPath);
                            if (bitmap != null) {
                                drawRadarImg(bitmap);
                            }
                        }else {
                            removeRadarOverlay();
                        }
                    }
                }

                //??????????????????????????????
                if (isShowRain) {
                    if (rainDataMap.containsKey(startTime)) {
                        StrongStreamDto dto = rainDataMap.get(startTime);
                        if (!TextUtils.isEmpty(dto.imgPath)) {
                            Bitmap bitmap = BitmapFactory.decodeFile(dto.imgPath);
                            if (bitmap != null) {
                                drawRainImg(bitmap, dto);
                            }
                        }else {
                            removeRainOverlay();
                        }
                    }
                }

                //????????????????????????????????????
                if (isShowYunding) {
                    if (yundingDataMap.containsKey(startTime)) {
                        StrongStreamDto dto = yundingDataMap.get(startTime);
                        if (!TextUtils.isEmpty(dto.imgPath)) {
                            Bitmap bitmap = BitmapFactory.decodeFile(dto.imgPath);
                            if (bitmap != null) {
                                drawYundingImg(bitmap, dto);
                            }
                        }else {
                            removeYundingOverlay();
                        }
                    }
                }

                //?????????????????????????????????
                if (isShowCloud) {
                    if (cloudDataMap.containsKey(startTime)) {
                        StrongStreamDto dto = cloudDataMap.get(startTime);
                        if (!TextUtils.isEmpty(dto.imgPath)) {
                            Bitmap bitmap = BitmapFactory.decodeFile(dto.imgPath);
                            if (bitmap != null) {
                                drawCloudImg(bitmap, dto);
                            }
                        }else {
                            removeCloudOverlay();
                        }
                    }
                }

                //?????????????????????????????????
                if (isShowLeibao) {
                    if (leibaoDataMap.containsKey(startTime)) {
                        StrongStreamDto dto = leibaoDataMap.get(startTime);
                        if (!TextUtils.isEmpty(dto.imgPath)) {
                            Bitmap bitmap = BitmapFactory.decodeFile(dto.imgPath);
                            if (bitmap != null) {
                                drawLeibaoImg(bitmap, dto);
                            }
                        }else {
                            removeLeibaoOverlay();
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * ?????????????????????
     */
    private void startDownloadRadarImgs() {
        cancelRadarThread();
        radarManager.loadImagesAsyn(radarDataMap, new RadarManager.RadarListener() {
            @Override
            public void onResult(int result, final Map<String, StrongStreamDto> radars) {
                if (result == RadarManager.RadarListener.RESULT_SUCCESSED) {
                    drawFirstRadarImg();
                }
            }

            @Override
            public void onProgress(String url, int progress) {
            }
        });
    }

    /**
     * ????????????????????????
     */
    private void drawFirstRadarImg() {
        if (!isShowRadar) {
            return;
        }
        for (String startTime : radarDataMap.keySet()) {
            if (!TextUtils.isEmpty(startTime) && radarDataMap.containsKey(startTime)) {
                StrongStreamDto dto = radarDataMap.get(startTime);
                if (!TextUtils.isEmpty(dto.imgPath)) {
                    Bitmap bitmap = BitmapFactory.decodeFile(dto.imgPath);
                    if (bitmap != null) {
                        drawRadarImg(bitmap);
                        break;
                    }
                }
            }
        }
    }

    /**
     * ???????????????
     * @param bitmap
     */
    private void drawRadarImg(final Bitmap bitmap) {
        if (bitmap == null || !isShowRadar) {
            removeRadarOverlay();
            return;
        }
        BitmapDescriptor fromView = BitmapDescriptorFactory.fromBitmap(bitmap);
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(new LatLng(53.56, 73.44))
                .include(new LatLng(10.15, 135.09))
                .build();

        if (radarOverlay == null) {
            radarOverlay = aMap.addGroundOverlay(new GroundOverlayOptions()
                    .anchor(0.5f, 0.5f)
                    .positionFromBounds(bounds)
                    .image(fromView)
                    .zIndex(1001)
                    .transparency(0.25f));
        } else {
            radarOverlay.setImage(null);
            radarOverlay.setPositionFromBounds(bounds);
            radarOverlay.setImage(fromView);
        }
        aMap.runOnDrawFrame();
    }

    /**
     * ???????????????
     */
    private void removeRadarOverlay() {
        if (radarOverlay != null) {
            radarOverlay.remove();
            radarOverlay = null;
        }
    }

    /**
     * ??????????????????
     */
    private void cancelRadarThread() {
        if (radarThread != null) {
            radarThread.cancel();
            radarThread = null;
        }
    }

    /**
     * ?????????????????????
     */
    private void removeScwLayer() {
        try {
            for (String startTime : scwWindPolylineMap.keySet()) {
                if (scwWindPolylineMap.containsKey(startTime)) {
                    List<Polyline> polylines = scwWindPolylineMap.get(startTime);
                    if (polylines != null) {
                        for (Polyline polyline : polylines) {
                            if (polyline != null) {
                                polyline.remove();
                            }
                        }
                    }
                }
            }
            scwWindPolylineMap.clear();

            for (String startTime : scwRainPolylineMap.keySet()) {
                if (scwRainPolylineMap.containsKey(startTime)) {
                    List<Polyline> polylines = scwRainPolylineMap.get(startTime);
                    if (polylines != null) {
                        for (Polyline polyline : polylines) {
                            if (polyline != null) {
                                polyline.remove();
                            }
                        }
                    }
                }
            }
            scwRainPolylineMap.clear();

            for (String startTime : scwHailPolylineMap.keySet()) {
                if (scwHailPolylineMap.containsKey(startTime)) {
                    List<Polyline> polylines = scwHailPolylineMap.get(startTime);
                    if (polylines != null) {
                        for (Polyline polyline : polylines) {
                            if (polyline != null) {
                                polyline.remove();
                            }
                        }
                    }
                }
            }
            scwHailPolylineMap.clear();
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }

    /**
     * ?????????????????????
     * @param startTime
     */
    private void drawScwLayer(final String startTime) {
        if (TextUtils.isEmpty(startTime)) {
            return;
        }
        removeScwLayer();
        //???????????????
        if (scwWindDataMap.containsKey(startTime)) {
            List<List<StrongStreamDto>> list = scwWindDataMap.get(startTime);
            List<Polyline> polylines = new ArrayList<>();
            for (List<StrongStreamDto> itemList : list) {
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(0xffdf9a44);
                polylineOptions.width(8);
                for (StrongStreamDto dto : itemList) {
                    polylineOptions.add(new LatLng(dto.lat, dto.lng));
                }
                Polyline polyline = aMap.addPolyline(polylineOptions);
                polylines.add(polyline);
            }
            scwWindPolylineMap.put(startTime, polylines);
        }

        //??????????????????
        if (scwRainDataMap.containsKey(startTime)) {
            List<List<StrongStreamDto>> list = scwRainDataMap.get(startTime);
            List<Polyline> polylines = new ArrayList<>();
            for (List<StrongStreamDto> itemList : list) {
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(0xff6cdede);
                polylineOptions.width(8);
                for (StrongStreamDto dto : itemList) {
                    polylineOptions.add(new LatLng(dto.lat, dto.lng));
                }
                Polyline polyline = aMap.addPolyline(polylineOptions);
                polylines.add(polyline);
            }
            scwRainPolylineMap.put(startTime, polylines);
        }

        //??????????????????
        if (scwHailDataMap.containsKey(startTime)) {
            List<List<StrongStreamDto>> list = scwHailDataMap.get(startTime);
            List<Polyline> polylines = new ArrayList<>();
            for (List<StrongStreamDto> itemList : list) {
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(0xffdd3de6);
                polylineOptions.width(8);
                for (StrongStreamDto dto : itemList) {
                    polylineOptions.add(new LatLng(dto.lat, dto.lng));
                }
                Polyline polyline = aMap.addPolyline(polylineOptions);
                polylines.add(polyline);
            }
            scwHailPolylineMap.put(startTime, polylines);
        }
    }

    /**
     * ?????????????????????????????????marker
     */
    private void removeThunderMarkers() {
        try {
            for (Marker marker : thunderMarkers) {
                if (marker != null) {
                    marker.remove();
                }
            }
            thunderMarkers.clear();
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????????????????markers
     */
    private void showOrHideThunderMarkers() {
        for (Marker marker : thunderMarkers) {
            if (marker != null) {
                marker.setVisible(isShowThunderMarkers);
            }
        }
    }

    /**
     * ????????????markers
     * @param startTime
     */
    private void drawThunderMarkers(final String startTime) {
        if (TextUtils.isEmpty(startTime) || !isShowThunderMarkers) {
            return;
        }
        removeThunderMarkers();
        if (thunderDataMap.containsKey(startTime)) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Map<String, StrongStreamDto> map = thunderDataMap.get(startTime);
            for (Map.Entry<String, StrongStreamDto> entry : map.entrySet()) {
                StrongStreamDto dto = entry.getValue();
                MarkerOptions options = new MarkerOptions();
                options.title(dto.num+"?????????");
                options.anchor(1.0f, 0.0f);
                options.position(new LatLng(dto.lat, dto.lng));
                View view = inflater.inflate(R.layout.shawn_thunder_marker, null);
                ImageView ivMarker = view.findViewById(R.id.ivMarker);
                if (TextUtils.equals(dto.type, "0")) {
                    ivMarker.setImageResource(R.drawable.shawn_icon_thunder_diji);
                }else if (TextUtils.equals(dto.type, "1")){
                    ivMarker.setImageResource(R.drawable.shawn_icon_thunder_luji);
                }
                options.icon(BitmapDescriptorFactory.fromView(view));
                Marker marker = aMap.addMarker(options);
                marker.setVisible(true);
                thunderMarkers.add(marker);
                expandMarker(marker);
            }
        }
    }

    /**
     * ????????????
     * @param marker
     */
    private void expandMarker(Marker marker) {
        Animation animation = new ScaleAnimation(0,1,0,1);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(300);
        marker.setAnimation(animation);
        marker.startAnimation();
    }

    /**
     * ????????????
     * @param marker
     */
    private void colloseMarker(Marker marker) {
        Animation animation = new ScaleAnimation(1,0,1,0);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(300);
        marker.setAnimation(animation);
        marker.startAnimation();
    }

    /**
     * ????????????????????????
     */
    private void OkHttpCloud() {
        showDialog();
        final String url = "http://decision-admin.tianqi.cn/Home/other/light_geth8cloud_imgs";
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
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                cloudDataMap.clear();
                                JSONObject obj = new JSONObject(result);
                                LatLng leftLatLng = null,rightLatLng = null;
                                if (!obj.isNull("rect")) {
                                    JSONArray rect = obj.getJSONArray("rect");
                                    leftLatLng = new LatLng(rect.getDouble(2), rect.getDouble(1));
                                    rightLatLng = new LatLng(rect.getDouble(0), rect.getDouble(3));
                                }
                                if (!obj.isNull("l")) {
                                    JSONArray array = obj.getJSONArray("l");
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject itemObj = array.getJSONObject(i);
                                        final StrongStreamDto dto = new StrongStreamDto();
                                        dto.leftLatLng = leftLatLng;
                                        dto.rightLatLng = rightLatLng;
                                        if (!itemObj.isNull("l1")) {
                                            try {
                                                dto.startTime = sdf3.format(sdf2.parse(itemObj.getString("l1")));
                                                if (!itemObj.isNull("l2")) {
                                                    dto.imgUrl = itemObj.getString("l2");
                                                    cloudDataMap.put(dto.startTime, dto);
                                                }
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    if (cloudDataMap.size() > 0) {
                                        startDownloadCloudImgs();
                                    }

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cancelDialog();
                            }
                        });
                    }
                });
            }
        }).start();
    }

    /**
     * ??????????????????
     */
    private void startDownloadCloudImgs() {
        cloudManager.loadImagesAsyn(cloudDataMap, new CloudManager.CloudListener() {
            @Override
            public void onResult(int result, final Map<String, StrongStreamDto> clouds) {
                if (result == CloudManager.CloudListener.RESULT_SUCCESSED) {
                    drawFirstCloudImg();
                }
            }

            @Override
            public void onProgress(String url, int progress) {
            }
        });
    }

    /**
     * ?????????????????????
     */
    private void drawFirstCloudImg() {
        if (!isShowCloud) {
            return;
        }
        for (String startTime : cloudDataMap.keySet()) {
            if (!TextUtils.isEmpty(startTime) && cloudDataMap.containsKey(startTime)) {
                StrongStreamDto dto = cloudDataMap.get(startTime);
                if (!TextUtils.isEmpty(dto.imgPath)) {
                    Bitmap bitmap = BitmapFactory.decodeFile(dto.imgPath);
                    if (bitmap != null) {
                        drawCloudImg(bitmap, dto);
                        break;
                    }
                }
            }
        }
    }

    /**
     * ??????h8??????
     * @param bitmap
     */
    private void drawCloudImg(final Bitmap bitmap, final StrongStreamDto dto) {
        if (bitmap == null || !isShowCloud) {
            removeCloudOverlay();
            return;
        }
        BitmapDescriptor fromView = BitmapDescriptorFactory.fromBitmap(bitmap);
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(dto.leftLatLng)
                .include(dto.rightLatLng)
                .build();

        if (cloudOverlay == null) {
            cloudOverlay = aMap.addGroundOverlay(new GroundOverlayOptions()
                    .anchor(0.5f, 0.5f)
                    .positionFromBounds(bounds)
                    .image(fromView)
                    .zIndex(1000)
                    .transparency(0.25f));
        } else {
            cloudOverlay.setImage(null);
            cloudOverlay.setPositionFromBounds(bounds);
            cloudOverlay.setImage(fromView);
        }
        aMap.runOnDrawFrame();
    }

    /**
     * ????????????
     */
    private void removeCloudOverlay() {
        if (cloudOverlay != null) {
            cloudOverlay.remove();
            cloudOverlay = null;
        }
    }

    /**
     * ????????????
     */
    private void narrowAnimation(final View view) {
        android.view.animation.ScaleAnimation animation = new android.view.animation.ScaleAnimation(
                1.0f, 0.0f, 1.0f, 0.0f, android.view.animation.Animation.RELATIVE_TO_SELF, 1.0f, android.view.animation.Animation.RELATIVE_TO_SELF, 1.0f
        );
        animation.setDuration(200);
        animation.setFillAfter(true);
        view.startAnimation(animation);
        animation.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override
            public void onAnimationStart(android.view.animation.Animation animation) {
            }
            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                view.clearAnimation();
            }
            @Override
            public void onAnimationRepeat(android.view.animation.Animation animation) {
            }
        });
    }

    /**
     * ????????????
     */
    private void enlargeAnimation(final View view) {
        android.view.animation.ScaleAnimation animation = new android.view.animation.ScaleAnimation(
                0.0f, 1.0f, 0.0f, 1.0f, android.view.animation.Animation.RELATIVE_TO_SELF, 1.0f, android.view.animation.Animation.RELATIVE_TO_SELF, 1.0f
        );
        animation.setDuration(200);
        animation.setFillAfter(true);
        view.startAnimation(animation);
        animation.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override
            public void onAnimationStart(android.view.animation.Animation animation) {
            }
            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                view.clearAnimation();
            }
            @Override
            public void onAnimationRepeat(android.view.animation.Animation animation) {
            }
        });
    }

    /**
     * ?????????????????????
     */
    private void OkHttpRain() {
        showDialog();
        final String url = "http://decision-admin.tianqi.cn/Home/other/light_getrain_imgs";
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
                        String result = response.body().string();
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                rainDataMap.clear();
                                JSONObject obj = new JSONObject(result);
                                LatLng leftLatLng = null,rightLatLng = null;
                                if (!obj.isNull("rect")) {
                                    JSONArray rect = obj.getJSONArray("rect");
                                    leftLatLng = new LatLng(rect.getDouble(2), rect.getDouble(1));
                                    rightLatLng = new LatLng(rect.getDouble(0), rect.getDouble(3));
                                }
                                if (!obj.isNull("l")) {
                                    JSONArray array = obj.getJSONArray("l");
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject itemObj = array.getJSONObject(i);
                                        StrongStreamDto dto = new StrongStreamDto();
                                        dto.leftLatLng = leftLatLng;
                                        dto.rightLatLng = rightLatLng;
                                        if (!itemObj.isNull("l1")) {
                                            try {
                                                dto.startTime = sdf3.format(sdf2.parse(itemObj.getString("l1")));
                                                if (!itemObj.isNull("l2")) {
                                                    dto.imgUrl = itemObj.getString("l2");
                                                    rainDataMap.put(dto.startTime, dto);
                                                }
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    if (rainDataMap.size() > 0) {
                                        startDownloadRainImgs();
                                    }

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cancelDialog();
                            }
                        });
                    }
                });
            }
        }).start();
    }

    /**
     * ?????????????????????
     */
    private void startDownloadRainImgs() {
        rainManager.loadImagesAsyn(rainDataMap, new RainManager.RainListener() {
            @Override
            public void onResult(int result, final Map<String, StrongStreamDto> map) {
                //?????????????????????????????????
                if (result == RainManager.RainListener.RESULT_SUCCESSED) {
                    drawFirstRainImg();
                }
            }

            @Override
            public void onProgress(String url, int progress) {
            }
        });
    }

    /**
     * ????????????????????????
     */
    private void drawFirstRainImg() {
        if (!isShowRain) {
            return;
        }
        for (String startTime : rainDataMap.keySet()) {
            if (!TextUtils.isEmpty(startTime) && rainDataMap.containsKey(startTime)) {
                StrongStreamDto dto = rainDataMap.get(startTime);
                if (!TextUtils.isEmpty(dto.imgPath)) {
                    Bitmap bitmap = BitmapFactory.decodeFile(dto.imgPath);
                    if (bitmap != null) {
                        drawRainImg(bitmap, dto);
                        break;
                    }
                }
            }
        }
    }

    /**
     * ???????????????
     * @param bitmap
     */
    private void drawRainImg(final Bitmap bitmap, final StrongStreamDto dto) {
        if (bitmap == null || !isShowRain) {
            removeRainOverlay();
            return;
        }
        BitmapDescriptor fromView = BitmapDescriptorFactory.fromBitmap(bitmap);
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(dto.leftLatLng)
                .include(dto.rightLatLng)
                .build();

        if (rainOverlay == null) {
            rainOverlay = aMap.addGroundOverlay(new GroundOverlayOptions()
                    .anchor(0.5f, 0.5f)
                    .positionFromBounds(bounds)
                    .image(fromView)
                    .zIndex(1002)
                    .transparency(0.25f));
        } else {
            rainOverlay.setImage(null);
            rainOverlay.setPositionFromBounds(bounds);
            rainOverlay.setImage(fromView);
        }
        aMap.runOnDrawFrame();
    }

    /**
     * ???????????????
     */
    private void removeRainOverlay() {
        if (rainOverlay != null) {
            rainOverlay.remove();
            rainOverlay = null;
        }
    }

    /**
     * ???????????????????????????
     */
    private void OkHttpLeibao() {
        showDialog();
        final String url = "http://decision-admin.tianqi.cn/Home/other/light_getlbcloud_imgs";
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
                        String result = response.body().string();
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                leibaoDataMap.clear();
                                JSONObject obj = new JSONObject(result);
                                LatLng leftLatLng = null,rightLatLng = null;
                                if (!obj.isNull("rect")) {
                                    JSONArray rect = obj.getJSONArray("rect");
                                    leftLatLng = new LatLng(rect.getDouble(2), rect.getDouble(1));
                                    rightLatLng = new LatLng(rect.getDouble(0), rect.getDouble(3));
                                }
                                if (!obj.isNull("l")) {
                                    JSONArray array = obj.getJSONArray("l");
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject itemObj = array.getJSONObject(i);
                                        StrongStreamDto dto = new StrongStreamDto();
                                        dto.leftLatLng = leftLatLng;
                                        dto.rightLatLng = rightLatLng;
                                        if (!itemObj.isNull("l1")) {
                                            try {
                                                dto.startTime = sdf3.format(sdf2.parse(itemObj.getString("l1")));
                                                if (!itemObj.isNull("l2")) {
                                                    dto.imgUrl = itemObj.getString("l2");
                                                    leibaoDataMap.put(dto.startTime, dto);
                                                }
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    if (leibaoDataMap.size() > 0) {
                                        startDownloadLeibaoImgs();
                                    }

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cancelDialog();
                            }
                        });
                    }
                });
            }
        }).start();
    }

    /**
     * ????????????????????????
     */
    private void startDownloadLeibaoImgs() {
        leibaoManager.loadImagesAsyn(leibaoDataMap, new LeibaoManager.LeibaoListener() {
            @Override
            public void onResult(int result, final Map<String, StrongStreamDto> map) {
                if (result == LeibaoManager.LeibaoListener.RESULT_SUCCESSED) {
                    drawFirstLeibaoImg();
                }
            }

            @Override
            public void onProgress(String url, int progress) {
            }
        });
    }

    /**
     * ???????????????????????????
     */
    private void drawFirstLeibaoImg() {
        if (!isShowLeibao) {
            return;
        }
        for (String startTime : leibaoDataMap.keySet()) {
            if (!TextUtils.isEmpty(startTime) && leibaoDataMap.containsKey(startTime)) {
                StrongStreamDto dto = leibaoDataMap.get(startTime);
                if (!TextUtils.isEmpty(dto.imgPath)) {
                    Bitmap bitmap = BitmapFactory.decodeFile(dto.imgPath);
                    if (bitmap != null) {
                        drawLeibaoImg(bitmap, dto);
                        break;
                    }
                }
            }
        }
    }

    /**
     * ??????????????????
     * @param bitmap
     */
    private void drawLeibaoImg(final Bitmap bitmap, final StrongStreamDto dto) {
        if (bitmap == null || !isShowLeibao) {
            removeLeibaoOverlay();
            return;
        }
        BitmapDescriptor fromView = BitmapDescriptorFactory.fromBitmap(bitmap);
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(dto.leftLatLng)
                .include(dto.rightLatLng)
                .build();

        if (leibaoOverlay == null) {
            leibaoOverlay = aMap.addGroundOverlay(new GroundOverlayOptions()
                    .anchor(0.5f, 0.5f)
                    .positionFromBounds(bounds)
                    .image(fromView)
                    .zIndex(1000)
                    .transparency(0.25f));
        } else {
            leibaoOverlay.setImage(null);
            leibaoOverlay.setPositionFromBounds(bounds);
            leibaoOverlay.setImage(fromView);
        }
        aMap.runOnDrawFrame();
    }

    /**
     * ??????????????????
     */
    private void removeLeibaoOverlay() {
        if (leibaoOverlay != null) {
            leibaoOverlay.remove();
            leibaoOverlay = null;
        }
    }

    /**
     * ???????????????????????????
     */
    private void OkHttpYunding() {
        showDialog();
        final String url = "https://decision-admin.tianqi.cn/home/other/light_geth8cloud_height_imgs";
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
                        String result = response.body().string();
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                yundingDataMap.clear();
                                JSONObject obj = new JSONObject(result);
                                LatLng leftLatLng = null,rightLatLng = null;
                                if (!obj.isNull("rect")) {
                                    JSONArray rect = obj.getJSONArray("rect");
                                    leftLatLng = new LatLng(rect.getDouble(2), rect.getDouble(1));
                                    rightLatLng = new LatLng(rect.getDouble(0), rect.getDouble(3));
                                }
                                if (!obj.isNull("l")) {
                                    JSONArray array = obj.getJSONArray("l");
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject itemObj = array.getJSONObject(i);
                                        StrongStreamDto dto = new StrongStreamDto();
                                        dto.leftLatLng = leftLatLng;
                                        dto.rightLatLng = rightLatLng;
                                        if (!itemObj.isNull("l1")) {
                                            try {
                                                dto.startTime = sdf3.format(sdf2.parse(itemObj.getString("l1")));
                                                if (!itemObj.isNull("l2")) {
                                                    dto.imgUrl = itemObj.getString("l2");
                                                    yundingDataMap.put(dto.startTime, dto);
                                                }
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    if (yundingDataMap.size() > 0) {
                                        startDownloadYundingImgs();
                                    }

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cancelDialog();
                            }
                        });
                    }
                });
            }
        }).start();
    }

    /**
     * ???????????????????????????
     */
    private void startDownloadYundingImgs() {
        yundingManager.loadImagesAsyn(yundingDataMap, new YdgdManager.YdgdListener() {
            @Override
            public void onResult(int result, final Map<String, StrongStreamDto> map) {
                if (result == YdgdManager.YdgdListener.RESULT_SUCCESSED) {
                    drawFirstYundingImg();
                }
            }

            @Override
            public void onProgress(String url, int progress) {
            }
        });
    }

    /**
     * ??????????????????????????????
     */
    private void drawFirstYundingImg() {
        if (!isShowYunding) {
            return;
        }
        for (String startTime : yundingDataMap.keySet()) {
            if (!TextUtils.isEmpty(startTime) && yundingDataMap.containsKey(startTime)) {
                StrongStreamDto dto = yundingDataMap.get(startTime);
                if (!TextUtils.isEmpty(dto.imgPath)) {
                    Bitmap bitmap = BitmapFactory.decodeFile(dto.imgPath);
                    if (bitmap != null) {
                        drawYundingImg(bitmap, dto);
                        break;
                    }
                }
            }
        }
    }

    /**
     * ?????????????????????
     * @param bitmap
     */
    private void drawYundingImg(final Bitmap bitmap, final StrongStreamDto dto) {
        if (bitmap == null || !isShowYunding) {
            removeYundingOverlay();
            return;
        }
        BitmapDescriptor fromView = BitmapDescriptorFactory.fromBitmap(bitmap);
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(dto.leftLatLng)
                .include(dto.rightLatLng)
                .build();

        if (yundingOverlay == null) {
            yundingOverlay = aMap.addGroundOverlay(new GroundOverlayOptions()
                    .anchor(0.5f, 0.5f)
                    .positionFromBounds(bounds)
                    .image(fromView)
                    .zIndex(1000)
                    .transparency(0.25f));
        } else {
            yundingOverlay.setImage(null);
            yundingOverlay.setPositionFromBounds(bounds);
            yundingOverlay.setImage(fromView);
        }
        aMap.runOnDrawFrame();
    }

    /**
     * ?????????????????????
     */
    private void removeYundingOverlay() {
        if (yundingOverlay != null) {
            yundingOverlay.remove();
            yundingOverlay = null;
        }
    }

    /**
     * ???????????????????????????????????????
     * @param type ???????????????Petrol?????????Scenic?????????Park
     */
    private void OkHttpPetrolScenicPark(final String type) {
        showDialog();
        final String url = "http://lightning.app.tianqi.cn/lightning/lhdata/ldzt?type="+type;
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
                        String result = response.body().string();
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                if (TextUtils.equals(type, Petrol)) {
                                    petrolDataMap.clear();
                                }
                                if (TextUtils.equals(type, Scenic)) {
                                    scenicList.clear();
                                }
                                if (TextUtils.equals(type, Park)) {
                                    parkList.clear();
                                }
                                JSONArray array = new JSONArray(result);
                                for (int i = 0; i < array.length(); i++) {
                                    StrongStreamDto dto = new StrongStreamDto();
                                    JSONObject itemObj = array.getJSONObject(i);
                                    if (!itemObj.isNull("lat")) {
                                        dto.lat = itemObj.getDouble("lat");
                                    }
                                    if (!itemObj.isNull("lon")) {
                                        dto.lng = itemObj.getDouble("lon");
                                    }
                                    if (!itemObj.isNull("city")) {
                                        dto.pro = itemObj.getString("city");
                                    }
                                    if (!itemObj.isNull("county")) {
                                        dto.city = itemObj.getString("county");
                                    }
                                    if (!itemObj.isNull("province")) {
                                        dto.stationName = itemObj.getString("province");
                                    }

                                    if (TextUtils.equals(type, Petrol)) {
                                        if (petrolDataMap.containsKey(dto.pro)) {
                                            List<StrongStreamDto> list = petrolDataMap.get(dto.pro);
                                            list.add(dto);
                                        }else {
                                            List<StrongStreamDto> list = new ArrayList<>();
                                            list.add(dto);
                                            petrolDataMap.put(dto.pro, list);
                                        }
                                    }
                                    if (TextUtils.equals(type, Scenic)) {
                                        scenicList.add(dto);
                                    }
                                    if (TextUtils.equals(type, Park)) {
                                        parkList.add(dto);
                                    }
                                }

                                removeStationMarkers(type);
                                drawStationMarkers(type);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        cancelDialog();
                                    }
                                });

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * ???????????????????????????marker
     */
    private void removeProStationMarkers() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Marker marker : proStationMarkers) {
                    marker.remove();
                }
                proStationMarkers.clear();
            }
        }).start();
    }

    /**
     * ????????????markers
     * @param type
     */
    private void removeStationMarkers(final String type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.equals(type, Petrol)) {
                    for (Marker marker : petrolMarkers) {
                        marker.remove();
                    }
                    petrolMarkers.clear();
                }

                if (TextUtils.equals(type, Scenic)) {
                    for (Marker marker : scenicMarkers) {
                        marker.remove();
                    }
                    scenicMarkers.clear();
                }

                if (TextUtils.equals(type, Park)) {
                    for (Marker marker : parkMarkers) {
                        marker.remove();
                    }
                    parkMarkers.clear();
                }
            }
        }).start();
    }

    /**
     * ????????????markers
     * @param type
     */
    private void drawStationMarkers(final String type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (TextUtils.equals(type, Petrol)) {
                    LatLngBounds.Builder builder = LatLngBounds.builder();
                    for (String pro : proStationMap.keySet()) {
                        StrongStreamDto dto = proStationMap.get(pro);
                        MarkerOptions options = new MarkerOptions();
                        options.title(dto.pro);
                        options.snippet(Petrol);
                        options.anchor(0.5f, 0.5f);
                        LatLng latLng = new LatLng(dto.lat, dto.lng);
                        options.position(latLng);
                        builder.include(latLng);
                        View view = inflater.inflate(R.layout.shawn_marker_icon_pro, null);
                        TextView tvMarker = view.findViewById(R.id.tvMarker);
                        tvMarker.setBackgroundResource(R.drawable.shawn_marker_bg_petrol);
                        if (petrolDataMap.containsKey(dto.pro)) {
                            List<StrongStreamDto> list = petrolDataMap.get(dto.pro);
                            int size = list.size();
                            if (size > 0) {
                                tvMarker.setText(size+"");
                                options.icon(BitmapDescriptorFactory.fromView(view));
                                Marker marker = aMap.addMarker(options);
                                proStationMarkers.add(marker);
                            }
                        }
                    }
                    aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
                }else if (TextUtils.equals(type, Scenic)) {
                    LatLngBounds.Builder builder = LatLngBounds.builder();
                    for (StrongStreamDto dto : scenicList) {
                        MarkerOptions options = new MarkerOptions();
                        options.title(dto.stationName+"|"+dto.pro+dto.city);
                        options.anchor(0.5f, 0.5f);
                        options.snippet(Scenic);
                        LatLng latLng = new LatLng(dto.lat, dto.lng);
                        options.position(latLng);
                        builder.include(latLng);
                        View view = inflater.inflate(R.layout.shawn_marker_icon, null);
                        ImageView ivMarker = view.findViewById(R.id.ivMarker);
                        ivMarker.setImageResource(R.drawable.shawn_marker_scenic);
                        options.icon(BitmapDescriptorFactory.fromView(view));
                        Marker marker = aMap.addMarker(options);
                        scenicMarkers.add(marker);
                    }
                    aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
                }else if (TextUtils.equals(type, Park)) {
                    LatLngBounds.Builder builder = LatLngBounds.builder();
                    for (StrongStreamDto dto : parkList) {
                        MarkerOptions options = new MarkerOptions();
                        options.title(dto.stationName+"|"+dto.pro+dto.city);
                        options.snippet(Park);
                        options.anchor(0.5f, 0.5f);
                        LatLng latLng = new LatLng(dto.lat, dto.lng);
                        options.position(latLng);
                        builder.include(latLng);
                        View view = inflater.inflate(R.layout.shawn_marker_icon, null);
                        ImageView ivMarker = view.findViewById(R.id.ivMarker);
                        ivMarker.setImageResource(R.drawable.shawn_marker_park);
                        options.icon(BitmapDescriptorFactory.fromView(view));
                        Marker marker = aMap.addMarker(options);
                        parkMarkers.add(marker);
                    }
                    aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
                }
            }
        }).start();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        locationLatLng = latLng;
        addLocationMarker();
        searchAddrByLatLng(latLng.latitude, latLng.longitude);
        OkHttpThunderForecast(latLng.longitude, latLng.latitude);
        OkHttpGeo(latLng.longitude, latLng.latitude);
    }

    /**
     * ???????????????????????????????????????
     * @param lat
     * @param lng
     */
    private void searchAddrByLatLng(double lat, double lng) {
        //latLonPoint??????????????????Latlng???????????????????????????????????????GeocodeSearch.AMAP?????????????????????????????????GPS???????????????
        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(lat, lng), 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
    }

    @Override
    public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
    }
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getRegeocodeAddress() != null && result.getRegeocodeAddress().getFormatAddress() != null) {
                tvPosition.setText(result.getRegeocodeAddress().getCity()+" | "+result.getRegeocodeAddress().getDistrict());
                tvStreet.setText(result.getRegeocodeAddress().getFormatAddress());
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker != null && marker != locationMarker) {
            if (TextUtils.equals(marker.getSnippet(), Petrol)) {//???????????????
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (!TextUtils.isEmpty(marker.getTitle()) && petrolDataMap.containsKey(marker.getTitle())) {
                    removeProStationMarkers();
                    LatLngBounds.Builder builder = LatLngBounds.builder();
                    List<StrongStreamDto> list = petrolDataMap.get(marker.getTitle());
                    for (StrongStreamDto dto : list) {
                        MarkerOptions options = new MarkerOptions();
                        options.title(dto.stationName + "|" + dto.pro + dto.city);
                        options.anchor(0.5f, 0.5f);
                        LatLng latLng = new LatLng(dto.lat, dto.lng);
                        options.position(latLng);
                        builder.include(latLng);
                        View view = inflater.inflate(R.layout.shawn_marker_icon, null);
                        ImageView ivMarker = view.findViewById(R.id.ivMarker);
                        ivMarker.setImageResource(R.drawable.shawn_marker_petrol);
                        options.icon(BitmapDescriptorFactory.fromView(view));
                        Marker m = aMap.addMarker(options);
                        petrolMarkers.add(m);
                    }
                    aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
                }
            }else {
                if (marker.isInfoWindowShown()) {
                    marker.hideInfoWindow();
                }else {
                    marker.showInfoWindow();
                }
            }
        }
        return true;
    }

    @Override
    public View getInfoContents(Marker marker) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.shawn_marker_info, null);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvInfo = view.findViewById(R.id.tvInfo);
        if (!TextUtils.isEmpty(marker.getTitle())) {
            if (marker.getTitle().contains("|")) {
                String[] title = marker.getTitle().split("\\|");
                if (!TextUtils.isEmpty(title[0])) {
                    tvName.setText(title[0]);
                }
                if (!TextUtils.isEmpty(title[1])) {
                    tvInfo.setText(title[1]);
                    tvInfo.setVisibility(View.VISIBLE);
                }
            }else {
                tvName.setText(marker.getTitle());
                tvInfo.setVisibility(View.GONE);
            }
        }
        return view;
    }

    @Override
    public View getInfoWindow(Marker arg0) {
        return null;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        zoom = cameraPosition.zoom;
        if (isShowPetrol) {//????????????????????????
            if (zoom <= 4.0f) {
                removeStationMarkers(Petrol);
                drawStationMarkers(Petrol);
            }
        }
    }

    /**
     * ????????????
     * @param dataType ??????1?????????2???????????????3???????????????4???????????????5???????????????6
     */
    private void dialogNetwork(final int dataType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.shawn_dialog_cache, null);
        TextView tvContent = view.findViewById(R.id.tvContent);
        TextView tvNegtive = view.findViewById(R.id.tvNegtive);
        TextView tvPositive = view.findViewById(R.id.tvPositive);

        final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
        dialog.setContentView(view);
        dialog.show();

        tvContent.setText("????????????wifi?????????????????????");
        tvNegtive.setText("??????");
        tvPositive.setText("??????");
        tvNegtive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });

        tvPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                if (dataType == 1) {

                }else if (dataType == 2) {
                    OkHttpRain();
                }else if (dataType == 3) {

                }else if (dataType == 4) {
                    OkHttpYunding();
                }else if (dataType == 5) {
                    OkHttpCloud();
                }else if (dataType == 6) {
                    OkHttpLeibao();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.ivLocation:
                mapLoaded();
                break;
            case R.id.ivLg:
                if (ivLegend.getVisibility() == View.VISIBLE) {
                    ivLegend.setVisibility(View.GONE);
                }else {
                    ivLegend.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.llThunder:
                isShowThunderMarkers = !isShowThunderMarkers;
                showOrHideThunderMarkers();
                break;
            case R.id.ivPlay:
                if (radarThread != null) {
                    if (radarThread.getCurrentState() == RadarThread.STATE_NONE) {
                        radarThread.start();
                        ivPlay.setImageResource(R.drawable.icon_pause);
                    }else if (radarThread.getCurrentState() == RadarThread.STATE_PLAYING) {
                        radarThread.pause();
                        ivPlay.setImageResource(R.drawable.icon_play);
                    }else if (radarThread.getCurrentState() == RadarThread.STATE_PAUSE) {
                        radarThread.play();
                        ivPlay.setImageResource(R.drawable.icon_pause);
                    }
                }
                break;
            case R.id.ll1:
            case R.id.ivMore1:
                isShowRadar = !isShowRadar;
                if (isShowRadar) {
                    iv1.setImageResource(R.drawable.shawn_icon_radaron);
                    tv1.setTextColor(Color.WHITE);
                    ll1.setBackgroundResource(R.drawable.shawn_bg_corner_top_blue);
                    ivMore1.setImageResource(R.drawable.shawn_icon_more_radaron);
                    ivLegend.setImageDrawable(getResources().getDrawable(R.drawable.shawn_legend_radar));
                    drawFirstRadarImg();
                }else {
                    iv1.setImageResource(R.drawable.shawn_icon_radar);
                    tv1.setTextColor(getResources().getColor(R.color.text_color3));
                    ll1.setBackgroundColor(Color.TRANSPARENT);
                    ivMore1.setImageResource(R.drawable.shawn_icon_more_radar);
                    ivLegend.setImageDrawable(null);
                    removeRadarOverlay();
                }
                break;
            case R.id.ll2:
            case R.id.ivMore2:
                Toast.makeText(mContext, "????????????????????????????????????", Toast.LENGTH_SHORT).show();
//                isShowRain = !isShowRain;
//                if (isShowRain) {
//                    iv2.setImageResource(R.drawable.shawn_icon_rainon);
//                    tv2.setTextColor(Color.WHITE);
//                    ll2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
//                    ivMore2.setImageResource(R.drawable.shawn_icon_more_rainon);
//                    ivLegend.setImageDrawable(getResources().getDrawable(R.drawable.shawn_legend_rain));
//                    if (rainDataMap.size() <= 0) {
//                        if (CommonUtil.getConnectedType(mContext) == 1) {
//                            OkHttpRain();
//                        }else {
//                            dialogNetwork(2);
//                        }
//                    }else {
//                        drawFirstRainImg();
//                    }
//                }else {
//                    iv2.setImageResource(R.drawable.shawn_icon_rain);
//                    tv2.setTextColor(getResources().getColor(R.color.text_color3));
//                    ll2.setBackgroundColor(Color.TRANSPARENT);
//                    ivMore2.setImageResource(R.drawable.shawn_icon_more_rain);
//                    ivLegend.setImageDrawable(null);
//                    removeRainOverlay();
//                }
                break;
            case R.id.llMore:
                isShowMore = !isShowMore;
                if (isShowMore) {
                    ivMore.setImageResource(R.drawable.shawn_icon_moreon);
                    tvMore.setTextColor(Color.WHITE);
                    llMore.setBackgroundResource(R.drawable.shawn_bg_corner_bottom_blue);
                    enlargeAnimation(reMore);
                    reMore.setVisibility(View.VISIBLE);
                } else {
                    ivMore.setImageResource(R.drawable.shawn_icon_more);
                    tvMore.setTextColor(getResources().getColor(R.color.text_color3));
                    llMore.setBackgroundColor(Color.TRANSPARENT);
                    narrowAnimation(reMore);
                    reMore.setVisibility(View.GONE);
                }
                break;
            case R.id.ivMore3:
//                isShowVisible = !isShowVisible;
//                if (isShowVisible) {
//                    ivMore3.setImageResource(R.drawable.shawn_icon_more_visibleon);
//                }else {
//                    ivMore3.setImageResource(R.drawable.shawn_icon_more_visible);
//                }
                Toast.makeText(mContext, "????????????????????????????????????", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ivMore4:
                isShowYunding = !isShowYunding;
                if (isShowYunding) {
                    ivMore4.setImageResource(R.drawable.shawn_icon_more_ydgdon);
                    ivLegend.setImageDrawable(getResources().getDrawable(R.drawable.shawn_legend_yunding));
                    if (yundingDataMap.size() <= 0) {
                        if (CommonUtil.getConnectedType(mContext) == 1) {
                            OkHttpYunding();
                        }else {
                            dialogNetwork(4);
                        }
                    }else {
                        drawFirstYundingImg();
                    }
                }else {
                    ivMore4.setImageResource(R.drawable.shawn_icon_more_ydgd);
                    ivLegend.setImageDrawable(null);
                    removeYundingOverlay();
                }
                break;
            case R.id.ivMore5:
                isShowCloud = !isShowCloud;
                if (isShowCloud) {
                    ivMore5.setImageResource(R.drawable.shawn_icon_more_ydddon);
                    ivLegend.setImageDrawable(getResources().getDrawable(R.drawable.shawn_legend_yunding));
                    if (cloudDataMap.size() <= 0) {
                        if (CommonUtil.getConnectedType(mContext) == 1) {
                            OkHttpCloud();
                        }else {
                            dialogNetwork(5);
                        }
                    }else {
                        drawFirstCloudImg();
                    }
                }else {
                    ivMore5.setImageResource(R.drawable.shawn_icon_more_yddd);
                    ivLegend.setImageDrawable(null);
                    removeCloudOverlay();
                }
                break;
            case R.id.ivMore6:
                isShowLeibao = !isShowLeibao;
                if (isShowLeibao) {
                    ivMore6.setImageResource(R.drawable.shawn_icon_more_lbon);
                    if (leibaoDataMap.size() <= 0) {
                        if (CommonUtil.getConnectedType(mContext) == 1) {
                            OkHttpLeibao();
                        }else {
                            dialogNetwork(6);
                        }
                    }else {
                        drawFirstLeibaoImg();
                    }
                }else {
                    ivMore6.setImageResource(R.drawable.shawn_icon_more_lb);
                    removeLeibaoOverlay();
                }
                break;


                //??????
            case R.id.ivSetting:
                isShowSetting = !isShowSetting;
                if (isShowSetting) {
                    ivSetting.setImageResource(R.drawable.shawn_icon_settingon);
                    enlargeAnimation(llSetting);
                    llSetting.setVisibility(View.VISIBLE);
                } else {
                    ivSetting.setImageResource(R.drawable.shawn_icon_setting);
                    narrowAnimation(llSetting);
                    llSetting.setVisibility(View.GONE);
                }
                break;
            case R.id.ivMapType1:
                if (AMapType == AMap.MAP_TYPE_SATELLITE) {
                    return;
                } else {
                    ivMapType1.setBackgroundResource(R.drawable.shawn_bg_corner_map_press);
                    ivMapType2.setBackgroundColor(getResources().getColor(R.color.transparent));
                    AMapType = AMap.MAP_TYPE_SATELLITE;
                    aMap.setMapType(AMapType);
                }
                break;
            case R.id.ivMapType2:
                if (AMapType == AMap.MAP_TYPE_NORMAL) {
                    return;
                } else {
                    ivMapType1.setBackgroundColor(getResources().getColor(R.color.transparent));
                    ivMapType2.setBackgroundResource(R.drawable.shawn_bg_corner_map_press);
                    AMapType = AMap.MAP_TYPE_NORMAL;
                    aMap.setMapType(AMapType);
                }
                break;
            case R.id.ivPetrol:
                if (proStationMap.size() <= 0) {
                    //???????????????????????????????????????
                    String[] petrolStations = getResources().getStringArray(R.array.petrol_stations);
                    for (String str : petrolStations) {
                        String[] value = str.split(",");
                        StrongStreamDto dto = new StrongStreamDto();
                        dto.pro = value[0];
                        dto.lat = Double.parseDouble(value[1]);
                        dto.lng = Double.parseDouble(value[2]);
                        proStationMap.put(dto.pro, dto);
                    }
                }

                isShowPetrol = !isShowPetrol;
                if (isShowPetrol) {
                    ivPetrol.setBackgroundResource(R.drawable.shawn_bg_corner_map_press);
                    if (petrolDataMap.size() <= 0) {
                        OkHttpPetrolScenicPark(Petrol);
                    }else {
                        drawStationMarkers(Petrol);
                    }
                }else {
                    ivPetrol.setBackgroundColor(getResources().getColor(R.color.transparent));
                    removeProStationMarkers();
                    removeStationMarkers(Petrol);
                }
                break;
            case R.id.ivScenic:
                isShowScenic = !isShowScenic;
                if (isShowScenic) {
                    ivScenic.setBackgroundResource(R.drawable.shawn_bg_corner_map_press);
                    if (scenicList.size() <= 0) {
                        OkHttpPetrolScenicPark(Scenic);
                    }else {
                        drawStationMarkers(Scenic);
                    }
                }else {
                    ivScenic.setBackgroundColor(getResources().getColor(R.color.transparent));
                    removeStationMarkers(Scenic);
                }
                break;
            case R.id.ivPark:
                isShowPark = !isShowPark;
                if (isShowPark) {
                    ivPark.setBackgroundResource(R.drawable.shawn_bg_corner_map_press);
                    if (parkList.size() <= 0) {
                        OkHttpPetrolScenicPark(Park);
                    }else {
                        drawStationMarkers(Park);
                    }
                }else {
                    ivPark.setBackgroundColor(getResources().getColor(R.color.transparent));
                    removeStationMarkers(Park);
                }
                break;

        }
    }

    /**
     * ??????????????????
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }

        startTimer();
    }

    /**
     * ??????????????????
     */
    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
        resetTimer();
    }

    /**
     * ??????????????????
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    /**
     * ??????????????????
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }

        cancelRadarThread();
        removeRadarOverlay();
        removeCloudOverlay();
        removeYundingOverlay();

        resetTimer();
    }

    private Timer timer;
    private void startTimer() {
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapLoaded();
                        }
                    });
                }
            }, 0, 1000*60*3);
        }
    }

    /**
     * ???????????????
     */
    private void resetTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

}
