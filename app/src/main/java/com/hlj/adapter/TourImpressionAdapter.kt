package com.hlj.adapter

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.hlj.dto.NewsDto
import com.hlj.utils.CommonUtil
import net.tsz.afinal.FinalBitmap
import shawn.cxwl.com.hlj.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * 旅游气象-新疆印象
 */
class TourImpressionAdapter constructor(private val activity: Activity, private val mArrayList: ArrayList<NewsDto>?) : BaseAdapter() {

    private var mInflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val sdf1 = SimpleDateFormat("HH", Locale.CHINA)

    private class ViewHolder {
        var imageView: ImageView? = null
        var tvTitle: TextView? = null
        var tvDesc: TextView? = null
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
            convertView = mInflater!!.inflate(R.layout.adapter_tour_impression, null)
            mHolder = ViewHolder()
            mHolder.imageView = convertView.findViewById(R.id.imageView)
            mHolder.tvTitle = convertView.findViewById(R.id.tvTitle)
            mHolder.tvDesc = convertView.findViewById(R.id.tvDesc)
            convertView.tag = mHolder
        } else {
            mHolder = convertView.tag as ViewHolder
        }

        val dto = mArrayList!![position]

        if (dto.title != null) {
            mHolder.tvTitle!!.text = dto.title
        }
        if (dto.desc != null) {
            mHolder.tvDesc!!.text = dto.desc
        }
        if (!TextUtils.isEmpty(dto.imgUrl)) {
            val finalBitmap = FinalBitmap.create(activity)
            finalBitmap.display(mHolder.imageView, dto.imgUrl, null, CommonUtil.dip2px(activity, 5f).toInt())
        } else {
            mHolder.imageView!!.setImageResource(R.drawable.icon_no_bitmap)
        }
        return convertView
    }

}
