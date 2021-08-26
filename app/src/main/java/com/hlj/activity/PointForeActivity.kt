package com.hlj.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
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
import com.hlj.utils.AuthorityUtil
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import kotlinx.android.synthetic.main.activity_point_fore.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import shawn.cxwl.com.hlj.R
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 格点预报
 */
class PointForeActivity : BaseActivity(), OnClickListener, AMapLocationListener, OnCameraChangeListener, AMap.OnMarkerClickListener {

    private var aMap: AMap? = null
    private var zoom = 3.7f
    private val dataList: MutableList<StationMonitorDto> = ArrayList()
    private val sdf1 = SimpleDateFormat("dd日HH时", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("yyyyMMddHH", Locale.CHINA)
    private val sdf3 = SimpleDateFormat("yyyy-MM-dd HH时", Locale.CHINA)
    private val markers: MutableList<Marker> = ArrayList()
    private var dataType = 1 //1温度、2湿度、3风速、4降水、5云量
    private var locationLat = 35.926628
    private var locationLng = 105.178100
    private var mapType = AMap.MAP_TYPE_NORMAL
    private var start: LatLng? = null
    private var end:LatLng? = null
    private var savedInstanceState: Bundle? = null
    private var currentIndex = 0 //当前时次数据
    private var mRadarThread: RadarThread? = null
    private var locationMarker: Marker? = null
    private val STATE_NONE = 0
    private val STATE_PLAYING = 1
    private val STATE_PAUSE = 2
    private val STATE_CANCEL = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_point_fore)
        this.savedInstanceState = savedInstanceState
        checkAuthority()
    }

    private fun init() {
        showDialog()
        initMap()
        initWidget()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack.setOnClickListener(this)
        ivTemp.setOnClickListener(this)
        ivHumidity.setOnClickListener(this)
        ivWind.setOnClickListener(this)
        ivCloud.setOnClickListener(this)
        ivRain.setOnClickListener(this)
        ivLocation.setOnClickListener(this)
        ivSwitch.setOnClickListener(this)
        tvDataSource.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        tvDataSource.setOnClickListener(this)
        ivDataSource.setOnClickListener(this)
        ivPlay.setOnClickListener(this)
        seekBar.setOnSeekBarChangeListener(seekbarListener)
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
        val bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(resources, R.drawable.shawn_icon_map_location),
                CommonUtil.dip2px(this, 21f).toInt(), CommonUtil.dip2px(this, 32f).toInt())
        if (bitmap != null) {
            options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        } else {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.shawn_icon_map_location))
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
        aMap!!.setOnCameraChangeListener(this)
        aMap!!.setOnMarkerClickListener(this)
        aMap!!.setOnMapLoadedListener {
            CommonUtil.drawHLJJson(this, aMap)
        }
    }

    override fun onCameraChange(arg0: CameraPosition?) {}

    override fun onCameraChangeFinish(arg0: CameraPosition) {
        val startPoint = Point(0, 0)
        val endPoint = Point(CommonUtil.widthPixels(this), CommonUtil.heightPixels(this))
        start = aMap!!.projection.fromScreenLocation(startPoint)
        end = aMap!!.projection.fromScreenLocation(endPoint)
        zoom = arg0.zoom
        getPointInfo()
    }

    /**
     * 获取格点数据
     */
    private fun getPointInfo() {
        val url = String.format("http://scapi.weather.com.cn/weather/getqggdybql?zoom=%s&statlonlat=%s,%s&endlonlat=%s,%s&test=ncg",
                zoom.toInt(), start!!.longitude, start!!.latitude, end!!.longitude, end!!.latitude)
        handler.removeMessages(1001)
        val msg = handler.obtainMessage(1001)
        msg.obj = url
        handler.sendMessageDelayed(msg, 1000)
    }

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                1001 -> okHttpList(msg.obj.toString() + "")
            }
        }
    }

    private fun okHttpList(url: String) {
        Thread(Runnable {
            Log.e("pointFore", url)
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
                                val array = JSONArray(result)
                                for (i in 0 until array.length()) {
                                    val dto = StationMonitorDto()
                                    val itemObj = array.getJSONObject(i)
                                    dto.lat = itemObj.getDouble("LAT")
                                    dto.lng = itemObj.getDouble("LON")
                                    dto.time = itemObj.getString("TIME")
                                    if (i == 0) {
                                        try {
                                            tvPublishTime.text = sdf3.format(sdf2.parse(dto.time)) + "发布"
                                        } catch (e: ParseException) {
                                            e.printStackTrace()
                                        }
                                    }
                                    val tempArray = itemObj.getJSONArray("TMP")
                                    val humidityArray = itemObj.getJSONArray("RRH")
                                    val windSArray = itemObj.getJSONArray("WINS")
                                    val windDArray = itemObj.getJSONArray("WIND")
                                    val cloudArray = itemObj.getJSONArray("ECT")
                                    val rainArray = itemObj.getJSONArray("R03")
                                    val list: MutableList<StationMonitorDto> = ArrayList()
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
                                            val currentTime = Date().time
                                            if (currentTime <= time) {
                                                list.add(data)
                                            }
                                        } catch (e: ParseException) {
                                            e.printStackTrace()
                                        }
                                    }
                                    dto.itemList.addAll(list)
                                    dataList.add(dto)
                                }
                                switchElement()
                                if (dataList.size > 0) {
                                    try {
                                        val dto = dataList[currentIndex]
                                        if (dto != null && dto.itemList.size > 0) {
                                            val data = dto.itemList[0]
                                            changeProgress(data.time, 0, dto.itemList.size - 1)
                                        }
                                    } catch (e: IndexOutOfBoundsException) {
                                        e.printStackTrace()
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })
        }).start()
    }

    private fun removeTexts() {
        for (i in markers.indices) {
            markers[i].remove()
        }
        markers.clear()
    }

    /**
     * 切换要素
     */
    private fun switchElement() {
        removeTexts()
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        for (dto in dataList) {
            val options = MarkerOptions()
            options.position(LatLng(dto.lat, dto.lng))
            val view = inflater.inflate(R.layout.shawn_point_fore_icon, null)
            val tvMarker = view.findViewById<TextView>(R.id.tvMarker)
            tvMarker.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10f)
            if (mapType == AMap.MAP_TYPE_NORMAL) {
                tvMarker.setTextColor(Color.RED)
            } else if (mapType == AMap.MAP_TYPE_SATELLITE) {
                tvMarker.setTextColor(Color.WHITE)
            }
            if (dto.itemList.size > 0) {
                var content: String? = ""
                when (dataType) {
                    1 -> {
                        content = dto.itemList[currentIndex].pointTemp
                    }
                    2 -> {
                        content = dto.itemList[currentIndex].humidity
                    }
                    3 -> {
                        content = dto.itemList[currentIndex].windSpeed
                    }
                    5 -> {
                        content = dto.itemList[currentIndex].cloud
                    }
                    4 -> {
                        content = dto.itemList[currentIndex].rain
                    }
                }
                tvMarker.text = content
                options.icon(BitmapDescriptorFactory.fromView(view))
                if (!TextUtils.isEmpty(content)) {
                    val value = java.lang.Float.valueOf(content)
                    if (value < 9000) {
                        val marker = aMap!!.addMarker(options)
                        markers.add(marker)
                    }
                }
            }
        }
    }

    private inner class RadarThread(private val itemList: MutableList<StationMonitorDto>) : Thread() {

        var currentState: Int
        private var index: Int
        private val count: Int
        private var isTracking: Boolean

        init {
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
                    switchElement()
                    changeProgress(dto.time, index++, count - 1)
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

    @SuppressLint("SimpleDateFormat")
    private fun changeProgress(time: String, progress: Int, max: Int) {
        if (seekBar != null) {
            seekBar!!.max = max
            seekBar!!.progress = progress
        }
        if (!TextUtils.isEmpty(time)) {
            tvTime.text = time
            changeLayerName()
        }
    }

    private fun changeLayerName() {
        when (dataType) {
            1 -> {
                tvName.text = tvTime.text.toString() + "格点温度预报[单位:" + getString(R.string.unit_degree) + "]"
            }
            2 -> {
                tvName.text = tvTime.text.toString() + "格点相对湿度预报[单位:" + getString(R.string.unit_percent) + "]"
            }
            3 -> {
                tvName.text = tvTime.text.toString() + "格点风速预报[单位:" + getString(R.string.unit_speed) + "]"
            }
            5 -> {
                tvName.text = tvTime.text.toString() + "格点云量预报[单位:" + getString(R.string.unit_percent) + "]"
            }
            4 -> {
                tvName.text = tvTime.text.toString() + "格点降水预报[单位:" + getString(R.string.unit_mm) + "]"
            }
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null && marker !== locationMarker) {
            val intent = Intent(this, PointForeDetailActivity::class.java)
            intent.putExtra("lat", marker.position.latitude)
            intent.putExtra("lng", marker.position.longitude)
            startActivity(intent)
        }
        return true
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
            R.id.ivTemp -> {
                ivTemp!!.setImageResource(R.drawable.com_temp_press)
                ivHumidity.setImageResource(R.drawable.com_humidity)
                ivWind.setImageResource(R.drawable.com_wind)
                ivCloud.setImageResource(R.drawable.com_cloud)
                ivRain.setImageResource(R.drawable.com_rain)
                dataType = 1
                switchElement()
                changeLayerName()
            }
            R.id.ivHumidity -> {
                ivTemp!!.setImageResource(R.drawable.com_temp)
                ivHumidity.setImageResource(R.drawable.com_humidity_press)
                ivWind.setImageResource(R.drawable.com_wind)
                ivCloud.setImageResource(R.drawable.com_cloud)
                ivRain.setImageResource(R.drawable.com_rain)
                dataType = 2
                switchElement()
                changeLayerName()
            }
            R.id.ivWind -> {
                ivTemp!!.setImageResource(R.drawable.com_temp)
                ivHumidity.setImageResource(R.drawable.com_humidity)
                ivWind.setImageResource(R.drawable.com_wind_press)
                ivCloud.setImageResource(R.drawable.com_cloud)
                ivRain.setImageResource(R.drawable.com_rain)
                dataType = 3
                switchElement()
                changeLayerName()
            }
            R.id.ivCloud -> {
                ivTemp!!.setImageResource(R.drawable.com_temp)
                ivHumidity.setImageResource(R.drawable.com_humidity)
                ivWind.setImageResource(R.drawable.com_wind)
                ivCloud.setImageResource(R.drawable.com_cloud_press)
                ivRain.setImageResource(R.drawable.com_rain)
                dataType = 5
                switchElement()
                changeLayerName()
            }
            R.id.ivRain -> {
                ivTemp!!.setImageResource(R.drawable.com_temp)
                ivHumidity.setImageResource(R.drawable.com_humidity)
                ivWind.setImageResource(R.drawable.com_wind)
                ivCloud.setImageResource(R.drawable.com_cloud)
                ivRain.setImageResource(R.drawable.com_rain_press)
                dataType = 4
                switchElement()
                changeLayerName()
            }
            R.id.ivSwitch -> {
                if (mapType == AMap.MAP_TYPE_NORMAL) {
                    mapType = AMap.MAP_TYPE_SATELLITE
                    ivSwitch.setImageResource(R.drawable.com_switch_map_press)
                    tvName!!.setTextColor(Color.WHITE)
                    tvPublishTime.setTextColor(Color.WHITE)
                } else if (mapType == AMap.MAP_TYPE_SATELLITE) {
                    mapType = AMap.MAP_TYPE_NORMAL
                    ivSwitch.setImageResource(R.drawable.com_switch_map)
                    tvName!!.setTextColor(Color.BLACK)
                    tvPublishTime.setTextColor(Color.BLACK)
                }
                if (aMap != null) {
                    aMap!!.mapType = mapType
                }
                switchElement()
            }
            R.id.ivDataSource -> if (tvDataSource.visibility == View.VISIBLE) {
                tvDataSource.visibility = View.GONE
                ivDataSource.setImageResource(R.drawable.com_data_source)
            } else {
                tvDataSource.visibility = View.VISIBLE
                ivDataSource.setImageResource(R.drawable.com_data_source_press)
            }
            R.id.tvDataSource -> {
                val intent = Intent(this, WebviewActivity::class.java)
                intent.putExtra(CONST.ACTIVITY_NAME, "中央气象台智能网格预报产品")
                intent.putExtra(CONST.WEB_URL, "http://www.cma.gov.cn/2011xzt/2017zt/2017qmt/20170728/")
                startActivity(intent)
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
                    mRadarThread = RadarThread(dataList[currentIndex].itemList)
                    mRadarThread!!.start()
                }
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
