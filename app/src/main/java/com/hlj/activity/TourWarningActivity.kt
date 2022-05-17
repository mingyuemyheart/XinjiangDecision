package com.hlj.activity

import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.*
import android.view.View.OnClickListener
import android.view.animation.LinearInterpolator
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.*
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.hlj.adapter.WarningAdapter
import com.hlj.adapter.WarningStatisticAdapter
import com.hlj.common.CONST
import com.hlj.dto.WarningDto
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.hlj.view.ArcMenu
import kotlinx.android.synthetic.main.activity_tour_warning.*
import kotlinx.android.synthetic.main.fragment_warning.*
import kotlinx.android.synthetic.main.layout_title.*
import kotlinx.android.synthetic.main.shawn_warning_marker_icon_info.view.*
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
 * 旅游气象-天气预警
 */
class TourWarningActivity : BaseActivity(), OnClickListener, OnMapClickListener, OnMarkerClickListener, InfoWindowAdapter {

    private var aMap: AMap? = null
    private var blue = true
    private var yellow = true
    private var orange = true
    private var red = true
    private val warningList: MutableList<WarningDto?> = ArrayList()
    private val proList: MutableList<WarningDto?> = ArrayList()
    private val cityList: MutableList<WarningDto?> = ArrayList()
    private val disList: MutableList<WarningDto?> = ArrayList()
    private val markers: MutableList<Marker> = ArrayList()
    private var selectMarker: Marker? = null
    private var isShowPrompt = true
    private val sdf3 = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    private val polylines: ArrayList<Polyline> = ArrayList()

    //预警统计列表
    private var statisticAdapter: WarningStatisticAdapter? = null
    private val statisticList: MutableList<WarningDto> = ArrayList()
    private val warningTypes: HashMap<String, WarningDto?> = HashMap()

    private var warningName = "自治区级预警"
    private var warningAdapter: WarningAdapter? = null
    private val dataList: MutableList<WarningDto?> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_warning)
        initAmap(savedInstanceState)
        initWidget()
        initListViewStatistic()
        initListView()
    }

    /**
     * 初始化高德地图
     */
    private fun initAmap(bundle: Bundle?) {
        mapView.onCreate(bundle)
        if (aMap == null) {
            aMap = mapView.map
        }
        aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.926628, 105.178100), 6.0f))
        aMap!!.uiSettings.isMyLocationButtonEnabled = false // 设置默认定位按钮是否显示
        aMap!!.uiSettings.isZoomControlsEnabled = false
        aMap!!.uiSettings.isRotateGesturesEnabled = false
        aMap!!.setOnMapClickListener(this)
        aMap!!.setOnMarkerClickListener(this)
        aMap!!.setInfoWindowAdapter(this)
        aMap!!.setOnMapLoadedListener {
            tvMapNumber.text = aMap!!.mapContentApprovalNumber
            CommonUtil.drawHLJJson(this, aMap)
            drawRailWay("全部站")
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

    private fun drawRailWay(lineName: String) {
        for (i in 0 until polylines.size) {
            val polyline = polylines[i]
            polyline.remove()
        }
        polylines.clear()
        if (intent.hasExtra(CONST.LOCAL_ID)) {
            val localId = intent.getStringExtra(CONST.LOCAL_ID)
            when(localId) {
                "9103" -> CommonUtil.drawRailWay(this, aMap, polylines,lineName)
                "9203" -> CommonUtil.drawRoadLine(this, aMap, polylines,lineName)
                "9303" -> {}
            }
        }
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack.setOnClickListener(this)
        arcMenu.onMenuItemClickListener = arcMenuListener
        ivRefresh.setOnClickListener(this)
        tvList.setOnClickListener(this)
        clHistory.setOnClickListener(this)
        clWarning.setOnClickListener(this)
        ivArrow.setOnClickListener(this)

        if (intent.hasExtra(CONST.ACTIVITY_NAME)) {
            val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
            if (title != null) {
                tvTitle.text = title
            }
        }

        addColumn()
        refresh()
    }

    /**
     * 添加子栏目
     */
    private fun addColumn() {
        llContainer!!.removeAllViews()
        llContainer1.removeAllViews()
        val nameList: ArrayList<String> = ArrayList()
        nameList.add("自治区级预警")
        nameList.add("市级预警")
        nameList.add("县级预警")
        val size = nameList.size
        for (i in 0 until size) {
            val tvName = TextView(this)
            tvName.gravity = Gravity.CENTER
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            tvName.paint.isFakeBoldText = true
            tvName.setPadding(0, CommonUtil.dip2px(this, 10f).toInt(), 0, CommonUtil.dip2px(this, 10f).toInt())
            tvName.maxLines = 1
            tvName.text = nameList[i]
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.weight = 1f
            tvName.layoutParams = params
            llContainer!!.addView(tvName)

            val tvBar = TextView(this)
            tvBar.gravity = Gravity.CENTER
            val params1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params1.weight = 1f
            params1.height = CommonUtil.dip2px(this, 2f).toInt()
            params1.gravity = Gravity.CENTER
            tvBar.layoutParams = params1
            llContainer1.addView(tvBar)

            if (i == 0) {
                tvName.setTextColor(ContextCompat.getColor(this!!, R.color.colorPrimary))
                tvBar.setBackgroundColor(ContextCompat.getColor(this!!, R.color.colorPrimary))
            } else {
                tvName.setTextColor(ContextCompat.getColor(this!!, R.color.text_color3))
                tvBar.setBackgroundColor(ContextCompat.getColor(this!!, R.color.light_gray))
            }

            tvName.setOnClickListener { arg0 ->
                if (llContainer != null) {
                    for (j in 0 until llContainer!!.childCount) {
                        val name = llContainer!!.getChildAt(j) as TextView
                        val bar = llContainer1.getChildAt(j) as TextView
                        if (TextUtils.equals(tvName.text.toString(), name.text.toString())) {
                            name.setTextColor(ContextCompat.getColor(this!!, R.color.colorPrimary))
                            bar.setBackgroundColor(ContextCompat.getColor(this!!, R.color.colorPrimary))
                            warningName = tvName.text.toString()
                            addWarnings()
                        } else {
                            name.setTextColor(ContextCompat.getColor(this!!, R.color.text_color3))
                            bar.setBackgroundColor(ContextCompat.getColor(this!!, R.color.light_gray))
                        }
                    }
                }
            }
        }
    }

    private fun addWarnings() {
        dataList.clear()
        when(warningName) {
            "自治区级预警" -> {
                dataList.addAll(proList)
            }
            "市级预警" -> {
                dataList.addAll(cityList)
            }
            "县级预警" -> {
                dataList.addAll(disList)
            }
        }
        if (warningAdapter != null) {
            warningAdapter!!.notifyDataSetChanged()
        }
        if (dataList.isEmpty()) {
            clNoWarning.visibility = View.VISIBLE
            tvList.visibility = View.GONE
        } else {
            clNoWarning.visibility = View.GONE
            tvList.visibility = View.VISIBLE
        }
    }

    private fun refresh() {
        okHttpWarning()
    }

    /**
     * 获取预警信息
     */
    private fun okHttpWarning() {
        Thread {
            val url = "https://decision-admin.tianqi.cn/Home/work2019/getwarns?areaid=65"
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
                                warningList.clear()
                                proList.clear()
                                cityList.clear()
                                disList.clear()
                                warningTypes.clear()
                                val obj = JSONObject(result)
                                if (!obj.isNull("data")) {
                                    val jsonArray = obj.getJSONArray("data")
                                    for (i in 0 until jsonArray.length()) {
                                        val tempArray = jsonArray.getJSONArray(i)
                                        val dto = WarningDto()
                                        dto.html = tempArray.getString(1)
                                        val array = dto.html.split("-").toTypedArray()
                                        val item0 = array[0]
                                        val item1 = array[1]
                                        val item2 = array[2]
                                        dto.item0 = item0
                                        dto.provinceId = item0.substring(0, 2)
                                        dto.type = item2.substring(0, 5)
                                        dto.color = item2.substring(5, 7)
                                        dto.time = item1
                                        dto.lng = tempArray.getDouble(2)
                                        dto.lat = tempArray.getDouble(3)
                                        dto.name = tempArray.getString(0)
                                        if (!dto.name.contains("解除")) {
                                            warningList.add(dto)
                                            if (TextUtils.equals(item0.substring(item0.length-4, item0.length), "0000")) {
                                                proList.add(dto)
                                            } else if (TextUtils.equals(item0.substring(item0.length-2, item0.length), "00")) {
                                                cityList.add(dto)
                                            } else {
                                                disList.add(dto)
                                            }
                                        }
                                        warningTypes[dto.type] = dto
                                    }
                                    addWarningMarkers()
                                    addWarnings()
                                }
                                try {
                                    var time = ""
                                    if (!obj.isNull("time")) {
                                        val t = obj.getLong("time")
                                        time = sdf3.format(Date(t * 1000))
                                    }
                                    tvTime.text = "${time}更新"

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
                                                name = CommonUtil.getWarningNameByType(this@TourWarningActivity, d!!.type)
                                                when(d!!.color) {
                                                    "04" -> redCount++
                                                    "03" -> orangeCount++
                                                    "02" -> yellowCount++
                                                    "01" -> blueCount++
                                                }
                                            }
                                        }

                                        if (redCount > 0) {
                                            strRed = "${redCount}红"
                                        }
                                        if (orangeCount > 0) {
                                            strOrange = "${orangeCount}橙"
                                        }
                                        if (yellowCount > 0) {
                                            strYellow = "${yellowCount}黄"
                                        }
                                        if (blueCount > 0) {
                                            strBlue = "${blueCount}蓝"
                                        }

                                        strType += "[${name}]${strRed}${strOrange}${strYellow}${strBlue}；"
                                    }

                                    val str1 = "新疆全区共有"
                                    val str2 = warningList.size.toString()
                                    val str3 = "条预警"
                                    val str4 = "([省级]预警${proList.size}条，[市级]预警${cityList.size}条，[县级]预警${disList.size}条；${strType})"
                                    tvWarningStatistic.text = "${str1}${str2}${str3}${str4}"
                                    clWarning!!.visibility = View.VISIBLE

                                    val builder = SpannableStringBuilder(tvWarningStatistic.text.toString())
                                    val builderSpan1 = ForegroundColorSpan(ContextCompat.getColor(this@TourWarningActivity, R.color.text_color3))
                                    val builderSpan2 = ForegroundColorSpan(ContextCompat.getColor(this@TourWarningActivity, R.color.colorPrimary))
                                    val builderSpan3 = ForegroundColorSpan(ContextCompat.getColor(this@TourWarningActivity, R.color.text_color3))
                                    val builderSpan4 = ForegroundColorSpan(ContextCompat.getColor(this@TourWarningActivity, R.color.text_color4))
                                    builder.setSpan(builderSpan1, 0, str1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    builder.setSpan(builderSpan2, str1.length, str1.length+str2.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    builder.setSpan(builderSpan3, str1.length+str2.length, str1.length+str2.length+str3.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    builder.setSpan(builderSpan4, str1.length+str2.length+str3.length, str1.length+str2.length+str3.length+str4.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    tvWarningStatistic!!.text = builder
                                    clWarning!!.visibility = View.VISIBLE

                                    //计算统计列表信息
                                    var rnation = 0
                                    var rpro = 0
                                    var rcity = 0
                                    var rdis = 0
                                    var onation = 0
                                    var opro = 0
                                    var ocity = 0
                                    var odis = 0
                                    var ynation = 0
                                    var ypro = 0
                                    var ycity = 0
                                    var ydis = 0
                                    var bnation = 0
                                    var bpro = 0
                                    var bcity = 0
                                    var bdis = 0
                                    var wnation = 0
                                    var wpro = 0
                                    var wcity = 0
                                    var wdis = 0
                                    for (i in warningList.indices) {
                                        val dto = warningList[i]
                                        if (TextUtils.equals(dto!!.color, "04")) {
                                            if (TextUtils.equals(dto.item0, "000000")) {
                                                rnation += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 4, dto.item0.length), "0000")) {
                                                rpro += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 2, dto.item0.length), "00")) {
                                                rcity += 1
                                            } else {
                                                rdis += 1
                                            }
                                        } else if (TextUtils.equals(dto.color, "03")) {
                                            if (TextUtils.equals(dto.item0, "000000")) {
                                                onation += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 4, dto.item0.length), "0000")) {
                                                opro += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 2, dto.item0.length), "00")) {
                                                ocity += 1
                                            } else {
                                                odis += 1
                                            }
                                        } else if (TextUtils.equals(dto.color, "02")) {
                                            if (TextUtils.equals(dto.item0, "000000")) {
                                                ynation += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 4, dto.item0.length), "0000")) {
                                                ypro += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 2, dto.item0.length), "00")) {
                                                ycity += 1
                                            } else {
                                                ydis += 1
                                            }
                                        } else if (TextUtils.equals(dto.color, "01")) {
                                            if (TextUtils.equals(dto.item0, "000000")) {
                                                bnation += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 4, dto.item0.length), "0000")) {
                                                bpro += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 2, dto.item0.length), "00")) {
                                                bcity += 1
                                            } else {
                                                bdis += 1
                                            }
                                        } else if (TextUtils.equals(dto.color, "05")) {
                                            if (TextUtils.equals(dto.item0, "000000")) {
                                                wnation += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 4, dto.item0.length), "0000")) {
                                                wpro += 1
                                            } else if (TextUtils.equals(dto.item0.substring(dto.item0.length - 2, dto.item0.length), "00")) {
                                                wcity += 1
                                            } else {
                                                wdis += 1
                                            }
                                        }
                                    }
                                    statisticList.clear()
                                    var wDto = WarningDto()
                                    wDto.colorName = "共" + warningList.size
                                    wDto.nationCount = "国家级" + (rnation + onation + ynation + bnation + wnation)
                                    wDto.proCount = "省级" + (rpro + opro + ypro + bpro + wpro)
                                    wDto.cityCount = "市级" + (rcity + ocity + ycity + bcity + wcity)
                                    wDto.disCount = "县级" + (rdis + odis + ydis + bdis + wdis)
                                    statisticList.add(wDto)
                                    wDto = WarningDto()
                                    wDto.colorName = "红" + (rnation + rpro + rcity + rdis)
                                    wDto.nationCount = rnation.toString() + ""
                                    wDto.proCount = rpro.toString() + ""
                                    wDto.cityCount = rcity.toString() + ""
                                    wDto.disCount = rdis.toString() + ""
                                    statisticList.add(wDto)
                                    wDto = WarningDto()
                                    wDto.colorName = "橙" + (onation + opro + ocity + odis)
                                    wDto.nationCount = onation.toString() + ""
                                    wDto.proCount = opro.toString() + ""
                                    wDto.cityCount = ocity.toString() + ""
                                    wDto.disCount = odis.toString() + ""
                                    statisticList.add(wDto)
                                    wDto = WarningDto()
                                    wDto.colorName = "黄" + (ynation + ypro + ycity + ydis)
                                    wDto.nationCount = ynation.toString() + ""
                                    wDto.proCount = ypro.toString() + ""
                                    wDto.cityCount = ycity.toString() + ""
                                    wDto.disCount = ydis.toString() + ""
                                    statisticList.add(wDto)
                                    wDto = WarningDto()
                                    wDto.colorName = "蓝" + (bnation + bpro + bcity + bdis)
                                    wDto.nationCount = bnation.toString() + ""
                                    wDto.proCount = bpro.toString() + ""
                                    wDto.cityCount = bcity.toString() + ""
                                    wDto.disCount = bdis.toString() + ""
                                    statisticList.add(wDto)
                                    wDto = WarningDto()
                                    wDto.colorName = "未知" + (wnation + wpro + wcity + wdis)
                                    wDto.nationCount = wnation.toString() + ""
                                    wDto.proCount = wpro.toString() + ""
                                    wDto.cityCount = wcity.toString() + ""
                                    wDto.disCount = wdis.toString() + ""
                                    statisticList.add(wDto)
                                    if (statisticAdapter != null) {
                                        statisticAdapter!!.notifyDataSetChanged()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
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
     * 在地图上添加marker
     */
    private fun addWarningMarkers() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        for (dto in warningList) {
            val optionsTemp = MarkerOptions()
            optionsTemp.title(dto!!.lat.toString() + "," + dto.lng + "," + dto.item0 + "," + dto.color)
            optionsTemp.snippet(dto.color)
            optionsTemp.anchor(0.5f, 0.5f)
            optionsTemp.position(LatLng(dto.lat, dto.lng))
            val mView = inflater.inflate(R.layout.shawn_warning_marker_icon, null)
            val ivMarker = mView.findViewById<ImageView>(R.id.ivMarker)
            var bitmap: Bitmap? = null
            if (dto.color == CONST.blue[0]) {
                bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + dto.type + CONST.blue[1] + CONST.imageSuffix)
            } else if (dto.color == CONST.yellow[0]) {
                bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + dto.type + CONST.yellow[1] + CONST.imageSuffix)
            } else if (dto.color == CONST.orange[0]) {
                bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + dto.type + CONST.orange[1] + CONST.imageSuffix)
            } else if (dto.color == CONST.red[0]) {
                bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + dto.type + CONST.red[1] + CONST.imageSuffix)
            }
            if (bitmap == null) {
                bitmap = CommonUtil.getImageFromAssetsFile(this, "warning/" + "default" + CONST.imageSuffix)
            }
            ivMarker.setImageBitmap(bitmap)
            optionsTemp.icon(BitmapDescriptorFactory.fromView(mView))
            var marker: Marker
            marker = if (TextUtils.equals(dto.color, "01") && blue) {
                aMap!!.addMarker(optionsTemp)
            } else if (TextUtils.equals(dto.color, "02") && yellow) {
                aMap!!.addMarker(optionsTemp)
            } else if (TextUtils.equals(dto.color, "03") && orange) {
                aMap!!.addMarker(optionsTemp)
            } else if (TextUtils.equals(dto.color, "04") && red) {
                aMap!!.addMarker(optionsTemp)
            } else {
                aMap!!.addMarker(optionsTemp)
            }
            markers.add(marker)
        }
    }

    private fun switchMarkers() {
        for (marker in markers) {
            if (TextUtils.equals(marker.snippet, "01")) {
                marker.isVisible = blue
            } else if (TextUtils.equals(marker.snippet, "02")) {
                marker.isVisible = yellow
            } else if (TextUtils.equals(marker.snippet, "03")) {
                marker.isVisible = orange
            } else if (TextUtils.equals(marker.snippet, "04")) {
                marker.isVisible = red
            }
        }
    }

    override fun onMapClick(arg0: LatLng?) {
        if (selectMarker != null) {
            selectMarker!!.hideInfoWindow()
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker != null) {
            selectMarker = marker
            if (selectMarker!!.isInfoWindowShown) {
                selectMarker!!.hideInfoWindow()
            } else {
                selectMarker!!.showInfoWindow()
            }
        }
        return true
    }

    override fun getInfoContents(marker: Marker): View? {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val mView = inflater.inflate(R.layout.shawn_warning_marker_icon_info, null)
        val infoList = addInfoList(marker)
        val mAdapter = WarningAdapter(this, infoList, true)
        mView.listView.adapter = mAdapter
        val params = mView.listView.layoutParams
        if (infoList.size == 1) {
            params.height = CommonUtil.dip2px(this, 50f).toInt()
        } else if (infoList.size == 2) {
            params.height = CommonUtil.dip2px(this, 100f).toInt()
        } else if (infoList.size > 2) {
            params.height = CommonUtil.dip2px(this, 150f).toInt()
        }
        mView.listView.layoutParams = params
        mView.listView.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 -> intentDetail(infoList[arg2]) }
        return mView
    }

    private fun addInfoList(marker: Marker): List<WarningDto?> {
        val infoList: MutableList<WarningDto?> = ArrayList()
        for (dto in warningList) {
            val latLng = marker.title.split(",").toTypedArray()
            if (TextUtils.equals(latLng[0], dto!!.lat.toString() + "") && TextUtils.equals(latLng[1], dto.lng.toString() + "")) {
                infoList.add(dto)
            }
        }
        return infoList
    }

    private fun intentDetail(data: WarningDto?) {
        val intentDetail = Intent(this, WarningDetailActivity::class.java)
        val bundle = Bundle()
        bundle.putParcelable("data", data)
        intentDetail.putExtras(bundle)
        startActivity(intentDetail)
    }

    override fun getInfoWindow(arg0: Marker?): View? {
        return null
    }

    private val arcMenuListener = ArcMenu.OnMenuItemClickListener { view, pos ->
        if (pos == 0) {
            blue = !blue
            if (!blue) {
                (view as ImageView).setImageResource(R.drawable.shawn_icon_arc_blue_press)
            } else {
                (view as ImageView).setImageResource(R.drawable.shawn_icon_arc_blue)
            }
            switchMarkers()
        } else if (pos == 1) {
            yellow = !yellow
            if (!yellow) {
                (view as ImageView).setImageResource(R.drawable.shawn_icon_arc_yellow_press)
            } else {
                (view as ImageView).setImageResource(R.drawable.shawn_icon_arc_yellow)
            }
            switchMarkers()
        } else if (pos == 2) {
            orange = !orange
            if (!orange) {
                (view as ImageView).setImageResource(R.drawable.shawn_icon_arc_orange_press)
            } else {
                (view as ImageView).setImageResource(R.drawable.shawn_icon_arc_orange)
            }
            switchMarkers()
        } else if (pos == 3) {
            red = !red
            if (!red) {
                (view as ImageView).setImageResource(R.drawable.shawn_icon_arc_red_press)
            } else {
                (view as ImageView).setImageResource(R.drawable.shawn_icon_arc_red)
            }
            switchMarkers()
        }
    }

    /**
     * 初始化预警统计列表
     */
    private fun initListViewStatistic() {
        statisticAdapter = WarningStatisticAdapter(this, statisticList)
        listViewStatistic.adapter = statisticAdapter
        listViewStatistic.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 -> clickPromptWarning() }
    }

    /**
     * 隐藏或显示ListView的动画
     */
    private fun hideOrShowListViewAnimator(view: View?, startValue: Int, endValue: Int) {
        //1.设置属性的初始值和结束值
        val mAnimator = ValueAnimator.ofInt(0, 100)
        //2.为目标对象的属性变化设置监听器
        mAnimator.addUpdateListener { animation ->
            val animatorValue = animation.animatedValue as Int
            val fraction = animatorValue / 100f
            val mEvaluator = IntEvaluator()
            //3.使用IntEvaluator计算属性值并赋值给ListView的高
            view!!.layoutParams.height = mEvaluator.evaluate(fraction, startValue, endValue)
            view.requestLayout()
        }
        //4.为ValueAnimator设置LinearInterpolator
        mAnimator.interpolator = LinearInterpolator()
        //5.设置动画的持续时间
        mAnimator.duration = 200
        //6.为ValueAnimator设置目标对象并开始执行动画
        mAnimator.setTarget(view)
        mAnimator.start()
    }

    private fun clickPromptWarning() {
        val height = CommonUtil.getListViewHeightBasedOnChildren(listViewStatistic)
        isShowPrompt = !isShowPrompt
        if (!isShowPrompt) {
            ivArrow.setImageResource(R.drawable.icon_arrow_up_black)
            hideOrShowListViewAnimator(listViewStatistic, 0, height)
        } else {
            ivArrow.setImageResource(R.drawable.icon_arrow_down_black)
            hideOrShowListViewAnimator(listViewStatistic, height, 0)
        }
    }

    /**
     * 初始化listview
     */
    private fun initListView() {
        warningAdapter = WarningAdapter(this, dataList, false)
        listView.adapter = warningAdapter
        listView.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val data = dataList[arg2]
            val intentDetail = Intent(this, WarningDetailActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("data", data)
            intentDetail.putExtras(bundle)
            startActivity(intentDetail)
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> finish()
            R.id.clWarning, R.id.ivArrow -> clickPromptWarning()
            R.id.ivRefresh -> refresh()
            R.id.tvList -> startActivity(Intent(this, WarningListActivity::class.java))
            R.id.clHistory -> startActivity(Intent(this, WarningHistoryActivity::class.java))
            R.id.ivStatistic -> startActivity(Intent(this, WarningStatisticActivity::class.java))
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
    override fun onDestroy() {
        super.onDestroy()
        if (mapView != null) {
            mapView!!.onDestroy()
        }
    }

}
