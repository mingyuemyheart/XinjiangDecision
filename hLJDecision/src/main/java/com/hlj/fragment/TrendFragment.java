package com.hlj.fragment;

/**
 * 天气实况
 */

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.hlj.stickygridheaders.StickyGridHeadersGridView;
import shawn.cxwl.com.hlj.decision.R;
import com.hlj.activity.TrendDetailActivity;
import com.hlj.adapter.HWeatherFactAdapter;
import com.hlj.dto.RangeDto;

public class TrendFragment extends Fragment implements OnClickListener{

	private StickyGridHeadersGridView mGridView = null;
	private HWeatherFactAdapter mAdapter = null;
	private List<RangeDto> mList = new ArrayList<RangeDto>();
	private int section = 1;
	private HashMap<String, Integer> sectionMap = new HashMap<String, Integer>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.trend_fragment, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initGridView(view);
	}
	
	/**
	 * 初始化initGridView
	 */
	private void initGridView(View view) {
		String[] stations = getResources().getStringArray(R.array.guizhou_stations);
		for (int i = 0; i < stations.length; i++) {
			String[] value = stations[i].split(",");
			RangeDto dto = new RangeDto();
			dto.cityId = value[0];
			dto.areaName = value[1];
			dto.cityName = value[2];
			mList.add(dto);
		}
		
		for (int i = 0; i < mList.size(); i++) {
			RangeDto sectionDto = mList.get(i);
			if (!sectionMap.containsKey(sectionDto.cityName)) {
				sectionDto.section = section;
				sectionMap.put(sectionDto.cityName, section);
				section++;
			}else {
				sectionDto.section = sectionMap.get(sectionDto.cityName);
			}
			mList.set(i, sectionDto);
		}
		
		mGridView = (StickyGridHeadersGridView) view.findViewById(R.id.stickyGridView);
		mAdapter = new HWeatherFactAdapter(getActivity(), mList);
		mGridView.setAdapter(mAdapter);
//		setGridViewHeightBasedOnChildren(mGridView);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				RangeDto dto = mList.get(arg2);
				Intent intent = new Intent(getActivity(), TrendDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
	
	/**
	 * 解决ScrollView与GridView共存的问题
	 * 
	 * @param listView
	 */
	private void setGridViewHeightBasedOnChildren(GridView gridView) {
		ListAdapter listAdapter = gridView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		
		Class<GridView> tempGridView = GridView.class; // 获得gridview这个类的class
		int column = -1;
        try {
 
            Field field = tempGridView.getDeclaredField("mRequestedNumColumns"); // 获得申明的字段
            field.setAccessible(true); // 设置访问权限
            column = Integer.valueOf(field.get(gridView).toString()); // 获取字段的值
        } catch (Exception e1) {
        }

		int totalHeight = 0;
		int itemHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i+=column) {
			View listItem = listAdapter.getView(i, null, gridView);
			listItem.measure(0, 0); 
			totalHeight += listItem.getMeasuredHeight();
			itemHeight = listItem.getMeasuredHeight();
		}
		
		totalHeight += (sectionMap.size()*(itemHeight-50));

		ViewGroup.LayoutParams params = gridView.getLayoutParams();
		params.height = totalHeight + (gridView.getVerticalSpacing() * (listAdapter.getCount()/column - 1) + 30);
		((MarginLayoutParams) params).setMargins(15, 15, 15, 0);
		gridView.setLayoutParams(params);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		default:
			break;
		}
	}
	
}
