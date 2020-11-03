package com.hlj.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.hlj.adapter.HanghuaDetailAdapter
import com.hlj.common.CONST
import com.hlj.dto.StationMonitorDto
import com.hlj.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_hanghua_detail.*
import kotlinx.android.synthetic.main.activity_hanghua_list.listView
import kotlinx.android.synthetic.main.activity_hanghua_list.refreshLayout
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import shawn.cxwl.com.hlj.R
import java.io.IOException

/**
 * 航化作业详情
 */
class HanghuaDetailActivity : BaseActivity(), View.OnClickListener {

    private var mAdapter: HanghuaDetailAdapter? = null
    private val dataList: ArrayList<StationMonitorDto> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hanghua_detail)
        initRefreshLayout()
        initWidget()
        initListView()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvTitle.setOnClickListener(this)

        val title: String = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }

        refresh()
    }

    private fun refresh() {
        okHttpList()
    }

    /**
     * 初始化下拉刷新布局
     */
    private fun initRefreshLayout() {
        refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4)
        refreshLayout.setProgressViewEndTarget(true, 400)
        refreshLayout.isRefreshing = true
        refreshLayout.setOnRefreshListener { refresh() }
    }

    private fun initListView() {
        mAdapter = HanghuaDetailAdapter(this, dataList)
        listView!!.adapter = mAdapter
    }

    private fun okHttpList() {
        Thread(Runnable {
            val lat = intent.getDoubleExtra(CONST.LAT, 0.0)
            val lng = intent.getDoubleExtra(CONST.LNG, 0.0)
            val url = "http://decision-admin.tianqi.cn/Home/work2019/hlj_gethanghuoData?lat=$lat&lon=$lng"
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        refreshLayout.isRefreshing = false
                        if (!TextUtils.isEmpty(result)) {
                            val obj = JSONObject(result)
                            if (!obj.isNull("msg")) {
                                val msg = obj.getString("msg")
                                if (!TextUtils.isEmpty(msg)) {
                                    tvName.text = msg
                                }
                            }
                            if (!obj.isNull("sunrise")) {
                                val sunrise = obj.getString("sunrise")
                                if (!TextUtils.isEmpty(sunrise)) {
                                    tvName.text = "${tvName.text}\n日升日落:$sunrise"
                                }
                            }
                            if (!obj.isNull("sunset")) {
                                val sunset = obj.getString("sunset")
                                if (!TextUtils.isEmpty(sunset)) {
                                    tvName.text = "${tvName.text}|$sunset"
                                }
                            }
                            if (!obj.isNull("data")) {
                                dataList.clear()
                                val array = obj.getJSONArray("data")
                                for (i in 0 until array.length()) {
                                    val dto = StationMonitorDto()
                                    val itemObj = array.getJSONObject(i)
                                    if (!itemObj.isNull("title")) {
                                        dto.name = itemObj.getString("title")
                                    }
                                    if (!itemObj.isNull("desc")) {
                                        dto.value = itemObj.getString("desc")
                                    }
                                    dataList.add(dto)
                                }
                                if (mAdapter != null) {
                                    mAdapter!!.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }
            })
        }).start()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> {
                finish()
            }
        }
    }

}
