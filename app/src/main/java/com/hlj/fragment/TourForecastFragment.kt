package com.hlj.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.hlj.adapter.WeeklyForecastAdapter
import com.hlj.dto.WeatherDto
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.hlj.utils.WeatherUtil
import com.hlj.view.WeeklyView
import kotlinx.android.synthetic.main.fragment_tour_forecast.*
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
import kotlin.collections.ArrayList

/**
 * 景点-天气预报
 */
class TourForecastFragment : BaseFragment(), OnClickListener {

    private var weeklyAdapter: WeeklyForecastAdapter? = null
    private val weeklyList: MutableList<WeatherDto> = ArrayList()
    private val sdf1 = SimpleDateFormat("HH", Locale.CHINA)
    private val sdf3 = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
    private val sdf4 = SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tour_forecast, null)
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
        tvChart.setOnClickListener(this)
        tvList.setOnClickListener(this)

        getWeatherInfo()
    }

    /**
     * 初始化listview
     */
    private fun initListView() {
        weeklyAdapter = WeeklyForecastAdapter(activity, weeklyList)
        listView.adapter = weeklyAdapter
    }

    private fun getWeatherInfo() {
        Thread {
            val cityId = arguments!!.getString("cityId")
            val url = String.format("https://hfapi.tianqi.cn/getweatherdata.php?area=%s&type=forecast|observe|alarm|air|rise&key=AErLsfoKBVCsU8hs", cityId)
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
                                val obj = JSONObject(result)

                                if (!obj.isNull("forecast")) {
                                    val forecast = obj.getJSONObject("forecast")
                                    //15天预报信息
                                    if (!forecast.isNull("24h")) {
                                        weeklyList.clear()
                                        val `object` = forecast.getJSONObject("24h")
                                        if (!`object`.isNull(cityId)) {
                                            val object1 = `object`.getJSONObject(cityId)
                                            val f0 = object1.getString("000")
                                            var foreDate: Long = 0
                                            var currentDate: Long = 0
                                            try {
                                                val fTime = sdf3.format(sdf4.parse(f0))
                                                foreDate = sdf3.parse(fTime).time
                                                currentDate = sdf3.parse(sdf3.format(Date())).time
                                            } catch (e: ParseException) {
                                                e.printStackTrace()
                                            }
                                            if (!object1.isNull("1001001")) {
                                                val f1 = object1.getJSONArray("1001001")
                                                var length = f1.length()
                                                if (length >= 15) {
                                                    length = 15
                                                }
                                                for (i in 0 until length) {
                                                    val dto = WeatherDto()

                                                    //预报时间
                                                    dto.date = CommonUtil.getDate(f0, i) //日期
                                                    dto.week = CommonUtil.getWeek(f0, i) //星期几

                                                    //预报内容
                                                    val weeklyObj = f1.getJSONObject(i)

                                                    //晚上
                                                    val two = weeklyObj.getString("002")
                                                    if (!TextUtils.isEmpty(two) && !TextUtils.equals(two, "?") && !TextUtils.equals(two, "null")) {
                                                        dto.lowPheCode = Integer.valueOf(two)
                                                        dto.lowPhe = getString(WeatherUtil.getWeatherId(dto.lowPheCode))
                                                    }
                                                    val four = weeklyObj.getString("004")
                                                    if (!TextUtils.isEmpty(two) && !TextUtils.equals(two, "?") && !TextUtils.equals(two, "null")) {
                                                        dto.lowTemp = Integer.valueOf(four)
                                                    }

                                                    //白天
                                                    val one = weeklyObj.getString("001")
                                                    if (!TextUtils.isEmpty(one) && !TextUtils.equals(one, "?") && !TextUtils.equals(one, "null")) {
                                                        dto.highPheCode = Integer.valueOf(one)
                                                        dto.highPhe = getString(WeatherUtil.getWeatherId(dto.highPheCode))
                                                    }
                                                    val three = weeklyObj.getString("003")
                                                    if (!TextUtils.isEmpty(three) && !TextUtils.equals(three, "?") && !TextUtils.equals(three, "null")) {
                                                        dto.highTemp = Integer.valueOf(three)
                                                    }

                                                    val hour = sdf1.format(Date()).toInt()
                                                    if (hour in 5..17) {
                                                        val seven = weeklyObj.getString("007")
                                                        if (!TextUtils.isEmpty(seven) && !TextUtils.equals(seven, "?") && !TextUtils.equals(seven, "null")) {
                                                            dto.windDir = Integer.valueOf(seven)
                                                        }
                                                        val five = weeklyObj.getString("005")
                                                        if (!TextUtils.isEmpty(five) && !TextUtils.equals(five, "?") && !TextUtils.equals(five, "null")) {
                                                            dto.windForce = Integer.valueOf(five)
                                                            dto.windForceString = WeatherUtil.getDayWindForce(dto.windForce)
                                                        }
                                                    } else {
                                                        val eight = weeklyObj.getString("008")
                                                        if (!TextUtils.isEmpty(eight) && !TextUtils.equals(eight, "?") && !TextUtils.equals(eight, "null")) {
                                                            dto.windDir = Integer.valueOf(eight)
                                                        }
                                                        val six = weeklyObj.getString("006")
                                                        if (!TextUtils.isEmpty(six) && !TextUtils.equals(six, "?") && !TextUtils.equals(six, "null")) {
                                                            dto.windForce = Integer.valueOf(six)
                                                            dto.windForceString = WeatherUtil.getDayWindForce(dto.windForce)
                                                        }
                                                    }
                                                    weeklyList.add(dto)
                                                }

                                                //一周预报列表
                                                if (weeklyAdapter != null) {
                                                    weeklyAdapter!!.foreDate = foreDate
                                                    weeklyAdapter!!.currentDate = currentDate
                                                    weeklyAdapter!!.notifyDataSetChanged()
                                                }

                                                //一周预报曲线
                                                val weeklyView = WeeklyView(activity)
                                                weeklyView.setData(weeklyList, foreDate, currentDate)
                                                llContainerFifteen!!.removeAllViews()
                                                llContainerFifteen!!.addView(weeklyView, CommonUtil.widthPixels(activity) * 2, CommonUtil.dip2px(activity, 320f).toInt())
                                            }
                                        }
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
            R.id.tvChart, R.id.tvList -> if (listView!!.visibility == View.VISIBLE) {
                tvChart!!.setBackgroundResource(R.drawable.bg_chart_press)
                tvList.setBackgroundResource(R.drawable.bg_list)
                listView!!.visibility = View.GONE
                hScrollView2!!.visibility = View.VISIBLE
            } else {
                tvChart!!.setBackgroundResource(R.drawable.bg_chart)
                tvList.setBackgroundResource(R.drawable.bg_list_press)
                listView!!.visibility = View.VISIBLE
                hScrollView2!!.visibility = View.GONE
            }
        }
    }

}