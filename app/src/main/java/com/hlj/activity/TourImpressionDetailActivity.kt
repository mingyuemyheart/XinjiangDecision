package com.hlj.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.webkit.WebSettings
import android.webkit.WebViewClient
import com.hlj.common.CONST
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_tour_impression_detail.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import shawn.cxwl.com.hlj.R
import java.io.IOException

/**
 * 旅游气象-新疆印象-详情
 */
class TourImpressionDetailActivity : BaseActivity(), OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_impression_detail)
        initWidget()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)

        if (intent.hasExtra(CONST.ACTIVITY_NAME)) {
            val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
            if (title != null) {
                tvTitle.text = title
            }
        }

        okHttpDetail()
    }

    private fun okHttpDetail() {
        if (!intent.hasExtra("id")) {
            return
        }
        val id = intent.getStringExtra("id")
        if (TextUtils.isEmpty(id)) {
            return
        }
        Thread {
            val url = "http://xinjiangdecision.tianqi.cn:81/home/api/get_travel_culture_details?id=$id"
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
                                if (!obj.isNull("title")) {
                                    val title = obj.getString("title")
                                    if (title != null) {
                                        tvName.text = title
                                    }
                                }
                                if (!obj.isNull("type")) {
                                    when(obj.getString("type")) {
                                        "1" -> ivType.setImageResource(R.drawable.icon_impre_culture)
                                        "2" -> ivType.setImageResource(R.drawable.icon_impre_food)
                                        "3" -> ivType.setImageResource(R.drawable.icon_impre_grass)
                                        "4" -> ivType.setImageResource(R.drawable.icon_impre_beatifull)
                                    }
                                }
                                if (!obj.isNull("addtime")) {
                                    val addtime = obj.getString("addtime")
                                    if (addtime != null) {
                                        tvTime.text = addtime
                                    }
                                }
                                if (!obj.isNull("desc")) {
                                    val desc = obj.getString("desc")
                                    if (desc != null) {
                                        tvDesc.text = desc
                                    }
                                }
                                if (!obj.isNull("img")) {
                                    val imgUrl = obj.getString("img")
                                    if (!TextUtils.isEmpty(imgUrl)) {
                                        Picasso.get().load(imgUrl).into(imageView)
                                        imageView.visibility = View.VISIBLE
                                    }
                                }
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
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        webSettings.loadWithOverviewMode = true
        val css = CommonUtil.getFromAssets(this, "p_item.css")
        val style = "<meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0,user-scalable=no\" /><style>$css</style>"
        webView!!.loadDataWithBaseURL("", style+content, "text/html", "utf-8", "")
        webView!!.webViewClient = object : WebViewClient() {
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> finish()
        }
    }

}
