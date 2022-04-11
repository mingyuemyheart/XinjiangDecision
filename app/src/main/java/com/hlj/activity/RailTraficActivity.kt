package com.hlj.activity

import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ThumbnailUtils
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.hlj.common.CONST
import com.hlj.dto.FactDto
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_rail_trafic.*
import kotlinx.android.synthetic.main.layout_fact_trafic.view.*
import kotlinx.android.synthetic.main.layout_fact_value.view.*
import kotlinx.android.synthetic.main.layout_title.*
import kotlinx.android.synthetic.main.weather_marker_info.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import shawn.cxwl.com.hlj.R
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * 专业服务-铁路气象服务-实况
 */
class RailTraficActivity : BaseFragmentActivity(), View.OnClickListener, AMapLocationListener,AMap.OnMapClickListener, AMap.OnMarkerClickListener, AMap.InfoWindowAdapter {

    private var localId = ""
    private var aMap: AMap? = null //高德地图
    private var zoom = 7.8f
    private var locationLat = CONST.centerLat
    private var locationLng = CONST.centerLng
    private var locationMarker: Marker? = null
    private var selectMarker: Marker? = null

    private val defaultName = "当前"
    private var selectItemName = defaultName

    private val dataList: MutableList<FactDto?> = ArrayList()
    private val markers: MutableList<Marker> = ArrayList()
    private val sdf1 = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA)
    private val layerMap: HashMap<String, JSONObject> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rail_trafic)
        localId = intent.getStringExtra(CONST.LOCAL_ID)
        initAmap(savedInstanceState)
        initWidget()
    }

    /**
     * 初始化高德地图
     */
    private fun initAmap(bundle: Bundle?) {
        mapView.visibility = View.VISIBLE
        mapView.onCreate(bundle)
        if (aMap == null) {
            aMap = mapView.map
        }
        aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(CONST.guizhouLatLng, zoom))
        aMap!!.uiSettings.isMyLocationButtonEnabled = false // 设置默认定位按钮是否显示
        aMap!!.uiSettings.isZoomControlsEnabled = false
        aMap!!.uiSettings.isRotateGesturesEnabled = false
        aMap!!.showMapText(false)
        aMap!!.setOnMapClickListener(this)
        aMap!!.setOnMarkerClickListener(this)
        aMap!!.setInfoWindowAdapter(this)
        aMap!!.setOnMapLoadedListener {
            startLocation()
            okHttpList()
        }
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        ivLegend.setOnClickListener(this)
        ivLocation.setOnClickListener(this)

        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }
    }

    /**
     * 开始定位
     */
    private fun startLocation() {
        val mLocationOption = AMapLocationClientOption() //初始化定位参数
        val mLocationClient = AMapLocationClient(this) //初始化定位
        mLocationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.isNeedAddress = true //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.isOnceLocation = true //设置是否只定位一次,默认为false
        mLocationOption.isMockEnable = false //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.interval = 2000 //设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption) //给定位客户端对象设置定位参数
        mLocationClient.setLocationListener(this)
        mLocationClient.startLocation() //启动定位
    }

    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (amapLocation != null && amapLocation.errorCode == AMapLocation.LOCATION_SUCCESS) {
            locationLat = amapLocation.latitude
            locationLng = amapLocation.longitude
        }
        if (locationMarker != null) {
            locationMarker!!.remove()
        }
        val latLng = LatLng(locationLat, locationLng)
        val options = MarkerOptions()
        options.anchor(0.5f, 1.0f)
        val bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(resources, R.drawable.icon_map_location),
                CommonUtil.dip2px(this, 21f).toInt(), CommonUtil.dip2px(this, 32f).toInt())
        if (bitmap != null) {
            options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        } else {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_location))
        }
        options.position(latLng)
        locationMarker = aMap!!.addMarker(options)
        locationMarker!!.isClickable = false
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> finish()
            R.id.ivLegend -> {
                if (ivChart.visibility == View.VISIBLE) {
                    ivChart.visibility = View.GONE
                } else {
                    ivChart.visibility = View.VISIBLE
                }
            }
            R.id.ivLocation -> {
                if (zoom >= 12f) {
                    aMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locationLat, locationLng), 3.5f))
                } else {
                    aMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locationLat, locationLng), 12.0f))
                }
            }
        }
    }

    private fun okHttpList() {
        showDialog()
        val url = intent.getStringExtra(CONST.WEB_URL)
        if (TextUtils.isEmpty(url)) {
            cancelDialog()
            return
        }
        Thread {
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
                                dataList.clear()
                                val dataArray = JSONArray(result)
                                for (m in 0 until dataArray.length()) {
                                    val dto = FactDto()
                                    val itemObj = dataArray.getJSONObject(m)
                                    if (!itemObj.isNull("id")) {
                                        dto.id = itemObj.getString("id")
                                    }
                                    if (!itemObj.isNull("roadcode")) {
                                        dto.roadcode = itemObj.getString("roadcode")
                                    }
                                    if (!itemObj.isNull("altitude")) {
                                        dto.altitude = itemObj.getString("altitude")
                                    }
                                    if (!itemObj.isNull("province")) {
                                        dto.province = itemObj.getString("province")
                                    }
                                    if (!itemObj.isNull("city")) {
                                        dto.city = itemObj.getString("city")
                                    }
                                    if (!itemObj.isNull("area")) {
                                        dto.area = itemObj.getString("area")
                                    }
                                    if (!itemObj.isNull("road")) {
                                        dto.road = itemObj.getString("road")
                                    }
                                    if (!itemObj.isNull("time")) {
                                        dto.time = itemObj.getString("time")
                                    }
                                    if (!itemObj.isNull("lat")) {
                                        dto.lat = itemObj.getDouble("lat")
                                    }
                                    if (!itemObj.isNull("lon")) {
                                        dto.lng = itemObj.getDouble("lon")
                                    }
                                    if (!itemObj.isNull("data")) {
                                        val dataObj = itemObj.getJSONObject("data")
                                        if (!dataObj.isNull("vis")) {
                                            val list: MutableList<String> = ArrayList()
                                            val itemArray = dataObj.getJSONArray("vis")
                                            for (j in 0 until itemArray.length()) {
                                                list.add(itemArray.getString(j))
                                            }
                                            dto.visList.addAll(list)
                                        }
                                        if (!dataObj.isNull("gale")) {
                                            val list: MutableList<String> = ArrayList()
                                            val itemArray = dataObj.getJSONArray("gale")
                                            for (j in 0 until itemArray.length()) {
                                                list.add(itemArray.getString(j))
                                            }
                                            dto.galeList.addAll(list)
                                        }
                                        if (!dataObj.isNull("rain")) {
                                            val list: MutableList<String> = ArrayList()
                                            val itemArray = dataObj.getJSONArray("rain")
                                            for (j in 0 until itemArray.length()) {
                                                list.add(itemArray.getString(j))
                                            }
                                            dto.rainList.addAll(list)
                                        }
                                        if (!dataObj.isNull("tem")) {
                                            val list: MutableList<String> = ArrayList()
                                            val itemArray = dataObj.getJSONArray("tem")
                                            for (j in 0 until itemArray.length()) {
                                                list.add(itemArray.getString(j))
                                            }
                                            dto.temList.addAll(list)
                                        }
                                        if (!dataObj.isNull("road")) {
                                            val list: MutableList<String> = ArrayList()
                                            val itemArray = dataObj.getJSONArray("road")
                                            for (j in 0 until itemArray.length()) {
                                                list.add(itemArray.getString(j))
                                            }
                                            dto.roadList.addAll(list)
                                        }
                                        if (!dataObj.isNull("comprehensive")) {
                                            val list: MutableList<String> = ArrayList()
                                            val itemArray = dataObj.getJSONArray("comprehensive")
                                            for (j in 0 until itemArray.length()) {
                                                list.add(itemArray.getString(j))
                                            }
                                            dto.comList.addAll(list)
                                        }
                                    }
                                    dataList.add(dto)
                                }

                                addColumn()
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
        for (i in 0 until 73) {
            val tvName = TextView(this)
            if (i == 0) {
                tvName.text = defaultName
                tvName.setTextColor(Color.WHITE)
                tvName.setBackgroundResource(R.drawable.corner_left_right_blue)
                selectItemName = tvName.text.toString()
                addMarkers()
            } else {
                tvName.text = "${i}小时"
                tvName.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvName.setBackgroundResource(R.drawable.corner_left_right_gray)
            }
            tvName.gravity = Gravity.CENTER
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            tvName.setPadding(25, 0, 25, 0)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.leftMargin = CommonUtil.dip2px(this, 10f).toInt()
            tvName.layoutParams = params
            llContainer!!.addView(tvName)

            tvName.setOnClickListener {
                if (llContainer != null) {
                    for (n in 0 until llContainer!!.childCount) {
                        val name = llContainer!!.getChildAt(n) as TextView
                        if (TextUtils.equals(tvName.text.toString(), name.text.toString())) {
                            name.setTextColor(Color.WHITE)
                            name.setBackgroundResource(R.drawable.corner_left_right_blue)
                            selectItemName = tvName.text.toString()
                            addMarkers()
                        } else {
                            name.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                            name.setBackgroundResource(R.drawable.corner_left_right_gray)
                        }
                    }
                }
            }
        }
    }

    /**
     * 清除站点信息
     */
    private fun removeMarkers() {
        for (i in markers.indices) {
            markers[i].remove()
        }
        markers.clear()
    }

    /**
     * 区分1小时~24小时
     */
    private fun addMarkers() {
        removeMarkers()
        for (i in 0 until dataList.size) {
            val dto = dataList[i]
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.layout_fact_trafic, null)

            val index = if (TextUtils.equals(selectItemName, defaultName)) {
                0
            } else {
                val hour = selectItemName.replace("小时", "")
                hour.toInt()
            }
            for (j in 0 until dto!!.comList.size) {
                if (j == index) {
                    val value = dto!!.comList[j]
                    var djhy = ""//等级含义
                    var fxdj = ""//风险等级
                    var gzjb = ""//管制级别
                    when(value) {
                        "0" -> {
                            view.imageView.setImageResource(R.drawable.icon_circle_0)
                            djhy = "无风险"
                            fxdj = "无风险"
                            gzjb = ""
                        }
                        "1" -> {
                            view.imageView.setImageResource(R.drawable.icon_circle_1)
                            djhy = "一般风险"
                            fxdj = "蓝色(Ⅳ)"
                            gzjb = "四级管制"
                        }
                        "2" -> {
                            view.imageView.setImageResource(R.drawable.icon_circle_2)
                            djhy = "较高风险"
                            fxdj = "黄色(Ⅲ)"
                            gzjb = "三级管制"
                        }
                        "3" -> {
                            view.imageView.setImageResource(R.drawable.icon_circle_3)
                            djhy = "很高风险"
                            fxdj = "橙色(Ⅱ)"
                            gzjb = "二极管制"
                        }
                        "4" -> {
                            view.imageView.setImageResource(R.drawable.icon_circle_4)
                            djhy = "严重风险"
                            fxdj = "红色(Ⅰ)"
                            gzjb = "一级管制"
                        }
                    }
                    val time = sdf1.format(sdf2.parse(dto.time).time+1000*60*60*index)
                    tvLayerName.text = "全疆公路交管风险预警\n$time"
                    val options = MarkerOptions()
                    options.title(dto.city+dto.road)
                    options.snippet("公路桩号：${dto.roadcode}\n海拔高度：${dto.altitude}\n所有省份：${dto.province}\n所有城市：${dto.city}\n所有区县：${dto.area}\n预警类型：\n风险等级：${fxdj}\n等级含义：${djhy}\n管制级别：${gzjb}")
                    options.anchor(0.5f, 0.5f)
                    options.position(LatLng(dto.lat, dto.lng))
                    options.icon(BitmapDescriptorFactory.fromView(view))
                    val marker = aMap!!.addMarker(options)
                    markers.add(marker)
                }
            }
        }
    }

    override fun onMapClick(arg0: LatLng?) {
        if (selectMarker != null) {
            selectMarker!!.hideInfoWindow()
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null) {
            selectMarker = marker
            selectMarker!!.showInfoWindow()
        }
        return true
    }

    override fun getInfoContents(marker: Marker): View? {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_trafic_marker_info, null)
        view.tvName.text = marker.title
        view.tvContent.text = marker.snippet
        return view
    }

    override fun getInfoWindow(arg0: Marker?): View? {
        return null
    }

}
