package com.hlj.activity

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout
import android.widget.TextView
import com.hlj.adapter.BaseViewPagerAdapter
import com.hlj.common.CONST
import com.hlj.dto.NewsDto
import com.hlj.fragment.TourImpressionFragment
import com.hlj.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_tour_impression.*
import kotlinx.android.synthetic.main.layout_title.*
import shawn.cxwl.com.hlj.R
import java.util.*
import kotlin.collections.ArrayList

/**
 * 旅游气象-新疆印象
 */
class TourImpressionActivity : BaseFragmentActivity(), OnClickListener {

    private val fragments: ArrayList<Fragment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_impression)
        initWidget()
        initViewPager()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)

        if (intent.hasExtra(CONST.ACTIVITY_NAME)) {
            val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
            if (title != null) {
                tvTitle.text = title
            }
        }
    }

    /**
     * 初始化viewPager
     */
    private fun initViewPager() {
        val dataList: ArrayList<NewsDto> = ArrayList()
        var dto = NewsDto()
        dto.title = "旅游文化"
        dto.id = "1"
        dataList.add(dto)
        dto = NewsDto()
        dto.title = "特色美食"
        dto.id = "2"
        dataList.add(dto)
        dto = NewsDto()
        dto.title = "广袤草原"
        dto.id = "3"
        dataList.add(dto)
        dto = NewsDto()
        dto.title = "雪山美景"
        dto.id = "4"
        dataList.add(dto)

        val size = dataList.size
        if (size <= 1) {
            llContainer!!.visibility = View.GONE
            llContainer1!!.visibility = View.GONE
        }
        fragments.clear()
        llContainer!!.removeAllViews()
        llContainer1!!.removeAllViews()
        for (i in dataList.indices) {
            val data = dataList[i]
            val tvName = TextView(this)
            tvName.gravity = Gravity.CENTER
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
            tvName.setPadding(0, (CommonUtil.dip2px(this, 10f)).toInt(), 0, (CommonUtil.dip2px(this, 10f)).toInt())
            tvName.setOnClickListener(MyOnClickListener(i))
            tvName.setTextColor(Color.WHITE)
            if (!TextUtils.isEmpty(data.title)) {
                tvName.text = data.title
            }
            if (i == 0) {
                tvName.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            } else {
                tvName.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
            }
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.weight = 1.0f
            tvName.layoutParams = params
            llContainer!!.addView(tvName, i)
            
            val tvBar = TextView(this)
            tvBar.gravity = Gravity.CENTER
            tvBar.setOnClickListener(MyOnClickListener(i))
            if (i == 0) {
                tvBar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
            } else {
                tvBar.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
            }
            val params1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params1.weight = 1.0f
            params1.height = (CommonUtil.dip2px(this, 2f)).toInt()
            params1.leftMargin = (CommonUtil.dip2px(this, 10f)).toInt()
            params1.rightMargin = (CommonUtil.dip2px(this, 10f)).toInt()
            tvBar.layoutParams = params1
            llContainer1!!.addView(tvBar, i)

            val fragment = TourImpressionFragment()
            val bundle = Bundle()
            bundle.putString(CONST.ACTIVITY_NAME, data.title)
            bundle.putString(CONST.WEB_URL, "http://xinjiangdecision.tianqi.cn:81/home/api/get_travel_culture?type=${data.id}")
            fragment.arguments = bundle
            fragments.add(fragment)
        }
        viewPager.setSlipping(false) //设置ViewPager是否可以滑动
        viewPager.offscreenPageLimit = fragments.size
        viewPager.setOnPageChangeListener(MyOnPageChangeListener())
        viewPager.adapter = BaseViewPagerAdapter(supportFragmentManager, fragments)
    }

    private inner class MyOnPageChangeListener : ViewPager.OnPageChangeListener {
        override fun onPageSelected(arg0: Int) {
            if (llContainer != null) {
                for (i in 0 until llContainer.childCount) {
                    val tvName = llContainer.getChildAt(i) as TextView
                    if (i == arg0) {
                        tvName.setTextColor(ContextCompat.getColor(this@TourImpressionActivity, R.color.colorPrimary))
                    } else {
                        tvName.setTextColor(ContextCompat.getColor(this@TourImpressionActivity, R.color.text_color4))
                    }
                }
            }
            if (llContainer1 != null) {
                for (i in 0 until llContainer1.childCount) {
                    val tvBar = llContainer1.getChildAt(i) as TextView
                    if (i == arg0) {
                        tvBar.setBackgroundColor(ContextCompat.getColor(this@TourImpressionActivity, R.color.colorPrimary))
                    } else {
                        tvBar.setBackgroundColor(ContextCompat.getColor(this@TourImpressionActivity, R.color.transparent))
                    }
                }
            }
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    /**
     * 头标点击监听
     * @author shawn_sun
     */
    private inner class MyOnClickListener(private val index: Int) : OnClickListener {
        override fun onClick(v: View) {
            if (viewPager != null) {
                viewPager.setCurrentItem(index, true)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> finish()
        }
    }

}
