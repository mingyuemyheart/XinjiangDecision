package com.hlj.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemClickListener
import android.widget.LinearLayout
import android.widget.TextView
import com.hlj.adapter.TourSearchAdapter
import com.hlj.adapter.TourSearchListAdapter
import com.hlj.dto.CityDto
import com.hlj.dto.NewsDto
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_tour_search.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import shawn.cxwl.com.hlj.R
import java.io.IOException

/**
 * 旅游气象-城市查询
 */
class TourSearchActivity : BaseActivity(), OnClickListener {

    //输入框搜索
    private var searchAdapter: TourSearchListAdapter? = null
    private val showList: ArrayList<NewsDto> = ArrayList()
    private val searchList: ArrayList<NewsDto> = ArrayList()
    private val typeMap: LinkedHashMap<String, String> = LinkedHashMap()

    //城市
    private var cityAdapter: TourSearchAdapter? = null
    private val cityList: ArrayList<NewsDto> = ArrayList()

    //景点
    private var scenicAdapter: TourSearchAdapter? = null
    private val scenicList: ArrayList<NewsDto> = ArrayList()

    //旅游文化
    private var cultureAdapter: TourSearchAdapter? = null
    private val cultureList: ArrayList<NewsDto> = ArrayList()

    //特色美食
    private var foodAdapter: TourSearchAdapter? = null
    private val foodList: ArrayList<NewsDto> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_search)
        initWidget()
        initListView()
        initGridView1()
        initGridView2()
        initGridView3()
        initGridView4()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack!!.setOnClickListener(this)
        etSearch!!.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun afterTextChanged(arg0: Editable) {
                searchList.clear()
                showList.clear()
                typeMap.clear()
                if (TextUtils.isEmpty(arg0.toString())) {
                    llContainer.visibility = View.GONE
                    listView.visibility = View.GONE
                    scrollView.visibility = View.VISIBLE
                } else {
                    llContainer.visibility = View.VISIBLE
                    listView.visibility = View.VISIBLE
                    scrollView.visibility = View.GONE

                    typeMap["全部"] = "全部"
                    for (i in 0 until cityList.size) {
                        val city = cityList[i]
                        if (city.title.contains(arg0.toString())) {
                            searchList.add(city)
                            showList.add(city)
                            typeMap["城市"] = "城市"
                        }
                    }
                    for (i in 0 until scenicList.size) {
                        val scenic = scenicList[i]
                        if (scenic.title.contains(arg0.toString())) {
                            searchList.add(scenic)
                            showList.add(scenic)
                            typeMap["景点"] = "景点"
                        }
                    }

                    llContainer.removeAllViews()
                    for ((key, value) in typeMap.entries) {
                        val tv = TextView(this@TourSearchActivity)
                        tv.text = key
                        tv.tag = key
                        tv.gravity = Gravity.CENTER
                        tv.setPadding(25, 0, 25, 0)
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                        if (TextUtils.equals(key, "全部")) {
                            tv.setTextColor(Color.WHITE)
                            tv.setBackgroundResource(R.drawable.corner_left_right_blue)
                        } else {
                            tv.setTextColor(ContextCompat.getColor(this@TourSearchActivity, R.color.text_color4))
                            tv.setBackgroundResource(R.drawable.corner_left_right_gray)
                        }
                        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        params.leftMargin = CommonUtil.dip2px(this@TourSearchActivity, 10f).toInt()
                        tv.layoutParams = params
                        llContainer.addView(tv)

                        tv.setOnClickListener { v ->
                            val name: String = (v!! as TextView).text.toString()
                            for (j in 0 until llContainer.childCount) {
                                val tvName = llContainer.getChildAt(j) as TextView
                                if (TextUtils.equals(tvName.text.toString(), name)) {
                                    tvName.setTextColor(Color.WHITE)
                                    tvName.setBackgroundResource(R.drawable.corner_left_right_blue)
                                    showList.clear()
                                    for (m in 0 until searchList.size) {
                                        val search = searchList[m]
                                        when(name) {
                                            "全部" -> {
                                                showList.add(search)
                                            }
                                            "城市" -> {
                                                if (TextUtils.equals(search.type, "1")) {
                                                    showList.add(search)
                                                }
                                            }
                                            "景点" -> {
                                                if (TextUtils.equals(search.type, "2")) {
                                                    showList.add(search)
                                                }
                                            }
                                            "旅游文化" -> {
                                                if (TextUtils.equals(search.type, "3")) {
                                                    showList.add(search)
                                                }
                                            }
                                            "特色美食" -> {
                                                if (TextUtils.equals(search.type, "4")) {
                                                    showList.add(search)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    tvName.setTextColor(ContextCompat.getColor(this@TourSearchActivity, R.color.text_color4))
                                    tvName.setBackgroundResource(R.drawable.corner_left_right_gray)
                                }
                            }
                        }
                    }
                }
                if (searchAdapter != null) {
                    searchAdapter!!.setKey(arg0.toString())
                    searchAdapter!!.notifyDataSetChanged()
                }
            }
        })

        okHttpList()
    }

    private fun initListView() {
        searchAdapter = TourSearchListAdapter(this, showList)
        listView.adapter = searchAdapter
        listView.setOnItemClickListener { parent, view, position, id ->
            val dto = showList[position]
            when(dto.type) {
                "1" -> {
                    toCityDetail(dto)
                }
                "2" -> {
                    toScenicDetail(dto)
                }
                "3" -> {
                    toCultureDetail(dto)
                }
                "4" -> {
                    toFoodDetail(dto)
                }
            }
        }
    }

    private fun initGridView1() {
        cityAdapter = TourSearchAdapter(this, cityList)
        gridView1!!.adapter = cityAdapter
        gridView1!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = cityList[arg2]
            toCityDetail(dto)
        }
    }

    private fun toCityDetail(dto: NewsDto) {
        val data = CityDto()
        data.cityId = dto.cityId
        data.areaName = dto.title
        val bundle = Bundle()
        bundle.putParcelable("data", data)
        intent = Intent(this, WeatherDetailActivity::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    private fun initGridView2() {
        scenicAdapter = TourSearchAdapter(this, scenicList)
        gridView2!!.adapter = scenicAdapter
        gridView2!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = scenicList[arg2]
            toScenicDetail(dto)
        }
    }

    private fun toScenicDetail(dto: NewsDto) {
        val intent = Intent(this, TourScenicDetailActivity::class.java)
        intent.putExtra("id", dto.id)
        startActivity(intent)
    }

    private fun initGridView3() {
        cultureAdapter = TourSearchAdapter(this, cultureList)
        gridView3!!.adapter = cultureAdapter
        gridView3!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = cultureList[arg2]
            toCultureDetail(dto)
        }
    }

    private fun toCultureDetail(dto: NewsDto) {

    }

    private fun initGridView4() {
        foodAdapter = TourSearchAdapter(this, foodList)
        gridView4!!.adapter = foodAdapter
        gridView4!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = foodList[arg2]
            toFoodDetail(dto)
        }
    }

    private fun toFoodDetail(dto: NewsDto) {

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
        }
    }

    private fun okHttpList() {
        Thread {
            val url = "http://xinjiangdecision.tianqi.cn:81/Home/api/get_search_list"
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                }
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        if (!TextUtils.isEmpty(result)) {
                            val obj = JSONObject(result)
                            if (!obj.isNull("type1")) {
                                val array = obj.getJSONArray("type1")
                                cityList.clear()
                                for (i in 0 until array.length()) {
                                    val dto = NewsDto()
                                    val itemObj = array.getJSONObject(i)
                                    if (!itemObj.isNull("name")) {
                                        dto.title = itemObj.getString("name")
                                    }
                                    if (!itemObj.isNull("areaid")) {
                                        dto.cityId = itemObj.getString("areaid")
                                    }
                                    if (!itemObj.isNull("type")) {
                                        dto.type = itemObj.getString("type")
                                    }
                                    cityList.add(dto)
                                }
                                if (cityAdapter != null) {
                                    cityAdapter!!.notifyDataSetChanged()
                                }
                            }
                            if (!obj.isNull("type2")) {
                                val array = obj.getJSONArray("type2")
                                scenicList.clear()
                                for (i in 0 until array.length()) {
                                    val dto = NewsDto()
                                    val itemObj = array.getJSONObject(i)
                                    if (!itemObj.isNull("name")) {
                                        dto.title = itemObj.getString("name")
                                    }
                                    if (!itemObj.isNull("id")) {
                                        dto.id = itemObj.getString("id")
                                    }
                                    if (!itemObj.isNull("type")) {
                                        dto.type = itemObj.getString("type")
                                    }
                                    scenicList.add(dto)
                                }
                                if (scenicAdapter != null) {
                                    scenicAdapter!!.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }
            })
        }.start()
    }

}
