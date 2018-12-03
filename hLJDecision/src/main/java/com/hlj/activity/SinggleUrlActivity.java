package com.hlj.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlj.dto.AgriDto;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

/**
 * 普通url，处理网页界面
 * @author shawn_sun
 *
 */

public class SinggleUrlActivity extends BaseActivity implements OnClickListener{
	
	private WebView webView;
	private String dataUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.url2);
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		LinearLayout llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
		
		refresh();
	}

	private void refresh() {
		AgriDto dto = getIntent().getExtras().getParcelable("data");
		if (dto != null) {
			if (!TextUtils.isEmpty(dto.dataUrl)) {
				OkHttpDetail(dto.dataUrl);
			}
		}
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
									try {
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("info")) {
											JSONArray array = obj.getJSONArray("info");
											if (array.length() > 0) {
												dataUrl = array.getString(0);
												if (!TextUtils.isEmpty(dataUrl)) {
													initWebView();
												}
											}

										}

									} catch (JSONException e) {
										e.printStackTrace();
									}
								}

								cancelDialog();
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
		if (dataUrl == null) {
			return;
		}
		webView = (WebView) findViewById(R.id.webView);
		WebSettings webSettings = webView.getSettings();
		
		//支持javascript
		webSettings.setJavaScriptEnabled(true); 
		// 设置可以支持缩放 
		webSettings.setSupportZoom(true); 
		// 设置出现缩放工具 
		webSettings.setBuiltInZoomControls(true);
		webSettings.setDisplayZoomControls(false);
		//扩大比例的缩放
		webSettings.setUseWideViewPort(true);
		//自适应屏幕
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webSettings.setLoadWithOverviewMode(true);
//		webView.loadUrl(url);
		
		//添加请求头
		Map<String, String> extraHeaders = new HashMap<String, String>();
		extraHeaders.put("Referer", CommonUtil.getRequestHeader());
		webView.loadUrl(dataUrl, extraHeaders);
		
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
				dataUrl = itemUrl;
//				webView.loadUrl(url);
				//添加请求头
				Map<String, String> extraHeaders = new HashMap<String, String>();
				extraHeaders.put("Referer", CommonUtil.getRequestHeader());
				webView.loadUrl(dataUrl, extraHeaders);
				return true;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
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
