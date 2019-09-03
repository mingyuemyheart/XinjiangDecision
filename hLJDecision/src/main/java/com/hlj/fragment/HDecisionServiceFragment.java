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
import android.widget.ListView;

import com.hlj.activity.HPDFActivity;
import com.hlj.adapter.CommonPdfListAdapter;
import com.hlj.common.CONST;
import com.hlj.common.ColumnData;
import com.hlj.dto.AgriDto;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.OkHttpUtil;
import com.hlj.view.RefreshLayout;
import com.hlj.view.RefreshLayout.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

/**
 * 决策服务
 */

public class HDecisionServiceFragment extends Fragment {
	
	private ListView mListView = null;
	private CommonPdfListAdapter mAdapter = null;
	private List<AgriDto> mList = new ArrayList<>();
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.hfragment_decision_service, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initRefreshLayout(view);
		refresh();
		initListView(view);

		String columnId = getArguments().getString(CONST.COLUMN_ID);
		String title = getArguments().getString(CONST.ACTIVITY_NAME);
		CommonUtil.submitClickCount(columnId, title);
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout(View view) {
		refreshLayout = (RefreshLayout) view.findViewById(R.id.refreshLayout);
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

	private void refresh() {
		ColumnData data = getArguments().getParcelable("data");
		if (data != null) {
			if (!TextUtils.isEmpty(data.dataUrl)) {
				OkHttpDetail(data.dataUrl);
			}
		}
	}
	
	private void initListView(View view) {
		mListView = (ListView) view.findViewById(R.id.listView);
		mAdapter = new CommonPdfListAdapter(getActivity(), mList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				AgriDto dto = mList.get(arg2);
				Intent intent = null;
				if (TextUtils.equals(dto.type, CONST.PDF)) {
					intent = new Intent(getActivity(), HPDFActivity.class);
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
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									refreshLayout.setRefreshing(false);
									try {
										JSONObject obj = new JSONObject(result);
										String type = null;
										if (!obj.isNull("type")) {
											type = obj.getString("type");
										}
										if (!obj.isNull("l")) {
											mList.clear();
											JSONArray array = obj.getJSONArray("l");
											for (int i = 0; i < array.length(); i++) {
												JSONObject itemObj = array.getJSONObject(i);
												AgriDto dto = new AgriDto();
												dto.title = itemObj.getString("l1");
												dto.dataUrl = itemObj.getString("l2");
												dto.time = itemObj.getString("l3");
												dto.type = type;
												mList.add(dto);
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
	
}
