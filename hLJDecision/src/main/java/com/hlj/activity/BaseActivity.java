package com.hlj.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.hlj.view.MyDialog2;

public class BaseActivity extends Activity{

	private Context mContext = null;
	private MyDialog2 mDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
	}
	
	/**
	 * 初始化dialog
	 */
	public void showDialog() {
		if (mDialog == null) {
			mDialog = new MyDialog2(mContext);
		}
		mDialog.show();
	}
	public void cancelDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}
	
}
