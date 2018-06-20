package com.hlj.activity;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.GroundOverlay;
import com.amap.api.maps.model.LatLng;
import com.hlj.common.CONST;
import com.hlj.dto.WindData;
import com.hlj.dto.WindDto;
import com.hlj.manager.RainManager;
import com.hlj.utils.OkHttpUtil;
import com.hlj.view.WaitWindView2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

public class WaitWindActivity extends BaseActivity implements OnClickListener, OnCameraChangeListener {

    private Context mContext = null;
    private LinearLayout llBack = null;
    private TextView tvTitle = null;
    private RelativeLayout reShare = null;
    private MapView mapView = null;
    private AMap aMap = null;
    private float zoom = 3.7f;
    private TextView tvFileTime = null;
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHH");
    private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy年MM月dd日HH时");
    private RelativeLayout container = null;
    public RelativeLayout container2 = null;
    private int width = 0, height = 0;
    private WaitWindView2 waitWindView = null;
    private GroundOverlay windOverlay = null;
    private ImageView ivSwitch = null;
    private boolean isGfs = true;//默认为风场新数据
    private WindData windData2 = null;//gfs
    private WindData windData1 = null;//t639

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_wind);
        mContext = this;
        showDialog();
        initWidget();
        initAmap(savedInstanceState);
    }

    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        reShare = (RelativeLayout) findViewById(R.id.reShare);
        tvFileTime = (TextView) findViewById(R.id.tvFileTime);
        container = (RelativeLayout) findViewById(R.id.container);
        container2 = (RelativeLayout) findViewById(R.id.container2);
        ivSwitch = (ImageView) findViewById(R.id.ivSwitch);
        ivSwitch.setOnClickListener(this);

        String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
        if (title != null) {
            tvTitle.setText(title);
        }

    }

    private void initAmap(Bundle bundle) {
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(bundle);
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), zoom));
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setRotateGesturesEnabled(false);
        aMap.setOnCameraChangeListener(this);
        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                width = mapView.getWidth();
                height = mapView.getHeight();

                asyncGFS();
            }
        });
    }

    private void asyncGFS() {
        if (windData2 != null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = getWindGFS("http://scapi.weather.com.cn/weather/getwindmincas", "1000");
                OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String requestResult = response.body().string();
                            if (requestResult != null) {
                                try {
                                    JSONObject obj = new JSONObject(requestResult);
                                    if (windData2 == null) {
                                        windData2 = new WindData();
                                    }
                                    if (obj != null) {
                                        if (!obj.isNull("gridHeight")) {
                                            windData2.height = obj.getInt("gridHeight");
                                        }
                                        if (!obj.isNull("gridWidth")) {
                                            windData2.width = obj.getInt("gridWidth");
                                        }
                                        if (!obj.isNull("x0")) {
                                            windData2.x0 = obj.getDouble("x0");
                                        }
                                        if (!obj.isNull("y0")) {
                                            windData2.y0 = obj.getDouble("y0");
                                        }
                                        if (!obj.isNull("x1")) {
                                            windData2.x1 = obj.getDouble("x1");
                                        }
                                        if (!obj.isNull("y1")) {
                                            windData2.y1 = obj.getDouble("y1");
                                        }
                                        if (!obj.isNull("filetime")) {
                                            windData2.filetime = obj.getString("filetime");
                                        }

                                        if (!obj.isNull("field")) {
                                            windData2.dataList.clear();
                                            JSONArray array = new JSONArray(obj.getString("field"));
                                            for (int i = 0; i < array.length(); i += 2) {
                                                WindDto dto2 = new WindDto();
                                                dto2.initX = (float) (array.optDouble(i));
                                                dto2.initY = (float) (array.optDouble(i + 1));
                                                windData2.dataList.add(dto2);
                                            }
                                        }

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                cancelDialog();
                                                reloadWind(true);
                                            }
                                        });

                                    }
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
        }).start();
    }

    private void asyncT639() {
        if (windData1 != null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = getWindT639("http://scapi.weather.com.cn/weather/micaps/windfile", "1000", "0");
                OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String requestResult = response.body().string();
                            if (requestResult != null) {
                                try {
                                    JSONObject obj = new JSONObject(requestResult);
                                    if (windData1 == null) {
                                        windData1 = new WindData();
                                    }
                                    if (obj != null) {
                                        if (!obj.isNull("gridHeight")) {
                                            windData1.height = obj.getInt("gridHeight");
                                        }
                                        if (!obj.isNull("gridWidth")) {
                                            windData1.width = obj.getInt("gridWidth");
                                        }
                                        if (!obj.isNull("x0")) {
                                            windData1.x0 = obj.getDouble("x0");
                                        }
                                        if (!obj.isNull("y0")) {
                                            windData1.y0 = obj.getDouble("y0");
                                        }
                                        if (!obj.isNull("x1")) {
                                            windData1.x1 = obj.getDouble("x1");
                                        }
                                        if (!obj.isNull("y1")) {
                                            windData1.y1 = obj.getDouble("y1");
                                        }
                                        if (!obj.isNull("filetime")) {
                                            windData1.filetime = obj.getString("filetime");
                                        }

                                        if (!obj.isNull("field")) {
                                            windData1.dataList.clear();
                                            JSONArray array = new JSONArray(obj.getString("field"));
                                            for (int i = 0; i < array.length(); i += 2) {
                                                WindDto dto2 = new WindDto();
                                                dto2.initX = (float) (array.optDouble(i));
                                                dto2.initY = (float) (array.optDouble(i + 1));
                                                windData1.dataList.add(dto2);
                                            }
                                        }

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                cancelDialog();
                                                reloadWind(false);
                                            }
                                        });

                                    }
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
        }).start();
    }

    private String getWindT639(String url, String type, String index) {
        String sysdate = RainManager.getDate(Calendar.getInstance(), "yyyyMMddHH");//系统时间
        StringBuffer buffer = new StringBuffer();
        buffer.append(url);
        buffer.append("?");
        buffer.append("type=").append(type);
        buffer.append("&");
        buffer.append("index=").append(index);
        buffer.append("&");
        buffer.append("date=").append(sysdate);
        buffer.append("&");
        buffer.append("appid=").append(RainManager.APPID);

        String key = RainManager.getKey(RainManager.CHINAWEATHER_DATA, buffer.toString());
        buffer.delete(buffer.lastIndexOf("&"), buffer.length());

        buffer.append("&");
        buffer.append("appid=").append(RainManager.APPID.substring(0, 6));
        buffer.append("&");
        buffer.append("key=").append(key.substring(0, key.length() - 3));
        String result = buffer.toString();
        return result;
    }

    private String getWindGFS(String url, String type) {
        String sysdate = RainManager.getDate(Calendar.getInstance(), "yyyyMMddHH");//系统时间
        StringBuffer buffer = new StringBuffer();
        buffer.append(url);
        buffer.append("?");
        buffer.append("type=").append(type);
        buffer.append("&");
        buffer.append("date=").append(sysdate);
        buffer.append("&");
        buffer.append("appid=").append(RainManager.APPID);

        String key = RainManager.getKey(RainManager.CHINAWEATHER_DATA, buffer.toString());
        buffer.delete(buffer.lastIndexOf("&"), buffer.length());

        buffer.append("&");
        buffer.append("appid=").append(RainManager.APPID.substring(0, 6));
        buffer.append("&");
        buffer.append("key=").append(key.substring(0, key.length() - 3));
        String result = buffer.toString();
        return result;
    }

    @Override
    public void onCameraChange(CameraPosition arg0) {
        container.removeAllViews();
        container2.removeAllViews();
        tvFileTime.setVisibility(View.GONE);
    }

    @Override
    public void onCameraChangeFinish(CameraPosition arg0) {
        if (isGfs) {
            reloadWind(true);
        }else {
            reloadWind(false);
        }
    }

    long t = new Date().getTime();

    /**
     * 重新加载风场
     */
    private void reloadWind(boolean isGfs) {
        t = new Date().getTime() - t;
        if (t < 1000) {
            return;
        }

        LatLng latLngStart = aMap.getProjection().fromScreenLocation(new Point(0, 0));
        LatLng latLngEnd = aMap.getProjection().fromScreenLocation(new Point(width, height));
        if (isGfs) {
            windData2.latLngStart = latLngStart;
            windData2.latLngEnd = latLngEnd;
        }else {
            windData1.latLngStart = latLngStart;
            windData1.latLngEnd = latLngEnd;
        }
        if (waitWindView == null) {
            waitWindView = new WaitWindView2(mContext);
            waitWindView.init(WaitWindActivity.this);
            if (isGfs) {
                waitWindView.setData(windData2);
            }else {
                waitWindView.setData(windData1);
            }
            waitWindView.start();
            waitWindView.invalidate();
        }else {
            if (isGfs) {
                waitWindView.setData(windData2);
            }else {
                waitWindView.setData(windData1);
            }
        }

        container2.removeAllViews();
        container.removeAllViews();
        container.addView(waitWindView);
        tvFileTime.setVisibility(View.VISIBLE);
        String time = "";
        if (isGfs) {
            time = windData2.filetime;
            if (!TextUtils.isEmpty(time)) {
                try {
                    tvFileTime.setText("GFS "+sdf3.format(sdf2.parse(time)) + "风场预报");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }else {
            time = windData1.filetime;
            if (!TextUtils.isEmpty(time)) {
                try {
                    tvFileTime.setText("T639 "+sdf3.format(sdf2.parse(time)) + "风场预报");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.ivSwitch:
                if (isGfs) {
                    ivSwitch.setImageResource(R.drawable.gfs);
                    if (windData1 == null) {
                        asyncT639();
                    }else {
                        reloadWind(false);
                    }
                    isGfs = false;
                }else {
                    ivSwitch.setImageResource(R.drawable.t639);
                    if (windData2 == null) {
                        asyncGFS();
                    }else {
                        reloadWind(true);
                    }
                    isGfs = true;
                }
                break;

            default:
                break;
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

}
