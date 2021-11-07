package com.hlj.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import com.hlj.activity.TourImpressionDetailActivity
import com.hlj.adapter.TourImpressionAdapter
import com.hlj.common.CONST
import com.hlj.dto.NewsDto
import com.hlj.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_tour_route.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import shawn.cxwl.com.hlj.R
import java.io.IOException

/**
 * 旅游气象-新疆印象
 */
class TourImpressionFragment : BaseFragment() {

    private var mAdapter: TourImpressionAdapter? = null
    private val showList: ArrayList<NewsDto> = ArrayList()
    private val dataList: ArrayList<NewsDto> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tour_impression, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRefreshLayout()
        initWidget()
        initListView()
    }

    private fun initWidget() {
        ivSearch.visibility = View.VISIBLE
        etSearch.visibility = View.VISIBLE
        val hint = arguments!!.getString(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(hint)) {
            etSearch.hint = hint
        }
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
        mAdapter = TourImpressionAdapter(activity!!, showList)
        listView!!.adapter = mAdapter
        listView!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = showList[arg2]
            val intent = Intent(activity, TourImpressionDetailActivity::class.java)
            intent.putExtra(CONST.ACTIVITY_NAME, arguments!!.getString(CONST.ACTIVITY_NAME)+"详情")
            intent.putExtra("id", dto.id)
            startActivity(intent)
        }
    }

    private fun okHttpList() {
        Thread {
            val url = arguments!!.getString(CONST.WEB_URL)
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    if (!isAdded) {
                        return
                    }
                    val result = response.body!!.string()
                    activity!!.runOnUiThread {
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
                                    if (!itemObj.isNull("title")) {
                                        dto.title = itemObj.getString("title")
                                    }
                                    if (!itemObj.isNull("img")) {
                                        dto.imgUrl = itemObj.getString("img")
                                    }
                                    if (!itemObj.isNull("desc")) {
                                        dto.desc = itemObj.getString("desc")
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

}
