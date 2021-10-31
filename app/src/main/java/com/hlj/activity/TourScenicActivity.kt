package com.hlj.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import com.hlj.adapter.TourScenicAdapter
import com.hlj.common.CONST
import com.hlj.dto.NewsDto
import com.hlj.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_tour_route.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import shawn.cxwl.com.hlj.R
import java.io.IOException

/**
 * 旅游气象-景点天气
 */
class TourScenicActivity : BaseActivity(), View.OnClickListener {

    private var mAdapter: TourScenicAdapter? = null
    private val showList: ArrayList<NewsDto> = ArrayList()
    private val dataList: ArrayList<NewsDto> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_route)
        initRefreshLayout()
        initWidget()
        initListView()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvTitle.setOnClickListener(this)
        ivSearch.visibility = View.VISIBLE
        etSearch.visibility = View.VISIBLE
        etSearch.hint = "景点"
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                showList.clear()
                if (TextUtils.isEmpty(s.toString())) {
                    showList.addAll(dataList)
                } else {
                    for (i in 0 until dataList.size) {
                        val data = dataList[i]
                        if (data.title.contains(s.toString())) {
                            showList.add(data)
                        }
                    }
                }
                if (mAdapter != null) {
                    mAdapter!!.notifyDataSetChanged()
                }
            }
        })

        val title: String = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }

        okHttpList()
    }

    /**
     * 初始化下拉刷新布局
     */
    private fun initRefreshLayout() {
        refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4)
        refreshLayout.setProgressViewEndTarget(true, 400)
        refreshLayout.isRefreshing = true
        refreshLayout.setOnRefreshListener { okHttpList() }
    }

    private fun initListView() {
        mAdapter = TourScenicAdapter(this, showList)
        listView!!.adapter = mAdapter
        listView!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = showList[arg2]
            val intent = Intent(this, TourScenicDetailActivity::class.java)
            intent.putExtra("id", dto.id)
            startActivity(intent)
        }
    }

    private fun okHttpList() {
        Thread {
            val url = "http://xinjiangdecision.tianqi.cn:81/Home/api/get_travel_sceniclist"
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    runOnUiThread {
                        refreshLayout!!.isRefreshing = false
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                dataList.clear()
                                showList.clear()
                                val array = JSONArray(result)
                                for (i in 0 until array.length()) {
                                    val dto = NewsDto()
                                    val itemObj = array.getJSONObject(i)
                                    if (!itemObj.isNull("id")) {
                                        dto.id = itemObj.getString("id")
                                    }
                                    if (!itemObj.isNull("name")) {
                                        dto.title = itemObj.getString("name")
                                    }
                                    if (!itemObj.isNull("img")) {
                                        dto.imgUrl = itemObj.getString("img")
                                    }
                                    if (!itemObj.isNull("level")) {
                                        dto.level = itemObj.getString("level")
                                    }
                                    if (!itemObj.isNull("lat")) {
                                        dto.lat = itemObj.getDouble("lat")
                                    }
                                    if (!itemObj.isNull("lon")) {
                                        dto.lng = itemObj.getDouble("lon")
                                    }
                                    if (!itemObj.isNull("areaid")) {
                                        dto.cityId = itemObj.getString("areaid")
                                    }
                                    if (!itemObj.isNull("areacode")) {
                                        dto.warningId = itemObj.getString("areacode")
                                    }
                                    if (!itemObj.isNull("address")) {
                                        dto.addr = itemObj.getString("address")
                                    }
                                    dataList.add(dto)
                                    showList.add(dto)
                                }
                                if (mAdapter != null) {
                                    mAdapter!!.notifyDataSetChanged()
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

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> {
                finish()
            }
        }
    }

}
