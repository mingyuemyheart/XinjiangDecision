package com.hlj.activity

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.github.abel533.echarts.axis.CategoryAxis
import com.github.abel533.echarts.axis.ValueAxis
import com.github.abel533.echarts.code.Magic
import com.github.abel533.echarts.code.Tool
import com.github.abel533.echarts.code.Trigger
import com.github.abel533.echarts.feature.MagicType
import com.github.abel533.echarts.series.Line
import com.hlj.echart.EchartOptionUtil
import kotlinx.android.synthetic.main.activity_warning_statistic.*
import kotlinx.android.synthetic.main.layout_title.*
import shawn.cxwl.com.hlj.R


class WarningStatisticActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warning_statistic)
        initWidget()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvTitle.text = "预警统计"

//        lineChart.webViewClient = object : WebViewClient() {
//            override fun onPageFinished(view: WebView, url: String) {
//                super.onPageFinished(view, url)
//                //最好在h5页面加载完毕后再加载数据，防止html的标签还未加载完成，不能正常显示
//                refreshLineChart()
//            }
//        }
    }

    private fun refreshLineChart() {
        val x = arrayOf<Any>(
                "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
        )
        val y = arrayOf<Any>(
                820, 932, 901, 934, 1290, 1330, 1320
        )
        lineChart.refreshEchartsWithOption(EchartOptionUtil.getLineChartOptions(x, y))
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.llBack -> finish()
        }
    }

}