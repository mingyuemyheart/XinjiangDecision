package com.hlj.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.webkit.WebViewClient
import android.widget.Toast
import com.hlj.adapter.WarningLegendAdapter
import com.hlj.adapter.WarningTypeAdapter
import com.hlj.dto.WarningDto
import com.hlj.echart.EchartOptionUtil
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.hlj.view.wheelview.NumericWheelAdapter
import com.hlj.view.wheelview.OnWheelScrollListener
import com.hlj.view.wheelview.WheelView
import kotlinx.android.synthetic.main.activity_warning_statistic.*
import kotlinx.android.synthetic.main.layout_date.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import shawn.cxwl.com.hlj.R
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * 天气预警-预警统计
 */
class WarningStatisticActivity : BaseActivity(), View.OnClickListener {

    private var isStart = true
    private val sdf1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA)
    private val warningList: ArrayList<WarningDto?> = ArrayList()
    private var mAdapter: WarningTypeAdapter? = null
    private val typeList: ArrayList<WarningDto?> = ArrayList()
    private var startTime = ""
    private var endTime = ""
    private var legendAdapter: WarningLegendAdapter? = null
    private val legendList: ArrayList<WarningDto?> = ArrayList()
    private var colorAdapter: WarningLegendAdapter? = null
    private val colorList: ArrayList<WarningDto?> = ArrayList()
    private val warningTypes: HashMap<String, WarningDto?> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warning_statistic)
        initWidget()
        initGridViewLegend()
        initGridViewColor()
        initGridView()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvTitle.text = "预警统计"
        tvControl.text = "列表"
        tvControl.setOnClickListener(this)
        tvStartTime.setOnClickListener(this)
        tvEndTime.setOnClickListener(this)
        tvCheck.setOnClickListener(this)
        tvNegtive.setOnClickListener(this)
        tvPositive.setOnClickListener(this)

        hourWheelView.visibility = View.GONE
        year.visibility = View.VISIBLE
        month.visibility = View.VISIBLE
        day.visibility = View.VISIBLE
        hour.visibility = View.VISIBLE
        minute.visibility = View.VISIBLE
        second.visibility = View.VISIBLE
        xunyear.visibility = View.GONE
        xunmonth.visibility = View.GONE
        xun.visibility = View.GONE
        monthWheelView.visibility = View.GONE
        monthYear.visibility = View.GONE
        monthMonth.visibility = View.GONE
        yearWheelView.visibility = View.GONE

        tvStartTime.text = sdf1.format(Date().time+1000*60*60*24*30)
        tvEndTime.text = sdf1.format(Date())
        startTime = sdf2.format(sdf1.parse(tvStartTime.text.toString()))
        endTime = sdf2.format(sdf1.parse(tvEndTime.text.toString()))

        initWheelView()

        echartView1.webViewClient = object : WebViewClient() {
        }
    }

    private fun refreshPie() {
        echartView0.refreshEchartsWithOption(EchartOptionUtil.pieOption(warningList))
        echartView1.refreshEchartsWithOption(EchartOptionUtil.nestedPieOption(warningList))
    }

    private fun refreshBar(list: ArrayList<WarningDto?>) {
        echartView2.refreshEchartsWithOption(EchartOptionUtil.stackedBarOption(list))
    }

    private fun initGridViewLegend() {
        legendAdapter = WarningLegendAdapter(this, legendList)
        gridViewLegend.adapter = legendAdapter
    }

    private fun setColorDatas() {
        colorList.clear()
        var dto = WarningDto()
        dto.type = "01"
        dto.name = "蓝色预警"
        colorList.add(dto)
        dto = WarningDto()
        dto.type = "02"
        dto.name = "黄色预警"
        colorList.add(dto)
        dto = WarningDto()
        dto.type = "03"
        dto.name = "橙色预警"
        colorList.add(dto)
        dto = WarningDto()
        dto.type = "04"
        dto.name = "红色预警"
        colorList.add(dto)
        dto = WarningDto()
        dto.type = "05"
        dto.name = "未知颜色"
        colorList.add(dto)
    }

    private fun initGridViewColor() {
        setColorDatas()
        colorAdapter = WarningLegendAdapter(this, colorList)
        gridViewColor.adapter = colorAdapter
    }

    private fun initGridView() {
        mAdapter = WarningTypeAdapter(this, typeList)
        gridView.adapter = mAdapter
        gridView.setOnItemClickListener { parent, view, position, id ->
            val data = typeList[position]
            val dataList: ArrayList<WarningDto?> = ArrayList()
            for (i in 0 until warningList.size) {
                if (TextUtils.equals(data!!.type, "*****")) {
                    dataList.addAll(warningList)
                } else if (TextUtils.equals(data.type, warningList[i]!!.type)) {
                    dataList.add(warningList[i])
                }
            }
            refreshBar(dataList)

            for (i in 0 until typeList.size) {
                val dto = typeList[i]
                dto!!.isSelected = position == i
            }
            mAdapter!!.notifyDataSetChanged()
        }
    }

    private fun okHttpList() {
        showDialog()
        Thread(Runnable {
            val url = "http://warn-wx.tianqi.cn/Test/getHistoryWarns?areaid=23&st=${startTime}&et=${endTime}"
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
                        pro.visibility = View.VISIBLE
                        tvPro.visibility = View.VISIBLE
                        city.visibility = View.VISIBLE
                        tvCity.visibility = View.VISIBLE
                        dis.visibility = View.VISIBLE
                        tvDis.visibility = View.VISIBLE
                        gridViewLegend.visibility = View.VISIBLE
                        gridViewColor.visibility = View.VISIBLE
                        gridView.visibility = View.VISIBLE
                        tvControl.visibility = View.VISIBLE
                        var pro = 0
                        var city = 0
                        var dis = 0
                        if (!TextUtils.isEmpty(result)) {
                            warningList.clear()
                            warningTypes.clear()
                            val jsonArray = JSONArray(result)
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
                                dto.typeColor = CommonUtil.randomColor()
                                dto.time = item1
                                dto.name = tempArray.getString(0)
                                if (!dto.name.contains("解除")) {
                                    warningList.add(dto)
                                    if (dto.item0.endsWith("0000")) {
                                        pro++
                                    } else if (dto.item0.substring(2,4) != "00" && dto.item0.substring(4,6) == "00") {
                                        city++
                                    } else {
                                        dis++
                                    }

                                    //获取所有的预警类型
                                    val typeDto = WarningDto()
                                    typeDto.type = dto.type
                                    var name = dto.name
                                    if (!TextUtils.isEmpty(dto.name)) {
                                        if (dto.name.contains("发布") && dto.name.contains("预警")) {
                                            name = dto.name.substring(dto.name.indexOf("发布")+2, dto.name.indexOf("预警")-2) + "预警"
                                        }
                                    }
                                    typeDto.color = dto.typeColor
                                    typeDto.name = name
                                    warningTypes[dto.type] = typeDto
                                }
                            }
                            tvPro.text = "省($pro)"
                            tvCity.text = "市($city)"
                            tvDis.text = "县($dis)"

                            //填充图例表格数据
                            legendList.clear()
                            for (entry in warningTypes.entries) {
                                val legend = entry.value
                                legendList.add(legend)
                            }
                            setColorDatas()
                            for (i in warningList.indices) {
                                val dto = warningList[i]
                                for (j in legendList.indices) {
                                    val legend = legendList[j]
                                    if (TextUtils.equals(legend!!.type, dto!!.type)) {
                                        legend.count++
                                    }
                                }

                                for (j in colorList.indices) {
                                    val color = colorList[j]
                                    if (TextUtils.equals(color!!.type, dto!!.color)) {
                                        color.count++
                                    }
                                }
                            }
                            if (legendAdapter != null) {
                                legendAdapter!!.notifyDataSetChanged()
                            }
                            if (colorAdapter != null) {
                                colorAdapter!!.notifyDataSetChanged()
                            }
                            //填充图例表格数据

                            typeList.clear()
                            val typeDto = WarningDto()
                            typeDto.type = "*****"
                            typeDto.name = "全部"
                            typeDto.isSelected = true
                            typeList.add(typeDto)
                            for (entry in warningTypes.entries) {
                                val legend = entry.value
                                typeList.add(legend)
                            }
                            if (mAdapter != null) {
                                mAdapter!!.notifyDataSetChanged()
                            }

                            refreshPie()
                            refreshBar(warningList)
                        }
                    }
                }
            })
        }).start()
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.llBack -> finish()
            R.id.tvControl -> {
                val intent = Intent(this, WarningListActivity::class.java)
                val bundle = Bundle()
                bundle.putParcelableArrayList("warningList", warningList as java.util.ArrayList<out Parcelable?>)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            R.id.tvStartTime -> {
                isStart = true
                bootTimeLayoutAnimation()
            }
            R.id.tvNegtive -> bootTimeLayoutAnimation()
            R.id.tvPositive -> {
                setTextViewValue()
                bootTimeLayoutAnimation()
            }
            R.id.tvEndTime -> {
                isStart = false
                bootTimeLayoutAnimation()
            }
            R.id.tvCheck -> {
                val start = sdf1.parse(tvStartTime.text.toString())
                val end = sdf1.parse(tvEndTime.text.toString())
                if (start > end) {
                    Toast.makeText(this, "开始时间不能大于结束时间", Toast.LENGTH_SHORT).show()
                } else {
                    startTime = sdf2.format(sdf1.parse(tvStartTime.text.toString()))
                    endTime = sdf2.format(sdf1.parse(tvEndTime.text.toString()))
                    okHttpList()
                }
            }
        }
    }

    private fun initWheelView() {
        val c = Calendar.getInstance()
        val curYear = c[Calendar.YEAR]
        val curMonth = c[Calendar.MONTH] + 1 //通过Calendar算出的月数要+1
        val curDate = c[Calendar.DATE]
        val curHour = c[Calendar.HOUR_OF_DAY]
        val curMinute = c[Calendar.MINUTE]
        val curSecond = c[Calendar.SECOND]

        val numericWheelAdapter1 = NumericWheelAdapter(this, 1950, curYear)
        numericWheelAdapter1.setLabel("年")
        year.viewAdapter = numericWheelAdapter1
        year.isCyclic = false //是否可循环滑动
        year.addScrollingListener(scrollListener)
        year.visibleItems = 7

        val numericWheelAdapter2 = NumericWheelAdapter(this, 1, 12, "%02d")
        numericWheelAdapter2.setLabel("月")
        month.viewAdapter = numericWheelAdapter2
        month.isCyclic = false
        month.addScrollingListener(scrollListener)
        month.visibleItems = 7

        initDay(curYear, curMonth)
        day.isCyclic = false
        day.visibleItems = 7

        val numericWheelAdapter3 = NumericWheelAdapter(this, 0, 23, "%02d")
        numericWheelAdapter3.setLabel("时")
        hour.viewAdapter = numericWheelAdapter3
        hour.isCyclic = false
        hour.addScrollingListener(scrollListener)
        hour.visibleItems = 7

        val numericWheelAdapter4 = NumericWheelAdapter(this, 0, 59, "%02d")
        numericWheelAdapter4.setLabel("分")
        minute.viewAdapter = numericWheelAdapter4
        minute.isCyclic = false
        minute.addScrollingListener(scrollListener)
        minute.visibleItems = 7

        val numericWheelAdapter5 = NumericWheelAdapter(this, 0, 59, "%02d")
        numericWheelAdapter5.setLabel("秒")
        second.viewAdapter = numericWheelAdapter5
        second.isCyclic = false
        second.addScrollingListener(scrollListener)
        second.visibleItems = 7

        year.currentItem = curYear - 1950
        month.currentItem = curMonth - 1
        day.currentItem = curDate - 1
        hour.currentItem = curHour
        minute.currentItem = curMinute
        second.currentItem = curSecond
    }

    private val scrollListener: OnWheelScrollListener = object : OnWheelScrollListener {
        override fun onScrollingStarted(wheel: WheelView) {}
        override fun onScrollingFinished(wheel: WheelView) {
            val nYear = year!!.currentItem + 1950 //年
            val nMonth: Int = month.currentItem + 1 //月
            initDay(nYear, nMonth)
        }
    }

    /**
     */
    private fun initDay(arg1: Int, arg2: Int) {
        val numericWheelAdapter = NumericWheelAdapter(this, 1, getDay(arg1, arg2), "%02d")
        numericWheelAdapter.setLabel("日")
        day.viewAdapter = numericWheelAdapter
    }

    /**
     *
     * @param year
     * @param month
     * @return
     */
    private fun getDay(year: Int, month: Int): Int {
        var day = 30
        var flag = false
        flag = when (year % 4) {
            0 -> true
            else -> false
        }
        day = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            2 -> if (flag) 29 else 28
            else -> 30
        }
        return day
    }

    /**
     */
    private fun setTextViewValue() {
        val yearStr = (year!!.currentItem + 1950).toString()
        val monthStr = if (month.currentItem + 1 < 10) "0" + (month.currentItem + 1) else (month.currentItem + 1).toString()
        val dayStr = if (day.currentItem + 1 < 10) "0" + (day.currentItem + 1) else (day.currentItem + 1).toString()
        val hourStr = if (hour.currentItem + 1 < 10) "0" + (hour.currentItem) else (hour.currentItem).toString()
        val minuteStr = if (minute.currentItem + 1 < 10) "0" + (minute.currentItem) else (minute.currentItem).toString()
        val secondStr = if (second.currentItem + 1 < 10) "0" + (second.currentItem) else (second.currentItem).toString()
        if (isStart) {
            tvStartTime.text = "$yearStr-$monthStr-$dayStr ${hourStr}:${minuteStr}:${secondStr}"
        } else {
            tvEndTime.text = "$yearStr-$monthStr-$dayStr ${hourStr}:${minuteStr}:${secondStr}"
        }
    }

    private fun bootTimeLayoutAnimation() {
        if (layoutDate!!.visibility == View.GONE) {
            timeLayoutAnimation(true, layoutDate)
            layoutDate!!.visibility = View.VISIBLE
        } else {
            timeLayoutAnimation(false, layoutDate)
            layoutDate!!.visibility = View.GONE
        }
    }

    /**
     * 时间图层动画
     * @param flag
     * @param view
     */
    private fun timeLayoutAnimation(flag: Boolean, view: View?) {
        //列表动画
        val animationSet = AnimationSet(true)
        val animation: TranslateAnimation = if (!flag) {
            TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 1f)
        } else {
            TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 1f,
                    Animation.RELATIVE_TO_SELF, 0f)
        }
        animation.duration = 200
        animationSet.addAnimation(animation)
        animationSet.fillAfter = true
        view!!.startAnimation(animationSet)
        animationSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                view.clearAnimation()
            }
        })
    }

}