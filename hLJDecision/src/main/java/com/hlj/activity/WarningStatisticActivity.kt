package com.hlj.activity

import android.os.Bundle
import android.view.View
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
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.llBack -> finish()
        }
    }

}