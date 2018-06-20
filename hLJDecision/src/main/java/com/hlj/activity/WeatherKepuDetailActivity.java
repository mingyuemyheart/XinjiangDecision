package com.hlj.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hlj.adapter.HFactTableAdapter;
import com.hlj.dto.AgriDto;
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

public class WeatherKepuDetailActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ImageView ivArrow = null;
	private RelativeLayout reContainer = null;
	private ListView tableListView = null;
	private HFactTableAdapter tableAdapter = null;
	private List<AgriDto> tableList = new ArrayList<>();
	private WebView webView = null;
	private WebSettings webSettings = null;
	private String url = null;
	private RefreshLayout refreshLayout = null;//下拉刷新布局

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weather_kepu_detail);
		mContext = this;
		initRefreshLayout();
		initWidget();
		initTableListView();
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
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setOnClickListener(this);
		ivArrow = (ImageView) findViewById(R.id.ivArrow);
		ivArrow.setOnClickListener(this);
		reContainer = (RelativeLayout) findViewById(R.id.reContainer);
		
		refresh();
	}
	
	private void refresh() {
		AgriDto dto = getIntent().getExtras().getParcelable("dto");
		if (dto != null) {
			if (!TextUtils.isEmpty(dto.dataUrl)) {
				OkHttpList(dto.dataUrl);
			}
		}
	}
	
	/**
	 * 获取详情
	 */
	private void OkHttpList(final String requestUrl) {
		refreshLayout.setRefreshing(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(requestUrl).build(), new Callback() {
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
									try {
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("info")) {
											tableList.clear();
											JSONArray array = obj.getJSONArray("info");
											for (int i = 0; i < array.length(); i++) {
												JSONObject itemObj = array.getJSONObject(i);
												AgriDto dto = new AgriDto();
												dto.title = itemObj.getString("name");
												dto.dataUrl = itemObj.getString("urladdress");
												dto.time = itemObj.getString("addtime");
												dto.showType = itemObj.getString("showtype");
												tableList.add(dto);

												if (i == 0) {
													url = dto.dataUrl;
													tvTitle.setText(dto.title);
													ivArrow.setVisibility(View.VISIBLE);
													initWebView();
												}
											}
										}

										if (tableAdapter != null) {
											tableAdapter.notifyDataSetChanged();
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
	
	/**
	 * 初始化webview
	 */
	private void initWebView() {
		if (url == null) {
			return;
		}
		webView = (WebView) findViewById(R.id.webView);
		webSettings = webView.getSettings();
		
		//支持javascript
		webSettings.setJavaScriptEnabled(true); 
		// 设置可以支持缩放 
		webSettings.setSupportZoom(true); 
		// 设置出现缩放工具 
		webSettings.setBuiltInZoomControls(true);
		//扩大比例的缩放
		webSettings.setUseWideViewPort(true);
		//自适应屏幕
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webSettings.setLoadWithOverviewMode(true);
		webView.loadUrl(url);
		
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				if (title != null) {
					tvTitle.setText(title);
				}
			}
		});
		
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String itemUrl) {
				url = itemUrl;
				webView.loadUrl(url);
				return true;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				refreshLayout.setRefreshing(false);
			}
		});
	}
	
	private void initTableListView() {
		tableListView = (ListView) findViewById(R.id.tableListView);
		tableAdapter = new HFactTableAdapter(mContext, tableList);
		tableListView.setAdapter(tableAdapter);
		tableListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				AgriDto dto = tableList.get(arg2);
				if (!TextUtils.isEmpty(dto.dataUrl)) {
					tvTitle.setText(dto.title);
					switchData();
					if (webView != null) {
						refreshLayout.setRefreshing(true);
						webView.loadUrl(dto.dataUrl);
					}
				}
			}
		});
	}
	
	/**
	 * 切换数据
	 */
	private void switchData() {
		if (reContainer.getVisibility() == View.GONE) {
			startAnimation(false, reContainer);
			reContainer.setVisibility(View.VISIBLE);
			ivArrow.setImageResource(R.drawable.iv_arrow_up);
		}else {
			startAnimation(true, reContainer);
			reContainer.setVisibility(View.GONE);
			ivArrow.setImageResource(R.drawable.iv_arrow_down);
		}
	}
	
	/**
	 * @param flag false为显示map，true为显示list
	 */
	private void startAnimation(final boolean flag, final RelativeLayout reContainer) {
		//列表动画
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation animation = null;
		if (flag == false) {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,-1.0f,
					Animation.RELATIVE_TO_SELF,0f);
		}else {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,-1.0f);
		}
		animation.setDuration(400);
		animationSet.addAnimation(animation);
		animationSet.setFillAfter(true);
		reContainer.startAnimation(animationSet);
		animationSet.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				reContainer.clearAnimation();
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (reContainer.getVisibility() == View.GONE) {
				finish();
			}else {
				startAnimation(true, reContainer);
				reContainer.setVisibility(View.GONE);
				ivArrow.setImageResource(R.drawable.iv_arrow_down);
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			if (reContainer.getVisibility() == View.GONE) {
				finish();
			}else {
				startAnimation(true, reContainer);
				reContainer.setVisibility(View.GONE);
				ivArrow.setImageResource(R.drawable.iv_arrow_down);
			}
			break;
		case R.id.tvTitle:
		case R.id.ivArrow:
			switchData();
			break;

		default:
			break;
		}
	}
	
}
