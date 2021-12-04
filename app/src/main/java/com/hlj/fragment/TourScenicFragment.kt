package com.hlj.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.OnMapClickListener
import com.amap.api.maps.AMap.OnMarkerClickListener
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.navi.AMapNavi
import com.amap.api.navi.AMapNaviListener
import com.amap.api.navi.enums.PathPlanningStrategy
import com.amap.api.navi.model.*
import com.autonavi.tbt.TrafficFacilityInfo
import com.hlj.activity.TourScenicDetailActivity
import com.hlj.activity.WarningDetailActivity
import com.hlj.common.CONST
import com.hlj.dto.NewsDto
import com.hlj.dto.WarningDto
import com.hlj.dto.WeatherDto
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.hlj.utils.WeatherUtil
import com.hlj.view.WeeklyViewTour
import kotlinx.android.synthetic.main.fragment_tour_scenic.*
import kotlinx.android.synthetic.main.marker_icon_tour.view.*
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
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * 旅游气象-旅游路线-详情-景点标注
 */
class TourScenicFragment : BaseFragment(), OnClickListener, OnMapClickListener, OnMarkerClickListener, AMapNaviListener {

    private var aMap: AMap? = null
    private val dataMap: HashMap<String, NewsDto> = HashMap()
    private val markers: MutableList<Marker> = ArrayList()
    private var selectMarker: Marker? = null
    private val sdf1 = SimpleDateFormat("HH", Locale.CHINA)
    private val sdf3 = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
    private val sdf4 = SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA)
    private val weatherMap: HashMap<String, ArrayList<WeatherDto>> = HashMap()
    private val warningMap: HashMap<String, ArrayList<WarningDto>> = HashMap()
    private val boundBuilder = LatLngBounds.builder()
    private var mAMapNavi: AMapNavi? = null
    private var foreDate: Long = 0//获取预报时间戳
    private val polylines: ArrayList<Polyline> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tour_scenic, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAmap(savedInstanceState)
        initWidget()
    }

    /**
     * 初始化高德地图
     */
    private fun initAmap(bundle: Bundle?) {
        mapView.onCreate(bundle)
        if (aMap == null) {
            aMap = mapView.map
        }
        aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.926628, 105.178100), 6.0f))
        aMap!!.uiSettings.isMyLocationButtonEnabled = false // 设置默认定位按钮是否显示
        aMap!!.uiSettings.isZoomControlsEnabled = false
        aMap!!.uiSettings.isRotateGesturesEnabled = false
        aMap!!.setOnMapClickListener(this)
        aMap!!.setOnMarkerClickListener(this)

        mAMapNavi = AMapNavi.getInstance(activity)
        mAMapNavi!!.addAMapNaviListener(this)
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        ivClose.setOnClickListener(this)
        okHttpList()
    }

    private fun okHttpList() {
        val id = arguments!!.getString("id")
        if (TextUtils.isEmpty(id)) {
            return
        }
        Thread {
            val url = "http://xinjiangdecision.tianqi.cn:81/Home/api/get_travel_route_details?id=$id"
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
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("list")) {
                                    dataMap.clear()
                                    weatherMap.clear()
                                    warningMap.clear()

                                    var start: NaviPoi? = null
                                    var end: NaviPoi? = null
                                    val waysPoiIds: MutableList<NaviPoi> = ArrayList()


                                    var lineStyle = "1"//1直连、2导航连线
                                    if (!obj.isNull("lineStyle")) {
                                        lineStyle = obj.getString("lineStyle")
                                    }

                                    val array = obj.getJSONArray("list")
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

                                        if (TextUtils.equals(lineStyle, "2")) {//导航连线
                                            when (i) {
                                                0 -> {
                                                    start = NaviPoi(dto.title, LatLng(dto.lat, dto.lng), "")
                                                }
                                                array.length()-1 -> {
                                                    end = NaviPoi(dto.title, LatLng(dto.lat, dto.lng), "")
                                                }
                                                else -> {
                                                    waysPoiIds.add(NaviPoi(dto.title, LatLng(dto.lat, dto.lng), ""))
                                                }
                                            }
                                        }

                                        dataMap[dto.id] = dto
                                        if (!TextUtils.isEmpty(dto.warningId)) {
                                            okHttpWarning(dto.id, dto.warningId, dto.cityId)
                                        }
                                    }

                                    //默认连线，如果是导航连线，在清除，避免导航失败不连线问题
                                    val polylineOptions = PolylineOptions()
                                    polylineOptions.color(ContextCompat.getColor(activity!!, R.color.colorPrimary))
                                    polylineOptions.width(20f)
                                    for ((key, value) in dataMap.entries) {
                                        val latLng = LatLng(value.lat, value.lng)
                                        polylineOptions.add(latLng)
                                    }
                                    val polyline = aMap!!.addPolyline(polylineOptions)
                                    polylines.add(polyline)
                                    if (TextUtils.equals(lineStyle, "2")) {//导航连线
                                        // POI算路
                                        mAMapNavi!!.calculateDriveRoute(start, end, waysPoiIds, PathPlanningStrategy.DRIVING_MULTIPLE_ROUTES_DEFAULT)
                                    }
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
     * 在地图上添加marker
     */
    private fun addSingleMarkers(id: String) {
        //天气
        var weatherDto: WeatherDto? = null
        if (weatherMap.containsKey(id)) {
            val weatherList = weatherMap[id]
            if (weatherList!!.size > 0) {
                weatherDto = weatherList[0]
            }
        }

        //预警
        var warningDto: WarningDto? = null
        if (warningMap.containsKey(id)) {
            val warningList = warningMap[id]
            if (warningList!!.size > 0) {
                warningDto = warningList[0]
            }
        }

        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val hour = sdf1.format(Date()).toInt()
        if (dataMap.containsKey(id)) {
            val dto = dataMap[id]
            val options = MarkerOptions()
            options.title(dto!!.title)
            options.snippet(id)
            options.anchor(0.5f, 0.5f)
            options.position(LatLng(dto.lat, dto.lng))
            boundBuilder.include(LatLng(dto.lat, dto.lng))
            val mView = inflater.inflate(R.layout.marker_icon_tour, null)
            mView.tvName.text = dto.title

            if (weatherDto != null) {
                val bitmap = if (hour in 5..17) {
                    WeatherUtil.getBitmap(activity, weatherDto.highPheCode)
                } else {
                    WeatherUtil.getNightBitmap(activity, weatherDto.lowPheCode)
                }
                if (bitmap != null) {
                    mView.ivPhe.setImageBitmap(bitmap)
                }
                mView.tvTemp.text = "${weatherDto!!.highTemp}/${weatherDto.lowTemp}℃"
            }
            if (warningDto != null) {
                var bitmap: Bitmap? = null
                when (warningDto.color) {
                    CONST.blue[0] -> {
                        bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + warningDto.type + CONST.blue[1] + CONST.imageSuffix)
                    }
                    CONST.yellow[0] -> {
                        bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + warningDto.type + CONST.yellow[1] + CONST.imageSuffix)
                    }
                    CONST.orange[0] -> {
                        bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + warningDto.type + CONST.orange[1] + CONST.imageSuffix)
                    }
                    CONST.red[0] -> {
                        bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + warningDto.type + CONST.red[1] + CONST.imageSuffix)
                    }
                }
                if (bitmap == null) {
                    bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + "default" + CONST.imageSuffix)
                }
                if (bitmap != null) {
                    mView.ivPhe.setImageBitmap(bitmap)
                }
                mView.tvTemp.text = "${warningDto!!.name}"
            }

            options.icon(BitmapDescriptorFactory.fromView(mView))
            val marker = aMap!!.addMarker(options)
            markers.add(marker)
        }
        if (dataMap.size > 0) {
            aMap!!.animateCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 50))
        }
    }

    private fun removeMarkers() {
        for (i in 0 until markers.size) {
            val marker = markers[i]
            marker.remove()
        }
        markers.clear()
    }

    override fun onMapClick(arg0: LatLng?) {
        if (selectMarker != null) {
            selectMarker!!.hideInfoWindow()
        }
    }

    private fun changeMarkerStatus(isPress: Boolean) {
        if (selectMarker == null) {
            return
        }
        if (!dataMap.containsKey(selectMarker!!.snippet)) {
            return
        }
        val dto = dataMap[selectMarker!!.snippet]
        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (isPress) {
            val mView = inflater.inflate(R.layout.marker_icon_tour_press, null)
            mView.tvName.text = dto!!.title
            selectMarker!!.setIcon(BitmapDescriptorFactory.fromView(mView))
        } else {
            //天气
            var weatherDto: WeatherDto? = null
            if (weatherMap.containsKey(selectMarker!!.snippet)) {
                val weatherList = weatherMap[selectMarker!!.snippet]
                if (weatherList!!.size > 0) {
                    weatherDto = weatherList[0]
                }
            }

            //预警
            var warningDto: WarningDto? = null
            if (warningMap.containsKey(selectMarker!!.snippet)) {
                val warningList = warningMap[selectMarker!!.snippet]
                if (warningList!!.size > 0) {
                    warningDto = warningList[0]
                }
            }
            val mView = inflater.inflate(R.layout.marker_icon_tour, null)
            mView.tvName.text = dto!!.title

            if (weatherDto != null) {
                val hour = sdf1.format(Date()).toInt()
                val bitmap = if (hour in 5..17) {
                    WeatherUtil.getBitmap(activity, weatherDto.highPheCode)
                } else {
                    WeatherUtil.getNightBitmap(activity, weatherDto.lowPheCode)
                }
                if (bitmap != null) {
                    mView.ivPhe.setImageBitmap(bitmap)
                }
                mView.tvTemp.text = "${weatherDto!!.highTemp}/${weatherDto.lowTemp}℃"
            }
            if (warningDto != null) {
                var bitmap: Bitmap? = null
                when (warningDto.color) {
                    CONST.blue[0] -> {
                        bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + warningDto.type + CONST.blue[1] + CONST.imageSuffix)
                    }
                    CONST.yellow[0] -> {
                        bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + warningDto.type + CONST.yellow[1] + CONST.imageSuffix)
                    }
                    CONST.orange[0] -> {
                        bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + warningDto.type + CONST.orange[1] + CONST.imageSuffix)
                    }
                    CONST.red[0] -> {
                        bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + warningDto.type + CONST.red[1] + CONST.imageSuffix)
                    }
                }
                if (bitmap == null) {
                    bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + "default" + CONST.imageSuffix)
                }
                if (bitmap != null) {
                    mView.ivPhe.setImageBitmap(bitmap)
                }
                mView.tvTemp.text = "${warningDto!!.name}"
            }
            selectMarker!!.setIcon(BitmapDescriptorFactory.fromView(mView))
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null) {
            if (selectMarker == marker) {//两次点击的是同一个marker
                if (dataMap.containsKey(marker.snippet)) {
                    val dto = dataMap[marker.snippet]
                    val intent = Intent(activity, TourScenicDetailActivity::class.java)
                    intent.putExtra("id", dto!!.id)
                    startActivity(intent)
                }
                return true
            }

            changeMarkerStatus(false)
            selectMarker = marker
            changeMarkerStatus(true)

            CommonUtil.bottomToTop(clBottom)
            if (marker.title != null) {
                tvName.text = marker.title
            }

            //预报
            if (weatherMap.containsKey(marker.snippet)) {
                val weatherList = weatherMap[marker.snippet]
                //一周预报曲线
                val weeklyView = WeeklyViewTour(activity)
                val currentDate = sdf3.parse(sdf3.format(Date())).time
                weeklyView.setData(weatherList, foreDate, currentDate)
                llContainerFifteen!!.removeAllViews()
                llContainerFifteen!!.addView(weeklyView, CommonUtil.widthPixels(activity) * 2, CommonUtil.dip2px(activity, 95f).toInt())
            }

            //预警
            val warningTypes: HashMap<String, WarningDto> = HashMap()
            if (warningMap.containsKey(marker.snippet)) {
                val warningList = warningMap[marker.snippet]
                llContainer.removeAllViews()
                for (i in 0 until warningList!!.size) {
                    val warningDto = warningList[i]
                    warningTypes[warningDto.type] = warningDto

                    val ivWarning = ImageView(activity)
                    var bitmap: Bitmap? = null
                    when (warningDto.color) {
                        CONST.blue[0] -> {
                            bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + warningDto.type + CONST.blue[1] + CONST.imageSuffix)
                        }
                        CONST.yellow[0] -> {
                            bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + warningDto.type + CONST.yellow[1] + CONST.imageSuffix)
                        }
                        CONST.orange[0] -> {
                            bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + warningDto.type + CONST.orange[1] + CONST.imageSuffix)
                        }
                        CONST.red[0] -> {
                            bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + warningDto.type + CONST.red[1] + CONST.imageSuffix)
                        }
                    }
                    if (bitmap == null) {
                        bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + "default" + CONST.imageSuffix)
                    }
                    if (bitmap != null) {
                        ivWarning.setImageBitmap(bitmap)
                    }
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.width = CommonUtil.dip2px(activity, 35f).toInt()
                    params.height = CommonUtil.dip2px(activity, 35f).toInt()
                    params.leftMargin = CommonUtil.dip2px(activity, 5f).toInt()
                    ivWarning.layoutParams = params
                    llContainer.addView(ivWarning)

                    ivWarning.setOnClickListener {
                        val intentDetail = Intent(activity, WarningDetailActivity::class.java)
                        val bundle = Bundle()
                        bundle.putParcelable("data", warningDto)
                        intentDetail.putExtras(bundle)
                        startActivity(intentDetail)
                    }
                }

                var strType = ""
                for (entry in warningTypes.entries) {
                    var name = ""
                    var redCount = 0
                    var orangeCount = 0
                    var yellowCount = 0
                    var blueCount = 0
                    var strRed = ""
                    var strOrange = ""
                    var strYellow = ""
                    var strBlue = ""

                    for (m in 0 until warningList.size) {
                        val d = warningList[m]
                        if (TextUtils.equals(entry.key, d!!.type)) {
                            name = CommonUtil.getWarningNameByType(activity, d!!.type)
                            when(d!!.color) {
                                "04" -> redCount++
                                "03" -> orangeCount++
                                "02" -> yellowCount++
                                "01" -> blueCount++
                            }
                        }
                    }

                    if (redCount > 0) {
                        strRed = "${redCount}红"
                    }
                    if (orangeCount > 0) {
                        strOrange = "${orangeCount}橙"
                    }
                    if (yellowCount > 0) {
                        strYellow = "${yellowCount}黄"
                    }
                    if (blueCount > 0) {
                        strBlue = "${blueCount}蓝"
                    }

                    strType += "[${name}]${strRed}${strOrange}${strYellow}${strBlue}；"
                }

                val str1 = "共有"
                val str2 = warningList.size.toString()
                val str3 = "条预警。"
                val str4 = strType
                tvWarningStatistic.text = "${str1}${str2}${str3}${str4}"
            }
        }
        return true
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivClose -> CommonUtil.topToBottom(clBottom)
        }
    }

    /**
     * 方法必须重写
     */
    override fun onResume() {
        super.onResume()
        if (mapView != null) {
            mapView!!.onResume()
        }
    }

    /**
     * 方法必须重写
     */
    override fun onPause() {
        super.onPause()
        if (mapView != null) {
            mapView!!.onPause()
        }
    }

    /**
     * 方法必须重写
     */
    override fun onDestroy() {
        super.onDestroy()
        if (mapView != null) {
            mapView!!.onDestroy()
        }
    }

    private fun getWeatherInfo(id: String, cityId: String) {
        Thread {
            val url = String.format("https://hfapi.tianqi.cn/getweatherdata.php?area=%s&type=forecast|observe|alarm|air|rise&key=AErLsfoKBVCsU8hs", cityId)
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    activity!!.runOnUiThread {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)

                                //预报
                                if (!obj.isNull("forecast")) {
                                    val forecast = obj.getJSONObject("forecast")

                                    //15天预报信息
                                    if (!forecast.isNull("24h")) {
                                        val weeklyList: ArrayList<WeatherDto> = ArrayList()
                                        val `object` = forecast.getJSONObject("24h")
                                        if (!`object`.isNull(cityId)) {
                                            val object1 = `object`.getJSONObject(cityId)
                                            val f0 = object1.getString("000")
                                            try {
                                                val fTime = sdf3.format(sdf4.parse(f0))
                                                foreDate = sdf3.parse(fTime).time
                                            } catch (e: ParseException) {
                                                e.printStackTrace()
                                            }
                                            if (!object1.isNull("1001001")) {
                                                val f1 = object1.getJSONArray("1001001")
                                                var length = f1.length()
                                                if (length >= 15) {
                                                    length = 15
                                                }
                                                val hour = sdf1.format(Date()).toInt()
                                                for (i in 0 until length) {
                                                    val dto = WeatherDto()

                                                    //预报时间
                                                    dto.date = CommonUtil.getDate(f0, i) //日期
                                                    dto.week = CommonUtil.getWeek(f0, i) //星期几

                                                    //预报内容
                                                    val weeklyObj = f1.getJSONObject(i)

                                                    //晚上
                                                    val two = weeklyObj.getString("002")
                                                    if (!TextUtils.isEmpty(two) && !TextUtils.equals(two, "?") && !TextUtils.equals(two, "null")) {
                                                        dto.lowPheCode = Integer.valueOf(two)
                                                        dto.lowPhe = getString(WeatherUtil.getWeatherId(dto.lowPheCode))
                                                    }
                                                    val four = weeklyObj.getString("004")
                                                    if (!TextUtils.isEmpty(two) && !TextUtils.equals(two, "?") && !TextUtils.equals(two, "null")) {
                                                        dto.lowTemp = Integer.valueOf(four)
                                                    }

                                                    //白天
                                                    val one = weeklyObj.getString("001")
                                                    if (!TextUtils.isEmpty(one) && !TextUtils.equals(one, "?") && !TextUtils.equals(one, "null")) {
                                                        dto.highPheCode = Integer.valueOf(one)
                                                        dto.highPhe = getString(WeatherUtil.getWeatherId(dto.highPheCode))
                                                    }
                                                    val three = weeklyObj.getString("003")
                                                    if (!TextUtils.isEmpty(three) && !TextUtils.equals(three, "?") && !TextUtils.equals(three, "null")) {
                                                        dto.highTemp = Integer.valueOf(three)
                                                    }
                                                    if (hour in 5..17) {
                                                        val seven = weeklyObj.getString("007")
                                                        if (!TextUtils.isEmpty(seven) && !TextUtils.equals(seven, "?") && !TextUtils.equals(seven, "null")) {
                                                            dto.windDir = Integer.valueOf(seven)
                                                        }
                                                        val five = weeklyObj.getString("005")
                                                        if (!TextUtils.isEmpty(five) && !TextUtils.equals(five, "?") && !TextUtils.equals(five, "null")) {
                                                            dto.windForce = Integer.valueOf(five)
                                                            dto.windForceString = WeatherUtil.getDayWindForce(dto.windForce)
                                                        }
                                                    } else {
                                                        val eight = weeklyObj.getString("008")
                                                        if (!TextUtils.isEmpty(eight) && !TextUtils.equals(eight, "?") && !TextUtils.equals(eight, "null")) {
                                                            dto.windDir = Integer.valueOf(eight)
                                                        }
                                                        val six = weeklyObj.getString("006")
                                                        if (!TextUtils.isEmpty(six) && !TextUtils.equals(six, "?") && !TextUtils.equals(six, "null")) {
                                                            dto.windForce = Integer.valueOf(six)
                                                            dto.windForceString = WeatherUtil.getDayWindForce(dto.windForce)
                                                        }
                                                    }
                                                    weeklyList.add(dto)
                                                }
                                            }
                                        }
                                        weatherMap[id] = weeklyList

                                        addSingleMarkers(id)
                                    }
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
     * 获取预警信息
     */
    private fun okHttpWarning(id: String, warningId: String, cityId: String) {
        Thread {
            val url = "http://decision-admin.tianqi.cn/Home/extra/getwarns?order=0&areaid=$warningId"
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    getWeatherInfo(id, cityId)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    activity!!.runOnUiThread {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("data")) {
                                    val warningList: ArrayList<WarningDto> = ArrayList()
                                    val jsonArray = obj.getJSONArray("data")
                                    for (i in 0 until jsonArray.length()) {
                                        val tempArray = jsonArray.getJSONArray(i)
                                        val dto = WarningDto()
                                        dto.html = tempArray.optString(1)
                                        val array = dto.html.split("-").toTypedArray()
                                        val item0 = array[0]
                                        val item1 = array[1]
                                        val item2 = array[2]
                                        dto.provinceId = item0.substring(0, 2)
                                        dto.type = item2.substring(0, 5)
                                        dto.color = item2.substring(5, 7)
                                        dto.time = item1
                                        dto.lng = tempArray.getDouble(2)
                                        dto.lat = tempArray.getDouble(3)
                                        dto.name = tempArray.optString(0)
                                        if (!dto.name.contains("解除")) {
                                            warningList.add(dto)
                                        }
                                    }
                                    warningMap[id] = warningList
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                        getWeatherInfo(id, cityId)
                    }
                }
            })
        }.start()
    }

    override fun onInitNaviFailure() {
        TODO("Not yet implemented")
    }

    override fun onInitNaviSuccess() {
        TODO("Not yet implemented")
    }

    override fun onStartNavi(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun onTrafficStatusUpdate() {
        TODO("Not yet implemented")
    }

    override fun onLocationChange(p0: AMapNaviLocation?) {
        TODO("Not yet implemented")
    }

    override fun onGetNavigationText(p0: Int, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun onGetNavigationText(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onEndEmulatorNavi() {
        TODO("Not yet implemented")
    }

    override fun onArriveDestination() {
        TODO("Not yet implemented")
    }

    override fun onCalculateRouteFailure(p0: Int) {
    }

    override fun onCalculateRouteFailure(p0: AMapCalcRouteResult?) {
        TODO("Not yet implemented")
    }

    override fun onReCalculateRouteForYaw() {
        TODO("Not yet implemented")
    }

    override fun onReCalculateRouteForTrafficJam() {
        TODO("Not yet implemented")
    }

    override fun onArrivedWayPoint(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun onGpsOpenStatus(p0: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onNaviInfoUpdate(p0: NaviInfo?) {
        TODO("Not yet implemented")
    }

    override fun onNaviInfoUpdated(p0: AMapNaviInfo?) {
        TODO("Not yet implemented")
    }

    override fun updateCameraInfo(p0: Array<out AMapNaviCameraInfo>?) {
        TODO("Not yet implemented")
    }

    override fun updateIntervalCameraInfo(p0: AMapNaviCameraInfo?, p1: AMapNaviCameraInfo?, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun onServiceAreaUpdate(p0: Array<out AMapServiceAreaInfo>?) {
        TODO("Not yet implemented")
    }

    override fun showCross(p0: AMapNaviCross?) {
        TODO("Not yet implemented")
    }

    override fun hideCross() {
        TODO("Not yet implemented")
    }

    override fun showModeCross(p0: AMapModelCross?) {
        TODO("Not yet implemented")
    }

    override fun hideModeCross() {
        TODO("Not yet implemented")
    }

    override fun showLaneInfo(p0: Array<out AMapLaneInfo>?, p1: ByteArray?, p2: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun showLaneInfo(p0: AMapLaneInfo?) {
        TODO("Not yet implemented")
    }

    override fun hideLaneInfo() {
        TODO("Not yet implemented")
    }

    override fun onCalculateRouteSuccess(p0: IntArray?) {
        TODO("Not yet implemented")
    }

    override fun onCalculateRouteSuccess(p0: AMapCalcRouteResult?) {
        // 获取路线数据对象
        val naviPaths = AMapNavi.getInstance(activity).naviPaths
//        for ((key, value) in naviPaths.entries) {
//            value.coordList[0].latitude
//        }

        for (i in 0 until polylines.size) {
            val polyline = polylines[i]
            polyline.remove()
        }
        polylines.clear()

        for (value in naviPaths.values) {
            val polylineOptions = PolylineOptions()
            polylineOptions.color(ContextCompat.getColor(activity!!, R.color.colorPrimary))
            polylineOptions.width(20f)
            for (i in 0 until value.coordList.size) {
                val latLng = value.coordList[i]
                polylineOptions.add(LatLng(latLng.latitude, latLng.longitude))
            }
            val polyline = aMap!!.addPolyline(polylineOptions)
        }
    }

    override fun notifyParallelRoad(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun OnUpdateTrafficFacility(p0: Array<out AMapNaviTrafficFacilityInfo>?) {
        TODO("Not yet implemented")
    }

    override fun OnUpdateTrafficFacility(p0: AMapNaviTrafficFacilityInfo?) {
        TODO("Not yet implemented")
    }

    override fun OnUpdateTrafficFacility(p0: TrafficFacilityInfo?) {
        TODO("Not yet implemented")
    }

    override fun updateAimlessModeStatistics(p0: AimLessModeStat?) {
        TODO("Not yet implemented")
    }

    override fun updateAimlessModeCongestionInfo(p0: AimLessModeCongestionInfo?) {
        TODO("Not yet implemented")
    }

    override fun onPlayRing(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun onNaviRouteNotify(p0: AMapNaviRouteNotifyData?) {
        TODO("Not yet implemented")
    }

}
