package com.hlj.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.hlj.dto.WarningDto
import shawn.cxwl.com.hlj.R

/**
 * 天气预警-历史预警-预警筛选-选择区域
 */
class WarningHistoryScreenAreaAdapter constructor(private var context: Context, private val mArrayList: MutableList<WarningDto>?) : BaseAdapter(){

    private val mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private class ViewHolder {
        var tvName: TextView? = null
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
            convertView = mInflater!!.inflate(R.layout.adapter_warning_statistic_screen_area, null)
            mHolder = ViewHolder()
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
