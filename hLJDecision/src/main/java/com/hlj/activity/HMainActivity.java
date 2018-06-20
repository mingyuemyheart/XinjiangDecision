package com.hlj.activity;

/**
 * 主界面
 */

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hlj.common.CONST;
import com.hlj.common.ColumnData;
import com.hlj.fragment.ContactUsFragment;
import com.hlj.fragment.HAgriWeatherFragment;
import com.hlj.fragment.HDecisionServiceFragment;
import com.hlj.fragment.HForecastFragment;
import com.hlj.fragment.HPersonInfuluceFragment;
import com.hlj.fragment.HWeatherForecastFragment;
import com.hlj.fragment.HWeatherWarningFragment;
import com.hlj.fragment.WeatherKepuFragment;
import com.hlj.utils.AutoUpdateUtil;
import com.hlj.utils.CommonUtil;
import com.hlj.view.MainViewPager;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

import shawn.cxwl.com.hlj.R;

public class HMainActivity extends BaseFragmentActivity implements OnClickListener{
	
	private Context mContext = null;
	private ImageView ivSetting = null;//设置按钮
	private ImageView ivAdd = null;//添加频道按钮
	private List<ColumnData> channelList = new ArrayList<>();
	private MainViewPager viewPager = null;
	private PagerAdapter mAdapter = null;
	private TabPageIndicator indicator = null;
	private long mExitTime;//记录点击完返回按钮后的long型时间
//	private ChannelsManager manager = null;
	private int position = 0;//用于存放下标
	static HMainActivity instance;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hactivity_main);
        mContext = this;
        instance = this;
		initWidget();
		initViewPager();
    }
    
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		AutoUpdateUtil.checkUpdate(HMainActivity.this, mContext, "41", getString(R.string.app_name), true);//黑龙江气象
//		AutoUpdateUtil.checkUpdate(HMainActivity.this, mContext, "53", getString(R.string.app_name), true);//决策气象服务

		ivSetting = (ImageView) findViewById(R.id.ivSetting);
		ivSetting.setOnClickListener(this);
		ivAdd = (ImageView) findViewById(R.id.ivAdd);
		ivAdd.setOnClickListener(this);

		//是否显示登录对话框
		SharedPreferences sp = getSharedPreferences("LOGINDIALOG", Context.MODE_PRIVATE);
		String versionCode = sp.getString("versionCode", "");

		//获取保存的用户信息
		SharedPreferences sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE);
		String userName = sharedPreferences.getString(CONST.UserInfo.userName, null);
		if (!TextUtils.equals(versionCode, CommonUtil.getVersion(mContext))) {
			if (TextUtils.equals(userName, CONST.publicUser) || TextUtils.isEmpty(userName)) {//公众用户或为空
				decisionLoginDialog(sp);
			}
		}
	}

	/**
	 * 决策用户登录对话框
	 */
	private void decisionLoginDialog(final SharedPreferences sp) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_decision_login, null);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);

		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();

		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				SharedPreferences.Editor editor = sp.edit();
				editor.putString("versionCode", CommonUtil.getVersion(mContext));
				editor.commit();
			}
		});

		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				SharedPreferences.Editor editor = sp.edit();
				editor.putString("versionCode", CommonUtil.getVersion(mContext));
				editor.commit();
				startActivity(new Intent(mContext, HLoginActivity.class));
			}
		});
	}
	
	/**
	 * 初始化viewpager、indicator
	 */
	private void initViewPager() {
        viewPager = (MainViewPager) findViewById(R.id.viewPager);
        indicator = (TabPageIndicator) findViewById(R.id.indicator);
        mAdapter = new PagerAdapter(getSupportFragmentManager(), viewPager, indicator);
        viewPager.setSlipping(true);//设置ViewPager是否可以滑动
		viewPager.setAdapter(mAdapter);
		viewPager.setOffscreenPageLimit(channelList.size());

		// 实例化TabPageIndicator然后设置ViewPager与之关联
		indicator.setVisibility(View.VISIBLE);
		indicator.setViewPager(viewPager);
		indicator.setOnPageChangeListener(new MyOnPageChangeListener());
		
//		int size = ChannelsManager.readData(mContext, channelList);
//		SharedPreferences sp = getSharedPreferences(com.hlj.common.CONST.CHANNELSIZESHARE, Context.MODE_PRIVATE);
//		int length = sp.getInt(com.hlj.common.CONST.CHANNELSIZE, 0);//保存的频道个数
//		
//        if (size != length || size <= 0) {
//        	manager = ChannelsManager.instance();
//            manager.setChannelsListener(this);
//            manager.init(this);
//		}else {
			channelList.clear();
			channelList.addAll(CONST.dataList);
			for (int i = 0; i < channelList.size(); i++) {
				ColumnData channel = channelList.get(i);
	        	if (channel.level.equals(com.hlj.common.CONST.ONE)) {
	        		addItem(channel);
				}
	        }
//		}
        
//        mAdapter.mIndicator.setCurrentItem(position);
	}
	
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			if (arg0 == 3) {
				viewPager.setSlipping(false);
			}else {
				viewPager.setSlipping(true);
			}
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}
	
//	@Override
//	public void addChannel(ColumnData item) {
//		addItem(item);
//	}
//
//	@Override
//	public void removeChannel(ColumnData item) {
//		removeItem(item);
//	}
//	
//	@Override
//	public void initFinished() {
//		int size = ChannelsManager.readData(mContext, channelList);
//		for (int i = 0; i < size; i++) {
//			ColumnData channel = channelList.get(i);
//        	if (channel.level.equals(com.hlj.common.CONST.ONE)) {
//        		addItem(channel);
//			}
//        }
//	}

    private class PagerAdapter extends FragmentStatePagerAdapter {
    	private List<PagerInfo> pagers = new ArrayList<>();
    	private ViewPager mPager;
    	private TabPageIndicator mIndicator;
    	
    	public class PagerInfo {
    		final ColumnData channel;
    		final Bundle bundle;
    		final Class<?> cls;
    		public PagerInfo(ColumnData _channel, Bundle _bundle, Class<?> _cls) {
    			this.channel = _channel;
    			this.bundle = _bundle;
    			this.cls = _cls;
			}
    	}
    	
    	public PagerAdapter(FragmentManager fm, ViewPager pager, TabPageIndicator indicator) {
            super(fm);
            mPager = pager;
            mPager.setAdapter(this);
            mIndicator = indicator;
        }
    	
    	void addItem(ColumnData channel, Bundle bundle, Class<?> cls) {
//    		if (getInfoByChannel(channel) == null) {
    			PagerInfo info = new PagerInfo(channel, bundle, cls);
    			pagers.add(info);
    			notifyDataSetChanged();
    			mIndicator.notifyDataSetChanged();
    			mPager.setOffscreenPageLimit(pagers.size());
//    		}
    	}
    	
    	PagerInfo getInfoByChannel(ColumnData channel) {
    		for (PagerInfo info : pagers) {
    			if (TextUtils.equals(info.channel.id, channel.id)) {
    				return info;
    			}
    		}
    		return null;
    	}
    	
    	void removeItem(ColumnData channel) {
    		int currentIndex = mPager.getCurrentItem();
    		int index = -1;
    		int len = pagers.size();
    		for (int i = 0; i < len ; i ++) {
    			PagerInfo info = pagers.get(i);
    			if (TextUtils.equals(info.channel.id, channel.id)) {
    				index = i;
    				pagers.remove(info);
    				break;
    			}
    		}
    		notifyDataSetChanged();
    		mIndicator.notifyDataSetChanged();
    		if (index == currentIndex && index > 1) {
    			mPager.setCurrentItem(index - 1);
    		}
    	}
    	
       
        @Override
        public Fragment getItem(int position) {
        	PagerInfo info = pagers.get(position);
            return Fragment.instantiate(getApplicationContext(), info.cls.getName(), info.bundle);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pagers.get(position).channel.name;
        }

        @Override
        public int getCount() {
            return pagers.size();
        }
    }
    
    private void addItem(ColumnData channel) {
    	if (channel == null) {
    		return;
    	}
    	Bundle bundle = new Bundle();
        bundle.putString(CONST.INTENT_APPID, com.hlj.common.CONST.APPID);
        bundle.putParcelable("data", channel);
        String showType = channel.showType;
        if (TextUtils.equals(showType, CONST.LOCAL)) {
        	String id = channel.id;
        	if (TextUtils.equals(id, "1")) {
        		mAdapter.addItem(channel, bundle, HForecastFragment.class);//首页
        	} else if (TextUtils.equals(id, "2")) {
        		mAdapter.addItem(channel, bundle, HAgriWeatherFragment.class);//农业气象
        	} else if (TextUtils.equals(id, "3")) {
        		mAdapter.addItem(channel, bundle, HWeatherForecastFragment.class);//天气实况
        	} else if (TextUtils.equals(id, "4")) {
        		mAdapter.addItem(channel, bundle, HWeatherForecastFragment.class);//天气预报
        	} else if (TextUtils.equals(id, "5")) {
        		mAdapter.addItem(channel, bundle, HWeatherWarningFragment.class);//天气预警
        	} else if (TextUtils.equals(id, "7")) {
        		mAdapter.addItem(channel, bundle, WeatherKepuFragment.class);//气象科普
        	} else if (TextUtils.equals(id, "8")) {
        		mAdapter.addItem(channel, bundle, HPersonInfuluceFragment.class);//人工影响天气
        	} else if (TextUtils.equals(id, "10")) {
        		mAdapter.addItem(channel, bundle, HWeatherForecastFragment.class);//电力气象服务
        	} else if (TextUtils.equals(id, "11")) {
        		mAdapter.addItem(channel, bundle, HWeatherForecastFragment.class);//铁路气象服务
        	} else if (TextUtils.equals(id, "12")) {
        		mAdapter.addItem(channel, bundle, ContactUsFragment.class);//联系我们
        	} 
		}else if (TextUtils.equals(showType, CONST.NEWS)) {
			mAdapter.addItem(channel, bundle, HDecisionServiceFragment.class);//决策服务
		}
    }
    
    private void removeItem(ColumnData item) {
    	mAdapter.removeItem(item);
    	viewPager.removeView(viewPager.getRootView());
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(mContext, getString(R.string.confirm_exit)+getString(R.string.app_name), Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else {
				finish();
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivSetting:
			startActivity(new Intent(mContext, HSettingActivity.class));
			break;
		case R.id.ivAdd:
			startActivityForResult(new Intent(mContext, HChannelManageActivity.class), 0);
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 0:
				if (data != null) {
					Bundle bundle = data.getExtras();
					if (bundle != null) {
						position = bundle.getInt("position");
						if (viewPager != null) {
							viewPager.setCurrentItem(position, true);
						}
					}
				}
//				initViewPager();
				break;

			default:
				break;
			}
		}
	}

}
