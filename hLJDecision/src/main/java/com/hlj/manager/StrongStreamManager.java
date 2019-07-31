package com.hlj.manager;

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
import java.util.List;

/**
 * 分钟降水与强对流
 */
public class StrongStreamManager {

	private Context mContext;
	private LoadThread mLoadThread;

	public interface StrongStreamListener {
		int RESULT_SUCCESSED = 1;
		int RESULT_FAILED = 2;
		void onResult(int result, List<StrongStreamDto> images);
		void onProgress(String url, int progress);
	}

	public StrongStreamManager(Context context) {
		mContext = context.getApplicationContext();
	}
	
	public void loadImagesAsyn(List<StrongStreamDto> radars, StrongStreamListener listener) {
		if (mLoadThread != null) {
			mLoadThread.cancel();
			mLoadThread = null;
		}
		mLoadThread = new LoadThread(radars, listener);
		mLoadThread.start();
	}
	
	private class LoadThread extends Thread {
		private List<StrongStreamDto> radars;
		private StrongStreamListener listener;
		private int count;
		
		public LoadThread(List<StrongStreamDto> radars, StrongStreamListener listener) {
			this.radars = radars;
			this.listener = listener;
		}
		
		@Override
		public void run() {
			super.run();
			int len = count = radars.size();
			for (int i = 0; i < len ; i++) {
				StrongStreamDto radar = radars.get(i);
				String url = radar.imgUrl;
				loadImage(i, url, radars);
			}
		}
		
		private void loadImage(final int index, final String url, final List<StrongStreamDto> radars) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					String path = decodeFromUrl(url, index);//图片下载后存放的路径
					if (!TextUtils.isEmpty(path)) {
						radars.get(index).path = path;
					}
					finished(path, radars);
				}
			}).start();
		}
		
		private String decodeFromUrl(String url, int index){
		    try {
		    	URLConnection connection = new URL(url).openConnection();
		    	connection.setConnectTimeout(2000);
		    	connection.connect();
		    	
				try {
					File file = new File(getDir() + "/"+index+".png");
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
		
		private synchronized void finished(String path, List<StrongStreamDto> radars) {
			int max = radars.size();
			count -- ;
			int progress = (int) (((max - count) * 1.0 / max) * 100);
			if (listener != null) {
				listener.onProgress(path, progress);
				if (count <= 0) {
					listener.onResult(radars == null ? StrongStreamListener.RESULT_FAILED : StrongStreamListener.RESULT_SUCCESSED, radars);
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
