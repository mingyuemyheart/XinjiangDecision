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

import com.hlj.common.CONST;
import com.hlj.dto.AgriDto;
import com.hlj.utils.OkHttpUtil;
import com.hlj.view.RefreshLayout;
import com.hlj.view.RefreshLayout.OnRefreshListener;
import com.hlj.adapter.CommonPdfListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日");

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
	private void OkHttpDetail(String url) {
		OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {

			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					return;
				}
				String result = response.body().string();
				if (!TextUtils.isEmpty(result)) {
					try {
						JSONObject obj = new JSONObject(result);
						if (!obj.isNull("info")) {
							mList.clear();
							JSONArray array = obj.getJSONArray("info");
							for (int i = 0; i < array.length(); i++) {
								AgriDto tempDto = new AgriDto();
								tempDto.title = dto.name;
								tempDto.dataUrl = array.getString(i);
								try {
									String time = tempDto.dataUrl.substring(tempDto.dataUrl.length()-12, tempDto.dataUrl.length()-4);
									tempDto.time = sdf2.format(sdf1.parse(time));
								} catch (ParseException e) {
									e.printStackTrace();
								}
								mList.add(tempDto);
							}
						}

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								refreshLayout.setRefreshing(false);
								if (mList.size() > 0 && mAdapter != null) {
									mAdapter.notifyDataSetChanged();
								}
							}
						});

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
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
