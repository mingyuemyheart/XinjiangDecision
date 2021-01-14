package com.hlj.activity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hlj.common.CONST;
import com.hlj.utils.CommonUtil;

import java.util.HashMap;
import java.util.Map;

import shawn.cxwl.com.hlj.R;

/**
 * 视频会商点播
 */
public class ShawnWeatherMeetingVideoActivity extends BaseActivity implements OnClickListener{
	
	private RelativeLayout reTitle = null;
	private TextView tvContent = null;
	private WebView webView = null;
	private String url = null;
	private SwipeRefreshLayout refreshLayout = null;//下拉刷新布局
	private Configuration configuration = null;//方向监听器

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_weather_meeting_video);
		initRefreshLayout();
		initWidget();
		initWebView();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		configuration = newConfig;
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			showPort();
		}else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			showLand();
		}
	}

	/**
	 * 显示竖屏，隐藏横屏
	 */
	private void showPort() {
		reTitle.setVisibility(View.VISIBLE);
		fullScreen(false);
	}

	/**
	 * 显示横屏，隐藏竖屏
	 */
	private void showLand() {
		reTitle.setVisibility(View.GONE);
		fullScreen(true);
	}

	private void fullScreen(boolean enable) {
		if (enable) {
			WindowManager.LayoutParams lp = getWindow().getAttributes();
			lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			getWindow().setAttributes(lp);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		} else {
			WindowManager.LayoutParams attr = getWindow().getAttributes();
			attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().setAttributes(attr);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		}
	}

	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = findViewById(R.id.refreshLayout);
		refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setProgressViewEndTarget(true, 300);
		refreshLayout.setRefreshing(true);
		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (webView != null && !TextUtils.isEmpty(url)) {
					//添加请求头
					Map<String, String> extraHeaders = new HashMap<>();
					extraHeaders.put("Referer", CommonUtil.getRequestHeader());
					webView.loadUrl(url, extraHeaders);
				}
			}
		});
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		reTitle = findViewById(R.id.reTitle);
		tvContent = findViewById(R.id.tvContent);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			if (title.length() > 10) {
				tvTitle.setText("视频点播");
				tvContent.setText(title);
				tvContent.setVisibility(View.VISIBLE);
			}else {
				tvTitle.setText(title);
				tvContent.setVisibility(View.GONE);
			}
		}
	}
	
	/**
	 * 初始化webview
	 */
	private void initWebView() {
		url = getIntent().getStringExtra(CONST.WEB_URL);
		if (TextUtils.isEmpty(url)) {
			return;
		}
		
		webView = findViewById(R.id.webView);
		WebSettings webSettings = webView.getSettings();
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
		
		//添加请求头
		Map<String, String> extraHeaders = new HashMap<>();
		extraHeaders.put("Referer", CommonUtil.getRequestHeader());
		webView.loadUrl(url, extraHeaders);
		
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
//				if (title != null) {
//					tvTitle.setText(title);
//				}
			}
		});
		
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String itemUrl) {
				url = itemUrl;
				Map<String, String> extraHeaders = new HashMap<>();
				extraHeaders.put("Referer", CommonUtil.getRequestHeader());
				webView.loadUrl(url, extraHeaders);
				return true;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				refreshLayout.setRefreshing(false);
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (webView != null) {
			webView.reload();
		}
	}

	private void exit() {
		if (configuration == null) {
			finish();
		}else {
			if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
				finish();
			}else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
		}
		return false;
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
