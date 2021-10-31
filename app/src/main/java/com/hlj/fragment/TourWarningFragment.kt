package com.hlj.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import com.hlj.activity.WarningDetailActivity
import com.hlj.adapter.WarningAdapter
import com.hlj.common.CONST
import com.hlj.dto.WarningDto
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import kotlinx.android.synthetic.main.fragment_tour_warning.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import shawn.cxwl.com.hlj.R
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 旅游气象-预警
 */
class TourWarningFragment : BaseFragment() {

    private val warningList: MutableList<WarningDto?> = ArrayList()
    private var warningAdapter: WarningAdapter? = null
    private val sdf3 = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    private val warningTypes: HashMap<String, WarningDto?> = HashMap()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tour_warning, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidget()
        initListView()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        okHttpWarning()
    }

    /**
     * 获取预警信息
     */
    private fun okHttpWarning() {
        Thread {
            val title = arguments!!.getString(CONST.ACTIVITY_NAME)
            val warningId = arguments!!.getString("warningId")
            val url = "https://decision-admin.tianqi.cn/Home/work2019/getwarns?areaid=${warningId}"
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    activity!!.runOnUiThread {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                warningList.clear()
                                warningTypes.clear()
                                val obj = JSONObject(result)
                                if (!obj.isNull("data")) {
                                    val jsonArray = obj.getJSONArray("data")
                                    for (i in 0 until jsonArray.length()) {
                                        val tempArray = jsonArray.getJSONArray(i)
                                        val dto = WarningDto()
                                        dto.html = tempArray.getString(1)
                                        val array = dto.html.split("-").toTypedArray()
                                        val item0 = array[0]
                                        val item1 = array[1]
                                        val item2 = array[2]
                                        dto.item0 = item0
                                        dto.provinceId = item0.substring(0, 2)
                                        dto.type = item2.substring(0, 5)
                                        dto.color = item2.substring(5, 7)
                                        dto.time = item1
                                        dto.lng = tempArray.getDouble(2)
                                        dto.lat = tempArray.getDouble(3)
                                        dto.name = tempArray.getString(0)
                                        if (!dto.name.contains("解除")) {
                                            warningList.add(dto)
                                        }
                                        warningTypes[dto.type] = dto
                                    }
                                    if (warningAdapter != null) {
                                        warningAdapter!!.notifyDataSetChanged()
                                    }
                                    if (warningList.isEmpty()) {
                                        clNoWarning.visibility = View.VISIBLE
                                    } else {
                                        clNoWarning.visibility = View.GONE
                                    }
                                }
                                try {
                                    var time = ""
                                    if (!obj.isNull("time")) {
                                        val t = obj.getLong("time")
                                        time = sdf3.format(Date(t * 1000))
                                    }
                                    tvTime.text = "${time}更新"

                                    var strType = ""
                                    for (entry in warningTypes.entries) {
                                        var name = ""
                                        var redCount = 0
                                        var orangeCount = 0
                                        var yellowCount = 0
                                        var blueCount = 0
                                        var strRed = ""
                                        var strOrange = ""
                                        var strYellow = ""
                                        var strBlue = ""

                                        for (m in 0 until warningList.size) {
                                            val d = warningList[m]
                                            if (TextUtils.equals(entry.key, d!!.type)) {
                                                name = CommonUtil.getWarningNameByType(activity, d!!.type)
                                                when(d!!.color) {
                                                    "04" -> redCount++
                                                    "03" -> orangeCount++
                                                    "02" -> yellowCount++
                                                    "01" -> blueCount++
                                                }
                                            }
                                        }

                                        if (redCount > 0) {
                                            strRed = "${redCount}红"
                                        }
                                        if (orangeCount > 0) {
                                            strOrange = "${orangeCount}橙"
                                        }
                                        if (yellowCount > 0) {
                                            strYellow = "${yellowCount}黄"
                                        }
                                        if (blueCount > 0) {
                                            strBlue = "${blueCount}蓝"
                                        }

                                        strType += "[${name}]${strRed}${strOrange}${strYellow}${strBlue}；"
                                    }

                                    val str1 = "${title}共有"
                                    val str2 = warningList.size.toString()
                                    val str3 = "条预警。"
                                    tvWarningStatistic.text = "${str1}${str2}${str3}${strType}"
                                    clWarning!!.visibility = View.VISIBLE

                                    val builder = SpannableStringBuilder(tvWarningStatistic.text.toString())
                                    val builderSpan1 = ForegroundColorSpan(ContextCompat.getColor(activity!!, R.color.text_color3))
                                    val builderSpan2 = ForegroundColorSpan(ContextCompat.getColor(activity!!, R.color.colorPrimary))
                                    val builderSpan3 = ForegroundColorSpan(ContextCompat.getColor(activity!!, R.color.text_color3))
                                    builder.setSpan(builderSpan1, 0, str1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    builder.setSpan(builderSpan2, str1.length, str1.length+str2.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    builder.setSpan(builderSpan3, str1.length+str2.length, str1.length+str2.length+str3.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    tvWarningStatistic!!.text = builder
                                    clWarning!!.visibility = View.VISIBLE
                                } catch (e: Exception) {
                                    e.printStackTrace()
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
     * 初始化listview
     */
    private fun initListView() {
        warningAdapter = WarningAdapter(activity, warningList, false)
        listView.adapter = warningAdapter
        listView.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val data = warningList[arg2]
            val intentDetail = Intent(activity, WarningDetailActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("data", data)
            intentDetail.putExtras(bundle)
            startActivity(intentDetail)
        }
    }

}
