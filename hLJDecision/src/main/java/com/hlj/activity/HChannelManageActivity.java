package com.hlj.activity;

/**
 * 频道管理
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlj.adapter.DragAdapter;
import com.hlj.adapter.OtherAdapter;
import com.hlj.common.CONST;
import com.hlj.common.ColumnData;
import com.hlj.manager.ChannelsManager;
import com.hlj.view.DragGridView;
import com.hlj.view.OtherGridView;

import java.util.ArrayList;
import java.util.List;

import shawn.cxwl.com.hlj.R;

public class HChannelManageActivity extends BaseActivity implements OnClickListener, OnItemClickListener{
	
	private Context mContext = null;
	private TextView tvTitle = null;
	private LinearLayout llBack = null;//返回按钮
	private TextView tvControl = null;//确定按钮
	private TextView tvPrompt = null;
	private DragGridView dragGridView = null;
	private DragAdapter dragAdapter = null;
	private List<ColumnData> dragList = new ArrayList<>();
	private OtherGridView otherGridView = null;
	private OtherAdapter otherAdapter = null;
	private List<ColumnData> otherList = new ArrayList<>();
	private boolean isMove = false;//判断动画是否在移动
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hactivity_channel_manage);
		mContext = this;
		initWidget();
		initDragGridView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvControl = (TextView) findViewById(R.id.tvControl);
		tvControl.setText(getString(R.string.order_delete));
		tvControl.setVisibility(View.GONE);
		tvControl.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.order_channel));
		tvPrompt = (TextView) findViewById(R.id.tvPrompt);
		
		List<ColumnData> tempList = new ArrayList<>();
//		ChannelsManager.readData(mContext, tempList);
		tempList.addAll(com.hlj.common.CONST.dataList);
		for (int i = 0; i < tempList.size(); i++) {
			ColumnData channel = tempList.get(i);
			if (channel.level.equals(CONST.ONE)) {
				dragList.add(channel);
			}else {
				otherList.add(channel);
			}
		}
	}
	
	/**
	 * 初始化gridview
	 */
	private void initDragGridView() {
		dragGridView = (DragGridView) findViewById(R.id.dragGridView);
		dragAdapter = new DragAdapter(mContext, dragList);
		dragGridView.setAdapter(dragAdapter);
		dragGridView.setOnItemClickListener(this);
		
		otherGridView = (OtherGridView) findViewById(R.id.otherGridView);
		otherAdapter = new OtherAdapter(mContext, otherList);
		otherGridView.setAdapter(otherAdapter);
		otherGridView.setOnItemClickListener(this);
	}
	
	/** GRIDVIEW对应的ITEM点击监听接口  */
	@Override
	public void onItemClick(AdapterView<?> parent, final View view, final int position,long id) {
		//如果点击的时候，之前动画还没结束，那么就让点击事件无效
		if(isMove){
			return;
		}
		switch (parent.getId()) {
		case R.id.dragGridView:
//			final ColumnData dragChannel = ((DragAdapter) parent.getAdapter()).getItem(position);//获取点击的频道内容
//			if (CONST.isDelete == false) {
				Intent intent = new Intent();
				intent.putExtra(CONST.POSITION, position);
				setResult(RESULT_OK, intent);
				finish();
//			}else {
//				//position为 0，1 的不可以进行任何操作
//				if (position != 0) {
//					final ImageView moveImageView = getView(view);
//					if (moveImageView != null) {
//						TextView newTextView = (TextView) view.findViewById(R.id.text_item);
//						final int[] startLocation = new int[2];
//						newTextView.getLocationInWindow(startLocation);
//						otherAdapter.setVisible(false);
//						//添加到最后一个
//						dragChannel.level = CONST.ZERO;
//						saveData();
//						otherAdapter.addItem(dragChannel);
//
//						new Handler().postDelayed(new Runnable() {
//							public void run() {
//								try {
//									int[] endLocation = new int[2];
//									//获取终点的坐标
//									otherGridView.getChildAt(otherGridView.getLastVisiblePosition()).getLocationInWindow(endLocation);
//									MoveAnim(moveImageView, startLocation , endLocation, dragChannel, dragGridView);
//									dragAdapter.setRemove(position);
//								} catch (Exception localException) {
//								}
//							}
//						}, 50L);
//
//					}
//				}
//			}
			break;
		case R.id.otherGridView:
//			final ColumnData otherChannel = ((OtherAdapter) parent.getAdapter()).getItem(position);
//			final ImageView moveImageView = getView(view);
//			if (moveImageView != null){
//				TextView newTextView = (TextView) view.findViewById(R.id.text_item);
//				final int[] startLocation = new int[2];
//				newTextView.getLocationInWindow(startLocation);
//				dragAdapter.setVisible(false);
//
//				otherChannel.level = CONST.ONE;
//				saveData();
//				dragAdapter.addItem(otherChannel);
//
//				new Handler().postDelayed(new Runnable() {
//					public void run() {
//						try {
//							int[] endLocation = new int[2];
//							//获取终点的坐标
//							dragGridView.getChildAt(dragGridView.getLastVisiblePosition()).getLocationInWindow(endLocation);
//							MoveAnim(moveImageView, startLocation , endLocation, otherChannel,otherGridView);
//							otherAdapter.setRemove(position);
//						} catch (Exception localException) {
//						}
//					}
//				}, 50L);
//
//			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * 获取点击的Item的对应View，
	 * @param view
	 * @return
	 */
	private ImageView getView(View view) {
		view.destroyDrawingCache();
		view.setDrawingCacheEnabled(true);
		Bitmap cache = Bitmap.createBitmap(view.getDrawingCache());
		view.setDrawingCacheEnabled(false);
		ImageView iv = new ImageView(this);
		iv.setImageBitmap(cache);
		return iv;
	}
	
	/**
	 * 点击ITEM移动动画
	 * @param moveView
	 * @param startLocation
	 * @param endLocation
	 * @param moveChannel
	 * @param clickGridView
	 */
	private void MoveAnim(View moveView, int[] startLocation,int[] endLocation, final ColumnData moveChannel, final GridView clickGridView) {
		int[] initLocation = new int[2];
		//获取传递过来的VIEW的坐标
		moveView.getLocationInWindow(initLocation);
		//得到要移动的VIEW,并放入对应的容器中
		final ViewGroup moveViewGroup = getMoveViewGroup();
		final View mMoveView = getMoveView(moveViewGroup, moveView, initLocation);
		//创建移动动画
		TranslateAnimation moveAnimation = new TranslateAnimation(
				startLocation[0], endLocation[0], startLocation[1],
				endLocation[1]);
		moveAnimation.setDuration(300L);//动画时间
		//动画配置
		AnimationSet moveAnimationSet = new AnimationSet(true);
		moveAnimationSet.setFillAfter(false);//动画效果执行完毕后，View对象不保留在终止的位置
		moveAnimationSet.addAnimation(moveAnimation);
		mMoveView.startAnimation(moveAnimationSet);
		moveAnimationSet.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				isMove = true;
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				moveViewGroup.removeView(mMoveView);
				// instanceof 方法判断2边实例是不是一样，判断点击的是DragGridView还是OtherGridView
				
				if (clickGridView instanceof DragGridView) {
					otherAdapter.setVisible(true);
					otherAdapter.notifyDataSetChanged();
					dragAdapter.remove();
				}else{
					dragAdapter.setVisible(true);
					dragAdapter.notifyDataSetChanged();
					otherAdapter.remove();
				}
				isMove = false;
			}
		});
	}
	
	/**
	 * 获取移动的VIEW，放入对应ViewGroup布局容器
	 * @param viewGroup
	 * @param view
	 * @param initLocation
	 * @return
	 */
	private View getMoveView(ViewGroup viewGroup, View view, int[] initLocation) {
		int x = initLocation[0];
		int y = initLocation[1];
		viewGroup.addView(view);
		LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mLayoutParams.leftMargin = x;
		mLayoutParams.topMargin = y;
		view.setLayoutParams(mLayoutParams);
		return view;
	}
	
	/**
	 * 创建移动的ITEM对应的ViewGroup布局容器
	 */
	private ViewGroup getMoveViewGroup() {
		ViewGroup moveViewGroup = (ViewGroup) getWindow().getDecorView();
		LinearLayout moveLinearLayout = new LinearLayout(this);
		moveLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		moveViewGroup.addView(moveLinearLayout);
		return moveLinearLayout;
	}
	
	private void saveData() {
		List<ColumnData> list = new ArrayList<ColumnData>();
		list.addAll(dragList);
		list.addAll(otherList);
		ChannelsManager.saveData(mContext, list);
	}
	
	private void exit() {
		saveData();
		setResult(RESULT_OK);
		finish();
	}
	
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			if (CONST.isDelete == false) {
//				exit();
//			}else {
//				tvControl.setText(getString(R.string.order_delete));
//				tvPrompt.setText(getString(R.string.switch_channel));
//				
//				CONST.isDelete = false;
//				return false;
//			}
//		}
//		return super.onKeyDown(keyCode, event);
//	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
//			if (CONST.isDelete == false) {
//				exit();
//			}else {
//				tvControl.setText(getString(R.string.order_delete));
//				tvPrompt.setText(getString(R.string.switch_channel));
//				
//				CONST.isDelete = false;
//				if (dragAdapter != null) {
//					dragAdapter.notifyDataSetChanged();
//				}
//			}
			break;
		case R.id.tvControl:
			if (CONST.isDelete == false) {
				tvControl.setText(getString(R.string.complete));
				tvPrompt.setText(getString(R.string.long_press_order));
				
				CONST.isDelete = true;
			}else {
				tvControl.setText(getString(R.string.order_delete));
				tvPrompt.setText(getString(R.string.switch_channel));
				
				CONST.isDelete = false;
				exit();
			}
			
			if (dragAdapter != null) {
				dragAdapter.notifyDataSetChanged();
			}
			break;
		default:
			break;
		}
	}

}
