package com.hlj.activity;

/**
 * 实况详情
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hlj.adapter.FactDetailAdapter;
import com.hlj.dto.FactDto;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import shawn.cxwl.com.hlj.R;

public class FactDetailActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView tvPrompt = null;
	private LinearLayout ll1, ll2, ll3;
	private TextView tv1, tv2, tv3;
	private ImageView iv1, iv2, iv3;
	private boolean b1 = false, b2 = false, b3 = false;//false为将序，true为升序
	private ListView listView = null;
	private FactDetailAdapter mAdapter = null;
	private List<FactDto> realDatas = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fact_detail);
		mContext = this;
		initWidget();
		initListView();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvPrompt = (TextView) findViewById(R.id.tvPrompt);
		ll1 = (LinearLayout) findViewById(R.id.ll1);
		ll1.setOnClickListener(this);
		ll2 = (LinearLayout) findViewById(R.id.ll2);
		ll2.setOnClickListener(this);
		ll3 = (LinearLayout) findViewById(R.id.ll3);
		ll3.setOnClickListener(this);
		tv1 = (TextView) findViewById(R.id.tv1);
		tv2 = (TextView) findViewById(R.id.tv2);
		tv3 = (TextView) findViewById(R.id.tv3);
		iv1 = (ImageView) findViewById(R.id.iv1);
		iv2 = (ImageView) findViewById(R.id.iv2);
		iv3 = (ImageView) findViewById(R.id.iv3);

		if (getIntent().hasExtra("realDatas")) {
			String title = getIntent().getStringExtra("title");
			String timeString = getIntent().getStringExtra("timeString");
			String stationName = getIntent().getStringExtra("stationName");
			String area = getIntent().getStringExtra("area");
			String val = getIntent().getStringExtra("val");
			if (!TextUtils.isEmpty(title)) {
				tvTitle.setText(title);
			}
			if (!TextUtils.isEmpty(timeString)) {
				tvPrompt.setText(timeString);
			}
			if (!TextUtils.isEmpty(stationName)) {
				tv1.setText(stationName);
			}
			if (!TextUtils.isEmpty(area)) {
				tv2.setText(area);
			}
			if (!TextUtils.isEmpty(val)) {
				tv3.setText(val);
			}
			
			realDatas.clear();
			realDatas.addAll(getIntent().getExtras().<FactDto>getParcelableArrayList("realDatas"));
		}
		
		if (!b3) {//将序
			iv3.setImageResource(R.drawable.arrow_down);
		}else {//将序
			iv3.setImageResource(R.drawable.arrow_up);
		}
		iv3.setVisibility(View.VISIBLE);
	}
	
	private void initListView() {
		listView = (ListView) findViewById(R.id.listView);
		mAdapter = new FactDetailAdapter(mContext, realDatas);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				FactDto dto = realDatas.get(arg2);
				Intent intent = new Intent(mContext, FactDetailChartActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}

	/**
	 * 返回中文的首字母
 	 * @param str
	 * @return
	 */
    public static String getPinYinHeadChar(String str) {
        String convert = "";
        int size = str.length();
        if (size >= 2) {
        	size = 2;
		}
        for (int j = 0; j < size; j++) {
            char word = str.charAt(j);
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
            if (pinyinArray != null) {
                convert += pinyinArray[0].charAt(0);
            } else {
                convert += word;
            }
        }
        return convert;
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ll1:
			if (b1) {//升序
				b1 = false;
				iv1.setImageResource(R.drawable.arrow_up);
				iv1.setVisibility(View.VISIBLE);
				iv2.setVisibility(View.INVISIBLE);
				iv3.setVisibility(View.INVISIBLE);
				Collections.sort(realDatas, new Comparator<FactDto>() {
					@Override
					public int compare(FactDto arg0, FactDto arg1) {
						if (TextUtils.isEmpty(arg0.stationName) || TextUtils.isEmpty(arg1.stationName)) {
							return 0;
						}else {
							return getPinYinHeadChar(arg0.stationName).compareTo(getPinYinHeadChar(arg1.stationName));
						}
					}
				});
			}else {//将序
				b1 = true;
				iv1.setImageResource(R.drawable.arrow_down);
				iv1.setVisibility(View.VISIBLE);
				iv2.setVisibility(View.INVISIBLE);
				iv3.setVisibility(View.INVISIBLE);
				Collections.sort(realDatas, new Comparator<FactDto>() {
					@Override
					public int compare(FactDto arg0, FactDto arg1) {
						if (TextUtils.isEmpty(arg0.stationName) || TextUtils.isEmpty(arg1.stationName)) {
							return -1;
						}else {
							return getPinYinHeadChar(arg1.stationName).compareTo(getPinYinHeadChar(arg0.stationName));
						}
					}
				});
			}
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
			break;
		case R.id.ll2:
			if (b2) {//升序
				b2 = false;
				iv2.setImageResource(R.drawable.arrow_up);
				iv1.setVisibility(View.INVISIBLE);
				iv2.setVisibility(View.VISIBLE);
				iv3.setVisibility(View.INVISIBLE);
				Collections.sort(realDatas, new Comparator<FactDto>() {
					@Override
					public int compare(FactDto arg0, FactDto arg1) {
						if (TextUtils.isEmpty(arg0.area) || TextUtils.isEmpty(arg1.area)) {
							return 0;
						}else {
							return getPinYinHeadChar(arg0.area).compareTo(getPinYinHeadChar(arg1.area));
						}
					}
				});
			}else {//将序
				b2 = true;
				iv2.setImageResource(R.drawable.arrow_down);
				iv1.setVisibility(View.INVISIBLE);
				iv2.setVisibility(View.VISIBLE);
				iv3.setVisibility(View.INVISIBLE);
				Collections.sort(realDatas, new Comparator<FactDto>() {
					@Override
					public int compare(FactDto arg0, FactDto arg1) {
						if (TextUtils.isEmpty(arg0.area) || TextUtils.isEmpty(arg1.area)) {
							return -1;
						}else {
							return getPinYinHeadChar(arg1.area).compareTo(getPinYinHeadChar(arg0.area));
						}
					}
				});
			}
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
			break;
		case R.id.ll3:
			if (b3) {//升序
				b3 = false;
				iv3.setImageResource(R.drawable.arrow_up);
				iv1.setVisibility(View.INVISIBLE);
				iv2.setVisibility(View.INVISIBLE);
				iv3.setVisibility(View.VISIBLE);
				Collections.sort(realDatas, new Comparator<FactDto>() {
					@Override
					public int compare(FactDto arg0, FactDto arg1) {
						return Double.valueOf(arg0.val).compareTo(Double.valueOf(arg1.val));
					}
				});
			}else {//将序
				b3 = true;
				iv3.setImageResource(R.drawable.arrow_down);
				iv1.setVisibility(View.INVISIBLE);
				iv2.setVisibility(View.INVISIBLE);
				iv3.setVisibility(View.VISIBLE);
				Collections.sort(realDatas, new Comparator<FactDto>() {
					@Override
					public int compare(FactDto arg0, FactDto arg1) {
						return Double.valueOf(arg1.val).compareTo(Double.valueOf(arg0.val));
					}
				});
			}
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
			break;

		default:
			break;
		}
	}
	
}
