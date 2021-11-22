package com.hlj.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ThumbnailUtils
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
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
import com.amap.api.maps.model.*
import com.hlj.common.CONST
import com.hlj.dto.FactDto
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_rail_fact.*
import kotlinx.android.synthetic.main.layout_fact_value.view.*
import kotlinx.android.synthetic.main.layout_title.*
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
class RailFactActivity : BaseFragmentActivity(), View.OnClickListener, AMapLocationListener, AMap.OnMarkerClickListener {

    private var aMap: AMap? = null //高德地图
    private var zoom = 7.8f
    private var locationLat = CONST.centerLat
    private var locationLng = CONST.centerLng
    private var locationMarker: Marker? = null

    private val itemName1 = "降水"
    private val itemName2 = "气温"
    private val itemName3 = "风速"
    private val itemName4 = "雷达拼图"
    private val itemName5 = "卫星云图"
    private var selectItemName = itemName1

    private val hour1 = "1小时"
    private val hour3 = "3小时"
    private val hour6 = "6小时"
    private val hour12 = "12小时"
    private val hour24 = "24小时"

    private val stationOption1 = "全部站"
    private val stationOption2 = "工务段"
    private val stationOption3 = "铁路段"
    private var selectStationOption = stationOption1

    private val dataList: MutableList<FactDto?> = ArrayList()
    private val markers: MutableList<Marker> = ArrayList()
    private val sdf1 = SimpleDateFormat("HH", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    private val sdf3 = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
    private val sdf4 = SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA)
    private var f0 = ""
    private var factOverlay: GroundOverlay? = null
    private val layerMap: HashMap<String, JSONObject> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rail_fact)
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
        aMap!!.setOnMarkerClickListener(this)
        aMap!!.setOnMapLoadedListener {
            CommonUtil.drawHLJJsonLine(this, aMap)
            CommonUtil.drawRailWay(this, aMap)
            startLocation()
            okHttpList()
            okHttpLayer()
        }
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        ivLegend.setOnClickListener(this)
        clCheck.setOnClickListener(this)
        ivLuoqu.setOnClickListener(this)
        clRailSection.setOnClickListener(this)
        ivLocation.setOnClickListener(this)

        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }

        addStationOptions()
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
            R.id.clCheck -> {
                if (llContainerCheck.visibility == View.VISIBLE) {
                    llContainerCheck.visibility = View.GONE
                } else {
                    llContainerCheck.visibility = View.VISIBLE
                }
            }
            R.id.ivLuoqu -> {
                if (factOverlay != null) {
                    if (factOverlay!!.isVisible) {
                        factOverlay!!.isVisible = false
                        ivLuoqu.setImageResource(R.drawable.icon_map_luoqu)
                    } else {
                        factOverlay!!.isVisible = true
                        ivLuoqu.setImageResource(R.drawable.icon_map_luoqu_press)
                    }
                }
            }
            R.id.clRailSection -> {
                if (llContainerRail.visibility == View.VISIBLE) {
                    llContainerRail.visibility = View.GONE
                } else {
                    llContainerRail.visibility = View.VISIBLE
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
        Thread {
            val url = "http://xinjiangdecision.tianqi.cn:81/Home/api/get_railway_SkData"
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
                                if (!obj.isNull("time")) {
                                    f0 = obj.getString("time")
                                }

                                if (!obj.isNull("data")) {
                                    dataList.clear()
                                    val dataArray = obj.getJSONArray("data")
                                    for (m in 0 until dataArray.length()) {
                                        val dto = FactDto()
                                        val itemObj = dataArray.getJSONObject(m)
                                        if (!itemObj.isNull("Station_Name")) {
                                            dto.stationName = itemObj.getString("Station_Name")
                                        }
                                        if (!itemObj.isNull("stationCode")) {
                                            dto.stationCode = itemObj.getString("stationCode")
                                        }
                                        if (!itemObj.isNull("Lat")) {
                                            dto.lat = itemObj.getDouble("Lat")
                                        }
                                        if (!itemObj.isNull("Lon")) {
                                            dto.lng = itemObj.getDouble("Lon")
                                        }
                                        if (!itemObj.isNull("PRE_1h")) {
                                            val value = itemObj.getString("PRE_1h")
                                            dto.factRain = value.toFloat()
                                            if (TextUtils.isEmpty(value) || TextUtils.equals(value, "999999")) {
                                                dto.factRain = 0f
                                            }
                                        }
                                        if (!itemObj.isNull("PRE_3h")) {
                                            val value = itemObj.getString("PRE_3h")
                                            dto.factRain3 = value.toFloat()
                                            if (TextUtils.isEmpty(value) || TextUtils.equals(value, "999999")) {
                                                dto.factRain3 = 0f
                                            }
                                        }
                                        if (!itemObj.isNull("PRE_6h")) {
                                            val value = itemObj.getString("PRE_6h")
                                            dto.factRain6 = value.toFloat()
                                            if (TextUtils.isEmpty(value) || TextUtils.equals(value, "999999")) {
                                                dto.factRain6 = 0f
                                            }
                                        }
                                        if (!itemObj.isNull("PRE_12h")) {
                                            val value = itemObj.getString("PRE_12h")
                                            dto.factRain12 = value.toFloat()
                                            if (TextUtils.isEmpty(value) || TextUtils.equals(value, "999999")) {
                                                dto.factRain12 = 0f
                                            }
                                        }
                                        if (!itemObj.isNull("PRE_24h")) {
                                            val value = itemObj.getString("PRE_24h")
                                            dto.factRain24 = value.toFloat()
                                            if (TextUtils.isEmpty(value) || TextUtils.equals(value, "999999")) {
                                                dto.factRain24 = 0f
                                            }
                                        }
                                        if (!itemObj.isNull("TEM")) {
                                            val value = itemObj.getString("TEM")
                                            dto.factTemp = value.toFloat()
                                            if (TextUtils.isEmpty(value) || TextUtils.equals(value, "999999")) {
                                                dto.factTemp = 0f
                                            }
                                        }
                                        if (!itemObj.isNull("WIN_S_Avg_10mi")) {
                                            val value = itemObj.getString("WIN_S_Avg_10mi")
                                            dto.factWind = value.toFloat()
                                            if (TextUtils.isEmpty(value) || TextUtils.equals(value, "999999")) {
                                                dto.factWind = 0f
                                            }
                                        }
                                        if (!itemObj.isNull("WIN_D_Avg_10mi")) {
                                            val value = itemObj.getString("WIN_D_Avg_10mi")
                                            dto.factWindDir = value.toFloat()
                                            if (TextUtils.isEmpty(value) || TextUtils.equals(value, "999999") || TextUtils.equals(value, "999017")) {
                                                dto.factWindDir = 0f
                                            }
                                        }
                                        dataList.add(dto)
                                    }
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
        val columnList: ArrayList<FactDto> = ArrayList()
        var column = FactDto()
        column.name = itemName1
        addItemOptions(column, true)
        columnList.add(column)
        column = FactDto()
        column.name = itemName2
        addItemOptions(column, false)
        columnList.add(column)
        column = FactDto()
        column.name = itemName3
        addItemOptions(column, false)
        columnList.add(column)
        column = FactDto()
        column.name = itemName4
        columnList.add(column)
        column = FactDto()
        column.name = itemName5
        columnList.add(column)

        llContainer!!.removeAllViews()
        for (i in 0 until columnList.size) {
            val dto = columnList[i]
            val tvName = TextView(this)
            tvName.text = dto.name
            tvName.gravity = Gravity.CENTER
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            tvName.setPadding(25, 0, 25, 0)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.leftMargin = CommonUtil.dip2px(this, 10f).toInt()
            tvName.layoutParams = params
            llContainer!!.addView(tvName)
            when(dto.name) {
                itemName1 -> {
                    tvName.setTextColor(Color.WHITE)
                    tvName.setBackgroundResource(R.drawable.corner_left_right_blue)
                    selectItemName = dto.name
                    ivLuoqu.visibility = View.VISIBLE
                    val start1 = sdf2.parse(f0).time-1000 * 60 * 60
                    val time = sdf2.format(start1)+"~"+f0
                    tvLayerName.text = "${tvCheck.text}${dto.name}实况\n$time"
                    addItem(dto)
                }
                else -> {
                    tvName.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                    tvName.setBackgroundResource(R.drawable.corner_left_right_gray)
                }
            }

            tvName.setOnClickListener {
                if (llContainer != null) {
                    for (n in 0 until llContainer!!.childCount) {
                        val name = llContainer!!.getChildAt(n) as TextView
                        if (TextUtils.equals(tvName.text.toString(), name.text.toString())) {
                            when(dto.name) {
                                itemName1 -> {
                                    name.setTextColor(Color.WHITE)
                                    name.setBackgroundResource(R.drawable.corner_left_right_blue)
                                    selectItemName = dto.name
                                    val start1 = sdf2.parse(f0).time-1000 * 60 * 60
                                    val time = sdf2.format(start1)+"~"+f0
                                    tvLayerName.text = "${tvCheck.text}${dto.name}实况\n$time"
                                    ivCheck.setImageResource(R.drawable.icon_rail_fore_press)
                                    ivLuoqu.visibility = View.VISIBLE
                                    addItem(dto)
                                }
                                itemName2 -> {
                                    name.setTextColor(Color.WHITE)
                                    name.setBackgroundResource(R.drawable.corner_left_right_blue)
                                    selectItemName = dto.name
                                    val start1 = sdf2.parse(f0).time-1000 * 60 * 60
                                    val time = sdf2.format(start1)+"~"+f0
                                    tvLayerName.text = "${tvCheck.text}${dto.name}实况\n$time"
                                    ivCheck.setImageResource(R.drawable.icon_temp_fore_press)
                                    ivLuoqu.visibility = View.VISIBLE
                                    addItem(dto)
                                }
                                itemName3 -> {
                                    name.setTextColor(Color.WHITE)
                                    name.setBackgroundResource(R.drawable.corner_left_right_blue)
                                    selectItemName = dto.name
                                    val start1 = sdf2.parse(f0).time-1000 * 60 * 60
                                    val time = sdf2.format(start1)+"~"+f0
                                    tvLayerName.text = "${tvCheck.text}${dto.name}实况\n$time"
                                    ivCheck.setImageResource(R.drawable.icon_wind_fore_press)
                                    ivLuoqu.visibility = View.VISIBLE
                                    addItem(dto)
                                }
                                itemName4 -> {
                                    //雷达拼图
                                    val intent = Intent(this, WebviewActivity::class.java)
                                    intent.putExtra(CONST.ACTIVITY_NAME, "雷达基本反射率")
                                    intent.putExtra(CONST.WEB_URL, "https:\\/\\/testdecision.tianqi.cn\\/data\\/page_cp\\/radar.html")
                                    startActivity(intent)
                                }
                                itemName5 -> {
                                    //卫星云图
                                    val intent = Intent(this, WebviewActivity::class.java)
                                    intent.putExtra(CONST.ACTIVITY_NAME, "风云四号气象卫星")
                                    intent.putExtra(CONST.WEB_URL, "http:\\/\\/testdecision.tianqi.cn\\/data\\/page\\/imgs.html?http:\\/\\/testdecision.tianqi.cn\\/data\\/fy4cloud_little.html")
                                    startActivity(intent)
                                }
                            }
                        } else {
                            when(dto.name) {
                                itemName1,itemName2,itemName3 -> {
                                    name.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                                    name.setBackgroundResource(R.drawable.corner_left_right_gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addItemOptions(item: FactDto, isRain: Boolean) {
        val childList: ArrayList<FactDto> = ArrayList()
        if (isRain) {
            var child = FactDto()
            child.name = hour1
            childList.add(child)
            child = FactDto()
            child.name = hour3
            childList.add(child)
            child = FactDto()
            child.name = hour6
            childList.add(child)
            child = FactDto()
            child.name = hour12
            childList.add(child)
            child = FactDto()
            child.name = hour24
            childList.add(child)
        } else {
            val child = FactDto()
            child.name = hour1
            childList.add(child)
        }
        item.itemList.addAll(childList)
    }

    private fun addItem(dto: FactDto?) {
        llContainerCheck.removeAllViews()
        for (j in 0 until dto!!.itemList.size) {
            val item = dto.itemList[j]
            val tvItem = TextView(this)
            tvItem.text = item.name
            tvItem.gravity = Gravity.CENTER
            tvItem.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f)
            val params1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, CommonUtil.dip2px(this, 35f).toInt())
            tvItem.layoutParams = params1
            llContainerCheck.addView(tvItem)
            if (j == 0) {
                tvItem.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                tvCheck.text = item.name
                addMarkers(item.name)

//                when(dto.name) {
//                    itemName1 -> {
//
//                    }
//                    itemName2 -> {
//                        if (layerMap.containsKey("${itemName2}${item.name}")) {
//                            val obj: JSONObject = layerMap["${itemName2}${item.name}"]!!
//                            okHttpFactBitmap(obj)
//                        }
//                    }
//                    itemName3 -> {
//                        if (layerMap.containsKey("${itemName3}${item.name}")) {
//                            val obj: JSONObject = layerMap["${itemName3}${item.name}"]!!
//                            okHttpFactBitmap(obj)
//                        }
//                    }
//                }
            } else {
                tvItem.setTextColor(ContextCompat.getColor(this, R.color.text_color3))
            }

            tvItem.setOnClickListener {
                for (m in 0 until llContainerCheck.childCount) {
                    val itemName = llContainerCheck.getChildAt(m) as TextView
                    if (TextUtils.equals(itemName.text.toString(), tvItem.text.toString())) {
                        itemName.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        tvCheck.text = itemName.text.toString()
                        addMarkers(itemName.text.toString())

//                        when(dto.name) {
//                            itemName1 -> {
//
//                            }
//                            itemName2 -> {
//                                if (layerMap.containsKey("${itemName2}${item.name}")) {
//                                    val obj: JSONObject = layerMap["${itemName2}${item.name}"]!!
//                                    okHttpFactBitmap(obj)
//                                }
//                            }
//                            itemName3 -> {
//                                if (layerMap.containsKey("${itemName3}${item.name}")) {
//                                    val obj: JSONObject = layerMap["${itemName3}${item.name}"]!!
//                                    okHttpFactBitmap(obj)
//                                }
//                            }
//                        }
                    } else {
                        itemName.setTextColor(ContextCompat.getColor(this, R.color.text_color3))
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
    private fun addMarkers(hourName: String) {
        removeMarkers()
        for (i in 0 until dataList.size) {
            val dto = dataList[i]
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.layout_fact_value, null)
            view.tvValue.setBgColor(ContextCompat.getColor(this, R.color.colorPrimary), Color.WHITE)
            view.tvValue.setTextColor(Color.WHITE)
            var start1 = sdf2.parse(f0).time-1000 * 60 * 60
            when(selectItemName) {
                itemName1 -> {
                    when(hourName) {
                        hour1 -> {
                            view.tvValue.text = "${dto!!.factRain}mm"
                            start1 = sdf2.parse(f0).time-1000 * 60 * 60
                        }
                        hour3 -> {
                            view.tvValue.text = "${dto!!.factRain3}mm"
                            start1 = sdf2.parse(f0).time-1000 * 60 * 60 * 3
                        }
                        hour6 -> {
                            view.tvValue.text = "${dto!!.factRain6}mm"
                            start1 = sdf2.parse(f0).time-1000 * 60 * 60 * 6
                        }
                        hour12 -> {
                            view.tvValue.text = "${dto!!.factRain12}mm"
                            start1 = sdf2.parse(f0).time-1000 * 60 * 60 * 12
                        }
                        hour24 -> {
                            view.tvValue.text = "${dto!!.factRain24}mm"
                            start1 = sdf2.parse(f0).time-1000 * 60 * 60 * 24
                        }
                    }
                    val time = sdf2.format(start1)+"~"+f0
                    tvLayerName.text = "${hourName}${itemName1}实况\n$time"
                    if (!TextUtils.isEmpty(dto!!.stationName)) {
                        view.tvName.text = dto!!.stationName
                    }
                    val options = MarkerOptions()
                    options.title(dto.stationName)
                    options.snippet(dto.stationCode)
                    options.anchor(0.5f, 0.5f)
                    options.position(LatLng(dto.lat, dto.lng))
                    options.icon(BitmapDescriptorFactory.fromView(view))
                    val marker = aMap!!.addMarker(options)
                    marker.isVisible = true
                    markers.add(marker)
                }
                itemName2 -> {
                    val time = sdf2.format(start1)+"~"+f0
                    tvLayerName.text = "${hour1}${itemName2}实况\n$time"
                    view.tvValue.text = "${dto!!.factTemp}℃"
                    if (!TextUtils.isEmpty(dto!!.stationName)) {
                        view.tvName.text = dto!!.stationName
                    }
                    val options = MarkerOptions()
                    options.title(dto.stationName)
                    options.snippet(dto.stationCode)
                    options.anchor(0.5f, 0.5f)
                    options.position(LatLng(dto.lat, dto.lng))
                    options.icon(BitmapDescriptorFactory.fromView(view))
                    val marker = aMap!!.addMarker(options)
                    marker.isVisible = true
                    markers.add(marker)
                }
                itemName3 -> {
                    val time = sdf2.format(start1)+"~"+f0
                    tvLayerName.text = "${hour1}${itemName3}实况\n$time"
                    view.tvValue.text = "${dto!!.factWind}m/s"
                    val rotation = dto!!.factWindDir
                    val b = CommonUtil.getWindMarker(this, dto!!.factWindDir.toDouble())
                    if (b != null) {
                        val matrix = Matrix()
                        matrix.postScale(1f, 1f)
                        matrix.postRotate(rotation)
                        val bitmap = Bitmap.createBitmap(b, 0, 0, b.width, b.height, matrix, true)
                        if (bitmap != null) {
                            view.ivWind.setImageBitmap(bitmap)
                            view.ivWind.visibility = View.VISIBLE
                        }
                    }
                    if (!TextUtils.isEmpty(dto.stationName)) {
                        view.tvName.text = dto.stationName
                    }
                    val options = MarkerOptions()
                    options.title(dto.stationName)
                    options.snippet(dto.stationCode)
                    options.anchor(0.5f, 0.5f)
                    options.position(LatLng(dto.lat, dto.lng))
                    options.icon(BitmapDescriptorFactory.fromView(view))
                    val marker = aMap!!.addMarker(options)
                    marker.isVisible = true
                    markers.add(marker)
                }
            }
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        val intent = Intent(this, FactDetailChartActivity::class.java)
        intent.putExtra(CONST.ACTIVITY_NAME, marker!!.title)
        intent.putExtra("stationCode", marker!!.snippet)
        startActivity(intent)
        return true
    }

    /**
     * 获取图层数据
     */
    private fun okHttpLayer() {
        Thread {
            val url = "http://hf-sos.tianqi.cn/tile_map/getcimisslayer/650000_yb"
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    if (!TextUtils.isEmpty(result)) {
                        try {
                            layerMap.clear()
                            val array = JSONArray(result)
                            for (i in 0 until array.length()) {
                                val itemObj = array.getJSONObject(i)
                                if (!itemObj.isNull("imgurl")) {
                                    val imgurl = itemObj.getString("imgurl")
                                    if (!TextUtils.isEmpty(imgurl)) {
                                        when {
                                            imgurl.contains("ybday1_24js") -> {
                                                layerMap["${itemName2}24h"] = itemObj
                                            }
                                            imgurl.contains("ybday2_24js") -> {
                                                layerMap["${itemName2}48h"] = itemObj
                                            }
                                            imgurl.contains("ybday3_24js") -> {
                                                layerMap["${itemName2}72h"] = itemObj
                                            }
                                            imgurl.contains("ybday4_24js") -> {
                                                layerMap["${itemName2}96h"] = itemObj
                                            }
                                            imgurl.contains("ybday5_24js") -> {
                                                layerMap["${itemName2}120h"] = itemObj
                                            }
                                            imgurl.contains("ybday6_24js") -> {
                                                layerMap["${itemName2}144h"] = itemObj
                                            }
                                            imgurl.contains("ybday7_24js") -> {
                                                layerMap["${itemName2}168h"] = itemObj
                                            }

                                            imgurl.contains("ybday1_wind") -> {
                                                layerMap["${itemName3}24h"] = itemObj
                                            }
                                            imgurl.contains("ybday2_wind") -> {
                                                layerMap["${itemName3}48h"] = itemObj
                                            }
                                            imgurl.contains("ybday3_wind") -> {
                                                layerMap["${itemName3}72h"] = itemObj
                                            }
                                            imgurl.contains("ybday4_wind") -> {
                                                layerMap["${itemName3}96h"] = itemObj
                                            }
                                            imgurl.contains("ybday5_wind") -> {
                                                layerMap["${itemName3}120h"] = itemObj
                                            }
                                            imgurl.contains("ybday6_wind") -> {
                                                layerMap["${itemName3}144h"] = itemObj
                                            }
                                            imgurl.contains("ybday7_wind") -> {
                                                layerMap["${itemName3}168h"] = itemObj
                                            }

                                            imgurl.contains("ybday1_h_temp") -> {
                                                layerMap["${itemName4}24h"] = itemObj
                                            }
                                            imgurl.contains("ybday2_h_temp") -> {
                                                layerMap["${itemName4}48h"] = itemObj
                                            }
                                            imgurl.contains("ybday3_h_temp") -> {
                                                layerMap["${itemName4}72h"] = itemObj
                                            }
                                            imgurl.contains("ybday4_h_temp") -> {
                                                layerMap["${itemName4}96h"] = itemObj
                                            }
                                            imgurl.contains("ybday5_h_temp") -> {
                                                layerMap["${itemName4}120h"] = itemObj
                                            }
                                            imgurl.contains("ybday6_h_temp") -> {
                                                layerMap["${itemName4}144h"] = itemObj
                                            }
                                            imgurl.contains("ybday7_h_temp") -> {
                                                layerMap["${itemName4}168h"] = itemObj
                                            }

                                            imgurl.contains("ybday1_l_temp") -> {
                                                layerMap["${itemName5}24h"] = itemObj
                                            }
                                            imgurl.contains("ybday2_l_temp") -> {
                                                layerMap["${itemName5}48h"] = itemObj
                                            }
                                            imgurl.contains("ybday3_l_temp") -> {
                                                layerMap["${itemName5}72h"] = itemObj
                                            }
                                            imgurl.contains("ybday4_l_temp") -> {
                                                layerMap["${itemName5}96h"] = itemObj
                                            }
                                            imgurl.contains("ybday5_l_temp") -> {
                                                layerMap["${itemName5}120h"] = itemObj
                                            }
                                            imgurl.contains("ybday6_l_temp") -> {
                                                layerMap["${itemName5}144h"] = itemObj
                                            }
                                            imgurl.contains("ybday7_l_temp") -> {
                                                layerMap["${itemName5}168h"] = itemObj
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        }.start()
    }

    /**
     * 获取并绘制图层
     */
    private fun okHttpFactBitmap(imgObj: JSONObject) {
        try {
            val maxlat = imgObj.getDouble("maxlat")
            val maxlon = imgObj.getDouble("maxlon")
            val minlat = imgObj.getDouble("minlat")
            val minlon = imgObj.getDouble("minlon")
            val imgurl = imgObj.getString("imgurl")
            Log.e("imgurl", imgurl)
            if (!imgObj.isNull("cutlineUrl")) {
                val cutlineUrl = imgObj.getString("cutlineUrl")
                if (!TextUtils.isEmpty(cutlineUrl)) {
                    Picasso.get().load(cutlineUrl).into(ivChart)
                }
            }
            Thread {
                OkHttpUtil.enqueue(Request.Builder().url(imgurl).build(), object : Callback {
                    override fun onFailure(call: Call, e: IOException) {}

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (!response.isSuccessful) {
                            return
                        }
                        val bytes = response.body!!.bytes()
                        runOnUiThread {
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            if (bitmap != null) {
                                drawFactBitmap(bitmap, LatLng(maxlat, maxlon), LatLng(minlat, minlon))
                            }
                        }
                    }
                })
            }.start()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    /**
     * 绘制实况图
     */
    private fun drawFactBitmap(bitmap: Bitmap?, max: LatLng, min: LatLng) {
        if (bitmap == null) {
            return
        }
        val fromView = BitmapDescriptorFactory.fromBitmap(bitmap)
        val bounds = LatLngBounds.Builder()
                .include(max)
                .include(min)
                .build()

//        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        if (factOverlay == null) {
            factOverlay = aMap!!.addGroundOverlay(GroundOverlayOptions()
                    .anchor(0.5f, 0.5f)
                    .positionFromBounds(bounds)
                    .image(fromView)
                    .transparency(0.2f))
        } else {
            factOverlay!!.setImage(null)
            factOverlay!!.setPositionFromBounds(bounds)
            factOverlay!!.setImage(fromView)
        }
    }

    /**
     * 增加站点选择
     */
    private fun addStationOptions() {
        val optionList: ArrayList<String> = ArrayList()
        optionList.add(stationOption1)
        optionList.add(stationOption2)
        optionList.add(stationOption3)

        llContainerRail.removeAllViews()
        for (j in 0 until optionList.size) {
            val name = optionList[j]
            val tvItem = TextView(this)
            tvItem.text = name
            tvItem.gravity = Gravity.CENTER
            tvItem.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f)
            val params1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, CommonUtil.dip2px(this, 35f).toInt())
            tvItem.layoutParams = params1
            llContainerRail.addView(tvItem)
            if (TextUtils.equals(name, stationOption1)) {
                tvItem.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                tvRailSection.text = name
            } else {
                tvItem.setTextColor(ContextCompat.getColor(this, R.color.text_color3))
            }

            tvItem.setOnClickListener {
                for (m in 0 until llContainerRail.childCount) {
                    val itemName = llContainerRail.getChildAt(m) as TextView
                    if (TextUtils.equals(itemName.text.toString(), tvItem.text.toString())) {
                        itemName.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        tvRailSection.text = itemName.text.toString()
                        when(itemName.text.toString()) {
                            stationOption1 -> {
                                for (i in 0 until markers.size) {
                                    val marker = markers[i]
                                    marker.isVisible = true
                                }
                            }
                            stationOption2 -> {

                            }
                            stationOption3 -> {
                                startActivityForResult(Intent(this, RailSectionActivity::class.java), 1001)
                            }
                        }
                    } else {
                        itemName.setTextColor(ContextCompat.getColor(this, R.color.text_color3))
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when(requestCode) {
                1001 -> {
                    if (data != null) {
                        val stationCodes = data.getStringExtra("stationCodes")
                        for (i in 0 until markers.size) {
                            val marker = markers[i]
                            marker.isVisible = stationCodes.contains(marker.snippet)
                        }
                    }
                }
            }
        }
    }

}
