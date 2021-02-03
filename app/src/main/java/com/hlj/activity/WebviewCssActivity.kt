package com.hlj.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.webkit.WebSettings.LayoutAlgorithm
import android.webkit.WebViewClient
import com.hlj.common.CONST
import com.hlj.utils.CommonUtil
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
 * css webview
 */
class WebviewCssActivity : BaseActivity(), OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        initWidget()
    }

    private fun refresh() {
        val url = intent.getStringExtra(CONST.WEB_URL)
        if (!TextUtils.isEmpty(url)) {
            okHttpDetail(url)
        }
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack!!.setOnClickListener(this)
        val title: String = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        } else {
            tvTitle!!.text = "详情"
        }
        refresh()
    }

    /**
     * 获取详情
     */
    private fun okHttpDetail(url: String) {
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
                                val type = obj.getString("type")
                                val c1 = obj.getString("c1")
                                val c2 = obj.getString("c2")
                                val c3 = obj.getString("c3")
                                val c4 = obj.getString("c4")
                                val c7 = obj.getString("c7")
                                initWebView(type, c1, c2, c3, c4, c7)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })
        }).start()
    }

    /**
     * 初始化webview
     */
    private fun initWebView(type: String, c1: String, c2: String, c3: String, c4: String, c7: String) {
        val webSettings = webView!!.settings
        //支持javascript
        webSettings.javaScriptEnabled = true
        // 设置可以支持缩放
        webSettings.setSupportZoom(true)
        // 设置出现缩放工具
        webSettings.builtInZoomControls = true
        //扩大比例的缩放
        webSettings.useWideViewPort = true
        //自适应屏幕
        webSettings.layoutAlgorithm = LayoutAlgorithm.SINGLE_COLUMN
        webSettings.loadWithOverviewMode = true
        //		webView.loadUrl(url);
        val css = CommonUtil.getFromAssets(this, "p_item.css")
        val style = "<meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0,user-scalable=no\" /><style>$css</style>"
        var html = "<div class=\"c_content $type\">"
        if (TextUtils.equals(type, "tw")) {
            html += "<div class=\"c_title\"><h1>$c1</h1></div>"
            html += "<div class=\"c_text\">$c2</div><span class=\"c_copyright\">$c3</span></div>"
        } else if (TextUtils.equals(type, "jcbw")) {
            html += "<div class=\"c_title c_f_red\"><h2>$c1</h2><div class=\"c_postil\"><span class=\"fl\">$c3</span><span class=\"fr\">$c4</span></div></div><div class=\"c_text\">$c7</div></div>"
        }
        html = style + html
        webView!!.loadDataWithBaseURL("", html, "text/html", "utf-8", "")
        webView!!.webViewClient = object : WebViewClient() {
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
        when (v.id) {
            R.id.llBack -> {
                finish()
            }
        }
    }

}
