package com.hlj.activity

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import com.hlj.common.CONST
import com.hlj.dto.FactDto
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.hlj.view.*
import kotlinx.android.synthetic.main.activity_fact_detail_chart.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import shawn.cxwl.com.hlj.R
import java.io.IOException
import java.util.*

/**
 * 实况详情图表
 */
class FactDetailChartActivity : BaseActivity(), OnClickListener {

    private val rainList: MutableList<FactDto> = ArrayList()
    private val tempList: MutableList<FactDto> = ArrayList()
    private val windList: MutableList<FactDto> = ArrayList()
    private val humidityList: MutableList<FactDto> = ArrayList()
    private val visibleList: MutableList<FactDto> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fact_detail_chart)
        showDialog()
        initWidget()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)

        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (title != null) {
            tvTitle.text = title
        }
        okHttpStationInfo()
    }

    /**
     * 获取站点数据
     */
    private fun okHttpStationInfo() {
        Thread {
            val stationCode = intent.getStringExtra("stationCode")
            val url = "http://xinjiangdecision.tianqi.cn:81/Home/api/xinjiang_single_tj?stationCode=$stationCode"
            Log.e("okHttpStationInfo", url)
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
                                val array = JSONArray(result)
                                rainList.clear()
                                tempList.clear()
                                windList.clear()
                                humidityList.clear()
                                visibleList.clear()
                                for (i in 0 until array.length()) {
                                    val dto = FactDto()
                                    val itemObj = array.getJSONObject(i)
                                    if (!itemObj.isNull("time")) {
                                        dto.factTime = itemObj.getString("time")
                                    }

                                    var value = ""
                                    if (!itemObj.isNull("rain")) {
                                        value = itemObj.getString("rain")
                                        dto.factRain = value.toFloat()
                                    }
                                    if (!value.contains("99999")) {
                                        rainList.add(dto)
                                    }

                                    value = ""
                                    if (!itemObj.isNull("tem")) {
                                        value = itemObj.getString("tem")
                                        dto.factTemp = value.toFloat()
                                    }
                                    if (!value.contains("99999")) {
                                        tempList.add(dto)
                                    }

                                    value = ""
                                    if (!itemObj.isNull("winds")) {
                                        value = itemObj.getString("winds")
                                        dto.factWind = value.toFloat()
                                    }
                                    if (!value.contains("99999")) {
                                        windList.add(dto)
                                    }

                                    value = ""
                                    if (!itemObj.isNull("xdsd")) {
                                        value = itemObj.getString("xdsd")
                                        dto.factHumidity = value.toFloat()
                                    }
                                    if (!value.contains("99999")) {
                                        humidityList.add(dto)
                                    }

                                    value = ""
                                    if (!itemObj.isNull("vis")) {
                                        value = itemObj.getString("vis")
                                        dto.factVisible = value.toFloat()
                                    }
                                    if (!value.contains("99999")) {
                                        visibleList.add(dto)
                                    }
                                }
                                llContainer1!!.removeAllViews()
                                val rainView = FactRainView(this@FactDetailChartActivity)
                                rainView.setData(rainList)
                                val viewWidth1 = if (rainList.size <= 25) CommonUtil.widthPixels(this@FactDetailChartActivity) * 2 else CommonUtil.widthPixels(this@FactDetailChartActivity) * 4
                                llContainer1!!.addView(rainView, viewWidth1, CommonUtil.heightPixels(this@FactDetailChartActivity) / 3)

                                llContainer2.removeAllViews()
                                val tempView = FactTempView(this@FactDetailChartActivity)
                                tempView.setData(tempList)
                                val viewWidth2 = if (rainList.size <= 25) CommonUtil.widthPixels(this@FactDetailChartActivity) * 2 else CommonUtil.widthPixels(this@FactDetailChartActivity) * 4
                                llContainer2.addView(tempView, viewWidth2, CommonUtil.heightPixels(this@FactDetailChartActivity) / 3)

                                llContainer3.removeAllViews()
                                val windView = FactWindView(this@FactDetailChartActivity)
                                windView.setData(windList)
                                val viewWidth3 = if (windList.size <= 25) CommonUtil.widthPixels(this@FactDetailChartActivity) * 2 else CommonUtil.widthPixels(this@FactDetailChartActivity) * 4
                                llContainer3.addView(windView, viewWidth3, CommonUtil.heightPixels(this@FactDetailChartActivity) / 3)

                                llContainer4.removeAllViews()
                                val humidityView = FactHumidityView(this@FactDetailChartActivity)
                                humidityView.setData(humidityList)
                                val viewWidth4 = if (humidityList.size <= 25) CommonUtil.widthPixels(this@FactDetailChartActivity) * 2 else CommonUtil.widthPixels(this@FactDetailChartActivity) * 4
                                llContainer4.addView(humidityView, viewWidth4, CommonUtil.heightPixels(this@FactDetailChartActivity) / 3)

                                llContainer5.removeAllViews()
                                val visibleView = FactVisibleView(this@FactDetailChartActivity)
                                visibleView.setData(visibleList)
                                val viewWidth5 = if (visibleList.size <= 25) CommonUtil.widthPixels(this@FactDetailChartActivity) * 2 else CommonUtil.widthPixels(this@FactDetailChartActivity) * 4
                                llContainer5.addView(visibleView, viewWidth5, CommonUtil.heightPixels(this@FactDetailChartActivity) / 3)

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
