package com.hlj.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlj.adapter.ShawnProductAdapter;
import com.hlj.common.CONST;
import com.hlj.common.ColumnData;
import com.hlj.dto.AgriDto;
import com.hlj.utils.OkHttpUtil;

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

/**
 * 农业气象等
 */
public class ShawnProductActivity2 extends BaseActivity implements OnClickListener {
	
	private Context mContext;
	private ShawnProductAdapter mAdapter;
	private List<ColumnData> dataList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_product2);
		mContext = this;
		initWidget();
		initListView();
	}

	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}

		dataList.clear();
		String dataUrl = getIntent().getStringExtra(CONST.WEB_URL);
		if (!TextUtils.isEmpty(dataUrl)) {
			showDialog();
			OkHttpList(dataUrl);
		}else {
			AgriDto data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				tvTitle.setText(data.name);
				dataList.clear();
				dataList.addAll(data.child);
			}
		}
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		GridView gridView = findViewById(R.id.gridView);
		mAdapter = new ShawnProductAdapter(mContext, dataList);
		gridView.setAdapter(mAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ColumnData dto = dataList.get(arg2);
				Intent intent;
				if (TextUtils.isEmpty(dto.showType) || TextUtils.equals(dto.showType, CONST.NEWS)) {//专业气象预报、中期旬报
					intent = new Intent(mContext, HPdfListActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable("data", dto);
					intent.putExtras(bundle);
					startActivity(intent);
				}else {
					if (!TextUtils.isEmpty(dto.dataUrl)) {
						if (dto.dataUrl.contains(".pdf") || dto.dataUrl.contains(".PDF")) {//pdf格式
							intent = new Intent(mContext, HPDFActivity.class);
							intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
							intent.putExtra(CONST.WEB_URL, dto.dataUrl);
							startActivity(intent);
						}else {//网页、图片
							intent = new Intent(mContext, HUrlActivity.class);
							intent.putExtra(CONST.ACTIVITY_NAME, dto.name);
							intent.putExtra(CONST.WEB_URL, dto.dataUrl);
							startActivity(intent);
						}
					}
				}

			}
		});
	}
	
	private void OkHttpList(final String url) {
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
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("l")) {
											JSONArray array = new JSONArray(obj.getString("l"));
											for (int i = 0; i < array.length(); i++) {
												JSONObject itemObj = array.getJSONObject(i);
												ColumnData dto = new ColumnData();
												dto.name = itemObj.getString("l1");
												dto.dataUrl = itemObj.getString("l2");
												dto.icon = itemObj.getString("l4");
												dataList.add(dto);
											}
										}
										if (mAdapter != null) {
											mAdapter.notifyDataSetChanged();
										}
									} catch (JSONException e1) {
										e1.printStackTrace();
									}
								}
								cancelDialog();
							}
						});
					}
				});
			}
		}).start();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llBack:
				finish();
				break;
		}
	}

}
