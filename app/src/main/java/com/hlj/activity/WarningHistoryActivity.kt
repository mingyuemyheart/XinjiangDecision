package com.hlj.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ExpandableListView.OnChildClickListener
import android.widget.ExpandableListView.OnGroupClickListener
import com.hlj.adapter.WarningHistoryAdapter
import com.hlj.dto.WarningDto
import com.hlj.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_warning_history.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import shawn.cxwl.com.hlj.R
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 天气预警-历史预警
 */
class WarningHistoryActivity : BaseActivity(), View.OnClickListener {

    private val sdf1 = SimpleDateFormat("yyyy年MM月dd HH:mm:ss", Locale.CHINA)
    private val sdf4 = SimpleDateFormat("yyyy0101000000", Locale.CHINA)
    private val sdf5 = SimpleDateFormat("yyyyMMdd000000", Locale.CHINA)
    private val sdf6 = SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA)
    private var mAdapter: WarningHistoryAdapter? = null
    private val groupList: MutableList<WarningDto> = ArrayList()
    private val childList: MutableList<MutableList<WarningDto>> = ArrayList()
    private var areaName = "全国"
    private var areaId: String? = null
    private var startTime: String? = null
    private var endTime: String? = null
    private val baseUrl = "http://testdecision.tianqi.cn/alarm12379/hisalarmcount.php?format=1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warning_history)
        initWidget()
        initListView()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvTitle.text = "${areaName}预警统计"
        tvControl.setOnClickListener(this)
        tvControl.text = "筛选"
        tvControl.visibility = View.VISIBLE
        startTime = if (intent.hasExtra("startTime")) {
            intent.getStringExtra("startTime")
        } else {
            sdf4.format(Date())
        }
        endTime = if (intent.hasExtra("endTime")) {
            intent.getStringExtra("endTime")
        } else {
            sdf5.format(Date())
        }
        try {
            tvTime.text = "${sdf1.format(sdf6.parse(startTime))} - ${sdf1.format(sdf6.parse(endTime))}"
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        var url = baseUrl
        if (intent.hasExtra("data")) {
            val data: WarningDto = intent.extras.getParcelable("data")
            if (data != null) {
                areaName = data.areaName
                tvTitle.text = areaName + "预警统计"
                if (!TextUtils.isEmpty(data.areaKey)) {
                    areaId = data.areaKey
                    url = String.format("$baseUrl&areaid=%s&starttime=%s&endtime=%s", data.areaKey, startTime, endTime)
                }
            }
        } else {
            areaName = "黑龙江"
            tvTitle.text = "${areaName}预警统计"
            areaId = "23"
            url = String.format("$baseUrl&areaid=%s&starttime=%s&endtime=%s", areaId, startTime, endTime)
        }
        okHttpStatistic(url)
    }

    private fun initListView() {
        mAdapter = WarningHistoryAdapter(this, groupList, childList, listView)
        mAdapter!!.setStartTime(startTime)
        mAdapter!!.setEndTime(endTime)
        listView.setAdapter(mAdapter)
        listView.setOnGroupClickListener(OnGroupClickListener { parent, v, groupPosition, id ->
            val dto = groupList[groupPosition]
            if (TextUtils.equals(dto.areaKey, "all")) { //总计不能点击
                return@OnGroupClickListener true
            }
            val intent = Intent(this, WarningHistoryListActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("data", dto)
            intent.putExtras(bundle)
            startActivity(intent)
            true
        })
        listView.setOnChildClickListener(OnChildClickListener { parent, v, groupPosition, childPosition, id ->
            val dto = childList[groupPosition][childPosition]
            if (TextUtils.equals(dto.areaKey, "all")) { //总计不能点击
                return@OnChildClickListener true
            }
            val intent = Intent(this, WarningHistoryListActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("data", dto)
            intent.putExtras(bundle)
            startActivity(intent)
            false
        })
    }

    /**
     * 获取预警统计信息
     * @param url
     */
    private fun okHttpStatistic(url: String) {
        showDialog()
        if (TextUtils.isEmpty(url)) {
            return
        }
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
                        cancelDialog()
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val `object` = JSONObject(result)
                                if (!`object`.isNull("data")) {
                                    groupList.clear()
                                    childList.clear()
                                    val array = `object`.getJSONArray("data")
                                    for (i in 0 until array.length()) {
                                        val dto = WarningDto()
                                        val obj = array.getJSONObject(i)
                                        if (!obj.isNull("name")) {
                                            dto.areaName = obj.getString("name")
                                        }
                                        if (!obj.isNull("areaid")) {
                                            dto.areaId = obj.getString("areaid")
                                        }
                                        if (!obj.isNull("areaKey")) {
                                            dto.areaKey = obj.getString("areaKey")
                                        }
                                        if (!obj.isNull("count")) {
                                            val count = obj.getString("count").split("|").toTypedArray()
                                            dto.warningCount = count[0]
                                            dto.redCount = count[1]
                                            dto.orangeCount = count[2]
                                            dto.yellowCount = count[3]
                                            dto.blueCount = count[4]
                                        }
                                        if (!obj.isNull("list")) {
                                            val list: MutableList<WarningDto> = ArrayList()
                                            list.clear()
                                            val listArray = obj.getJSONArray("list")
                                            for (j in 0 until listArray.length()) {
                                                val itemObj = listArray.getJSONObject(j)
                                                val d = WarningDto()
                                                d.areaKey = dto.areaKey
                                                if (!itemObj.isNull("name")) {
                                                    d.shortName = itemObj.getString("name")
                                                    d.areaName = dto.areaName + d.shortName
                                                }
                                                if (!itemObj.isNull("type")) {
                                                    d.type = itemObj.getString("type")
                                                }
                                                if (!itemObj.isNull("count")) {
                                                    val count = itemObj.getString("count").split("|").toTypedArray()
                                                    d.warningCount = count[0]
                                                    d.redCount = count[1]
                                                    d.orangeCount = count[2]
                                                    d.yellowCount = count[3]
                                                    d.blueCount = count[4]
                                                }
                                                list.add(d)
                                            }
                                            childList.add(list)
                                        }
                                        groupList.add(dto)
                                    }
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
        }).start()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
            R.id.tvControl -> {
                val intent = Intent(this, WarningHistoryScreenActivity::class.java)
                intent.putExtra("startTime", startTime)
                intent.putExtra("endTime", endTime)
                intent.putExtra("areaName", areaName)
                intent.putExtra("areaId", areaId)
                startActivityForResult(intent, 1000)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1000 -> {
                    areaId = data!!.extras.getString("areaId")
                    areaName = data.extras.getString("areaName")
                    startTime = data.extras.getString("startTime")
                    endTime = data.extras.getString("endTime")
                    mAdapter!!.setStartTime(startTime)
                    mAdapter!!.setEndTime(endTime)
                    tvTitle!!.text = "${areaName}预警统计"
                    try {
                        tvTime.text = "${sdf1.format(sdf6.parse(startTime))} - ${sdf1.format(sdf6.parse(endTime))}"
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                    val url = if (!TextUtils.isEmpty(areaId)) {
                        String.format("$baseUrl&areaid=%s&starttime=%s&endtime=%s", areaId, startTime, endTime)
                    } else {
                        String.format("$baseUrl&starttime=%s&endtime=%s", startTime, endTime)
                    }
                    okHttpStatistic(url)
                }
            }
        }
    }

}
