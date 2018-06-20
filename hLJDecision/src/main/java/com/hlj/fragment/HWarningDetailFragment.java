package com.hlj.fragment;

/**
 * 天气预警
 */

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hlj.common.CONST;
import com.hlj.dto.WarningDto;
import com.hlj.manager.DBManager;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

public class HWarningDetailFragment extends Fragment{
	
	private ImageView imageView = null;//预警图标
	private TextView tvName = null;//预警名称
	private TextView tvTime = null;//预警时间
	private TextView tvIntro = null;//预警介绍
	private TextView tvGuide = null;//防御指南
	private String url = "http://decision.tianqi.cn/alarm12379/content2/";//详情页面url
	private WarningDto data = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.hfragment_warning_detail, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
	}
	
	private void refresh() {
		data = getArguments().getParcelable("data");
		try {
			OkHttpWarningDetail(url+data.html);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget(View view) {
		imageView = (ImageView) view.findViewById(R.id.imageView);
		tvName = (TextView) view.findViewById(R.id.tvName);
		tvTime = (TextView) view.findViewById(R.id.tvTime);
		tvIntro = (TextView) view.findViewById(R.id.tvIntro);
		tvGuide = (TextView) view.findViewById(R.id.tvGuide);
		
		refresh();
	}
	
	/**
	 * 初始化数据库
	 */
	private void initDBManager() {
		DBManager dbManager = new DBManager(getActivity());
		dbManager.openDateBase();
		dbManager.closeDatabase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		Cursor cursor = null;
		cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME2 + " where WarningId = " + "\"" + data.type+data.color + "\"",null);
		String content = null;
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			content = cursor.getString(cursor.getColumnIndex("WarningGuide"));
		}
		if (!TextUtils.isEmpty(content)) {
			tvGuide.setText(getString(R.string.warning_guide)+content);
			tvGuide.setVisibility(View.VISIBLE);
		}else {
			tvGuide.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 异步请求
	 */
	private void OkHttpWarningDetail(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (isAdded()) {//判断fragment是否已经添加
									if (!TextUtils.isEmpty(result)) {
										try {
											JSONObject object = new JSONObject(result);
											if (object != null) {
												if (!object.isNull("sendTime")) {
													tvTime.setText(object.getString("sendTime"));
												}

												if (!object.isNull("description")) {
													tvIntro.setText(object.getString("description"));
												}

												String name = object.getString("headline");
												if (!TextUtils.isEmpty(name)) {
													tvName.setText(name.replace(getString(R.string.publish), getString(R.string.publish)+"\n"));
												}

												Bitmap bitmap = null;
												if (object.getString("severityCode").equals(CONST.blue[0])) {
													bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+object.getString("eventType")+CONST.blue[1]+CONST.imageSuffix);
													if (bitmap == null) {
														bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.blue[1]+CONST.imageSuffix);
													}
												}else if (object.getString("severityCode").equals(CONST.yellow[0])) {
													bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+object.getString("eventType")+CONST.yellow[1]+CONST.imageSuffix);
													if (bitmap == null) {
														bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.yellow[1]+CONST.imageSuffix);
													}
												}else if (object.getString("severityCode").equals(CONST.orange[0])) {
													bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+object.getString("eventType")+CONST.orange[1]+CONST.imageSuffix);
													if (bitmap == null) {
														bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.orange[1]+CONST.imageSuffix);
													}
												}else if (object.getString("severityCode").equals(CONST.red[0])) {
													bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+object.getString("eventType")+CONST.red[1]+CONST.imageSuffix);
													if (bitmap == null) {
														bitmap = CommonUtil.getImageFromAssetsFile(getActivity(),"warning/"+"default"+CONST.red[1]+CONST.imageSuffix);
													}
												}
												imageView.setImageBitmap(bitmap);

												initDBManager();
											}
										} catch (JSONException e) {
											e.printStackTrace();
										}
									}
								}
							}
						});
					}
				});
			}
		}).start();
	}
	
}
