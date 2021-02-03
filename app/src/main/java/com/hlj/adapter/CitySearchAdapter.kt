package com.hlj.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.hlj.dto.CityDto
import shawn.cxwl.com.hlj.R

/**
 * 城市选择
 */
class CitySearchAdapter constructor(context: Context?, private var mArrayList: MutableList<CityDto>) : BaseAdapter() {

    private val mInflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

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
            convertView = mInflater!!.inflate(R.layout.adapter_city_search, null)
            mHolder = ViewHolder()
            mHolder!!.tvName = convertView.findViewById<View>(R.id.tvName) as TextView
            convertView.tag = mHolder
        } else {
            mHolder = convertView.tag as ViewHolder
        }

        val dto = mArrayList[position]
        if (dto!!.provinceName.contains(dto.cityName)) {
            mHolder!!.tvName!!.text = "${dto.cityName}-${dto.areaName}"
        } else {
            mHolder!!.tvName!!.text = "${dto.provinceName}-${dto.cityName}-${dto.areaName}"
        }
        return convertView
    }

}
