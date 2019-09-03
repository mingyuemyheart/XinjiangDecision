package com.hlj.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

import com.hlj.common.CONST;
import com.hlj.common.ColumnData;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

public class ShawnWelcomeActivity extends BaseActivity{

	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_welcome);
		mContext = this;

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				SharedPreferences sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
				String userName = sharedPreferences.getString(CONST.UserInfo.userName, null);
				String pwd = sharedPreferences.getString(CONST.UserInfo.passWord, null);

				if (!TextUtils.isEmpty(userName) && !TextUtils.equals(userName, CONST.publicUser)) {//决策用户
					OkHttpLogin(userName, pwd);
				}else {
					OkHttpLogin(CONST.publicUser, CONST.publicPwd);
				}
			}
		}, 1500);
	}

	/**
	 * 登录
	 */
	private void OkHttpLogin(final String userName, final String pwd) {
		final String url = "http://decision-admin.tianqi.cn/Home/Work/login";
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("username", userName);
		builder.add("password", pwd);
		builder.add("appid", CONST.APPID);
		builder.add("device_id", "");
		builder.add("platform", "android");
		builder.add("os_version", android.os.Build.VERSION.RELEASE);
		builder.add("software_version", CommonUtil.getVersion(mContext));
		builder.add("mobile_type", android.os.Build.MODEL);
		builder.add("address", "");
		builder.add("lat", "");
		builder.add("lon", "");
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("status")) {
												int status  = object.getInt("status");
												if (status == 1) {//成功

													JSONArray array = new JSONArray(object.getString("column"));

//								SharedPreferences sp = getSharedPreferences(com.hlj.common.CONST.CHANNELSIZESHARE, Context.MODE_PRIVATE);
//								Editor channelEditor = sp.edit();
//								int size = array.length();
//								channelEditor.putInt(com.hlj.common.CONST.CHANNELSIZE, size);
//								channelEditor.commit();

													CONST.dataList.clear();
													for (int i = 0; i < array.length(); i++) {
														JSONObject obj = array.getJSONObject(i);
														ColumnData data = new ColumnData();
														if (!obj.isNull("id")) {
															data.columnId = obj.getString("id");
														}
														if (!obj.isNull("localviewid")) {
															data.id = obj.getString("localviewid");
														}
														if (!obj.isNull("name")) {
															data.name = obj.getString("name");
														}
														if (!obj.isNull("default")) {
															data.level = obj.getString("default");
														}
														if (!obj.isNull("icon")) {
															data.icon = obj.getString("icon");
														}
														if (!obj.isNull("icon2")) {
															data.icon2 = obj.getString("icon2");
														}
														if (!obj.isNull("desc")) {
															data.desc = obj.getString("desc");
														}
														if (!obj.isNull("dataurl")) {
															data.dataUrl = obj.getString("dataurl");
														}
														if (!obj.isNull("showtype")) {
															data.showType = obj.getString("showtype");
														}
														if (!obj.isNull("child")) {
															JSONArray childArray = new JSONArray(obj.getString("child"));
															for (int j = 0; j < childArray.length(); j++) {
																JSONObject childObj = childArray.getJSONObject(j);
																ColumnData dto = new ColumnData();
																if (!childObj.isNull("localviewid")) {
																	dto.id = childObj.getString("localviewid");
																}
																if (!childObj.isNull("name")) {
																	dto.name = childObj.getString("name");
																}
																if (!childObj.isNull("desc")) {
																	dto.desc = childObj.getString("desc");
																}
																if (!childObj.isNull("icon")) {
																	dto.icon = childObj.getString("icon");
																}
																if (!childObj.isNull("icon2")) {
																	dto.icon2 = childObj.getString("icon2");
																}
																if (!childObj.isNull("showtype")) {
																	dto.showType = childObj.getString("showtype");
																}
																if (!childObj.isNull("dataurl")) {
																	dto.dataUrl = childObj.getString("dataurl");
																}
																if (!childObj.isNull("child")) {
																	JSONArray child2Array = new JSONArray(childObj.getString("child"));
																	for (int k = 0; k < child2Array.length(); k++) {
																		JSONObject child2Obj = child2Array.getJSONObject(k);
																		ColumnData child2 = new ColumnData();
																		if (!child2Obj.isNull("localviewid")) {
																			child2.id = child2Obj.getString("localviewid");
																		}
																		if (!child2Obj.isNull("name")) {
																			child2.name = child2Obj.getString("name");
																		}
																		if (!child2Obj.isNull("desc")) {
																			child2.desc = child2Obj.getString("desc");
																		}
																		if (!child2Obj.isNull("icon")) {
																			child2.icon = child2Obj.getString("icon");
																		}
																		if (!child2Obj.isNull("icon2")) {
																			child2.icon2 = child2Obj.getString("icon2");
																		}
																		if (!child2Obj.isNull("dataurl")) {
																			child2.dataUrl = child2Obj.getString("dataurl");
																		}
																		if (!child2Obj.isNull("showtype")) {
																			child2.showType = child2Obj.getString("showtype");
																		}
																		dto.child.add(child2);
																	}
																}
																data.child.add(dto);
															}
														}
														CONST.dataList.add(data);
													}

													if (!object.isNull("info")) {
														JSONObject obj = new JSONObject(object.getString("info"));
														if (!obj.isNull("id")) {
															String uid = obj.getString("id");
															if (uid != null) {
																//把用户信息保存在sharedPreferance里
																if (!TextUtils.equals(userName, CONST.publicUser)) {//决策用户
																	SharedPreferences sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
																	SharedPreferences.Editor editor = sharedPreferences.edit();
																	editor.putString(CONST.UserInfo.uId, uid);
																	editor.putString(CONST.UserInfo.userName, userName);
																	editor.putString(CONST.UserInfo.passWord, pwd);
																	editor.commit();

																	CONST.UID = uid;
																	CONST.USERNAME = userName;
																	CONST.PASSWORD = pwd;
																}

																cancelDialog();
																startActivity(new Intent(mContext, HMainActivity.class));
																finish();

															}
														}
													}
												}else {
													//失败
													if (!object.isNull("msg")) {
														final String msg = object.getString("msg");
														cancelDialog();
														if (msg != null) {
															Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
														}
													}
												}
											}
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
						});
					}
				});
			}
		}).start();
	}
	
	@Override
	public boolean onKeyDown(int KeyCode, KeyEvent event){
		if (KeyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return super.onKeyDown(KeyCode, event);
	}
	
}
