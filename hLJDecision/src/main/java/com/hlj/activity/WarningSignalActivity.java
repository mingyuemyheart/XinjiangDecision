package com.hlj.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import shawn.cxwl.com.hlj.R;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hlj.common.CONST;
import com.hlj.dto.AgriDto;
import com.hlj.utils.CustomHttpClient;
import com.hlj.view.RefreshLayout;
import com.hlj.view.RefreshLayout.OnRefreshListener;
import com.hlj.adapter.WarningSignalAdapter;

/**
 * 预警信号
 * @author shawn_sun
 *
 */

public class WarningSignalActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ListView mListView = null;
	private WarningSignalAdapter mAdapter = null;
	private List<AgriDto> mList = new ArrayList<AgriDto>();
	private RefreshLayout refreshLayout = null;//下拉刷新布局

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.warning_signal);
		mContext = this;
		initRefreshLayout();
		initWidget();
		initListView();
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
		refreshLayout.setColor(com.hlj.common.CONST.color1, com.hlj.common.CONST.color2, com.hlj.common.CONST.color3, com.hlj.common.CONST.color4);
		refreshLayout.setMode(RefreshLayout.Mode.PULL_FROM_START);
		refreshLayout.setLoadNoFull(false);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		
		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (title != null) {
			tvTitle.setText(title);
		}

		refresh();
	}
	
	private void refresh() {
		String url = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(url)) {
			asyncQuery(url);
		}
	}
	
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new WarningSignalAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
	}
	
	/**
	 * 获取详情
	 */
	private void asyncQuery(String requestUrl) {
		refreshLayout.setRefreshing(true);
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(requestUrl);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private String method = "GET";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTask() {
		}
		
		@Override
		protected String doInBackground(String... url) {
			String result = null;
			if (method.equalsIgnoreCase("POST")) {
				result = CustomHttpClient.post(url[0], nvpList);
			} else if (method.equalsIgnoreCase("GET")) {
				result = CustomHttpClient.get(url[0]);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String requestResult) {
			super.onPostExecute(requestResult);
			refreshLayout.setRefreshing(false);
			if (!TextUtils.isEmpty(requestResult)) {
				try {
					mList.clear();
					JSONObject obj = new JSONObject(requestResult);
					if (!obj.isNull("l")) {
						JSONArray array = obj.getJSONArray("l");
						String[] warningTypes = getResources().getStringArray(R.array.warningType);
						for (int i = 0; i < array.length(); i++) {
							JSONObject itemObj = array.getJSONObject(i);
							AgriDto dto = new AgriDto();
							if (!itemObj.isNull("l1")) {
								String l1 = itemObj.getString("l1");
								String name = l1.substring(0, l1.indexOf("（"));
								dto.name = name+getString(R.string.warning)+":";
								for (int j = 0; j < warningTypes.length; j++) {
									String[] types = warningTypes[j].split(",");
									if (types[1].equals(name)) {
										dto.warningType = types[0];
									}
								}
								
								if (l1.contains(getString(R.string.color_blue))) {
									dto.blue = getString(R.string.color_blue);
									dto.blueCode = "01";
								}
								if (l1.contains(getString(R.string.color_yellow))) {
									dto.yellow = getString(R.string.color_yellow);
									dto.yellowCode = "02";
								}
								if (l1.contains(getString(R.string.color_orange))) {
									dto.orange = getString(R.string.color_orange);
									dto.orangeCode = "03";
								}
								if (l1.contains(getString(R.string.color_red))) {
									dto.red = getString(R.string.color_red);
									dto.redCode = "04";
								}
								
								dto.dataUrl = itemObj.getString("l2");
								dto.title = tvTitle.getText().toString();
								
								mList.add(dto);
							}
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

		@SuppressWarnings("unused")
		private void setParams(NameValuePair nvp) {
			nvpList.add(nvp);
		}

		private void setMethod(String method) {
			this.method = method;
		}

		private void setTimeOut(int timeOut) {
			CustomHttpClient.TIME_OUT = timeOut;
		}

		/**
		 * 取消当前task
		 */
		@SuppressWarnings("unused")
		private void cancelTask() {
			CustomHttpClient.shuttdownRequest();
			this.cancel(true);
		}
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
