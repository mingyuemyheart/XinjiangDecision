package com.hlj.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import shawn.cxwl.com.hlj.decision.R;

public class MyDialog extends Dialog {

	private Context mContext = null;
	private String message = null;
	private TextView tvPercent = null;
	private TextView tvContent = null;// message
	
	public MyDialog(Context context) {
		super(context);
		mContext = context;
	}
	
	public MyDialog(Context context, String msg) {
		super(context);
		mContext = context;
		message = msg;
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
		tvContent = (TextView) findViewById(R.id.content);
		tvPercent = (TextView) findViewById(R.id.tvPercent);

		if (tvContent != null) {
			if (message == null) {
				tvContent.setText(mContext.getResources().getString(R.string.is_loading));
			} else {
				tvContent.setText(message);
			}
		}
		
	}
	
	public void setPercent(int percent) {
		if (tvPercent != null) {
			tvPercent.setText(percent + "%");
		}
	}
	
}
