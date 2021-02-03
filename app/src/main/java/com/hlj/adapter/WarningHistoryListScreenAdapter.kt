package com.hlj.adapter;

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.hlj.dto.WarningDto
import shawn.cxwl.com.hlj.R
import java.util.*

/**
 * 历史预警-预警列表-筛选
 */
class WarningHistoryListScreenAdapter constructor(private var context: Context?, private val mArrayList: MutableList<WarningDto>?) : BaseAdapter() {

    private var mInflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var index = 0

    fun setIndex(index :Int) {
        this.index = index
        setValue()
    }

    private val isSelected = HashMap<Int, Boolean>()

    init {
        setValue()
    }

    private fun setValue() {
        for (i in mArrayList!!.indices) {
            isSelected[i] = i == index
        }
    }

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
            convertView = mInflater!!.inflate(R.layout.adapter_warning_history_list_screen, null)
            mHolder = ViewHolder()
            mHolder.tvName = convertView.findViewById(R.id.tvName)
            convertView.tag = mHolder
        } else {
            mHolder = convertView.tag as ViewHolder
        }
        val dto = mArrayList!![position]
        if (dto.name != null) {
            mHolder.tvName!!.text = dto.name
        }
        if (isSelected[position]!!) {
            mHolder.tvName!!.setTextColor(-0xd2a563)
            mHolder.tvName!!.setBackgroundResource(R.drawable.bg_layer_button)
        } else {
            if (position == 0) {
                mHolder.tvName!!.setTextColor(ContextCompat.getColor(context!!, R.color.text_color3))
            } else {
                if (dto.count == 0) {
                    mHolder.tvName!!.setTextColor(ContextCompat.getColor(context!!, R.color.text_color2))
                } else {
                    mHolder.tvName!!.setTextColor(ContextCompat.getColor(context!!, R.color.text_color3))
                }
            }
            mHolder.tvName!!.setBackgroundColor(ContextCompat.getColor(context!!, R.color.white))
        }
        return convertView
    }

}
