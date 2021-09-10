package com.hlj.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ThumbnailUtils
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
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
import kotlinx.android.synthetic.main.activity_point_fore.*
import kotlinx.android.synthetic.main.layout_title.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import shawn.cxwl.com.hlj.R
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 格点预报
 */
class PointForeActivity : BaseActivity(), OnClickListener, AMapLocationListener, AMap.OnMarkerClickListener, AMap.OnMapClickListener, FactManager.RadarListener {

    private var aMap: AMap? = null
    private var zoom = 3.7f
    private val dataList: MutableList<StationMonitorDto> = ArrayList()
    private val sdf1 = SimpleDateFormat("dd日HH时", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("yyyyMMddHH", Locale.CHINA)
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
        aMap!!.setOnMarkerClickListener(this)
        aMap!!.setOnMapClickListener(this)
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

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null && marker !== locationMarker) {
            val intent = Intent(this, PointForeDetailActivity::class.java)
            intent.putExtra("lat", marker.position.latitude)
            intent.putExtra("lng", marker.position.longitude)
            startActivity(intent)
        }
        return true
    }

    override fun onMapClick(p0: LatLng?) {
        val intent = Intent(this, PointForeDetailActivity::class.java)
        intent.putExtra("lat", p0!!.latitude)
        intent.putExtra("lng", p0!!.longitude)
        startActivity(intent)
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
