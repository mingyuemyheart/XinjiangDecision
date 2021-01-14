package com.hlj.activity

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebSettings.LayoutAlgorithm
import android.webkit.WebView
import android.webkit.WebViewClient
import com.hlj.common.CONST
import com.hlj.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_webview.*
import kotlinx.android.synthetic.main.layout_title2.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import shawn.cxwl.com.hlj.R
import java.io.IOException

/**
 * 普通网页
 */
class WebviewVideoActivity : BaseActivity(), OnClickListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw()
        }
        setContentView(R.layout.activity_webview)
        initWidget()
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

        val url = "https://decision-admin.tianqi.cn/Home/work2019/hlg_getVideos"
        okHttpList(url)
    }

    /**
     * 初始化webview
     */
    private fun initWebView(url: String) {
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
        webView!!.loadUrl(url)

        webView!!.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
            }
        }
        webView!!.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                callback.invoke(origin, true, false)
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

    private fun okHttpList(url: String) {
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                var type: String? = null
                                if (!obj.isNull("type")) {
                                    type = obj.getString("type")
                                }
                                if (!obj.isNull("l")) {
                                    val array = obj.getJSONArray("l")
                                    if (array.length() > 0) {
                                        val itemObj = array.getJSONObject(0)
                                        if (!itemObj.isNull("l1")) {
                                            val title = itemObj.getString("l1")
                                            if (title != null) {
                                                tvTitle!!.text = title
                                            }
                                        }
                                        if (!itemObj.isNull("l2")) {
                                            val dataUrl = itemObj.getString("l2")
                                            initWebView(dataUrl)
                                        }
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })
        }).start()
    }
	
}
