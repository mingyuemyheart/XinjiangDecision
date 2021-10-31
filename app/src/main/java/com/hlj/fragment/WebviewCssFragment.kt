package com.hlj.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings.LayoutAlgorithm
import android.webkit.WebViewClient
import com.hlj.common.CONST
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import kotlinx.android.synthetic.main.fragment_webview.*
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
class WebviewCssFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_webview, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidget()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        val url = arguments!!.getString(CONST.WEB_URL)
        if (!TextUtils.isEmpty(url)) {
            okHttpDetail(url)
        }
    }

    /**
     * 获取详情
     */
    private fun okHttpDetail(url: String) {
        Thread {
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    if (!isAdded) {
                        return
                    }
                    val result = response.body!!.string()
                    activity!!.runOnUiThread {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("content")) {
                                    val content = obj.getString("content")
                                    initWebView(content)
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })
        }.start()
    }

    /**
     * 初始化webview
     */
    private fun initWebView(content: String) {
        if (TextUtils.isEmpty(content)) {
            return
        }
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
        val css = CommonUtil.getFromAssets(activity, "p_item.css")
        val style = "<meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0,user-scalable=no\" /><style>$css</style>"
        webView!!.loadDataWithBaseURL("", style+content, "text/html", "utf-8", "")
        webView!!.webViewClient = object : WebViewClient() {
        }
    }

}
