package com.hlj.activity;

/**
 * 实况详情图标
 */

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.hlj.dto.FactDto;
import com.hlj.fragment.FactDetailFragment;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.OkHttpUtil;
import com.hlj.view.MainViewPager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

public class FactDetailChartActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private MainViewPager viewPager;
	private List<Fragment> fragments = new ArrayList<>();
	private List<FactDto> rainList = new ArrayList<>();
	private List<FactDto> tempList = new ArrayList<>();
	private List<FactDto> windList = new ArrayList<>();
	private LinearLayout llContainer = null, llContainer1 = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fact_detail_chart);
		mContext = this;
		showDialog();
		initWidget();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		llContainer = (LinearLayout) findViewById(R.id.llContainer);
		llContainer1 = (LinearLayout) findViewById(R.id.llContainer1);

		FactDto data = getIntent().getParcelableExtra("data");
		if (data != null) {
			if (!TextUtils.isEmpty(data.stationName)) {
				tvTitle.setText(data.stationName);
			}
			if (!TextUtils.isEmpty(data.stationCode)) {
				OkHttpStationInfo("http://decision-171.tianqi.cn/api/heilj/dates/getone48?id="+data.stationCode);
			}
		}
	}
	
	/**
	 * 初始化viewPager
	 */
	@SuppressWarnings("unchecked")
	private void initViewPager() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		llContainer.removeAllViews();
		llContainer1.removeAllViews();
		for (int j = 0; j < 3; j++) {
			TextView tvName = new TextView(mContext);
			tvName.setGravity(Gravity.CENTER);
			tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			tvName.setPadding(0, 30, 0, 30);
			tvName.setMaxLines(1);
			tvName.setTag(j);
			
			TextView tvBar = new TextView(mContext);
			tvBar.setGravity(Gravity.CENTER);

			if (j == 0) {
				tvName.setText("降水");
				tvName.setTextColor(getResources().getColor(R.color.title_bg));
				tvBar.setBackgroundColor(getResources().getColor(R.color.title_bg));
			}else if (j == 1) {
				tvName.setText("温度");
				tvName.setTextColor(getResources().getColor(R.color.text_color3));
				tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
			}else if (j == 2) {
				tvName.setText("风速");
				tvName.setTextColor(getResources().getColor(R.color.text_color3));
				tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
			}

			llContainer.addView(tvName);
			LayoutParams params = tvName.getLayoutParams();
			params.width = dm.widthPixels/3;
			tvName.setLayoutParams(params);
			
			llContainer1.addView(tvBar);
			LayoutParams params1 = tvBar.getLayoutParams();
			params1.width = dm.widthPixels/3;
			params1.height = (int) CommonUtil.dip2px(mContext, 2);
			tvBar.setLayoutParams(params1);
			
			tvName.setOnClickListener(new MyOnClickListener(j));
			tvBar.setOnClickListener(new MyOnClickListener(j));

			Fragment fragment = new FactDetailFragment();
			Bundle bundle = new Bundle();
			bundle.putInt("index", j);
			if (j == 0) {
				bundle.putParcelableArrayList("dataList", (ArrayList<? extends Parcelable>) rainList);
			}else if (j == 1) {
				bundle.putParcelableArrayList("dataList", (ArrayList<? extends Parcelable>) tempList);
			}else if (j == 2) {
				bundle.putParcelableArrayList("dataList", (ArrayList<? extends Parcelable>) windList);
			}
			fragment.setArguments(bundle);
			fragments.add(fragment);
		}
		
		viewPager = (MainViewPager) findViewById(R.id.viewPager);
		viewPager.setSlipping(true);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		viewPager.setAdapter(new MyPagerAdapter());
	}
	
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			for (int i = 0; i < llContainer.getChildCount(); i++) {
				TextView tvName = (TextView) llContainer.getChildAt(i);
				if (i == arg0) {
					tvName.setTextColor(getResources().getColor(R.color.title_bg));
				}else {
					tvName.setTextColor(getResources().getColor(R.color.text_color3));
				}
			}
			
			for (int i = 0; i < llContainer1.getChildCount(); i++) {
				TextView tvBar = (TextView) llContainer1.getChildAt(i);
				if (i == arg0) {
					tvBar.setBackgroundColor(getResources().getColor(R.color.title_bg));
				}else {
					tvBar.setBackgroundColor(getResources().getColor(R.color.transparent));
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
	 * @ClassName: MyOnClickListener
	 * @Description: TODO头标点击监听
	 * @author Panyy
	 * @date 2013 2013年11月6日 下午2:46:08
	 *
	 */
	private class MyOnClickListener implements OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			if (viewPager != null) {
				viewPager.setCurrentItem(index);
			}
		}
	};

	/**
	 * @ClassName: MyPagerAdapter
	 * @Description: TODO填充ViewPager的数据适配器
	 * @author Panyy
	 * @date 2013 2013年11月6日 下午2:37:47
	 *
	 */
	private class MyPagerAdapter extends PagerAdapter {
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(fragments.get(position).getView());
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Fragment fragment = fragments.get(position);
			if (!fragment.isAdded()) { // 如果fragment还没有added
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.add(fragment, fragment.getClass().getSimpleName());
				ft.commit();
				/**
				 * 在用FragmentTransaction.commit()方法提交FragmentTransaction对象后
				 * 会在进程的主线程中,用异步的方式来执行。
				 * 如果想要立即执行这个等待中的操作,就要调用这个方法(只能在主线程中调用)。
				 * 要注意的是,所有的回调和相关的行为都会在这个调用中被执行完成,因此要仔细确认这个方法的调用位置。
				 */
				getFragmentManager().executePendingTransactions();
			}

			if (fragment.getView().getParent() == null) {
				container.addView(fragment.getView()); // 为viewpager增加布局
			}
			return fragment.getView();
		}
	}
	
	/**
	 * 获取站点数据
	 */
	private void OkHttpStationInfo(String url) {
		OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {

			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					return;
				}
				String result = response.body().string();
				if (!TextUtils.isEmpty(result)) {
					try {
						JSONObject obj = new JSONObject(result);
						if (!obj.isNull("list")) {
							JSONArray array = obj.getJSONArray("list");
							rainList.clear();
							tempList.clear();
							windList.clear();
							for (int i = 0; i < array.length(); i++) {
								FactDto dto = new FactDto();
								JSONObject itemObj = array.getJSONObject(i);
								if (!itemObj.isNull("Datetime")) {
									dto.factTime = itemObj.getString("Datetime");
								}
								String value = "";
								if (!itemObj.isNull("JS")) {
									value = itemObj.getString("JS");
									dto.factRain = Float.parseFloat(value);
								}
								if (!value.contains("99999")) {
									rainList.add(dto);
								}

								value = "";
								if (!itemObj.isNull("WD")) {
									value = itemObj.getString("WD");
									dto.factTemp = Float.parseFloat(value);
								}
								if (!value.contains("99999")) {
									tempList.add(dto);
								}

								value = "";
								if (!itemObj.isNull("FS")) {
									value = itemObj.getString("FS");
									dto.factWind = Float.parseFloat(value);
								}
								if (!value.contains("99999")) {
									windList.add(dto);
								}

							}

						}

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								cancelDialog();
								initViewPager();
							}
						});
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}
	
}
