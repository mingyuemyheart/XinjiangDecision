package com.hlj.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.hlj.dto.WeatherMeetingDto;
import com.hlj.utils.CommonUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import shawn.cxwl.com.hlj.R;

/**
 * 视频会商
 */
public class ShawnWeatherMeetingAdapter extends BaseExpandableListAdapter {

	private List<WeatherMeetingDto> groupList;
	private List<List<WeatherMeetingDto>> childList;
	private List<WeatherMeetingDto> videoList;
	private LayoutInflater mInflater;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm", Locale.CHINA);
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
	private SimpleDateFormat sdf4 = new SimpleDateFormat("MM月dd日", Locale.CHINA);
	private SimpleDateFormat sdf5 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

	public ShawnWeatherMeetingAdapter(Context context, List<WeatherMeetingDto> groupList, List<List<WeatherMeetingDto>> childList, List<WeatherMeetingDto> videoList){
		this.groupList = groupList;
		this.childList = childList;
		this.videoList = videoList;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	class GroupHolder{
		TextView tvHeader;
	}

	class ChildHolder{
		TextView tvTime,tvTitle,tvLive;
	}

	@Override
	public int getGroupCount() {
		return groupList.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return childList.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groupList.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return childList.get(groupPosition).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		GroupHolder groupHolder;
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.shawn_adapter_weather_meeting_header, null);
			groupHolder = new GroupHolder();
			groupHolder.tvHeader = convertView.findViewById(R.id.tvHeader);
			convertView.setTag(groupHolder);
		}else{
			groupHolder = (GroupHolder) convertView.getTag();
		}

		WeatherMeetingDto dto = groupList.get(groupPosition);
		if (!TextUtils.isEmpty(dto.date)) {
			try {
				groupHolder.tvHeader.setText(CommonUtil.dateToWeek(dto.date)+"  "+sdf4.format(sdf3.parse(dto.date)));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		ChildHolder childHolder;
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.shawn_adapter_weather_meeting_content, null);
			childHolder = new ChildHolder();
			childHolder.tvTime = convertView.findViewById(R.id.tvTime);
			childHolder.tvTitle = convertView.findViewById(R.id.tvTitle);
			childHolder.tvLive = convertView.findViewById(R.id.tvLive);
			convertView.setTag(childHolder);
		}else{
			childHolder = (ChildHolder) convertView.getTag();
		}

		WeatherMeetingDto dto = childList.get(groupPosition).get(childPosition);
		try {
			childHolder.tvTime.setText(sdf2.format(sdf1.parse(dto.startTime))+"-"+sdf2.format(sdf1.parse(dto.endTime)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (!TextUtils.isEmpty(dto.title)) {
			childHolder.tvTitle.setText(dto.title);
		}

		long current = Long.valueOf(sdf1.format(new Date()));
		long start = Long.valueOf(dto.startTime);
		long end = Long.valueOf(dto.endTime);

		if (current < start) {
			dto.state = 0;
			childHolder.tvLive.setVisibility(View.INVISIBLE);
		}else if (current <= end) {
			dto.state = 1;
			childHolder.tvLive.setVisibility(View.VISIBLE);
			childHolder.tvLive.setText("直播");
		}else {
			try {
				for (int i = 0; i < videoList.size(); i++) {
					WeatherMeetingDto video = videoList.get(i);
					long videoTime = Long.valueOf(sdf1.format(sdf5.parse(video.videoTime)));
					if (videoTime >= start && videoTime <= end) {
						dto.state = 2;
						childHolder.tvLive.setVisibility(View.VISIBLE);
						childHolder.tvLive.setText("点播");
						break;
					}else {
						dto.state = 0;
						childHolder.tvLive.setVisibility(View.INVISIBLE);
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
