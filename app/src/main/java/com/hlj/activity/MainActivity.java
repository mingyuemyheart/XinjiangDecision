package com.hlj.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hlj.adapter.MyPagerAdapter;
import com.hlj.common.CONST;
import com.hlj.common.ColumnData;
import com.hlj.common.MyApplication;
import com.hlj.fragment.CommonListFragment;
import com.hlj.fragment.ContactUsFragment;
import com.hlj.fragment.ForecastFragment;
import com.hlj.fragment.JueceListFragment;
import com.hlj.fragment.TourFragment;
import com.hlj.fragment.WarningFragment;
import com.hlj.fragment.WeatherFactFragment;
import com.hlj.fragment.WeatherVideoFragment;
import com.hlj.fragment.WebviewFragment;
import com.hlj.manager.SystemStatusManager;
import com.hlj.utils.AuthorityUtil;
import com.hlj.utils.AutoUpdateUtil;
import com.hlj.utils.CommonUtil;
import com.hlj.view.MainViewPager;

import java.util.ArrayList;
import java.util.List;

import shawn.cxwl.com.hlj.R;

public class MainActivity extends BaseFragmentActivity implements OnClickListener {
	
	private Context mContext = null;
	private MainViewPager viewPager = null;
	private LinearLayout llContainer,llContainer1;
	private HorizontalScrollView hScrollView1;
	private List<Fragment> fragments = new ArrayList<>();
	private long mExitTime;//?????????????????????????????????long?????????
	private int columnWidth = 0;
	private String BROADCAST_ACTION_NAME = "";//??????fragment????????????

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		MyApplication.addDestoryActivity(this, "MainActivity");
        mContext = this;
		setTranslucentStatus();
		checkMultiAuthority();
    }

	/**
	 * ???????????????????????????
	 */
	private void setTranslucentStatus() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window win = getWindow();
			WindowManager.LayoutParams winParams = win.getAttributes();
			final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
			winParams.flags |= bits;
			win.setAttributes(winParams);
		}
		SystemStatusManager tintManager = new SystemStatusManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(0);// ??????????????????
	}

    private void init() {
		initWidget();
		initViewPager();
	}

	/**
	 * ???????????????
	 */
	private void initWidget() {
		AutoUpdateUtil.checkUpdate(MainActivity.this, mContext, "140", getString(R.string.app_name), true);

		ImageView ivSetting = findViewById(R.id.ivSetting);
		ivSetting.setOnClickListener(this);
		llContainer = findViewById(R.id.llContainer);
		llContainer1 = findViewById(R.id.llContainer1);
		hScrollView1 = findViewById(R.id.hScrollView1);
	}

	/**
	 * ?????????viewpager
	 */
	private void initViewPager() {
		int columnSize = MyApplication.columnDataList.size();
		if (columnSize <= 1) {
			llContainer.setVisibility(View.GONE);
			llContainer1.setVisibility(View.GONE);
		}

		fragments.clear();
		llContainer.removeAllViews();
		llContainer1.removeAllViews();
		for (int i = 0; i < columnSize; i++) {
			ColumnData channel = MyApplication.columnDataList.get(i);

			TextView tvName = new TextView(mContext);
			tvName.setGravity(Gravity.CENTER);
			tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			tvName.setPadding(0, (int)(CommonUtil.dip2px(mContext, 6)), 0, (int)(CommonUtil.dip2px(mContext, 6)));
			tvName.setOnClickListener(new MyOnClickListener(i));
			tvName.setTextColor(getResources().getColor(R.color.white));
			if (!TextUtils.isEmpty(channel.name)) {
				tvName.setText(channel.name);
			}
			tvName.measure(0, 0);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			params.width = tvName.getMeasuredWidth();
			columnWidth += params.width;
			params.setMargins((int)(CommonUtil.dip2px(mContext, 10)), 0, (int)(CommonUtil.dip2px(mContext, 10)), 0);
			tvName.setLayoutParams(params);
			llContainer.addView(tvName, i);

			TextView tvBar = new TextView(mContext);
			tvBar.setGravity(Gravity.CENTER);
			tvBar.setOnClickListener(new MyOnClickListener(i));
			if (i == 0) {
				tvBar.setBackgroundColor(getResources().getColor(R.color.white));
			}else {
				tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
			}
			LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			params1.width = tvName.getMeasuredWidth();
			params1.height = (int)(CommonUtil.dip2px(mContext, 3));
			params1.setMargins((int)(CommonUtil.dip2px(mContext, 10)), 0, (int)(CommonUtil.dip2px(mContext, 10)), 0);
			tvBar.setLayoutParams(params1);
			llContainer1.addView(tvBar, i);

			Fragment fragment = null;
			String showType = channel.showType;
			if (TextUtils.equals(showType, CONST.LOCAL)) {
				String id = channel.id;
				if (TextUtils.equals(id, "1")) {
					fragment = new ForecastFragment();//??????
				} else if (TextUtils.equals(id, "4")) {
					fragment = new WarningFragment();//????????????
				} else if (TextUtils.equals(id, "5") || TextUtils.equals(id, "6") || TextUtils.equals(id, "10")) {
					fragment = new JueceListFragment();//??????????????????????????????????????????
				} else if (TextUtils.equals(id, "2") || TextUtils.equals(id, "3") || TextUtils.equals(id, "8") || TextUtils.equals(id, "9")) {
					fragment = new WeatherFactFragment();//???????????????????????????????????????????????????????????????
				} else if (TextUtils.equals(id, "7")) {
					fragment = new TourFragment();//????????????
				} else if (TextUtils.equals(id, "11")) {
					fragment = new WeatherVideoFragment();//????????????
				} else if (TextUtils.equals(id, "12")) {
					fragment = new ContactUsFragment();//????????????
				}
			} else if (TextUtils.equals(showType, CONST.NEWS)) {
				fragment = new CommonListFragment();
			} else {
				fragment = new WebviewFragment();
			}

			if (fragment != null) {
				Bundle bundle = new Bundle();
				bundle.putString(CONST.BROADCAST_ACTION, fragment.getClass().getName()+channel.name);
				bundle.putString(CONST.COLUMN_ID, channel.columnId);
				bundle.putString(CONST.ACTIVITY_NAME, channel.name);
				bundle.putString(CONST.WEB_URL, channel.dataUrl);
				bundle.putString(CONST.LOCAL_ID, channel.id);
				bundle.putParcelable("data", channel);
				fragment.setArguments(bundle);
				fragments.add(fragment);
			}
		}

		viewPager = findViewById(R.id.viewPager);
		viewPager.setSlipping(false);//??????ViewPager??????????????????
		viewPager.setOffscreenPageLimit(fragments.size());
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(), fragments));
	}
	
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			if (llContainer != null) {
				hScrollView1.smoothScrollTo(columnWidth/llContainer.getChildCount()*arg0, 0);
				for (int i = 0; i < llContainer.getChildCount(); i++) {
					TextView tvName = (TextView) llContainer.getChildAt(i);
					if (i == arg0) {
						String actionName = fragments.get(arg0).getClass().getName()+tvName.getText().toString();
						if (!BROADCAST_ACTION_NAME.contains(actionName)) {
							Intent intent = new Intent();
							intent.setAction(actionName);
							sendBroadcast(intent);
							BROADCAST_ACTION_NAME += actionName;
						}
					}
				}
			}
			if (llContainer1 != null) {
				for (int i = 0; i < llContainer1.getChildCount(); i++) {
					TextView tvBar = (TextView) llContainer1.getChildAt(i);
					if (i == arg0) {
						tvBar.setBackgroundColor(getResources().getColor(R.color.white));
					}else {
						tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
					}
				}
			}
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	/**
	 * ??????????????????
	 * @author shawn_sun
	 */
	private class MyOnClickListener implements OnClickListener {
		private int index;

		private MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			if (viewPager != null) {
				viewPager.setCurrentItem(index, true);
			}
		}
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
			startActivity(new Intent(mContext, SettingActivity.class));
			break;
		default:
			break;
		}
	}
	
	//???????????????????????????
	private String[] allPermissions = new String[] {
			Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.READ_PHONE_STATE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.CALL_PHONE
	};

	//?????????????????????
	private static List<String> deniedList = new ArrayList<>();
	/**
	 * ??????????????????
	 */
	private void checkMultiAuthority() {
		if (Build.VERSION.SDK_INT < 23) {
			init();
		}else {
			deniedList.clear();
			for (String permission : allPermissions) {
				if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
					deniedList.add(permission);
				}
			}
			if (deniedList.isEmpty()) {//?????????????????????
				init();
			}else {
				String[] permissions = deniedList.toArray(new String[deniedList.size()]);//???list????????????
				ActivityCompat.requestPermissions(this, permissions, AuthorityUtil.AUTHOR_MULTI);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case AuthorityUtil.AUTHOR_MULTI:
				if (grantResults.length > 0) {
					boolean isAllGranted = true;//??????????????????
					for (int gResult : grantResults) {
						if (gResult != PackageManager.PERMISSION_GRANTED) {
							isAllGranted = false;
							break;
						}
					}
					if (isAllGranted) {//?????????????????????
						init();
					}else {//???????????????????????????????????????????????????????????????
						AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"?????????????????????????????????????????????????????????????????????????????????????????????????????????");
					}
				}else {
					for (String permission : permissions) {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
							AuthorityUtil.intentAuthorSetting(mContext, "\""+getString(R.string.app_name)+"\""+"?????????????????????????????????????????????????????????????????????????????????????????????????????????");
							break;
						}
					}
				}
				break;
		}
	}

}
