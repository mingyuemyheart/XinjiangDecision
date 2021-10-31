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
import com.hlj.dto.NewsDto
import com.hlj.fragment.TourScenicFragment
import com.hlj.utils.CommonUtil
import com.squareup.picasso.Picasso
import com.yanzhenjie.sofia.Sofia
import kotlinx.android.synthetic.main.activity_tour_route_detail.*
import kotlinx.android.synthetic.main.layout_title.*
import shawn.cxwl.com.hlj.R
import java.util.*
import kotlin.collections.ArrayList

/**
 * 旅游气象-旅游路线-详情
 */
class TourRouteDetailActivity : BaseFragmentActivity(), OnClickListener {

    private val fragments: ArrayList<Fragment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_route_detail)
//        initSofia()
        initWidget()
    }

    private fun initSofia() {
        Sofia.with(this)
                .statusBarLightFont()//状态栏浅色字体
                .invasionStatusBar()//内容入侵状态栏
                .statusBarBackground(ContextCompat.getColor(this, R.color.transparent))
                .navigationBarBackground(ContextCompat.getColor(this, R.color.transparent));//导航栏背景色//状态
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        reTitle.setBackgroundColor(Color.TRANSPARENT)
        imageView.setImageResource(R.drawable.icon_no_bitmap)
        
        if (intent.hasExtra("data")) {
            val data: NewsDto = intent.getParcelableExtra("data")
            if (data.title != null) {
                tvName.text = data.title
            }
            if (data.playTime != null) {
                tvPlayTime.setBackgroundResource(R.drawable.corner_left_right_green_line)
                tvPlayTime.text = "适合游玩时间：${data.playTime}"
            }
            if (data.length != null) {
                tvLength.text = data.length
            }
            if (data.startEnd != null) {
                ivRoute.setImageResource(R.drawable.icon_route)
                tvRoute.text = "起-终"
                tvStartEnd.text = data.startEnd
            }
            if (!TextUtils.isEmpty(data.imgUrl)) {
                Picasso.get().load(data.imgUrl).into(imageView)
            }
            if (!TextUtils.isEmpty(data.id)) {
                initViewPager(data.id)
            }
        }
    }

    /**
     * 初始化viewPager
     */
    private fun initViewPager(id: String) {
        val dataList: ArrayList<String> = ArrayList()
        dataList.add("景点标注")
        dataList.add("旅游路线介绍")
        val size = dataList.size
        if (size <= 1) {
            llContainer!!.visibility = View.GONE
            llContainer1!!.visibility = View.GONE
        }
        fragments.clear()
        llContainer!!.removeAllViews()
        llContainer1!!.removeAllViews()
        for (i in dataList.indices) {
            val name = dataList[i]
            val tvName = TextView(this)
            tvName.gravity = Gravity.CENTER
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
            tvName.setPadding(0, (CommonUtil.dip2px(this, 10f)).toInt(), 0, (CommonUtil.dip2px(this, 10f)).toInt())
            tvName.setOnClickListener(MyOnClickListener(i))
            tvName.setTextColor(Color.WHITE)
            if (!TextUtils.isEmpty(name)) {
                tvName.text = name
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
            params1.leftMargin = (CommonUtil.dip2px(this, 50f)).toInt()
            params1.rightMargin = (CommonUtil.dip2px(this, 50f)).toInt()
            tvBar.layoutParams = params1
            llContainer1!!.addView(tvBar, i)

            val fragment = TourScenicFragment()
            val bundle = Bundle()
            bundle.putString("id", id)
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
                        tvName.setTextColor(ContextCompat.getColor(this@TourRouteDetailActivity, R.color.colorPrimary))
                    } else {
                        tvName.setTextColor(ContextCompat.getColor(this@TourRouteDetailActivity, R.color.text_color4))
                    }
                }
            }
            if (llContainer1 != null) {
                for (i in 0 until llContainer1.childCount) {
                    val tvBar = llContainer1.getChildAt(i) as TextView
                    if (i == arg0) {
                        tvBar.setBackgroundColor(ContextCompat.getColor(this@TourRouteDetailActivity, R.color.colorPrimary))
                    } else {
                        tvBar.setBackgroundColor(ContextCompat.getColor(this@TourRouteDetailActivity, R.color.transparent))
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
