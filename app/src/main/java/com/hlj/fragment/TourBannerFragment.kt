package com.hlj.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hlj.activity.WebviewActivity
import com.hlj.common.CONST
import com.hlj.dto.NewsDto
import kotlinx.android.synthetic.main.fragment_tour_banner.*
import net.tsz.afinal.FinalBitmap
import shawn.cxwl.com.hlj.R

/**
 * 旅游气象-banner
 */
class TourBannerFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_tour_banner, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidget()
    }

    private fun initWidget() {
        val data: NewsDto = arguments!!.getParcelable("data")
        if (!TextUtils.isEmpty(data.imgUrl)) {
            val finalBitmap = FinalBitmap.create(activity)
            finalBitmap.display(imageView, data.imgUrl, null, 10)
        }
        if (!TextUtils.isEmpty(data.detailUrl) && data.detailUrl.startsWith("http")) {
            imageView.setOnClickListener {
                val intent = Intent(activity, WebviewActivity::class.java)
                intent.putExtra(CONST.ACTIVITY_NAME, data.title)
                intent.putExtra(CONST.WEB_URL, data.detailUrl)
                startActivity(intent)
            }
        }
    }

}
