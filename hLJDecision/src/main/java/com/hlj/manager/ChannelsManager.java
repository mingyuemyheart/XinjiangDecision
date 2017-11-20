package com.hlj.manager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.hlj.common.CONST;
import com.hlj.common.ColumnData;

public class ChannelsManager {
	
	public static final String SHOWCHANNEL = "show_channel";//我的频道
	public static final String CHILDCHANNEL = "child_channel";//儿子辈
	
	private static ChannelsManager sInstance;
	private ChannelsListener mListener = null;
	
	public void init(final Context context) {
		List<ColumnData> channels = new ArrayList<ColumnData>();
		if (CONST.dataList.size() > 0) {
			channels.addAll(CONST.dataList);
			saveData(context, channels);
		}
		
		if (mListener != null) {
			mListener.initFinished();
		}
	}
	
	public static final synchronized ChannelsManager instance() {
		if (sInstance == null) {
			sInstance = new ChannelsManager();
		}
		return sInstance;
	}
	
	public void setChannelsListener(ChannelsListener listener) {
		mListener = listener;
	}
	
	public interface ChannelsListener {
		void addChannel(ColumnData channel);
		void removeChannel(ColumnData channel);
		void initFinished();
	}
	public interface ChannelsInitListener{
		void finished();
	}
	
	/**
	 * 读取保存在本地数据
	 */
	public static int readData(Context context, List<ColumnData> list) {
		list.clear();
		
		//获取一级列表数据
		SharedPreferences sp = context.getSharedPreferences(SHOWCHANNEL, Context.MODE_PRIVATE);
		int size = sp.getInt("saveListSize", 0);
		for (int i = 0; i < size; i++) {
			ColumnData channel = new ColumnData();
			channel.id = sp.getString("id"+i, null);
			channel.name = sp.getString("name"+i, null);
			channel.level = sp.getString("level"+i, null);
			channel.dataUrl = sp.getString("dataUrl"+i, null);
			channel.showType = sp.getString("showType"+i, null);
			list.add(channel);
			
			//获取二级列表数据
			SharedPreferences childSp = context.getSharedPreferences(sp.getString("id"+i, null), Context.MODE_PRIVATE);
			int childSize = childSp.getInt("childSize", 0);
			for (int j = 0; j < childSize; j++) {
				ColumnData child = new ColumnData();
				child.id = childSp.getString("childId"+j, null);
				child.name = childSp.getString("childName"+j, null);
				child.dataUrl = childSp.getString("dataUrl"+j, null);
				child.showType = childSp.getString("showType"+j, null);
				child.icon = childSp.getString("icon"+j, null);
				list.get(i).child.add(child);
				
				//获取三级列表数据
				SharedPreferences child2Sp = context.getSharedPreferences(childSp.getString("childName"+j, null), Context.MODE_PRIVATE);
				int child2Size = child2Sp.getInt("child2Size", 0);
				for (int m = 0; m < child2Size; m++) {
					ColumnData child2 = new ColumnData();
					child2.name = child2Sp.getString("childName2"+m, null);
					child2.dataUrl = child2Sp.getString("dataUrl2"+m, null);
					child2.showType = child2Sp.getString("showType2"+m, null);
					child2.icon = child2Sp.getString("icon2"+m, null);
					list.get(i).child.get(j).child.add(child2);
				}
			}
			
		}
		return size;
	}
	
	/**
	 * 保存数据到手机本地
	 */
	public static void saveData(Context context, List<ColumnData> saveList) {
		if (saveList == null) {
			return;
		}
		
		//保存一级列表数据
		SharedPreferences sp = context.getSharedPreferences(SHOWCHANNEL, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		int size = saveList.size();
		editor.putInt("saveListSize", size);
		for (int i = 0; i < size; i++) {
			editor.remove("name"+i);
			editor.remove("id"+i);
			editor.remove("level"+i);
			editor.remove("dataUrl"+i);
			editor.remove("showType"+i);
			editor.putString("name"+i, saveList.get(i).name);
			editor.putString("id"+i, saveList.get(i).id);
			editor.putString("level"+i, saveList.get(i).level);
			editor.putString("dataUrl"+i, saveList.get(i).dataUrl);
			editor.putString("showType"+i, saveList.get(i).showType);
			
			//保存二级列表数据
			int childSize = saveList.get(i).child.size();
			if (childSize > 0) {
				SharedPreferences childSp = context.getSharedPreferences(sp.getString("id"+i, saveList.get(i).id), Context.MODE_PRIVATE);
				Editor cEditor = childSp.edit();
				cEditor.putInt("childSize", childSize);
				for (int j = 0; j < childSize; j++) {
					cEditor.remove("childId"+j);
					cEditor.remove("childName"+j);
					cEditor.remove("dataUrl"+j);
					cEditor.remove("showType"+j);
					cEditor.remove("icon"+j);
					cEditor.putString("childId"+j, saveList.get(i).child.get(j).id);
					cEditor.putString("childName"+j, saveList.get(i).child.get(j).name);
					cEditor.putString("dataUrl"+j, saveList.get(i).child.get(j).dataUrl);
					cEditor.putString("showType"+j, saveList.get(i).child.get(j).showType);
					cEditor.putString("icon"+j, saveList.get(i).child.get(j).icon);
					
					//保存三级列表数据
					int child2Size = saveList.get(i).child.get(j).child.size();
					if (child2Size > 0) {
						SharedPreferences child2Sp = context.getSharedPreferences(childSp.getString("childName"+i, saveList.get(i).child.get(j).name), Context.MODE_PRIVATE);
						Editor tEditor = child2Sp.edit();
						tEditor.putInt("child2Size", child2Size);
						for (int m = 0; m < child2Size; m++) {
							tEditor.remove("childName2"+m);
							tEditor.remove("dataUrl2"+m);
							tEditor.remove("showType2"+m);
							tEditor.putString("childName2"+m, saveList.get(i).child.get(j).child.get(m).name);
							tEditor.putString("dataUrl2"+m, saveList.get(i).child.get(j).child.get(m).dataUrl);
							tEditor.putString("showType2"+m, saveList.get(i).child.get(j).child.get(m).showType);
						}
						tEditor.commit();
					}
				}
				cEditor.commit();
			}
		}
		editor.commit();
	}
	
	public static void clearData(Context context) {
		SharedPreferences sp = context.getSharedPreferences(SHOWCHANNEL, Context.MODE_PRIVATE);
		SharedPreferences childSp = context.getSharedPreferences(CHILDCHANNEL, Context.MODE_PRIVATE);
		sp.edit().clear().commit();
		childSp.edit().clear().commit();
	}
	
}
