package com.hlj.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemClickListener
import com.hlj.adapter.WarningHistoryScreenAreaAdapter
import com.hlj.dto.WarningDto
import com.hlj.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_warning_history_screen_area.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import shawn.cxwl.com.hlj.R
import java.io.IOException
import java.util.*

/**
 * 天气预警-历史预警-预警筛选-选择区域
 */
class WarningHistoryScreenAreaActivity : BaseActivity(), OnClickListener {

    //搜索城市后的结果列表
    private var cityAdapter: WarningHistoryScreenAreaAdapter? = null
    private val cityList: MutableList<WarningDto> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warning_history_screen_area)
        initWidget()
        initListView()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvTitle.text = "选择区域"
        okHttpArea()
    }

    /**
     * 初始化listview
     */
    private fun initListView() {
        cityAdapter = WarningHistoryScreenAreaAdapter(this, cityList)
        listView.adapter = cityAdapter
        listView.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val data = cityList[arg2]
            val intent = Intent()
            intent.putExtra("areaName", data.areaName)
            intent.putExtra("areaId", data.areaId)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun okHttpArea() {
        Thread(Runnable {
            val url = "http://testdecision.tianqi.cn/alarm12379/hisgrepcityclild.php?k=新疆"
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
                                cityList.clear()
                                val array = JSONArray(result)
                                for (i in 0 until array.length()) {
                                    val itemObj = array.getJSONObject(i)
                                    var areaid = ""
                                    if (!itemObj.isNull("areaid")) {
                                        areaid = itemObj.getString("areaid")
                                    }
                                    var names = ""
                                    if (!itemObj.isNull("names")) {
                                        val nameArray = itemObj.getJSONArray("names")
                                        for (j in 0 until nameArray.length()) {
                                            names = if (TextUtils.isEmpty(names)) {
                                                names + nameArray.getString(j)
                                            } else {
                                                names + "-" + nameArray.getString(j)
                                            }
                                        }
                                        var dto = WarningDto()
                                        dto.areaName = names
                                        when {
                                            nameArray.length() == 1 -> {
                                                dto.areaId = areaid.substring(0, 2)
                                            }
                                            nameArray.length() == 2 -> {
                                                dto.areaId = areaid.substring(0, 4)
                                            }
                                            nameArray.length() == 3 -> {
                                                dto.areaId = areaid
                                            }
                                        }
                                        cityList.add(dto)
                                        if (!itemObj.isNull("child")) {
                                            val childArray = itemObj.getJSONArray("child")
                                            for (j in 0 until childArray.length()) {
                                                val childObj = childArray.getJSONObject(j)
                                                dto = WarningDto()
                                                if (!childObj.isNull("name")) {
                                                    dto.areaName = names + "-" + childObj.getString("name")
                                                }
                                                if (!childObj.isNull("areaid")) {
                                                    dto.areaId = childObj.getString("areaid").substring(0, 4)
                                                }
                                                cityList.add(dto)
                                            }
                                        }
                                    }
                                }
                                if (cityAdapter != null) {
                                    cityAdapter!!.notifyDataSetChanged()
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
        }
    }

}
