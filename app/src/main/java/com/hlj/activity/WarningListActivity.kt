package com.hlj.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemClickListener
import android.widget.LinearLayout
import android.widget.TextView
import com.hlj.adapter.WarningAdapter
import com.hlj.dto.WarningDto
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_warning_list.*
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

/**
 * 预警列表
 */
class WarningListActivity : BaseActivity(), OnClickListener {

    private var warningName = "自治区级预警"
    private var warningAdapter: WarningAdapter? = null
    private val dataList: MutableList<WarningDto?> = ArrayList() //上个界面传过来的所有预警数据
    private val proList: MutableList<WarningDto?> = ArrayList()
    private val cityList: MutableList<WarningDto?> = ArrayList()
    private val disList: MutableList<WarningDto?> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warning_list)
        initWidget()
        initListView()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvTitle.text = "预警列表"
        okHttpWarning()
    }

    /**
     * 初始化listview
     */
    private fun initListView() {
        warningAdapter = WarningAdapter(this, dataList, false)
        listView.adapter = warningAdapter
        listView.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val data = dataList[arg2]
            val intentDetail = Intent(this, WarningDetailActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("data", data)
            intentDetail.putExtras(bundle)
            startActivity(intentDetail)
        }
    }

    /**
     * 获取预警信息
     */
    private fun okHttpWarning() {
        Thread {
            val url = "https://decision-admin.tianqi.cn/Home/work2019/getwarns?areaid=65"
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
                                proList.clear()
                                cityList.clear()
                                disList.clear()
                                val obj = JSONObject(result)
                                if (!obj.isNull("data")) {
                                    val jsonArray = obj.getJSONArray("data")
                                    for (i in 0 until jsonArray.length()) {
                                        val tempArray = jsonArray.getJSONArray(i)
                                        val dto = WarningDto()
                                        dto.html = tempArray.getString(1)
                                        val array = dto.html.split("-").toTypedArray()
                                        val item0 = array[0]
                                        val item1 = array[1]
                                        val item2 = array[2]
                                        dto.item0 = item0
                                        dto.provinceId = item0.substring(0, 2)
                                        dto.type = item2.substring(0, 5)
                                        dto.color = item2.substring(5, 7)
                                        dto.time = item1
                                        dto.lng = tempArray.getDouble(2)
                                        dto.lat = tempArray.getDouble(3)
                                        dto.name = tempArray.getString(0)
                                        if (!dto.name.contains("解除")) {
                                            if (TextUtils.equals(item0.substring(item0.length-4, item0.length), "0000")) {
                                                proList.add(dto)
                                            } else if (TextUtils.equals(item0.substring(item0.length-2, item0.length), "00")) {
                                                cityList.add(dto)
                                            } else {
                                                disList.add(dto)
                                            }
                                        }
                                    }
                                    addColumn()
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })
        }.start()
    }

    /**
     * 添加子栏目
     */
    private fun addColumn() {
        llContainer!!.removeAllViews()
        llContainer1.removeAllViews()
        val nameList: ArrayList<String> = ArrayList()
        nameList.add("自治区级预警")
        nameList.add("市级预警")
        nameList.add("县级预警")
        val size = nameList.size
        for (i in 0 until size) {
            val tvName = TextView(this)
            tvName.gravity = Gravity.CENTER
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            tvName.paint.isFakeBoldText = true
            tvName.setPadding(0, CommonUtil.dip2px(this, 10f).toInt(), 0, CommonUtil.dip2px(this, 10f).toInt())
            tvName.maxLines = 1
            tvName.text = nameList[i]
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.weight = 1f
            tvName.layoutParams = params
            llContainer!!.addView(tvName)

            val tvBar = TextView(this)
            tvBar.gravity = Gravity.CENTER
            val params1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params1.weight = 1f
            params1.height = CommonUtil.dip2px(this, 2f).toInt()
            params1.gravity = Gravity.CENTER
            tvBar.layoutParams = params1
            llContainer1.addView(tvBar)

            if (i == 0) {
                tvName.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                tvBar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                warningName = tvName.text.toString()
                addWarnings()
            } else {
                tvName.setTextColor(ContextCompat.getColor(this, R.color.text_color3))
                tvBar.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray))
            }

            tvName.setOnClickListener { arg0 ->
                if (llContainer != null) {
                    for (j in 0 until llContainer!!.childCount) {
                        val name = llContainer!!.getChildAt(j) as TextView
                        val bar = llContainer1.getChildAt(j) as TextView
                        if (TextUtils.equals(tvName.text.toString(), name.text.toString())) {
                            name.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                            bar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                            warningName = tvName.text.toString()
                            addWarnings()
                        } else {
                            name.setTextColor(ContextCompat.getColor(this, R.color.text_color3))
                            bar.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray))
                        }
                    }
                }
            }
        }
    }

    private fun addWarnings() {
        dataList.clear()
        when(warningName) {
            "自治区级预警" -> {
                dataList.addAll(proList)
            }
            "市级预警" -> {
                dataList.addAll(cityList)
            }
            "县级预警" -> {
                dataList.addAll(disList)
            }
        }
        if (warningAdapter != null) {
            warningAdapter!!.notifyDataSetChanged()
        }
        if (dataList.isEmpty()) {
            clNoWarning.visibility = View.VISIBLE
        } else {
            clNoWarning.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> finish()
        }
    }

}
