package com.hlj.activity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.exoplayer.DemoPlayer;
import com.google.android.exoplayer.exoplayer.PlayerUtils;
import com.hlj.common.CONST;

import shawn.cxwl.com.hlj.R;

/**
 * 视频会商-直播页面
 */
public class ShawnWeatherMeetingDetailActivity extends BaseActivity implements SurfaceHolder.Callback, DemoPlayer.Listener, OnClickListener {
	
	private SurfaceView surfaceView; // 播放区
	private DemoPlayer player;
	private Uri contentUri; // 视频的uri
	private int contentType;// 流媒体传输协议类型
	private long playerPosition;
	private boolean playerNeedsPrepare;
	
	private ConstraintLayout reTitle = null;
	private TextView tvPrompt = null;
	private Configuration configuration = null;//方向监听器
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shawn_activity_weather_meeting_detail);
		// 常亮（必须加这个）
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		initWidget();
	}
	
	private void initWidget() {
		LinearLayout llBack = findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		TextView tvTitle = findViewById(R.id.tvTitle);
		tvTitle.setText("视频会商");
		reTitle = findViewById(R.id.reTitle);
		surfaceView = findViewById(R.id.surfaceView);
		tvPrompt = findViewById(R.id.tvPrompt);
		
		showPort();
		onShown(getIntent().getStringExtra(CONST.WEB_URL));
	}

	// 获取视频数据
	private void onShown(String url) {
		if (TextUtils.isEmpty(url)) {
			return;
		}
		contentUri = Uri.parse(url);
		contentType = PlayerUtils.inferContentType(contentUri);
		if (player == null) {
			preparePlayer(true);
		} else {
			player.setBackgrounded(false);
		}
	}

	private void preparePlayer(boolean playWhenReady) {
		if (player == null) {
			player = new DemoPlayer(PlayerUtils.getRendererBuilder(this, contentType, contentUri));
			player.addListener(this);
			player.seekTo(playerPosition);// 播放进度的设置
			playerNeedsPrepare = true; // 是否立即播放
		}
		if (playerNeedsPrepare) {
			player.prepare();
			playerNeedsPrepare = false;
		}
		player.setSurface(surfaceView.getHolder().getSurface());
		player.setPlayWhenReady(playWhenReady);
	}

	// surfaceView的监听
	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		if (player != null) {
			player.setSurface(surfaceHolder.getSurface());
		}
	}
	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		if (player != null) {
			player.blockingClearSurface();
		}
	}

	@Override
	public void onStateChanged(boolean playWhenReady, int playbackState) {
			switch (playbackState) {
			case ExoPlayer.STATE_BUFFERING:
//				tvPrompt.setText("正在缓冲,请稍后...");
				break;
			case ExoPlayer.STATE_ENDED:
				tvPrompt.setText("会商直播已结束~~~");
				break;
			case ExoPlayer.STATE_IDLE:// 空的
				tvPrompt.setText("会商直播已结束~~~");
				break;
			case ExoPlayer.STATE_PREPARING:
//				tvPrompt.setText("正在缓冲,请稍后...");
				break;
			case ExoPlayer.STATE_READY:
				break;
			default:

				break;
			}
	}

	@Override
	public void onError(Exception e) {
		playerNeedsPrepare = true;
	}
	// pixelWidthHeightRatio 显示器的宽高比
	@Override
	public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
	}

	private void releasePlayer() {
		if (player != null) {
			playerPosition = player.getCurrentPosition();
			player.release();
			player = null;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		releasePlayer();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
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
	}
	
	/**
	 * 显示竖屏，隐藏横屏
	 */
	private void showPort() {
		reTitle.setVisibility(View.VISIBLE);
		fullScreen(false);
		switchVideo();
	}
	
	/**
	 * 显示横屏，隐藏竖屏
	 */
	private void showLand() {
		reTitle.setVisibility(View.GONE);
		fullScreen(true);
		switchVideo();
	}
	
	/**
	 * 横竖屏切换视频窗口
	 */
	private void switchVideo() {
		if (surfaceView != null) {
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getRealMetrics(dm);
			int width = dm.widthPixels;
			int height = width*9/16;
			LayoutParams params = surfaceView.getLayoutParams();
			params.width = width;
			params.height = height;
			surfaceView.setLayoutParams(params);
		}
	}
	
	private void fullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }
	
	private void exit() {
		if (configuration == null) {
	        finish();
		}else {
			if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
		        finish();
			}else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
		}
		return false;
	}

}
