package com.hlj.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Parcelable
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.hlj.adapter.FactDetailAdapter
import com.hlj.adapter.FactMonitorAdapter
import com.hlj.adapter.FactTimeAdapter
import com.hlj.adapter.SelectCityAdapter
import com.hlj.common.CONST
import com.hlj.common.ColumnData
import com.hlj.dto.AgriDto
import com.hlj.dto.CityDto
import com.hlj.dto.FactDto
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.hlj.view.wheelview.NumericWheelAdapter
import com.hlj.view.wheelview.OnWheelScrollListener
import com.hlj.view.wheelview.WheelView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_fact_monitor.*
import kotlinx.android.synthetic.main.dialog_fact_history.view.*
import kotlinx.android.synthetic.main.layout_date.*
import kotlinx.android.synthetic.main.layout_fact_value.view.*
import kotlinx.android.synthetic.main.layout_select_city.*
import kotlinx.android.synthetic.main.layout_title.*
import net.sourceforge.pinyin4j.PinyinHelper
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

/**
 * 自动站实况监测
 */
class FactMonitorActivity : BaseFragmentActivity(), View.OnClickListener, AMap.OnCameraChangeListener {

    private var aMap: AMap? = null //高德地图
    private var zoom = 11.8f
    private var factAdapter: FactMonitorAdapter? = null
    private val factList: MutableList<FactDto> = ArrayList()
    private val markers: MutableList<Marker> = ArrayList() //市县名称
    private val cityInfos: MutableList<FactDto> = ArrayList() //城市信息
    private val timeList: MutableList<FactDto> = ArrayList() //时间列表
    private val realDatas: MutableList<FactDto?> = ArrayList() //全省站点列表
    private val sdf1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    private val sdf2 = SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss", Locale.CHINA)

    private var b1 = false
    private var b2 = false
    private var b3 = false //false为将序，true为升序
    private var mAdapter: FactDetailAdapter? = null
    private val detailList: MutableList<FactDto?> = ArrayList() //全省站点列表

    private var stationName = ""
    private var area = ""
    private var `val` = ""
    private var timeString = ""
    private var childId = ""
    private var childDataUrl = ""
    private var factOverlay: GroundOverlay? = null

    private var isStart = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fact_monitor)
        initAmap(savedInstanceState)
        initWidget()
        initWheelView()
        initListViewRank()
        initListView()
        initListViewCity()
    }

    /**
     * 初始化高德地图
     */
    private fun initAmap(bundle: Bundle?) {
        mapView.visibility = View.VISIBLE
        mapView.onCreate(bundle)
        if (aMap == null) {
            aMap = mapView.map
        }
        aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(CONST.guizhouLatLng, zoom))
        aMap!!.uiSettings.isMyLocationButtonEnabled = false // 设置默认定位按钮是否显示
        aMap!!.uiSettings.isZoomControlsEnabled = false
        aMap!!.uiSettings.isRotateGesturesEnabled = false
        aMap!!.showMapText(false)
        aMap!!.setOnCameraChangeListener(this)
        tvMapNumber.text = aMap!!.mapContentApprovalNumber
        aMap!!.setOnMapTouchListener { arg0 ->
            if (scrollView != null) {
                if (arg0.action == MotionEvent.ACTION_UP) {
                    scrollView!!.requestDisallowInterceptTouchEvent(false)
                } else {
                    scrollView!!.requestDisallowInterceptTouchEvent(true)
                }
            }
        }
//        aMap!!.setOnMapLoadedListener { CommonUtil.drawHLJJson(this, aMap) }
    }

    private fun initWidget() {
        llBack.setOnClickListener(this)
        ivLegend.setOnClickListener(this)
        tvDetail.setOnClickListener(this)
        tvHistory.setOnClickListener(this)
        ivCheck.setOnClickListener(this)
        tvList.setOnClickListener(this)

        ll1.setOnClickListener(this)
        ll2.setOnClickListener(this)
        ll3.setOnClickListener(this)
        if (!b3) { //将序
            iv3.setImageResource(R.drawable.arrow_down)
        } else { //将序
            iv3.setImageResource(R.drawable.arrow_up)
        }
        iv3.visibility = View.VISIBLE

        val title = intent.getStringExtra(CONST.ACTIVITY_NAME)
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }
        addColumn()
        val columnId = intent.getStringExtra(CONST.COLUMN_ID)
        CommonUtil.submitClickCount(columnId, title)
    }

    private fun initListViewRank() {
        mAdapter = FactDetailAdapter(this, detailList)
        listViewRank!!.adapter = mAdapter
        listViewRank!!.onItemClickListener = AdapterView.OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val dto = detailList[arg2]
            val intent = Intent(this, FactDetailChartActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("data", dto)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    /**
     * 返回中文的首字母
     * @param str
     * @return
     */
    fun getPinYinHeadChar(str: String): String {
        var convert = ""
        var size = str.length
        if (size >= 2) {
            size = 2
        }
        for (j in 0 until size) {
            val word = str[j]
            val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word)
            convert += if (pinyinArray != null) {
                pinyinArray[0][0]
            } else {
                word
            }
        }
        return convert
    }

    private fun initListView() {
        factAdapter = FactMonitorAdapter(this, factList)
        listView.adapter = factAdapter
    }

    /**
     * 添加子栏目
     */
    private fun addColumn() {
        if (intent.hasExtra("data")) {
            val data: AgriDto = intent.getParcelableExtra("data")
            if (data != null) {
                llContainer!!.removeAllViews()
                llContainer1.removeAllViews()
                val size = data.child.size
                for (i in 0 until size) {
                    val dto = data.child[i]
                    val tvName = TextView(this)
                    tvName.text = dto.name
                    tvName.gravity = Gravity.CENTER
                    tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                    tvName.setPadding(25, 0, 25, 0)
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.leftMargin = CommonUtil.dip2px(this, 10f).toInt()
                    tvName.layoutParams = params
                    llContainer!!.addView(tvName)
                    if (i == 0) {
                        tvName.setTextColor(Color.WHITE)
                        tvName.setBackgroundResource(R.drawable.corner_left_right_blue)
                        addItem(dto)
                    } else {
                        tvName.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                        tvName.setBackgroundResource(R.drawable.corner_left_right_gray)
                    }

                    tvName.setOnClickListener { arg0 ->
                        if (llContainer != null) {
                            for (n in 0 until llContainer!!.childCount) {
                                val name = llContainer!!.getChildAt(n) as TextView
                                if (TextUtils.equals(tvName.text.toString(), name.text.toString())) {
                                    name.setTextColor(Color.WHITE)
                                    name.setBackgroundResource(R.drawable.corner_left_right_blue)
                                    addItem(dto)
                                } else {
                                    name.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                                    name.setBackgroundResource(R.drawable.corner_left_right_gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addItem(dto: ColumnData?) {
        llContainer1.removeAllViews()
        tvFactRank.text = "实况排名-${dto!!.name}"
        for (j in 0 until dto!!.child.size) {
            val item = dto!!.child[j]
            val tvItem = TextView(this)
            tvItem.text = item.name
            tvItem.tag = item.id+"---"+item.dataUrl
            tvItem.gravity = Gravity.CENTER
            tvItem.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f)
            tvItem.setPadding(25, 0, 25, 0)
            val params1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params1.leftMargin = CommonUtil.dip2px(this, 10f).toInt()
            tvItem.layoutParams = params1
            llContainer1.addView(tvItem)
            if (j == 0) {
                if (TextUtils.equals(dto.name, "降水")) {
                    ivCheck.visibility = View.VISIBLE
                } else {
                    ivCheck.visibility = View.GONE
                }
                tvItem.setTextColor(Color.WHITE)
                tvItem.setBackgroundResource(R.drawable.corner_left_right_blue)
                childId = item.id
                childDataUrl = item.dataUrl
                okHttpFact()
            } else {
                tvItem.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                tvItem.setBackgroundResource(R.drawable.corner_left_right_gray)
            }

            tvItem.setOnClickListener { arg0 ->
                if (TextUtils.equals(tvItem.text.toString(), "降水")) {
                    ivCheck.visibility = View.VISIBLE
                } else {
                    ivCheck.visibility = View.GONE
                }
                val itemTag = arg0.tag.toString()
                if (!itemTag.contains("---")) {
                    return@setOnClickListener
                }
                val tagArray = itemTag.split("---")
                for (m in 0 until llContainer1.childCount) {
                    val itemName = llContainer1.getChildAt(m) as TextView
                    if (TextUtils.equals(itemName.tag.toString(), itemTag)) {
                        itemName.setTextColor(Color.WHITE)
                        itemName.setBackgroundResource(R.drawable.corner_left_right_blue)
                        childId = tagArray[0]
                        childDataUrl = tagArray[1]
                        okHttpFact()
                    } else {
                        itemName.setTextColor(ContextCompat.getColor(this, R.color.text_color4))
                        itemName.setBackgroundResource(R.drawable.corner_left_right_gray)
                    }
                }
            }
        }

        tvList.setOnClickListener {
            val intent = Intent(this, FactDetailActivity::class.java)
            intent.putExtra(CONST.ACTIVITY_NAME, tvFactRank.text.toString())
            intent.putExtra("childId", childId)
            val bundle = Bundle()
            bundle.putParcelable("data", dto)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    /**
     * 获取实况信息
     */
    private fun okHttpFact() {
        if (TextUtils.isEmpty(childDataUrl) || TextUtils.isEmpty(childId)) {
            return
        }
        showDialog()
        Thread {
            try {
                Log.e("okHttpFact", childDataUrl)
                OkHttpUtil.enqueue(Request.Builder().url(childDataUrl).build(), object : Callback {
                    override fun onFailure(call: Call, e: IOException) {}

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (!response.isSuccessful) {
                            return
                        }
                        val result = response.body!!.string()
                        runOnUiThread {
                            scrollView.visibility = View.VISIBLE
                            if (!TextUtils.isEmpty(result)) {
                                try {
                                    val obj = JSONObject(result)
                                    if (!obj.isNull("zx")) {
                                        val zx = obj.getString("zx")
                                        if (zx != null) {
                                            tvIntro.text = zx
                                        }
                                    }
                                    if (!obj.isNull("updatetime")) {
                                        val updatetime = obj.getString("updatetime")
                                        if (updatetime != null) {
                                            tvTime.text = updatetime+"更新"
                                            tvStartTime.text = sdf2.format(sdf1.parse(updatetime).time-1000*60*60)
                                            tvEndTime.text = sdf2.format(sdf1.parse(updatetime))
                                        }
                                    }
                                    if (!obj.isNull("title")) {
                                        val title = obj.getString("title")
                                        if (title != null) {
                                            tvLayerName!!.text = obj.getString("title")
                                        }
                                    }
                                    if (!obj.isNull("time")) {
                                        val time = obj.getString("time")
                                        if (time != null) {
                                            tvLayerName!!.text = tvLayerName!!.text.toString()+"\n"+time
                                        }
                                    }
                                    if (!obj.isNull("cutlineUrl")) {
                                        val imgUrl = obj.getString("cutlineUrl")
                                        if (!TextUtils.isEmpty(imgUrl)) {
                                            Picasso.get().load(imgUrl).into(ivChart)
                                        }
                                    }
                                    if (!obj.isNull("zh")) {//listview标题
                                        val itemObj = obj.getJSONObject("zh")
                                        if (!itemObj.isNull("stationName")) {
                                            tv33.text = itemObj.getString("stationName")
                                        }
                                        if (!itemObj.isNull("area")) {
                                            tv22.text = itemObj.getString("area")
                                        }
                                        if (!itemObj.isNull("val")) {
                                            tv11!!.text = itemObj.getString("val")
                                        }
                                    }
                                    if (!obj.isNull("t")) {//城市信息
                                        cityInfos.clear()
                                        val array = obj.getJSONArray("t")
                                        for (i in 0 until array.length()) {
                                            val f = FactDto()
                                            val o = array.getJSONObject(i)
                                            if (!o.isNull("name")) {
                                                f.name = o.getString("name")
                                                f.stationName = o.getString("name")
                                            }
                                            if (!o.isNull("lon")) {
                                                f.lng = o.getString("lon").toDouble()
                                            }
                                            if (!o.isNull("lat")) {
                                                f.lat = o.getString("lat").toDouble()
                                            }
                                            if (!o.isNull("value")) {
                                                f.`val` = o.getString("value").toDouble()
                                            }
                                            if (!o.isNull("val1")) {
                                                f.val1 = o.getString("val1").toDouble()
                                            }
                                            cityInfos.add(f)
                                        }
                                    }

                                    if (!obj.isNull("img")) {
                                        val imgObj = obj.getJSONObject("img")
                                        okHttpFactBitmap(imgObj)
                                    }

                                    //详情开始
                                    if (!obj.isNull("th")) {
                                        val itemObj = obj.getJSONObject("th")
                                        if (!itemObj.isNull("stationName")) {
                                            stationName = itemObj.getString("stationName")
                                            if (stationName != null) {
                                                tv1.text = stationName
                                            }
                                        }
                                        if (!itemObj.isNull("area")) {
                                            area = itemObj.getString("area")
                                            if (area != null) {
                                                tv2.text = area
                                            }
                                        }
                                        if (!itemObj.isNull("val")) {
                                            `val` = itemObj.getString("val")
                                            if (`val` != null) {
                                                tv3.text = `val`
                                            }
                                        }
                                    }
                                    if (!obj.isNull("realDatas")) {
                                        realDatas.clear()
                                        detailList.clear()
                                        val array = JSONArray(obj.getString("realDatas"))
                                        for (i in 0 until array.length()) {
                                            val itemObj = array.getJSONObject(i)
                                            val dto = FactDto()
                                            if (!itemObj.isNull("stationCode")) {
                                                dto.stationCode = itemObj.getString("stationCode")
                                            }
                                            if (!itemObj.isNull("stationName")) {
                                                dto.stationName = itemObj.getString("stationName")
                                            }
                                            if (!itemObj.isNull("area")) {
                                                dto.area = itemObj.getString("area")
                                            }
                                            if (!itemObj.isNull("area1")) {
                                                dto.area1 = itemObj.getString("area1")
                                            }
                                            if (!itemObj.isNull("val")) {
                                                dto.`val` = itemObj.getDouble("val")
                                                //图例
                                                if (!obj.isNull("tl_config")) {
                                                    val tlArray = obj.getJSONArray("tl_config")
                                                    for (l in 0 until tlArray.length()) {
                                                        val tl = tlArray.getString(l).split(",")
                                                        if (dto.`val` >= tl[0].toDouble() && dto.`val` < tl[1].toDouble()) {
                                                            dto.bgColor = Color.parseColor(tl[2])
                                                            dto.lineColor = Color.parseColor(tl[3])
                                                        }
                                                    }
                                                }
                                                //图例
                                            }
                                            if (!itemObj.isNull("val1")) {
                                                dto.val1 = itemObj.getDouble("val1")
                                            }
                                            if (!itemObj.isNull("Lon")) {
                                                dto.lng = itemObj.getString("Lon").toDouble()
                                            }
                                            if (!itemObj.isNull("Lat")) {
                                                dto.lat = itemObj.getString("Lat").toDouble()
                                            }
                                            if (!TextUtils.isEmpty(dto.stationName) && !TextUtils.isEmpty(dto.area)) {
                                                realDatas.add(dto)
                                                if (i < 5) {
                                                    detailList.add(dto)
                                                }
                                            }
                                        }
                                        if (mAdapter != null) {
                                            mAdapter!!.notifyDataSetChanged()
                                        }
                                    }
                                    if (!obj.isNull("times")) {
                                        timeList.clear()
                                        val timeArray = obj.getJSONArray("times")
                                        for (i in 0 until timeArray.length()) {
                                            val f = FactDto()
                                            val fo = timeArray.getJSONObject(i)
                                            if (!fo.isNull("timeString")) {
                                                f.timeString = fo.getString("timeString")
                                            }
                                            if (!fo.isNull("timestart")) {
                                                f.timeStart = fo.getString("timestart")
                                            }
                                            if (!fo.isNull("timeParams")) {
                                                f.timeParams = fo.getString("timeParams")
                                            }
                                            timeList.add(f)
                                            if (i == 0) {
                                                timeString = f.timeString
                                            }
                                        }
                                    }
                                    //详情结束
                                    if (!obj.isNull("jb")) {
                                        factList.clear()
                                        val array = obj.getJSONArray("jb")
                                        for (i in 0 until array.length()) {
                                            val itemObj = array.getJSONObject(i)
                                            val data = FactDto()
                                            if (!itemObj.isNull("lv")) {
                                                data.rainLevel = itemObj.getString("lv")
                                            }
                                            if (!itemObj.isNull("count")) {
                                                data.count = itemObj.getInt("count").toString()
                                            }
                                            if (!itemObj.isNull("xs")) {
                                                val xsArray = itemObj.getJSONArray("xs")
                                                val list: MutableList<FactDto> = ArrayList()
                                                list.clear()
                                                for (j in 0 until xsArray.length()) {
                                                    val d = FactDto()
                                                    d.area = xsArray.getString(j)
                                                    list.add(d)
                                                }
                                                data.areaList.addAll(list)
                                            }
                                            factList.add(data)
                                        }
                                        if (factList.size > 0 && factAdapter != null) {
                                            factAdapter!!.timeString = timeString
                                            factAdapter!!.stationName = stationName
                                            factAdapter!!.area = area
                                            factAdapter!!.`val` = `val`
                                            factAdapter!!.realDatas.clear()
                                            factAdapter!!.realDatas.addAll(realDatas)
                                            factAdapter!!.notifyDataSetChanged()
                                            listTitle!!.visibility = View.VISIBLE
                                            listView!!.visibility = View.VISIBLE
                                            tvDetail!!.visibility = View.VISIBLE
                                            tvHistory.visibility = View.VISIBLE
                                        }
                                    } else {
                                        listTitle!!.visibility = View.GONE
                                        listView!!.visibility = View.GONE
                                        tvDetail!!.visibility = View.GONE
                                        tvHistory.visibility = View.GONE
                                    }
                                    tvLayerName!!.isFocusable = true
                                    tvLayerName!!.isFocusableInTouchMode = true
                                    tvLayerName!!.requestFocus()
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                })
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }.start()
    }

    /**
     * 获取并绘制图层
     */
    private fun okHttpFactBitmap(imgObj: JSONObject) {
        Thread {
            try {
                val maxlat = imgObj.getDouble("maxlat")
                val maxlon = imgObj.getDouble("maxlon")
                val minlat = imgObj.getDouble("minlat")
                val minlon = imgObj.getDouble("minlon")
                val imgurl = imgObj.getString("imgurl")
                OkHttpUtil.enqueue(Request.Builder().url(imgurl).build(), object : Callback {
                    override fun onFailure(call: Call, e: IOException) {}

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (!response.isSuccessful) {
                            return
                        }
                        val bytes = response.body!!.bytes()
                        runOnUiThread {
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            if (bitmap != null) {
                                drawFactBitmap(bitmap, LatLng(maxlat, maxlon), LatLng(minlat, minlon))
                            }
                        }
                    }
                })
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }.start()
    }

    /**
     * 绘制实况图
     */
    private fun drawFactBitmap(bitmap: Bitmap?, max: LatLng, min: LatLng) {
        if (bitmap == null) {
            return
        }
        val fromView = BitmapDescriptorFactory.fromBitmap(bitmap)
        val bounds = LatLngBounds.Builder()
                .include(max)
                .include(min)
                .build()

//        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        if (factOverlay == null) {
            factOverlay = aMap!!.addGroundOverlay(GroundOverlayOptions()
                    .anchor(0.5f, 0.5f)
                    .positionFromBounds(bounds)
                    .image(fromView)
                    .transparency(0.2f))
        } else {
            factOverlay!!.setImage(null)
            factOverlay!!.setPositionFromBounds(bounds)
            factOverlay!!.setImage(fromView)
        }
        drawDataToMap()
    }

    /**
     * 清除站点信息
     */
    private fun removeMarkers() {
        for (i in markers.indices) {
            markers[i].remove()
        }
        markers.clear()
    }

    /**
     * 绘制图层
     */
    private fun drawDataToMap() {
        if (aMap == null) {
            return
        }
        removeMarkers()
        drawAllDistrict()
        cancelDialog()
    }

    /**
     * 绘制广西市县边界
     */
    private fun drawAllDistrict() {
        handler.removeMessages(1001)
        val msg = handler.obtainMessage()
        msg.what = 1001
        msg.obj = zoom
        handler.sendMessageDelayed(msg, 1000)
    }

    override fun onCameraChange(arg0: CameraPosition?) {}

    override fun onCameraChangeFinish(arg0: CameraPosition) {
//        val dm = DisplayMetrics()
//        windowManager.defaultDisplay.getMetrics(dm)
//        val leftPoint = Point(0, dm.heightPixels)
//        val rightPoint = Point(dm.widthPixels, 0)
//        val leftlatlng = aMap!!.projection.fromScreenLocation(leftPoint)
//        val rightLatlng = aMap!!.projection.fromScreenLocation(rightPoint)
//        Log.e("leftlatlng", ""+(leftlatlng.latitude+rightLatlng.latitude)/2+","+(leftlatlng.longitude+rightLatlng.longitude)/2+","+zoom)

        zoom = arg0.zoom
        handler.removeMessages(1001)
        val msg = handler.obtainMessage()
        msg.what = 1001
        handler.sendMessageDelayed(msg, 1000)
    }

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                1001 -> {
                    removeMarkers()
                    if (zoom <= 7.5f) {
                        addCityMarkers()
                    } else if (zoom <= 8.2f) { //乡镇站点
                        addCityMarkers()
                        addAutoMarkers()
                    } else {
                        addCityMarkers()
                        addAutoMarkers()
                        addAutoHMarkers()
                    }
                }
            }
        }
    }

    private fun addCityMarkers() {
        //绘制人工站
        for (i in cityInfos.indices) {
            val dto = cityInfos[i]
            addSingleMarker(dto)
        }
    }

    private fun addAutoMarkers() {
        for (i in realDatas.indices) {
            val dto = realDatas[i]
            if (!dto!!.stationCode.startsWith("Y")) {
                addSingleMarker(dto)
            }
        }
    }

    private fun addAutoHMarkers() {
        for (i in realDatas.indices) {
            val dto = realDatas[i]
            if (dto!!.stationCode.startsWith("Y")) {
                addSingleMarker(dto)
            }
        }
    }

    private fun addSingleMarker(dto: FactDto) {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_fact_value, null)
        if (dto.`val` >= 99999) {
            view.tvValue.text = ""
        } else {
            var unit = ""
            when {
                tvFactRank.text.toString().endsWith("降水") -> {
                    unit = getString(R.string.unit_mm)
                }
                tvFactRank.text.toString().endsWith("气温") -> {
                    unit = getString(R.string.unit_degree)
                }
                tvFactRank.text.toString().endsWith("风速风向") -> {
                    unit = getString(R.string.unit_speed)
                }
                tvFactRank.text.toString().endsWith("相对湿度") -> {
                    unit = getString(R.string.unit_percent)
                }
                tvFactRank.text.toString().endsWith("能见度") -> {
                    unit = getString(R.string.unit_km)
                }
            }
            view.tvValue.setBgColor(dto.bgColor, Color.WHITE)
            view.tvValue.setTextColor(dto.lineColor)
            view.tvValue.text = dto.`val`.toString()+unit
            if (dto.val1 != -1.0) {
                val b = CommonUtil.getWindMarker(this, dto.`val`)
                if (b != null) {
                    val matrix = Matrix()
                    matrix.postScale(1f, 1f)
                    matrix.postRotate(dto.val1.toFloat())
                    val bitmap = Bitmap.createBitmap(b, 0, 0, b.width, b.height, matrix, true)
                    if (bitmap != null) {
                        view.ivWind.setImageBitmap(bitmap)
                        view.ivWind.visibility = View.VISIBLE
                    }
                }
            }
            if (!TextUtils.isEmpty(dto.stationCode)) {
                view.tvName.text = dto.stationCode
            }
            val options = MarkerOptions()
            options.title(dto.stationName)
            options.anchor(0.5f, 0.5f)
            options.position(LatLng(dto.lat, dto.lng))
            options.icon(BitmapDescriptorFactory.fromView(view))
            val marker = aMap!!.addMarker(options)
            marker.isVisible = true
            markers.add(marker)
        }
    }

    private fun dialogHistory() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_fact_history, null)
        val mAdapter = FactTimeAdapter(this, timeList)
        view.listView.adapter = mAdapter
        val dialog = Dialog(this, R.style.CustomProgressDialog)
        dialog.setContentView(view)
        dialog.show()
        view.listView.onItemClickListener = AdapterView.OnItemClickListener { arg0, arg1, arg2, arg3 ->
            dialog.dismiss()
            val dto = timeList[arg2]
            okHttpFact()
        }
        view.tvNegative.setOnClickListener { dialog.dismiss() }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.llBack -> finish()
            R.id.ivLegend -> {
                if (ivChart.visibility == View.VISIBLE) {
                    ivChart.visibility = View.GONE
                } else {
                    ivChart.visibility = View.VISIBLE
                }
            }
            R.id.ivCheck -> {
                if (llCheck.visibility == View.VISIBLE) {
                    llCheck.visibility = View.GONE
                } else {
                    llCheck.visibility = View.VISIBLE
                }
            }
            R.id.ll1 -> {
                if (b1) { //升序
                    b1 = false
                    iv1!!.setImageResource(R.drawable.arrow_up)
                    iv1!!.visibility = View.VISIBLE
                    iv2.visibility = View.INVISIBLE
                    iv3.visibility = View.INVISIBLE
                    realDatas.sortWith(Comparator { arg0, arg1 ->
                        if (TextUtils.isEmpty(arg0!!.stationName) || TextUtils.isEmpty(arg1!!.stationName)) {
                            0
                        } else {
                            getPinYinHeadChar(arg0!!.stationName).compareTo(getPinYinHeadChar(arg1!!.stationName))
                        }
                    })
                } else { //将序
                    b1 = true
                    iv1!!.setImageResource(R.drawable.arrow_down)
                    iv1!!.visibility = View.VISIBLE
                    iv2.visibility = View.INVISIBLE
                    iv3.visibility = View.INVISIBLE
                    realDatas.sortWith(Comparator { arg0, arg1 ->
                        if (TextUtils.isEmpty(arg0!!.stationName) || TextUtils.isEmpty(arg1!!.stationName)) {
                            -1
                        } else {
                            getPinYinHeadChar(arg1!!.stationName).compareTo(getPinYinHeadChar(arg0!!.stationName))
                        }
                    })
                }
                if (mAdapter != null) {
                    mAdapter!!.notifyDataSetChanged()
                }
            }
            R.id.ll2 -> {
                if (b2) { //升序
                    b2 = false
                    iv2.setImageResource(R.drawable.arrow_up)
                    iv1!!.visibility = View.INVISIBLE
                    iv2.visibility = View.VISIBLE
                    iv3.visibility = View.INVISIBLE
                    realDatas.sortWith(Comparator { arg0, arg1 ->
                        if (TextUtils.isEmpty(arg0!!.area) || TextUtils.isEmpty(arg1!!.area)) {
                            0
                        } else {
                            getPinYinHeadChar(arg0!!.area).compareTo(getPinYinHeadChar(arg1!!.area))
                        }
                    })
                } else { //将序
                    b2 = true
                    iv2.setImageResource(R.drawable.arrow_down)
                    iv1!!.visibility = View.INVISIBLE
                    iv2.visibility = View.VISIBLE
                    iv3.visibility = View.INVISIBLE
                    realDatas.sortWith(Comparator { arg0, arg1 ->
                        if (TextUtils.isEmpty(arg0!!.area) || TextUtils.isEmpty(arg1!!.area)) {
                            -1
                        } else {
                            getPinYinHeadChar(arg1!!.area).compareTo(getPinYinHeadChar(arg0!!.area))
                        }
                    })
                }
                if (mAdapter != null) {
                    mAdapter!!.notifyDataSetChanged()
                }
            }
            R.id.ll3 -> {
                if (b3) { //升序
                    b3 = false
                    iv3.setImageResource(R.drawable.arrow_up)
                    iv1!!.visibility = View.INVISIBLE
                    iv2.visibility = View.INVISIBLE
                    iv3.visibility = View.VISIBLE
                    realDatas.sortWith(Comparator { arg0, arg1 -> java.lang.Double.valueOf(arg0!!.`val`).compareTo(java.lang.Double.valueOf(arg1!!.`val`)) })
                } else { //将序
                    b3 = true
                    iv3.setImageResource(R.drawable.arrow_down)
                    iv1!!.visibility = View.INVISIBLE
                    iv2.visibility = View.INVISIBLE
                    iv3.visibility = View.VISIBLE
                    realDatas.sortWith(Comparator { arg0, arg1 -> java.lang.Double.valueOf(arg1!!.`val`).compareTo(java.lang.Double.valueOf(arg0!!.`val`)) })
                }
                if (mAdapter != null) {
                    mAdapter!!.notifyDataSetChanged()
                }
            }
            R.id.tvDetail -> {
                val intent = Intent(this, FactDetailActivity::class.java)
                intent.putExtra("title", "详情数据")
                intent.putExtra("timeString", timeString)
                intent.putExtra("stationName", stationName)
                intent.putExtra("area", area)
                intent.putExtra("val", `val`)
                val bundle = Bundle()
                bundle.putParcelableArrayList("realDatas", realDatas as ArrayList<out Parcelable?>)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            R.id.tvHistory -> dialogHistory()
            R.id.llCity, R.id.ivCloseCity -> bootTimeLayoutAnimation(layoutCity)
            R.id.tvStartTime -> {
                isStart = true
                bootTimeLayoutAnimation(layoutDate)
            }
            R.id.tvNegtive -> bootTimeLayoutAnimation(layoutDate)
            R.id.tvPositive -> {
                setTextViewValue()
                bootTimeLayoutAnimation(layoutDate)
            }
            R.id.tvEndTime -> {
                isStart = false
                bootTimeLayoutAnimation(layoutDate)
            }
            R.id.tvCheck -> {
                val start = sdf2.parse(tvStartTime.text.toString())
                val end = sdf2.parse(tvEndTime.text.toString())
                if (start > end) {
                    Toast.makeText(this, "开始时间不能大于结束时间", Toast.LENGTH_SHORT).show()
                } else {
                    val endTime = sdf1.format(sdf2.parse(tvEndTime.text.toString()))
                    okHttpFact()
                }
            }
        }
    }

    private fun initWheelView() {
        tvStartTime.setOnClickListener(this)
        tvEndTime.setOnClickListener(this)
        tvNegtive.setOnClickListener(this)
        tvPositive.setOnClickListener(this)
        tvCheck.setOnClickListener(this)

        val c = Calendar.getInstance()
        val curYear = c[Calendar.YEAR]
        val curMonth = c[Calendar.MONTH] + 1 //通过Calendar算出的月数要+1
        val curDate = c[Calendar.DATE]
        val curHour = c[Calendar.HOUR_OF_DAY]
        val curMinute = c[Calendar.MINUTE]
        val curSecond = c[Calendar.SECOND]

        val numericWheelAdapter1 = NumericWheelAdapter(this, 1950, curYear)
        numericWheelAdapter1.setLabel("年")
        year.viewAdapter = numericWheelAdapter1
        year.isCyclic = false //是否可循环滑动
        year.addScrollingListener(scrollListener)
        year.visibleItems = 7
        year.visibility = View.VISIBLE

        val numericWheelAdapter2 = NumericWheelAdapter(this, 1, 12, "%02d")
        numericWheelAdapter2.setLabel("月")
        month.viewAdapter = numericWheelAdapter2
        month.isCyclic = false
        month.addScrollingListener(scrollListener)
        month.visibleItems = 7
        month.visibility = View.VISIBLE

        initDay(curYear, curMonth)
        day.isCyclic = false
        day.visibleItems = 7
        day.visibility = View.VISIBLE

        val numericWheelAdapter3 = NumericWheelAdapter(this, 0, 23, "%02d")
        numericWheelAdapter3.setLabel("时")
        hour.viewAdapter = numericWheelAdapter3
        hour.isCyclic = false
        hour.addScrollingListener(scrollListener)
        hour.visibleItems = 7
        hour.visibility = View.VISIBLE

        val numericWheelAdapter4 = NumericWheelAdapter(this, 0, 59, "%02d")
        numericWheelAdapter4.setLabel("分")
        minute.viewAdapter = numericWheelAdapter4
        minute.isCyclic = false
        minute.addScrollingListener(scrollListener)
        minute.visibleItems = 7
        minute.visibility = View.VISIBLE

        val numericWheelAdapter5 = NumericWheelAdapter(this, 0, 59, "%02d")
        numericWheelAdapter5.setLabel("秒")
        second.viewAdapter = numericWheelAdapter5
        second.isCyclic = false
        second.addScrollingListener(scrollListener)
        second.visibleItems = 7
        second.visibility = View.VISIBLE

        year.currentItem = curYear - 1950
        month.currentItem = curMonth - 1
        day.currentItem = curDate - 1
        hour.currentItem = curHour
        minute.currentItem = curMinute
        second.currentItem = curSecond
    }

    private val scrollListener: OnWheelScrollListener = object : OnWheelScrollListener {
        override fun onScrollingStarted(wheel: WheelView) {}
        override fun onScrollingFinished(wheel: WheelView) {
            val nYear = year!!.currentItem + 1950 //年
            val nMonth: Int = month.currentItem + 1 //月
            initDay(nYear, nMonth)
        }
    }

    /**
     */
    private fun initDay(arg1: Int, arg2: Int) {
        val numericWheelAdapter = NumericWheelAdapter(this, 1, getDay(arg1, arg2), "%02d")
        numericWheelAdapter.setLabel("日")
        day.viewAdapter = numericWheelAdapter
    }

    /**
     *
     * @param year
     * @param month
     * @return
     */
    private fun getDay(year: Int, month: Int): Int {
        var day = 30
        var flag = false
        flag = when (year % 4) {
            0 -> true
            else -> false
        }
        day = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            2 -> if (flag) 29 else 28
            else -> 30
        }
        return day
    }

    /**
     */
    private fun setTextViewValue() {
        val yearStr = (year!!.currentItem + 1950).toString()
        val monthStr = if (month.currentItem + 1 < 10) "0" + (month.currentItem + 1) else (month.currentItem + 1).toString()
        val dayStr = if (day.currentItem + 1 < 10) "0" + (day.currentItem + 1) else (day.currentItem + 1).toString()
        val hourStr = if (hour.currentItem + 1 < 10) "0" + (hour.currentItem) else (hour.currentItem).toString()
        val minuteStr = if (minute.currentItem + 1 < 10) "0" + (minute.currentItem) else (minute.currentItem).toString()
        val secondStr = if (second.currentItem + 1 < 10) "0" + (second.currentItem) else (second.currentItem).toString()
        if (isStart) {
            tvStartTime.text = "$yearStr-$monthStr-$dayStr\n${hourStr}:${minuteStr}:${secondStr}"
        } else {
            tvEndTime.text = "$yearStr-$monthStr-$dayStr\n${hourStr}:${minuteStr}:${secondStr}"
        }
    }

    private fun bootTimeLayoutAnimation(view: View) {
        if (view!!.visibility == View.GONE) {
            timeLayoutAnimation(true, view)
            view!!.visibility = View.VISIBLE
        } else {
            timeLayoutAnimation(false, view)
            view!!.visibility = View.GONE
        }
    }

    /**
     * 时间图层动画
     * @param flag
     * @param view
     */
    private fun timeLayoutAnimation(flag: Boolean, view: View?) {
        //列表动画
        val animationSet = AnimationSet(true)
        val animation: TranslateAnimation = if (!flag) {
            TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 1f)
        } else {
            TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 1f,
                    Animation.RELATIVE_TO_SELF, 0f)
        }
        animation.duration = 200
        animationSet.addAnimation(animation)
        animationSet.fillAfter = true
        view!!.startAnimation(animationSet)
        animationSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                view.clearAnimation()
            }
        })
    }

    private fun initListViewCity() {
        llCity.setOnClickListener(this)
        ivCloseCity.setOnClickListener(this)
        okHttpCityList()
        cityAdapter = SelectCityAdapter(this, cityList)
        listViewCity.adapter = cityAdapter
        listViewCity.setOnItemClickListener { parent, view, position, id ->
            bootTimeLayoutAnimation(layoutCity)
            val data: CityDto = cityList[position]
            if (data.areaName != null) {
                tvCity.text = data.areaName
            }
        }
    }

    private var cityAdapter: SelectCityAdapter? = null
    private val cityList: MutableList<CityDto> = ArrayList()
    private fun okHttpCityList() {
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
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                cityList.clear()
                                val array = JSONArray(result)
                                for (i in 0 until array.length()) {
                                    val dto = CityDto()
                                    val itemObj = array.getJSONObject(i)
                                    if (!itemObj.isNull("name")) {
                                        dto.areaName = itemObj.getString("name")
                                        if (i == 0) {
                                            tvCity.text = dto.areaName
                                        }
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
                                    cityList.add(dto)
                                }
                                if (cityAdapter != null) {
                                    cityAdapter!!.notifyDataSetChanged()
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
