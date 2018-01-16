package com.hlj.activity;

/**
 * 设置界面
 */

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlj.common.CONST;
import com.hlj.common.MyApplication;
import com.hlj.manager.DataCleanManager;
import com.hlj.utils.AutoUpdateUtil;
import com.hlj.utils.CommonUtil;

import shawn.cxwl.com.hlj.R;

public class HSettingActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;//返回按钮
	private TextView tvTitle = null;
	private LinearLayout llFeedBack = null;//意见反馈
	private LinearLayout llVersion = null;//版本检测
	private LinearLayout llClearCache = null;//清除缓存
	private LinearLayout llClearData = null;//清除数据
	private LinearLayout llBuild = null;//企业信息
	private LinearLayout llCity = null;//关注城市
	private TextView tvCache = null;
	private TextView tvData = null;
	private ImageView ivPortrait = null;
	private TextView tvUserName = null;
	private TextView tvVersion = null;//版本号
	private TextView tvLogout = null;//退出登录
	private ImageView ivPushNews = null;//消息推送
	static HSettingActivity instance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hactivity_setting);
		mContext = this;
		instance = this;
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		llFeedBack = (LinearLayout) findViewById(R.id.llFeedBack);
		llFeedBack.setOnClickListener(this);
		llVersion = (LinearLayout) findViewById(R.id.llVersion);
		llVersion.setOnClickListener(this);
		llClearCache = (LinearLayout) findViewById(R.id.llClearCache);
		llClearCache.setOnClickListener(this);
		llClearData = (LinearLayout) findViewById(R.id.llClearData);
		llClearData.setOnClickListener(this);
		llBuild = (LinearLayout) findViewById(R.id.llBuild);
		llBuild.setOnClickListener(this);
		llCity = (LinearLayout) findViewById(R.id.llCity);
		llCity.setOnClickListener(this);
		tvCache = (TextView) findViewById(R.id.tvCache);
		tvData = (TextView) findViewById(R.id.tvData);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.setting));
		tvLogout = (TextView) findViewById(R.id.tvLogout);
		tvLogout.setOnClickListener(this);
		tvVersion = (TextView) findViewById(R.id.tvVersion);
		tvVersion.setText(CommonUtil.getVersion(mContext));
		ivPortrait = (ImageView) findViewById(R.id.ivPortrait);
		ivPortrait.setOnClickListener(this);
		tvUserName = (TextView) findViewById(R.id.tvUserName);
		tvUserName.setOnClickListener(this);
		ivPushNews = (ImageView) findViewById(R.id.ivPushNews);
		ivPushNews.setOnClickListener(this);

		SharedPreferences sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
		String userName = sharedPreferences.getString(CONST.UserInfo.userName, null);
		if (TextUtils.equals(userName, CONST.publicUser) || TextUtils.isEmpty(userName)) {//公众用户或为空
			tvUserName.setText("点击登录");
			tvLogout.setVisibility(View.GONE);
		}else {
			tvUserName.setText(userName);
			tvLogout.setVisibility(View.VISIBLE);
		}

		try {
			String cache = DataCleanManager.getCacheSize(mContext);
			tvCache.setText(cache);
		} catch (Exception e) {
			e.printStackTrace();
		}

		SharedPreferences push = getSharedPreferences("PUSH_STATE", Context.MODE_PRIVATE);
		boolean pushState = push.getBoolean("state", true);
		if (pushState) {
			ivPushNews.setImageResource(R.drawable.setting_checkbox_on);
		}else {
			ivPushNews.setImageResource(R.drawable.setting_checkbox_off);
		}
	}
	
	/**
	 * 删除对话框
	 * @param message 标题
	 * @param content 内容
	 * @param flag 0删除本地存储，1删除缓存
	 */
	private void deleteDialog(final boolean flag, String message, String content, final TextView textView) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.delete_dialog, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvMessage.setText(message);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (flag) {
					DataCleanManager.clearCache(mContext);
					try {
						String cache = DataCleanManager.getCacheSize(mContext);
						if (cache != null) {
							textView.setText(cache);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else {
//					ChannelsManager.clearData(mContext);//清除保存在本地的频道数据
					DataCleanManager.clearLocalSave(mContext);
					try {
						String data = DataCleanManager.getLocalSaveSize(mContext);
						if (data != null) {
							textView.setText(data);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				dialog.dismiss();
			}
		});
	}
	
	/**
	 * 删除对话框
	 * @param message 标题
	 * @param content 内容
	 */
	private void logout(String message, String content) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.delete_dialog, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvMessage.setText(message);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				SharedPreferences sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
				Editor editor = sharedPreferences.edit();
				editor.clear();
				editor.commit();
				HMainActivity.instance.finish();
				startActivity(new Intent(mContext, HWelcomeActivity.class));
				finish();
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ivPortrait:
		case R.id.tvUserName:
			startActivity(new Intent(mContext, HLoginActivity.class));
			break;
		case R.id.llFeedBack:
			Intent intent = new Intent(mContext, HFeedbackActivity.class);
			intent.putExtra(CONST.ACTIVITY_NAME, getString(R.string.setting_feedback));
			intent.putExtra(CONST.INTENT_APPID, com.hlj.common.CONST.APPID);
			startActivity(intent);
			break;
		case R.id.llVersion:
			AutoUpdateUtil.checkUpdate(HSettingActivity.this, mContext, "41", getString(R.string.app_name), false);//黑龙江气象
//			AutoUpdateUtil.checkUpdate(HSettingActivity.this, mContext, "53", getString(R.string.app_name), false);//决策气象服务
			break;
		case R.id.llClearCache:
			deleteDialog(true, getString(R.string.delete_cache), getString(R.string.sure_delete_cache), tvCache);
			break;
		case R.id.llClearData:
			deleteDialog(false, getString(R.string.delete_data), getString(R.string.sure_delete_data), tvData);
			break;
		case R.id.llBuild:
			Intent intentBuild = new Intent(mContext, HUrlActivity.class);
			intentBuild.putExtra(CONST.ACTIVITY_NAME, getString(R.string.setting_build));
			intentBuild.putExtra(CONST.WEB_URL, com.hlj.common.CONST.BUILD_URL);
			startActivity(intentBuild);
			break;
		case R.id.tvLogout:
			logout(getString(R.string.logout), getString(R.string.sure_logout));
			break;
		case R.id.llCity:
			startActivity(new Intent(mContext, HReserveCityActivity.class));
			break;
		case R.id.ivPushNews:
			SharedPreferences push = getSharedPreferences("PUSH_STATE", Context.MODE_PRIVATE);
			boolean pushState = push.getBoolean("state", true);
			Editor editor = push.edit();
			if (pushState) {
				editor.putBoolean("state", false);
				editor.commit();
				ivPushNews.setImageResource(R.drawable.setting_checkbox_off);
				MyApplication.disablePush();
			}else {
				editor.putBoolean("state", true);
				editor.commit();
				ivPushNews.setImageResource(R.drawable.setting_checkbox_on);
				MyApplication.enablePush();
			}
			break;

		default:
			break;
		}
	}
}
