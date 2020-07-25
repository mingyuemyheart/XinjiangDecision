package com.hlj.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hlj.common.CONST;
import com.hlj.utils.OkHttpUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

/**
 * surfaceview 播放视频
 * @author shawn_sun
 */
public class SurfaceViewActivity extends BaseActivity implements View.OnClickListener, SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

	private RelativeLayout reTitle;
	private LinearLayout llBack;
	private TextView tvTitle;
	private SurfaceView surfaceView = null;
	private SurfaceHolder surfaceHolder = null;
	private MediaPlayer mPlayer = null;
	private ImageView ivPlay, ivExpand;
	private int width = 0;
	private Configuration configuration = null;//方向监听器

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_surfaceview);
		showDialog();
		initWidget();
		initSurfaceView();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		configuration = newConfig;
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			showPort();
		}else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			showLand();
		}
		setSurfaceViewLayout();
	}

	/**
	 * 显示竖屏，隐藏横屏
	 */
	private void showPort() {
		reTitle.setVisibility(View.VISIBLE);
		ivExpand.setImageResource(R.drawable.eye_iv_expand);
		fullScreen(false);
	}

	/**
	 * 显示横屏，隐藏竖屏
	 */
	private void showLand() {
		reTitle.setVisibility(View.GONE);
		ivExpand.setImageResource(R.drawable.eye_iv_collose);
		fullScreen(true);
	}

	private void fullScreen(boolean enable) {
		if (enable) {
			WindowManager.LayoutParams lp = getWindow().getAttributes();
			lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			getWindow().setAttributes(lp);
//			getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		} else {
			WindowManager.LayoutParams attr = getWindow().getAttributes();
			attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().setAttributes(attr);
//			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		}
	}

	private void initWidget() {
		reTitle = (RelativeLayout) findViewById(R.id.reTitle);
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		ivPlay = (ImageView) findViewById(R.id.ivPlay);
		ivPlay.setOnClickListener(this);
		ivExpand = (ImageView) findViewById(R.id.ivExpand);
		ivExpand.setOnClickListener(this);

		String title = getIntent().getStringExtra(CONST.ACTIVITY_NAME);
		if (!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}

		showPort();

	}

	private void setSurfaceViewLayout() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getRealMetrics(dm);
		width = dm.widthPixels;

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, width*9/16);
		surfaceView.setLayoutParams(params);
	}

	/**
	 * 初始化surfaceView
	 */
	private void initSurfaceView() {
		surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		surfaceView.setOnClickListener(this);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceHolder.addCallback(this);

		setSurfaceViewLayout();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		surfaceHolder = holder;
		mPlayer = new MediaPlayer();
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mPlayer.setDisplay(holder);
		mPlayer.setOnPreparedListener(this);
		mPlayer.setOnCompletionListener(this);

		String videoUrl = getIntent().getStringExtra(CONST.WEB_URL);
		if (TextUtils.isEmpty(videoUrl)) {
			okHttpList();
		} else {
			playMedia(videoUrl);
		}
	}

	private void playMedia(String videoUrl) {
		//设置显示视频显示在SurfaceView上
		try {
			if (!TextUtils.isEmpty(videoUrl)) {
				mPlayer.setDataSource(videoUrl);
				mPlayer.prepareAsync();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int arg1, int arg2, int arg3) {
		surfaceHolder = holder;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceHolder = holder;
		releaseMediaPlayer();
	}

	@Override
	public void onPrepared(MediaPlayer player) {
		cancelDialog();
		swithVideo();
	}

	private void swithVideo() {
		if (mPlayer != null) {
			if (mPlayer.isPlaying()) {
				mPlayer.pause();
				ivPlay.setImageResource(R.drawable.icon_pause);
			}else {
				mPlayer.start();
				ivPlay.setImageResource(R.drawable.icon_play);
			}
		}
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		handler.removeMessages(1001);
		ivPlay.setVisibility(View.VISIBLE);
		ivPlay.setImageResource(R.drawable.icon_play);
		ivExpand.setVisibility(View.VISIBLE);
	}

	/**
	 * 释放MediaPlayer资源
	 */
	private void releaseMediaPlayer() {
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 1001:
					ivPlay.setVisibility(View.GONE);
					ivExpand.setVisibility(View.GONE);
					break;
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (configuration == null) {
                finish();
            }else {
                if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    finish();
                }else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    return true;
                }
            }
        }
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ivPlay.setVisibility(View.GONE);
		ivExpand.setVisibility(View.GONE);
		releaseMediaPlayer();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.surfaceView:
				if (ivPlay.getVisibility() == View.VISIBLE) {
					ivPlay.setVisibility(View.GONE);
					ivExpand.setVisibility(View.GONE);
				}else {
					ivPlay.setVisibility(View.VISIBLE);
					ivExpand.setVisibility(View.VISIBLE);
					handler.removeMessages(1001);
					Message msg = handler.obtainMessage(1001);
					msg.what = 1001;
					handler.sendMessageDelayed(msg, 5000);
				}
				break;
			case R.id.ivPlay:
				swithVideo();
				break;
			case R.id.ivExpand:
				if (configuration == null) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}else {
					if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					}else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					}
				}
				break;
		}
	}

	private void okHttpList() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String url = "https://decision-admin.tianqi.cn/Home/work2019/hlg_getVideos";
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(@NotNull Call call, @NotNull IOException e) {
					}
					@Override
					public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
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
										String type = null;
										if (!obj.isNull("type")) {
											type = obj.getString("type");
										}
										if (!obj.isNull("l")) {
											JSONArray array = obj.getJSONArray("l");
											if (array.length() > 0) {
												JSONObject itemObj = array.getJSONObject(0);
												if (!itemObj.isNull("l1")) {
													String title = itemObj.getString("l1");
													if (title != null) {
														tvTitle.setText(title);
													}
												}
												if (!itemObj.isNull("l2")) {
													String dataUrl = itemObj.getString("l2");
													playMedia(dataUrl);
												}
											}
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
						});
					}
				});
			}
		}).start();
	}

}
