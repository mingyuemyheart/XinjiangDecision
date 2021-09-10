package com.hlj.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import cn.com.weather.api.WeatherAPI
import cn.com.weather.beans.Weather
import cn.com.weather.constants.Constants.Language
import cn.com.weather.listener.AsyncResponseHandler
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.*
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.maps.model.animation.ScaleAnimation
import com.hlj.common.CONST
import com.hlj.dto.CityDto
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.hlj.utils.WeatherUtil
import kotlinx.android.synthetic.main.activity_city_forecast.*
import kotlinx.android.synthetic.main.layout_title.*
import kotlinx.android.synthetic.main.layout_weather1.view.*
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 城市天气预报
 */
class CityForecastActivity : BaseActivity(), OnClickListener, OnMarkerClickListener, OnMapClickListener, InfoWindowAdapter, OnCameraChangeListener {

    private var aMap: AMap? = null //高德地图
    private var zoom = 5.5f
    private var selectMarker: Marker? = null
    private val sdf1 = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("MM月dd日", Locale.CHINA)
    private val sdf3 = SimpleDateFormat("HH", Locale.CHINA)
    private val sdf4 = SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA)
    private val sdf5 = SimpleDateFormat("yyyy年MM月dd日 HH时", Locale.CHINA)
    private val cityList: MutableList<CityDto> = ArrayList() //市级
    private val districtList: MutableList<CityDto> = ArrayList() //县级
    private val cityMarkers: MutableList<Marker> = ArrayList()
    private val disMarkers: MutableList<Marker> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_forecast)
        initAmap(savedInstanceState)
        initWidget()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack!!.setOnClickListener(this)

        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (title != null) {
            tvTitle!!.text = title
        }

        okHttpList()
    }

    /**
     * 初始化高德地图
     */
    private fun initAmap(bundle: Bundle?) {
        mapView!!.onCreate(bundle)
        if (aMap == null) {
            aMap = mapView!!.map
        }
        val guizhouLatLng = LatLng(49.302915, 128.121040)
        aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(guizhouLatLng, zoom))
        aMap!!.uiSettings.isMyLocationButtonEnabled = false // 设置默认定位按钮是否显示
        aMap!!.uiSettings.isZoomControlsEnabled = false
        aMap!!.uiSettings.isRotateGesturesEnabled = false
        aMap!!.setOnMarkerClickListener(this)
        aMap!!.setOnMapClickListener(this)
        aMap!!.setInfoWindowAdapter(this)
        aMap!!.setOnCameraChangeListener(this)
        aMap!!.setOnMapLoadedListener {
            tvMapNumber.text = aMap!!.mapContentApprovalNumber
            CommonUtil.drawHLJJson(this, aMap)
        }
    }

    private fun okHttpList() {
        showDialog()
        Thread {
            val url = "http://xinjiangdecision.tianqi.cn:81/Home/work/forcast_citys"
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
                                cityList.clear()
                                districtList.clear()
                                val array = JSONArray(result)
                                for (i in 0 until array.length()) {
                                    val dto = CityDto()
                                    val itemObj = array.getJSONObject(i)
                                    if (!itemObj.isNull("name")) {
                                        dto.areaName = itemObj.getString("name")
                                    }
                                    if (!itemObj.isNull("level")) {
                                        dto.level = itemObj.getString("level")
                                    }
                                    if (!itemObj.isNull("areaid")) {
                                        dto.areaId = itemObj.getString("areaid")
                                    }
                                    if (!itemObj.isNull("lat")) {
                                        dto.lat = itemObj.getDouble("lat")
                                    }
                                    if (!itemObj.isNull("lon")) {
                                        dto.lng = itemObj.getDouble("lon")
                                    }
                                    if (TextUtils.equals(dto.level, "3")) {
                                        districtList.add(dto)
                                    } else {
                                        cityList.add(dto)
                                    }
                                }
                                removeMarkers()
                                for (i in cityList.indices) {
                                    val dto = cityList[i]
                                    getWeatherInfos(dto, cityMarkers, true)
                                }
                                for (i in districtList.indices) {
                                    val dto = districtList[i]
                                    getWeatherInfos(dto, disMarkers, false)
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
     * 获取多个城市天气信息
     */
    private fun getWeatherInfos(dto: CityDto, markers: MutableList<Marker>, isVisible: Boolean) {
        Thread {
            val url = String.format("https://hfapi.tianqi.cn/getweatherdata.php?area=%s&type=forecast&key=AErLsfoKBVCsU8hs", dto.areaId)
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

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
                                //获取明天预报信息
                                if (!obj.isNull("forecast")) {
                                    val forecast = obj.getJSONObject("forecast")
                                    //15天预报信息
                                    if (!forecast.isNull("24h")) {
                                        val `object` = forecast.getJSONObject("24h")
                                        if (!`object`.isNull(dto.areaId)) {
                                            val object1 = `object`.getJSONObject(dto.areaId)
                                            if (!object1.isNull("000")) {
                                                val time = object1.getString("000")
                                                tvTime.text = sdf5.format(sdf4.parse(time))+"预报"
                                            }
                                            if (!object1.isNull("1001001")) {
                                                val f1 = object1.getJSONArray("1001001")
                                                //预报内容
                                                val weeklyObj = f1.getJSONObject(1)

                                                //晚上
                                                val two = weeklyObj.getString("002")
                                                if (!TextUtils.isEmpty(two) && !TextUtils.equals(two, "?") && !TextUtils.equals(two, "null")) {
                                                    dto.lowPheCode = Integer.valueOf(two)
                                                }
                                                val four = weeklyObj.getString("004")
                                                if (!TextUtils.isEmpty(two) && !TextUtils.equals(two, "?") && !TextUtils.equals(two, "null")) {
                                                    dto.lowTemp = four
                                                }

                                                //白天
                                                val one = weeklyObj.getString("001")
                                                if (!TextUtils.isEmpty(one) && !TextUtils.equals(one, "?") && !TextUtils.equals(one, "null")) {
                                                    dto.highPheCode = Integer.valueOf(one)
                                                }
                                                val three = weeklyObj.getString("003")
                                                if (!TextUtils.isEmpty(three) && !TextUtils.equals(three, "?") && !TextUtils.equals(three, "null")) {
                                                    dto.highTemp = three
                                                }
                                                addMarker(dto, markers, isVisible)
                                            }
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

    private fun removeMarkers() {
        for (i in cityMarkers.indices) {
            val m = cityMarkers[i]
            markerColloseAnimation(m)
            m.remove()
        }
        cityMarkers.clear()
        for (i in disMarkers.indices) {
            val m = disMarkers[i]
            markerColloseAnimation(m)
            m.remove()
        }
        disMarkers.clear()
    }

    private fun addMarker(dto: CityDto, markers: MutableList<Marker>, isVisible: Boolean) {
        val options = MarkerOptions()
        options.title(dto.areaId)
        options.snippet(dto.areaName)
        options.anchor(0.5f, 0.5f)
        options.position(LatLng(dto.lat, dto.lng))
        options.icon(BitmapDescriptorFactory.fromView(getTextBitmap1(dto)))
        val marker = aMap!!.addMarker(options)
        if (marker != null) {
            marker.isVisible = isVisible
            markers.add(marker)
            markerExpandAnimation(marker)
        }
    }

    private fun markerExpandAnimation(marker: Marker) {
        val animation = ScaleAnimation(0f, 1f, 0f, 1f)
        animation.setInterpolator(LinearInterpolator())
        animation.setDuration(300)
        marker.setAnimation(animation)
        marker.startAnimation()
    }

    private fun markerColloseAnimation(marker: Marker) {
        val animation = ScaleAnimation(1f, 0f, 1f, 0f)
        animation.setInterpolator(LinearInterpolator())
        animation.setDuration(300)
        marker.setAnimation(animation)
        marker.startAnimation()
    }

    /**
     * 给marker添加文字
     * @return
     */
    private fun getTextBitmap1(dto: CityDto): View? {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_weather1, null) ?: return null
        view.tvName.text = dto.areaName
        var drawable = resources.getDrawable(R.drawable.phenomenon_drawable)
        try {
            val zao8 = sdf3.parse("06").time
            val wan8 = sdf3.parse("18").time
            val current = sdf3.parse(sdf3.format(Date())).time
            if (current in zao8 until wan8) {
                drawable = resources.getDrawable(R.drawable.phenomenon_drawable)
                drawable.level = dto.highPheCode
            } else {
                drawable = resources.getDrawable(R.drawable.phenomenon_drawable_night)
                drawable.level = dto.lowPheCode
            }
            view.ivPhe.background = drawable
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        view.tvTemp.text = dto.lowTemp + "~" + dto.highTemp + "℃"
        return view
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
        val view = inflater.inflate(R.layout.weather_marker_info, null)
        view.tvDetail.setOnClickListener {
            val data = CityDto()
            data.areaName = marker.snippet
            data.cityId = marker.title
            val intent = Intent(this, WeatherDetailActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("data", data)
            intent.putExtras(bundle)
            startActivity(intent)
        }
        view.tvContent.text = ""
        getWeatherInfo(marker.title, marker.snippet, view.tvContent, view.progressBar, view.tvDetail)
        return view
    }

    override fun getInfoWindow(arg0: Marker?): View? {
        return null
    }

    override fun onCameraChange(arg0: CameraPosition?) {}

    override fun onCameraChangeFinish(arg0: CameraPosition) {
        if (zoom == arg0.zoom) { //如果是地图缩放级别不变，并且点击就不做处理
            return
        }
        zoom = arg0.zoom
        if (arg0.zoom <= 8.0f) {
            for (i in cityMarkers.indices) {
                val m = cityMarkers[i]
                m.isVisible = true
                markerExpandAnimation(m)
            }
            for (i in disMarkers.indices) {
                val m = disMarkers[i]
                m.isVisible = false
                markerColloseAnimation(m)
            }
        }
        if (arg0.zoom > 8.0f) {
            for (i in cityMarkers.indices) {
                val m = cityMarkers[i]
                m.isVisible = true
                markerExpandAnimation(m)
            }
            for (i in disMarkers.indices) {
                val m = disMarkers[i]
                m.isVisible = true
                markerExpandAnimation(m)
            }
        }
    }

    /**
     * 获取天气数据
     */
    private fun getWeatherInfo(cityId: String, cityName: String, tvContent: TextView, progressBar: ProgressBar, tvDetail: TextView) {
        WeatherAPI.getWeather2(this, cityId, Language.ZH_CN, object : AsyncResponseHandler() {
            override fun onComplete(content: Weather) {
                super.onComplete(content)
                val result = content.toString()
                if (!TextUtils.isEmpty(result)) {
                    try {
                        val obj = JSONObject(result)
                        var factContent = cityName + "预报"

                        //实况信息
                        if (!obj.isNull("l")) {
                            val l = obj.getJSONObject("l")
                            if (!l.isNull("l7")) {
                                val time = l.getString("l7")
                                if (time != null) {
                                    factContent = "$factContent（$time）发布：\n"
                                }
                            }
                        }

                        //获取明天预报信息
                        if (!obj.isNull("f")) {
                            val f = obj.getJSONObject("f")
                            val f0 = f.getString("f0")
                            val f1 = f.getJSONArray("f1")
                            val i = 1
                            val weeklyObj = f1.getJSONObject(i)
                            val week = CommonUtil.getWeek(f0, i) //星期几
                            val date = CommonUtil.getDate(f0, i) //日期
                            try {
                                factContent = factContent + sdf2.format(sdf1.parse(date)) + "（" + week + "），"
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }
                            val lowPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fb"))))
                            val highPhe = getString(WeatherUtil.getWeatherId(Integer.valueOf(weeklyObj.getString("fa"))))
                            if (highPhe != null && lowPhe != null) {
                                var phe = lowPhe
                                phe = if (!TextUtils.equals(highPhe, lowPhe)) {
                                    lowPhe + "转" + highPhe
                                } else {
                                    lowPhe
                                }
                                factContent = "$factContent$phe，"
                            }
                            val lowTemp = weeklyObj.getString("fd")
                            val highTemp = weeklyObj.getString("fc")
                            if (lowTemp != null && highTemp != null) {
                                factContent = "$factContent$lowTemp ~ $highTemp℃，"
                            }
                            val lowDir = getString(WeatherUtil.getWindDirection(Integer.valueOf(weeklyObj.getString("ff"))))
                            val lowForce = WeatherUtil.getDayWindForce(Integer.valueOf(weeklyObj.getString("fh")))
                            val highDir = getString(WeatherUtil.getWindDirection(Integer.valueOf(weeklyObj.getString("fe"))))
                            val highForce = WeatherUtil.getDayWindForce(Integer.valueOf(weeklyObj.getString("fg")))
                            factContent = if (!TextUtils.equals(lowDir + lowForce, highDir + highForce)) {
                                factContent + lowDir + lowForce + "转" + highDir + highForce
                            } else {
                                factContent + lowDir + lowForce
                            }
                            tvContent.text = factContent
                            tvDetail.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onError(error: Throwable, content: String) {
                super.onError(error, content)
            }
        })
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> finish()
        }
    }

}
