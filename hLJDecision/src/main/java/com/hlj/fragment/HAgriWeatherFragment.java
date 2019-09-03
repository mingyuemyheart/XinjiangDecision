package com.hlj.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.hlj.activity.HAgriWeatherDetailActivity;
import com.hlj.activity.HUrlActivity;
import com.hlj.activity.ShawnAgriActivity;
import com.hlj.adapter.HWeatherForecastFragmentAdapter;
import com.hlj.common.CONST;
import com.hlj.common.ColumnData;
import com.hlj.dto.AgriDto;
import com.hlj.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 农业气象
 * @author shawn_sun
 *
 */

public class HAgriWeatherFragment extends Fragment{
	
	private GridView gridView = null;
	private HWeatherForecastFragmentAdapter mAdapter = null;
	private List<AgriDto> mList = new ArrayList<>();
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.hfragment_agri_weather, null);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
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
		final ColumnData data = getArguments().getParcelable("data");
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
				if (TextUtils.equals(dto.id, "205")) {//农业气象服务
					Intent intentBuild = new Intent(getActivity(), ShawnAgriActivity.class);
					intentBuild.putExtra(CONST.ACTIVITY_NAME, dto.name);
					intentBuild.putExtra(CONST.WEB_URL, dto.dataUrl);
					intentBuild.putExtra(CONST.COLUMN_ID, dto.columnId);
					startActivity(intentBuild);
				}else {
					Intent intent = new Intent(getActivity(), HAgriWeatherDetailActivity.class);
					intent.putExtra(CONST.COLUMN_ID, dto.columnId);
					Bundle bundle = new Bundle();
					bundle.putParcelable("dto", dto);
					bundle.putParcelable("data", data);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			}
		});
	}

}
