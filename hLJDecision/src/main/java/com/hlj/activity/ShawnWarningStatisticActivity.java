package com.hlj.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlj.adapter.ShawnWarningStatisticGroupAdapter;
import com.hlj.dto.WarningDto;
import com.hlj.utils.OkHttpUtil;

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
 * 预警统计
 */
public class ShawnWarningStatisticActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;
    private TextView tvTitle, tvTime;
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
    private SimpleDateFormat sdf3 = new SimpleDateFormat("MM月dd日", Locale.CHINA);
    private SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy0101000000", Locale.CHINA);
    private SimpleDateFormat sdf5 = new SimpleDateFormat("yyyyMMdd000000", Locale.CHINA);
    private SimpleDateFormat sdf6 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
    private ShawnWarningStatisticGroupAdapter mAdapter;
    private List<WarningDto> groupList = new ArrayList<>();
    private List<List<WarningDto>> childList = new ArrayList<>();
    private String areaName = "全国", areaId, startTime, endTime;
    private String baseUrl = "http://testdecision.tianqi.cn/alarm12379/hisalarmcount.php?format=1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shawn_activity_warning_statistic);
        mContext = this;
        initWidget();
        initListView();
    }

    private void initWidget() {
        LinearLayout llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(areaName + "预警统计");
        TextView tvControl = findViewById(R.id.tvControl);
        tvControl.setOnClickListener(this);
        tvControl.setText("筛选");
        tvControl.setVisibility(View.VISIBLE);
        tvTime = findViewById(R.id.tvTime);

        if (getIntent().hasExtra("startTime")) {
            startTime = getIntent().getStringExtra("startTime");
        } else {
            startTime = sdf4.format(new Date());
        }
        if (getIntent().hasExtra("endTime")) {
            endTime = getIntent().getStringExtra("endTime");
        } else {
            endTime = sdf5.format(new Date());
        }
        try {
            tvTime.setText(sdf2.format(sdf6.parse(startTime)) + " - " + sdf2.format(sdf6.parse(endTime)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String url = baseUrl;
        if (getIntent().hasExtra("data")) {
            WarningDto data = getIntent().getExtras().getParcelable("data");
            if (data != null) {
                areaName = data.areaName;
                tvTitle.setText(areaName + "预警统计");
                if (!TextUtils.isEmpty(data.areaKey)) {
                    areaId = data.areaKey;
                    url = String.format(baseUrl + "&areaid=%s&starttime=%s&endtime=%s", data.areaKey, startTime, endTime);
                }
            }
        } else {
            areaName = "黑龙江";
            tvTitle.setText(areaName + "预警统计");
            areaId = "23";
            url = String.format(baseUrl + "&areaid=%s&starttime=%s&endtime=%s", areaId, startTime, endTime);
        }
        OkHttpStatistic(url);
    }

    private void initListView() {
        ExpandableListView listView = findViewById(R.id.listView);
        mAdapter = new ShawnWarningStatisticGroupAdapter(mContext, groupList, childList, listView);
        mAdapter.setStartTime(startTime);
        mAdapter.setEndTime(endTime);
        listView.setAdapter(mAdapter);
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                WarningDto dto = groupList.get(groupPosition);
                if (TextUtils.equals(dto.areaKey, "all")) {//总计不能点击
                    return true;
                }
                Intent intent = new Intent(mContext, ShawnWarningStatisticListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("data", dto);
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            }
        });
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                WarningDto dto = childList.get(groupPosition).get(childPosition);
                if (TextUtils.equals(dto.areaKey, "all")) {//总计不能点击
                    return true;
                }
                Intent intent = new Intent(mContext, ShawnWarningStatisticListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("data", dto);
                intent.putExtras(bundle);
                startActivity(intent);
                return false;
            }
        });

    }

    /**
     * 获取预警统计信息
     *
     * @param url
     */
    private void OkHttpStatistic(final String url) {
        showDialog();
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Log.e("statisticurl:", url);
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
                                        JSONObject object = new JSONObject(result);
                                        if (!object.isNull("data")) {
                                            groupList.clear();
                                            childList.clear();
                                            JSONArray array = object.getJSONArray("data");
                                            for (int i = 0; i < array.length(); i++) {
                                                WarningDto dto = new WarningDto();
                                                JSONObject obj = array.getJSONObject(i);
                                                if (!obj.isNull("name")) {
                                                    dto.areaName = obj.getString("name");
                                                }
                                                if (!obj.isNull("areaid")) {
                                                    dto.areaId = obj.getString("areaid");
                                                }
                                                if (!obj.isNull("areaKey")) {
                                                    dto.areaKey = obj.getString("areaKey");
                                                }
                                                if (!obj.isNull("count")) {
                                                    String[] count = obj.getString("count").split("\\|");
                                                    dto.warningCount = count[0];
                                                    dto.redCount = count[1];
                                                    dto.orangeCount = count[2];
                                                    dto.yellowCount = count[3];
                                                    dto.blueCount = count[4];
                                                }

                                                if (!obj.isNull("list")) {
                                                    List<WarningDto> list = new ArrayList<>();
                                                    list.clear();
                                                    JSONArray listArray = obj.getJSONArray("list");
                                                    for (int j = 0; j < listArray.length(); j++) {
                                                        JSONObject itemObj = listArray.getJSONObject(j);
                                                        WarningDto d = new WarningDto();
                                                        d.areaKey = dto.areaKey;
                                                        if (!itemObj.isNull("name")) {
                                                            d.shortName = itemObj.getString("name");
                                                            d.areaName = dto.areaName + d.shortName;
                                                        }
                                                        if (!itemObj.isNull("type")) {
                                                            d.type = itemObj.getString("type");
                                                        }
                                                        if (!itemObj.isNull("count")) {
                                                            String[] count = itemObj.getString("count").split("\\|");
                                                            d.warningCount = count[0];
                                                            d.redCount = count[1];
                                                            d.orangeCount = count[2];
                                                            d.yellowCount = count[3];
                                                            d.blueCount = count[4];
                                                        }
                                                        list.add(d);
                                                    }
                                                    childList.add(list);
                                                }

                                                groupList.add(dto);
                                            }
                                        }

                                        if (mAdapter != null) {
                                            mAdapter.notifyDataSetChanged();
                                        }
                                        cancelDialog();

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.tvControl:
                Intent intent = new Intent(mContext, WarningHistoryScreenActivity.class);
                intent.putExtra("startTime", startTime);
                intent.putExtra("endTime", endTime);
                intent.putExtra("areaName", areaName);
                intent.putExtra("areaId", areaId);
                startActivityForResult(intent, 1000);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1000:
                    areaId = data.getExtras().getString("areaId");
                    areaName = data.getExtras().getString("areaName");
                    startTime = data.getExtras().getString("startTime");
                    endTime = data.getExtras().getString("endTime");
                    mAdapter.setStartTime(startTime);
                    mAdapter.setEndTime(endTime);
                    tvTitle.setText(areaName + "预警统计");
                    try {
                        tvTime.setText(sdf2.format(sdf6.parse(startTime)) + " - " + sdf2.format(sdf6.parse(endTime)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String url;
                    if (!TextUtils.isEmpty(areaId)) {
                        url = String.format(baseUrl + "&areaid=%s&starttime=%s&endtime=%s", areaId, startTime, endTime);
                    } else {
                        url = String.format(baseUrl + "&starttime=%s&endtime=%s", startTime, endTime);
                    }
                    OkHttpStatistic(url);
                    break;
            }
        }
    }
}
