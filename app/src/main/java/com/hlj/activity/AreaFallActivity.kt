package com.hlj.activity

import android.Manifest
import android.content.pm.PackageManager
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
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.hlj.common.CONST
import com.hlj.utils.AuthorityUtil
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_area_fall.*
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

/**
 * 全区降水量预报
 */
class AreaFallActivity : BaseActivity(), OnClickListener, AMapLocationListener, AMap.OnCameraChangeListener {

    private var aMap: AMap? = null
    private val sdf1 = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    private var zoom = 3.5f
    private var locationLat = 35.926628
    private var locationLng = 105.178100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_area_fall)
        initMap(savedInstanceState)
        initWidget()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack.setOnClickListener(this)
        ivLegendPrompt.setOnClickListener(this)
        ivLocation.setOnClickListener(this)

        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }
        checkAuthority()
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
            ivLocation!!.visibility = View.VISIBLE
            val latLng = LatLng(amapLocation.latitude, amapLocation.longitude)
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
            val locationMarker = aMap!!.addMarker(options)
            locationMarker.isClickable = false
        }
    }

    /**
     * 初始化地图
     */
    private fun initMap(bundle: Bundle?) {
        mapView.onCreate(bundle)
        if (aMap == null) {
            aMap = mapView.map
        }
        aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locationLat, locationLng), zoom))
        aMap!!.uiSettings.isZoomControlsEnabled = false
        aMap!!.uiSettings.isRotateGesturesEnabled = false
        aMap!!.setOnCameraChangeListener(this)
        aMap!!.setOnMapLoadedListener {
            CommonUtil.drawHLJJson(this, aMap)
            okHttpData()
        }
    }

    override fun onCameraChange(arg0: CameraPosition?) {}

    override fun onCameraChangeFinish(arg0: CameraPosition) {
        zoom = arg0.zoom
    }

    /**
     * 绘制单要素图层
     */
    private fun okHttpData() {
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
                                val obj = JSONObject(result)
                                tvName.text = "全区降水量预报"
                                if (!obj.isNull("time")) {
                                    val startTime = obj.getLong("time") * 1000
                                    val endTime = startTime + 1000 * 60 * 60
                                    tvTime.text = "${sdf1.format(startTime)}~${sdf1.format(endTime)}"
                                }
                                if (!obj.isNull("tuliurl")) {
                                    val tuliurl = obj.getString("tuliurl")
                                    if (!TextUtils.isEmpty(tuliurl)) {
                                        Picasso.get().load(tuliurl).into(ivLegend)
                                    }
                                }
                                if (!obj.isNull("lines")) {
                                    val lines = obj.getJSONArray("lines")
                                    for (i in 0 until lines.length()) {
                                        val itemObj = lines.getJSONObject(i)
                                        var color = Color.BLACK
                                        if (!itemObj.isNull("c")) {
                                            val c = itemObj.getString("c")
                                            color = Color.parseColor(c)
                                        }
                                        if (!itemObj.isNull("points")) {
                                            val points = itemObj.getJSONArray("points")
                                            val polylineOption = PolylineOptions()
                                            polylineOption.width(6.0f).color(color)
                                            for (j in 0 until points.length()) {
                                                val point = points.getJSONObject(j)
                                                val lat = point.getDouble("y")
                                                val lng = point.getDouble("x")
                                                polylineOption.add(LatLng(lat, lng))
                                            }
                                            aMap!!.addPolyline(polylineOption)
                                        }
                                        if (!itemObj.isNull("flag")) {
                                            val flag = itemObj.getJSONObject("flag")
                                            if (!flag.isNull("text")) {
                                                val text = flag.getString("text")
                                                val lat = flag.getDouble("y")
                                                val lng = flag.getDouble("x")
                                                val to = TextOptions()
                                                to.position(LatLng(lat, lng))
                                                to.text(text)
                                                to.fontColor(color)
                                                to.fontSize(36)
                                                to.backgroundColor(Color.TRANSPARENT)
                                                aMap!!.addText(to)
                                            }
                                        }
                                    }
                                }
                                if (!obj.isNull("closed_contours")) {
                                    val closedContours = obj.getJSONArray("closed_contours")
                                    for (i in 0 until closedContours.length()) {
                                        val itemObj = closedContours.getJSONObject(i)
                                        var color = Color.BLACK
                                        if (!itemObj.isNull("c")) {
                                            val c = itemObj.getString("c")
                                            color = Color.parseColor(c)
                                        }
                                        if (!itemObj.isNull("points")) {
                                            val points = itemObj.getJSONArray("points")
                                            val polylineOption = PolylineOptions()
                                            polylineOption.width(6.0f).color(color)
                                            for (j in 0 until points.length()) {
                                                val point = points.getJSONObject(j)
                                                val lat = point.getDouble("y")
                                                val lng = point.getDouble("x")
                                                polylineOption.add(LatLng(lat, lng))
                                            }
                                            aMap!!.addPolyline(polylineOption)
                                        }
                                        if (!itemObj.isNull("flag")) {
                                            val flag = itemObj.getJSONObject("flag")
                                            if (!flag.isNull("text")) {
                                                val text = flag.getString("text")
                                                val lat = flag.getDouble("y")
                                                val lng = flag.getDouble("x")
                                                val to = TextOptions()
                                                to.position(LatLng(lat, lng))
                                                to.text(text)
                                                to.fontColor(color)
                                                to.fontSize(36)
                                                to.backgroundColor(Color.TRANSPARENT)
                                                aMap!!.addText(to)
                                            }
                                        }
                                    }
                                }
                                if (!obj.isNull("lines_symbol")) {
                                    val linesSymbol = obj.getJSONArray("lines_symbol")
                                    for (i in 0 until linesSymbol.length()) {
                                        val itemObj = linesSymbol.getJSONObject(i)
                                        var color = Color.BLACK
                                        if (!itemObj.isNull("c")) {
                                            val c = itemObj.getString("c")
                                            color = Color.parseColor(c)
                                        }
                                        if (!itemObj.isNull("points")) {
                                            val points = itemObj.getJSONArray("points")
                                            val polylineOption = PolylineOptions()
                                            polylineOption.width(6.0f).color(color)
                                            for (j in 0 until points.length()) {
                                                val point = points.getJSONObject(j)
                                                val lat = point.getDouble("y")
                                                val lng = point.getDouble("x")
                                                polylineOption.add(LatLng(lat, lng))
                                            }
                                            aMap!!.addPolyline(polylineOption)
                                        }
                                        if (!itemObj.isNull("flag")) {
                                            val flag = itemObj.getJSONObject("flag")
                                            if (!flag.isNull("text")) {
                                                val text = flag.getString("text")
                                                val lat = flag.getDouble("y")
                                                val lng = flag.getDouble("x")
                                                val to = TextOptions()
                                                to.position(LatLng(lat, lng))
                                                to.text(text)
                                                to.fontColor(color)
                                                to.fontSize(36)
                                                to.backgroundColor(Color.TRANSPARENT)
                                                aMap!!.addText(to)
                                            }
                                        }
                                    }
                                }
                                if (!obj.isNull("symbols")) {
                                    val symbols = obj.getJSONArray("symbols")
                                    for (i in 0 until symbols.length()) {
                                        val itemObj = symbols.getJSONObject(i)
                                        if (!itemObj.isNull("text")) {
                                            val text = itemObj.getString("text")
                                            val lat = itemObj.getDouble("y")
                                            val lng = itemObj.getDouble("x")
                                            val to = TextOptions()
                                            to.position(LatLng(lat, lng))
                                            to.text(text)
                                            to.fontColor(Color.BLACK)
                                            to.fontSize(36)
                                            to.backgroundColor(Color.TRANSPARENT)
                                            aMap!!.addText(to)
                                        }
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

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> finish()
            R.id.ivLegendPrompt -> {
                if (ivLegend.visibility == View.VISIBLE) {
                    ivLegend.visibility = View.INVISIBLE
                } else {
                    ivLegend.visibility = View.VISIBLE
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

    /**
     * 申请权限
     */
    private fun checkAuthority() {
        if (Build.VERSION.SDK_INT < 23) {
            startLocation()
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), AuthorityUtil.AUTHOR_LOCATION)
            } else {
                startLocation()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AuthorityUtil.AUTHOR_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocation()
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        AuthorityUtil.intentAuthorSetting(this, "\"" + getString(R.string.app_name) + "\"" + "需要使用定位权限，是否前往设置？")
                    }
                }
            }
        }
    }

}
