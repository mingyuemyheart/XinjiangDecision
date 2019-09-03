package com.hlj.fragment;

/**
 * 天气预报、天气实况、电力气象服务、铁路气象服务
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.hlj.activity.FactActivity2;
import com.hlj.activity.HAirPolutionActivity;
import com.hlj.activity.HCommonPdfListActivity;
import com.hlj.activity.HMinuteFallActivity;
import com.hlj.activity.HPdfListActivity;
import com.hlj.activity.HProvinceForecastActivity;
import com.hlj.activity.HSixHourRainfallActivity;
import com.hlj.activity.HTempratureForecastActivity;
import com.hlj.activity.HUrlActivity;
import com.hlj.activity.HWeatherChartAnalysisActivity;
import com.hlj.activity.HWeatherRadarActivity;
import com.hlj.activity.HWeatherStaticsActivity;
import com.hlj.activity.ShawnFactMonitorActivity;
import com.hlj.activity.ShawnPointForeActivity;
import com.hlj.activity.ShawnProductActivity2;
import com.hlj.activity.ShawnStreamFactActivity;
import com.hlj.activity.ShawnStrongStreamActivity;
import com.hlj.activity.ShawnWaitWindActivity;
import com.hlj.activity.ShawnWeatherMeetingActivity;
import com.hlj.activity.TyphoonRouteActivity;
import com.hlj.adapter.HWeatherForecastFragmentAdapter;
import com.hlj.common.CONST;
import com.hlj.common.ColumnData;
import com.hlj.dto.AgriDto;
import com.hlj.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

import shawn.cxwl.com.hlj.R;

public class HWeatherForecastFragment extends Fragment {
	
	private GridView gridView = null;
	private HWeatherForecastFragmentAdapter mAdapter = null;
	private List<AgriDto> mList = new ArrayList<>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.hfragment_weather_forecast, null);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initGridView(view);

		String columnId = getArguments().getString(CONST.COLUMN_ID);
		String title = getArguments().getString(CONST.ACTIVITY_NAME);
		CommonUtil.submitClickCount(columnId, title);
	}
	
	/**
	 * 初始化listview
	 */
	private void initGridView(View view) {
		mList.clear();
		ColumnData data = getArguments().getParcelable("data");
		if (data != null) {
			for (int i = 0; i < data.child.size(); i++) {
				AgriDto dto = new AgriDto();
				dto.columnId = data.child.get(i).columnId;
				dto.id = data.child.get(i).id;
				dto.icon = data.child.get(i).icon;
				dto.icon2 = data.child.get(i).icon2;
				dto.showType = data.child.get(i).showType;
				dto.name = data.child.get(i).name;
				dto.dataUrl = data.child.get(i).dataUrl;
				dto.child = data.child.get(i).child;
				mList.add(dto);
			}
		}
		
		gridView = (GridView) view.findViewById(R.id.gridView);
		mAdapter = new HWeatherForecastFragmentAdapter(getActivity(), mList);
		gridView.setAdapter(mAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				AgriDto dto = mList.get(arg2);
				Intent intent;
				if (TextUtils.equals(dto.showType, CONST.URL)) {//三天降水量预报
					intent = new Intent(getActivity(), HUrlActivity.class);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
					intent.putExtra(CONST.WEB_URL, dto.dataUrl);
					intent.putExtra(CONST.COLUMN_ID, dto.columnId);
					startActivity(intent);
				}else if (TextUtils.equals(dto.showType, CONST.PRODUCT)) {
					intent = new Intent(getActivity(), ShawnProductActivity2.class);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
					intent.putExtra(CONST.WEB_URL, dto.dataUrl);
					intent.putExtra(CONST.COLUMN_ID, dto.columnId);
					Bundle bundle = new Bundle();
					bundle.putParcelable("data", dto);
					intent.putExtras(bundle);
					startActivity(intent);
				}else if (TextUtils.equals(dto.showType, CONST.LOCAL)) {//气温预报、雾霾预报、降温大风沙尘预报
					if (TextUtils.equals(dto.id, "111")) {//天气雷达
						intent = new Intent(getActivity(), HWeatherRadarActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						Bundle bundle = new Bundle();
						bundle.putParcelable("data", dto);
						intent.putExtras(bundle);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "106")) {//天气图分析
						intent = new Intent(getActivity(), HWeatherChartAnalysisActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						Bundle bundle = new Bundle();
						bundle.putParcelable("data", dto);
						intent.putExtras(bundle);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "108")) {//空气质量
						intent = new Intent(getActivity(), HAirPolutionActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "112")) {//天气统计
						intent = new Intent(getActivity(), HWeatherStaticsActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "117")) {//台风路径
						intent = new Intent(getActivity(), TyphoonRouteActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "120")) {//强对流天气实况（新）
						intent = new Intent(getActivity(), ShawnStreamFactActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "201")) {//气温预报
						intent = new Intent(getActivity(), HTempratureForecastActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						Bundle bundle = new Bundle();
						bundle.putParcelable("data", dto);
						intent.putExtras(bundle);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "203")) {//分钟级降水
						intent = new Intent(getActivity(), HMinuteFallActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "205")) {//等风来
						intent = new Intent(getActivity(), ShawnWaitWindActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "207")) {//格点预报
						intent = new Intent(getActivity(), ShawnPointForeActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "208")) {//分钟降水与强对流
						intent = new Intent(getActivity(), ShawnStrongStreamActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "701")) {//全省预报
						intent = new Intent(getActivity(), HProvinceForecastActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "1001")) {//电力气象预报、铁路气象服务子项目
						intent = new Intent(getActivity(), HCommonPdfListActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						Bundle bundle = new Bundle();
						bundle.putParcelable("data", dto);
						intent.putExtras(bundle);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "1002")) {//铁路气象服务（6小时降水量）
						intent = new Intent(getActivity(), HSixHourRainfallActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						Bundle bundle = new Bundle();
						bundle.putParcelable("data", dto);
						intent.putExtras(bundle);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "115") || TextUtils.equals(dto.id, "113") || TextUtils.equals(dto.id, "114") || TextUtils.equals(dto.id, "116")) {//115降水实况，113气温实况，114风向风速实况，116相对湿度分析
						intent = new Intent(getActivity(), FactActivity2.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						Bundle bundle = new Bundle();
						bundle.putParcelable("data", dto);
						intent.putExtras(bundle);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "118")) {//天气会商
						intent = new Intent(getActivity(), ShawnWeatherMeetingActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						startActivity(intent);
					}else if (TextUtils.equals(dto.id, "119")) {//自动站实况监测
						intent = new Intent(getActivity(), ShawnFactMonitorActivity.class);
						intent.putExtra(CONST.COLUMN_ID, dto.columnId);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}
				}else if (TextUtils.isEmpty(dto.showType) || TextUtils.equals(dto.showType, CONST.NEWS)) {//专业气象预报、中期旬报
					intent = new Intent(getActivity(), HPdfListActivity.class);
					intent.putExtra(CONST.COLUMN_ID, dto.columnId);
					Bundle bundle = new Bundle();
					bundle.putParcelable("data", dto);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}
		});
	}
	
}
