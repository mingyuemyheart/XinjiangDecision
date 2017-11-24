package com.hlj.fragment;

/**
 * 实况趋势图
 */

import android.app.Fragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlj.dto.FactDto;
import com.hlj.view.FactRainView;
import com.hlj.view.FactTempView;
import com.hlj.view.FactWindView;

import java.util.ArrayList;
import java.util.List;

import shawn.cxwl.com.hlj.decision.R;

public class FactDetailFragment extends Fragment{
	
	private LinearLayout llContainer1 = null;
	private TextView tvPrompt = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_fact_detail, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
	}
	
	@SuppressWarnings("unchecked")
	private void initWidget(View view) {
		llContainer1 = (LinearLayout) view.findViewById(R.id.llContainer1);
		tvPrompt = (TextView) view.findViewById(R.id.tvPrompt);
		
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		int index = getArguments().getInt("index", 0);
		List<FactDto> dataList = new ArrayList<>();
		dataList.clear();
		dataList.addAll(getArguments().<FactDto>getParcelableArrayList("dataList"));
		if (dataList.size() > 0) {
			if (index == 0) {
				FactRainView rainView = new FactRainView(getActivity());
				rainView.setData(dataList);
				llContainer1.removeAllViews();
				int viewWidth;
				if (dataList.size() <= 25) {
					viewWidth = dm.widthPixels*2;
				}else {
					viewWidth = dm.widthPixels*4;
				}
				llContainer1.addView(rainView, viewWidth, LinearLayout.LayoutParams.MATCH_PARENT);
			}else if (index == 1) {
				FactTempView rainView = new FactTempView(getActivity());
				rainView.setData(dataList);
				llContainer1.removeAllViews();
				int viewWidth;
				if (dataList.size() <= 25) {
					viewWidth = dm.widthPixels*2;
				}else {
					viewWidth = dm.widthPixels*4;
				}
				llContainer1.addView(rainView, viewWidth, LinearLayout.LayoutParams.MATCH_PARENT);
			}else if (index == 2) {
				FactWindView rainView = new FactWindView(getActivity());
				rainView.setData(dataList);
				llContainer1.removeAllViews();
				int viewWidth;
				if (dataList.size() <= 25) {
					viewWidth = dm.widthPixels*2;
				}else {
					viewWidth = dm.widthPixels*4;
				}
				llContainer1.addView(rainView, viewWidth, LinearLayout.LayoutParams.MATCH_PARENT);
			}
			
		}else {
			tvPrompt.setVisibility(View.VISIBLE);
		}
	}
	
}
