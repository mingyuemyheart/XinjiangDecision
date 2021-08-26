package com.hlj.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.hlj.dto.StationMonitorDto
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.hlj.view.pointfore.ForeCloudView
import com.hlj.view.pointfore.ForeHumidityView
import com.hlj.view.pointfore.ForeTempView
import com.hlj.view.pointfore.ForeWindView
import kotlinx.android.synthetic.main.activity_point_fore_detail.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import shawn.cxwl.com.hlj.R
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 格点预报详情
 */
class PointForeDetailActivity : BaseActivity(), View.OnClickListener, GeocodeSearch.OnGeocodeSearchListener {

    private var geocoderSearch: GeocodeSearch? = null
    private val sdf1 = SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("yyyy-MM-dd HH时", Locale.CHINA)
    private val sdf3 = SimpleDateFormat("HH时", Locale.CHINA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_point_fore_detail)
        showDialog()
        initWidget()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvTitle.text = "格点预报详情"
        tvName.text = "未知位置"
        geocoderSearch = GeocodeSearch(this)
        geocoderSearch!!.setOnGeocodeSearchListener(this)
        val lat = intent.getDoubleExtra("lat", 0.0)
        val lng = intent.getDoubleExtra("lng", 0.0)
        searchAddrByLatLng(lat, lng)
        okHttpList(lat, lng)
    }

    /**
     * 通过经纬度获取地理位置信息
     * @param lat
     * @param lng
     */
    private fun searchAddrByLatLng(lat: Double, lng: Double) {
        //latLonPoint参数表示一个Latlng，第二参数表示范围多少米，GeocodeSearch.AMAP表示是国测局坐标系还是GPS原生坐标系
        val query = RegeocodeQuery(LatLonPoint(lat, lng), 200f, GeocodeSearch.AMAP)
        geocoderSearch!!.getFromLocationAsyn(query)
    }

    override fun onGeocodeSearched(arg0: GeocodeResult?, arg1: Int) {}
    override fun onRegeocodeSearched(result: RegeocodeResult?, rCode: Int) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.regeocodeAddress != null && result.regeocodeAddress.formatAddress != null) {
                tvName!!.text = result.regeocodeAddress.formatAddress
            } else {
                tvName!!.text = "未知位置"
            }
        } else {
            tvName!!.text = "未知位置"
        }
    }

    private fun okHttpList(lat: Double, lng: Double) {
        val url = String.format("http://scapi.weather.com.cn/weather/getqggdyb?type=EDA10,R03,ECT,TMP,RRH&lonlat=%s,%s&tier=24&test=ncg", lng, lat)
        Thread(Runnable {
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
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
                                if (!obj.isNull("Time")) {
                                    val publishTime = obj.getString("Time")
                                    if (!TextUtils.isEmpty(publishTime)) {
                                        try {
                                            tvPublishTime.text = "北纬:" + lng + "°" + " " + "东经：" + lat + "°" + "   " + "中央气象台" + sdf2.format(sdf1.parse(publishTime)) + "发布"
                                        } catch (e: ParseException) {
                                            e.printStackTrace()
                                        }
                                        if (!obj.isNull("TMP")) {
                                            val array = obj.getJSONArray("TMP")
                                            val list: MutableList<StationMonitorDto> = ArrayList()
                                            for (i in 0 until array.length()) {
                                                val dto = StationMonitorDto()
                                                dto.pointTemp = array.getString(i)
                                                try {
                                                    val time = sdf1.parse(publishTime).time + 1000 * 60 * 60 * i * 3
                                                    dto.time = sdf3.format(time)
                                                    val currentTime = Date().time
                                                    if (currentTime <= time) {
                                                        list.add(dto)
                                                    }
                                                } catch (e: ParseException) {
                                                    e.printStackTrace()
                                                }
                                            }
                                            llContainerTemp!!.removeAllViews()
                                            val view = ForeTempView(this@PointForeDetailActivity)
                                            view.setData(list)
                                            llContainerTemp!!.addView(view, CommonUtil.widthPixels(this@PointForeDetailActivity), CommonUtil.dip2px(this@PointForeDetailActivity, 150f).toInt())
                                        }
                                        if (!obj.isNull("RRH")) {
                                            val array = obj.getJSONArray("RRH")
                                            val list: MutableList<StationMonitorDto> = ArrayList()
                                            for (i in 0 until array.length()) {
                                                val dto = StationMonitorDto()
                                                dto.humidity = array.getString(i)
                                                try {
                                                    val time = sdf1.parse(publishTime).time + 1000 * 60 * 60 * i * 3
                                                    dto.time = sdf3.format(time)
                                                    val currentTime = Date().time
                                                    if (currentTime <= time) {
                                                        list.add(dto)
                                                    }
                                                } catch (e: ParseException) {
                                                    e.printStackTrace()
                                                }
                                            }
                                            llContainerHumidity.removeAllViews()
                                            val view = ForeHumidityView(this@PointForeDetailActivity)
                                            view.setData(list)
                                            llContainerHumidity.addView(view, CommonUtil.widthPixels(this@PointForeDetailActivity), CommonUtil.dip2px(this@PointForeDetailActivity, 150f).toInt())
                                        }
                                        if (!obj.isNull("WINS")) {
                                            val array = obj.getJSONArray("WINS")
                                            val array2 = obj.getJSONArray("WIND")
                                            val list: MutableList<StationMonitorDto> = ArrayList()
                                            for (i in 0 until array.length()) {
                                                val dto = StationMonitorDto()
                                                dto.windSpeed = array.getString(i)
                                                dto.windDir = array2.getString(i)
                                                try {
                                                    val time = sdf1.parse(publishTime).time + 1000 * 60 * 60 * i * 3
                                                    dto.time = sdf3.format(time)
                                                    val currentTime = Date().time
                                                    if (currentTime <= time) {
                                                        list.add(dto)
                                                    }
                                                } catch (e: ParseException) {
                                                    e.printStackTrace()
                                                }
                                            }
                                            llContainerWind.removeAllViews()
                                            val view = ForeWindView(this@PointForeDetailActivity)
                                            view.setData(list)
                                            llContainerWind.addView(view, CommonUtil.widthPixels(this@PointForeDetailActivity), CommonUtil.dip2px(this@PointForeDetailActivity, 150f).toInt())
                                        }
                                        if (!obj.isNull("ECT")) {
                                            val array = obj.getJSONArray("ECT")
                                            val list: MutableList<StationMonitorDto> = ArrayList()
                                            for (i in 0 until array.length()) {
                                                val dto = StationMonitorDto()
                                                dto.cloud = array.getString(i)
                                                try {
                                                    val time = sdf1.parse(publishTime).time + 1000 * 60 * 60 * i * 3
                                                    dto.time = sdf3.format(time)
                                                    val currentTime = Date().time
                                                    if (currentTime <= time) {
                                                        list.add(dto)
                                                    }
                                                } catch (e: ParseException) {
                                                    e.printStackTrace()
                                                }
                                            }
                                            llContainerCloud.removeAllViews()
                                            val view = ForeCloudView(this@PointForeDetailActivity)
                                            view.setData(list)
                                            llContainerCloud.addView(view, CommonUtil.widthPixels(this@PointForeDetailActivity), CommonUtil.dip2px(this@PointForeDetailActivity, 150f).toInt())
                                        }
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                        scrollView!!.visibility = View.VISIBLE
                    }
                }
            })
        }).start()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
        }
    }

}
