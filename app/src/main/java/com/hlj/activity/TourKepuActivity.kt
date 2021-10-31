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
import com.hlj.common.ColumnData
import com.hlj.fragment.JueceListFragment
import com.hlj.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_tour_kepu.*
import kotlinx.android.synthetic.main.layout_title.*
import shawn.cxwl.com.hlj.R
import java.util.*
import kotlin.collections.ArrayList

/**
 * 旅游气象-科普
 */
class TourKepuActivity : BaseFragmentActivity(), OnClickListener {

    private val fragments: ArrayList<Fragment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_kepu)
        initWidget()
        initViewPager()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        
        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (title != null) {
            tvTitle.text = title
        }
    }

    /**
     * 初始化viewPager
     */
    private fun initViewPager() {
        val dataList: ArrayList<ColumnData> = ArrayList()
        if (intent.hasExtra("data")) {
            val data: ColumnData = intent.getParcelableExtra("data")
            if (TextUtils.equals(data.id, "7104")) {//交通旅游专报
                dataList.add(data)
            } else {
                dataList.addAll(data.child)
            }
        }
        val size = dataList.size
        if (size <= 1) {
            llContainer!!.visibility = View.GONE
            llContainer1!!.visibility = View.GONE
        }
        fragments.clear()
        llContainer!!.removeAllViews()
        llContainer1!!.removeAllViews()
        for (i in dataList.indices) {
            val dto = dataList[i]
            val tvName = TextView(this)
            tvName.gravity = Gravity.CENTER
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
            tvName.setPadding(0, (CommonUtil.dip2px(this, 10f)).toInt(), 0, (CommonUtil.dip2px(this, 10f)).toInt())
            tvName.setOnClickListener(MyOnClickListener(i))
            tvName.setTextColor(Color.WHITE)
            if (!TextUtils.isEmpty(dto.name)) {
                tvName.text = dto.name
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

            val fragment = JueceListFragment()
            val bundle = Bundle()
            bundle.putParcelable("data", dto)
            fragment.arguments = bundle
            fragments.add(fragment)
        }
        viewPager.setSlipping(true) //设置ViewPager是否可以滑动
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
                        tvName.setTextColor(ContextCompat.getColor(this@TourKepuActivity, R.color.colorPrimary))
                    } else {
                        tvName.setTextColor(ContextCompat.getColor(this@TourKepuActivity, R.color.text_color4))
                    }
                }
            }
            if (llContainer1 != null) {
                for (i in 0 until llContainer1.childCount) {
                    val tvBar = llContainer1.getChildAt(i) as TextView
                    if (i == arg0) {
                        tvBar.setBackgroundColor(ContextCompat.getColor(this@TourKepuActivity, R.color.colorPrimary))
                    } else {
                        tvBar.setBackgroundColor(ContextCompat.getColor(this@TourKepuActivity, R.color.transparent))
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
