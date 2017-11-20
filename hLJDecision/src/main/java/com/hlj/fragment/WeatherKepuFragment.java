package com.hlj.fragment;

import java.util.ArrayList;
import java.util.List;

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
import android.widget.ListView;

import com.hlj.common.CONST;
import com.hlj.common.ColumnData;
import com.hlj.dto.AgriDto;
import com.hlj.activity.HWebviewActivity;
import shawn.cxwl.com.hlj.decision.R;
import com.hlj.activity.WarningSignalActivity;
import com.hlj.activity.WeatherKepuDetailActivity;
import com.hlj.adapter.HAgriWeatherAdapter;

/**
 * 气象科普
 * @author shawn_sun
 *
 */

public class WeatherKepuFragment extends Fragment{
	
	private ListView mListView = null;
	private HAgriWeatherAdapter mAdapter = null;
	private List<AgriDto> mList = new ArrayList<AgriDto>();
	private ColumnData data = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.hfragment_person_infuluce, null);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		data = getArguments().getParcelable("data");
		initListView(view);
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView(View view) {
		mList.clear();
		for (int i = 0; i < data.child.size(); i++) {
			AgriDto dto = new AgriDto();
			dto.id = data.child.get(i).id;
			dto.name = data.child.get(i).name;
			dto.showType = data.child.get(i).showType;
			dto.icon = data.child.get(i).icon;
			dto.dataUrl = data.child.get(i).dataUrl;
			dto.child = data.child.get(i).child;
			mList.add(dto);
		}
		mListView = (ListView) view.findViewById(R.id.listView);
		mAdapter = new HAgriWeatherAdapter(getActivity(), mList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				AgriDto dto = mList.get(arg2);
				Intent intent = null;
				if (TextUtils.equals(dto.showType, CONST.NEWS)) {// 气象百科、动漫科普
					intent = new Intent(getActivity(), WeatherKepuDetailActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable("dto", dto);
					intent.putExtras(bundle);
					startActivity(intent);
				} else if (TextUtils.equals(dto.showType, CONST.LOCAL)) {
					if (TextUtils.equals(dto.id, "701")) {// 气候背景
						intent = new Intent(getActivity(), HWebviewActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					} else if (TextUtils.equals(dto.id, "702")) {// 预警信号
						intent = new Intent(getActivity(), WarningSignalActivity.class);
						intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
						intent.putExtra(CONST.WEB_URL, dto.dataUrl);
						startActivity(intent);
					}
				}
			}
		});
	}
}
