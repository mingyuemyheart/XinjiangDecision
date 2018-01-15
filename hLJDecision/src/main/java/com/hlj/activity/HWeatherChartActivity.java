package com.hlj.activity;

/**
 * 天气图分析
 */

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.hlj.dto.AgriDto;
import com.hlj.dto.RadarDto;
import com.hlj.adapter.HFactTableAdapter;
import com.hlj.manager.RadarManager;
import com.hlj.manager.RadarManager.RadarListener;
import com.hlj.adapter.WeatherRadarDetailAdpater;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.CustomHttpClient;
import com.hlj.view.MyDialog;

import shawn.cxwl.com.hlj.R;

public class HWeatherChartActivity extends BaseActivity implements OnClickListener, RadarListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;//返回按钮
	private TextView tvTitle = null;
	private ImageView ivArrow = null;
	private ListView tableListView = null;
	private HFactTableAdapter tableAdapter = null;
	private List<AgriDto> tableList = new ArrayList<>();
	private RelativeLayout reContainer = null;
	private List<RadarDto> radarList = new ArrayList<>();
	private ImageView imageView = null;
	private RadarManager mRadarManager = null;
	private RadarThread mRadarThread = null;
	private static final int HANDLER_SHOW_RADAR = 1;
	private static final int HANDLER_PROGRESS = 2;
	private static final int HANDLER_LOAD_FINISHED = 3;
	private static final int HANDLER_PAUSE = 4;
	private LinearLayout llSeekBar = null;
	private ImageView ivPlay = null;
	private SeekBar seekBar = null;
	private TextView tvTime = null;
	private GridView mGridView = null;
	private WeatherRadarDetailAdpater mAdapter = null;
//	private SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
//	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM月dd日HH时mm分");
	private String selected = "1";//选中
	private String unselected = "0";//未选中
	private AgriDto data = null;
	private MyDialog dialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hactivity_weather_chart);
		mContext = this;
		initWidget();
		initTableListView();
		initGridView();
	}

	private void initDialog() {
		if (dialog == null) {
			dialog = new MyDialog(mContext);
		}
		dialog.setPercent(0);
		dialog.show();
	}

	private void dismissDialog() {
		if (dialog != null) {
			dialog.dismiss();
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		ivArrow = (ImageView) findViewById(R.id.ivArrow);
		reContainer = (RelativeLayout) findViewById(R.id.reContainer);
		imageView = (ImageView) findViewById(R.id.imageView);
		imageView.setOnClickListener(this);
		ivPlay = (ImageView) findViewById(R.id.ivPlay);
		ivPlay.setOnClickListener(this);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(seekbarListener);
		tvTime = (TextView) findViewById(R.id.tvTime);
		llSeekBar = (LinearLayout) findViewById(R.id.llSeekBar);
		
		mRadarManager = new RadarManager(mContext);
		
		data = getIntent().getExtras().getParcelable("data");
		if (data != null) {
			if (data.name != null) {
				tvTitle.setText(data.name);
			}
			
			if (data.child.size() == 0) {
				ivArrow.setVisibility(View.GONE);
				if (!TextUtils.isEmpty(data.dataUrl)) {
					getRadarData(data.dataUrl);
				}
			}else {
				tvTitle.setOnClickListener(this);
				ivArrow.setOnClickListener(this);
				ivArrow.setVisibility(View.VISIBLE);
				if (!TextUtils.isEmpty(data.child.get(0).dataUrl)) {
					getRadarData(data.child.get(0).dataUrl);
				}
			}
		}
	}
	
	private OnSeekBarChangeListener seekbarListener = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			if (mRadarThread != null) {
				mRadarThread.setCurrent(seekBar.getProgress());
				mRadarThread.stopTracking();
			}
		}
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			if (mRadarThread != null) {
				mRadarThread.startTracking();
			}
		}
		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		}
	};
	
	private void initTableListView() {
		tableList.clear();
		if (data != null) {
			for (int i = 0; i < data.child.size(); i++) {
				AgriDto dto = new AgriDto();
				dto.title = data.child.get(i).name;
				dto.showType = "local";
				dto.dataUrl = data.child.get(i).dataUrl;
				tableList.add(dto);
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
					if (mRadarThread != null) {
						mRadarThread.cancel();
						mRadarThread = null;
					}
					getRadarData(dto.dataUrl);
				}
			}
		});
	}
	
	/**
	 * 切换数据
	 */
	private void switchData() {
		if (reContainer.getVisibility() == View.GONE) {
			startAnimation(false, reContainer);
			reContainer.setVisibility(View.VISIBLE);
			ivArrow.setImageResource(R.drawable.iv_arrow_up);
		}else {
			startAnimation(true, reContainer);
			reContainer.setVisibility(View.GONE);
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
	
	/**
	 * 获取雷达图片集信息
	 */
	private void getRadarData(String url) {
		initDialog();
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(url);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private String method = "GET";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTask() {
		}

		@Override
		protected String doInBackground(String... url) {
			String result = null;
			if (method.equalsIgnoreCase("POST")) {
				result = CustomHttpClient.post(url[0], nvpList);
			} else if (method.equalsIgnoreCase("GET")) {
				result = CustomHttpClient.get(url[0]);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			dismissDialog();
			if (result != null) {
				try {
					JSONObject obj = new JSONObject(result.toString());
					if (!obj.isNull("imgs")) {
						radarList.clear();
						JSONArray array = obj.getJSONArray("imgs");
						for (int i = 0; i < array.length(); i++) {
							JSONObject itemObj = array.getJSONObject(i);
							RadarDto dto = new RadarDto();
							if (!itemObj.isNull("i")) {
								dto.url = itemObj.getString("i");
							}
							if (!itemObj.isNull("n")) {
								dto.time = itemObj.getString("n");
							}
							if (i == array.length()-1) {
								dto.isSelect = "1";
							}else {
								dto.isSelect = "0";
							}
							radarList.add(dto);
							
							if (i == array.length()-1) {
								if (!TextUtils.isEmpty(dto.url)) {
									downloadPortrait(dto.url);
									tvTime.setText(dto.time);
								}
							}
						}
					}
					
					if (seekBar != null) {
						seekBar.setMax(radarList.size());
						seekBar.setProgress(radarList.size());
					}
					
					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
					}
					
					if (radarList.size() <= 0) {
						cancelDialog();
						imageView.setImageResource(R.drawable.iv_no_pic);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		@SuppressWarnings("unused")
		private void setParams(NameValuePair nvp) {
			nvpList.add(nvp);
		}

		private void setMethod(String method) {
			this.method = method;
		}

		private void setTimeOut(int timeOut) {
			CustomHttpClient.TIME_OUT = timeOut;
		}

		/**
		 * 取消当前task
		 */
		@SuppressWarnings("unused")
		private void cancelTask() {
			CustomHttpClient.shuttdownRequest();
			this.cancel(true);
		}
	}
	
	/**
	 * 下载头像保存在本地
	 */
	private void downloadPortrait(String imgUrl) {
		AsynLoadTask task = new AsynLoadTask(new AsynLoadCompleteListener() {
			@Override
			public void loadComplete(Bitmap bitmap) {
				if (bitmap != null) {
					imageView.setImageBitmap(bitmap);
					llSeekBar.setVisibility(View.VISIBLE);
				}
			}
		}, imgUrl);  
        task.execute();
	}
	
	private interface AsynLoadCompleteListener {
		public void loadComplete(Bitmap bitmap);
	}
    
	private class AsynLoadTask extends AsyncTask<Void, Bitmap, Bitmap> {
		
		private String imgUrl;
		private AsynLoadCompleteListener completeListener;

		private AsynLoadTask(AsynLoadCompleteListener completeListener, String imgUrl) {
			this.imgUrl = imgUrl;
			this.completeListener = completeListener;
		}

		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected void onProgressUpdate(Bitmap... values) {
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			Bitmap bitmap = CommonUtil.getHttpBitmap(imgUrl);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (completeListener != null) {
				completeListener.loadComplete(bitmap);
            }
		}
	}
	
	/**
	 * 初始化gridview
	 */
	private void initGridView() {
		mGridView = (GridView) findViewById(R.id.gridView);
		mAdapter = new WeatherRadarDetailAdpater(mContext, radarList);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				for (int i = 0; i < radarList.size(); i++) {
					if (i == arg2) {
						radarList.get(arg2).isSelect = selected;
					}else {
						radarList.get(i).isSelect = unselected;
					}
				}
				mAdapter.notifyDataSetChanged();
				
				if (seekBar != null && radarList.size() > 0) {
					seekBar.setProgress(arg2+1);
				}
				
				RadarDto dto = radarList.get(arg2);
				if (dto.url != null) {
					downloadPortrait(dto.url);
					tvTime.setText(dto.time);
				}
			}
		});
	}
	
	private void startDownLoadImgs(List<RadarDto> list) {
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
		if (list.size() > 0) {
			mRadarManager.loadImagesAsyn(list, this);
		}
	}
	
	@Override
	public void onResult(int result, List<RadarDto> images) {
		mHandler.sendEmptyMessage(HANDLER_LOAD_FINISHED);
		if (result == RadarListener.RESULT_SUCCESSED) {
			if (mRadarThread != null) {
				mRadarThread.cancel();
				mRadarThread = null;
			}
			if (images.size() > 0) {
				mRadarThread = new RadarThread(images);
				mRadarThread.start();
			}
		}
	}
	
	private class RadarThread extends Thread {
		static final int STATE_NONE = 0;
		static final int STATE_PLAYING = 1;
		static final int STATE_PAUSE = 2;
		static final int STATE_CANCEL = 3;
		private List<RadarDto> images;
		private int state;
		private int index;
		private int count;
		private boolean isTracking = false;
		
		public RadarThread(List<RadarDto> images) {
			this.images = images;
			this.count = images.size();
			this.index = 0;
			this.state = STATE_NONE;
			this.isTracking = false;
		}
		
		public int getCurrentState() {
			return state;
		}
		
		@Override
		public void run() {
			super.run();
			this.state = STATE_PLAYING;
			while (true) {
				if (state == STATE_CANCEL) {
					break;
				}
				if (state == STATE_PAUSE) {
					continue;
				}
				if (isTracking) {
					continue;
				}
				sendRadar();
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void sendRadar() {
			if (index >= count || index < 0) {
				index = 0;
				
				if (mRadarThread != null) {
					mRadarThread.pause();
					
					Message message = mHandler.obtainMessage();
					message.what = HANDLER_PAUSE;
					mHandler.sendMessage(message);
					if (seekBar != null) {
						seekBar.setProgress(radarList.size());
					}
				}
			}else {
				RadarDto radar = images.get(index);
				Message message = mHandler.obtainMessage();
				message.what = HANDLER_SHOW_RADAR;
				message.obj = radar;
				message.arg1 = count - 1;
				message.arg2 = index ++;
				mHandler.sendMessage(message);
			}
			
		}
		
		public void cancel() {
			this.state = STATE_CANCEL;
		}
		public void pause() {
			this.state = STATE_PAUSE;
		}
		public void play() {
			this.state = STATE_PLAYING;
		}
		
		public void setCurrent(int index) {
			this.index = index;
		}
		
		public void startTracking() {
			isTracking = true;
		}
		
		public void stopTracking() {
			isTracking = false;
			if (this.state == STATE_PAUSE) {
				sendRadar();
			}
		}
	}

	@Override
	public void onProgress(String url, int progress) {
		Message msg = new Message();
		msg.obj = progress;
		msg.what = HANDLER_PROGRESS;
		mHandler.sendMessage(msg);
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case HANDLER_SHOW_RADAR: 
				if (msg.obj != null) {
					RadarDto radar = (RadarDto) msg.obj;
					if (radar != null) {
						Bitmap bitmap = BitmapFactory.decodeFile(radar.url);
						if (bitmap != null) {
							imageView.setImageBitmap(bitmap);
						}
					}
					changeProgress(radar.time, msg.arg2, msg.arg1);
					
					for (int i = 0; i < radarList.size(); i++) {
						if (i == msg.arg2) {
							radarList.get(msg.arg2).isSelect = selected;
						}else {
							radarList.get(i).isSelect = unselected;
						}
					}
					mAdapter.notifyDataSetChanged();
				}
				break;
			case HANDLER_PROGRESS: 
				if (dialog != null) {
					if (msg.obj != null) {
						int progress = (Integer) msg.obj;
						dialog.setPercent(progress);
					}
				}
				break;
			case HANDLER_LOAD_FINISHED: 
				dismissDialog();
				if (ivPlay != null) {
					ivPlay.setImageResource(R.drawable.iv_pause);
				}
				break;
			case HANDLER_PAUSE:
				if (ivPlay != null) {
					ivPlay.setImageResource(R.drawable.iv_play);
				}
				break;
			default:
				break;
			}
			
		};
	};
	
	private void changeProgress(String time, int progress, int max) {
		if (seekBar != null) {
			seekBar.setMax(max);
			seekBar.setProgress(progress);
		}
		tvTime.setText(time);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mRadarManager != null) {
			mRadarManager.onDestory();
		}
		if (mRadarThread != null) {
			mRadarThread.cancel();
			mRadarThread = null;
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.llBack) {
			finish();
		}else if (v.getId() == R.id.ivPlay) {
			if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PLAYING) {
				mRadarThread.pause();
				ivPlay.setImageResource(R.drawable.iv_play);
			} else if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PAUSE) {
				mRadarThread.play();
				ivPlay.setImageResource(R.drawable.iv_pause);
			} else if (mRadarThread == null) {
				initDialog();
				startDownLoadImgs(radarList);//开始下载
			}
		}else if (v.getId() == R.id.tvTitle) {
			switchData();
		}else if (v.getId() == R.id.ivArrow) {
			switchData();
		}else if (v.getId() == R.id.imageView) {
			for (int i = 0; i < radarList.size(); i++) {
				if (radarList.get(i).isSelect.equals("1")) {
					if (mRadarThread != null && mRadarThread.getCurrentState() == RadarThread.STATE_PLAYING) {
						mRadarThread.pause();
						ivPlay.setImageResource(R.drawable.iv_play);
					}
					
					Intent intent = new Intent(mContext, RadarZoomActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable("data", radarList.get(i));
					intent.putExtras(bundle);
					startActivity(intent);
					break;
				}
			}
		}
	}

}
