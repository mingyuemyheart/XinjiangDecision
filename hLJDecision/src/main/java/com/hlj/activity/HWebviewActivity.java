package com.hlj.activity;

/**
 * 普通webview
 */

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlj.common.CONST;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.OkHttpUtil;
import com.hlj.view.RefreshLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

public class HWebviewActivity extends BaseActivity implements OnClickListener{
	
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private WebView webView = null;
	private WebSettings webSettings = null;
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hactivity_webview);
		initRefreshLayout();
		initWidget();
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
		refreshLayout.setColor(com.hlj.common.CONST.color1, com.hlj.common.CONST.color2, com.hlj.common.CONST.color3, com.hlj.common.CONST.color4);
		refreshLayout.setMode(RefreshLayout.Mode.PULL_FROM_START);
		refreshLayout.setLoadNoFull(false);
		refreshLayout.setOnRefreshListener(new RefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
	}
	
	private void refresh() {
		String url = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(url)) {
			OkHttpDetail(url);
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("详情");

		refresh();
	}
	
	/**
	 * 获取详情
	 */
	private void OkHttpDetail(final String requestUrl) {
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
										String type = obj.getString("type");
										String c1 = obj.getString("c1");
										String c2 = obj.getString("c2");
										String c3 = obj.getString("c3");
										String c4 = obj.getString("c4");
										String c7 = obj.getString("c7");
										initWebView(type, c1, c2, c3, c4, c7);
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
	private void initWebView(String type, String c1, String c2, String c3, String c4, String c7) {
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
//		webView.loadUrl(url);
		
		String css = CommonUtil.getFromAssets(this, "p_item.css");
		String style = "<meta charset=\"UTF-8\">"+ "<meta name=\"viewport\" content=\"initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0,user-scalable=no\" />" +"<style>"+ css +"</style>";
		String html = "<div class=\"c_content "+ type +"\">";
		if (TextUtils.equals(type, "tw")) {
			html += "<div class=\"c_title\"><h1>"+ c1 +"</h1></div>";
			html += "<div class=\"c_text\">"+ c2 +"</div><span class=\"c_copyright\">"+ c3 +"</span></div>";
		}else if (TextUtils.equals(type, "jcbw")) {
			html += "<div class=\"c_title c_f_red\"><h2>"+ c1 +"</h2><div class=\"c_postil\"><span class=\"fl\">"+ c3 +"</span><span class=\"fr\">"+ c4 +"</span></div></div><div class=\"c_text\">"+ c7 +"</div></div>";
		}
		html = style + html;
		webView.loadDataWithBaseURL("", html, "text/html", "utf-8", "");
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				refreshLayout.setRefreshing(false);
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (webView != null && webView.canGoBack()) {
				webView.goBack();
				return true;
			}else {
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.llBack) {
			finish();
		}
	}
}
