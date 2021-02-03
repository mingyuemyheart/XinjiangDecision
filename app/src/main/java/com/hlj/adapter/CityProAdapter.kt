package com.hlj.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.hlj.dto.CityDto
import com.hlj.stickygridheaders.StickyGridHeadersSimpleAdapter
import shawn.cxwl.com.hlj.R

/**
 * 城市选择，省内热门
 */
class CityProAdapter constructor(context: Context?, private var mArrayList: MutableList<CityDto>) : BaseAdapter(), StickyGridHeadersSimpleAdapter {

    private val mInflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private class HeaderViewHolder {
        var tvHeader: TextView? = null
    }

    override fun getHeaderId(position: Int): Long {
        return mArrayList!![position].section.toLong()
    }

    override fun getHeaderView(position: Int, view: View?, parent: ViewGroup?): View? {
        var convertView = view
        val mHeaderHolder: HeaderViewHolder
        if (convertView == null) {
            mHeaderHolder = HeaderViewHolder()
            convertView = mInflater!!.inflate(R.layout.adapter_city_pro_header, null)
            mHeaderHolder.tvHeader = convertView.findViewById(R.id.tvHeader)
            convertView.tag = mHeaderHolder
        } else {
            mHeaderHolder = convertView.tag as HeaderViewHolder
        }

        val dto = mArrayList!![position]
        if (dto.sectionName != null) {
            mHeaderHolder.tvHeader!!.text = dto.sectionName
        }

        return convertView
    }

    private class ViewHolder {
        var tvName: TextView? = null
    }

    override fun getCount(): Int {
        return mArrayList!!.size
    }

    override fun getItem(position: Int): Any? {
        return mArrayList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View? {
        var convertView = view
        val mHolder: ViewHolder
        if (convertView == null) {
            mHolder = ViewHolder()
            convertView = mInflater!!.inflate(R.layout.adapter_city_pro_content, null)
            mHolder.tvName = convertView.findViewById(R.id.tvName)
            convertView.tag = mHolder
        } else {
            mHolder = convertView.tag as ViewHolder
        }

        val dto = mArrayList!![position]

        if (dto.areaName != null) {
            mHolder.tvName!!.text = dto.areaName
        }

        return convertView
    }

}
