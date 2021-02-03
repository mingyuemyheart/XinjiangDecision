package com.hlj.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.hlj.activity.WarningHistoryActivity
import com.hlj.dto.WarningDto
import shawn.cxwl.com.hlj.R

/**
 * 历史预警
 */
class WarningHistoryAdapter constructor(private var context: Context?, private var groupList: List<WarningDto>?, private var childList: List<List<WarningDto>>?, private var listView: ExpandableListView?) : BaseExpandableListAdapter() {

    private val mInflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var startTime: String? = null
    private var endTime: String? = null

    fun setStartTime(startTime: String?) {
        this.startTime = startTime
    }

    fun setEndTime(endTime: String?) {
        this.endTime = endTime
    }

    internal class GroupHolder {
        var tvAreaName: TextView? = null
        var tvShortName: TextView? = null
        var tvCount: TextView? = null
        var tvRed: TextView? = null
        var tvOrange: TextView? = null
        var tvYellow: TextView? = null
        var tvBlue: TextView? = null
        var llAll: LinearLayout? = null
        var ivArrow: ImageView? = null
    }

    internal class ChildHolder {
        var tvAreaName: TextView? = null
        var tvShortName: TextView? = null
        var tvCount: TextView? = null
        var tvRed: TextView? = null
        var tvOrange: TextView? = null
        var tvYellow: TextView? = null
        var tvBlue: TextView? = null
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
            convertView = mInflater!!.inflate(R.layout.adapter_warning_history_group, null)
            groupHolder = GroupHolder()
            groupHolder.tvAreaName = convertView.findViewById(R.id.tvAreaName)
            groupHolder.llAll = convertView.findViewById(R.id.llAll)
            groupHolder.tvShortName = convertView.findViewById(R.id.tvShortName)
            groupHolder.ivArrow = convertView.findViewById(R.id.ivArrow)
            groupHolder.tvCount = convertView.findViewById(R.id.tvCount)
            groupHolder.tvRed = convertView.findViewById(R.id.tvRed)
            groupHolder.tvOrange = convertView.findViewById(R.id.tvOrange)
            groupHolder.tvYellow = convertView.findViewById(R.id.tvYellow)
            groupHolder.tvBlue = convertView.findViewById(R.id.tvBlue)
            convertView.tag = groupHolder
        } else {
            groupHolder = convertView.tag as GroupHolder
        }

        //判断是否已经打开列表
        if (isExpanded) {
            groupHolder.ivArrow!!.setImageResource(R.drawable.statistic_arrow_top)
        } else {
            groupHolder.ivArrow!!.setImageResource(R.drawable.statistic_arrow_bottom)
        }
        val dto = groupList!![groupPosition]
        if (!TextUtils.isEmpty(dto.areaName)) {
            groupHolder.tvAreaName!!.text = dto.areaName
        } else {
            groupHolder.tvAreaName!!.text = "总计"
        }
        if (!TextUtils.isEmpty(dto.areaKey) && !TextUtils.equals(dto.areaKey, "all") && !dto.areaKey.contains("00") && dto.areaKey.length != 6) {
            if (!TextUtils.isEmpty(dto.areaName)) {
                val ss = SpannableString(dto.areaName)
                ss.setSpan(UnderlineSpan(), 0, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                groupHolder.tvAreaName!!.text = ss
            }
        }
        if (!TextUtils.isEmpty(dto.shortName)) {
            groupHolder.tvShortName!!.text = dto.shortName
        } else {
            groupHolder.tvShortName!!.text = "全部"
        }
        groupHolder.tvCount!!.text = dto.warningCount
        groupHolder.tvRed!!.text = dto.redCount
        groupHolder.tvOrange!!.text = dto.orangeCount
        groupHolder.tvYellow!!.text = dto.yellowCount
        groupHolder.tvBlue!!.text = dto.blueCount
        groupHolder.tvAreaName!!.setOnClickListener {
            if (!TextUtils.equals(dto.areaKey, "all") && !dto.areaKey.contains("00") && dto.areaKey.length != 6) {
                val intent = Intent(context, WarningHistoryActivity::class.java)
                val bundle = Bundle()
                bundle.putParcelable("data", dto)
                intent.putExtras(bundle)
                intent.putExtra("startTime", startTime)
                intent.putExtra("endTime", endTime)
                context!!.startActivity(intent)
            }
        }
        groupHolder.llAll!!.setOnClickListener {
            if (listView!!.isGroupExpanded(groupPosition)) {
                listView!!.collapseGroup(groupPosition)
            } else {
                listView!!.expandGroup(groupPosition, true)
            }
        }
        return convertView
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, view: View?, parent: ViewGroup?): View? {
        var convertView = view
        val childHolder: ChildHolder
        if (convertView == null) {
            convertView = mInflater!!.inflate(R.layout.adapter_warning_history_child, null)
            childHolder = ChildHolder()
            childHolder.tvAreaName = convertView.findViewById(R.id.tvAreaName)
            childHolder.tvShortName = convertView.findViewById(R.id.tvShortName)
            childHolder.tvCount = convertView.findViewById(R.id.tvCount)
            childHolder.tvRed = convertView.findViewById(R.id.tvRed)
            childHolder.tvOrange = convertView.findViewById(R.id.tvOrange)
            childHolder.tvYellow = convertView.findViewById(R.id.tvYellow)
            childHolder.tvBlue = convertView.findViewById(R.id.tvBlue)
            convertView.tag = childHolder
        } else {
            childHolder = convertView.tag as ChildHolder
        }
        val dto = childList!![groupPosition][childPosition]
        if (!TextUtils.isEmpty(dto.shortName)) {
            childHolder.tvShortName!!.text = dto.shortName
        }
        childHolder.tvCount!!.text = dto.warningCount
        childHolder.tvRed!!.text = dto.redCount
        childHolder.tvOrange!!.text = dto.orangeCount
        childHolder.tvYellow!!.text = dto.yellowCount
        childHolder.tvBlue!!.text = dto.blueCount
        return convertView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

}
