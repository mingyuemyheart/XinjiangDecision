package com.hlj.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import shawn.cxwl.com.hlj.R;

public class MyDialog extends Dialog {

	private TextView tvPercent;

	public MyDialog(Context context) {
		super(context);
	}

	public void setStyle(int featureId) {
		requestWindowFeature(featureId);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawableResource(R.color.transparent);
		setContentView(R.layout.loading);
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		tvPercent = findViewById(R.id.tvPercent);
	}
	
	public void setPercent(int percent) {
		if (tvPercent != null) {
			tvPercent.setText(percent + "%");
		}
	}
	
}
