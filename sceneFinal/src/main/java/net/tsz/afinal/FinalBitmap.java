/**
 * Copyright (c) 2012-2013, Michael Yang 鏉ㄧ娴�(www.yangfuhai.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tsz.afinal;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import net.tsz.afinal.bitmap.core.BitmapCache;
import net.tsz.afinal.bitmap.core.BitmapDisplayConfig;
import net.tsz.afinal.bitmap.core.BitmapProcess;
import net.tsz.afinal.bitmap.display.Displayer;
import net.tsz.afinal.bitmap.display.SimpleDisplayer;
import net.tsz.afinal.bitmap.download.Downloader;
import net.tsz.afinal.bitmap.download.SimpleDownloader;
import net.tsz.afinal.core.AsyncTask;
import net.tsz.afinal.utils.Utils;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.scene.file.crypt.util.Crypter;

public class FinalBitmap {
	private FinalBitmapConfig mConfig;
	private BitmapCache mImageCache;
	private BitmapProcess mBitmapProcess;
	private boolean mExitTasksEarly = false;
	private boolean mPauseWork = false;
	private final Object mPauseWorkLock = new Object();
	private Context mContext;
	private boolean mInit = false ;
	private ExecutorService bitmapLoadAndDisplayExecutor;

	private static FinalBitmap mFinalBitmap;
	
	////////////////////////// config method start////////////////////////////////////
	private FinalBitmap(Context context) {
		mContext = context;
		mConfig = new FinalBitmapConfig(context);
		configDiskCachePath(Utils.getDiskCacheDir(context, "afinalCache").getAbsolutePath());//閰嶇疆缂撳瓨璺緞
		configDisplayer(new SimpleDisplayer());//閰嶇疆鏄剧ず鍣�		
		configDownlader(new SimpleDownloader());//閰嶇疆涓嬭浇鍣�	
		}
	
	/**
	 * 鍒涘缓finalbitmap
	 * @param ctx
	 * @return
	 */
	public static synchronized FinalBitmap create(Context ctx){
		if(mFinalBitmap == null){
			mFinalBitmap = new FinalBitmap(ctx.getApplicationContext());
		}
		return mFinalBitmap;
	}
	
	
	
	/**
	 * 璁剧疆鍥剧墖姝ｅ湪鍔犺浇鐨勬椂鍊欐樉绀虹殑鍥剧墖
	 * @param bitmap
	 */
	public FinalBitmap configLoadingImage(Bitmap bitmap) {
		mConfig.defaultDisplayConfig.setLoadingBitmap(bitmap);
		return this;
	}

	/**
	 * 璁剧疆鍥剧墖姝ｅ湪鍔犺浇鐨勬椂鍊欐樉绀虹殑鍥剧墖
	 * @param bitmap
	 */
	public FinalBitmap configLoadingImage(int resId) {
		mConfig.defaultDisplayConfig.setLoadingBitmap(BitmapFactory.decodeResource(mContext.getResources(), resId));
		return this;
	}
	
	/**
	 * 璁剧疆鍥剧墖鍔犺浇澶辫触鏃跺�鏄剧ず鐨勫浘鐗�	 * @param bitmap
	 */
	public FinalBitmap configLoadfailImage(Bitmap bitmap) {
		mConfig.defaultDisplayConfig.setLoadfailBitmap(bitmap);
		return this;
	}
	
	/**
	 * 璁剧疆鍥剧墖鍔犺浇澶辫触鏃跺�鏄剧ず鐨勫浘鐗�	 * @param resId
	 */
	public FinalBitmap configLoadfailImage(int resId) {
		mConfig.defaultDisplayConfig.setLoadfailBitmap(BitmapFactory.decodeResource(mContext.getResources(), resId));
		return this;
	}
	
	
	/**
	 * 閰嶇疆榛樿鍥剧墖鐨勫皬鐨勯珮搴�	 * @param bitmapHeight
	 */
	public FinalBitmap configBitmapMaxHeight(int bitmapHeight){
		mConfig.defaultDisplayConfig.setBitmapHeight(bitmapHeight);
		return this;
	}
	
	/**
	 * 閰嶇疆榛樿鍥剧墖鐨勫皬鐨勫搴�	 * @param bitmapHeight
	 */
	public FinalBitmap configBitmapMaxWidth(int bitmapWidth){
		mConfig.defaultDisplayConfig.setBitmapWidth(bitmapWidth);
		return this;
	}
	
	/**
	 * 璁剧疆涓嬭浇鍣紝姣斿閫氳繃ftp鎴栬�鍏朵粬鍗忚鍘荤綉缁滆鍙栧浘鐗囩殑鏃跺�鍙互璁剧疆杩欓」
	 * @param downlader
	 * @return
	 */
	public FinalBitmap configDownlader(Downloader downlader){
		mConfig.downloader = downlader;
		return this;
	}
	
	/**
	 * 璁剧疆鏄剧ず鍣紝姣斿鍦ㄦ樉绀虹殑杩囩▼涓樉绀哄姩鐢荤瓑
	 * @param displayer
	 * @return
	 */
	public FinalBitmap configDisplayer(Displayer displayer){
		mConfig.displayer = displayer;
		return this;
	}
	
	
	/**
	 * 閰嶇疆纾佺洏缂撳瓨璺緞
	 * @param strPath
	 * @return
	 */
	public FinalBitmap configDiskCachePath(String strPath){
		if(!TextUtils.isEmpty(strPath)){
			mConfig.cachePath = strPath;
		}
		return this;
	}

	/**
	 * 閰嶇疆鍐呭瓨缂撳瓨澶у皬 澶т簬2MB浠ヤ笂鏈夋晥
	 * @param size 缂撳瓨澶у皬
	 */
	public FinalBitmap configMemoryCacheSize(int size){
		mConfig.memCacheSize = size;
		return this;
	}
	
	/**
	 * 璁剧疆搴旂紦瀛樼殑鍦ˋPK鎬诲唴瀛樼殑鐧惧垎姣旓紝浼樺厛绾уぇ浜巆onfigMemoryCacheSize
	 * @param percent 鐧惧垎姣旓紝鍊肩殑鑼冨洿鏄湪 0.05 鍒�0.8涔嬮棿
	 */
	public FinalBitmap configMemoryCachePercent(float percent){
		mConfig.memCacheSizePercent = percent;
		return this;
	}
	
	/**
	 * 璁剧疆纾佺洏缂撳瓨澶у皬 5MB 浠ヤ笂鏈夋晥
	 * @param size
	 */
	public FinalBitmap configDiskCacheSize(int size){
		mConfig.diskCacheSize = size;
		return this;
	} 
	
	/**
	 * 璁剧疆鍔犺浇鍥剧墖鐨勭嚎绋嬪苟鍙戞暟閲�	 * @param size
	 */
	public FinalBitmap configBitmapLoadThreadSize(int size){
		if(size >= 1)
			mConfig.poolSize = size;
		return this;
	}
	
	/**
	 * 閰嶇疆鏄惁绔嬪嵆鍥炴敹鍥剧墖璧勬簮
	 * @param recycleImmediately
	 * @return
	 */
	public FinalBitmap configRecycleImmediately(boolean recycleImmediately){
		mConfig.recycleImmediately = recycleImmediately;
		return this;
	} 
	
	/**
	 * 鍒濆鍖杅inalBitmap
	 * @return
	 */
	private FinalBitmap init(){
		
		if( ! mInit ){
			
			BitmapCache.ImageCacheParams imageCacheParams = new BitmapCache.ImageCacheParams(mConfig.cachePath);
			if(mConfig.memCacheSizePercent>0.05 && mConfig.memCacheSizePercent<0.8){
				imageCacheParams.setMemCacheSizePercent(mContext, mConfig.memCacheSizePercent);
			}else{
				if(mConfig.memCacheSize > 1024 * 1024 * 2){
					imageCacheParams.setMemCacheSize(mConfig.memCacheSize);	
				}else{
					//璁剧疆榛樿鐨勫唴瀛樼紦瀛樺ぇ灏�					imageCacheParams.setMemCacheSizePercent(mContext, 0.3f);
				}
			}
			if(mConfig.diskCacheSize > 1024 * 1024 * 5)
				imageCacheParams.setDiskCacheSize(mConfig.diskCacheSize);
			
			imageCacheParams.setRecycleImmediately(mConfig.recycleImmediately);
			//init Cache
			mImageCache = new BitmapCache(imageCacheParams);
			
			//init Executors
			bitmapLoadAndDisplayExecutor = Executors.newFixedThreadPool(mConfig.poolSize,new ThreadFactory() {
				@Override
	            public Thread newThread(Runnable r) {
					Thread t = new Thread(r);
					// 璁剧疆绾跨▼鐨勪紭鍏堢骇鍒紝璁╃嚎绋嬪厛鍚庨『搴忔墽琛岋紙绾у埆瓒婇珮锛屾姠鍒癱pu鎵ц鐨勬椂闂磋秺澶氾級
					t.setPriority(Thread.NORM_PRIORITY - 1);
					return t;
				}
			});
			
			//init BitmapProcess
			mBitmapProcess = new BitmapProcess(mConfig.downloader,mImageCache);
			
			mInit = true ;
		}
		
		return this;
	}
	
	////////////////////////// config method end////////////////////////////////////
	
	public void display(View imageView,String uri, Crypter crypter, int corner){
		doDisplay(imageView,uri,null, crypter, corner);
	}

	public void display(View imageView,String uri,int imageWidth,int imageHeight, Crypter crypter, int corner){
		BitmapDisplayConfig displayConfig = configMap.get(imageWidth+"_"+imageHeight);
		if(displayConfig==null){
			displayConfig = getDisplayConfig();
			displayConfig.setBitmapHeight(imageHeight);
			displayConfig.setBitmapWidth(imageWidth);
			configMap.put(imageWidth+"_"+imageHeight, displayConfig);
		}
		
		doDisplay(imageView,uri,displayConfig, crypter, corner);
	}
	
	public void display(View imageView,String uri,Bitmap loadingBitmap, Crypter crypter, int corner){
		BitmapDisplayConfig displayConfig = configMap.get(String.valueOf(loadingBitmap));
		if(displayConfig==null){
			displayConfig = getDisplayConfig();
			displayConfig.setLoadingBitmap(loadingBitmap);
			configMap.put(String.valueOf(loadingBitmap), displayConfig);
		}
		
		doDisplay(imageView,uri,displayConfig, crypter, corner);
	}
	
	
	public void display(View imageView,String uri,Bitmap loadingBitmap,Bitmap laodfailBitmap, Crypter crypter, int corner){
		BitmapDisplayConfig displayConfig = configMap.get(String.valueOf(loadingBitmap)+"_"+String.valueOf(laodfailBitmap));
		if(displayConfig==null){
			displayConfig = getDisplayConfig();
			displayConfig.setLoadingBitmap(loadingBitmap);
			displayConfig.setLoadfailBitmap(laodfailBitmap);
			configMap.put(String.valueOf(loadingBitmap)+"_"+String.valueOf(laodfailBitmap), displayConfig);
		}
		
		doDisplay(imageView,uri,displayConfig, crypter, corner);
	}
	
	public void display(View imageView,String uri,int imageWidth,int imageHeight,Bitmap loadingBitmap,Bitmap laodfailBitmap, Crypter crypter, int corner){
		BitmapDisplayConfig displayConfig = configMap.get(imageWidth+"_"+imageHeight+"_"+String.valueOf(loadingBitmap)+"_"+String.valueOf(laodfailBitmap));
		if(displayConfig==null){
			displayConfig = getDisplayConfig();
			displayConfig.setBitmapHeight(imageHeight);
			displayConfig.setBitmapWidth(imageWidth);
			displayConfig.setLoadingBitmap(loadingBitmap);
			displayConfig.setLoadfailBitmap(laodfailBitmap);
			configMap.put(imageWidth+"_"+imageHeight+"_"+String.valueOf(loadingBitmap)+"_"+String.valueOf(laodfailBitmap), displayConfig);
		}
		
		doDisplay(imageView,uri,displayConfig, crypter, corner);
	}
	
	
	public void display(View imageView,String uri,BitmapDisplayConfig config, Crypter crypter, int corner){
		doDisplay(imageView,uri,config, crypter, corner);
	}
	
	private void doDisplay(View imageView, String uri, BitmapDisplayConfig displayConfig, Crypter crypter, int corner) {
		if(!mInit ){
			init();
		}
		
		if (TextUtils.isEmpty(uri) || imageView == null) {
			return;
		}
		
		if(displayConfig == null)
			displayConfig = mConfig.defaultDisplayConfig;
	
		Bitmap bitmap = null;
	
		if (mImageCache != null) {
			bitmap = mImageCache.getBitmapFromMemoryCache(uri);
		}
	
		if (bitmap != null) {
			if(imageView instanceof ImageView){
				((ImageView)imageView).setImageBitmap(bitmap);
			}else{
				imageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}
			
			
		}else if (checkImageTask(uri, imageView)) {
			final BitmapLoadAndDisplayTask task = new BitmapLoadAndDisplayTask(imageView, displayConfig, crypter, corner);
			//璁剧疆榛樿鍥剧墖
			final AsyncDrawable asyncDrawable = new AsyncDrawable(mContext.getResources(), displayConfig.getLoadingBitmap(), task);
	       
			if(imageView instanceof ImageView){
				((ImageView)imageView).setImageDrawable(asyncDrawable);
			}else{
				imageView.setBackgroundDrawable(asyncDrawable);
			}
	        
	        task.executeOnExecutor(bitmapLoadAndDisplayExecutor, uri);
	    }
	}
	
	private HashMap<String, BitmapDisplayConfig> configMap = new HashMap<String, BitmapDisplayConfig>();
	
	
	private BitmapDisplayConfig getDisplayConfig(){
		BitmapDisplayConfig config = new BitmapDisplayConfig();
		config.setAnimation(mConfig.defaultDisplayConfig.getAnimation());
		config.setAnimationType(mConfig.defaultDisplayConfig.getAnimationType());
		config.setBitmapHeight(mConfig.defaultDisplayConfig.getBitmapHeight());
		config.setBitmapWidth(mConfig.defaultDisplayConfig.getBitmapWidth());
		config.setLoadfailBitmap(mConfig.defaultDisplayConfig.getLoadfailBitmap());
		config.setLoadingBitmap(mConfig.defaultDisplayConfig.getLoadingBitmap());
		return config;
	}
	

	private void clearCacheInternalInBackgroud() {
		if (mImageCache != null) {
			mImageCache.clearCache();
		}
	}
	
	
	
	private void clearDiskCacheInBackgroud(){
		if (mImageCache != null) {
			mImageCache.clearDiskCache();
		}
	}
	
	
	private void clearCacheInBackgroud(String key){
		if (mImageCache != null) {
			mImageCache.clearCache(key);
		}
	}
	
	private void clearDiskCacheInBackgroud(String key){
		if (mImageCache != null) {
			mImageCache.clearDiskCache(key);
		}
	}
	

    /**
     * 鎵ц杩囨鏂规硶鍚�FinalBitmap鐨勭紦瀛樺凡缁忓け鏁�寤鸿閫氳繃FinalBitmap.create()鑾峰彇鏂扮殑瀹炰緥
     * @author fantouch
     */
	private void closeCacheInternalInBackgroud() {
		if (mImageCache != null) {
			mImageCache.close();
			mImageCache = null;
            mFinalBitmap = null;
		}
	}

	/**
	 * 缃戠粶鍔犺浇bitmap
	 * @param data
	 * @return
	 */
	private Bitmap processBitmap(String uri,BitmapDisplayConfig config, Crypter crypter, int corner) {
		if (mBitmapProcess != null) {
			return mBitmapProcess.getBitmap(uri,config, crypter, corner);
		}
		return null;
	}
	
	/**
	 * 浠庣紦瀛橈紙鍐呭瓨缂撳瓨鍜岀鐩樼紦瀛橈級涓洿鎺ヨ幏鍙朾itmap锛屾敞鎰忚繖閲屾湁io鎿嶄綔锛屾渶濂戒笉瑕佹斁鍦╱i绾跨▼鎵ц
	 * @param key
	 * @return
	 */
	public Bitmap getBitmapFromCache(String key){
		Bitmap bitmap  = getBitmapFromMemoryCache(key);
		if(bitmap == null)
			bitmap = getBitmapFromDiskCache(key);
		
		return bitmap;
	}
	
	/**
	 * 浠庡唴瀛樼紦瀛樹腑鑾峰彇bitmap
	 * @param key
	 * @return
	 */
	public Bitmap getBitmapFromMemoryCache(String key){
		return mImageCache.getBitmapFromMemoryCache(key);
	}
	
	/**
	 * 浠庣鐩樼紦瀛樹腑鑾峰彇bitmap锛岋紝娉ㄦ剰杩欓噷鏈塱o鎿嶄綔锛屾渶濂戒笉瑕佹斁鍦╱i绾跨▼鎵ц
	 * @param key 
	 * @return
	 */
	public Bitmap getBitmapFromDiskCache(String key){
		return getBitmapFromDiskCache(key,null);
	}
	
	public Bitmap getBitmapFromDiskCache(String key,BitmapDisplayConfig config){
		return mBitmapProcess.getFromDisk(key, config);
	}
	
	public void setExitTasksEarly(boolean exitTasksEarly) {
		mExitTasksEarly = exitTasksEarly;
	}
	
	/**
     * activity onResume鐨勬椂鍊欒皟鐢ㄨ繖涓柟娉曪紝璁╁姞杞藉浘鐗囩嚎绋嬬户缁�     */
    public void onResume(){
    	setExitTasksEarly(false);
    }
    
    /**
     * activity onPause鐨勬椂鍊欒皟鐢ㄨ繖涓柟娉曪紝璁╃嚎绋嬫殏鍋�     */
    public void onPause() {
        setExitTasksEarly(true);
    }
    
    /**
     * activity onDestroy鐨勬椂鍊欒皟鐢ㄨ繖涓柟娉曪紝閲婃斁缂撳瓨
     * 鎵ц杩囨鏂规硶鍚�FinalBitmap鐨勭紦瀛樺凡缁忓け鏁�寤鸿閫氳繃FinalBitmap.create()鑾峰彇鏂扮殑瀹炰緥
     * 
     * @author fantouch
     */
    public void onDestroy() {
        closeCache();
    }

	/**
	 * 娓呴櫎鎵�湁缂撳瓨锛堢鐩樺拰鍐呭瓨锛�	 */
	public void clearCache() {
		new CacheExecutecTask().execute(CacheExecutecTask.MESSAGE_CLEAR);
	}
	
	/**
	 * 鏍规嵁key娓呴櫎鎸囧畾鐨勫唴瀛樼紦瀛�	 * @param key
	 */
	public void clearCache(String key) {
		new CacheExecutecTask().execute(CacheExecutecTask.MESSAGE_CLEAR_KEY,key);
	}
	
	/**
	 * 娓呴櫎缂撳瓨
	 */
	public void clearMemoryCache() {
		if(mImageCache!=null)
			mImageCache.clearMemoryCache();
	}
	
	/**
	 * 鏍规嵁key娓呴櫎鎸囧畾鐨勫唴瀛樼紦瀛�	 * @param key
	 */
	public void clearMemoryCache(String key) {
		if(mImageCache!=null)
			mImageCache.clearMemoryCache(key);
	}
	
	
	/**
	 * 娓呴櫎纾佺洏缂撳瓨
	 */
	public void clearDiskCache() {
		new CacheExecutecTask().execute(CacheExecutecTask.MESSAGE_CLEAR_DISK);
	}
	
	/**
	 * 鏍规嵁key娓呴櫎鎸囧畾鐨勫唴瀛樼紦瀛�	 * @param key
	 */
	public void clearDiskCache(String key) {
		new CacheExecutecTask().execute(CacheExecutecTask.MESSAGE_CLEAR_KEY_IN_DISK,key);
	}
	

    /**
     * 鍏抽棴缂撳瓨
     * 鎵ц杩囨鏂规硶鍚�FinalBitmap鐨勭紦瀛樺凡缁忓け鏁�寤鸿閫氳繃FinalBitmap.create()鑾峰彇鏂扮殑瀹炰緥
     * @author fantouch
     */
	public void closeCache() {
		new CacheExecutecTask().execute(CacheExecutecTask.MESSAGE_CLOSE);
	}

	/**
	 * 閫�嚭姝ｅ湪鍔犺浇鐨勭嚎绋嬶紝绋嬪簭閫�嚭鐨勬椂鍊欒皟鐢ㄨ瘝鏂规硶
	 * @param exitTasksEarly
	 */
	public void exitTasksEarly(boolean exitTasksEarly) {
		mExitTasksEarly = exitTasksEarly;
		if(exitTasksEarly)
			pauseWork(false);//璁╂殏鍋滅殑绾跨▼缁撴潫
	}

	/**
	 * 鏆傚仠姝ｅ湪鍔犺浇鐨勭嚎绋嬶紝鐩戝惉listview鎴栬�gridview姝ｅ湪婊戝姩鐨勬椂鍊欐潯鐢ㄨ瘝鏂规硶
	 * @param pauseWork true鍋滄鏆傚仠绾跨▼锛宖alse缁х画绾跨▼
	 */
	public void pauseWork(boolean pauseWork) {
		synchronized (mPauseWorkLock) {
			mPauseWork = pauseWork;
			if (!mPauseWork) {
				mPauseWorkLock.notifyAll();
			}
		}
	}

	    
	private static BitmapLoadAndDisplayTask getBitmapTaskFromImageView(View imageView) {
		if (imageView != null) {
			Drawable drawable = null;
			if(imageView instanceof ImageView){
				drawable = ((ImageView)imageView).getDrawable();
			}else{
				drawable = imageView.getBackground();
			}
			
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}


	    /**
	     * 妫�祴 imageView涓槸鍚﹀凡缁忔湁绾跨▼鍦ㄨ繍琛�	     * @param data
	     * @param imageView
	     * @return true 娌℃湁 false 鏈夌嚎绋嬪湪杩愯浜�	     */
	public static boolean checkImageTask(Object data, View imageView) {
			final BitmapLoadAndDisplayTask bitmapWorkerTask = getBitmapTaskFromImageView(imageView);

			if (bitmapWorkerTask != null) {
				final Object bitmapData = bitmapWorkerTask.data;
				if (bitmapData == null || !bitmapData.equals(data)) {
					bitmapWorkerTask.cancel(true);
				} else {
					// 鍚屼竴涓嚎绋嬪凡缁忓湪鎵ц
					return false;
				}
			}
			return true;
		}
		
		
		private static class AsyncDrawable extends BitmapDrawable {
			private final WeakReference<BitmapLoadAndDisplayTask> bitmapWorkerTaskReference;

			public AsyncDrawable(Resources res, Bitmap bitmap,BitmapLoadAndDisplayTask bitmapWorkerTask) {
				super(res, bitmap);
				bitmapWorkerTaskReference = new WeakReference<BitmapLoadAndDisplayTask>(
						bitmapWorkerTask);
			}

			public BitmapLoadAndDisplayTask getBitmapWorkerTask() {
				return bitmapWorkerTaskReference.get();
			}
		}


	private class CacheExecutecTask extends AsyncTask<Object, Void, Void> {
		public static final int MESSAGE_CLEAR = 1;
		public static final int MESSAGE_CLOSE = 2;
		public static final int MESSAGE_CLEAR_DISK = 3;
		public static final int MESSAGE_CLEAR_KEY = 4;
		public static final int MESSAGE_CLEAR_KEY_IN_DISK = 5;
		@Override
		protected Void doInBackground(Object... params) {
			switch ((Integer) params[0]) {
			case MESSAGE_CLEAR:
				clearCacheInternalInBackgroud();
				break;
			case MESSAGE_CLOSE:
				closeCacheInternalInBackgroud();
				break;
			case MESSAGE_CLEAR_DISK:
				clearDiskCacheInBackgroud();
				break;
			case MESSAGE_CLEAR_KEY:
				clearCacheInBackgroud(String.valueOf(params[1]));
				break;
			case MESSAGE_CLEAR_KEY_IN_DISK:
				clearDiskCacheInBackgroud(String.valueOf(params[1]));
				break;
			}
			return null;
		}
	}
	
	/**
	 * bitmap涓嬭浇鏄剧ず鐨勭嚎绋�	 * @author michael yang
	 */
	private class BitmapLoadAndDisplayTask extends AsyncTask<Object, Boolean, Bitmap> {
		private Object data;
		private final WeakReference<View> imageViewReference;
		private final BitmapDisplayConfig displayConfig;
		private Crypter crypter;
		private int corner;

		public BitmapLoadAndDisplayTask(View imageView,BitmapDisplayConfig config, Crypter crypter, int corner) {
			imageViewReference = new WeakReference<View>(imageView);
			displayConfig = config;
			this.crypter = crypter;
			this.corner = corner;
		}

		@Override
		protected Bitmap doInBackground(Object... params) {
			data = params[0];
			final String dataString = String.valueOf(data);
			Bitmap bitmap = null;

			synchronized (mPauseWorkLock) {
				while (mPauseWork && !isCancelled()) {
					try {
						mPauseWorkLock.wait();
					} catch (InterruptedException e) {
					}
				}
			}

			if (bitmap == null && !isCancelled()&& getAttachedImageView() != null && !mExitTasksEarly) {
				bitmap = processBitmap(dataString,displayConfig, crypter, corner);
			}
			
			if(bitmap!=null && mImageCache != null){
				mImageCache.addToMemoryCache(dataString, bitmap);
			}

			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled() || mExitTasksEarly) {
				bitmap = null;
			}

			// 鍒ゆ柇绾跨▼鍜屽綋鍓嶇殑imageview鏄惁鏄尮閰�			
			final View imageView = getAttachedImageView();
			if (bitmap != null && imageView != null) {
				mConfig.displayer.loadCompletedisplay(imageView,bitmap,displayConfig);			
			}else if(bitmap == null && imageView!=null ){
				mConfig.displayer.loadFailDisplay(imageView, displayConfig.getLoadfailBitmap());
			}
		}

		@Override
		protected void onCancelled(Bitmap bitmap) {
			super.onCancelled(bitmap);
			synchronized (mPauseWorkLock) {
				mPauseWorkLock.notifyAll();
			}
		}

		/**
		 * 鑾峰彇绾跨▼鍖归厤鐨刬mageView,闃叉鍑虹幇闂姩鐨勭幇璞�		 * @return
		 */
		private View getAttachedImageView() {
			final View imageView = imageViewReference.get();
			final BitmapLoadAndDisplayTask bitmapWorkerTask = getBitmapTaskFromImageView(imageView);

			if (this == bitmapWorkerTask) {
				return imageView;
			}

			return null;
		}
	}
	
	
	/**
	 * @title 閰嶇疆淇℃伅
	 * @description FinalBitmap鐨勯厤缃俊鎭�	 * @company 鎺㈢储鑰呯綉缁滃伐浣滃(www.tsz.net)
	 * @author michael Young (www.YangFuhai.com)
	 * @version 1.0
	 * @created 2012-10-28
	 */
	private class FinalBitmapConfig {
		public String cachePath;
		 public Displayer displayer;
		 public Downloader downloader;
		 public BitmapDisplayConfig defaultDisplayConfig;
		 public float memCacheSizePercent;//缂撳瓨鐧惧垎姣旓紝android绯荤粺鍒嗛厤缁欐瘡涓猘pk鍐呭瓨鐨勫ぇ灏�		 
		 public int memCacheSize;//鍐呭瓨缂撳瓨鐧惧垎姣�		 
		 public int diskCacheSize;//纾佺洏鐧惧垎姣�		 
		 public int poolSize = 3;//榛樿鐨勭嚎绋嬫睜绾跨▼骞跺彂鏁伴噺
		 public boolean recycleImmediately = true;//鏄惁绔嬪嵆鍥炴敹鍐呭瓨
		
		 public FinalBitmapConfig(Context context) {
				defaultDisplayConfig = new BitmapDisplayConfig();
				
				defaultDisplayConfig.setAnimation(null);
				defaultDisplayConfig.setAnimationType(BitmapDisplayConfig.AnimationType.fadeIn);
				
				//璁剧疆鍥剧墖鐨勬樉绀烘渶澶у昂瀵革紙涓哄睆骞曠殑澶у皬,榛樿涓哄睆骞曞搴︾殑1/2锛�				
				DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
				int defaultWidth = (int)Math.floor(displayMetrics.widthPixels/2);
				int defaultHeight = (int)Math.floor(displayMetrics.heightPixels/2);
				defaultDisplayConfig.setBitmapHeight(defaultHeight);
				defaultDisplayConfig.setBitmapWidth(defaultWidth);
				
		}
	}
	
}
