package com.hlj.activity

import android.content.Intent
import android.graphics.*
import android.media.ThumbnailUtils
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.hlj.common.CONST
import com.hlj.dto.FactDto
import com.hlj.dto.WeatherDto
import com.hlj.manager.XiangJiManager
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.hlj.utils.WeatherUtil
import com.hlj.view.WeeklyView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_rail_fore.*
import kotlinx.android.synthetic.main.layout_fact_value.view.*
import kotlinx.android.synthetic.main.layout_rail_fore_marker.view.*
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * 专业服务-铁路气象服务-预报
 */
class RailForeActivity : BaseFragmentActivity(), View.OnClickListener, AMapLocationListener, AMap.OnMarkerClickListener, AMap.OnMapClickListener {

    private var localId = ""
    private var aMap: AMap? = null //高德地图
    private var zoom = 7.8f
    private var locationLat = CONST.centerLat
    private var locationLng = CONST.centerLng
    private var locationMarker: Marker? = null
    private val polylines: ArrayList<Polyline> = ArrayList()
    private var isShowMarker = true

    private val itemName1 = "综合预报"
    private val itemName2 = "降水"
    private val itemName3 = "风速"
    private val itemName4 = "最高温"
    private val itemName5 = "最低温"
    private var selectItemName = itemName1

    private val dataList: MutableList<FactDto?> = ArrayList()
    private val markers: MutableList<Marker> = ArrayList()
    private val sdf1 = SimpleDateFormat("HH", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    private val sdf3 = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
    private val sdf4 = SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA)
    private val dayAqiList: ArrayList<WeatherDto> = ArrayList()
    private var selectMarker: Marker? = null
    private var f0 = ""
    private var foreDate: Long = 0//获取预报时间戳
    private var factOverlay: GroundOverlay? = null
    private val layerMap: HashMap<String, JSONObject> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rail_fore)
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
        aMap!!.setOnMarkerClickListener(this)
        aMap!!.setOnMapClickListener(this)
        aMap!!.setOnMapLoadedListener {
            CommonUtil.drawHLJJsonLine(this, aMap)
            drawRailWay("全部站")
            startLocation()
            getWeatherInfo()
            okHttpLayer()
        }
    }

    private fun drawRailWay(lineName: String) {
        for (i in 0 until polylines.size) {
            val polyline = polylines[i]
            polyline.remove()
        }
        polylines.clear()
        when(localId) {
            "9102" -> CommonUtil.drawRailWay(this, aMap, polylines,lineName)
            "9202" -> CommonUtil.drawRoadLine(this, aMap, polylines,lineName)
            "9302" -> {}
        }
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        ivLegend.setOnClickListener(this)
        clCheck.setOnClickListener(this)
        ivLuoqu.setOnClickListener(this)
        clRailSection.setOnClickListener(this)
        ivShowMarker.setOnClickListener(this)
        ivLocation.setOnClickListener(this)
        ivClose.setOnClickListener(this)

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
        addLocationMarker()
    }

    private fun addLocationMarker() {
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
                if (llContainer1.visibility == View.VISIBLE) {
                    llContainer1.visibility = View.GONE
                } else {
                    llContainer1.visibility = View.VISIBLE
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
                val intent = Intent(this, RailSectionActivity::class.java)
                intent.putExtra(CONST.LOCAL_ID, localId)
                startActivityForResult(intent, 1001)
            }
            R.id.ivShowMarker -> {
                isShowMarker = !isShowMarker
                if (isShowMarker) {
                    ivShowMarker.setImageResource(R.drawable.icon_map_marker_show)
                } else {
                    ivShowMarker.setImageResource(R.drawable.icon_map_marker_hide)
                }
                showingMarkers()
            }
            R.id.ivLocation -> {
                if (zoom >= 12f) {
                    aMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locationLat, locationLng), 3.5f))
                } else {
                    aMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locationLat, locationLng), 12.0f))
                }
            }
            R.id.ivClose -> CommonUtil.topToBottom(clBottom)
        }
    }

    private var stationCodes = ""
    private fun showingMarkers() {
        for (i in 0 until markers.size) {
            val marker = markers[i]
            if (TextUtils.isEmpty(stationCodes)) {
                marker.isVisible = isShowMarker
            } else {
                if (marker.snippet.contains(",")) {
                    val snippets = marker.snippet.split(",")
                    if (stationCodes.contains(snippets[1])) {
                        marker.isVisible = isShowMarker
                    } else {
                        marker.isVisible = false
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
                        val stationName = data.getStringExtra("stationName")
                        if (!TextUtils.isEmpty(stationName)) {
                            drawRailWay(stationName)
                            tvRailSection.text = stationName
                            if (stationName.length >= 3) {
                                tvRailSection.text = stationName.substring(0, 3)
                            }
                        }
                        if (TextUtils.equals(stationName, "全部站")) {
                            for (i in 0 until markers.size) {
                                val marker = markers[i]
                                marker.isVisible = isShowMarker
                            }
                            return
                        }
                        stationCodes = data.getStringExtra("stationCodes")
                        showingMarkers()
                    }
                }
            }
        }
    }

    private fun getWeatherInfo() {
        showDialog()
        var url = ""
        when(localId) {
            "9102" -> url = "http://xinjiangdecision.tianqi.cn:81/home/work/stationYBData"
            "9202" -> url = "http://xinjiangdecision.tianqi.cn:81/home/work/highway_stationYBData"
            "9302" -> url = ""
        }
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
                                val obj = JSONObject(result)
                                f0 = sdf4.format(Date())
                                try {
                                    if (!obj.isNull("time")) {
                                        val time = obj.getString("time")
                                        if (!TextUtils.isEmpty(time)) {
                                            f0 = time+"00"
                                            foreDate = sdf4.parse(time).time
                                        }
                                    }
                                } catch (e: ParseException) {
                                    e.printStackTrace()
                                }

                                if (!obj.isNull("data")) {
                                    dataList.clear()
                                    val dataArray = obj.getJSONArray("data")
                                    for (m in 0 until dataArray.length()) {
                                        val fact = FactDto()
                                        val itemObj = dataArray.getJSONObject(m)

                                        if (!itemObj.isNull("trip")) {
                                            val trip = itemObj.getJSONObject("trip")
                                            if (!trip.isNull("tlid")) {
                                                fact.id = trip.getString("tlid")
                                            }
                                            if (!trip.isNull("stationName")) {
                                                fact.stationName = trip.getString("stationName")
                                            }
                                            if (!trip.isNull("stationCode")) {
                                                fact.stationCode = trip.getString("stationCode")
                                            }
                                            if (!trip.isNull("lat")) {
                                                fact.lat = trip.getDouble("lat")
                                            }
                                            if (!trip.isNull("lon")) {
                                                fact.lng = trip.getDouble("lon")
                                            }
                                        }

                                        if (!itemObj.isNull("forecast")) {
                                            val weeklyList: ArrayList<WeatherDto> = ArrayList()
                                            val f1 = itemObj.getJSONArray("forecast")
                                            for (i in 0 until f1.length()) {
                                                val dto = WeatherDto()

                                                //预报时间
                                                dto.date = CommonUtil.getDate(f0, i) //日期
                                                dto.week = CommonUtil.getWeek(f0, i) //星期几

                                                //预报内容
                                                val weeklyObj = f1.getJSONObject(i)

                                                //晚上
                                                val two = weeklyObj.getString("WeatherPhenomena2")
                                                if (!TextUtils.isEmpty(two) && !TextUtils.equals(two, "?") && !TextUtils.equals(two, "null")) {
                                                    dto.lowPheCode = Integer.valueOf(two)
                                                    dto.lowPhe = getString(WeatherUtil.getWeatherId(dto.lowPheCode))
                                                }
                                                val four = weeklyObj.getString("Ltem")
                                                if (!TextUtils.isEmpty(two) && !TextUtils.equals(two, "?") && !TextUtils.equals(two, "null")) {
                                                    dto.lowTemp = Integer.valueOf(four)
                                                }

                                                //白天
                                                val one = weeklyObj.getString("WeatherPhenomena2")
                                                if (!TextUtils.isEmpty(one) && !TextUtils.equals(one, "?") && !TextUtils.equals(one, "null")) {
                                                    dto.highPheCode = Integer.valueOf(one)
                                                    dto.highPhe = getString(WeatherUtil.getWeatherId(dto.highPheCode))
                                                }
                                                val three = weeklyObj.getString("Htem")
                                                if (!TextUtils.isEmpty(three) && !TextUtils.equals(three, "?") && !TextUtils.equals(three, "null")) {
                                                    dto.highTemp = Integer.valueOf(three)
                                                }

                                                val hour = sdf1.format(Date()).toInt()
                                                if (hour in 5..17) {
                                                    val seven = weeklyObj.getString("WindDir2")
                                                    if (!TextUtils.isEmpty(seven) && !TextUtils.equals(seven, "?") && !TextUtils.equals(seven, "null")) {
                                                        dto.windDir = Integer.valueOf(seven)
                                                    }
                                                    val five = weeklyObj.getString("WindPow2")
                                                    if (!TextUtils.isEmpty(five) && !TextUtils.equals(five, "?") && !TextUtils.equals(five, "null")) {
                                                        dto.windForce = Integer.valueOf(five)
                                                        dto.windForceString = WeatherUtil.getDayWindForce(dto.windForce)
                                                    }
                                                } else {
                                                    val eight = weeklyObj.getString("WindDir2")
                                                    if (!TextUtils.isEmpty(eight) && !TextUtils.equals(eight, "?") && !TextUtils.equals(eight, "null")) {
                                                        dto.windDir = Integer.valueOf(eight)
                                                    }
                                                    val six = weeklyObj.getString("WindPow2")
                                                    if (!TextUtils.isEmpty(six) && !TextUtils.equals(six, "?") && !TextUtils.equals(six, "null")) {
                                                        dto.windForce = Integer.valueOf(six)
                                                        dto.windForceString = WeatherUtil.getDayWindForce(dto.windForce)
                                                    }
                                                }

                                                //降水
                                                val rain = weeklyObj.getString("rain24")
                                                if (!TextUtils.isEmpty(rain) && !TextUtils.equals(rain, "?") && !TextUtils.equals(rain, "null")) {
                                                    dto.rain = rain
                                                }

                                                weeklyList.add(dto)
                                            }
                                            fact.weeklyList.addAll(weeklyList)
                                        }

                                        dataList.add(fact)
                                    }
                                }

                                addColumn(f0)
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
    private fun addColumn(f0: String) {
        val foreTime = sdf2.format(sdf4.parse(f0))

        val columnList: ArrayList<FactDto> = ArrayList()
        var column = FactDto()
        column.name = itemName1
        addItemOptions(column)
        columnList.add(column)
        column = FactDto()
        column.name = itemName2
        addItemOptions(column)
        columnList.add(column)
        column = FactDto()
        column.name = itemName3
        addItemOptions(column)
        columnList.add(column)
        column = FactDto()
        column.name = itemName4
        addItemOptions(column)
        columnList.add(column)
        column = FactDto()
        column.name = itemName5
        addItemOptions(column)
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
                    tvLayerName.text = "全疆站点${dto.name}\n$foreTime"
                    addItem(dto)
                }
                else -> {
                    tvName.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                    tvName.setBackgroundResource(R.drawable.corner_left_right_gray)
                }
            }

            tvName.setOnClickListener { arg0 ->
                if (llContainer != null) {
                    for (n in 0 until llContainer!!.childCount) {
                        val name = llContainer!!.getChildAt(n) as TextView
                        if (TextUtils.equals(tvName.text.toString(), name.text.toString())) {
                            name.setTextColor(Color.WHITE)
                            name.setBackgroundResource(R.drawable.corner_left_right_blue)
                            selectItemName = dto.name
                            tvRailSection.text = "全部站"
                            stationCodes = ""
                            tvLayerName.text = "全疆站点${dto.name}\n$foreTime"
                            when(dto.name) {
                                itemName1 -> {
                                    ivCheck.setImageResource(R.drawable.icon_rail_fore_press)
                                    ivLuoqu.visibility = View.GONE
                                    ivLegend.visibility = View.GONE
                                    ivChart.visibility = View.GONE
                                }
                                itemName2 -> {
                                    ivCheck.setImageResource(R.drawable.icon_rain_fore_press)
                                    ivLuoqu.visibility = View.VISIBLE
                                    ivLegend.visibility = View.VISIBLE
                                }
                                itemName3 -> {
                                    ivCheck.setImageResource(R.drawable.icon_wind_fore_press)
                                    ivLuoqu.visibility = View.VISIBLE
                                    ivLegend.visibility = View.VISIBLE
                                }
                                itemName4 -> {
                                    ivCheck.setImageResource(R.drawable.icon_temp_fore_press)
                                    ivLuoqu.visibility = View.VISIBLE
                                    ivLegend.visibility = View.VISIBLE
                                }
                                itemName5 -> {
                                    ivCheck.setImageResource(R.drawable.icon_temp_fore_press)
                                    ivLuoqu.visibility = View.VISIBLE
                                    ivLegend.visibility = View.VISIBLE
                                }
                            }
                            addItem(dto)
                        } else {
                            name.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                            name.setBackgroundResource(R.drawable.corner_left_right_gray)
                        }
                    }
                }
            }
        }
    }

    private fun addItemOptions(item: FactDto) {
        val childList: ArrayList<FactDto> = ArrayList()
        var child = FactDto()
        child.name = "24h"
        childList.add(child)
        child = FactDto()
        child.name = "48h"
        childList.add(child)
        child = FactDto()
        child.name = "72h"
        childList.add(child)
        child = FactDto()
        child.name = "96h"
        childList.add(child)
        child = FactDto()
        child.name = "120h"
        childList.add(child)
        child = FactDto()
        child.name = "144h"
        childList.add(child)
        child = FactDto()
        child.name = "168h"
        childList.add(child)
        item.itemList.addAll(childList)
    }

    private fun addItem(dto: FactDto?) {
        llContainer1.removeAllViews()
        for (j in 0 until dto!!.itemList.size) {
            val item = dto.itemList[j]
            val tvItem = TextView(this)
            tvItem.text = item.name
            tvItem.gravity = Gravity.CENTER
            tvItem.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f)
            val params1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, CommonUtil.dip2px(this, 35f).toInt())
            tvItem.layoutParams = params1
            llContainer1.addView(tvItem)
            if (j == 0) {
                tvItem.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                tvCheck.text = item.name
                addMarkers(j)

                when(dto.name) {
                    itemName1 -> {

                    }
                    itemName2 -> {
                        if (layerMap.containsKey("${itemName2}${item.name}")) {
                            val obj: JSONObject = layerMap["${itemName2}${item.name}"]!!
                            okHttpFactBitmap(obj)
                        }
                    }
                    itemName3 -> {
                        if (layerMap.containsKey("${itemName3}${item.name}")) {
                            val obj: JSONObject = layerMap["${itemName3}${item.name}"]!!
                            okHttpFactBitmap(obj)
                        }
                    }
                    itemName4 -> {
                        if (layerMap.containsKey("${itemName4}${item.name}")) {
                            val obj: JSONObject = layerMap["${itemName4}${item.name}"]!!
                            okHttpFactBitmap(obj)
                        }
                    }
                    itemName5 -> {
                        if (layerMap.containsKey("${itemName5}${item.name}")) {
                            val obj: JSONObject = layerMap["${itemName5}${item.name}"]!!
                            okHttpFactBitmap(obj)
                        }
                    }
                }
            } else {
                tvItem.setTextColor(ContextCompat.getColor(this, R.color.text_color3))
            }

            tvItem.setOnClickListener {
                for (m in 0 until llContainer1.childCount) {
                    val itemName = llContainer1.getChildAt(m) as TextView
                    if (TextUtils.equals(itemName.text.toString(), tvItem.text.toString())) {
                        itemName.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        tvCheck.text = itemName.text.toString()
                        addMarkers(m)

                        when(dto.name) {
                            itemName1 -> {

                            }
                            itemName2 -> {
                                if (layerMap.containsKey("${itemName2}${item.name}")) {
                                    val obj: JSONObject = layerMap["${itemName2}${item.name}"]!!
                                    okHttpFactBitmap(obj)
                                }
                            }
                            itemName3 -> {
                                if (layerMap.containsKey("${itemName3}${item.name}")) {
                                    val obj: JSONObject = layerMap["${itemName3}${item.name}"]!!
                                    okHttpFactBitmap(obj)
                                }
                            }
                            itemName4 -> {
                                if (layerMap.containsKey("${itemName4}${item.name}")) {
                                    val obj: JSONObject = layerMap["${itemName4}${item.name}"]!!
                                    okHttpFactBitmap(obj)
                                }
                            }
                            itemName5 -> {
                                if (layerMap.containsKey("${itemName5}${item.name}")) {
                                    val obj: JSONObject = layerMap["${itemName5}${item.name}"]!!
                                    okHttpFactBitmap(obj)
                                }
                            }
                        }
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
     * 预报数据下标，匹配24h~168h
     */
    private fun addMarkers(index: Int) {
        removeMarkers()
        for (i in 0 until dataList.size) {
            val dto = dataList[i]
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var view = inflater.inflate(R.layout.layout_fact_value, null)
            val weatherDto = dto!!.weeklyList[index]
            when(selectItemName) {
                itemName1 -> {
                    view = inflater.inflate(R.layout.layout_rail_fore_marker, null)
                    val hour = sdf1.format(Date()).toInt()
                    val bitmap = if (hour in 5..17) {
                        WeatherUtil.getBitmap(this, weatherDto.highPheCode)
                    } else {
                        WeatherUtil.getNightBitmap(this, weatherDto.lowPheCode)
                    }
                    if (bitmap != null) {
                        view.ivPhe.setImageBitmap(bitmap)
                    }
                    view.tvStationName.text = dto.stationName
                    val options = MarkerOptions()
                    options.title(dto.stationName)
                    options.snippet(dto.stationCode+","+dto.id)
                    options.anchor(0.5f, 0.5f)
                    options.position(LatLng(dto.lat, dto.lng))
                    options.icon(BitmapDescriptorFactory.fromView(view))
                    val marker = aMap!!.addMarker(options)
                    marker.isVisible = isShowMarker
                    markers.add(marker)
                }
                itemName2 -> {
                    view = inflater.inflate(R.layout.layout_fact_value, null)
                    view.tvValue.setBgColor(ContextCompat.getColor(this, R.color.colorPrimary), Color.WHITE)
                    view.tvValue.setTextColor(Color.WHITE)
                    view.tvValue.text = "${weatherDto.rain}mm"

                    if (!TextUtils.isEmpty(dto.stationName)) {
                        view.tvName.text = dto.stationName
                    }
                    val options = MarkerOptions()
                    options.title(dto.stationName)
                    options.snippet(dto.stationCode+","+dto.id)
                    options.anchor(0.5f, 0.5f)
                    options.position(LatLng(dto.lat, dto.lng))
                    options.icon(BitmapDescriptorFactory.fromView(view))
                    val marker = aMap!!.addMarker(options)
                    marker.isVisible = isShowMarker
                    markers.add(marker)
                }
                itemName3 -> {
                    view = inflater.inflate(R.layout.layout_fact_value, null)
                    view.tvValue.setBgColor(ContextCompat.getColor(this, R.color.colorPrimary), Color.WHITE)
                    view.tvValue.setTextColor(Color.WHITE)
                    view.tvValue.text = "${weatherDto.windForceString}"

                    val dir = getString(WeatherUtil.getWindDirection(weatherDto.windDir))
                    var rotation = 0f
                    when {
                        TextUtils.equals(dir, "北风") -> {
                            rotation = 0f
                        }
                        TextUtils.equals(dir, "东北风") -> {
                            rotation = 45f
                        }
                        TextUtils.equals(dir, "东风") -> {
                            rotation = 90f
                        }
                        TextUtils.equals(dir, "东南风") -> {
                            rotation = 135f
                        }
                        TextUtils.equals(dir, "南风") -> {
                            rotation = 180f
                        }
                        TextUtils.equals(dir, "西南风") -> {
                            rotation = 225f
                        }
                        TextUtils.equals(dir, "西风") -> {
                            rotation = 270f
                        }
                        TextUtils.equals(dir, "西北风") -> {
                            rotation = 315f
                        }
                    }

                    val b = CommonUtil.getWindMarker(this, weatherDto.windForce)
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
                    options.snippet(dto.stationCode+","+dto.id)
                    options.anchor(0.5f, 0.5f)
                    options.position(LatLng(dto.lat, dto.lng))
                    options.icon(BitmapDescriptorFactory.fromView(view))
                    val marker = aMap!!.addMarker(options)
                    marker.isVisible = isShowMarker
                    markers.add(marker)
                }
                itemName4 -> {
                    view = inflater.inflate(R.layout.layout_fact_value, null)
                    view.tvValue.setBgColor(ContextCompat.getColor(this, R.color.colorPrimary), Color.WHITE)
                    view.tvValue.setTextColor(Color.WHITE)
                    view.tvValue.text = "${weatherDto.highTemp}℃"
                    if (!TextUtils.isEmpty(dto.stationName)) {
                        view.tvName.text = dto.stationName
                    }
                    val options = MarkerOptions()
                    options.title(dto.stationName)
                    options.snippet(dto.stationCode+","+dto.id)
                    options.anchor(0.5f, 0.5f)
                    options.position(LatLng(dto.lat, dto.lng))
                    options.icon(BitmapDescriptorFactory.fromView(view))
                    val marker = aMap!!.addMarker(options)
                    marker.isVisible = isShowMarker
                    markers.add(marker)
                }
                itemName5 -> {
                    view = inflater.inflate(R.layout.layout_fact_value, null)
                    view.tvValue.setBgColor(ContextCompat.getColor(this, R.color.colorPrimary), Color.WHITE)
                    view.tvValue.setTextColor(Color.WHITE)
                    view.tvValue.text = "${weatherDto.lowTemp}℃"
                    if (!TextUtils.isEmpty(dto.stationName)) {
                        view.tvName.text = dto.stationName
                    }
                    val options = MarkerOptions()
                    options.title(dto.stationName)
                    options.snippet(dto.stationCode+","+dto.id)
                    options.anchor(0.5f, 0.5f)
                    options.position(LatLng(dto.lat, dto.lng))
                    options.icon(BitmapDescriptorFactory.fromView(view))
                    val marker = aMap!!.addMarker(options)
                    marker.isVisible = isShowMarker
                    markers.add(marker)
                }
            }
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
//        val intent = Intent(this, FactDetailChartActivity::class.java)
//        intent.putExtra(CONST.ACTIVITY_NAME, marker!!.title)
//        if (marker.snippet.contains(",")) {
//            val snippets = marker.snippet.split(",")
//            intent.putExtra("stationCode", snippets[0])
//        }
//        startActivity(intent)
        if (marker != null) {
            if (selectMarker == marker) {//两次点击的是同一个marker
                return true
            }
            selectMarker = marker

            CommonUtil.bottomToTop(clBottom)
            if (marker.title != null) {
                tvName.text = marker.title
            }

            okHttpDayAqi(marker)
        }
        return true
    }

    override fun onMapClick(latLng: LatLng?) {
        locationLat = latLng!!.latitude
        locationLng = latLng!!.longitude
        addLocationMarker()
        CommonUtil.bottomToTop(clBottom)
        okHttpDayAqi(latLng)
    }

    /**
     * 获取15天aqi
     */
    private fun okHttpDayAqi(marker: Marker?) {
        if (TextUtils.isEmpty(f0)) {
            return
        }
        Thread {
            val timestamp = Date().time
            val start1 = sdf3.format(sdf4.parse(f0))
            val end1 = sdf3.format(sdf3.parse(start1).time + 1000 * 60 * 60 * 24 * 15)
            val url = XiangJiManager.getXJSecretUrl2(marker!!.position.longitude, marker.position.latitude, start1, end1, timestamp)
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
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

                                if (!obj.isNull("series")) {
                                    dayAqiList.clear()
                                    val array = obj.getJSONArray("series")
                                    for (i in 0 until array.length()) {
                                        val data = WeatherDto()
                                        data.aqi = array[i].toString()
                                        dayAqiList.add(data)
                                    }

                                    //预报
                                    for (i in 0 until dataList.size) {
                                        val dto = dataList[i]
                                        if (marker.snippet.contains(",")) {
                                            val snippets = marker.snippet.split(",")
                                            if (TextUtils.equals(dto!!.stationCode, snippets[0])) {
                                                for (j in 0 until dto.weeklyList.size) {
                                                    val weatherDto = dto.weeklyList[j]
                                                    if (dayAqiList.size > 0 && j < dayAqiList.size) {
                                                        val aqiValue = dayAqiList[j].aqi
                                                        if (!TextUtils.isEmpty(aqiValue)) {
                                                            weatherDto.aqi = aqiValue
                                                        }
                                                    }
                                                }
                                                //一周预报曲线
                                                val weeklyView = WeeklyView(this@RailForeActivity)
                                                val currentDate = sdf3.parse(sdf3.format(Date())).time
                                                weeklyView.setData(dto.weeklyList, foreDate, currentDate, Color.WHITE)
                                                llContainerFifteen!!.removeAllViews()
                                                llContainerFifteen!!.addView(weeklyView, CommonUtil.widthPixels(this@RailForeActivity), CommonUtil.dip2px(this@RailForeActivity, 360f).toInt())
                                                break
                                            }
                                        }
                                    }
                                }
                            } catch (e1: JSONException) {
                                e1.printStackTrace()
                            }
                        }
                    }
                }
            })
        }.start()
    }

    /**
     * 获取15天aqi
     */
    private fun okHttpDayAqi(latLng: LatLng?) {
        if (TextUtils.isEmpty(f0)) {
            return
        }
        Thread {
            val timestamp = Date().time
            val start1 = sdf3.format(sdf4.parse(f0))
            val end1 = sdf3.format(sdf3.parse(start1).time + 1000 * 60 * 60 * 24 * 15)
            val url = XiangJiManager.getXJSecretUrl2(latLng!!.longitude, latLng!!.latitude, start1, end1, timestamp)
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }
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

                                if (!obj.isNull("series")) {
                                    dayAqiList.clear()
                                    val array = obj.getJSONArray("series")
                                    for (i in 0 until array.length()) {
                                        val data = WeatherDto()
                                        data.aqi = array[i].toString()
                                        dayAqiList.add(data)
                                    }

                                    okHttpClickWeather(latLng)
                                }
                            } catch (e1: JSONException) {
                                e1.printStackTrace()
                            }
                        }
                    }
                }
            })
        }.start()
    }

    private fun okHttpClickWeather(latLng: LatLng?) {
        showDialog()
        Thread {
            val url = "http://xinjiangdecision.tianqi.cn:81/Home/api/xjgdyb?lat=${latLng!!.latitude}&lon=${latLng!!.longitude}"
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
                                val itemObj = JSONObject(result)
                                if (!itemObj.isNull("trip")) {
                                    val trip = itemObj.getJSONObject("trip")
                                    if (!trip.isNull("stationName")) {
                                        val stationName = trip.getString("stationName")
                                        if (stationName != null) {
                                            tvName.text = stationName
                                        }
                                    }
                                }

                                if (!itemObj.isNull("forecast")) {
                                    val weeklyList: ArrayList<WeatherDto> = ArrayList()
                                    val f1 = itemObj.getJSONArray("forecast")
                                    for (i in 0 until f1.length()) {
                                        val dto = WeatherDto()

                                        //预报时间
                                        dto.date = CommonUtil.getDate(f0, i) //日期
                                        dto.week = CommonUtil.getWeek(f0, i) //星期几

                                        //预报内容
                                        val weeklyObj = f1.getJSONObject(i)

                                        //晚上
                                        val two = weeklyObj.getString("WeatherPhenomena2")
                                        if (!TextUtils.isEmpty(two) && !TextUtils.equals(two, "?") && !TextUtils.equals(two, "null")) {
                                            dto.lowPheCode = Integer.valueOf(two)
                                            dto.lowPhe = getString(WeatherUtil.getWeatherId(dto.lowPheCode))
                                        }
                                        val four = weeklyObj.getString("Ltem")
                                        if (!TextUtils.isEmpty(two) && !TextUtils.equals(two, "?") && !TextUtils.equals(two, "null")) {
                                            dto.lowTemp = Integer.valueOf(four)
                                        }

                                        //白天
                                        val one = weeklyObj.getString("WeatherPhenomena2")
                                        if (!TextUtils.isEmpty(one) && !TextUtils.equals(one, "?") && !TextUtils.equals(one, "null")) {
                                            dto.highPheCode = Integer.valueOf(one)
                                            dto.highPhe = getString(WeatherUtil.getWeatherId(dto.highPheCode))
                                        }
                                        val three = weeklyObj.getString("Htem")
                                        if (!TextUtils.isEmpty(three) && !TextUtils.equals(three, "?") && !TextUtils.equals(three, "null")) {
                                            dto.highTemp = Integer.valueOf(three)
                                        }

                                        val hour = sdf1.format(Date()).toInt()
                                        if (hour in 5..17) {
                                            val seven = weeklyObj.getString("WindDir2")
                                            if (!TextUtils.isEmpty(seven) && !TextUtils.equals(seven, "?") && !TextUtils.equals(seven, "null")) {
                                                dto.windDir = Integer.valueOf(seven)
                                            }
                                            val five = weeklyObj.getString("WindPow2")
                                            if (!TextUtils.isEmpty(five) && !TextUtils.equals(five, "?") && !TextUtils.equals(five, "null")) {
                                                dto.windForce = Integer.valueOf(five)
                                                dto.windForceString = WeatherUtil.getDayWindForce(dto.windForce)
                                            }
                                        } else {
                                            val eight = weeklyObj.getString("WindDir2")
                                            if (!TextUtils.isEmpty(eight) && !TextUtils.equals(eight, "?") && !TextUtils.equals(eight, "null")) {
                                                dto.windDir = Integer.valueOf(eight)
                                            }
                                            val six = weeklyObj.getString("WindPow2")
                                            if (!TextUtils.isEmpty(six) && !TextUtils.equals(six, "?") && !TextUtils.equals(six, "null")) {
                                                dto.windForce = Integer.valueOf(six)
                                                dto.windForceString = WeatherUtil.getDayWindForce(dto.windForce)
                                            }
                                        }

                                        //降水
                                        val rain = weeklyObj.getString("rain24")
                                        if (!TextUtils.isEmpty(rain) && !TextUtils.equals(rain, "?") && !TextUtils.equals(rain, "null")) {
                                            dto.rain = rain
                                        }

                                        if (dayAqiList.size > 0 && i < dayAqiList.size) {
                                            val aqiValue = dayAqiList[i].aqi
                                            if (!TextUtils.isEmpty(aqiValue)) {
                                                dto.aqi = aqiValue
                                            }
                                        }

                                        weeklyList.add(dto)
                                    }

                                    //一周预报曲线
                                    val weeklyView = WeeklyView(this@RailForeActivity)
                                    val currentDate = sdf3.parse(sdf3.format(Date())).time
                                    weeklyView.setData(weeklyList, foreDate, currentDate, Color.WHITE)
                                    llContainerFifteen!!.removeAllViews()
                                    llContainerFifteen!!.addView(weeklyView, CommonUtil.widthPixels(this@RailForeActivity), CommonUtil.dip2px(this@RailForeActivity, 360f).toInt())
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

}
