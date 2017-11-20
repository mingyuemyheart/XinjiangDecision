/**
 * Copyright (c) 2012-2013, Michael Yang 杨福海 (www.yangfuhai.com).
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
package net.tsz.afinal.bitmap.core;

import net.tsz.afinal.bitmap.core.BytesBufferPool.BytesBuffer;
import net.tsz.afinal.bitmap.download.Downloader;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.scene.file.crypt.util.Crypter;

public class BitmapProcess {
	private Downloader mDownloader;
	private BitmapCache mCache;
	
	private static final int BYTESBUFFE_POOL_SIZE = 4;
    private static final int BYTESBUFFER_SIZE = 200 * 1024;
    private static final BytesBufferPool sMicroThumbBufferPool = new BytesBufferPool(BYTESBUFFE_POOL_SIZE, BYTESBUFFER_SIZE);

	public BitmapProcess(Downloader downloader,BitmapCache cache) {
		this.mDownloader = downloader;
		this.mCache = cache;
	}

	public Bitmap getBitmap(String url, BitmapDisplayConfig config, Crypter crypter, int corner) {
		
		Bitmap bitmap = getFromDisk(url,config);
		
		if(bitmap == null){
			byte[] data = mDownloader.download(url, crypter);
			if(data != null && data.length > 0){
				if(config !=null)
					bitmap =  BitmapDecoder.decodeSampledBitmapFromByteArray(data,0,data.length,config.getBitmapWidth(),config.getBitmapHeight());
				else
					bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
				
				mCache.addToDiskCache(url, data);
			}
		}
		
		if (corner > 0) {
			bitmap = getRoundedCornerBitmap(bitmap, corner);
		} 
		
		return bitmap;
	}
	
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int corner) {
	    try {
	        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
	                bitmap.getHeight(), Config.ARGB_8888);
	        Canvas canvas = new Canvas(output);                
	        final Paint paint = new Paint();
	        final Rect rect = new Rect(0, 0, bitmap.getWidth(),
	                bitmap.getHeight());       
	        final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(),
	                bitmap.getHeight()));
	        paint.setAntiAlias(true);
	        canvas.drawARGB(0, 0, 0, 0);
	        paint.setColor(Color.BLACK);       
	        canvas.drawRoundRect(rectF, corner, corner, paint);
	        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));            
	  
	        final Rect src = new Rect(0, 0, bitmap.getWidth(),
	                bitmap.getHeight());
	          
	        canvas.drawBitmap(bitmap, src, rect, paint);   
	        bitmap.recycle();
	        return output;
	    } catch (Exception e) {        
	        return bitmap;
	    }
	}
	 
	
	
	public Bitmap getFromDisk(String key,BitmapDisplayConfig config) {
        BytesBuffer buffer = sMicroThumbBufferPool.get();
        Bitmap b = null;
        try {
        	boolean found = mCache.getImageData(key, buffer);
            if ( found && buffer.length - buffer.offset > 0) {
    	        if( config != null){
    	            b = BitmapDecoder.decodeSampledBitmapFromByteArray(buffer.data,buffer.offset, buffer.length ,config.getBitmapWidth(),config.getBitmapHeight());
    	        }else{
    	        	b = BitmapFactory.decodeByteArray(buffer.data, buffer.offset, buffer.length);
    	        }
            }
        } finally {
        	sMicroThumbBufferPool.recycle(buffer);
        }
        return b;
    }

	
}
