package com.hlj.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import cn.com.weather.api.WeatherAPI
import cn.com.weather.listener.AsyncResponseHandler
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.hlj.activity.*
import com.hlj.adapter.TourAdapter
import com.hlj.adapter.WeeklyForecastAdapter
import com.hlj.common.CONST
import com.hlj.common.ColumnData
import com.hlj.dto.AgriDto
import com.hlj.dto.WeatherDto
import com.hlj.manager.XiangJiManager
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.hlj.utils.WeatherUtil
import com.hlj.view.WeeklyView
import kotlinx.android.synthetic.main.activity_rail.*
import kotlinx.android.synthetic.main.fragment_tour_forecast.*
import kotlinx.android.synthetic.main.layout_title.*
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

/**
 * 专业服务-铁路气象服务
 */
class RailActivity : BaseActivity(), AMapLocationListener, View.OnClickListener {

    private var mAdapter: TourAdapter? = null
    private val dataList: ArrayList<ColumnData> = ArrayList()
    private var lat = CONST.centerLat
    private var lng = CONST.centerLng
    private var weeklyAdapter: WeeklyForecastAdapter? = null
    private val weeklyList: MutableList<WeatherDto> = ArrayList()
    private val sdf1 = SimpleDateFormat("HH", Locale.CHINA)
    private val sdf3 = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
    private val sdf4 = SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA)
    private val sdf6 = SimpleDateFormat("HH:mm", Locale.CHINA)
    private val dayAqiList: ArrayList<WeatherDto> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rail)
        initWidget()
        initGridView()
        initListView()
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        tvChart.setOnClickListener(this)
        tvList.setOnClickListener(this)

        if (intent.hasExtra(CONST.ACTIVITY_NAME)) {
            val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
            if (title != null) {
                tvTitle.text = title
            }
        }

        if (CommonUtil.isLocationOpen(this)) {
            startLocation()
        } else {
            firstLoginDialog()
//            Toast.makeText(activity, "未开启定位，请选择城市", Toast.LENGTH_LONG).show()
//            val intent = Intent(activity, CityActivity::class.java)
//            intent.putExtra("selectCity", "selectCity")
//            startActivityForResult(intent, 1001)
            locationComplete()
        }
    }

    /**
     * 初始化listview
     */
    private fun initGridView() {
        dataList.clear()
        val data: AgriDto = intent.getParcelableExtra("data")
        for (i in 0 until data.child.size) {
            val dto = ColumnData()
            dto.columnId = data.child[i].columnId
            dto.id = data.child[i].id
            dto.icon = data.child[i].icon
            dto.icon2 = data.child[i].icon2
            dto.showType = data.child[i].showType
            dto.name = data.child[i].name
            dto.dataUrl = data.child[i].dataUrl
            dto.child = data.child[i].child
            dataList.add(dto)
        }
        mAdapter = TourAdapter(this, dataList)
        gridView!!.adapter = mAdapter
        gridView!!.onItemClickListener = AdapterView.OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = dataList[arg2]
            val intent: Intent
            when(dto.showType) {
                CONST.LOCAL -> {
                    when(dto.id) {
                        "9101","9201","9301" -> { //实况数据
                            intent = Intent(this, RailFactActivity::class.java)
                            intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
                            intent.putExtra(CONST.LOCAL_ID, dto.id)
                            startActivity(intent)
                        }
                        "9102","9202","9302" -> { //预报
                            intent = Intent(this, RailForeActivity::class.java)
                            intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
                            intent.putExtra(CONST.LOCAL_ID, dto.id)
                            startActivity(intent)
                        }
                        "9103","9203","9303" -> { //气象预警
                            intent = Intent(this, TourWarningActivity::class.java)
                            intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
                            intent.putExtra(CONST.LOCAL_ID, dto.id)
                            startActivity(intent)
                        }
                        "9104" -> { //风险预警
                            intent = Intent(this, RiskWarningActivity::class.java)
                            intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
                            intent.putExtra(CONST.LOCAL_ID, dto.id)
                            startActivity(intent)
                        }
                        "9105","9204","9304" -> { //气象专题
                            intent = Intent(this, TourKepuActivity::class.java)
                            intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
                            intent.putExtra(CONST.LOCAL_ID, dto.id)
                            val bundle = Bundle()
                            bundle.putParcelable("data", dto)
                            intent.putExtras(bundle)
                            startActivity(intent)
                        }
                        "9106","9205" -> { //灾情反馈
                            intent = Intent(this, DisasterActivity::class.java)
                            intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
                            intent.putExtra(CONST.LOCAL_ID, dto.id)
                            startActivity(intent)
                        }
                    }
                }
                CONST.URL -> {
                    intent = Intent(this, WebviewActivity::class.java)
                    intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
                    intent.putExtra(CONST.LOCAL_ID, dto.id)
                    intent.putExtra(CONST.WEB_URL, dto.dataUrl)
                    startActivity(intent)
                }
            }
        }
    }

    /**
     * 第一次登陆
     */
    private fun firstLoginDialog() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_location_prompt, null)
        val tvSure = view.findViewById<TextView>(R.id.tvSure)
        val dialog = Dialog(this, R.style.CustomProgressDialog)
        dialog.setContentView(view)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        tvSure.setOnClickListener { dialog.dismiss() }
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
        mLocationOption.isWifiActiveScan = true //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.isMockEnable = false //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.interval = 2000 //设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption) //给定位客户端对象设置定位参数
        mLocationClient.setLocationListener(this)
        mLocationClient.startLocation() //启动定位
    }

    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (amapLocation != null && amapLocation.errorCode == AMapLocation.LOCATION_SUCCESS) {
            lat = amapLocation.latitude
            lng = amapLocation.longitude
            val district = amapLocation.district
            val street = amapLocation.street + amapLocation.streetNum
            tvPosition!!.text = district+street
            ivAdd.setImageResource(R.drawable.icon_location_blue)
            okHttpCityId()
        }
    }

    private fun locationComplete() {
        tvPosition!!.text = "乌鲁木齐"
        ivAdd.setImageResource(R.drawable.icon_location_blue)
        okHttpCityId()
    }

    /**
     * 初始化listview
     */
    private fun initListView() {
        weeklyAdapter = WeeklyForecastAdapter(this, weeklyList, Color.BLACK)
        listView.adapter = weeklyAdapter
    }

    /**
     * 获取城市id
     */
    private fun okHttpCityId() {
        WeatherAPI.getGeo(this, lng.toString(), lat.toString(), object : AsyncResponseHandler() {
            override fun onComplete(content: JSONObject) {
                super.onComplete(content)
                if (!content.isNull("geo")) {
                    try {
                        val geoObj = content.getJSONObject("geo")
                        if (!geoObj.isNull("id")) {
                            val cityId = geoObj.getString("id")
                            if (!TextUtils.isEmpty(cityId)) {
                                getWeatherInfo(cityId)
                            }
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

    private fun getWeatherInfo(cityId: String) {
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
                    runOnUiThread {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)

                                //实况信息
                                if (!obj.isNull("observe")) {
                                    val observe = obj.getJSONObject("observe")
                                    if (!observe.isNull(cityId)) {
                                        val `object` = observe.getJSONObject(cityId)
                                        if (!`object`.isNull("1001002")) {
                                            val o = `object`.getJSONObject("1001002")
                                            if (!o.isNull("000")) {
                                                val time = o.getString("000")
                                                if (time != null) {
                                                    tvTime!!.text = time + getString(R.string.update)
                                                }
                                            }
                                            if (!o.isNull("001")) {
                                                val weatherCode = o.getString("001")
                                                if (!TextUtils.isEmpty(weatherCode) && !TextUtils.equals(weatherCode, "?") && !TextUtils.equals(weatherCode, "null")) {
                                                    try {
                                                        tvPhe!!.text = getString(WeatherUtil.getWeatherId(Integer.valueOf(weatherCode)))

                                                        val hour = sdf1.format(Date()).toInt()
                                                        val bitmap = if (hour in 5..17) {
                                                            WeatherUtil.getBitmap(this@RailActivity, weatherCode.toInt())
                                                        } else {
                                                            WeatherUtil.getNightBitmap(this@RailActivity, weatherCode.toInt())
                                                        }
                                                        if (bitmap != null) {
                                                            ivPhe.setImageBitmap(bitmap)
                                                        }
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                }
                                            }
                                            if (!o.isNull("002")) {
                                                val factTemp = o.getString("002")
                                                tvTemp!!.text = "$factTemp°"
                                            }
                                            if (!o.isNull("004")) {
                                                val windDir = o.getString("004")
                                                if (!TextUtils.isEmpty(windDir) && !TextUtils.equals(windDir, "?") && !TextUtils.equals(windDir, "null")) {
                                                    val dir = getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir)))
                                                    if (!o.isNull("003")) {
                                                        val windForce = o.getString("003")
                                                        if (!TextUtils.isEmpty(windForce) && !TextUtils.equals(windForce, "?") && !TextUtils.equals(windForce, "null")) {
                                                            val force = WeatherUtil.getFactWindForce(Integer.valueOf(windForce))
                                                            tvWind!!.text = "$dir $force"
                                                            when {
                                                                TextUtils.equals(dir, "北风") -> {
                                                                    ivWind!!.rotation = 0f
                                                                }
                                                                TextUtils.equals(dir, "东北风") -> {
                                                                    ivWind!!.rotation = 45f
                                                                }
                                                                TextUtils.equals(dir, "东风") -> {
                                                                    ivWind!!.rotation = 90f
                                                                }
                                                                TextUtils.equals(dir, "东南风") -> {
                                                                    ivWind!!.rotation = 135f
                                                                }
                                                                TextUtils.equals(dir, "南风") -> {
                                                                    ivWind!!.rotation = 180f
                                                                }
                                                                TextUtils.equals(dir, "西南风") -> {
                                                                    ivWind!!.rotation = 225f
                                                                }
                                                                TextUtils.equals(dir, "西风") -> {
                                                                    ivWind!!.rotation = 270f
                                                                }
                                                                TextUtils.equals(dir, "西北风") -> {
                                                                    ivWind!!.rotation = 315f
                                                                }
                                                            }
                                                            ivWind!!.setImageResource(R.drawable.icon_winddir_gray)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (!obj.isNull("rise")) {
                                    val rise = obj.getJSONObject("rise")
                                    if (!rise.isNull(cityId)) {
                                        val obj1 = rise.getJSONObject(cityId)
                                        if (!obj1.isNull("1001008")) {
                                            val riseArray = obj1.getJSONArray("1001008")
                                            if (riseArray.length() > 0) {
                                                val itemObj: JSONObject = riseArray.getJSONObject(0)
                                                if (!itemObj.isNull("001") && !itemObj.isNull("002")) {
                                                    val riseTime = itemObj.getString("001")
                                                    val setTime = itemObj.getString("002")
                                                    val diviTime = sdf6.parse(setTime).time - sdf6.parse(riseTime).time
                                                    val hour = diviTime / (1000 * 60 * 60)
                                                    val hourStr = if (hour < 10) {
                                                        "0$hour"
                                                    } else {
                                                        "$hour"
                                                    }
                                                    val minute = (diviTime - hour * 1000 * 60 * 60) / (1000 * 60)
                                                    val minuteStr = if (minute < 10) {
                                                        "0$minute"
                                                    } else {
                                                        "$minute"
                                                    }
                                                    tvRiseTime.text = "日出时间：$riseTime  日落时间：$setTime  日照时长：${hourStr}时${minuteStr}分"
                                                }
                                            }
                                        }
                                    }
                                }

                                //空气质量
                                if (!obj.isNull("air")) {
                                    val `object` = obj.getJSONObject("air")
                                    if (!`object`.isNull(cityId)) {
                                        val object1 = `object`.getJSONObject(cityId)
                                        if (!object1.isNull("2001006")) {
                                            val k = object1.getJSONObject("2001006")
                                            if (!k.isNull("002")) {
                                                val aqi = k.getString("002")
                                                if (!TextUtils.isEmpty(aqi) && !TextUtils.equals(aqi, "?") && !TextUtils.equals(aqi, "null")) {
                                                    tvAqiCount!!.text = aqi
                                                    try {
                                                        tvAqiCount!!.setBackgroundResource(WeatherUtil.getAqiIcon(Integer.valueOf(aqi)))
                                                        tvAqi.text = "空气质量 " + WeatherUtil.getAqi(this@RailActivity, Integer.valueOf(aqi))
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (!obj.isNull("forecast")) {
                                    val forecast = obj.getJSONObject("forecast")
                                    //15天预报信息
                                    if (!forecast.isNull("24h")) {
                                        weeklyList.clear()
                                        val `object` = forecast.getJSONObject("24h")
                                        if (!`object`.isNull(cityId)) {
                                            val object1 = `object`.getJSONObject(cityId)
                                            val f0 = object1.getString("000")
                                            if (!object1.isNull("1001001")) {
                                                val f1 = object1.getJSONArray("1001001")
                                                var length = f1.length()
                                                if (length >= 15) {
                                                    length = 15
                                                }
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

                                                    val hour = sdf1.format(Date()).toInt()
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

                                                okHttpDayAqi(f0)
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

    /**
     * 获取15天aqi
     */
    private fun okHttpDayAqi(f0: String) {
        Thread {
            val timestamp = Date().time
            val start1 = sdf3.format(sdf4.parse(f0))
            val end1 = sdf3.format(sdf3.parse(start1).time + 1000 * 60 * 60 * 24 * 15)
            val url = XiangJiManager.getXJSecretUrl2(lng, lat, start1, end1, timestamp)
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

                                    for (i in 0 until weeklyList.size) {
                                        val dto = weeklyList[i]
                                        if (dayAqiList.size > 0 && i < dayAqiList.size) {
                                            val aqiValue = dayAqiList[i].aqi
                                            if (!TextUtils.isEmpty(aqiValue)) {
                                                dto.aqi = aqiValue
                                            }
                                        }
                                    }

                                    var foreDate: Long = 0
                                    var currentDate: Long = 0
                                    try {
                                        val fTime = sdf3.format(sdf4.parse(f0))
                                        foreDate = sdf3.parse(fTime).time
                                        currentDate = sdf3.parse(sdf3.format(Date())).time
                                    } catch (e: ParseException) {
                                        e.printStackTrace()
                                    }
                                    //一周预报列表
                                    if (weeklyAdapter != null) {
                                        weeklyAdapter!!.foreDate = foreDate
                                        weeklyAdapter!!.currentDate = currentDate
                                        weeklyAdapter!!.notifyDataSetChanged()
                                    }

                                    //一周预报曲线
                                    val weeklyView = WeeklyView(this@RailActivity)
                                    weeklyView.setData(weeklyList, foreDate, currentDate, Color.BLACK)
                                    llContainerFifteen!!.removeAllViews()
                                    llContainerFifteen!!.addView(weeklyView, CommonUtil.widthPixels(this@RailActivity) * 3, CommonUtil.dip2px(this@RailActivity, 360f).toInt())
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

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.llBack -> finish()
            R.id.tvChart, R.id.tvList -> {
                if (listView!!.visibility == View.VISIBLE) {
                    tvChart.setTextColor(Color.WHITE)
                    tvList.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                    tvChart!!.setBackgroundResource(R.drawable.bg_chart_press)
                    tvList.setBackgroundResource(R.drawable.bg_list)
                    listView!!.visibility = View.GONE
                    hScrollView2!!.visibility = View.VISIBLE
                } else {
                    tvChart.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                    tvList.setTextColor(Color.WHITE)
                    tvChart!!.setBackgroundResource(R.drawable.bg_chart)
                    tvList.setBackgroundResource(R.drawable.bg_list_press)
                    listView!!.visibility = View.VISIBLE
                    hScrollView2!!.visibility = View.GONE
                }
            }
        }
    }

}
