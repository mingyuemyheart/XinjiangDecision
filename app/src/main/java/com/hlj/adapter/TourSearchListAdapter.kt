package com.hlj.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.hlj.dto.NewsDto
import kotlinx.android.synthetic.main.fragment_warning.*
import shawn.cxwl.com.hlj.R

/**
 * 旅游气象-城市查询-关键字查询
 */
class TourSearchListAdapter constructor(private var context: Context?, private var mArrayList: ArrayList<NewsDto>) : BaseAdapter() {

    private val mInflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var key = ""
    fun setKey(key: String) {
        this.key = key
    }

    private class ViewHolder {
        var tvName: TextView? = null
    }

    override fun getCount(): Int {
        return mArrayList.size
    }

    override fun getItem(position: Int): Any? {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View? {
        val mHolder: ViewHolder
        var convertView = view
        if (convertView == null) {
            convertView = mInflater!!.inflate(R.layout.adapter_tour_search_list, null)
            mHolder = ViewHolder()
            mHolder.tvName = convertView.findViewById<View>(R.id.tvName) as TextView
            convertView.tag = mHolder
        } else {
            mHolder = convertView.tag as ViewHolder
        }

        val dto = mArrayList[position]

        if (dto.title != null) {
            mHolder.tvName!!.text = dto.title

            if (dto.title.contains(key)) {
                val index = dto.title.indexOf(key)
                if (index != -1) {
                    val builder = SpannableStringBuilder(dto.title)
                    val builderSpan1 = ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.text_color4))
                    val builderSpan2 = ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.colorPrimary))
                    val builderSpan3 = ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.text_color4))
                    builder.setSpan(builderSpan1, 0, index, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.setSpan(builderSpan2, index, index+key.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.setSpan(builderSpan3, index+key.length, dto.title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    mHolder.tvName!!.text = builder
                }
            }
        }

        return convertView
    }

}
