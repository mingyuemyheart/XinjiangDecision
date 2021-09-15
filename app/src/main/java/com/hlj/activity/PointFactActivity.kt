package com.hlj.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
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
import android.widget.TextView
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.OnCameraChangeListener
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
import kotlinx.android.synthetic.main.layout_point_info.view.*
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 格点实况
 */
class PointFactActivity : BaseActivity(), OnClickListener, AMapLocationListener, AMap.OnMapClickListener, FactManager.RadarListener, OnCameraChangeListener {

    private var aMap: AMap? = null
    private var zoom = 3.7f
    private val dataList: MutableList<StationMonitorDto> = ArrayList()
    private val sdf1 = SimpleDateFormat("dd日HH时", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("yyyyMMddHH", Locale.CHINA)
    private val sdf3 = SimpleDateFormat("HH时", Locale.CHINA)
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
        setContentView(R.layout.activity_point_fact)
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
        tvVisible.setOnClickListener(this)
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
        aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.926628, 105.178100), zoom))
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
                                        val list: MutableList<StationMonitorDto> = ArrayList()
                                        val data = StationMonitorDto()
                                        data.name = dto.name
                                        if (!itemObj.isNull("time")) {
                                            data.time = itemObj.getString("time")
                                            data.time = sdf1.format(sdf2.parse(data.time))
                                        }
                                        if (!itemObj.isNull("imgurl")) {
                                            data.imgUrl = itemObj.getString("imgurl")
                                        }
                                        data.leftLat = minlat
                                        data.leftLng = minlon
                                        data.rightLat = maxlat
                                        data.rightLng = maxlon
                                        list.add(data)
                                        dto.itemList.addAll(list)
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
            Picasso.get().load(data.legendUrl).into(imageLegend)
            if (TextUtils.equals(data.name, dataType)) {
                startDownLoadImgs(data.itemList)
                break
            }
        }
    }

    private fun startDownLoadImgs(images: ArrayList<StationMonitorDto>?) {
        if (mRadarThread != null) {
            mRadarThread!!.cancel()
            mRadarThread = null
        }
        mFactManager!!.loadImagesAsyn(images, this)
    }

    override fun onResult(result: Int, images: ArrayList<StationMonitorDto>) {
        runOnUiThread {
            cancelDialog()
//            llSeekBar!!.visibility = View.VISIBLE
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
                tvVisible.text.toString() -> unit = getString(R.string.unit_m)
            }

            val endTime = sdf1.parse(dto.time).time
            val startTime = sdf1.parse(dto.time).time-1000*60*60
            tvName.text = "逐小时${dto.name}格点实况(${sdf3.format(startTime)}-${sdf3.format(endTime)})[单位:${unit}]"
        }
        if (!TextUtils.isEmpty(dto.imgPath)) {
            val bitmap = BitmapFactory.decodeFile(dto.imgPath)
            if (bitmap != null) {
                showRadar(bitmap, dto.leftLat, dto.leftLng, dto.rightLat, dto.rightLng)
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
        okHttpPointDetail(p0)
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
        val url = "https://scapi-py.tianqi.cn/api/getqggdsk?zoom=${zoom.toInt()}&statlonlat=${start.longitude},${start.latitude}&endlonlat=${end.longitude},${end.latitude}&date=$date&appid=f63d32&key=x4pI82d2gd0bNRWNnw7un0baSUo%3D"
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
                                val itemObj = array.getJSONObject(i)
                                val dto = StationMonitorDto()
                                if (!itemObj.isNull("LAT")) {
                                    dto.lat = itemObj.getDouble("LAT")
                                }
                                if (!itemObj.isNull("LON")) {
                                    dto.lng = itemObj.getDouble("LON")
                                }
                                if (!itemObj.isNull("TEM")) {
                                    dto.pointTemp = itemObj.getString("TEM")
                                }
                                if (!itemObj.isNull("RHU")) {
                                    dto.humidity = itemObj.getString("RHU")
                                }
                                if (!itemObj.isNull("WINS")) {
                                    dto.windSpeed = itemObj.getString("WINS")
                                }
                                if (!itemObj.isNull("VIS")) {
                                    dto.visibility = itemObj.getString("VIS")
                                }
                                if (!itemObj.isNull("TCDC")) {
                                    dto.cloud = itemObj.getString("TCDC")
                                }
                                pointList.add(dto)
                            }

                            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            for (dto in pointList) {
                                val options = MarkerOptions()
                                options.position(LatLng(dto.lat, dto.lng))
                                val view = inflater.inflate(R.layout.layout_point_marker, null)
                                view.tvMarker.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10f)
                                if (mapType == AMap.MAP_TYPE_NORMAL) {
                                    view.tvMarker.setTextColor(Color.RED)
                                } else if (mapType == AMap.MAP_TYPE_SATELLITE) {
                                    view.tvMarker.setTextColor(Color.WHITE)
                                }
                                var content = ""
                                when(dataType) {
                                    tvTemp.text.toString() -> content = dto.pointTemp
                                    tvHumidity.text.toString() -> content = dto.humidity
                                    tvWind.text.toString() -> content = dto.windSpeed
                                    tvVisible.text.toString() -> content = dto.visibility
                                }
                                view.tvMarker.text = content
                                options.icon(BitmapDescriptorFactory.fromView(view))
                                if (!TextUtils.isEmpty(content) && !content.contains("99999")) {
                                    val text = aMap!!.addMarker(options)
                                    texts.add(text)
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

    private var detailMarker: Marker? = null
    private fun okHttpPointDetail(p0: LatLng?) {
        showDialog()
        Thread {
            val url = "http://data-mic.cxwldata.cn/other/gdsk/${p0!!.latitude}/${p0!!.longitude}"
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
                        try {
                            val obj = JSONObject(result)
                            var tem = "气温："
                            if (!obj.isNull("tem")) {
                                tem += obj.getString("tem")+getString(R.string.unit_degree)
                            }
                            var rhu = "相对湿度："
                            if (!obj.isNull("rhu")) {
                                rhu += obj.getString("rhu")+getString(R.string.unit_percent)
                            }
                            var wind_dir = ""
                            if (!obj.isNull("wd")) {
                                val fx = obj.getDouble("wd")
                                if (fx >= 22.5 && fx < 67.5) {
                                    wind_dir = "东北风"
                                } else if (fx >= 67.5 && fx < 112.5) {
                                    wind_dir = "东风"
                                } else if (fx >= 112.5 && fx < 157.5) {
                                    wind_dir = "东南风"
                                } else if (fx >= 157.5 && fx < 202.5) {
                                    wind_dir = "南风"
                                } else if (fx >= 202.5 && fx < 247.5) {
                                    wind_dir = "西南风"
                                } else if (fx >= 247.5 && fx < 292.5) {
                                    wind_dir = "西风"
                                } else if (fx >= 292.5 && fx < 337.5) {
                                    wind_dir = "西北风"
                                } else {
                                    wind_dir = "北风"
                                }
                            }
                            var ws = ""
                            if (!obj.isNull("ws")) {
                                ws = obj.getString("ws")+getString(R.string.unit_speed)
                            }
                            var vis = "能见度："
                            if (!obj.isNull("vis")) {
                                vis += obj.getString("vis")+getString(R.string.unit_km)
                            }

                            if (detailMarker != null) {
                                detailMarker!!.remove()
                            }
                            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            val options = MarkerOptions()
                            options.position(LatLng(p0.latitude, p0.longitude))
                            val view = inflater.inflate(R.layout.layout_point_info, null)
                            view.tvContent.text = "$tem\n$rhu\n风向风速：$wind_dir $ws\n$vis"
                            options.icon(BitmapDescriptorFactory.fromView(view))
                            detailMarker = aMap!!.addMarker(options)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        }.start()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> finish()
            R.id.tvTemp -> {
                tvTemp.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvHumidity.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvWind.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvVisible.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvTemp.setBackgroundResource(R.drawable.bg_map_btn_press)
                tvHumidity.setBackgroundResource(R.drawable.bg_map_btn)
                tvWind.setBackgroundResource(R.drawable.bg_map_btn)
                tvVisible.setBackgroundResource(R.drawable.bg_map_btn)
                ivTemp!!.setImageResource(R.drawable.icon_temp_on)
                ivHumidity.setImageResource(R.drawable.icon_humidity_off)
                ivWind.setImageResource(R.drawable.icon_wind_off)
                ivVisible.setImageResource(R.drawable.icon_cloud_off)
                dataType = tvTemp.text.toString()
                switchElement()
            }
            R.id.tvHumidity -> {
                tvTemp.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvHumidity.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvWind.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvVisible.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvTemp.setBackgroundResource(R.drawable.bg_map_btn)
                tvHumidity.setBackgroundResource(R.drawable.bg_map_btn_press)
                tvWind.setBackgroundResource(R.drawable.bg_map_btn)
                tvVisible.setBackgroundResource(R.drawable.bg_map_btn)
                ivTemp!!.setImageResource(R.drawable.icon_temp_off)
                ivHumidity.setImageResource(R.drawable.icon_humidity_on)
                ivWind.setImageResource(R.drawable.icon_wind_off)
                ivVisible.setImageResource(R.drawable.icon_cloud_off)
                dataType = tvHumidity.text.toString()
                switchElement()
            }
            R.id.tvWind -> {
                tvTemp.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvHumidity.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvWind.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvVisible.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvTemp.setBackgroundResource(R.drawable.bg_map_btn)
                tvHumidity.setBackgroundResource(R.drawable.bg_map_btn)
                tvWind.setBackgroundResource(R.drawable.bg_map_btn_press)
                tvVisible.setBackgroundResource(R.drawable.bg_map_btn)
                ivTemp!!.setImageResource(R.drawable.icon_temp_off)
                ivHumidity.setImageResource(R.drawable.icon_humidity_off)
                ivWind.setImageResource(R.drawable.icon_wind_on)
                ivVisible.setImageResource(R.drawable.icon_cloud_off)
                dataType = tvWind.text.toString()
                switchElement()
            }
            R.id.tvVisible -> {
                tvTemp.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvHumidity.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvWind.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvVisible.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvTemp.setBackgroundResource(R.drawable.bg_map_btn)
                tvHumidity.setBackgroundResource(R.drawable.bg_map_btn)
                tvWind.setBackgroundResource(R.drawable.bg_map_btn)
                tvVisible.setBackgroundResource(R.drawable.bg_map_btn_press)
                ivTemp!!.setImageResource(R.drawable.icon_temp_off)
                ivHumidity.setImageResource(R.drawable.icon_humidity_off)
                ivWind.setImageResource(R.drawable.icon_wind_off)
                ivVisible.setImageResource(R.drawable.icon_cloud_on)
                dataType = tvVisible.text.toString()
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
