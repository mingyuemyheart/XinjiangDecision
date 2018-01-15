package com.hlj.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hlj.adapter.HWarningAdapter;
import com.hlj.dto.WarningDto;

import java.util.ArrayList;
import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 预警列表
 */

public class HWarningListActivity extends BaseActivity implements View.OnClickListener{

    private Context mContext;
    private LinearLayout llBack;
    private TextView tvTitle;
    private ListView listView;
    private HWarningAdapter madapter;
    private List<WarningDto> mList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hactivity_warning_list);
        mContext = this;
        initWidget();
        initListView();
    }

    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("预警列表");
    }

    private void initListView() {
        mList.clear();
        mList.addAll(getIntent().getExtras().<WarningDto>getParcelableArrayList("warningList"));
        listView = (ListView) findViewById(R.id.listView);
        madapter = new HWarningAdapter(mContext, mList, false);
        listView.setAdapter(madapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WarningDto data = mList.get(position);
                Intent intentDetail = new Intent(mContext, HWarningDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("data", data);
                intentDetail.putExtras(bundle);
                mContext.startActivity(intentDetail);
            }
        });
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
