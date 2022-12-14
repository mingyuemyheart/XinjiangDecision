package com.hlj.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.TextView
import com.hlj.dto.CityDto
import shawn.cxwl.com.hlj.R

/**
 * 城市选择
 */
class SelectCityAdapter constructor(private var context: Context?, private var groupList: ArrayList<CityDto>?, private var childList: ArrayList<ArrayList<CityDto>>?, private var listView: ExpandableListView?) : BaseExpandableListAdapter() {

    private val mInflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    internal class GroupHolder {
        var tvName: TextView? = null
    }

    internal class ChildHolder {
        var tvName: TextView? = null
    }

    override fun getGroupCount(): Int {
        return groupList!!.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return childList!![groupPosition].size
    }

    override fun getGroup(groupPosition: Int): Any? {
        return groupList!![groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any? {
        return childList!![groupPosition][childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, view: View?, parent: ViewGroup?): View? {
        var convertView = view
        val groupHolder: GroupHolder
        if (convertView == null) {
            convertView = mInflater!!.inflate(R.layout.adapter_select_city_group, null)
            groupHolder = GroupHolder()
            groupHolder.tvName = convertView.findViewById(R.id.tvName)
            convertView.tag = groupHolder
        } else {
            groupHolder = convertView.tag as GroupHolder
        }

        val dto = groupList!![groupPosition]
        if (dto.cityName != null) {
            groupHolder.tvName!!.text = dto.cityName
        }
//        if (listView!!.isGroupExpanded(groupPosition)) {
//            listView!!.collapseGroup(groupPosition)
//        } else {
//            listView!!.expandGroup(groupPosition, true)
//        }

        return convertView
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, view: View?, parent: ViewGroup?): View? {
        var convertView = view
        val childHolder: ChildHolder
        if (convertView == null) {
            convertView = mInflater!!.inflate(R.layout.adapter_select_city_child, null)
            childHolder = ChildHolder()
            childHolder.tvName = convertView.findViewById(R.id.tvName)
            convertView.tag = childHolder
        } else {
            childHolder = convertView.tag as ChildHolder
        }
        val dto = childList!![groupPosition][childPosition]
        if (dto.cityName != null) {
            childHolder.tvName!!.text = dto.cityName
        }
        return convertView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

}
