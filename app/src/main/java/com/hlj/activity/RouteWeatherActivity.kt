package com.hlj.activity

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.media.ThumbnailUtils
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import cn.com.weather.api.WeatherAPI
import cn.com.weather.listener.AsyncResponseHandler
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.OnMapClickListener
import com.amap.api.maps.AMap.OnMarkerClickListener
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.route.*
import com.amap.api.services.route.RouteSearch.*
import com.hlj.common.CONST
import com.hlj.dto.NewsDto
import com.hlj.dto.WarningDto
import com.hlj.dto.WeatherDto
import com.hlj.manager.DBManager
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.hlj.utils.WeatherUtil
import com.hlj.view.WeeklyViewTour
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechError
import com.iflytek.cloud.SpeechSynthesizer
import com.iflytek.cloud.SynthesizerListener
import kotlinx.android.synthetic.main.activity_route.*
import kotlinx.android.synthetic.main.layout_title.*
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

/**
 * ????????????
 * @author shawn_sun
 */
class RouteWeatherActivity : BaseActivity(), OnClickListener, AMapLocationListener, OnRouteSearchListener,
OnMarkerClickListener, OnMapClickListener {

    private var routeSearch: RouteSearch? = null //????????????
    private var startPoint: LatLonPoint? = null //??????????????????
    private var endPoint: LatLonPoint? = null //??????????????????
    private var mLocationOption: AMapLocationClientOption? = null //??????mLocationOption??????
    private var mLocationClient: AMapLocationClient? = null //??????AMapLocationClient?????????

    private var aMap: AMap? = null
    private val dataMap: HashMap<String, NewsDto> = HashMap()
    private val markers: MutableList<Marker> = ArrayList()
    private var selectMarker: Marker? = null
    private val sdf1 = SimpleDateFormat("HH", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("yyyy???MM???dd??? HH???mm???", Locale.CHINA)
    private val sdf3 = SimpleDateFormat("yyyyMMdd", Locale.CHINA)
    private val sdf4 = SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA)
    private val weatherMap: HashMap<String, ArrayList<WeatherDto>> = HashMap()
    private val warningMap: HashMap<String, ArrayList<WarningDto>> = HashMap()
    private val boundBuilder = LatLngBounds.builder()
    private var foreDate: Long = 0//?????????????????????
    private val polylines: ArrayList<Polyline> = ArrayList()
    private val pointList: MutableList<LatLonPoint> = ArrayList()

    //????????????
    private var mTts : SpeechSynthesizer? = null// ??????????????????
    private var voicer = "xiaoyan"// ???????????????
    private var mPercentForBuffering = 0// ????????????
    private var mPercentForPlaying = 0// ????????????
    private var mEngineType = SpeechConstant.TYPE_CLOUD// ????????????
    private var mToast : Toast? = null
    private var weatherText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)
        initAmap(savedInstanceState)
        initWidget()
    }

    private fun initSpeech() {
        // ?????????????????????
        mTts = SpeechSynthesizer.createSynthesizer(this) { code ->

        }
        mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT)

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

    /**
     * ???????????????
     */
    private fun initWidget() {
        llBack!!.setOnClickListener(this)
        clExchange.setOnClickListener(this)
        tvStart!!.setOnClickListener(this)
        tvStartAddr.setOnClickListener(this)
        tvEnd!!.setOnClickListener(this)
        tvEndAddr.setOnClickListener(this)
        ivClose.setOnClickListener(this)
        clAudio.setOnClickListener(this)

        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (title != null) {
            tvTitle!!.text = title
        }
        routeSearch = RouteSearch(this)
        routeSearch!!.setRouteSearchListener(this)

        if (CommonUtil.isLocationOpen(this)) {
            startLocation()
        }

        initSpeech()
    }

    /**
     * ????????????
     */
    private fun startLocation() {
        mLocationOption = AMapLocationClientOption() //?????????????????????
        mLocationClient = AMapLocationClient(this) //???????????????
        mLocationOption!!.locationMode = AMapLocationMode.Hight_Accuracy //???????????????????????????????????????Battery_Saving?????????????????????Device_Sensors??????????????????
        mLocationOption!!.isNeedAddress = true //????????????????????????????????????????????????????????????
        mLocationOption!!.isOnceLocation = true //???????????????????????????,?????????false
        mLocationOption!!.isWifiActiveScan = true //????????????????????????WIFI????????????????????????
        mLocationOption!!.isMockEnable = false //??????????????????????????????,?????????false????????????????????????
        mLocationOption!!.interval = 2000 //??????????????????,????????????,?????????2000ms
        mLocationClient!!.setLocationOption(mLocationOption) //??????????????????????????????????????????
        mLocationClient!!.setLocationListener(this)
        mLocationClient!!.startLocation() //????????????
    }

    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (amapLocation != null && amapLocation.errorCode == AMapLocation.LOCATION_SUCCESS) {
            startPoint = LatLonPoint(amapLocation.latitude, amapLocation.longitude)
            tvStart!!.text = getString(R.string.route_current_position)
            tvStartAddr.text = amapLocation.address
            addLocationMarker(LatLng(amapLocation.latitude, amapLocation.longitude))
        }
    }

    /**
     * ???????????????
     */
    private fun initAmap(bundle: Bundle?) {
        mapView!!.onCreate(bundle)
        if (aMap == null) {
            aMap = mapView!!.map
        }
        val centerLatLng = LatLng(CONST.centerLat, CONST.centerLng)
        aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, 6.8f))
        aMap!!.uiSettings.isZoomControlsEnabled = false
        aMap!!.uiSettings.isRotateGesturesEnabled = false
        aMap!!.setOnMapClickListener(this)
        aMap!!.setOnMarkerClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
            R.id.clExchange -> {
                val start = tvStart!!.text.toString()
                val startAddr = tvStartAddr!!.text.toString()
                val end = tvEnd!!.text.toString()
                val endAddr = tvEndAddr!!.text.toString()
                val startTemp = startPoint
                val endTemp = endPoint

                tvStart!!.text = end
                tvStartAddr!!.text = endAddr
                tvEnd!!.text = start
                tvEndAddr!!.text = startAddr
                startPoint = endTemp
                endPoint = startTemp
            }
            R.id.tvStart, R.id.tvStartAddr -> {
                val intentStart = Intent(this, RouteSearchActivity::class.java)
                intentStart.putExtra("startOrEnd", "start")
                startActivityForResult(intentStart, 1001)
            }
            R.id.tvEnd, R.id.tvEndAddr -> {
                val intentEnd = Intent(this, RouteSearchActivity::class.java)
                intentEnd.putExtra("startOrEnd", "end")
                startActivityForResult(intentEnd, 1002)
            }
            R.id.ivClose -> CommonUtil.topToBottom(clBottom)
            R.id.clAudio -> {
                if (!mTts!!.isSpeaking) {
                    ivAudio.setImageResource(R.drawable.audio_animation)
                    val audioAnimation = ivAudio.drawable as AnimationDrawable
                    audioAnimation.start()

                    if (tvName.tag != null && weatherMap.containsKey(tvName.tag.toString())) {
                        val dto: WeatherDto = weatherMap[tvName.tag.toString()]!![0]
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
                        weatherText = "??????????????????${tvName.text}???????????????????????????????????????????????????" + pheText + "???" + temperatureText + "???" + windDirText + windForceText
                    }

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
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            RESULT_OK -> when (requestCode) {
                1001 -> {
                    startPoint = getResultData(data, tvStart, tvStartAddr)
                    if (!TextUtils.isEmpty(tvStart!!.text.toString()) && !TextUtils.isEmpty(tvEnd!!.text.toString())) {
                        showDialog()
                        val ft = FromAndTo(startPoint, endPoint)
                        val drive = DriveRouteQuery(ft, DrivingMultiStrategy, null, null, null)
                        routeSearch!!.calculateDriveRouteAsyn(drive)
                    }
                }
                1002 -> {
                    endPoint = getResultData(data, tvEnd, tvEndAddr)
                    if (!TextUtils.isEmpty(tvStart!!.text.toString()) && !TextUtils.isEmpty(tvEnd!!.text.toString())) {
                        showDialog()
                        val ft = FromAndTo(startPoint, endPoint)
                        val drive = DriveRouteQuery(ft, DrivingMultiStrategy, null, null, null)
                        routeSearch!!.calculateDriveRouteAsyn(drive)
                    }
                }
            }
            RESULT_CANCELED -> when (requestCode) {
                1 -> startLocation()
            }
        }
    }

    private fun getResultData(data: Intent?, tvName: TextView?, tvAddr: TextView?): LatLonPoint? {
        var latLngPoint: LatLonPoint? = null
        if (data != null) {
            val bundle = data.extras
            if (bundle != null) {
                tvName!!.text = bundle.getString("cityName")
                tvAddr!!.text = bundle.getString("addr")
                latLngPoint = LatLonPoint(bundle.getDouble("lat"), bundle.getDouble("lng"))
            }
        }
        return latLngPoint
    }

    /**
     * ??????????????????
     */
    override fun onResume() {
        super.onResume()
        if (mapView != null) {
            mapView!!.onResume()
        }
    }

    /**
     * ??????????????????
     */
    override fun onPause() {
        super.onPause()
        if (mapView != null) {
            mapView!!.onPause()
        }
    }

    /**
     * ??????????????????
     */
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (mapView != null) {
            mapView!!.onSaveInstanceState(outState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mTts != null) {
            mTts!!.stopSpeaking()
            mTts!!.destroy()
        }
        if (mapView != null) {
            mapView!!.onDestroy()
        }
    }

    override fun onBusRouteSearched(arg0: BusRouteResult?, arg1: Int) {}
    override fun onDriveRouteSearched(arg0: DriveRouteResult?, arg1: Int) {
        if (arg0 == null || arg0.startPos == null || arg0.targetPos == null) {
            cancelDialog()
            Toast.makeText(this, getString(R.string.no_result), Toast.LENGTH_SHORT).show()
            return
        }
        removeMarkers()
        removePolylines()
        dataMap.clear()
        weatherMap.clear()
        warningMap.clear()

        val drivePath = arg0.paths[0]
        Thread {
            val driveSteps = drivePath.steps
            pointList.clear()
            for (i in driveSteps.indices) {
                val step = driveSteps[i]
                val polylineOptions = PolylineOptions()
                polylineOptions.color(ContextCompat.getColor(this, R.color.colorPrimary))
                polylineOptions.width(20f)
                for (j in step.polyline.indices) {
                    val point = step.polyline[j]
                    pointList.add(point)
                    polylineOptions.add(LatLng(point.latitude, point.longitude))
                }
                val polyline = aMap!!.addPolyline(polylineOptions)
                polyline.zIndex = -1000f
                polylines.add(polyline)
            }

            val distance = drivePath.distance.toDouble()
            val maxSize = when {
                distance <= 10 * 1000 -> {
                    5
                }
                distance <= 100 * 1000 -> {
                    10
                }
                distance <= 200 * 1000 -> {
                    10
                }
                distance <= 500 * 1000 -> {
                    20
                }
                distance <= 1000 * 1000 -> {
                    20
                }
                distance <= 2000 * 1000 -> {
                    25
                }
                distance <= 5000 * 1000 -> {
                    30
                }
                else -> {
                    30
                }
            }
            val divider = pointList.size / maxSize
            var i = 0
            getGeo(pointList[0].longitude, pointList[0].latitude, true)
            while (i < pointList.size) {
                val comparePoint = pointList[i]
                getGeo(comparePoint.longitude, comparePoint.latitude, true)
                i += divider
            }
            getGeo(pointList[pointList.size - 1].longitude, pointList[pointList.size - 1].latitude, true)
        }.start()
        cancelDialog()
    }

    override fun onWalkRouteSearched(arg0: WalkRouteResult?, arg1: Int) {}

    override fun onRideRouteSearched(rideRouteResult: RideRouteResult?, i: Int) {}

    /**
     * ??????????????????
     */
    private fun getGeo(lng: Double, lat: Double, isAnimate: Boolean) {
        WeatherAPI.getGeo(this, lng.toString(), lat.toString(), object : AsyncResponseHandler() {
            override fun onComplete(content: JSONObject) {
                super.onComplete(content)
                if (!content.isNull("geo")) {
                    try {
                        val itemObj = content.getJSONObject("geo")
                        val dto = NewsDto()
                        if (!itemObj.isNull("id")) {
                            dto.cityId = itemObj.getString("id")
                            dto.warningId = queryWarningIdByCityId(dto.cityId)
                            if (TextUtils.isEmpty(dto.warningId)) {
                                dto.warningId = ""
                            }
                        }
                        if (!itemObj.isNull("city")) {
                            dto.title = itemObj.getString("city")
                        }
                        dto.lat = lat
                        dto.lng = lng

                        dataMap[dto.cityId] = dto
                        getWeatherInfo(dto.cityId, dto.warningId, isAnimate)
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

    /**
     * ????????????id
     */
    private fun queryWarningIdByCityId(cityId: String): String? {
        val dbManager = DBManager(this)
        dbManager.openDateBase()
        dbManager.closeDatabase()
        val database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null)
        var cursor: Cursor? = null
        cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME3 + " where cid = " + "\"" + cityId + "\"", null)
        var warningId: String? = null
        for (i in 0 until cursor.count) {
            cursor.moveToPosition(i)
            warningId = cursor.getString(cursor.getColumnIndex("wid"))
        }
        return warningId
    }

    /**
     * ??????????????????marker
     */
    private fun addSingleMarkers(cityId: String, isAnimate: Boolean) {
        //??????
        var weatherDto: WeatherDto? = null
        if (weatherMap.containsKey(cityId)) {
            val weatherList = weatherMap[cityId]
            if (weatherList!!.size > 0) {
                weatherDto = weatherList[0]
            }
        }

        //??????
        var warningDto: WarningDto? = null
        if (warningMap.containsKey(cityId)) {
            val warningList = warningMap[cityId]
            if (warningList!!.size > 0) {
                warningDto = warningList[0]
            }
        }

        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val hour = sdf1.format(Date()).toInt()
        if (dataMap.containsKey(cityId)) {
            val dto = dataMap[cityId]
            val options = MarkerOptions()
            options.title(dto!!.title)
            options.snippet(cityId)
            options.anchor(0.5f, 0.5f)
            options.position(LatLng(dto.lat, dto.lng))
            boundBuilder.include(LatLng(dto.lat, dto.lng))
            val mView = inflater.inflate(R.layout.marker_icon_tour, null)
            mView.tvName.text = dto.title

            if (weatherDto != null) {
                val bitmap = if (hour in 5..17) {
                    WeatherUtil.getBitmap(this, weatherDto.highPheCode)
                } else {
                    WeatherUtil.getNightBitmap(this, weatherDto.lowPheCode)
                }
                if (bitmap != null) {
                    mView.ivPhe.setImageBitmap(bitmap)
                }
                mView.tvTemp.text = "${weatherDto!!.highTemp}/${weatherDto.lowTemp}???"
            }
            if (warningDto != null) {
                var bitmap: Bitmap? = null
                when (warningDto.color) {
                    CONST.blue[0] -> {
                        bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + warningDto.type + CONST.blue[1] + CONST.imageSuffix)
                    }
                    CONST.yellow[0] -> {
                        bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + warningDto.type + CONST.yellow[1] + CONST.imageSuffix)
                    }
                    CONST.orange[0] -> {
                        bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + warningDto.type + CONST.orange[1] + CONST.imageSuffix)
                    }
                    CONST.red[0] -> {
                        bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + warningDto.type + CONST.red[1] + CONST.imageSuffix)
                    }
                }
                if (bitmap == null) {
                    bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + "default" + CONST.imageSuffix)
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

        if (isAnimate) {
            if (dataMap.size > 0) {
                aMap!!.animateCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 50))
            }
        }
    }

    private fun removeMarkers() {
        for (i in 0 until markers.size) {
            val marker = markers[i]
            marker.remove()
        }
        markers.clear()
    }

    private fun removePolylines() {
        for (i in 0 until polylines.size) {
            val polygon = polylines[i]
            polygon.remove()
        }
        polylines.clear()
    }

    override fun onMapClick(arg0: LatLng?) {
        if (selectMarker != null) {
            selectMarker!!.hideInfoWindow()
        }
        CommonUtil.topToBottom(clBottom)
        getGeo(arg0!!.longitude, arg0.latitude, false)
    }

    private fun changeMarkerStatus() {
        if (selectMarker == null) {
            return
        }
        if (!dataMap.containsKey(selectMarker!!.snippet)) {
            return
        }
        val dto = dataMap[selectMarker!!.snippet]
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        //??????
        var weatherDto: WeatherDto? = null
        if (weatherMap.containsKey(selectMarker!!.snippet)) {
            val weatherList = weatherMap[selectMarker!!.snippet]
            if (weatherList!!.size > 0) {
                weatherDto = weatherList[0]
            }
        }

        //??????
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
                WeatherUtil.getBitmap(this, weatherDto.highPheCode)
            } else {
                WeatherUtil.getNightBitmap(this, weatherDto.lowPheCode)
            }
            if (bitmap != null) {
                mView.ivPhe.setImageBitmap(bitmap)
            }
            mView.tvTemp.text = "${weatherDto!!.highTemp}/${weatherDto.lowTemp}???"
        }
        if (warningDto != null) {
            var bitmap: Bitmap? = null
            when (warningDto.color) {
                CONST.blue[0] -> {
                    bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + warningDto.type + CONST.blue[1] + CONST.imageSuffix)
                }
                CONST.yellow[0] -> {
                    bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + warningDto.type + CONST.yellow[1] + CONST.imageSuffix)
                }
                CONST.orange[0] -> {
                    bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + warningDto.type + CONST.orange[1] + CONST.imageSuffix)
                }
                CONST.red[0] -> {
                    bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + warningDto.type + CONST.red[1] + CONST.imageSuffix)
                }
            }
            if (bitmap == null) {
                bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + "default" + CONST.imageSuffix)
            }
            if (bitmap != null) {
                mView.ivPhe.setImageBitmap(bitmap)
            }
            mView.tvTemp.text = "${warningDto!!.name}"
        }
        selectMarker!!.setIcon(BitmapDescriptorFactory.fromView(mView))
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null) {
            selectMarker = marker
            changeMarkerStatus()

            CommonUtil.bottomToTop(clBottom)
            if (marker.title != null) {
                tvName.text = marker.title
                tvName.tag = marker.snippet
            }

            //??????
            if (weatherMap.containsKey(marker.snippet)) {
                val weatherList = weatherMap[marker.snippet]
                //??????????????????
                val weeklyView = WeeklyViewTour(this)
                val currentDate = sdf3.parse(sdf3.format(Date())).time
                weeklyView.setData(weatherList, foreDate, currentDate)
                llContainerFifteen!!.removeAllViews()
                llContainerFifteen!!.addView(weeklyView, CommonUtil.widthPixels(this) * 2, CommonUtil.dip2px(this, 95f).toInt())
            }

            //??????
            val warningTypes: HashMap<String, WarningDto> = HashMap()
            if (warningMap.containsKey(marker.snippet)) {
                val warningList = warningMap[marker.snippet]
                llContainer.removeAllViews()
                for (i in 0 until warningList!!.size) {
                    val warningDto = warningList[i]
                    warningTypes[warningDto.type] = warningDto

                    val ivWarning = ImageView(this)
                    var bitmap: Bitmap? = null
                    when (warningDto.color) {
                        CONST.blue[0] -> {
                            bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + warningDto.type + CONST.blue[1] + CONST.imageSuffix)
                        }
                        CONST.yellow[0] -> {
                            bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + warningDto.type + CONST.yellow[1] + CONST.imageSuffix)
                        }
                        CONST.orange[0] -> {
                            bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + warningDto.type + CONST.orange[1] + CONST.imageSuffix)
                        }
                        CONST.red[0] -> {
                            bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + warningDto.type + CONST.red[1] + CONST.imageSuffix)
                        }
                    }
                    if (bitmap == null) {
                        bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + "default" + CONST.imageSuffix)
                    }
                    if (bitmap != null) {
                        ivWarning.setImageBitmap(bitmap)
                    }
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.width = CommonUtil.dip2px(this, 35f).toInt()
                    params.height = CommonUtil.dip2px(this, 35f).toInt()
                    params.leftMargin = CommonUtil.dip2px(this, 5f).toInt()
                    ivWarning.layoutParams = params
                    llContainer.addView(ivWarning)

                    ivWarning.setOnClickListener {
                        val intentDetail = Intent(this, WarningDetailActivity::class.java)
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
                            name = CommonUtil.getWarningNameByType(this, d!!.type)
                            when(d!!.color) {
                                "04" -> redCount++
                                "03" -> orangeCount++
                                "02" -> yellowCount++
                                "01" -> blueCount++
                            }
                        }
                    }

                    if (redCount > 0) {
                        strRed = "${redCount}???"
                    }
                    if (orangeCount > 0) {
                        strOrange = "${orangeCount}???"
                    }
                    if (yellowCount > 0) {
                        strYellow = "${yellowCount}???"
                    }
                    if (blueCount > 0) {
                        strBlue = "${blueCount}???"
                    }

                    strType += "[${name}]${strRed}${strOrange}${strYellow}${strBlue}???"
                }

                val str1 = "??????"
                val str2 = warningList.size.toString()
                val str3 = "????????????"
                val str4 = strType
                tvWarningStatistic.text = "${str1}${str2}${str3}${str4}"
            }
        }
        return true
    }

    private fun getWeatherInfo(cityId: String, warningId: String, isAnimate: Boolean) {
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

                                //??????
                                if (!obj.isNull("forecast")) {
                                    val forecast = obj.getJSONObject("forecast")

                                    //15???????????????
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
                                                }
                                            }
                                        }
                                        weatherMap[cityId] = weeklyList
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })

            okHttpWarning(warningId, cityId, isAnimate)
        }.start()
    }

    /**
     * ??????????????????
     */
    private fun okHttpWarning(warningId: String, cityId: String, isAnimate: Boolean) {
        Thread {
            if (!TextUtils.isEmpty(warningId)) {
                val url = "http://decision-admin.tianqi.cn/Home/extra/getwarns?order=0&areaid=$warningId"
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
                                            if (!dto.name.contains("??????")) {
                                                warningList.add(dto)
                                            }
                                        }
                                        warningMap[cityId] = warningList
                                    }
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                })
            }

            addSingleMarkers(cityId, isAnimate)
        }.start()
    }

    private var locationMarker: Marker? = null
    private fun addLocationMarker(arg0: LatLng?) {
        if (locationMarker != null) {
            locationMarker!!.remove()
        }
        val options = MarkerOptions()
        options.anchor(0.5f, 1.0f)
        val bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(resources, R.drawable.icon_map_location),
                CommonUtil.dip2px(this, 21f).toInt(), CommonUtil.dip2px(this, 32f).toInt())
        if (bitmap != null) {
            options.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        } else {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_location))
        }
        options.position(arg0)
        locationMarker = aMap!!.addMarker(options)
        locationMarker!!.isClickable = false
    }

}
