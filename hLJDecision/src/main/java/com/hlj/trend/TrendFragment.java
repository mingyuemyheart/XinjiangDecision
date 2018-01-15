package com.hlj.trend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.hlj.activity.TrendDetailActivity;
import com.hlj.common.CONST;
import com.hlj.dto.TrendDto;
import com.hlj.stickygridheaders.StickyGridHeadersGridView;

import shawn.cxwl.com.hlj.R;

public class TrendFragment extends Fragment{

	private StickyGridHeadersGridView mGridView = null;
	private TrendAdapter mAdapter = null;
	private List<TrendDto> mList = new ArrayList<TrendDto>();
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
	 * 初始化GridView
	 */
	private void initGridView(View view) {
		TrendDto dto = new TrendDto();
		dto.areaName = "津南区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "津南区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "津南区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "津南区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "津南区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "津南区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		
		dto = new TrendDto();
		dto.areaName = "西青区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "西青区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "西青区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "西青区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		
		dto = new TrendDto();
		dto.areaName = "宁河区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "宁河区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "宁河区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "宁河区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "宁河区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "宁河区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		
		dto = new TrendDto();
		dto.areaName = "东丽区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "东丽区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "东丽区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "东丽区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "东丽区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "东丽区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "东丽区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "东丽区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "东丽区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "东丽区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "东丽区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "东丽区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "东丽区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		dto = new TrendDto();
		dto.areaName = "东丽区";
		dto.streetName = "咸水沽镇";
		mList.add(dto);
		
		
		for (int i = 0; i < mList.size(); i++) {
			TrendDto sectionDto = mList.get(i);
			if (!sectionMap.containsKey(sectionDto.areaName)) {
				sectionDto.section = section;
				sectionMap.put(sectionDto.areaName, section);
				section++;
			}else {
				sectionDto.section = sectionMap.get(sectionDto.areaName);
			}
			mList.set(i, sectionDto);
		}
		
		mGridView = (StickyGridHeadersGridView) view.findViewById(R.id.stickyGridView);
		mAdapter = new TrendAdapter(getActivity(), mList);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent intent = new Intent(getActivity(), TrendDetailActivity.class);
				intent.putExtra(CONST.ACTIVITY_NAME, mList.get(arg2).streetName);
				startActivity(intent);
			}
		});
	}
}
