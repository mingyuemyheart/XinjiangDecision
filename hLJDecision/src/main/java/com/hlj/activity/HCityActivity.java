package com.hlj.activity;

/**
 * 城市选择
 */

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hlj.stickygridheaders.StickyGridHeadersGridView;
import com.hlj.adapter.HCityAdapter;
import com.hlj.adapter.HCityNationAdapter;
import com.hlj.adapter.HCityLocalAdapter;
import com.hlj.dto.CityDto;
import com.hlj.manager.DBManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import shawn.cxwl.com.hlj.decision.R;

public class HCityActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;//返回按钮
	private TextView tvTitle = null;
	private EditText etSearch = null;
	private TextView tvProvince = null;//省内热门
	private TextView tvNational = null;//全国热门
	private LinearLayout llGroup = null;
	private LinearLayout llGridView = null;

	//搜索城市后的结果列表
	private ListView mListView = null;
	private HCityAdapter cityAdapter = null;
	private List<CityDto> cityList = new ArrayList<>();

	//省内热门
	private StickyGridHeadersGridView pGridView = null;
	private HCityLocalAdapter pAdapter = null;
	private List<CityDto> pList = new ArrayList<>();
	private int section = 1;
	private HashMap<String, Integer> sectionMap = new HashMap<>();
	
	//全国热门
	private GridView nGridView = null;
	private HCityNationAdapter nAdapter = null;
	private List<CityDto> nList = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hactivity_city);
		mContext = this;
		initWidget();
		initListView();
		initPGridView();
		initNGridView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		etSearch = (EditText) findViewById(R.id.etSearch);
		etSearch.addTextChangedListener(watcher);
		tvProvince = (TextView) findViewById(R.id.tvProvince);
		tvProvince.setOnClickListener(this);
		tvNational = (TextView) findViewById(R.id.tvNational);
		tvNational.setOnClickListener(this);
		llGroup = (LinearLayout) findViewById(R.id.llGroup);
		llGridView = (LinearLayout) findViewById(R.id.llGridView);
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.select_city));
	}
	
	private TextWatcher watcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void afterTextChanged(Editable arg0) {
			if (arg0.toString() == null) {
				return;
			}

			cityList.clear();
			if (arg0.toString().trim().equals("")) {
				mListView.setVisibility(View.GONE);
				llGroup.setVisibility(View.VISIBLE);
				llGridView.setVisibility(View.VISIBLE);
			}else {
				mListView.setVisibility(View.VISIBLE);
				llGridView.setVisibility(View.GONE);
				llGroup.setVisibility(View.GONE);
				getCityInfo(arg0.toString().trim());
			}

		}
	};
	
	/**
	 * 迁移到天气详情界面
	 */
	private void intentWeatherDetail(CityDto data) {
		Intent intent = new Intent(mContext, HWeatherDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable("data", data);
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		cityAdapter = new HCityAdapter(mContext, cityList);
		mListView.setAdapter(cityAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				intentWeatherDetail(cityList.get(arg2));
			}
		});
	}
	
	/**
	 * 初始化省内热门gridview
	 */
	private void initPGridView() {
		String[] stations = getResources().getStringArray(R.array.pro_hotCity);
		for (int i = 0; i < stations.length; i++) {
			String[] value = stations[i].split(",");
			CityDto dto = new CityDto();
			dto.cityId = value[2];
			dto.areaName = value[3];
			dto.lat = Double.valueOf(value[1]);
			dto.lng = Double.valueOf(value[0]);
			dto.level = value[4];
			dto.sectionName = value[5];
			pList.add(dto);
		}

		for (int i = 0; i < pList.size(); i++) {
			CityDto sectionDto = pList.get(i);
			if (!sectionMap.containsKey(sectionDto.sectionName)) {
				sectionDto.section = section;
				sectionMap.put(sectionDto.sectionName, section);
				section++;
			}else {
				sectionDto.section = sectionMap.get(sectionDto.sectionName);
			}
			pList.set(i, sectionDto);
		}

		pGridView = (StickyGridHeadersGridView) findViewById(R.id.pGridView);
		pAdapter = new HCityLocalAdapter(mContext, pList);
		pGridView.setAdapter(pAdapter);
		pGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				intentWeatherDetail(pList.get(arg2));
			}
		});
	}
	
	/**
	 * 获取全国热门城市
	 * @param context
	 * @return
	 */
	private List<CityDto> getNationHotCity(Context context) {
		List<CityDto> nList = new ArrayList<>();
		String[] array = context.getResources().getStringArray(R.array.nation_hotCity);
		for (int i = 0; i < array.length; i++) {
			String[] data = array[i].split(",");
			CityDto dto = new CityDto();
			dto.lng = Double.valueOf(data[0]);
			dto.lat = Double.valueOf(data[1]);
			dto.cityId = data[2];
			dto.areaName = data[3];
			dto.spellName = data[4];
			nList.add(dto);
		}
		return nList;
	}
	
	/**
	 * 初始化全国热门
	 */
	private void initNGridView() {
		nList.clear();
		nList.addAll(getNationHotCity(mContext));
		nGridView = (GridView) findViewById(R.id.nGridView);
		nAdapter = new HCityNationAdapter(mContext, nList);
		nGridView.setAdapter(nAdapter);
		nGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				intentWeatherDetail(nList.get(arg2));
			}
		});
	}
	
	/**
	 * 获取城市信息
	 */
	private void getCityInfo(String keyword) {
		DBManager dbManager = new DBManager(mContext);
		dbManager.openDateBase();
		dbManager.closeDatabase();
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null);
		Cursor cursor = null;
		cursor = database.rawQuery("select * from "+DBManager.TABLE_NAME3+" where pro like "+"\"%"+keyword+"%\""+" or city like "+"\"%"+keyword+"%\""+" or dis like "+"\"%"+keyword+"%\"",null);
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			CityDto dto = new CityDto();
			dto.provinceName = cursor.getString(cursor.getColumnIndex("pro"));
			dto.cityName = cursor.getString(cursor.getColumnIndex("city"));
			dto.areaName = cursor.getString(cursor.getColumnIndex("dis"));
			dto.cityId = cursor.getString(cursor.getColumnIndex("cid"));
			cityList.add(dto);
		}
		if (cityList.size() > 0 && cityAdapter != null) {
			cityAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.tvProvince:
			tvProvince.setTextColor(getResources().getColor(R.color.white));
			tvNational.setTextColor(getResources().getColor(R.color.title_bg));
			tvProvince.setBackgroundResource(R.drawable.corner_left_blue);
			tvNational.setBackgroundResource(R.drawable.corner_right_white);
			pGridView.setVisibility(View.VISIBLE);
			nGridView.setVisibility(View.GONE);
			break;
		case R.id.tvNational:
			tvProvince.setTextColor(getResources().getColor(R.color.title_bg));
			tvNational.setTextColor(getResources().getColor(R.color.white));
			tvProvince.setBackgroundResource(R.drawable.corner_left_white);
			tvNational.setBackgroundResource(R.drawable.corner_right_blue);
			pGridView.setVisibility(View.GONE);
			nGridView.setVisibility(View.VISIBLE);
			break;

		default:
			break;
		}
	}
}
