package com.hlj.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import com.hlj.adapter.RailSectionAdapter
import com.hlj.common.CONST
import com.hlj.dto.FactDto
import com.hlj.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_rail_section.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import shawn.cxwl.com.hlj.R
import java.io.IOException

/**
 * 专业服务-铁路气象服务-实况数据-铁路段
 */
class RailSectionActivity : BaseActivity(), View.OnClickListener {

    private var mAdapter: RailSectionAdapter? = null
    private val dataList: ArrayList<FactDto> = ArrayList()
    private var stationCodes = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rail_section)
        initWidget()
        initGridView()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvTitle.text = "选择铁路沿线"

        okHttpList()
    }

    /**
     * 初始化listview
     */
    private fun initGridView() {
        mAdapter = RailSectionAdapter(this, dataList)
        gridView!!.adapter = mAdapter
        gridView!!.onItemClickListener = AdapterView.OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = dataList[arg2]
            for (i in 0 until dto.itemList.size) {
                val item = dto.itemList[i]
                stationCodes += item.stationCode+","
            }
            val intent = Intent()
            Log.e("stationCodes", stationCodes)
            intent.putExtra("stationName", dto.name)
            intent.putExtra("stationCodes", stationCodes)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun okHttpList() {
        showDialog()
        val localId = intent.getStringExtra(CONST.LOCAL_ID)
        var url = ""
        when(localId) {
            "9101","9102" -> {
                tvTitle.text = "选择铁路段"
                url = "http://xinjiangdecision.tianqi.cn:81/Home/api/get_railway_section"
            }
            "9201","9202" -> {
                tvTitle.text = "选择公路段"
                url = "http://xinjiangdecision.tianqi.cn:81/Home/api/get_highway_section"
            }
            "9301","9302" -> {
                tvTitle.text = "选择电力路段"
                url = "http://xinjiangdecision.tianqi.cn:81/Home/api/get_electricity_section"
            }
        }
        if (TextUtils.isEmpty(url)) {
            cancelDialog()
            return
        }
        Thread {
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        cancelDialog()
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val dataArray = JSONArray(result)
                                dataList.clear()
                                var dto = FactDto()
                                dto.name = "全部站"
                                dataList.add(dto)
                                for (m in 0 until dataArray.length()) {
                                    dto = FactDto()
                                    val itemObj = dataArray.getJSONObject(m)
                                    if (!itemObj.isNull("name")) {
                                        dto.name = itemObj.getString("name")
                                    }
                                    if (!itemObj.isNull("ids")) {
                                        val itemList: ArrayList<FactDto> = ArrayList()
                                        val ids = itemObj.getJSONArray("ids")
                                        for (i in 0 until ids.length()) {
                                            val d = FactDto()
                                            d.stationCode = ids.getString(i)
                                            itemList.add(d)
                                        }
                                        dto.itemList.addAll(itemList)
                                    }
                                    dataList.add(dto)
                                }
                                if (mAdapter != null) {
                                    mAdapter!!.notifyDataSetChanged()
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
        when(v!!.id) {
            R.id.llBack -> finish()
        }
    }

}
