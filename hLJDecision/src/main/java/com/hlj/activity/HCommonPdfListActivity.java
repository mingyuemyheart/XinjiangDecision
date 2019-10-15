package com.hlj.activity;

/**
 * 电力气象服务（一周天气预报、短期天气预报、全省重大天气预报、省电力预报、旬月回顾与展望）
 * 铁路气象服务（站点预报、旬预报、一周天气预报、全省重大天气预报、短时预警预报）
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hlj.adapter.CommonPdfListAdapter;
import com.hlj.common.CONST;
import com.hlj.dto.AgriDto;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.OkHttpUtil;
import com.hlj.view.RefreshLayout;
import com.hlj.view.RefreshLayout.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

public class HCommonPdfListActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ListView mListView = null;
	private CommonPdfListAdapter mAdapter = null;
	private List<AgriDto> mList = new ArrayList<>();
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private AgriDto dto = null;
	private SimpleDateFormat sdf11 = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
	private SimpleDateFormat sdf12 = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
	private SimpleDateFormat sdf21 = new SimpleDateFormat("yyyyMMddHH", Locale.CHINA);
	private SimpleDateFormat sdf22 = new SimpleDateFormat("yyyy年MM月dd日HH时", Locale.CHINA);
	private SimpleDateFormat sdf31 = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
	private SimpleDateFormat sdf32 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA);
	private SimpleDateFormat sdf41 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
	private SimpleDateFormat sdf42 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hactivity_common_pdf_list);
		mContext = this;
		initRefreshLayout();
		initWidget();
		initListView();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);

		refresh();
	}
	
	private void refresh() {
		dto = getIntent().getExtras().getParcelable("data");
		if (dto != null) {
			if (!TextUtils.isEmpty(dto.name)) {
				tvTitle.setText(dto.name);
			}
			if (!TextUtils.isEmpty(dto.dataUrl)) {
				OkHttpDetail(dto.dataUrl);
			}
		}

		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, dto.name);
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
		refreshLayout.setColor(com.hlj.common.CONST.color1, com.hlj.common.CONST.color2, com.hlj.common.CONST.color3, com.hlj.common.CONST.color4);
		refreshLayout.setMode(RefreshLayout.Mode.PULL_FROM_START);
		refreshLayout.setLoadNoFull(false);
		refreshLayout.setRefreshing(true);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
	}
	
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new CommonPdfListAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				AgriDto dto = mList.get(arg2);
				Intent intent = null;
				if (!TextUtils.isEmpty(dto.dataUrl)) {
					if (dto.dataUrl.contains(".pdf") || dto.dataUrl.contains(".PDF")) {
						intent = new Intent(mContext, HPDFActivity.class);
					}else {
						intent = new Intent(mContext, Url2Activity.class);
					}
				}
				if (intent != null) {
					intent.putExtra(CONST.ACTIVITY_NAME, dto.title);
					intent.putExtra(CONST.WEB_URL, dto.dataUrl);
					startActivity(intent);
				}
			}
		});
	}
	
	/**
	 * 获取详情
	 */
	private void OkHttpDetail(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									refreshLayout.setRefreshing(false);
									try {
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("info")) {
											mList.clear();
											JSONArray array = obj.getJSONArray("info");
											for (int i = 0; i < array.length(); i++) {
												AgriDto tempDto = new AgriDto();
												tempDto.dataUrl = array.getString(i);
												String title = "-因无业务需求,暂停发布本产品";
												if (tempDto.dataUrl.contains(title)) {
													tempDto.title = dto.name+title;
												}else {
													tempDto.title = dto.name;
												}
												tempDto.time = time(tempDto.dataUrl);
												mList.add(tempDto);
											}
										}

										if (mAdapter != null) {
											mAdapter.notifyDataSetChanged();
										}

									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
						});
					}
				});
			}
		}).start();
	}

	private boolean isMatches(String pattern, String content) {
		return Pattern.matches(pattern, content);
	}

	private String time(String dataUrl) {
		String time = "";
		try {
			if (isMatches("^[0-9]\\d{13}", dataUrl.substring(dataUrl.length()-18, dataUrl.length()-4))) {
				time = sdf42.format(sdf41.parse(dataUrl.substring(dataUrl.length()-18, dataUrl.length()-4)));
			}else if (isMatches("^[0-9]\\d{11}", dataUrl.substring(dataUrl.length()-16, dataUrl.length()-4))) {
				time = sdf32.format(sdf31.parse(dataUrl.substring(dataUrl.length()-16, dataUrl.length()-4)));
			}else if (isMatches("^[0-9]\\d{9}", dataUrl.substring(dataUrl.length()-14, dataUrl.length()-4))) {
				time = sdf22.format(sdf21.parse(dataUrl.substring(dataUrl.length()-14, dataUrl.length()-4)));
			}else if (isMatches("^[0-9]\\d{7}", dataUrl.substring(dataUrl.length()-12, dataUrl.length()-4))) {
				time = sdf12.format(sdf11.parse(dataUrl.substring(dataUrl.length()-12, dataUrl.length()-4)));
			}else if (isMatches("^[0-9]\\d{7}-[0-9]", dataUrl.substring(dataUrl.length()-14, dataUrl.length()-4))) {
				time = sdf12.format(sdf11.parse(dataUrl.substring(dataUrl.length()-14, dataUrl.length()-6)));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return time;
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
