package com.hlj.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.hlj.common.ColumnData;

import shawn.cxwl.com.hlj.decision.R;

public class WebViewFragment extends Fragment {
	
	private WebView webView = null;
	private String url = null;
	private ColumnData channel = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.webview_fragment, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWebView(view);
	}
	
	/**
	 * 初始化webview
	 */
	private void initWebView(View view) {
		channel = getArguments().getParcelable("data");
		if (channel != null) {
			url = channel.dataUrl;
			if (url == null) {
				return;
			}
			webView = (WebView) view.findViewById(R.id.webView);
			webView.loadUrl(url);
			
			WebChromeClient wcc = new WebChromeClient() {
				@Override
				public void onReceivedTitle(WebView view, String title) {
					super.onReceivedTitle(view, title);
				}
			};
			webView.setWebChromeClient(wcc);
			
			WebViewClient wvc = new WebViewClient(){
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String itemUrl) {
					url = itemUrl;
					webView.loadUrl(url);
					return true;
				}
			};
			
			webView.setWebViewClient(wvc);
		}
	}
	
}
