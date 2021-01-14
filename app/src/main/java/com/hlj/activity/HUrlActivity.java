package com.hlj.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hlj.common.CONST;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.OkHttpUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
 */
public class HUrlActivity extends BaseActivity implements OnClickListener{
	
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private WebView webView = null;
	private WebSettings webSettings = null;
	private String url = null;
	private String fileDir = Environment.getExternalStorageDirectory()+"/HLJ";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.url);
		showDialog();
		initWidget();
		initWebView();
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

		String columnId = getIntent().getStringExtra(CONST.COLUMN_ID);
		CommonUtil.submitClickCount(columnId, title);
	}

	/**
	 * 初始化webview
	 */
	private void initWebView() {
		url = getIntent().getStringExtra(CONST.WEB_URL);
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
//		webView.loadUrl(url);
		
		//添加请求头
		Map<String, String> extraHeaders = new HashMap<String, String>();
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
//				webView.loadUrl(url);
				
				//添加请求头
				Map<String, String> extraHeaders = new HashMap<String, String>();
				extraHeaders.put("Referer", CommonUtil.getRequestHeader());
				webView.loadUrl(url, extraHeaders);
				return true;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				cancelDialog();
			}
		});

		webView.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
				String fileName = contentDisposition.substring(contentDisposition.indexOf("\"")+1, contentDisposition.lastIndexOf("\""));
				OkHttpFile(url, fileName);
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

	private void OkHttpFile(final String url, final String fileName) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
		Log.e("OkHttpFile url:", url);
		showDialog();
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
						String filePath = null;
						InputStream is = null;
						FileOutputStream fos = null;
						try {
							is = response.body().byteStream();//获取输入流
							float total = response.body().contentLength();//获取文件大小
							if(is != null){
								File files = new File(fileDir);
								if (!files.exists()) {
									files.mkdirs();
								}
								filePath = files.getAbsolutePath()+"/"+fileName;
								fos = new FileOutputStream(filePath);
								byte[] buf = new byte[1024];
								int ch = -1;
								int process = 0;
								while ((ch = is.read(buf)) != -1) {
									fos.write(buf, 0, ch);
									process += ch;

//									int percent = (int) Math.floor((process / total * 100));
//									Log.e("percent", process+"--"+total+"--"+percent);
//									Message msg = handler.obtainMessage(1001);
//									msg.what = 1001;
//									msg.obj = filePath;
//									msg.arg1 = percent;
//									handler.sendMessage(msg);
								}
							}
							fos.flush();
							fos.close();// 下载完成

							Message msg = handler.obtainMessage(1001);
							msg.what = 1001;
							msg.obj = filePath;
							handler.sendMessage(msg);

						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (is != null) {
								is.close();
							}
							if (fos != null) {
								fos.close();
							}
						}

					}
				});
			}
		}).start();
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1001) {
				cancelDialog();
				String filePath = String.valueOf(msg.obj);
				if (TextUtils.isEmpty(filePath)) {
					Toast.makeText(HUrlActivity.this, "文件下载失败，请点击重新下载", Toast.LENGTH_LONG).show();
				}else {
					CommonUtil.intentWPSOffice(HUrlActivity.this, filePath);
				}
			}
		}
	};
	
}
