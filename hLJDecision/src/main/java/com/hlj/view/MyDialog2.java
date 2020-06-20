package com.hlj.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import shawn.cxwl.com.hlj.R;

public class MyDialog2 extends Dialog {

	public MyDialog2(Context context) {
		super(context);
	}
	
	public MyDialog2(Context context, String msg) {
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
		setContentView(R.layout.dialog_loading);
	}
	
}
