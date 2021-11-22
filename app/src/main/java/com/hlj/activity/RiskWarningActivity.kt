package com.hlj.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import com.hlj.common.CONST
import com.hlj.utils.OkHttpUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_risk_warning.*
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
 * 专业服务-铁路气象服务-风险预警
 */
class RiskWarningActivity : BaseFragmentActivity(), OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_risk_warning)
        initWidget()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        imageView.setImageResource(R.drawable.icon_no_bitmap)

        if (intent.hasExtra(CONST.ACTIVITY_NAME)) {
            val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
            if (title != null) {
                tvTitle.text = title
            }
        }

        okHttpDetail()
    }

    private fun okHttpDetail() {
        showDialog()
        Thread {
            val url = "http://xinjiangdecision.tianqi.cn:81/Home/api/get_railway_risk"
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        cancelDialog()
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("img")) {
                                    val imgUrl = obj.getString("img")
                                    if (!TextUtils.isEmpty(imgUrl)) {
                                        Picasso.get().load(imgUrl).into(imageView)
                                    }
                                }
                                if (!obj.isNull("title")) {
                                    val name = obj.getString("title")
                                    if (name != null) {
                                        tvName.text = name
                                    }
                                }
                                if (!obj.isNull("content")) {
                                    val content = obj.getString("content")
                                    if (content != null) {
                                        tvContent.text = content
                                    }
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

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> finish()
        }
    }

}
