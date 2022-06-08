package com.hlj.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemClickListener
import android.widget.Toast
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener
import com.hlj.adapter.RouteSearchAdapter
import com.hlj.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_route_search.*
import kotlinx.android.synthetic.main.layout_title.*
import shawn.cxwl.com.hlj.R
import java.util.*

/**
 * 交通气象
 */
class RouteSearchActivity : BaseActivity(), OnClickListener {

    private var mAdapter: RouteSearchAdapter? = null
    private val poiList: MutableList<PoiItem> = ArrayList() //搜索地点的列表
    private var count = 0 //计数器，计算获取地点列表次数，这里只获取2次，当count==2时，就执行搜索

    override fun onCreate(arg0: Bundle?) {
        super.onCreate(arg0)
        setContentView(R.layout.activity_route_search)
        initWidget()
        initListView()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack!!.setOnClickListener(this)
        editText!!.addTextChangedListener(wathcer)

        if (intent.hasExtra("startOrEnd")) {
            val startOrEnd = intent.getStringExtra("startOrEnd")
            if (startOrEnd == "start") {
                tvTitle!!.text = getString(R.string.select_start_point)
                imageView!!.setImageResource(R.drawable.route_start)
                textView!!.text = "起点："
                editText!!.hint = getString(R.string.select_start_point)
            } else if (startOrEnd == "end") {
                tvTitle!!.text = getString(R.string.select_end_point)
                imageView!!.setImageResource(R.drawable.route_end)
                textView!!.text = "终点："
                editText!!.hint = getString(R.string.select_end_point)
            }
        }
    }

    private val wathcer: TextWatcher = object : TextWatcher {
        override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
        override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
        override fun afterTextChanged(arg0: Editable) {
            if (!TextUtils.isEmpty(arg0.toString().trim { it <= ' ' })) {
                progressBar!!.visibility = View.VISIBLE
                searchPoints(arg0.toString().trim { it <= ' ' })
            } else {
                poiList.clear()
                if (mAdapter != null) {
                    mAdapter!!.notifyDataSetChanged()
                }
            }
        }
    }

    /**
     * 根据关键字查找相关列表数据
     *
     * @param keyWord
     */
    private fun searchPoints(keyWord: String) {
        // 第一个参数表示查询关键字，第二参数表示poi搜索类型，第三个参数表示城市区号或者城市名
        val startSearchQuery = PoiSearch.Query(keyWord, "", "")
        startSearchQuery.pageNum = 0 // 设置查询第几页，第一页从0开始
        startSearchQuery.pageSize = 20 // 设置每页返回多少条数据
        val poiSearch = PoiSearch(this, startSearchQuery)
        poiSearch.searchPOIAsyn() // 异步poi查询
        poiSearch.setOnPoiSearchListener(object : OnPoiSearchListener {
            override fun onPoiSearched(result: PoiResult, rCode: Int) {
                progressBar!!.visibility = View.GONE
                if (rCode == 1000) { // 返回成功
                    if (result.query != null && result.pois != null && result.pois.size > 0) { // 搜索poi的结果
                        count++
                        poiList.clear()
                        poiList.addAll(result.pois)
                        if (mAdapter != null) {
                            mAdapter!!.notifyDataSetChanged()
                        }
                    }
                } else {
                    Toast.makeText(this@RouteSearchActivity, getString(R.string.no_result), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onPoiItemSearched(poiItem: PoiItem, i: Int) {}
        })
    }

    /**
     * 初始化listview
     */
    private fun initListView() {
        mAdapter = RouteSearchAdapter(this, poiList)
        listView!!.adapter = mAdapter
        listView!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = poiList[arg2]
            if (dto.title != null) {
                editText!!.setText(dto.title)
                editText!!.setSelection(dto.title.length)
                if (count >= 2) {
                    count = 0
                    CommonUtil.hideInputSoft(editText, this)
                    val intent = Intent()
                    intent.putExtra("cityName", dto.title)
                    if (dto.provinceName.contains(dto.cityName)) {
                        intent.putExtra("addr", dto.cityName+dto.adName)
                    } else {
                        intent.putExtra("addr", dto.provinceName+dto.cityName+dto.adName)
                    }
                    intent.putExtra("lat", dto.latLonPoint.latitude)
                    intent.putExtra("lng", dto.latLonPoint.longitude)
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    searchPoints(dto.title)
                }
            }
        }
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.llBack -> finish()
        }
    }

}
