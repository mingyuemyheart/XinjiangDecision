package com.hlj.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.LinearLayout
import com.hlj.adapter.BaseViewPagerAdapter
import com.hlj.dto.WarningDto
import com.hlj.fragment.WarningDetailFragment
import kotlinx.android.synthetic.main.activity_heading_warning.*
import kotlinx.android.synthetic.main.layout_title.*
import shawn.cxwl.com.hlj.R

/**
 * 预警，左右滑动切换
 */
class HeadWarningActivity : BaseFragmentActivity(), OnClickListener {

    private val fragments: ArrayList<Fragment> = ArrayList()
    private val warnList = ArrayList<WarningDto>()
    private var ivTips: Array<ImageView?>? = null //装载点的数组

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heading_warning)
        initWidget()
        initViewPager()
    }

    private fun initWidget() {
        llBack!!.setOnClickListener(this)
    }

    /**
     * 初始化viewPager
     */
    private fun initViewPager() {
        warnList.clear()
        warnList.addAll(intent.extras.getParcelableArrayList("warningList"))
        for (i in warnList.indices) {
            val dto = warnList[i]
            val fragment: Fragment = WarningDetailFragment()
            val bundle = Bundle()
            bundle.putParcelable("data", dto)
            fragment.arguments = bundle
            fragments.add(fragment)
        }
        ivTips = arrayOfNulls(warnList.size)
        viewGroup!!.removeAllViews()
        for (i in warnList.indices) {
            val imageView = ImageView(this)
            imageView.layoutParams = LayoutParams(5, 5)
            ivTips!![i] = imageView
            if (i == 0) {
                ivTips!![i]!!.setBackgroundResource(R.drawable.point_black)
            } else {
                ivTips!![i]!!.setBackgroundResource(R.drawable.point_gray)
            }
            val layoutParams = LinearLayout.LayoutParams(LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
            layoutParams.leftMargin = 10
            layoutParams.rightMargin = 10
            viewGroup!!.addView(imageView, layoutParams)
        }
        if (warnList.size <= 1) {
            viewGroup!!.visibility = View.GONE
        }
        viewPager!!.setSlipping(true) //设置ViewPager是否可以滑动
        viewPager!!.offscreenPageLimit = fragments.size
        viewPager!!.setOnPageChangeListener(MyOnPageChangeListener())
        viewPager!!.adapter = BaseViewPagerAdapter(supportFragmentManager, fragments)
    }

    private inner class MyOnPageChangeListener : OnPageChangeListener {
        override fun onPageSelected(arg0: Int) {
            for (i in warnList.indices) {
                if (i == arg0) {
                    ivTips!![i]!!.setBackgroundResource(R.drawable.point_black)
                } else {
                    ivTips!![i]!!.setBackgroundResource(R.drawable.point_gray)
                }
            }
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
        }
    }

}
