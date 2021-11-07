package com.hlj.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemClickListener
import android.widget.LinearLayout
import android.widget.TextView
import com.hlj.adapter.FactDetailAdapter
import com.hlj.common.CONST
import com.hlj.common.ColumnData
import com.hlj.dto.FactDto
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_fact_detail.*
import kotlinx.android.synthetic.main.layout_title.*
import net.sourceforge.pinyin4j.PinyinHelper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import shawn.cxwl.com.hlj.R
import java.io.IOException
import java.util.*

/**
 * 实况详情
 */
class FactDetailActivity : BaseActivity(), OnClickListener {

    private var b1 = false
    private var b2 = false
    private var b3 = false //false为将序，true为升序
    private var mAdapter: FactDetailAdapter? = null
    private val realDatas: MutableList<FactDto> = ArrayList()
    private var childId = ""
    private var childDataUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fact_detail)
        initWidget()
        initListViewRank()
    }

    private fun initWidget() {
        llBack!!.setOnClickListener(this)
        ll1!!.setOnClickListener(this)
        ll2.setOnClickListener(this)
        ll3.setOnClickListener(this)
        if (!b3) { //将序
            iv3.setImageResource(R.drawable.arrow_down)
        } else { //将序
            iv3.setImageResource(R.drawable.arrow_up)
        }
        iv3.visibility = View.VISIBLE

        if (intent.hasExtra(CONST.ACTIVITY_NAME)) {
            val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
            if (!TextUtils.isEmpty(title)) {
                tvTitle!!.text = title
            }
        }
        if (intent.hasExtra("childId")) {
            childId = intent.getStringExtra("childId")
        }
        if (intent.hasExtra("data")) {
            val dto: ColumnData = intent.getParcelableExtra("data")
            if (dto != null) {
                addItem(dto)
            }
        }
    }

    private fun initListViewRank() {
        mAdapter = FactDetailAdapter(this, realDatas)
        listViewRank!!.adapter = mAdapter
        listViewRank!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = realDatas[arg2]
            val intent = Intent(this, FactDetailChartActivity::class.java)
            intent.putExtra(CONST.ACTIVITY_NAME, dto!!.stationName)
            intent.putExtra("stationCode", dto!!.stationCode)
            startActivity(intent)
        }
    }

    /**
     * 返回中文的首字母
     * @param str
     * @return
     */
    fun getPinYinHeadChar(str: String): String {
        var convert = ""
        var size = str.length
        if (size >= 2) {
            size = 2
        }
        for (j in 0 until size) {
            val word = str[j]
            val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word)
            convert += if (pinyinArray != null) {
                pinyinArray[0][0]
            } else {
                word
            }
        }
        return convert
    }

    private fun addItem(dto: ColumnData?) {
        llContainer1.removeAllViews()
        tvTitle.text = "实况排名-${dto!!.name}"
        for (j in 0 until dto!!.child.size) {
            val item = dto!!.child[j]
            val tvItem = TextView(this)
            tvItem.text = item.name
            tvItem.tag = item.id+"---"+item.dataUrl
            tvItem.gravity = Gravity.CENTER
            tvItem.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f)
            tvItem.setPadding(25, 0, 25, 0)
            val params1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params1.leftMargin = CommonUtil.dip2px(this, 10f).toInt()
            tvItem.layoutParams = params1
            llContainer1.addView(tvItem)
            if (childId == item.id) {
                tvItem.setTextColor(Color.WHITE)
                tvItem.setBackgroundResource(R.drawable.corner_left_right_blue)
                childDataUrl = item.dataUrl
                okHttpFact()
            } else {
                tvItem.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvItem.setBackgroundResource(R.drawable.corner_left_right_gray)
            }

            tvItem.setOnClickListener { arg0 ->
                val itemTag = arg0.tag.toString()
                if (!itemTag.contains("---")) {
                    return@setOnClickListener
                }
                val tagArray = itemTag.split("---")
                for (m in 0 until llContainer1.childCount) {
                    val itemName = llContainer1.getChildAt(m) as TextView
                    if (TextUtils.equals(itemName.tag.toString(), itemTag)) {
                        itemName.setTextColor(Color.WHITE)
                        itemName.setBackgroundResource(R.drawable.corner_left_right_blue)
                        childId = tagArray[0]
                        childDataUrl = tagArray[1]
                        okHttpFact()
                    } else {
                        itemName.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                        itemName.setBackgroundResource(R.drawable.corner_left_right_gray)
                    }
                }
            }
        }
    }

    /**
     * 获取实况信息
     */
    private fun okHttpFact() {
        if (TextUtils.isEmpty(childDataUrl) || TextUtils.isEmpty(childId)) {
            return
        }
        showDialog()
        Thread {
            try {
                Log.e("okHttpFact", childDataUrl)
                OkHttpUtil.enqueue(Request.Builder().url(childDataUrl).build(), object : Callback {
                    override fun onFailure(call: Call, e: IOException) {}

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (!response.isSuccessful) {
                            return
                        }
                        val result = response.body!!.string()
                        runOnUiThread {
                            cancelDialog()
                            if (!TextUtils.isEmpty(result)) {
                                try {
                                    val obj = JSONObject(result)
                                    //详情开始
                                    if (!obj.isNull("th")) {
                                        val itemObj = obj.getJSONObject("th")
                                        if (!itemObj.isNull("stationName")) {
                                            val stationName = itemObj.getString("stationName")
                                            if (stationName != null) {
                                                tv1.text = stationName
                                            }
                                        }
                                        if (!itemObj.isNull("area")) {
                                            val area = itemObj.getString("area")
                                            if (area != null) {
                                                tv2.text = area
                                            }
                                        }
                                        if (!itemObj.isNull("val")) {
                                            val `val` = itemObj.getString("val")
                                            if (`val` != null) {
                                                tv3.text = `val`
                                            }
                                        }
                                    }
                                    if (!obj.isNull("realDatas")) {
                                        realDatas.clear()
                                        val array = JSONArray(obj.getString("realDatas"))
                                        for (i in 0 until array.length()) {
                                            val itemObj = array.getJSONObject(i)
                                            val dto = FactDto()
                                            if (!itemObj.isNull("stationCode")) {
                                                dto.stationCode = itemObj.getString("stationCode")
                                            }
                                            if (!itemObj.isNull("stationName")) {
                                                dto.stationName = itemObj.getString("stationName")
                                            }
                                            if (!itemObj.isNull("area")) {
                                                dto.area = itemObj.getString("area")
                                            }
                                            if (!itemObj.isNull("area1")) {
                                                dto.area1 = itemObj.getString("area1")
                                            }
                                            if (!itemObj.isNull("val")) {
                                                dto.`val` = itemObj.getDouble("val")
                                            }
                                            if (!itemObj.isNull("val1")) {
                                                dto.val1 = itemObj.getDouble("val1")
                                            }
                                            if (!itemObj.isNull("Lon")) {
                                                dto.lng = itemObj.getString("Lon").toDouble()
                                            }
                                            if (!itemObj.isNull("Lat")) {
                                                dto.lat = itemObj.getString("Lat").toDouble()
                                            }
                                            if (!TextUtils.isEmpty(dto.stationName) && !TextUtils.isEmpty(dto.area)) {
                                                realDatas.add(dto)
                                            }
                                        }
                                        if (realDatas.size > 0) {
                                            clNoData.visibility = View.GONE
                                        } else {
                                            clNoData.visibility = View.VISIBLE
                                        }
                                        if (mAdapter != null) {
                                            mAdapter!!.notifyDataSetChanged()
                                        }
                                    }
                                    //详情结束

                                    llItemTitle.visibility = View.VISIBLE
                                    clNoData.visibility = View.GONE
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            } else {
                                clNoData.visibility = View.VISIBLE
                            }
                        }
                    }
                })
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> finish()
            R.id.ll1 -> {
                if (b1) { //升序
                    b1 = false
                    iv1!!.setImageResource(R.drawable.arrow_up)
                    iv1!!.visibility = View.VISIBLE
                    iv2.visibility = View.INVISIBLE
                    iv3.visibility = View.INVISIBLE
                    realDatas.sortWith(Comparator { arg0, arg1 ->
                        if (TextUtils.isEmpty(arg0!!.stationName) || TextUtils.isEmpty(arg1!!.stationName)) {
                            0
                        } else {
                            getPinYinHeadChar(arg0!!.stationName).compareTo(getPinYinHeadChar(arg1!!.stationName))
                        }
                    })
                } else { //将序
                    b1 = true
                    iv1!!.setImageResource(R.drawable.arrow_down)
                    iv1!!.visibility = View.VISIBLE
                    iv2.visibility = View.INVISIBLE
                    iv3.visibility = View.INVISIBLE
                    realDatas.sortWith(Comparator { arg0, arg1 ->
                        if (TextUtils.isEmpty(arg0!!.stationName) || TextUtils.isEmpty(arg1!!.stationName)) {
                            -1
                        } else {
                            getPinYinHeadChar(arg1!!.stationName).compareTo(getPinYinHeadChar(arg0!!.stationName))
                        }
                    })
                }
                if (mAdapter != null) {
                    mAdapter!!.notifyDataSetChanged()
                }
            }
            R.id.ll2 -> {
                if (b2) { //升序
                    b2 = false
                    iv2.setImageResource(R.drawable.arrow_up)
                    iv1!!.visibility = View.INVISIBLE
                    iv2.visibility = View.VISIBLE
                    iv3.visibility = View.INVISIBLE
                    realDatas.sortWith(Comparator { arg0, arg1 ->
                        if (TextUtils.isEmpty(arg0!!.area) || TextUtils.isEmpty(arg1!!.area)) {
                            0
                        } else {
                            getPinYinHeadChar(arg0!!.area).compareTo(getPinYinHeadChar(arg1!!.area))
                        }
                    })
                } else { //将序
                    b2 = true
                    iv2.setImageResource(R.drawable.arrow_down)
                    iv1!!.visibility = View.INVISIBLE
                    iv2.visibility = View.VISIBLE
                    iv3.visibility = View.INVISIBLE
                    realDatas.sortWith(Comparator { arg0, arg1 ->
                        if (TextUtils.isEmpty(arg0!!.area) || TextUtils.isEmpty(arg1!!.area)) {
                            -1
                        } else {
                            getPinYinHeadChar(arg1!!.area).compareTo(getPinYinHeadChar(arg0!!.area))
                        }
                    })
                }
                if (mAdapter != null) {
                    mAdapter!!.notifyDataSetChanged()
                }
            }
            R.id.ll3 -> {
                if (b3) { //升序
                    b3 = false
                    iv3.setImageResource(R.drawable.arrow_up)
                    iv1!!.visibility = View.INVISIBLE
                    iv2.visibility = View.INVISIBLE
                    iv3.visibility = View.VISIBLE
                    realDatas.sortWith(Comparator { arg0, arg1 -> java.lang.Double.valueOf(arg0!!.`val`).compareTo(java.lang.Double.valueOf(arg1!!.`val`)) })
                } else { //将序
                    b3 = true
                    iv3.setImageResource(R.drawable.arrow_down)
                    iv1!!.visibility = View.INVISIBLE
                    iv2.visibility = View.INVISIBLE
                    iv3.visibility = View.VISIBLE
                    realDatas.sortWith(Comparator { arg0, arg1 -> java.lang.Double.valueOf(arg1!!.`val`).compareTo(java.lang.Double.valueOf(arg0!!.`val`)) })
                }
                if (mAdapter != null) {
                    mAdapter!!.notifyDataSetChanged()
                }
            }
        }
    }

}
