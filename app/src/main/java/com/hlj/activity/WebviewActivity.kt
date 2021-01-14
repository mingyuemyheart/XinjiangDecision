package com.hlj.activity

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebChromeClient.CustomViewCallback
import android.webkit.WebSettings.LayoutAlgorithm
import android.webkit.WebView
import android.webkit.WebViewClient
import com.hlj.common.CONST
import com.hlj.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_webview.*
import kotlinx.android.synthetic.main.fact_weather_detail.*
import kotlinx.android.synthetic.main.layout_title2.*
import shawn.cxwl.com.hlj.R
import java.util.*


/**
 * 普通网页
 */
class WebviewActivity : BaseActivity(), OnClickListener{

    private var mCustomView: View? = null //用于全屏渲染视频的View
    private var mCustomViewCallback: CustomViewCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw()
        }
        setContentView(R.layout.activity_webview)
        initWidget()
        initWebView()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack!!.setOnClickListener(this)
        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (title != null) {
            tvTitle!!.text = title
        }
    }

    /**
     * 初始化webview
     */
    private fun initWebView() {
        val url = intent.getStringExtra(CONST.WEB_URL)
        val webSettings = webView!!.settings
        //支持javascript

        //支持javascript
        webSettings.javaScriptEnabled = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.domStorageEnabled = true
        webSettings.setGeolocationEnabled(true)
        // 设置可以支持缩放
        webSettings.setSupportZoom(true)
        // 设置出现缩放工具
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        //扩大比例的缩放
        webSettings.useWideViewPort = true
        //自适应屏幕
        webSettings.layoutAlgorithm = LayoutAlgorithm.SINGLE_COLUMN
        webSettings.loadWithOverviewMode = true

        //添加请求头
        val extraHeaders: MutableMap<String, String> = HashMap()
        extraHeaders["Referer"] = CommonUtil.getRequestHeader()
        webView!!.loadUrl(url, extraHeaders)
        webView!!.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
            }
        }
        webView!!.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                callback.invoke(origin, true, false)
            }

            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                super.onShowCustomView(view, callback)
                //如果view 已经存在，则隐藏
                if (mCustomView != null) {
                    callback.onCustomViewHidden()
                    return
                }
                mCustomView = view
                mCustomView!!.visibility = View.VISIBLE
                mCustomViewCallback = callback
                mLayout.addView(mCustomView)
                mLayout.visibility = View.VISIBLE
                mLayout.bringToFront()

                //设置横屏
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }

            override fun onHideCustomView() {
                super.onHideCustomView()
                if (mCustomView == null) {
                    return
                }
                mCustomView!!.visibility = View.GONE
                mLayout.removeView(mCustomView)
                mCustomView = null
                mLayout.visibility = View.GONE
                try {
                    mCustomViewCallback!!.onCustomViewHidden()
                } catch (e: Exception) {
                }
                //                titleView.setVisibility(View.VISIBLE);
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT //竖屏
            }
        }
        webView!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, itemUrl: String): Boolean {
                webView!!.loadUrl(itemUrl)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView != null && webView!!.canGoBack()) {
                webView!!.goBack()
                return true
            } else {
                finish()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.llBack) {
            finish()
        }
    }

    /**
     * 横竖屏切换监听
     */
    override fun onConfigurationChanged(config: Configuration) {
        super.onConfigurationChanged(config)
        when (config.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//                mToolbar.setVisibility(View.GONE)
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
//                mToolbar.setVisibility(View.VISIBLE)
            }
        }
    }
	
}
