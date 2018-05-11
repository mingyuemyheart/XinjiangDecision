package com.hlj.fragment;

import shawn.cxwl.com.hlj.R;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hlj.activity.HPDFActivity;
import com.hlj.common.CONST;

/**
 * 联系我们
 * @author shawn_sun
 *
 */

public class ContactUsFragment extends Fragment{
	
	private TextView tvData,tvPhone;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_contact_us, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		tvData = (TextView) view.findViewById(R.id.tvData);
		tvPhone = (TextView) view.findViewById(R.id.tvPhone);

		tvData.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), HPDFActivity.class);
				intent.putExtra(CONST.ACTIVITY_NAME, "部分产品数据说明");
				intent.putExtra(CONST.WEB_URL, "http://decision-admin.tianqi.cn/data/%E9%BB%91%E9%BE%99%E6%B1%9F%E6%B0%94%E8%B1%A1%E6%89%8B%E6%9C%BAAPP%E9%83%A8%E5%88%86%E4%BA%A7%E5%93%81%E6%95%B0%E6%8D%AE%E8%AF%B4%E6%98%8E.pdf");
				startActivity(intent);
			}
		});

		tvPhone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
//				dialPhone("联系电话", "0451-55172953", "拨打");
			}
		});
	}
	
	private void dialPhone(String message, final String content, String positive) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.delete_dialog, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		TextView tvPositive = (TextView) view.findViewById(R.id.tvPositive);
		
		final Dialog dialog = new Dialog(getActivity(), R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvPositive.setText(positive);
		tvMessage.setText(message);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+content)));
			}
		});
	}
	
}
