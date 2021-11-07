package com.hlj.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.com.weather.api.WeatherAPI
import cn.com.weather.listener.AsyncResponseHandler
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.hlj.activity.*
import com.hlj.adapter.BaseViewPagerAdapter
import com.hlj.adapter.TourAdapter
import com.hlj.common.CONST
import com.hlj.common.ColumnData
import com.hlj.dto.NewsDto
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.hlj.utils.WeatherUtil
import kotlinx.android.synthetic.main.fragment_forecast.*
import kotlinx.android.synthetic.main.fragment_tour.*
import kotlinx.android.synthetic.main.fragment_tour.ivAdd
import kotlinx.android.synthetic.main.fragment_tour.ivWind
import kotlinx.android.synthetic.main.fragment_tour.tvAqi
import kotlinx.android.synthetic.main.fragment_tour.tvAqiCount
import kotlinx.android.synthetic.main.fragment_tour.tvPhe
import kotlinx.android.synthetic.main.fragment_tour.tvPosition
import kotlinx.android.synthetic.main.fragment_tour.tvRiseTime
import kotlinx.android.synthetic.main.fragment_tour.tvTemp
import kotlinx.android.synthetic.main.fragment_tour.tvTime
import kotlinx.android.synthetic.main.fragment_tour.tvWind
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
 * 旅游气象
 */
class TourFragment : BaseFragment(), AMapLocationListener {

    private var mReceiver: MyBroadCastReceiver? = null
    private var mAdapter1: TourAdapter? = null
    private val dataList1: ArrayList<ColumnData> = ArrayList()
    private var mAdapter2: TourAdapter? = null
    private val dataList2: ArrayList<ColumnData> = ArrayList()
    private val sdf1 = SimpleDateFormat("HH", Locale.CHINA)
    private val sdf6 = SimpleDateFormat("HH:mm", Locale.CHINA)

    //banner
    private val bannerFragments: ArrayList<Fragment> = ArrayList()
    private val bannerList: ArrayList<NewsDto> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_tour, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBroadCast()
    }

    private fun initBroadCast() {
        mReceiver = MyBroadCastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(arguments!!.getString(CONST.BROADCAST_ACTION))
        activity!!.registerReceiver(mReceiver, intentFilter)
    }

    private inner class MyBroadCastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            refresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mReceiver != null) {
            activity!!.unregisterReceiver(mReceiver)
        }
    }

    private fun refresh() {
        if (CommonUtil.isLocationOpen(activity)) {
            startLocation()
        } else {
            firstLoginDialog()
//            Toast.makeText(activity, "未开启定位，请选择城市", Toast.LENGTH_LONG).show()
//            val intent = Intent(activity, CityActivity::class.java)
//            intent.putExtra("selectCity", "selectCity")
//            startActivityForResult(intent, 1001)
            locationComplete()
        }
        okHttpBanner()

        tvSearch.setOnClickListener {
            startActivity(Intent(activity, TourSearchActivity::class.java))
        }

        dataList1.clear()
        dataList2.clear()
        val data: ColumnData = arguments!!.getParcelable("data")
        for (i in data.child.indices) {
            val child = data.child[i]
            if (TextUtils.equals(child.columnId, "803")) {
                dataList1.addAll(data.child[i].child)
            } else if (TextUtils.equals(child.columnId, "804")) {
                dataList2.addAll(data.child[i].child)
            }
        }
        initGridView1()
        initGridView2()
    }

    /**
     * 第一次登陆
     */
    private fun firstLoginDialog() {
        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_first_login, null)
        val tvSure = view.findViewById<TextView>(R.id.tvSure)
        val dialog = Dialog(activity, R.style.CustomProgressDialog)
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
        val mLocationClient = AMapLocationClient(activity) //初始化定位
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
            val district = amapLocation.district
            val street = amapLocation.street + amapLocation.streetNum
            tvPosition!!.text = district+street
            ivAdd.setImageResource(R.drawable.icon_location_blue)
            okHttpCityId(amapLocation.latitude,amapLocation.longitude)
        }
    }

    private fun locationComplete() {
        tvPosition!!.text = "乌鲁木齐"
        ivAdd.setImageResource(R.drawable.icon_location_blue)
        okHttpCityId(CONST.guizhouLatLng.latitude, CONST.guizhouLatLng.longitude)
    }

    private fun initViewPager() {
        bannerFragments.clear()
        viewGroup.removeAllViews()
        val size = bannerList.size
        if (size <= 1) {
            viewGroup.visibility = View.GONE
        }
        for (i in 0 until size) {
            val banner = bannerList[i]
            val fragment = TourBannerFragment()
            val bundle = Bundle()
            bundle.putParcelable("data", banner)
            fragment.arguments = bundle
            bannerFragments.add(fragment)

            val imageView = ImageView(activity)
            if (i == 0) {
                imageView.setBackgroundResource(R.drawable.banner_white)
            } else {
                imageView.setBackgroundResource(R.drawable.point_gray)
            }
            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            layoutParams.leftMargin = 10
            layoutParams.rightMargin = 10
            imageView.layoutParams = layoutParams
            viewGroup.addView(imageView)
        }
        viewPager.adapter = BaseViewPagerAdapter(childFragmentManager, bannerFragments)
        viewPager.setSlipping(true) //设置ViewPager是否可以滑动
        viewPager.offscreenPageLimit = bannerFragments.size //取消viewpager的预加载
        viewPager.setOnPageChangeListener(MyOnPageChangeListener())

        mHandler.sendEmptyMessageDelayed(AUTO_PLUS, PHOTO_CHANGE_TIME.toLong())
    }

    private val AUTO_PLUS = 1
    private val PHOTO_CHANGE_TIME = 2000 //定时变量
    private var index_plus = 0

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                AUTO_PLUS -> {
                    viewPager.currentItem = index_plus++ //收到消息后设置当前要显示的图片
                    sendEmptyMessageDelayed(AUTO_PLUS, PHOTO_CHANGE_TIME.toLong())
                    if (index_plus >= bannerFragments.size) {
                        index_plus = 0
                    }
                }
            }
        }
    }

    private inner class MyOnPageChangeListener : ViewPager.OnPageChangeListener {
        override fun onPageSelected(arg0: Int) {
            index_plus = arg0
            for (i in 0 until viewGroup.childCount) {
                val imageView = viewGroup.getChildAt(i) as ImageView
                if (i == arg0) {
                    imageView.setBackgroundResource(R.drawable.banner_white)
                } else {
                    imageView.setBackgroundResource(R.drawable.point_gray)
                }
            }
        }
        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    /**
     * 初始化listview
     */
    private fun initGridView1() {
        mAdapter1 = TourAdapter(activity, dataList1)
        gridView1!!.adapter = mAdapter1
        gridView1!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = dataList1[arg2]
            val intent: Intent
            if (TextUtils.equals(dto.showType, CONST.LOCAL)) {
                when {
                    TextUtils.equals(dto.id, "7001") -> { //城市天气
                        startActivity(Intent(activity, CityActivity::class.java))
                    }
                    TextUtils.equals(dto.id, "7002") -> { //景点天气
                        intent = Intent(activity, TourScenicActivity::class.java)
                        intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
                        startActivity(intent)
                    }
                    TextUtils.equals(dto.id, "7003") -> { //预警信息
                        intent = Intent(activity, TourWarningActivity::class.java)
                        intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
                        startActivity(intent)
                    }
                    TextUtils.equals(dto.id, "7004") -> { //新疆印象
                        intent = Intent(activity, TourImpressionActivity::class.java)
                        intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
                        startActivity(intent)
                    }
                    TextUtils.equals(dto.id, "7005") -> { //实景上报
                        intent = Intent(activity, DisasterActivity::class.java)
                        intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    /**
     * 初始化listview
     */
    private fun initGridView2() {
        mAdapter2 = TourAdapter(activity, dataList2)
        gridView2!!.adapter = mAdapter2
        gridView2!!.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = dataList2[arg2]
            val intent: Intent
            if (TextUtils.equals(dto.showType, CONST.LOCAL)) {
                when(dto.id) {
                    "7101" -> { //旅游路线
                        intent = Intent(activity, TourRouteActivity::class.java)
                        intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
                        startActivity(intent)
                    }
                    "7102" -> { //智慧观景台
                        intent = Intent(activity, TourObserveActivity::class.java)
                        intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
                        startActivity(intent)
                    }
                    "7103", "7104" -> { //科普、交通旅游专报
                        intent = Intent(activity, TourKepuActivity::class.java)
                        intent.putExtra(CONST.ACTIVITY_NAME, dto.name)
                        val bundle = Bundle()
                        bundle.putParcelable("data", dto)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    /**
     * 获取banner数据
     */
    private fun okHttpBanner() {
        Thread {
            val url = "http://xinjiangdecision.tianqi.cn:81/Home/api/get_travel_rotation"
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
                                bannerList.clear()
                                val array = JSONArray(result)
                                for (i in 0 until array.length()) {
                                    val dto = NewsDto()
                                    val itemObj = array.getJSONObject(i)
                                    if (!itemObj.isNull("name")) {
                                        dto.title = itemObj.getString("name")
                                    }
                                    if (!itemObj.isNull("img")) {
                                        dto.imgUrl = itemObj.getString("img")
                                    }
                                    if (!itemObj.isNull("url")) {
                                        dto.detailUrl = itemObj.getString("url")
                                    }
                                    bannerList.add(dto)
                                }
                                initViewPager()
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
     * 获取城市id
     */
    private fun okHttpCityId(lat: Double, lng: Double) {
        WeatherAPI.getGeo(activity, lng.toString(), lat.toString(), object : AsyncResponseHandler() {
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
                    activity!!.runOnUiThread {
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
                                                            WeatherUtil.getBitmap(activity, weatherCode.toInt())
                                                        } else {
                                                            WeatherUtil.getNightBitmap(activity, weatherCode.toInt())
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
                                                        tvAqi.text = "空气质量 " + WeatherUtil.getAqi(activity, Integer.valueOf(aqi))
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
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
                }
            })
        }.start()
    }
	
}
