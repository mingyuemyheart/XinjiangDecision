package com.hlj.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.hlj.dto.NewsDto
import com.hlj.dto.WeatherDto
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.hlj.utils.WeatherUtil
import net.tsz.afinal.FinalBitmap
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
 * 旅游气象-景点天气
 */
class TourScenicAdapter constructor(private val activity: Activity, private val mArrayList: ArrayList<NewsDto>?) : BaseAdapter() {

    private var mInflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val sdf1 = SimpleDateFormat("HH", Locale.CHINA)

    private class ViewHolder {
        var imageView: ImageView? = null
        var tvTitle: TextView? = null
        var tvLevel: TextView? = null
        var tvAqi: TextView? = null
        var tvTemp: TextView? = null
        var tvWind: TextView? = null
        var ivPheDay: ImageView? = null
        var ivPheNight: ImageView? = null
        var ivWind: ImageView? = null
    }

    override fun getCount(): Int {
        return mArrayList!!.size
    }

    override fun getItem(position: Int): Any? {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View? {
        var convertView = view
        val mHolder: ViewHolder
        if (convertView == null) {
            convertView = mInflater!!.inflate(R.layout.adapter_tour_scenic, null)
            mHolder = ViewHolder()
            mHolder.imageView = convertView.findViewById(R.id.imageView)
            mHolder.tvTitle = convertView.findViewById(R.id.tvTitle)
            mHolder.tvLevel = convertView.findViewById(R.id.tvLevel)
            mHolder.tvAqi = convertView.findViewById(R.id.tvAqi)
            mHolder.tvTemp = convertView.findViewById(R.id.tvTemp)
            mHolder.tvWind = convertView.findViewById(R.id.tvWind)
            mHolder.ivPheDay = convertView.findViewById(R.id.ivPheDay)
            mHolder.ivPheNight = convertView.findViewById(R.id.ivPheNight)
            mHolder.ivWind = convertView.findViewById(R.id.ivWind)
            convertView.tag = mHolder
        } else {
            mHolder = convertView.tag as ViewHolder
        }

        val dto = mArrayList!![position]
        if (dto.level != null) {
            mHolder.tvLevel!!.text = dto.level
            mHolder.tvLevel!!.setBackgroundResource(R.drawable.corner_green)
        }
        if (dto.title != null) {
            mHolder.tvTitle!!.text = dto.title
        }
        if (!TextUtils.isEmpty(dto.imgUrl)) {
            val finalBitmap = FinalBitmap.create(activity)
            finalBitmap.display(mHolder.imageView, dto.imgUrl, null, CommonUtil.dip2px(activity, 5f).toInt())
        } else {
            mHolder.imageView!!.setImageResource(R.drawable.icon_no_bitmap)
        }
        getWeatherInfo(dto.cityId, mHolder.tvAqi, mHolder.ivPheDay, mHolder.ivPheNight, mHolder.tvTemp, mHolder.ivWind, mHolder.tvWind)
        return convertView
    }

    private fun getWeatherInfo(cityId: String, tvAqi: TextView?, ivPheDay: ImageView?, ivPheNight: ImageView?, tvTemp: TextView?, ivWind: ImageView?, tvWind: TextView?) {
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
                                                    try {
                                                        val aqiCount = Integer.valueOf(aqi)
                                                        tvAqi!!.setBackgroundResource(WeatherUtil.getAqiIcon(aqiCount))
                                                        if (aqiCount <= 300) {
                                                            tvAqi.setTextColor(Color.BLACK)
                                                        } else {
                                                            tvAqi.setTextColor(Color.WHITE)
                                                        }
                                                        tvAqi!!.text = aqi+" "+WeatherUtil.getAqi(activity, aqiCount)
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
                                        val `object` = forecast.getJSONObject("24h")
                                        if (!`object`.isNull(cityId)) {
                                            val object1 = `object`.getJSONObject(cityId)
                                            if (!object1.isNull("1001001")) {
                                                val f1 = object1.getJSONArray("1001001")
                                                if (f1.length() > 0) {
                                                    val dto = WeatherDto()
                                                    //预报内容
                                                    val weeklyObj = f1.getJSONObject(0)

                                                    //晚上
                                                    val two = weeklyObj.getString("002")
                                                    if (!TextUtils.isEmpty(two) && !TextUtils.equals(two, "?") && !TextUtils.equals(two, "null")) {
                                                        dto.lowPheCode = Integer.valueOf(two)
                                                    }
                                                    val four = weeklyObj.getString("004")
                                                    if (!TextUtils.isEmpty(two) && !TextUtils.equals(two, "?") && !TextUtils.equals(two, "null")) {
                                                        dto.lowTemp = Integer.valueOf(four)
                                                    }

                                                    //白天
                                                    val one = weeklyObj.getString("001")
                                                    if (!TextUtils.isEmpty(one) && !TextUtils.equals(one, "?") && !TextUtils.equals(one, "null")) {
                                                        dto.highPheCode = Integer.valueOf(one)
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

                                                    val dayBitmap = WeatherUtil.getBitmap(activity, dto.highPheCode)
                                                    if (dayBitmap != null) {
                                                        ivPheDay!!.setImageBitmap(dayBitmap)
                                                    }
                                                    val nightBitmap = WeatherUtil.getNightBitmap(activity, dto.lowPheCode)
                                                    if (nightBitmap != null) {
                                                        ivPheNight!!.setImageBitmap(nightBitmap)
                                                    }
                                                    tvTemp!!.text = "${dto.highTemp}/${dto.lowTemp}℃"

                                                    val dir = activity.getString(WeatherUtil.getWindDirection(dto.windDir))
                                                    tvWind!!.text = "$dir ${dto.windForceString}"
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
