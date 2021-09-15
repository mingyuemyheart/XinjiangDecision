package com.hlj.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.media.ThumbnailUtils
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.hlj.common.CONST
import com.hlj.dto.StationMonitorDto
import com.hlj.manager.FactManager
import com.hlj.utils.AuthorityUtil
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_point_fact.*
import kotlinx.android.synthetic.main.activity_point_fore.*
import kotlinx.android.synthetic.main.activity_point_fore.imageLegend
import kotlinx.android.synthetic.main.activity_point_fore.ivHumidity
import kotlinx.android.synthetic.main.activity_point_fore.ivLayer
import kotlinx.android.synthetic.main.activity_point_fore.ivLegend
import kotlinx.android.synthetic.main.activity_point_fore.ivLocation
import kotlinx.android.synthetic.main.activity_point_fore.ivPlay
import kotlinx.android.synthetic.main.activity_point_fore.ivPoint
import kotlinx.android.synthetic.main.activity_point_fore.ivSwitch
import kotlinx.android.synthetic.main.activity_point_fore.ivTemp
import kotlinx.android.synthetic.main.activity_point_fore.ivWind
import kotlinx.android.synthetic.main.activity_point_fore.llSeekBar
import kotlinx.android.synthetic.main.activity_point_fore.mapView
import kotlinx.android.synthetic.main.activity_point_fore.seekBar
import kotlinx.android.synthetic.main.activity_point_fore.tvHumidity
import kotlinx.android.synthetic.main.activity_point_fore.tvName
import kotlinx.android.synthetic.main.activity_point_fore.tvTemp
import kotlinx.android.synthetic.main.activity_point_fore.tvTime
import kotlinx.android.synthetic.main.activity_point_fore.tvWind
import kotlinx.android.synthetic.main.layout_point_marker.view.*
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

/**
 * 格点预报
 */
class PointForeActivity : BaseActivity(), OnClickListener, AMapLocationListener, AMap.OnMapClickListener, FactManager.RadarListener, AMap.OnCameraChangeListener {

    private var aMap: AMap? = null
    private var zoom = 3.7f
    private val dataList: MutableList<StationMonitorDto> = ArrayList()
    private val sdf1 = SimpleDateFormat("dd日HH时", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("yyyyMMddHH", Locale.CHINA)
    private val sdf4 = SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA)
    private var dataType = "气温"
    private var locationLat = 35.926628
    private var locationLng = 105.178100
    private var mapType = AMap.MAP_TYPE_NORMAL
    private var savedInstanceState: Bundle? = null
    private var currentIndex = 0 //当前时次数据
    private var mFactManager: FactManager? = null
    private var mRadarThread: RadarThread? = null
    private var mOverlay: GroundOverlay? = null
    private var locationMarker: Marker? = null
    private val STATE_NONE = 0
    private val STATE_PLAYING = 1
    private val STATE_PAUSE = 2
    private val STATE_CANCEL = 3
    private val pointList: MutableList<StationMonitorDto> = ArrayList()
    private val texts: ArrayList<Marker> = ArrayList()
    private var isShowPoint = true
    private var isShowLayer = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_point_fore)
        this.savedInstanceState = savedInstanceState
        checkAuthority()
    }

    private fun init() {
        initMap()
        initWidget()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvTemp.setOnClickListener(this)
        tvHumidity.setOnClickListener(this)
        tvWind.setOnClickListener(this)
        tvCloud.setOnClickListener(this)
        tvRain.setOnClickListener(this)
        ivPoint.setOnClickListener(this)
        ivLayer.setOnClickListener(this)
        ivLocation.setOnClickListener(this)
        ivSwitch.setOnClickListener(this)
        ivLegend.setOnClickListener(this)
        ivPlay.setOnClickListener(this)
        seekBar.setOnSeekBarChangeListener(seekbarListener)

        mFactManager = FactManager(this)
        
        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }
        if (CommonUtil.isLocationOpen(this)) {
            startLocation()
        } else {
            locationComplete()
        }
    }

    private val seekbarListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onStopTrackingTouch(arg0: SeekBar) {
            if (mRadarThread != null) {
                mRadarThread!!.setCurrent(seekBar!!.progress)
                mRadarThread!!.stopTracking()
            }
        }

        override fun onStartTrackingTouch(arg0: SeekBar) {
            if (mRadarThread != null) {
                mRadarThread!!.startTracking()
            }
        }

        override fun onProgressChanged(arg0: SeekBar, arg1: Int, arg2: Boolean) {}
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
            locationComplete()
        }
    }

    private fun locationComplete() {
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

    /**
     * 初始化地图
     */
    private fun initMap() {
        mapView.onCreate(savedInstanceState)
        if (aMap == null) {
            aMap = mapView.map
        }
        aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locationLat, locationLng), zoom))
        aMap!!.uiSettings.isZoomControlsEnabled = false
        aMap!!.uiSettings.isRotateGesturesEnabled = false
        aMap!!.setOnMapClickListener(this)
        aMap!!.setOnCameraChangeListener(this)
        aMap!!.setOnMapLoadedListener {
            CommonUtil.drawHLJJson(this, aMap)
            okHttpList()
        }
    }

    private fun okHttpList() {
        if (!intent.hasExtra(CONST.WEB_URL)) {
            return
        }
        val url = intent.getStringExtra(CONST.WEB_URL)
        if (TextUtils.isEmpty(url)) {
            return
        }
        showDialog()
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
                                val obj = JSONObject(result)
                                val maxlat = obj.getDouble("maxlat")
                                val maxlon = obj.getDouble("maxlon")
                                val minlat = obj.getDouble("minlat")
                                val minlon = obj.getDouble("minlon")
                                if (!obj.isNull("data")) {
                                    val array = obj.getJSONArray("data")
                                    for (i in 0 until array.length()) {
                                        val dto = StationMonitorDto()
                                        val itemObj = array.getJSONObject(i)
                                        if (!itemObj.isNull("name")) {
                                            dto.name = itemObj.getString("name")
                                        }
                                        if (!itemObj.isNull("tuliurl")) {
                                            dto.legendUrl = itemObj.getString("tuliurl")
                                        }
                                        if (!itemObj.isNull("imgs")) {
                                            val list: MutableList<StationMonitorDto> = ArrayList()
                                            val imgsArray = itemObj.getJSONArray("imgs")
                                            for (j in 0 until imgsArray.length()) {
                                                val data = StationMonitorDto()
                                                val imgsObj = imgsArray.getJSONObject(j)
                                                data.name = dto.name
                                                if (!imgsObj.isNull("time")) {
                                                    data.time = imgsObj.getString("time")
                                                    data.time = sdf1.format(sdf2.parse(data.time))
                                                }
                                                if (!imgsObj.isNull("imgurl")) {
                                                    data.imgUrl = imgsObj.getString("imgurl")
                                                }
                                                data.leftLat = minlat
                                                data.leftLng = minlon
                                                data.rightLat = maxlat
                                                data.rightLng = maxlon
                                                list.add(data)
                                            }
                                            dto.itemList.addAll(list)
                                        }
                                        dataList.add(dto)
                                    }
                                    switchElement()
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
     * 切换要素
     */
    private fun switchElement() {
        for (i in 0 until dataList.size) {
            val data = dataList[i]
            if (TextUtils.equals(data.name, dataType)) {
                Picasso.get().load(data.legendUrl).into(imageLegend)
                startDownLoadImgs(data.itemList)
                break
            }
        }
    }

    private fun startDownLoadImgs(images: ArrayList<StationMonitorDto>?) {
        showDialog()
        if (mRadarThread != null) {
            mRadarThread!!.cancel()
            mRadarThread = null
        }
        mFactManager!!.loadImagesAsyn(images, this)
    }

    override fun onResult(result: Int, images: ArrayList<StationMonitorDto>) {
        runOnUiThread {
            cancelDialog()
            llSeekBar!!.visibility = View.VISIBLE
            if (result == FactManager.RadarListener.RESULT_SUCCESSED) {
                changeProgress(images[currentIndex], 0, images.size-1)
            }
        }
    }

    override fun onProgress(url: String?, progress: Int) {}

    @SuppressLint("SimpleDateFormat")
    private fun changeProgress(dto: StationMonitorDto, progress: Int, max: Int) {
        if (seekBar != null) {
            seekBar!!.max = max
            seekBar!!.progress = progress
        }
        if (!TextUtils.isEmpty(dto.time)) {
            tvTime.text = dto.time
            var unit = ""
            when(dto.name) {
                tvTemp.text.toString() -> unit = getString(R.string.unit_degree)
                tvHumidity.text.toString() -> unit = getString(R.string.unit_percent)
                tvWind.text.toString() -> unit = getString(R.string.unit_speed)
                tvCloud.text.toString() -> unit = getString(R.string.unit_percent)
                tvRain.text.toString() -> unit = getString(R.string.unit_mm)
            }
            tvName.text = "${dto.time}格点${dto.name}预报[单位:${unit}]"
        }
        if (!TextUtils.isEmpty(dto.imgPath)) {
            val bitmap = BitmapFactory.decodeFile(dto.imgPath)
            if (bitmap != null) {
                showRadar(bitmap, dto.leftLat, dto.leftLng, dto.rightLat, dto.rightLng)
            }
        }

        addPoint(dto.time)
    }

    private fun addPoint(time: String) {
        removeTexts()
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        for (i in 0 until pointList.size) {
            val point = pointList[i]
            val options = MarkerOptions()
            options.position(LatLng(point.lat, point.lng))
            val view = inflater.inflate(R.layout.layout_point_marker, null)
            view.tvMarker.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10f)
            if (mapType == AMap.MAP_TYPE_NORMAL) {
                view.tvMarker.setTextColor(Color.RED)
            } else if (mapType == AMap.MAP_TYPE_SATELLITE) {
                view.tvMarker.setTextColor(Color.WHITE)
            }
            for (j in 0 until point.itemList.size) {
                if (TextUtils.isEmpty(time)) {
                    val item = point.itemList[0]
                    var content = ""
                    when(dataType) {
                        tvTemp.text.toString() -> content = item.pointTemp
                        tvHumidity.text.toString() -> content = item.humidity
                        tvWind.text.toString() -> content = item.windSpeed
                        tvCloud.text.toString() -> content = item.cloud
                        tvRain.text.toString() -> content = item.rain
                    }
                    view.tvMarker.text = content
                    options.icon(BitmapDescriptorFactory.fromView(view))
                    if (!TextUtils.isEmpty(content) && !content.contains("99999")) {
                        val text = aMap!!.addMarker(options)
                        texts.add(text)
                    }
                    break
                } else {
                    val item = point.itemList[j]
                    if (TextUtils.equals(time, item.time)) {
                        var content = ""
                        when(dataType) {
                            tvTemp.text.toString() -> content = item.pointTemp
                            tvHumidity.text.toString() -> content = item.humidity
                            tvWind.text.toString() -> content = item.windSpeed
                            tvCloud.text.toString() -> content = item.cloud
                            tvRain.text.toString() -> content = item.rain
                        }
                        view.tvMarker.text = content
                        options.icon(BitmapDescriptorFactory.fromView(view))
                        if (!TextUtils.isEmpty(content) && !content.contains("99999")) {
                            val text = aMap!!.addMarker(options)
                            texts.add(text)
                        }
                        break
                    }
                }
            }
        }
    }

    private fun showRadar(bitmap: Bitmap, minLat: Double, minLng: Double, maxLat: Double, maxLng: Double) {
        val fromView = BitmapDescriptorFactory.fromBitmap(bitmap)
        val bounds = LatLngBounds.Builder()
                .include(LatLng(minLat, minLng))
                .include(LatLng(maxLat, maxLng))
                .build()
        if (mOverlay == null) {
            mOverlay = aMap!!.addGroundOverlay(GroundOverlayOptions()
                    .anchor(0.5f, 0.5f)
                    .positionFromBounds(bounds)
                    .image(fromView)
                    .transparency(0f))
        } else {
            mOverlay!!.setImage(null)
            mOverlay!!.setPositionFromBounds(bounds)
            mOverlay!!.setImage(fromView)
        }
        mOverlay!!.isVisible = isShowLayer
        aMap!!.runOnDrawFrame()
    }

    private inner class RadarThread: Thread() {

        private val itemList: MutableList<StationMonitorDto> = ArrayList()

        var currentState: Int
        private var index: Int
        private val count: Int
        private var isTracking: Boolean

        init {
            for (i in 0 until dataList.size) {
                val dto = dataList[i]
                if (TextUtils.equals(dto.name, dataType)) {
                    try {
                        itemList.addAll(dto.itemList)
                    } catch (e: IndexOutOfBoundsException) {
                        e.printStackTrace()
                    }
                    break
                }
            }

            count = itemList.size
            index = 0
            currentState = STATE_NONE
            isTracking = false
        }

        override fun run() {
            super.run()
            currentState = STATE_PLAYING
            while (true) {
                if (currentState == STATE_CANCEL) {
                    break
                }
                if (currentState == STATE_PAUSE) {
                    continue
                }
                if (isTracking) {
                    continue
                }
                sendRadar()
                try {
                    sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }

        private fun sendRadar() {
            if (index >= count || index < 0) {
                index = 0
            } else {
                runOnUiThread {
                    currentIndex = index
                    val dto = itemList[index]
                    changeProgress(dto, index++, count-1)
                }
            }
        }

        fun cancel() {
            currentState = STATE_CANCEL
        }

        fun pause() {
            currentState = STATE_PAUSE
        }

        fun play() {
            currentState = STATE_PLAYING
        }

        fun setCurrent(index: Int) {
            this.index = index
        }

        fun startTracking() {
            isTracking = true
        }

        fun stopTracking() {
            isTracking = false
            if (currentState == STATE_PAUSE) {
                sendRadar()
            }
        }
    }

    override fun onMapClick(p0: LatLng?) {
        val intent = Intent(this, PointForeDetailActivity::class.java)
        intent.putExtra("lat", p0!!.latitude)
        intent.putExtra("lng", p0!!.longitude)
        startActivity(intent)
    }

    override fun onCameraChange(arg0: CameraPosition?) {}

    override fun onCameraChangeFinish(arg0: CameraPosition) {
        Log.e("zoom", arg0.zoom.toString())
        val startPoint = Point(0, 0)
        val endPoint = Point(CommonUtil.widthPixels(this), CommonUtil.heightPixels(this))
        val start = aMap!!.projection.fromScreenLocation(startPoint)
        val end = aMap!!.projection.fromScreenLocation(endPoint)
        zoom = arg0.zoom
        getPointInfo(1000, start, end)
    }

    /**
     * 获取格点数据
     */
    private fun getPointInfo(delayMillis: Long, start: LatLng, end: LatLng) {
        removeTexts()
        val date = sdf4.format(Date())
        val url = "https://scapi-py.tianqi.cn/api/getqggdybql?zoom=${zoom.toInt()}&statlonlat=${start.longitude},${start.latitude}&endlonlat=${end.longitude},${end.latitude}&date=$date&appid=f63d32&key=x4pI82d2gd0bNRWNnw7un0baSUo%3D"
        handler.removeMessages(1000)
        val msg = handler.obtainMessage(1001)
        msg.obj = url
        handler.sendMessageDelayed(msg, delayMillis)
    }

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                1001 -> okHttpPoints(msg.obj.toString())
            }
        }
    }

    private fun okHttpPoints(url: String) {
        Log.e("okHttpPoints", url)
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
                        try {
                            pointList.clear()
                            val array = JSONArray(result)
                            for (i in 0 until array.length()) {
                                val dto = StationMonitorDto()
                                val itemObj = array.getJSONObject(i)
                                dto.lat = itemObj.getDouble("LAT")
                                dto.lng = itemObj.getDouble("LON")
                                dto.time = itemObj.getString("TIME")
                                val tempArray = itemObj.getJSONArray("TMP")
                                val humidityArray = itemObj.getJSONArray("RRH")
                                val windSArray = itemObj.getJSONArray("WINS")
                                val windDArray = itemObj.getJSONArray("WIND")
                                val cloudArray = itemObj.getJSONArray("ECT")
                                val rainArray = itemObj.getJSONArray("R03")
                                val list: MutableList<StationMonitorDto> = java.util.ArrayList()
                                for (j in 0 until tempArray.length()) {
                                    val data = StationMonitorDto()
                                    data.pointTemp = tempArray.getString(j)
                                    data.humidity = humidityArray.getString(j)
                                    data.windSpeed = windSArray.getString(j)
                                    data.windDir = windDArray.getString(j)
                                    data.cloud = cloudArray.getString(j)
                                    data.rain = rainArray.getString(j)
                                    try {
                                        val time = sdf2.parse(dto.time).time + 1000 * 60 * 60 * 3 * j
                                        data.time = sdf1.format(time)
//                                        val currentTime = Date().time
//                                        if (currentTime <= time) {
                                            list.add(data)
//                                        }
                                    } catch (e: ParseException) {
                                        e.printStackTrace()
                                    }
                                }
                                dto.itemList.addAll(list)
                                pointList.add(dto)
                            }

                            addPoint("")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        }.start()
    }

    private fun removeTexts() {
        for (i in texts.indices) {
            texts[i].remove()
        }
        texts.clear()
    }

    private fun showTexts() {
        for (i in texts.indices) {
            texts[i].isVisible = isShowPoint
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> finish()
            R.id.tvTemp -> {
                tvTemp.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvHumidity.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvWind.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvCloud.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvRain.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvTemp.setBackgroundResource(R.drawable.bg_map_btn_press)
                tvHumidity.setBackgroundResource(R.drawable.bg_map_btn)
                tvWind.setBackgroundResource(R.drawable.bg_map_btn)
                tvCloud.setBackgroundResource(R.drawable.bg_map_btn)
                tvRain.setBackgroundResource(R.drawable.bg_map_btn)
                ivTemp!!.setImageResource(R.drawable.icon_temp_on)
                ivHumidity.setImageResource(R.drawable.icon_humidity_off)
                ivWind.setImageResource(R.drawable.icon_wind_off)
                ivCloud.setImageResource(R.drawable.icon_cloud_off)
                ivRain.setImageResource(R.drawable.icon_rain_off)
                dataType = tvTemp.text.toString()
                switchElement()
            }
            R.id.tvHumidity -> {
                tvTemp.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvHumidity.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvWind.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvCloud.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvRain.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvTemp.setBackgroundResource(R.drawable.bg_map_btn)
                tvHumidity.setBackgroundResource(R.drawable.bg_map_btn_press)
                tvWind.setBackgroundResource(R.drawable.bg_map_btn)
                tvCloud.setBackgroundResource(R.drawable.bg_map_btn)
                tvRain.setBackgroundResource(R.drawable.bg_map_btn)
                ivTemp!!.setImageResource(R.drawable.icon_temp_off)
                ivHumidity.setImageResource(R.drawable.icon_humidity_on)
                ivWind.setImageResource(R.drawable.icon_wind_off)
                ivCloud.setImageResource(R.drawable.icon_cloud_off)
                ivRain.setImageResource(R.drawable.icon_rain_off)
                dataType = tvHumidity.text.toString()
                switchElement()
            }
            R.id.tvWind -> {
                tvTemp.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvHumidity.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvWind.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvCloud.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvRain.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvTemp.setBackgroundResource(R.drawable.bg_map_btn)
                tvHumidity.setBackgroundResource(R.drawable.bg_map_btn)
                tvWind.setBackgroundResource(R.drawable.bg_map_btn_press)
                tvCloud.setBackgroundResource(R.drawable.bg_map_btn)
                tvRain.setBackgroundResource(R.drawable.bg_map_btn)
                ivTemp!!.setImageResource(R.drawable.icon_temp_off)
                ivHumidity.setImageResource(R.drawable.icon_humidity_off)
                ivWind.setImageResource(R.drawable.icon_wind_on)
                ivCloud.setImageResource(R.drawable.icon_cloud_off)
                ivRain.setImageResource(R.drawable.icon_rain_off)
                dataType = tvWind.text.toString()
                switchElement()
            }
            R.id.tvCloud -> {
                tvTemp.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvHumidity.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvWind.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvCloud.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvRain.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvTemp.setBackgroundResource(R.drawable.bg_map_btn)
                tvHumidity.setBackgroundResource(R.drawable.bg_map_btn)
                tvWind.setBackgroundResource(R.drawable.bg_map_btn)
                tvCloud.setBackgroundResource(R.drawable.bg_map_btn_press)
                tvRain.setBackgroundResource(R.drawable.bg_map_btn)
                ivTemp!!.setImageResource(R.drawable.icon_temp_off)
                ivHumidity.setImageResource(R.drawable.icon_humidity_off)
                ivWind.setImageResource(R.drawable.icon_wind_off)
                ivCloud.setImageResource(R.drawable.icon_cloud_on)
                ivRain.setImageResource(R.drawable.icon_rain_off)
                dataType = tvCloud.text.toString()
                switchElement()
            }
            R.id.tvRain -> {
                tvTemp.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvHumidity.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvWind.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvCloud.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvRain.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvTemp.setBackgroundResource(R.drawable.bg_map_btn)
                tvHumidity.setBackgroundResource(R.drawable.bg_map_btn)
                tvWind.setBackgroundResource(R.drawable.bg_map_btn)
                tvCloud.setBackgroundResource(R.drawable.bg_map_btn)
                tvRain.setBackgroundResource(R.drawable.bg_map_btn_press)
                ivTemp!!.setImageResource(R.drawable.icon_temp_off)
                ivHumidity.setImageResource(R.drawable.icon_humidity_off)
                ivWind.setImageResource(R.drawable.icon_wind_off)
                ivCloud.setImageResource(R.drawable.icon_cloud_off)
                ivRain.setImageResource(R.drawable.icon_rain_on)
                dataType = tvRain.text.toString()
                switchElement()
            }
            R.id.ivSwitch -> {
                if (mapType == AMap.MAP_TYPE_NORMAL) {
                    mapType = AMap.MAP_TYPE_SATELLITE
                    ivSwitch.setImageResource(R.drawable.icon_map_switch_press)
                    tvName!!.setTextColor(Color.WHITE)
                } else if (mapType == AMap.MAP_TYPE_SATELLITE) {
                    mapType = AMap.MAP_TYPE_NORMAL
                    ivSwitch.setImageResource(R.drawable.icon_map_switch)
                    tvName!!.setTextColor(Color.BLACK)
                }
                if (aMap != null) {
                    aMap!!.mapType = mapType
                }
                switchElement()
            }
            R.id.ivLegend -> {
                if (imageLegend.visibility == View.VISIBLE) {
                    imageLegend.visibility = View.GONE
                } else {
                    imageLegend.visibility = View.VISIBLE
                }
            }
            R.id.ivLocation -> {
                if (zoom >= 12f) {
                    aMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locationLat, locationLng), 3.5f))
                } else {
                    aMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locationLat, locationLng), 12.0f))
                }
            }
            R.id.ivPlay -> {
                if (mRadarThread != null && mRadarThread!!.currentState == STATE_PLAYING) {
                    mRadarThread!!.pause()
                    ivPlay.setImageResource(R.drawable.icon_play)
                } else if (mRadarThread != null && mRadarThread!!.currentState == STATE_PAUSE) {
                    mRadarThread!!.play()
                    ivPlay.setImageResource(R.drawable.icon_pause)
                } else if (mRadarThread == null) {
                    ivPlay.setImageResource(R.drawable.icon_pause)
                    if (mRadarThread != null) {
                        mRadarThread!!.cancel()
                        mRadarThread = null
                    }
                    mRadarThread = RadarThread()
                    mRadarThread!!.start()
                }
            }
            R.id.ivPoint -> {
                isShowPoint = !isShowPoint
                if (isShowPoint) {
                    ivPoint.setImageResource(R.drawable.icon_map_value_press)
                } else {
                    ivPoint.setImageResource(R.drawable.icon_map_value)
                }
                showTexts()
            }
            R.id.ivLayer -> {
                if (mOverlay == null) {
                    return
                }
                isShowLayer = !isShowLayer
                if (isShowLayer) {
                    ivLayer.setImageResource(R.drawable.icon_map_layer_press)
                } else {
                    ivLayer.setImageResource(R.drawable.icon_map_layer)
                }
                mOverlay!!.isVisible = isShowLayer
            }
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
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (mapView != null) {
            mapView!!.onSaveInstanceState(outState)
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

    //需要申请的所有权限
    var allPermissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    //拒绝的权限集合
    var deniedList: MutableList<String> = ArrayList()

    /**
     * 申请定位权限
     */
    private fun checkAuthority() {
        if (Build.VERSION.SDK_INT < 23) {
            init()
        } else {
            deniedList.clear()
            for (permission in allPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    deniedList.add(permission)
                }
            }
            if (deniedList.isEmpty()) { //所有权限都授予
                init()
            } else {
                val permissions = deniedList.toTypedArray() //将list转成数组
                ActivityCompat.requestPermissions(this, permissions, AuthorityUtil.AUTHOR_LOCATION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AuthorityUtil.AUTHOR_LOCATION -> if (grantResults.isNotEmpty()) {
                var isAllGranted = true //是否全部授权
                for (gResult in grantResults) {
                    if (gResult != PackageManager.PERMISSION_GRANTED) {
                        isAllGranted = false
                        break
                    }
                }
                if (isAllGranted) { //所有权限都授予
                    init()
                } else { //只要有一个没有授权，就提示进入设置界面设置
                    AuthorityUtil.intentAuthorSetting(this, "\"" + getString(R.string.app_name) + "\"" + "需要使用定位权限、存储权限，是否前往设置？")
                }
            } else {
                for (permission in permissions) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission!!)) {
                        AuthorityUtil.intentAuthorSetting(this, "\"" + getString(R.string.app_name) + "\"" + "需要使用定位权限、存储权限，是否前往设置？")
                        break
                    }
                }
            }
        }
    }

}
