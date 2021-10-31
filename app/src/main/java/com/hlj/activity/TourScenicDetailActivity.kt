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
import com.hlj.fragment.TourForecastFragment
import com.hlj.fragment.TourWarningFragment
import com.hlj.fragment.WebviewCssFragment
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.squareup.picasso.Picasso
import com.yanzhenjie.sofia.Sofia
import kotlinx.android.synthetic.main.activity_tour_scenic_detail.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import shawn.cxwl.com.hlj.R
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

/**
 * 旅游气象-景点天气-详情
 */
class TourScenicDetailActivity : BaseFragmentActivity(), OnClickListener {

    private val fragments: ArrayList<Fragment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_scenic_detail)
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

        okHttpDetail()
    }

    /**
     * 初始化viewPager
     */
    private fun initViewPager(dto: NewsDto) {
        val dataList: ArrayList<String> = ArrayList()
        dataList.add("介绍")
        dataList.add("预报")
        dataList.add("预警")
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

            var fragment: Fragment? = null
            when(name) {
                "介绍" -> fragment = WebviewCssFragment()
                "预报" -> fragment = TourForecastFragment()
                "预警" -> fragment = TourWarningFragment()
            }
            val bundle = Bundle()
            bundle.putString(CONST.ACTIVITY_NAME, dto.title)
            bundle.putString("cityId", dto.cityId)
            bundle.putString("warningId", dto.warningId)
            bundle.putString(CONST.WEB_URL, "http://xinjiangdecision.tianqi.cn:81/Home/api/get_travel_scenic_details?id=${dto.id}")
            fragment!!.arguments = bundle
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
                        tvName.setTextColor(ContextCompat.getColor(this@TourScenicDetailActivity, R.color.colorPrimary))
                    } else {
                        tvName.setTextColor(ContextCompat.getColor(this@TourScenicDetailActivity, R.color.text_color4))
                    }
                }
            }
            if (llContainer1 != null) {
                for (i in 0 until llContainer1.childCount) {
                    val tvBar = llContainer1.getChildAt(i) as TextView
                    if (i == arg0) {
                        tvBar.setBackgroundColor(ContextCompat.getColor(this@TourScenicDetailActivity, R.color.colorPrimary))
                    } else {
                        tvBar.setBackgroundColor(ContextCompat.getColor(this@TourScenicDetailActivity, R.color.transparent))
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

    /**
     * 获取详情
     */
    private fun okHttpDetail() {
        Thread {
            val id = intent.getStringExtra("id")
            val url = "http://xinjiangdecision.tianqi.cn:81/Home/api/get_travel_scenic_details?id=$id"
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                val dto = NewsDto()
                                if (!obj.isNull("name")) {
                                    dto.title = obj.getString("name")
                                    if (dto.title != null) {
                                        tvName.text = dto.title
                                    }
                                }
                                if (!obj.isNull("level")) {
                                    dto.level = obj.getString("level")
                                    if (dto.level != null) {
                                        tvLevel.setBackgroundResource(R.drawable.corner_green)
                                        tvLevel.text = dto.level
                                    }
                                }
                                if (!obj.isNull("address")) {
                                    dto.addr = obj.getString("address")
                                    if (dto.addr != null) {
                                        ivLocation.setImageResource(R.drawable.icon_location_blue)
                                        tvAddr.text = dto.addr
                                    }
                                }
                                if (!obj.isNull("img")) {
                                    dto.imgUrl = obj.getString("img")
                                    if (!TextUtils.isEmpty(dto.imgUrl)) {
                                        Picasso.get().load(dto.imgUrl).into(imageView)
                                    }
                                }
                                if (!obj.isNull("id")) {
                                    dto.id = obj.getString("id")
                                }
                                if (!obj.isNull("areaid")) {
                                    dto.cityId = obj.getString("areaid")
                                }
                                if (!obj.isNull("areacode")) {
                                    dto.warningId = obj.getString("areacode")
                                }
                                initViewPager(dto)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })
        }.start()
    }

}
