package com.hlj.activity;

import java.util.ArrayList;
import java.util.List;
import shawn.cxwl.com.hlj.decision.R;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hlj.dto.AgriDto;
import com.hlj.adapter.HTemperatureForecastAdapter;

/**
 * 气温预报、雾霾预报、降温大风沙尘预报  同一种类型
 * @author shawn_sun
 *
 */

public class HTempratureForecastActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ListView mListView = null;
	private HTemperatureForecastAdapter mAdapter = null;
	private List<AgriDto> mList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hactivity_temperature_forecast);
		mContext = this;
		initWidget();
		initListView();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		
		AgriDto data = getIntent().getExtras().getParcelable("data");
		if (data != null) {
			tvTitle.setText(data.name);
			
			mList.clear();
			for (int i = 0; i < data.child.size(); i++) {
				AgriDto dto = new AgriDto();
				dto.name = data.child.get(i).name;
				dto.dataUrl = data.child.get(i).dataUrl;
				mList.add(dto);
			}
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
		}
	}
	
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new HTemperatureForecastAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}
	
}
