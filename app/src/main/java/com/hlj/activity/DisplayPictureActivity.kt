package com.hlj.activity

import android.os.Bundle
import android.text.TextUtils
import com.hlj.common.CONST
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_display_picture.*
import shawn.cxwl.com.hlj.R
import java.io.File

/**
 * 图片预览
 */
class DisplayPictureActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_picture)
        initWidget()
    }

    private fun initWidget() {
        if (intent.hasExtra(CONST.WEB_URL)) {
            val imgUrl = intent.getStringExtra(CONST.WEB_URL)
            if (!TextUtils.isEmpty(imgUrl)) {
                if (imgUrl.startsWith("http")) {
                    Picasso.get().load(imgUrl).into(imageView)
                } else {
                    val file = File(imgUrl)
                    if (file.exists()) {
                        Picasso.get().load(file).into(imageView)
                    }
                }
            }
        }
    }

}
