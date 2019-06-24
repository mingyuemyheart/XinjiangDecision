package com.hlj.activity;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.hlj.dto.AgriDto;
import com.hlj.utils.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

/**
 * 天气图分析
 */

public class HWeatherChartAnalysisActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext = null;
    private LinearLayout llBack = null;
    private TextView tvTitle = null;
    private MapView mMapView = null;
    private AMap aMap = null;
    private float zoom = 3.7f;
    private List<Polyline> polyline1 = new ArrayList<>();
    private List<Text> textList1 = new ArrayList<>();
    private List<Polyline> polyline2 = new ArrayList<>();
    private List<Text> textList2 = new ArrayList<>();
    private ImageView ivSwitch = null;
    private boolean isShowSwitch = true;
    private LinearLayout llSwitch = null;
    private int swithWidth = 0;
    private TextView tv1, tv2, tv3;
    private String baseUrl = "https://scapi.tianqi.cn/weather/xstu?test=ncg&type=1&hm=";
    private int type = 1;
    private String result1, result2, result3;
    private ImageView ivChart, ivLegend;
    private TextView tvTime = null;
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hactivity_weather_chart_analysis);
        mContext = this;
        initMap(savedInstanceState);
        initWidget();
    }

    /**
     * 初始化地图
     */
    private void initMap(Bundle bundle) {
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(bundle);
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), zoom));
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setRotateGesturesEnabled(false);

        TextView tvMapNumber = findViewById(R.id.tvMapNumber);
        tvMapNumber.setText(aMap.getMapContentApprovalNumber());

        refresh();
    }

    private void refresh() {
        showDialog();
        String url = "";
        if (type == 1) {
            url = baseUrl+"h000";
        }else if (type == 2) {
            url = baseUrl+"h850";
        }else if (type == 3) {
            url = baseUrl+"h500";
        }
        OkHttpData(url);
    }

    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        ivChart = (ImageView) findViewById(R.id.ivChart);
        ivChart.setOnClickListener(this);
        ivLegend = (ImageView) findViewById(R.id.ivLegend);
        ivSwitch = (ImageView) findViewById(R.id.ivSwitch);
        ivSwitch.setOnClickListener(this);
        llSwitch = (LinearLayout) findViewById(R.id.llSwitch);
        tv1 = (TextView) findViewById(R.id.tv1);
        tv1.setOnClickListener(this);
        tv2 = (TextView) findViewById(R.id.tv2);
        tv2.setOnClickListener(this);
        tv3 = (TextView) findViewById(R.id.tv3);
        tv3.setOnClickListener(this);
        tvTime = (TextView) findViewById(R.id.tvTime);

        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        llSwitch.measure(w, h);
        swithWidth = llSwitch.getMeasuredWidth();

        AgriDto data = getIntent().getExtras().getParcelable("data");
        if (data != null) {
            if (data.name != null) {
                tvTitle.setText(data.name);
            }
        }
    }

    private void OkHttpData(String url) {
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
                if (type == 1) {
                    result1 = result;
                }else if (type == 2) {
                    result2 = result;
                }else if (type == 3) {
                    result3 = result;
                }
                parseData(result);
            }
        });
    }

    private void parseData(String result) {
        aMap.clear();
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject obj = new JSONObject(result);
                if (!obj.isNull("list")) {
                    JSONArray array = obj.getJSONArray("list");
                    String dataUrl = array.getString(0);
                    OkHttpUtil.enqueue(new Request.Builder().url(dataUrl).build(), new Callback() {
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

                                            if (!obj.isNull("mtime")) {
                                                long mTime = obj.getLong("mtime");
                                                tvTime.setText(sdf1.format(new Date(mTime))+"更新");
                                                tvTime.setVisibility(View.VISIBLE);
                                            }

                                            if (!obj.isNull("lines")) {
                                                JSONArray lines = obj.getJSONArray("lines");
                                                for (int i = 0; i < lines.length(); i++) {
                                                    JSONObject itemObj = lines.getJSONObject(i);
                                                    if (!itemObj.isNull("point")) {
                                                        JSONArray points = itemObj.getJSONArray("point");
                                                        PolylineOptions polylineOption = new PolylineOptions();
                                                        polylineOption.width(6).color(0xff406bbf);
                                                        for (int j = 0; j < points.length(); j++) {
                                                            JSONObject point = points.getJSONObject(j);
                                                            double lat = point.getDouble("y");
                                                            double lng = point.getDouble("x");
                                                            polylineOption.add(new LatLng(lat, lng));
                                                        }
                                                        Polyline p = aMap.addPolyline(polylineOption);
                                                        polyline1.add(p);
                                                    }
                                                    if (!itemObj.isNull("flags")) {
                                                        JSONObject flags = itemObj.getJSONObject("flags");
                                                        String text = "";
                                                        if (!flags.isNull("text")) {
                                                            text = flags.getString("text");
                                                        }
                                                        if (!flags.isNull("items")) {
                                                            JSONArray items = flags.getJSONArray("items");
                                                            JSONObject item = items.getJSONObject(0);
                                                            double lat = item.getDouble("y");
                                                            double lng = item.getDouble("x");
                                                            TextOptions to = new TextOptions();
                                                            to.position(new LatLng(lat, lng));
                                                            to.text(text);
                                                            to.fontColor(Color.BLACK);
                                                            to.fontSize(30);
                                                            to.backgroundColor(Color.TRANSPARENT);
                                                            Text t = aMap.addText(to);
                                                            textList1.add(t);
                                                        }
                                                    }
                                                }
                                            }
                                            if (!obj.isNull("line_symbols")) {
                                                JSONArray line_symbols = obj.getJSONArray("line_symbols");
                                                for (int i = 0; i < line_symbols.length(); i++) {
                                                    JSONObject itemObj = line_symbols.getJSONObject(i);
                                                    if (!itemObj.isNull("items")) {
                                                        JSONArray items = itemObj.getJSONArray("items");
                                                        PolylineOptions polylineOption = new PolylineOptions();
                                                        polylineOption.width(6).color(0xff406bbf);
                                                        for (int j = 0; j < items.length(); j++) {
                                                            JSONObject item = items.getJSONObject(j);
                                                            double lat = item.getDouble("y");
                                                            double lng = item.getDouble("x");
                                                            polylineOption.add(new LatLng(lat, lng));
                                                        }
                                                        Polyline p = aMap.addPolyline(polylineOption);
                                                        polyline2.add(p);
                                                    }
                                                }
                                            }
                                            if (!obj.isNull("symbols")) {
                                                JSONArray symbols = obj.getJSONArray("symbols");
                                                for (int i = 0; i < symbols.length(); i++) {
                                                    JSONObject itemObj = symbols.getJSONObject(i);
                                                    String text = "";
                                                    int color = Color.BLACK;
                                                    if (!itemObj.isNull("type")) {
                                                        String type = itemObj.getString("type");
                                                        if (TextUtils.equals(type, "60")) {
                                                            text = "H";
                                                            color = Color.RED;
                                                        }else if (TextUtils.equals(type, "61")) {
                                                            text = "L";
                                                            color = Color.BLUE;
                                                        }else if (TextUtils.equals(type, "37")) {
                                                            text = "台";
                                                            color = Color.GREEN;
                                                        }
                                                    }
                                                    double lat = itemObj.getDouble("y");
                                                    double lng = itemObj.getDouble("x");
                                                    TextOptions to = new TextOptions();
                                                    to.position(new LatLng(lat, lng));
                                                    to.text(text);
                                                    to.fontColor(color);
                                                    to.fontSize(60);
                                                    to.backgroundColor(Color.TRANSPARENT);
                                                    Text t = aMap.addText(to);
                                                    textList2.add(t);
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    cancelDialog();
                                }
                            });

                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 隐藏或显示的动画
     */
    public void switchAnimation(final View view, final int startValue,final int endValue){
        //1.设置属性的初始值和结束值
        ValueAnimator mAnimator = ValueAnimator.ofInt(0,100);
        //2.为目标对象的属性变化设置监听器
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatorValue = (Integer) animation.getAnimatedValue();
                float fraction = animatorValue/100f;
                IntEvaluator mEvaluator = new IntEvaluator();
                //3.使用IntEvaluator计算属性值并赋值给ListView的高
                view.getLayoutParams().width = mEvaluator.evaluate(fraction, startValue, endValue);
                view.requestLayout();
            }
        });
        //4.为ValueAnimator设置LinearInterpolator
        mAnimator.setInterpolator(new LinearInterpolator());
        //5.设置动画的持续时间
        mAnimator.setDuration(200);
        //6.为ValueAnimator设置目标对象并开始执行动画
        mAnimator.setTarget(view);
        mAnimator.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.ivSwitch:
                if (isShowSwitch) {
                    isShowSwitch = false;
                    switchAnimation(llSwitch, swithWidth, 0);
                }else {
                    isShowSwitch = true;
                    switchAnimation(llSwitch, 0, swithWidth);
                }
                break;
            case R.id.tv1:
                type = 1;
                tv1.setTextColor(getResources().getColor(R.color.title_bg));
                tv2.setTextColor(getResources().getColor(R.color.text_color3));
                tv3.setTextColor(getResources().getColor(R.color.text_color3));

                if (!TextUtils.isEmpty(result1)) {
                    parseData(result1);
                }else {
                    refresh();
                }
                break;
            case R.id.tv2:
                type = 2;
                tv1.setTextColor(getResources().getColor(R.color.text_color3));
                tv2.setTextColor(getResources().getColor(R.color.title_bg));
                tv3.setTextColor(getResources().getColor(R.color.text_color3));

                if (!TextUtils.isEmpty(result2)) {
                    parseData(result2);
                }else {
                    refresh();
                }
                break;
            case R.id.tv3:
                type = 3;
                tv1.setTextColor(getResources().getColor(R.color.text_color3));
                tv2.setTextColor(getResources().getColor(R.color.text_color3));
                tv3.setTextColor(getResources().getColor(R.color.title_bg));

                if (!TextUtils.isEmpty(result3)) {
                    parseData(result3);
                }else {
                    refresh();
                }
                break;
            case R.id.ivChart:
                if (ivLegend.getVisibility() == View.VISIBLE) {
                    ivLegend.setVisibility(View.INVISIBLE);
                }else {
                    ivLegend.setVisibility(View.VISIBLE);
                }
                break;
        }
    }
}
