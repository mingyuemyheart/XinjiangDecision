package com.hlj.activity;

import java.util.ArrayList;
import java.util.List;
import shawn.cxwl.com.hlj.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hlj.adapter.TrendRankAdapter;
import com.hlj.common.CONST;
import com.hlj.dto.RangeDto;

public class TrendRankActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;//返回按钮
	private TextView tvTitle = null;
	private TextView tvContent = null;
	private ListView mListView = null;
	private TrendRankAdapter mAdapter = null;
	private List<RangeDto> mList = new ArrayList<RangeDto>();
	private String element = null;
	private RangeDto data = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trend_rank);
		mContext = this;
		initWidget();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.rank_weather));
		tvContent = (TextView) findViewById(R.id.tvContent);
		tvContent.setText(getIntent().getStringExtra(com.hlj.common.CONST.ACTIVITY_NAME));
		
		if (getIntent().hasExtra("data")) {
			data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				element = getIntent().getStringExtra("element");
				if (element != null) {
					mList.clear();
					if (element.equals(CONST.TEM_f)) {
						mList.addAll(data.maxTempList);
					}else if (element.equals(CONST.TEM)) {
						mList.addAll(data.minTempList);
					}else if (element.equals(CONST.PRE_1h_f)) {
						mList.addAll(data.oneRainList);
					}else if (element.equals(CONST.PRE_24h_f)) {
						mList.addAll(data.tfRainList);
					}else if (element.equals(CONST.RHU_f)) {
						mList.addAll(data.maxHumidityList);
					}else if (element.equals(CONST.RHU)) {
						mList.addAll(data.minHumidityList);
					}else if (element.equals(CONST.WIN_S_Avg_2mi)) {
						mList.addAll(data.windSpeedList);
					}else if (element.equals(CONST.VIS)) {
						mList.addAll(data.visibleList);
					}
					initListView();
				}
			}
		}
		
	}
	
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new TrendRankAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				RangeDto dto = mList.get(arg2);
				Intent intent = new Intent(mContext, TrendDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
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
