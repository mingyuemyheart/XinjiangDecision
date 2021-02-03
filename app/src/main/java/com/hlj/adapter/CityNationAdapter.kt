package com.hlj.adapter;

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.hlj.dto.CityDto
import shawn.cxwl.com.hlj.R

/**
 * 城市选择，全国热门
 */
class CityNationAdapter constructor(context: Context?, private var mArrayList: MutableList<CityDto>) : BaseAdapter() {

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
            convertView = mInflater!!.inflate(R.layout.adapter_city_pro_content, null)
            mHolder = ViewHolder()
            mHolder!!.tvName = convertView.findViewById<View>(R.id.tvName) as TextView
            convertView.tag = mHolder
        } else {
            mHolder = convertView.tag as ViewHolder
        }

        val dto = mArrayList[position]

        if (dto.areaName != null) {
            mHolder!!.tvName!!.text = dto.areaName
        }

        return convertView
    }

}
