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

import com.hlj.activity.HCommonPdfListActivity;
import com.hlj.activity.SinggleUrlActivity;
import com.hlj.activity.SinglePDFActivity;
import com.hlj.adapter.HWeatherForecastFragmentAdapter;
import com.hlj.common.ColumnData;
import com.hlj.dto.AgriDto;

import java.util.ArrayList;
import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 人工影响天气
 * @author shawn_sun
 *
 */

public class HPersonInfuluceFragment extends Fragment{

	private GridView gridView = null;
	private HWeatherForecastFragmentAdapter mAdapter = null;
	private List<AgriDto> mList = new ArrayList<>();
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.hfragment_person_infuluce, null);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initGridView(view);
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
				if (TextUtils.equals(dto.id, "131")) {
					intent = new Intent(getActivity(), SinggleUrlActivity.class);
				}else if (TextUtils.equals(dto.id, "132")) {
					intent = new Intent(getActivity(), SinglePDFActivity.class);
				}else {
					intent = new Intent(getActivity(), HCommonPdfListActivity.class);
				}
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
	
}
