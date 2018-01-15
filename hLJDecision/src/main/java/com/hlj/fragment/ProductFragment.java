package com.hlj.fragment;

/**
 * 热点新闻
 */

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.hlj.activity.NewsActivity;
import com.hlj.activity.ProductActivity;
import com.hlj.common.CONST;
import com.hlj.common.ColumnData;
import com.hlj.dto.NewsDto;
import com.hlj.adapter.ProductFragmentAdapter;
import com.hlj.activity.HPDFActivity;
import com.hlj.activity.HWebviewActivity;
import com.hlj.utils.CustomHttpClient;
import com.hlj.view.RefreshLayout;

import shawn.cxwl.com.hlj.R;

public class ProductFragment extends Fragment implements RefreshLayout.OnRefreshListener, RefreshLayout.OnLoadListener {
	
	private GridView gridView = null;
	private ProductFragmentAdapter mAdapter = null;
	private List<NewsDto> mList = new ArrayList<NewsDto>();
	private int countpage = 0;//总页数
	private int page = 1;
	private int pageSize = 20;
	private ColumnData data = null;
	private ProgressBar progressBar = null;
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private String appid = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.product_fragment, null);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initRefreshLayout(view);
		initWidget(view);
		initListView(view);
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout(View view) {
		refreshLayout = (RefreshLayout) view.findViewById(R.id.refreshLayout);
		refreshLayout.setColor(com.hlj.common.CONST.color1, com.hlj.common.CONST.color2, com.hlj.common.CONST.color3, com.hlj.common.CONST.color4);
		refreshLayout.setMode(RefreshLayout.Mode.BOTH);
		refreshLayout.setLoadNoFull(false);
		refreshLayout.setOnRefreshListener(this);
		refreshLayout.setOnLoadListener(this);
	}
	
	@Override
	public void onRefresh() {
		page = 1;
		pageSize = 20;
		mList.clear();
		operate();
	}
	
	@Override
	public void onLoad() {
		if (page >= countpage) {
			refreshLayout.setLoading(false);
			return;
		}else {
			if (data.dataUrl != null) {
				page += 1;
				
				String url = data.dataUrl;
				if (url.contains("pagesize")) {
					if (TextUtils.equals(appid, "15")) {
						url = CONST.GUIZHOU_BASE+"/Work/getnewslist/p/"+page+"/pagesize/"+pageSize+"/type/";
					}else if (TextUtils.equals(appid, "14")) {
						url = CONST.TIANJIN_BASE+"/Work/getnewslist/p/"+page+"/pagesize/"+pageSize+"/type/";
					}else if (TextUtils.equals(appid, "13")) {
						url = CONST.XIZANG_BASE+"/Work/getnewslist/p/"+page+"/pagesize/"+pageSize+"/type/";
					}
					
					String[] urls = data.dataUrl.split("/");
					asyncQuery(url + urls[urls.length-1]);
				}
			}
		}
	}

	
	private void initWidget(View view) {
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		
		appid = getArguments().getString(CONST.INTENT_APPID);
		
		operate();
	}
	
	private void operate() {
		data = getArguments().getParcelable("data");
		if (data != null) {
			if (data.dataUrl != null) {
				asyncQuery(data.dataUrl);
			}
		}
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView(View view) {
		gridView = (GridView) view.findViewById(R.id.gridView);
		mAdapter = new ProductFragmentAdapter(getActivity(), mList, appid);
		gridView.setAdapter(mAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				NewsDto dto = mList.get(arg2);
				Intent intent = null;
				if (TextUtils.equals(dto.showType, CONST.URL)) {//url
					intent = new Intent(getActivity(), HWebviewActivity.class);
				}else if (TextUtils.equals(dto.showType, CONST.PDF)) {//pdf
					intent = new Intent(getActivity(), HPDFActivity.class);
				}else if (TextUtils.equals(dto.showType, CONST.NEWS)) {//news
					intent = new Intent(getActivity(), NewsActivity.class);
				}else if (TextUtils.equals(dto.showType, CONST.PRODUCT)) {//product
					intent = new Intent(getActivity(), ProductActivity.class);
				}
				if (intent != null) {
					intent.putExtra(CONST.ACTIVITY_NAME, dto.title);
					intent.putExtra(CONST.WEB_URL, dto.detailUrl);
					intent.putExtra(CONST.INTENT_APPID, appid);
					intent.putExtra(CONST.INTENT_IMGURL, dto.imgUrl);
					startActivity(intent);
				}
			}
		});
	}
	
	private void asyncQuery(String url) {
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(url);
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
			progressBar.setVisibility(View.GONE);
			refreshLayout.setRefreshing(false);
			refreshLayout.setLoading(false);
			if (requestResult != null) {
				try {
					JSONObject obj = new JSONObject(requestResult);
					if (!obj.isNull("count")) {
						String num = obj.getString("countpage");
						if (!TextUtils.isEmpty(num)) {
							countpage = Integer.valueOf(obj.getString("countpage"));
						}
					}
					if (!obj.isNull("info")) {
						JSONArray array = new JSONArray(obj.getString("info"));
						for (int i = 0; i < array.length(); i++) {
							JSONObject itemObj = array.getJSONObject(i);
							NewsDto dto = new NewsDto();
							dto.imgUrl = itemObj.getString("icon");
							dto.title = itemObj.getString("name");
							dto.time = itemObj.getString("addtime");
							dto.detailUrl = itemObj.getString("urladdress");
							dto.showType = itemObj.getString("showtype");
							mList.add(dto);
						}
						
						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
						}
					}
				} catch (JSONException e1) {
					e1.printStackTrace();
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
	
}
