package com.hlj.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import com.hlj.activity.PDFActivity
import com.hlj.activity.WebviewActivity
import com.hlj.adapter.WeatherVideoAdapter
import com.hlj.common.CONST
import com.hlj.common.ColumnData
import com.hlj.dto.AgriDto
import com.hlj.utils.OkHttpUtil
import kotlinx.android.synthetic.main.fragment_weather_video.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import shawn.cxwl.com.hlj.R
import java.io.IOException

/**
 * 天气视频
 */
class WeatherVideoFragment : BaseFragment() {

    private var mReceiver: MyBroadCastReceiver? = null
    private var mAdapter: WeatherVideoAdapter? = null
    private val dataList: ArrayList<AgriDto> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_weather_video, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBroadCast()
    }

    private fun initBroadCast() {
        mReceiver = MyBroadCastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(arguments!!.getString(CONST.BROADCAST_ACTION))
        activity!!.registerReceiver(mReceiver, intentFilter)
    }

    private inner class MyBroadCastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            refresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mReceiver != null) {
            activity!!.unregisterReceiver(mReceiver)
        }
    }

    private fun refresh() {
        initRefreshLayout()
        initWidget()
        initListView()
    }

    /**
     * 初始化下拉刷新布局
     */
    private fun initRefreshLayout() {
        refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4)
        refreshLayout.setProgressViewEndTarget(true, 400)
        refreshLayout.isRefreshing = true
        refreshLayout.setOnRefreshListener { okHttpList() }
    }

    private fun initWidget() {
        val data: ColumnData = arguments!!.getParcelable("data")
        okHttpList()
    }

    private fun initListView() {
        mAdapter = WeatherVideoAdapter(activity, dataList)
        listView!!.adapter = mAdapter
        listView!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = dataList[arg2]
            val intent = when {
                TextUtils.equals(dto.type, CONST.PDF) -> {
                    Intent(activity, PDFActivity::class.java)
                }
                TextUtils.equals(dto.type, CONST.MP4) -> {
                    Intent(activity, WebviewActivity::class.java)
                }
                else -> {
                    Intent(activity, WebviewActivity::class.java)
                }
            }
            intent.putExtra(CONST.ACTIVITY_NAME, dto.title)
            intent.putExtra(CONST.WEB_URL, dto.dataUrl)
            startActivity(intent)
        }
    }

    private fun okHttpList() {
        val dataUrl = arguments!!.getString(CONST.WEB_URL)
        refreshLayout.isRefreshing = true
        if (TextUtils.isEmpty(dataUrl)) {
            refreshLayout.isRefreshing = false
            return
        }
        Thread {
            Log.e("dataUrl", dataUrl)
            OkHttpUtil.enqueue(Request.Builder().url(dataUrl).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    activity!!.runOnUiThread {
                        refreshLayout!!.isRefreshing = false
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                dataList.clear()
                                val ob = JSONTokener(result).nextValue()
                                if (ob is JSONObject) {
                                    val obj = JSONObject(result)
                                    var type: String? = null
                                    if (!obj.isNull("type")) {
                                        type = obj.getString("type")
                                    }
                                    if (!obj.isNull("l")) {
                                        val array = obj.getJSONArray("l")
                                        for (i in 0 until array.length()) {
                                            val itemObj = array.getJSONObject(i)
                                            val dto = AgriDto()
                                            if (!itemObj.isNull("l1")) {
                                                dto.title = itemObj.getString("l1")
                                            }
                                            if (!itemObj.isNull("l2")) {
                                                dto.dataUrl = itemObj.getString("l2")
                                            }
                                            if (!itemObj.isNull("l3")) {
                                                dto.time = itemObj.getString("l3")
                                            }
                                            if (!itemObj.isNull("l4")) {
                                                dto.icon = itemObj.getString("l4")
                                            }
                                            dto.type = type
                                            if (dto.dataUrl.endsWith(".pdf") || dto.dataUrl.endsWith(".PDF")) {
                                                dto.type = CONST.PDF
                                            }
                                            dataList.add(dto)
                                        }
                                    }
                                } else if (ob is JSONArray) {
                                    val array = JSONArray(result)
                                    for (i in 0 until array.length()) {
                                        val dto = AgriDto()
                                        val itemObj = array.getJSONObject(i)
                                        if (!itemObj.isNull("destfile")) {
                                            dto.dataUrl = itemObj.getString("destfile")
                                        }
                                        if (!itemObj.isNull("filetime")) {
                                            dto.time = itemObj.getString("filetime")
                                        }
                                        if (!itemObj.isNull("icon")) {
                                            dto.icon = itemObj.getString("icon")
                                        }
                                        if (!itemObj.isNull("title")) {
                                            dto.title = itemObj.getString("title")
                                        }
                                        if (dto.dataUrl.endsWith(".pdf") || dto.dataUrl.endsWith(".PDF")) {
                                            dto.type = CONST.PDF
                                        }
                                        dataList.add(dto)
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
        }.start()
    }

}
