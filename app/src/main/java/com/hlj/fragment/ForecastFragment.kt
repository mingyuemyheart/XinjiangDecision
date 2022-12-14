package com.hlj.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.os.*
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.View.OnClickListener
import android.widget.TextView
import android.widget.Toast
import cn.com.weather.api.WeatherAPI
import cn.com.weather.beans.Weather
import cn.com.weather.constants.Constants.Language
import cn.com.weather.listener.AsyncResponseHandler
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.hlj.activity.*
import com.hlj.adapter.WeeklyForecastAdapter
import com.hlj.common.CONST
import com.hlj.common.MyApplication
import com.hlj.dto.CityDto
import com.hlj.dto.MinuteFallDto
import com.hlj.dto.WarningDto
import com.hlj.dto.WeatherDto
import com.hlj.manager.CaiyunManager
import com.hlj.manager.DBManager
import com.hlj.manager.XiangJiManager
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.hlj.utils.WeatherUtil
import com.hlj.view.CubicView2
import com.hlj.view.MinuteFallView
import com.hlj.view.SunriseView
import com.hlj.view.WeeklyView
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechError
import com.iflytek.cloud.SpeechSynthesizer
import com.iflytek.cloud.SynthesizerListener
import kotlinx.android.synthetic.main.activity_weather_detail.*
import kotlinx.android.synthetic.main.fragment_forecast.*
import kotlinx.android.synthetic.main.fragment_forecast.clAudio
import kotlinx.android.synthetic.main.fragment_forecast.clHour
import kotlinx.android.synthetic.main.fragment_forecast.clMain
import kotlinx.android.synthetic.main.fragment_forecast.clMinute
import kotlinx.android.synthetic.main.fragment_forecast.hScrollView
import kotlinx.android.synthetic.main.fragment_forecast.hScrollView2
import kotlinx.android.synthetic.main.fragment_forecast.hsTime
import kotlinx.android.synthetic.main.fragment_forecast.ivAudio
import kotlinx.android.synthetic.main.fragment_forecast.ivClose
import kotlinx.android.synthetic.main.fragment_forecast.ivClose2
import kotlinx.android.synthetic.main.fragment_forecast.ivPlay2
import kotlinx.android.synthetic.main.fragment_forecast.ivWind
import kotlinx.android.synthetic.main.fragment_forecast.listView
import kotlinx.android.synthetic.main.fragment_forecast.llContainer3
import kotlinx.android.synthetic.main.fragment_forecast.llContainerFifteen
import kotlinx.android.synthetic.main.fragment_forecast.llContainerHour
import kotlinx.android.synthetic.main.fragment_forecast.llContainerRain
import kotlinx.android.synthetic.main.fragment_forecast.llContainerTime
import kotlinx.android.synthetic.main.fragment_forecast.mapView
import kotlinx.android.synthetic.main.fragment_forecast.refreshLayout
import kotlinx.android.synthetic.main.fragment_forecast.tvAqi
import kotlinx.android.synthetic.main.fragment_forecast.tvAqi1
import kotlinx.android.synthetic.main.fragment_forecast.tvAqi2
import kotlinx.android.synthetic.main.fragment_forecast.tvAqiCount
import kotlinx.android.synthetic.main.fragment_forecast.tvBody
import kotlinx.android.synthetic.main.fragment_forecast.tvChart
import kotlinx.android.synthetic.main.fragment_forecast.tvCityWarning
import kotlinx.android.synthetic.main.fragment_forecast.tvDay1
import kotlinx.android.synthetic.main.fragment_forecast.tvDay2
import kotlinx.android.synthetic.main.fragment_forecast.tvDisWarning
import kotlinx.android.synthetic.main.fragment_forecast.tvFact
import kotlinx.android.synthetic.main.fragment_forecast.tvList
import kotlinx.android.synthetic.main.fragment_forecast.tvPhe
import kotlinx.android.synthetic.main.fragment_forecast.tvPhe1
import kotlinx.android.synthetic.main.fragment_forecast.tvPhe2
import kotlinx.android.synthetic.main.fragment_forecast.tvPosition
import kotlinx.android.synthetic.main.fragment_forecast.tvProWarning
import kotlinx.android.synthetic.main.fragment_forecast.tvRain
import kotlinx.android.synthetic.main.fragment_forecast.tvRiseTime
import kotlinx.android.synthetic.main.fragment_forecast.tvTemp
import kotlinx.android.synthetic.main.fragment_forecast.tvTemp1
import kotlinx.android.synthetic.main.fragment_forecast.tvTemp2
import kotlinx.android.synthetic.main.fragment_forecast.tvTime
import kotlinx.android.synthetic.main.fragment_forecast.tvWind
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
 * ????????????
 */
class ForecastFragment : BaseFragment(), OnClickListener, AMapLocationListener, CaiyunManager.RadarListener{

    private var isFusion = false
    private var lat = CONST.centerLat
    private var lng = CONST.centerLng
    private var mAdapter: WeeklyForecastAdapter? = null
    private val weeklyList: MutableList<WeatherDto> = ArrayList()
    private val sdf1 = SimpleDateFormat("HH", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("yyyy???MM???dd??? HH???mm???", Locale.CHINA)
    private val sdf3 = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
    private val sdf4 = SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA)
    private val sdf5 = SimpleDateFormat("yyyyMMddHH", Locale.CHINA)
    private val sdf6 = SimpleDateFormat("HH:mm", Locale.CHINA)
    private var hour = 0
    private val disWarnings: MutableList<WarningDto?> = ArrayList()
    private val cityWarnings: MutableList<WarningDto?> = ArrayList()
    private val proWarnings: MutableList<WarningDto?> = ArrayList()
    private val dayAqiList: ArrayList<WeatherDto> = ArrayList()

    //????????????
    private var mTts : SpeechSynthesizer? = null// ??????????????????
    private var voicer = "xiaoyan"// ???????????????
    private var mPercentForBuffering = 0// ????????????
    private var mPercentForPlaying = 0// ????????????
    private var mEngineType = SpeechConstant.TYPE_CLOUD// ????????????
    private var mToast : Toast? = null
    private var weatherText = ""
    private var cityName : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_forecast, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMap(savedInstanceState)
        refresh()
    }

    private fun refresh() {
        initRefreshLayout()
        initWidget()
        initListView()
    }

    /**
     * ???????????????????????????
     */
    private fun initRefreshLayout() {
        refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4)
        refreshLayout.setProgressViewEndTarget(true, 300)
        refreshLayout.isRefreshing = true
        refreshLayout.setOnRefreshListener { refresh() }
    }

    /**
     * ???????????????
     */
    private fun initWidget() {
        ivMap.setOnClickListener(this)
        ivRouteWeather.setOnClickListener(this)
        tvPosition.setOnClickListener(this)
        tvFact.setOnClickListener(this)
        tvBody.setOnClickListener(this)
        tvChart.setOnClickListener(this)
        tvList.setOnClickListener(this)
        tvAqiCount.setOnClickListener(this)
        tvAqi.setOnClickListener(this)
        clMinute.setOnClickListener(this)
        tvDisWarning!!.setOnClickListener(this)
        tvCityWarning!!.setOnClickListener(this)
        tvProWarning!!.setOnClickListener(this)
        tvRain.setOnClickListener(this)
        clHour.setOnClickListener(this)
        tvDivPolicy.setOnClickListener(this)
        tvInfo.setOnClickListener(this)
        ivClimate.setOnClickListener(this)
        clVideo.setOnClickListener(this)
        clVideo.visibility = View.GONE
        clAudio.setOnClickListener(this)
        ivPlay2!!.setOnClickListener(this)
        hour = sdf1.format(Date()).toInt()

        if (CommonUtil.isLocationOpen(activity)) {
            startLocation()
        } else {
            refreshLayout.isRefreshing = false
            dialogLocationPrompt()
//            Toast.makeText(activity, "?????????????????????????????????", Toast.LENGTH_LONG).show()
//            val intent = Intent(activity, CityActivity::class.java)
//            intent.putExtra("selectCity", "selectCity")
//            startActivityForResult(intent, 1001)
            locationComplete()
        }
        initSpeech()
    }

    /**
     * ????????????
     */
    private fun dialogLocationPrompt() {
        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_location_prompt, null)
        val tvSure = view.findViewById<TextView>(R.id.tvSure)
        val dialog = Dialog(activity, R.style.CustomProgressDialog)
        dialog.setContentView(view)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        tvSure.setOnClickListener { dialog.dismiss() }
    }

    /**
     * ????????????
     */
    private fun startLocation() {
        val mLocationOption = AMapLocationClientOption() //?????????????????????
        val mLocationClient = AMapLocationClient(activity) //???????????????
        mLocationOption.locationMode = AMapLocationMode.Hight_Accuracy //???????????????????????????????????????Battery_Saving?????????????????????Device_Sensors??????????????????
        mLocationOption.isNeedAddress = true //????????????????????????????????????????????????????????????
        mLocationOption.isOnceLocation = true //???????????????????????????,?????????false
        mLocationOption.isWifiActiveScan = true //????????????????????????WIFI????????????????????????
        mLocationOption.isMockEnable = false //??????????????????????????????,?????????false????????????????????????
        mLocationOption.interval = 2000 //??????????????????,????????????,?????????2000ms
        mLocationClient.setLocationOption(mLocationOption) //??????????????????????????????????????????
        mLocationClient.setLocationListener(this)
        mLocationClient.startLocation() //????????????
    }

    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (amapLocation != null && amapLocation.errorCode == AMapLocation.LOCATION_SUCCESS) {
            val district = amapLocation.district
            val street = amapLocation.street + amapLocation.streetNum
            cityName = district+street
            tvPosition!!.text = cityName
            lat = amapLocation.latitude
            lng = amapLocation.longitude
            addMarkerToMap()
            okHttpHourRain()
            getGeo(isFusion)
        }
    }

    private fun locationComplete() {
        cityName = "????????????"
        tvPosition!!.text = cityName
        addMarkerToMap()
        okHttpHourRain()
        getGeo(isFusion)
    }

    private fun addMarkerToMap() {
        val options = MarkerOptions()
        options.position(LatLng(lat, lng))
        options.anchor(0.5f, 0.5f)
        val bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(resources, R.drawable.iv_map_location),
                CommonUtil.dip2px(activity, 15f).toInt(), CommonUtil.dip2px(activity, 15f).toInt())
        if (bitmap != null) {
            options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        } else {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.iv_map_location))
        }
        aMap!!.addMarker(options)
        aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 6.0f))
    }

    private fun initSpeech() {
        // ?????????????????????
        mTts = SpeechSynthesizer.createSynthesizer(activity) { code ->

        }
        mToast = Toast.makeText(activity,"",Toast.LENGTH_SHORT)

        // ????????????
        mTts!!.setParameter(SpeechConstant.PARAMS, null)
        // ????????????????????????????????????
        if(mEngineType == SpeechConstant.TYPE_CLOUD) {
            mTts!!.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD)
            // ???????????????????????????
            mTts!!.setParameter(SpeechConstant.VOICE_NAME, voicer)
            //??????????????????
            mTts!!.setParameter(SpeechConstant.SPEED, "50")
            //??????????????????
            mTts!!.setParameter(SpeechConstant.PITCH, "50")
            //??????????????????
            mTts!!.setParameter(SpeechConstant.VOLUME, "50")
        }else {
            mTts!!.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL)
            // ??????????????????????????? voicer???????????????????????????????????????????????????
            mTts!!.setParameter(SpeechConstant.VOICE_NAME, "")
        }
        //??????????????????????????????
        mTts!!.setParameter(SpeechConstant.STREAM_TYPE, "3")
        // ??????????????????????????????????????????????????????true
        mTts!!.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true")

        // ???????????????????????????????????????????????????pcm???wav??????????????????sd????????????WRITE_EXTERNAL_STORAGE??????
        // ??????AUDIO_FORMAT??????????????????????????????????????????
        mTts!!.setParameter(SpeechConstant.AUDIO_FORMAT, "wav")
        mTts!!.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory().toString()+"/msc/weather.wav")
    }

    override fun onDestroy() {
        super.onDestroy()
        mTts!!.stopSpeaking()
        mTts!!.destroy()

        if (mRadarManager != null) {
            mRadarManager!!.onDestory()
        }
        if (mRadarThread != null) {
            mRadarThread!!.cancel()
            mRadarThread = null
        }
    }

    private var aMap: AMap? = null
    private fun initMap(bundle: Bundle?) {
        mapView.onCreate(bundle)
        if (aMap == null) {
            aMap = mapView.map
        }
        aMap!!.moveCamera(CameraUpdateFactory.zoomTo(8.0f))
        aMap!!.uiSettings.isZoomControlsEnabled = false
        aMap!!.uiSettings.isRotateGesturesEnabled = false
        aMap!!.setOnMapLoadedListener {
            mRadarManager = CaiyunManager(activity)
            okHttpMinuteImage()
            CommonUtil.drawHLJJson(activity, aMap)
        }
        aMap!!.setOnMapTouchListener { arg0 ->
            if (scrollView != null) {
                if (arg0.action == MotionEvent.ACTION_UP) {
                    scrollView.requestDisallowInterceptTouchEvent(false)
                } else {
                    scrollView.requestDisallowInterceptTouchEvent(true)
                }
            }
        }
    }

    private val dataList: ArrayList<MinuteFallDto> = ArrayList()
    private val images: ArrayList<MinuteFallDto> = ArrayList()
    private var mOverlay: GroundOverlay? = null
    private var mRadarManager: CaiyunManager? = null
    private var mRadarThread: RadarThread? = null
    private val HANDLER_SHOW_RADAR = 1
    private fun okHttpMinuteImage() {
        Thread(Runnable {
            val url = "http://api.tianqi.cn:8070/v1/img.py"
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
                                if (!obj.isNull("status")) {
                                    if (obj.getString("status") == "ok") {
                                        if (!obj.isNull("radar_img")) {
                                            dataList.clear()
                                            val array = JSONArray(obj.getString("radar_img"))
                                            for (i in 0 until array.length()) {
                                                val array0 = array.getJSONArray(i)
                                                val dto = MinuteFallDto()
                                                dto.imgUrl = array0.optString(0)
                                                dto.time = array0.optLong(1)
                                                val itemArray = array0.getJSONArray(2)
                                                dto.p1 = itemArray.optDouble(0)
                                                dto.p2 = itemArray.optDouble(1)
                                                dto.p3 = itemArray.optDouble(2)
                                                dto.p4 = itemArray.optDouble(3)
                                                dataList.add(dto)
                                            }
                                            if (dataList.size > 0) {
                                                startDownLoadImgs(dataList)
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
        }).start()
    }

    private fun startDownLoadImgs(list: ArrayList<MinuteFallDto>) {
        if (mRadarThread != null) {
            mRadarThread!!.cancel()
            mRadarThread = null
        }
        mRadarManager!!.loadImagesAsyn(list, this)
    }

    override fun onResult(result: Int, images: ArrayList<MinuteFallDto>) {
        activity!!.runOnUiThread {
            llContainerTime.removeAllViews()
            for (i in 0 until images.size) {
                val tv = TextView(activity)
                tv.setPadding(10, 10, 10, 10)
                tv.setBackgroundColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
                tv.gravity = Gravity.CENTER
                tv.setTextColor(Color.WHITE)
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                val dto = images[i]
                val value = dto.time.toString() + "000"
                val date = Date(value.toLong())
                tv.text = sdf6.format(date)
                llContainerTime.addView(tv)
            }
        }
        if (result == CaiyunManager.RadarListener.RESULT_SUCCESSED) {
            this.images.clear()
            this.images.addAll(images)

            //????????????????????????????????????????????????
            val radar = images[images.size - 1]
            val message = mHandler.obtainMessage()
            message.what = HANDLER_SHOW_RADAR
            message.obj = radar
            message.arg1 = 100
            message.arg2 = 100
            mHandler.sendMessage(message)
        }
    }

    override fun onProgress(url: String?, progress: Int) {}

    private fun showRadar(bitmap: Bitmap, p1: Double, p2: Double, p3: Double, p4: Double) {
        val fromView = BitmapDescriptorFactory.fromBitmap(bitmap)
        val bounds = LatLngBounds.Builder()
                .include(LatLng(p3, p2))
                .include(LatLng(p1, p4))
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

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                HANDLER_SHOW_RADAR -> if (msg.obj != null) {
                    val dto = msg.obj as MinuteFallDto
                    if (dto.getPath() != null) {
                        val bitmap = BitmapFactory.decodeFile(dto.getPath())
                        if (bitmap != null) {
                            showRadar(bitmap, dto.p1, dto.p2, dto.p3, dto.p4)
                        }
                    }
                    changeProgress(dto.time, msg.arg2, msg.arg1)
                }
            }
        }
    }

    private val STATE_NONE = 0
    private val STATE_PLAYING = 1
    private val STATE_PAUSE = 2
    private val STATE_CANCEL = 3
    private inner class RadarThread(private val images: ArrayList<MinuteFallDto>) : Thread() {
        var currentState: Int
        private var index: Int
        private val count: Int
        private var isTracking = false

        init {
            count = images.size
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
                    sleep(200)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }

        private fun sendRadar() {
            if (index >= count || index < 0) {
                index = 0
            } else {
                val radar = images[index]
                val message: Message = mHandler.obtainMessage()
                message.what = HANDLER_SHOW_RADAR
                message.obj = radar
                message.arg1 = count - 1
                message.arg2 = index++
                mHandler.sendMessage(message)
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

    private fun changeProgress(time: Long, progress: Int, max: Int) {
        val value = time.toString() + "000"
        val date = Date(value.toLong())
        val text = sdf6.format(date)

        for (i in 0 until llContainerTime.childCount) {
            val tv = llContainerTime.getChildAt(i) as TextView
            if (TextUtils.equals(text, tv.text)) {
                tv.setBackgroundColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
            } else {
                tv.setBackgroundColor(0xff9DC7FA.toInt())
            }
        }
    }

    private fun getGeo(isFusion: Boolean) {
        refreshLayout.isRefreshing = true
        WeatherAPI.getGeo(activity, lng.toString(), lat.toString(), object : AsyncResponseHandler() {
            override fun onComplete(content: JSONObject) {
                super.onComplete(content)
                if (!content.isNull("geo")) {
                    try {
                        val geoObj = content.getJSONObject("geo")
                        if (!geoObj.isNull("id")) {
                            val cityId = geoObj.getString("id")
                            if (!TextUtils.isEmpty(cityId)) {
                                getWeatherInfo(cityId, isFusion)
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

    private fun getWeatherInfo(cityId: String, isFusion: Boolean) {
        Thread {
            var url = "https://hfapi.tianqi.cn/getweatherdata.php?area=$cityId&type=forecast|observe|alarm|air|rise&key=AErLsfoKBVCsU8hs"
            if (isFusion) {
                url = "http://decision-admin.tianqi.cn/Home/Work2019/hlj_fusion_weather?lat=$lat&lon=$lng&type=forecast|observe&key=AErLsfoKBVCsU8hs"
            }
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    activity!!.runOnUiThread {
                        refreshLayout.isRefreshing = false
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)

                                //????????????
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
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                }
                                            }
                                            if (!o.isNull("002")) {
                                                val factTemp = o.getString("002")
                                                tvTemp!!.text = "$factTemp??"
                                                tvFact.tag = "$factTemp"
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
                                                                TextUtils.equals(dir, "??????") -> {
                                                                    ivWind!!.rotation = 0f
                                                                }
                                                                TextUtils.equals(dir, "?????????") -> {
                                                                    ivWind!!.rotation = 45f
                                                                }
                                                                TextUtils.equals(dir, "??????") -> {
                                                                    ivWind!!.rotation = 90f
                                                                }
                                                                TextUtils.equals(dir, "?????????") -> {
                                                                    ivWind!!.rotation = 135f
                                                                }
                                                                TextUtils.equals(dir, "??????") -> {
                                                                    ivWind!!.rotation = 180f
                                                                }
                                                                TextUtils.equals(dir, "?????????") -> {
                                                                    ivWind!!.rotation = 225f
                                                                }
                                                                TextUtils.equals(dir, "??????") -> {
                                                                    ivWind!!.rotation = 270f
                                                                }
                                                                TextUtils.equals(dir, "?????????") -> {
                                                                    ivWind!!.rotation = 315f
                                                                }
                                                            }
                                                            ivWind!!.setImageResource(R.drawable.iv_winddir)
                                                        }
                                                    }
                                                }
                                            }

                                            okHttpBody(cityId, o.getString("002"), o.getString("005"), o.getString("012"))
                                        }
                                    }
                                }

                                //????????????
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
                                                    tvRiseTime.text = "???????????????$riseTime\n???????????????$setTime\n???????????????${hourStr}???${minuteStr}???"

                                                    //????????????
                                                    val sunriseView = SunriseView(activity)
                                                    sunriseView.setData(riseTime, setTime)
                                                    llContainer3.removeAllViews()
                                                    llContainer3.addView(sunriseView, CommonUtil.widthPixels(activity), CommonUtil.dip2px(activity, 150f).toInt())
                                                }
                                            }
                                        }
                                    }
                                }

                                //????????????
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
                                                        val value = Integer.valueOf(aqi)
                                                        tvAqiCount!!.setBackgroundResource(WeatherUtil.getAqiIcon(value))
                                                        tvAqi.text = "???????????? " + WeatherUtil.getAqi(activity, value)
                                                        if (value <= 300) {
                                                            tvAqiCount.setTextColor(Color.BLACK)
                                                        } else {
                                                            tvAqiCount.setTextColor(Color.WHITE)
                                                        }
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

                                    //?????????????????????
                                    if (!forecast.isNull("1h")) {
                                        val `object` = forecast.getJSONObject("1h")
                                        if (!`object`.isNull(cityId)) {
                                            val object1 = `object`.getJSONObject(cityId)
                                            if (!object1.isNull("1001001")) {
                                                val array = object1.getJSONArray("1001001")
                                                val hourlyList: MutableList<WeatherDto> = ArrayList()
                                                var length = array.length()
                                                if (length >= 24) {
                                                    length = 24
                                                }
                                                for (i in 0 until length) {
                                                    val itemObj = array.getJSONObject(i)
                                                    val dto = WeatherDto()
                                                    dto.hourlyTime = itemObj.getString("000")
                                                    try {
                                                        val one = itemObj.getString("001")
                                                        if (!TextUtils.isEmpty(one) && !TextUtils.equals(one, "?") && !TextUtils.equals(one, "null")) {
                                                            dto.hourlyCode = Integer.valueOf(one)
                                                        }
                                                        val two = itemObj.getString("002")
                                                        if (!TextUtils.isEmpty(two) && !TextUtils.equals(two, "?") && !TextUtils.equals(two, "null")) {
                                                            dto.hourlyTemp = Integer.valueOf(two)
                                                        }
                                                        val four = itemObj.getString("004")
                                                        if (!TextUtils.isEmpty(four) && !TextUtils.equals(four, "?") && !TextUtils.equals(four, "null")) {
                                                            dto.hourlyWindDirCode = Integer.valueOf(four)
                                                        }
                                                        val three = itemObj.getString("003")
                                                        if (!TextUtils.isEmpty(three) && !TextUtils.equals(three, "?") && !TextUtils.equals(three, "null")) {
                                                            dto.hourlyWindForceCode = Integer.valueOf(three)
                                                        }
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                    hourlyList.add(dto)
                                                }

                                                //?????????????????????
                                                val cubicView = CubicView2(activity)
                                                cubicView.setData(hourlyList)
                                                llContainerHour!!.removeAllViews()
                                                llContainerHour!!.addView(cubicView, CommonUtil.widthPixels(activity)*4, CommonUtil.dip2px(activity, 300f).toInt())
                                            }
                                        }
                                    }

                                    //15???????????????
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

                                                    //????????????
                                                    dto.date = CommonUtil.getDate(f0, i) //??????
                                                    dto.week = CommonUtil.getWeek(f0, i) //?????????

                                                    //????????????
                                                    val weeklyObj = f1.getJSONObject(i)

                                                    //??????
                                                    val two = weeklyObj.getString("002")
                                                    if (!TextUtils.isEmpty(two) && !TextUtils.equals(two, "?") && !TextUtils.equals(two, "null")) {
                                                        dto.lowPheCode = Integer.valueOf(two)
                                                        dto.lowPhe = getString(WeatherUtil.getWeatherId(dto.lowPheCode))
                                                    }
                                                    val four = weeklyObj.getString("004")
                                                    if (!TextUtils.isEmpty(two) && !TextUtils.equals(two, "?") && !TextUtils.equals(two, "null")) {
                                                        dto.lowTemp = Integer.valueOf(four)
                                                    }

                                                    //??????
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
                                                    if (i == 0) {
                                                        weatherText = ""
                                                        var pheText: String? = null
                                                        var temperatureText: String? = null
                                                        var windDirText: String? = null
                                                        var windForceText: String? = null
                                                        pheText = if (dto.lowPheCode == dto.highPheCode) {
                                                            getString(WeatherUtil.getWeatherId(dto.lowPheCode))
                                                        } else {
                                                            getString(WeatherUtil.getWeatherId(dto.highPheCode)) + "???" + getString(WeatherUtil.getWeatherId(dto.lowPheCode))
                                                        }
                                                        temperatureText = "????????????" + dto.highTemp + "????????????" + "????????????" + dto.lowTemp + "?????????"
                                                        windDirText = getString(WeatherUtil.getWindDirection(dto.windDir))
                                                        windForceText = WeatherUtil.getDayWindForce(dto.windForce)
                                                        weatherText = "?????????????????????????????????" + pheText + "???" + temperatureText + "???" + windDirText + windForceText


                                                        tvDay1!!.text = "??????"
                                                        var drawable: Drawable
                                                        if (hour in 5..17) {
                                                            drawable = resources.getDrawable(R.drawable.phenomenon_drawable)
                                                            drawable.level = dto.highPheCode
                                                            tvPhe1!!.text = getString(WeatherUtil.getWeatherId(dto.highPheCode))
                                                        } else {
                                                            drawable = resources.getDrawable(R.drawable.phenomenon_drawable_night)
                                                            drawable.level = dto.lowPheCode
                                                            tvPhe1!!.text = getString(WeatherUtil.getWeatherId(dto.lowPheCode))
                                                        }
                                                        tvTemp1!!.text = dto.lowTemp.toString() + "/" + dto.highTemp + "???"
                                                    }
                                                    if (i == 1) {
                                                        tvDay2!!.text = "??????"
                                                        var drawable: Drawable
                                                        if (hour in 5..17) {
                                                            drawable = resources.getDrawable(R.drawable.phenomenon_drawable)
                                                            drawable.level = dto.highPheCode
                                                            tvPhe2!!.text = getString(WeatherUtil.getWeatherId(dto.highPheCode))
                                                        } else {
                                                            drawable = resources.getDrawable(R.drawable.phenomenon_drawable_night)
                                                            drawable.level = dto.lowPheCode
                                                            tvPhe2!!.text = getString(WeatherUtil.getWeatherId(dto.lowPheCode))
                                                        }
                                                        tvTemp2!!.text = dto.lowTemp.toString() + "/" + dto.highTemp + "???"
                                                    }
                                                }

                                                okHttpDayAqi(f0)
                                            }
                                        }
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                            clMain!!.visibility = View.VISIBLE
                            refreshLayout!!.isRefreshing = false
                        }
                        activity!!.runOnUiThread { //??????????????????
                            val warningId = queryWarningIdByCityId(cityId)
                            if (!TextUtils.isEmpty(warningId)) {
                                setPushTags(warningId)
                                okHttpWarning("http://decision-admin.tianqi.cn/Home/extra/getwarns?order=0&areaid=" + warningId!!.substring(0, 2), warningId)
                            }
                        }
                    }
                }
            })
        }.start()
    }

    /**
     * ??????15???aqi
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
                    if (!isAdded) {
                        return
                    }
                    val result = response.body!!.string()
                    activity!!.runOnUiThread {
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

                                        if (i == 0) {
                                            if (!TextUtils.isEmpty(dto.aqi) && !TextUtils.equals(dto.aqi, "?") && !TextUtils.equals(dto.aqi, "null")) {
                                                val value: Int = dto.aqi.toInt()
                                                if (value <= 150) {
                                                    tvAqi1!!.setTextColor(Color.BLACK)
                                                } else {
                                                    tvAqi1!!.setTextColor(Color.WHITE)
                                                }
                                                tvAqi1.text = CommonUtil.getAqiDes(activity, value)
                                                tvAqi1.setBackgroundResource(CommonUtil.getCornerBackground(value))
                                            }
                                        }
                                        if (i == 1) {
                                            if (dayAqiList.size > 1) {
                                                if (!TextUtils.isEmpty(dto.aqi) && !TextUtils.equals(dto.aqi, "?") && !TextUtils.equals(dto.aqi, "null")) {
                                                    val value: Int = dto.aqi.toInt()
                                                    if (value <= 150) {
                                                        tvAqi2!!.setTextColor(Color.BLACK)
                                                    } else {
                                                        tvAqi2!!.setTextColor(Color.WHITE)
                                                    }
                                                    tvAqi2.text = CommonUtil.getAqiDes(activity, value)
                                                    tvAqi2.setBackgroundResource(CommonUtil.getCornerBackground(value))
                                                }
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
                                    //??????????????????
                                    if (mAdapter != null) {
                                        mAdapter!!.foreDate = foreDate
                                        mAdapter!!.currentDate = currentDate
                                        mAdapter!!.notifyDataSetChanged()
                                    }

                                    //??????????????????
                                    val weeklyView = WeeklyView(activity)
                                    weeklyView.setData(weeklyList, foreDate, currentDate, Color.WHITE)
                                    llContainerFifteen!!.removeAllViews()
                                    llContainerFifteen!!.addView(weeklyView, CommonUtil.widthPixels(activity) * 3, CommonUtil.dip2px(activity, 360f).toInt())
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

    private fun okHttpBody(cityId: String, l1: String, l2: String, l11: String) {
        Thread {
            val url = "http://decision-admin.tianqi.cn/home/work2019/getBodyTem?cityId=$cityId&l1=$l1&l2=$l2&l11=$l11"
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
                                //????????????
                                if (!obj.isNull("l12")) {
                                    val bodyTemp = obj.getString("l12")
                                    tvBody.tag = "$bodyTemp"
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
     * ??????umeng?????????tags
     * @param warningId
     */
    private fun setPushTags(warningId: String?) {
        if (activity == null) {
            return
        }
        var tags = warningId
        val sharedPreferences = activity!!.getSharedPreferences("RESERVE_CITY", Context.MODE_PRIVATE)
        val cityInfo = sharedPreferences.getString("cityInfo", "")
        if (!TextUtils.isEmpty(cityInfo)) {
            tags = "$tags,"
            if (cityInfo.contains(";")) {
                val array = cityInfo.split(";").toTypedArray()
                for (i in array.indices) {
                    val item = array[i]
                    if (!TextUtils.isEmpty(item) && item.contains(",")) {
                        val itemArray = item.split(",").toTypedArray()
                        tags = if (i == array.size - 1) {
                            tags + itemArray[2]
                        } else {
                            tags + itemArray[2] + ","
                        }
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(tags)) {
            MyApplication.resetTags(tags)
        }
    }

    /**
     * ????????????id
     */
    private fun queryWarningIdByCityId(cityId: String): String? {
        val dbManager = DBManager(activity)
        dbManager.openDateBase()
        dbManager.closeDatabase()
        val database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null)
        val cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME3 + " where cid = " + "\"" + cityId + "\"", null)
        var warningId: String? = null
        for (i in 0 until cursor.count) {
            cursor.moveToPosition(i)
            warningId = cursor.getString(cursor.getColumnIndex("wid"))
        }
        return warningId
    }

    /**
     * ??????????????????
     */
    private fun okHttpWarning(url: String, warningId: String?) {
        Thread {
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
                                val `object` = JSONObject(result)
                                if (`object` != null) {
                                    if (!`object`.isNull("data")) {
                                        disWarnings.clear()
                                        cityWarnings.clear()
                                        proWarnings.clear()
                                        val jsonArray = `object`.getJSONArray("data")
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
                                            if (!dto.name.contains("??????")) {
                                                if (!TextUtils.isEmpty(warningId)) {
                                                    when {
                                                        TextUtils.equals(warningId, item0) -> {
                                                            disWarnings.add(dto)
                                                        }
                                                        TextUtils.equals(warningId!!.substring(0, 4) + "00", item0) -> {
                                                            cityWarnings.add(dto)
                                                        }
                                                        TextUtils.equals(warningId!!.substring(0, 2) + "0000", item0) -> {
                                                            proWarnings.add(dto)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (disWarnings.size > 0) {
                                            tvDisWarning.text = "????????????${disWarnings.size}???"
                                            tvDisWarning.visibility = View.VISIBLE
                                        }
                                        if (cityWarnings.size > 0) {
                                            tvCityWarning.text = "????????????${cityWarnings.size}???"
                                            tvCityWarning.visibility = View.VISIBLE
                                        }
                                        if (proWarnings.size > 0) {
                                            tvProWarning.text = "??????????????????${proWarnings.size}???"
                                            tvProWarning.visibility = View.VISIBLE
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
     * ?????????listview
     */
    private fun initListView() {
        mAdapter = WeeklyForecastAdapter(activity, weeklyList, Color.WHITE)
        listView.adapter = mAdapter
    }

    /**
     * ????????????????????????????????????????????????
     * @param lng
     * @param lat
     */
    private fun okHttpHourRain() {
        val url = String.format("http://api.caiyunapp.com/v2/HyTVV5YAkoxlQ3Zd/%s,%s/forecast", lng, lat)
        Thread {
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
                                val `object` = JSONObject(result)
                                if (!`object`.isNull("result")) {
                                    val obj = `object`.getJSONObject("result")
                                    if (!obj.isNull("minutely")) {
                                        val objMin = obj.getJSONObject("minutely")
                                        if (!objMin.isNull("description")) {
                                            val rain = objMin.getString("description")
                                            if (!TextUtils.isEmpty(rain)) {
                                                tvRain!!.text = rain.replace(getString(R.string.little_caiyun), "")
                                            }
                                        }
                                        if (!objMin.isNull("precipitation_2h")) {
                                            val array = objMin.getJSONArray("precipitation_2h")
                                            val size = array.length()
                                            val minuteList: MutableList<WeatherDto> = ArrayList()
                                            for (i in 0 until size) {
                                                val dto = WeatherDto()
                                                dto.minuteFall = array.getDouble(i).toFloat()
                                                minuteList.add(dto)
                                            }
                                            val minuteFallView = MinuteFallView(activity)
                                            minuteFallView.setData(minuteList, tvRain!!.text.toString())
                                            llContainerRain!!.removeAllViews()
                                            llContainerRain!!.addView(minuteFallView, CommonUtil.widthPixels(activity), CommonUtil.dip2px(activity, 150f).toInt())
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

    private fun okHttpVideoList() {
        Thread {
            val url = "https://decision-admin.tianqi.cn/Home/work2019/hlg_getVideos"
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
                                var type: String? = null
                                if (!obj.isNull("type")) {
                                    type = obj.getString("type")
                                }
                                if (!obj.isNull("l")) {
                                    val array = obj.getJSONArray("l")
                                    if (array.length() > 0) {
                                        val itemObj = array.getJSONObject(0)
                                        val title = itemObj.getString("l1")
                                        val dataUrl = itemObj.getString("l2")

                                        val intent = Intent(activity, WebviewActivity::class.java)
                                        intent.putExtra(CONST.ACTIVITY_NAME, title)
                                        intent.putExtra(CONST.WEB_URL, dataUrl)
                                        startActivity(intent)
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
            R.id.ivMap -> {
                val intent = Intent(activity, SelectPositionActivity::class.java)
                intent.putExtra("lat", lat)
                intent.putExtra("lng", lng)
                startActivityForResult(intent, 1004)
            }
            R.id.ivRouteWeather -> {
                val intent = Intent(activity, RouteWeatherActivity::class.java)
                intent.putExtra(CONST.ACTIVITY_NAME, "????????????")
                startActivity(intent)
            }
            R.id.tvPosition -> startActivity(Intent(activity, CityActivity::class.java))
            R.id.tvFact -> {
                tvTemp.text = tvFact.tag.toString() + "??"
                tvFact.setTextColor(Color.WHITE)
                tvFact.setBackgroundResource(R.drawable.bg_fact_temp_press)
                tvBody.setTextColor(0x60ffffff)
                tvBody.setBackgroundResource(R.drawable.bg_body_temp)
            }
            R.id.tvBody -> {
                tvTemp.text = tvBody.tag.toString() + "??"
                tvFact.setTextColor(0x60ffffff)
                tvFact.setBackgroundResource(R.drawable.bg_fact_temp)
                tvBody.setTextColor(Color.WHITE)
                tvBody.setBackgroundResource(R.drawable.bg_body_temp_press)
            }
            R.id.tvAqiCount, R.id.tvAqi -> {
//                val intent = Intent(activity, HAirPolutionActivity::class.java)
//                intent.putExtra(CONST.ACTIVITY_NAME, "????????????")
//                startActivity(intent)
            }
            R.id.tvChart, R.id.tvList -> if (listView!!.visibility == View.VISIBLE) {
                tvChart!!.setBackgroundResource(R.drawable.bg_chart_press)
                tvList.setBackgroundResource(R.drawable.bg_list)
                listView!!.visibility = View.GONE
                hScrollView2!!.visibility = View.VISIBLE
            } else {
                tvChart!!.setBackgroundResource(R.drawable.bg_chart)
                tvList.setBackgroundResource(R.drawable.bg_list_press)
                listView!!.visibility = View.VISIBLE
                hScrollView2!!.visibility = View.GONE
            }
            R.id.tvRain -> {
                val intent = Intent(activity, MinuteFallActivity::class.java)
                intent.putExtra(CONST.ACTIVITY_NAME, "?????????????????????")
                startActivity(intent)
            }
            R.id.clMinute -> if (llContainerRain!!.visibility == View.VISIBLE) {
                ivClose!!.setImageResource(R.drawable.iv_open)
                llContainerRain!!.visibility = View.GONE
                mapView!!.visibility = View.GONE
                ivPlay2!!.visibility = View.GONE
                hsTime!!.visibility = View.GONE
            } else {
                ivClose!!.setImageResource(R.drawable.iv_close)
                llContainerRain!!.visibility = View.VISIBLE
                mapView!!.visibility = View.VISIBLE
                ivPlay2!!.visibility = View.VISIBLE
                hsTime!!.visibility = View.VISIBLE
            }
            R.id.clHour -> if (hScrollView!!.visibility == View.VISIBLE) {
                hScrollView!!.visibility = View.GONE
                ivClose2.setImageResource(R.drawable.iv_open)
            } else {
                hScrollView!!.visibility = View.VISIBLE
                ivClose2.setImageResource(R.drawable.iv_close)
            }
            R.id.tvDisWarning -> {
                val intent = Intent(activity, HeadWarningActivity::class.java)
                val bundle = Bundle()
                bundle.putParcelableArrayList("warningList", disWarnings as ArrayList<out Parcelable?>)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            R.id.tvCityWarning -> {
                val intent = Intent(activity, HeadWarningActivity::class.java)
                val bundle = Bundle()
                bundle.putParcelableArrayList("warningList", cityWarnings as ArrayList<out Parcelable?>)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            R.id.tvProWarning -> {
                val intent = Intent(activity, HeadWarningActivity::class.java)
                val bundle = Bundle()
                bundle.putParcelableArrayList("warningList", proWarnings as ArrayList<out Parcelable?>)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            R.id.clVideo -> {
                okHttpVideoList()
            }
            R.id.clAudio -> {
                if (!mTts!!.isSpeaking) {
                    ivAudio.setImageResource(R.drawable.audio_animation)
                    val audioAnimation = ivAudio.drawable as AnimationDrawable
                    audioAnimation.start()
                    val currentTime = sdf2.format(Date())
                    val audioText = "?????????????????????$currentTime$weatherText"
                    mTts!!.startSpeaking(audioText, object : SynthesizerListener {
                        override fun onBufferProgress(percent: Int, p1: Int, p2: Int, p3: String?) {
                            // ????????????
                            mPercentForBuffering = percent
                        }

                        override fun onSpeakBegin() {
                        }

                        override fun onSpeakProgress(percent: Int, p1: Int, p2: Int) {
                            // ????????????
                            mPercentForPlaying = percent
                        }

                        override fun onEvent(eventType: Int, p1: Int, p2: Int, obj: Bundle?) {
                            // ??????????????????????????????????????????id??????????????????????????????id??????????????????????????????????????????????????????????????????????????????
                            // ??????????????????????????????id???null
//							if (SpeechEvent.EVENT_SESSION_ID == eventType) {
//								val sid = obj!!.getString(SpeechEvent.KEY_EVENT_SESSION_ID)
//							}
                        }

                        override fun onSpeakPaused() {
                        }

                        override fun onSpeakResumed() {
                        }

                        override fun onCompleted(error: SpeechError?) {
                            if (error == null) {
                                ivAudio.setImageResource(R.drawable.icon_audio)
                            } else {
                            }
                        }
                    })
                }else {
                    ivAudio.setImageResource(R.drawable.icon_audio)
                    mTts!!.stopSpeaking()
                }
            }
            R.id.ivPlay2 -> {
                if (mRadarThread != null && mRadarThread!!.currentState == STATE_PLAYING) {
                    mRadarThread!!.pause()
                    ivPlay2!!.setImageResource(R.drawable.iv_play2)
                } else if (mRadarThread != null && mRadarThread!!.currentState == STATE_PAUSE) {
                    mRadarThread!!.play()
                    ivPlay2!!.setImageResource(R.drawable.iv_pause2)
                } else if (mRadarThread == null) {
                    ivPlay2!!.setImageResource(R.drawable.iv_pause2)
                    if (mRadarThread != null) {
                        mRadarThread!!.cancel()
                        mRadarThread = null
                    }
                    if (images.isNotEmpty()) {
                        mRadarThread = RadarThread(images)
                        mRadarThread!!.start()
                    }
                }
            }
            R.id.ivClimate -> {
                val intent = Intent(activity, WebviewActivity::class.java)
                intent.putExtra(CONST.ACTIVITY_NAME, "24??????")
                intent.putExtra(CONST.WEB_URL, "http://wx.tianqi.cn/Solar/jieqidetail")
                startActivity(intent)
            }
            R.id.tvDivPolicy -> {
                val intent = Intent(activity, WebviewActivity::class.java)
                intent.putExtra(CONST.ACTIVITY_NAME, "????????????????????????")
                intent.putExtra(CONST.WEB_URL, "http://m.heb.bendibao.com/news/gelizhengce/all.php?leavecity=&src=12379")
                startActivity(intent)
            }
            R.id.tvInfo -> {
                val intent = Intent(activity, WebviewActivity::class.java)
                intent.putExtra(CONST.ACTIVITY_NAME, "??????????????????????????????????????????????????????????????????")
                intent.putExtra(CONST.WEB_URL, "https://voice.baidu.com/act/newpneumonia/newpneumonia?fraz=partner&paaz=gjyj")
                startActivity(intent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1001 -> if (data != null) {
                    val dto: CityDto = data.getParcelableExtra("data")
                    tvPosition!!.text = dto.areaName
                    lat = dto.lat
                    lng = dto.lng
                    if (dto.lng == 0.0 || dto.lat == 0.0) {
                        getLatlngByCityid(dto.cityId)
                    } else {
                        isFusion = false
                        okHttpHourRain()
                        getGeo(isFusion)
                    }
                }
                1004 -> {
                    if (data != null) {
                        val bundle = data.extras
                        if (bundle != null) {
                            lat = bundle.getDouble("lat", lat)
                            lng = bundle.getDouble("lng", lng)
                            val position = bundle.getString("position")
                            Log.e("position", position)
                            if (!TextUtils.isEmpty(position)) {
                                tvPosition.text = position
                            }
                            isFusion = true
                            getGeo(isFusion)
                        }
                    }
                }
            }
        }
    }

    private fun getLatlngByCityid(cityId: String) {
        Thread {
            WeatherAPI.getWeather2(activity, cityId, Language.ZH_CN, object : AsyncResponseHandler() {
                override fun onComplete(content: Weather) {
                    super.onComplete(content)
                    activity!!.runOnUiThread {
                        val result = content.toString()
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                val obj = JSONObject(result)
                                if (!obj.isNull("c")) {
                                    val c = obj.getJSONObject("c")
                                    lng = c.getDouble("c13")
                                    lat = c.getDouble("c14")
                                    okHttpHourRain()
                                    getGeo(isFusion)
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                override fun onError(error: Throwable, content: String) {
                    super.onError(error, content)
                }
            })
        }.start()
    }

}