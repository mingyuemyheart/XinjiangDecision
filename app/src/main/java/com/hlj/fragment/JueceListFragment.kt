package com.hlj.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.LinearLayout
import android.widget.TextView
import com.hlj.activity.PDFActivity
import com.hlj.activity.WebviewActivity
import com.hlj.adapter.JueceListAdapter
import com.hlj.common.CONST
import com.hlj.common.ColumnData
import com.hlj.dto.AgriDto
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import kotlinx.android.synthetic.main.fragment_juece_list.*
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
 * 决策服务
 */
class JueceListFragment : BaseFragment() {

    private var dataUrl = ""
    private var mAdapter: JueceListAdapter? = null
    private val dataList: ArrayList<AgriDto> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_juece_list, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        if (data != null) {
            llContainer.removeAllViews()
            val dataList: ArrayList<ColumnData> = ArrayList()
            if (data.child.isEmpty()) {
                dataList.add(data)
            } else {
                dataList.addAll(data.child)
            }

            for (i in 0 until dataList.size) {
                val dto = dataList[i]
                val tv = TextView(activity)
                tv.text = dto.name
                if (dto.dataUrl != null) {
                    tv.tag = dto.dataUrl
                }
                tv.gravity = Gravity.CENTER
                tv.setPadding(25, 0, 25, 0)
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                if (i == 0) {
                    tv.setTextColor(Color.WHITE)
                    tv.setBackgroundResource(R.drawable.corner_left_right_blue)
                    if (dto.dataUrl != null) {
                        dataUrl = dto.dataUrl
                    }
                    okHttpList()
                } else {
                    tv.setTextColor(ContextCompat.getColor(activity!!, R.color.text_color4))
                    tv.setBackgroundResource(R.drawable.corner_left_right_gray)
                }
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.leftMargin = CommonUtil.dip2px(activity, 10f).toInt()
                tv.layoutParams = params
                llContainer.addView(tv)

                tv.setOnClickListener { v ->
                    val name: String = (v!! as TextView).text.toString()
                    for (j in 0 until llContainer.childCount) {
                        val tvName = llContainer.getChildAt(j) as TextView
                        if (TextUtils.equals(tvName.text.toString(), name)) {
                            tvName.setTextColor(Color.WHITE)
                            tvName.setBackgroundResource(R.drawable.corner_left_right_blue)
                            if (dto.dataUrl != null) {
                                dataUrl = v.tag.toString()
                            }
                            okHttpList()
                        } else {
                            tvName.setTextColor(ContextCompat.getColor(activity!!, R.color.text_color4))
                            tvName.setBackgroundResource(R.drawable.corner_left_right_gray)
                        }
                    }
                }
            }
        }
    }

    private fun initListView() {
        mAdapter = JueceListAdapter(activity, dataList)
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
                                            if (!itemObj.isNull("l5")) {
                                                dto.icon = itemObj.getString("l5")
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
