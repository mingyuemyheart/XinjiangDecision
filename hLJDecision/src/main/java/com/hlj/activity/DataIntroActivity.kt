package com.hlj.activity

import android.os.Bundle
import android.text.TextUtils
import com.hlj.common.CONST
import kotlinx.android.synthetic.main.activity_data_intro.*
import kotlinx.android.synthetic.main.layout_title.*
import shawn.cxwl.com.hlj.R

class DataIntroActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_intro)

        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }

        val desc = intent.getStringExtra("desc")
        if (!TextUtils.isEmpty(desc)) {
            tvDesc.text = desc
        }
    }

}