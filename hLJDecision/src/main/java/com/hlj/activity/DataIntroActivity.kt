package com.hlj.activity

import android.os.Bundle
import android.text.TextUtils
import com.hlj.common.CONST
import com.hlj.common.MyApplication
import kotlinx.android.synthetic.main.activity_data_intro.*
import kotlinx.android.synthetic.main.layout_title.*
import shawn.cxwl.com.hlj.R

class DataIntroActivity : BaseActivity() {

    private var desc = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_intro)

        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }

        setDesc()
    }

    private fun setDesc() {
        for (i in 0 until MyApplication.columnDataList.size) {
            val dto1 = MyApplication.columnDataList[i]
            var desc1 = "${(i+1)}.${dto1.name}：${dto1.desc}\n"
            for (j in 0 until dto1.child.size) {
                val dto2 = dto1.child[j]
                val desc2 = "   ${(i+1)}.${(j+1)}.${dto2.name}：${dto2.desc}\n"
                desc1 += desc2
            }
            desc+=desc1
        }
        tvDesc.text = desc
    }

}