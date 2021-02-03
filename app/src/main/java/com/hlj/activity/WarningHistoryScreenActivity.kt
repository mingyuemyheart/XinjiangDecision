package com.hlj.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.Toast
import com.hlj.view.wheelview.NumericWheelAdapter
import com.hlj.view.wheelview.OnWheelScrollListener
import com.hlj.view.wheelview.WheelView
import kotlinx.android.synthetic.main.activity_warning_history_screen.*
import kotlinx.android.synthetic.main.layout_date.*
import kotlinx.android.synthetic.main.layout_title.*
import shawn.cxwl.com.hlj.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 天气预警-历史预警-预警筛选
 */
class WarningHistoryScreenActivity : BaseActivity(), OnClickListener {

    private val sdf2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    private val sdf6 = SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA)
    private var startTime: String? = null
    private var endTime: String? = null
    private var areaName: String? = null
    private var areaId:String? = null
    private var isStart = true //true为start

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warning_history_screen)
        initWidget()
        initWheelView()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvTitle.text = "预警筛选"
        tvStartTime.setOnClickListener(this)
        tvEndTime.setOnClickListener(this)
        tvArea.setOnClickListener(this)
        tvCheck.setOnClickListener(this)
        tvNegtive.setOnClickListener(this)
        tvPositive.setOnClickListener(this)
        try {
            startTime = intent.getStringExtra("startTime")
            tvStartTime.text = sdf2.format(sdf6.parse(startTime))
            endTime = intent.getStringExtra("endTime")
            tvEndTime.text = sdf2.format(sdf6.parse(endTime))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        areaId = intent.getStringExtra("areaId")
        areaName = intent.getStringExtra("areaName")
        tvArea.text = areaName
    }

    private fun initWheelView() {
        val c = Calendar.getInstance()
        val curYear = c[Calendar.YEAR]
        val curMonth = c[Calendar.MONTH] + 1 //通过Calendar算出的月数要+1
        val curDate = c[Calendar.DATE]
        val curHour = c[Calendar.HOUR_OF_DAY]
        val curMinute = c[Calendar.MINUTE]
        val curSecond = c[Calendar.SECOND]

        year.visibility = View.VISIBLE
        month.visibility = View.VISIBLE
        day.visibility = View.VISIBLE
        hour.visibility = View.VISIBLE
        minute.visibility = View.VISIBLE
        second.visibility = View.VISIBLE

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
            startTime = "$yearStr$monthStr$dayStr${hourStr}${minuteStr}${secondStr}"
            tvStartTime.text = "$yearStr-$monthStr-$dayStr ${hourStr}:${minuteStr}:${secondStr}"
        } else {
            endTime = "$yearStr$monthStr$dayStr${hourStr}${minuteStr}${secondStr}"
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
            R.id.tvArea -> startActivityForResult(Intent(this, WarningHistoryScreenAreaActivity::class.java), 1000)
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
                try {
                    val lStart = sdf6.parse(startTime).time
                    val lEnd = sdf6.parse(endTime).time
                    if (lStart >= lEnd) {
                        Toast.makeText(this, getString(R.string.start_big_end), Toast.LENGTH_SHORT).show()
                        return
                    } else {
                        val intent = Intent()
                        intent.putExtra("startTime", startTime)
                        intent.putExtra("endTime", endTime)
                        intent.putExtra("areaName", areaName)
                        intent.putExtra("areaId", areaId)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1000 -> {
                    if (data != null) {
                        areaName = data.extras.getString("areaName")
                        tvArea.text = areaName
                        areaId = data.extras.getString("areaId")
                    }
                }
            }
        }
    }

}
