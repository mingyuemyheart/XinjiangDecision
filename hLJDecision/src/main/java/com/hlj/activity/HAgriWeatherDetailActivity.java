package com.hlj.activity;

/**
 * 农业气象详情
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hlj.common.CONST;
import com.hlj.common.ColumnData;
import com.hlj.dto.AgriDto;
import com.hlj.adapter.HFactTableAdapter;
import com.hlj.utils.OkHttpUtil;
import com.hlj.view.RefreshLayout;
import com.hlj.view.RefreshLayout.OnRefreshListener;
import com.hlj.adapter.CommonPdfListAdapter;

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
import shawn.cxwl.com.hlj.decision.R;

public class HAgriWeatherDetailActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ImageView ivArrow = null;
	private ListView mListView = null;
	private CommonPdfListAdapter mAdapter = null;
	private List<AgriDto> mList = new ArrayList<>();
	private RefreshLayout refreshLayout = null;//下拉刷新布局
	private AgriDto dto = null;
	
	private ListView tableListView = null;
	private HFactTableAdapter tableAdapter = null;
	private List<AgriDto> tableList = new ArrayList<>();
	private RelativeLayout llContainer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hactivity_pdf_list);
		mContext = this;
		initRefreshLayout();
		initWidget();
		initListView();
		initTableListView();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setOnClickListener(this);
		ivArrow = (ImageView) findViewById(R.id.ivArrow);
		ivArrow.setOnClickListener(this);
		llContainer = (RelativeLayout) findViewById(R.id.llContainer);
		
		dto = getIntent().getExtras().getParcelable("dto");
		if (dto != null) {
			if (dto.child.get(0).name != null) {
				tvTitle.setText(dto.child.get(0).name);
			}
			
			refresh();
		}
	}
	
	private void refresh() {
		if (TextUtils.isEmpty(dto.showType)) {
			if (!TextUtils.isEmpty(dto.child.get(0).dataUrl)) {
				OkHttpDetail(dto.child.get(0).dataUrl);
			}
		}else {
			if (!TextUtils.isEmpty(dto.dataUrl)) {
				OkHttpDetail(dto.dataUrl);
			}
		}
	}
	
	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
		refreshLayout.setColor(com.hlj.common.CONST.color1, com.hlj.common.CONST.color2, com.hlj.common.CONST.color3, com.hlj.common.CONST.color4);
		refreshLayout.setMode(RefreshLayout.Mode.PULL_FROM_START);
		refreshLayout.setLoadNoFull(false);
		refreshLayout.setRefreshing(true);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
	}
	
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new CommonPdfListAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				AgriDto dto = mList.get(arg2);
				Intent intent = null;
				if (TextUtils.equals(dto.type, CONST.PDF)) {
					intent = new Intent(mContext, HPDFActivity.class);
					intent.putExtra(CONST.ACTIVITY_NAME, dto.title);
					intent.putExtra(CONST.WEB_URL, dto.dataUrl);
					startActivity(intent);
				}
			}
		});
	}
	
	/**
	 * 获取详情
	 */
	private void OkHttpDetail(String url) {
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
					refreshLayout.setRefreshing(false);
					try {
						JSONObject obj = new JSONObject(result);
						String type = null;
						if (!obj.isNull("type")) {
							type = obj.getString("type");
						}
						if (!obj.isNull("l")) {
							mList.clear();
							JSONArray array = obj.getJSONArray("l");
							for (int i = 0; i < array.length(); i++) {
								JSONObject itemObj = array.getJSONObject(i);
								AgriDto dto = new AgriDto();
								dto.title = itemObj.getString("l1");
								dto.dataUrl = itemObj.getString("l2");
								dto.time = itemObj.getString("l3");
								dto.type = type;
								mList.add(dto);
							}
						}

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (mList.size() > 0 && mAdapter != null) {
									mAdapter.notifyDataSetChanged();
								}
								refreshLayout.setRefreshing(false);
							}
						});

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	private void initTableListView() {
		tableList.clear();
		ColumnData data = getIntent().getExtras().getParcelable("data");
		if (data != null) {
			for (int i = 0; i < data.child.size(); i++) {
				AgriDto dto = new AgriDto();
				dto.title = data.child.get(i).name;
				dto.showType = data.child.get(i).showType;
				tableList.add(dto);
				
				for (int j = 0; j < data.child.get(i).child.size(); j++) {
					dto = new AgriDto();
					dto.title = data.child.get(i).child.get(j).name;
					dto.showType = data.child.get(i).child.get(j).showType;
					dto.dataUrl = data.child.get(i).child.get(j).dataUrl;
					tableList.add(dto);
				}
			}
		}
		tableListView = (ListView) findViewById(R.id.tableListView);
		tableAdapter = new HFactTableAdapter(mContext, tableList);
		tableListView.setAdapter(tableAdapter);
		tableListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				AgriDto dto = tableList.get(arg2);
				if (!TextUtils.isEmpty(dto.dataUrl)) {
					tvTitle.setText(dto.title);
					switchData();
					OkHttpDetail(dto.dataUrl);
				}
			}
		});
	}
	
	/**
	 * 切换数据
	 */
	private void switchData() {
		if (llContainer.getVisibility() == View.GONE) {
			startAnimation(false, llContainer);
			llContainer.setVisibility(View.VISIBLE);
			ivArrow.setImageResource(R.drawable.iv_arrow_up);
		}else {
			startAnimation(true, llContainer);
			llContainer.setVisibility(View.GONE);
			ivArrow.setImageResource(R.drawable.iv_arrow_down);
		}
	}
	
	/**
	 * @param flag false为显示map，true为显示list
	 */
	private void startAnimation(final boolean flag, final RelativeLayout llContainer) {
		//列表动画
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation animation = null;
		if (flag == false) {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,-1.0f,
					Animation.RELATIVE_TO_SELF,0f);
		}else {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,-1.0f);
		}
		animation.setDuration(400);
		animationSet.addAnimation(animation);
		animationSet.setFillAfter(true);
		llContainer.startAnimation(animationSet);
		animationSet.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				llContainer.clearAnimation();
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (llContainer.getVisibility() == View.GONE) {
				finish();
			}else {
				startAnimation(true, llContainer);
				llContainer.setVisibility(View.GONE);
				ivArrow.setImageResource(R.drawable.iv_arrow_down);
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			if (llContainer.getVisibility() == View.GONE) {
				finish();
			}else {
				startAnimation(true, llContainer);
				llContainer.setVisibility(View.GONE);
				ivArrow.setImageResource(R.drawable.iv_arrow_down);
			}
			break;
		case R.id.tvTitle:
		case R.id.ivArrow:
			switchData();
			break;

		default:
			break;
		}
	}
	
}
