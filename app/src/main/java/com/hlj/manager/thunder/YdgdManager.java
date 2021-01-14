package com.hlj.manager.thunder;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.hlj.dto.StrongStreamDto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ConcurrentModificationException;
import java.util.Map;

/**
 * 云顶高度图下载
 */
public class YdgdManager {

	private Context mContext;
	private LoadThread mLoadThread;

	public interface YdgdListener {
		int RESULT_SUCCESSED = 1;
		int RESULT_FAILED = 2;
		void onResult(int result, Map<String, StrongStreamDto> images);
		void onProgress(String url, int progress);
	}

	public YdgdManager(Context context) {
		mContext = context.getApplicationContext();
	}
	
	public void loadImagesAsyn(Map<String, StrongStreamDto> radars, YdgdListener listener) {
		if (mLoadThread != null) {
			mLoadThread.cancel();
			mLoadThread = null;
		}
		mLoadThread = new LoadThread(radars, listener);
		mLoadThread.start();
	}
	
	private class LoadThread extends Thread {
		private Map<String, StrongStreamDto> radars;
		private YdgdListener listener;
		private int count;
		
		private LoadThread(Map<String, StrongStreamDto> radars, YdgdListener listener) {
			this.radars = radars;
			this.listener = listener;
		}
		
		@Override
		public void run() {
			super.run();
			try {
				for (String startTime : radars.keySet()) {
					if (radars.containsKey(startTime)) {
						StrongStreamDto radar = radars.get(startTime);
						loadImage(startTime, radar.imgUrl, radars);
					}
				}
			}catch (ConcurrentModificationException e) {
				e.printStackTrace();
			}
		}
		
		private void loadImage(final String startTime, final String imgUrl, final Map<String, StrongStreamDto> radars) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						String imgPath = decodeFromUrl(imgUrl, startTime);//图片下载后存放的路径
						if (!TextUtils.isEmpty(imgPath)) {
							radars.get(startTime).imgPath = imgPath;
						}
						finished(imgPath, radars);
					}catch (NullPointerException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
		
		private String decodeFromUrl(String url, String startTime){
		    try {
		    	URLConnection connection = new URL(url).openConnection();
		    	connection.setConnectTimeout(2000);
		    	connection.connect();
		    	
				try {
					File file = new File(getDir() + "/ydgd"+startTime+".png");
					FileOutputStream os = new FileOutputStream(file);
			    	InputStream is = connection.getInputStream();
			    	byte[] buffer = new byte[8 * 1024];
			    	int read = -1;
			    	while ((read = is.read(buffer)) != -1) {
						os.write(buffer, 0, read);
					}
			    	os.flush();
			    	os.close();
			    	System.gc();
			    	return file.getAbsolutePath();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
		    } catch (Exception e) {
		    	Log.e("SceneException", e.getMessage(), e);
		    }
		    return null;
		}
		
		private synchronized void finished(String path, Map<String, StrongStreamDto> radars) {
			int max = radars.size();
			count -- ;
			int progress = (int) (((max - count) * 1.0 / max) * 100);
			if (listener != null) {
				listener.onProgress(path, progress);
				if (count <= 0) {
					listener.onResult(radars == null ? YdgdListener.RESULT_FAILED : YdgdListener.RESULT_SUCCESSED, radars);
				}
			}
		}
		
		void cancel() {
			listener = null;
		}
	}
	
	public void onDestory() {
		File file = getDir();
		if (file != null && file.exists()) {
			File[] files = file.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					return filename.endsWith(".png");
				}
			});
			for (File f : files) {
				f.delete();
			}
		}
	}
	
	private File getDir() {
		return mContext.getCacheDir();
	}
	
}
