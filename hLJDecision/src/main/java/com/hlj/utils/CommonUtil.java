package com.hlj.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.PolylineOptions;

import shawn.cxwl.com.hlj.R;

public class CommonUtil {

	/** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
    public static float dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return dpValue * scale;
    }  
  
    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static float px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return pxValue / scale;
    }

	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	public static String getVersion(Context context) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			return info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
    
    /**
	 * 解决ScrollView与ListView共存的问题
	 * 
	 * @param listView
	 */
	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0); 
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		((MarginLayoutParams) params).setMargins(0, 0, 0, 0);
		listView.setLayoutParams(params);
	}
	
	/**
	 * 解决ScrollView与GridView共存的问题
	 */
	public static void setGridViewHeightBasedOnChildren(GridView gridView) {
		ListAdapter listAdapter = gridView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		
		Class<GridView> tempGridView = GridView.class; // 获得gridview这个类的class
		int column = -1;
        try {
 
            Field field = tempGridView.getDeclaredField("mRequestedNumColumns"); // 获得申明的字段
            field.setAccessible(true); // 设置访问权限
            column = Integer.valueOf(field.get(gridView).toString()); // 获取字段的值
        } catch (Exception e1) {
        }

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i+=column) {
			View listItem = listAdapter.getView(i, null, gridView);
			listItem.measure(0, 0); 
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = gridView.getLayoutParams();
		params.height = totalHeight + (gridView.getVerticalSpacing() * (listAdapter.getCount()/column - 1) + 30);
		((MarginLayoutParams) params).setMargins(15, 15, 15, 0);
		gridView.setLayoutParams(params);
	}
	
	/**
	 * 判断白天或晚上对应的天气现象
	 * @param fa 白天天气现象编号
	 * @param fb 晚上天气现象编号
	 * @return
	 */
	public static int getPheCode(int fa, int fb) {
		int pheCode = 0;
		SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
		
		try {
			long currentTime = new Date().getTime();//当前时间
			long eight = sdf1.parse("08:00").getTime();//早上08:00
			long twenty = sdf1.parse("20:00").getTime();//晚上20:00
			
			if (currentTime >= eight && currentTime <= twenty) {
				pheCode = fa;
			}else {
				pheCode = fb;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return pheCode;
	}
	
	/**
	 * 从Assets中读取图片
	 */
	public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
		Bitmap image = null;
		AssetManager am = context.getResources().getAssets();
		try {
			InputStream is = am.open(fileName);
			image = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}
	
	/**
	 * 获取圆角图片
	 * @param bitmap
	 * @param corner
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int corner) {
		try {
			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(output);
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(Color.BLACK);
			canvas.drawRoundRect(rectF, corner, corner, paint);
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

			final Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			canvas.drawBitmap(bitmap, src, rect, paint);
			bitmap.recycle();
			return output;
		} catch (Exception e) {
			return bitmap;
		}
	}
	
	/**
	 * 隐藏虚拟键盘
	 * @param editText 输入框
	 * @param context 上下文
	 */
	public static void hideInputSoft(EditText editText, Context context) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}
	
	/**
	 * 转换图片成圆形
	 * 
	 * @param bitmap
	 *            传入Bitmap对象
	 * @return
	 */
	public static Bitmap toRoundBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;
			top = 0;
			bottom = width;
			left = 0;
			right = width;
			height = width;
			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;
			float clip = (width - height) / 2;
			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;
			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}
		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
		return output;
	}
	
	/**
     * 获取网落图片资源 
     * @param url
     * @return
     */
	public static Bitmap getHttpBitmap(String url) {
		URL myFileURL;
		Bitmap bitmap = null;
		try {
			myFileURL = new URL(url);
			// 获得连接
			HttpURLConnection conn = (HttpURLConnection) myFileURL.openConnection();
			// 设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
			conn.setConnectTimeout(6000);
			// 连接设置获得数据流
			conn.setDoInput(true);
			// 不使用缓存
			conn.setUseCaches(false);
			// 这句可有可无，没有影响
			conn.connect();
			// 得到数据流
			InputStream is = conn.getInputStream();
			// 解析得到图片
			bitmap = BitmapFactory.decodeStream(is);
			// 关闭数据流
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	/**
	 * 转换图片成六边形
	 * @return
	 */
	public static Bitmap getHexagonShape(Bitmap bitmap) {
		int targetWidth = bitmap.getWidth();
		int targetHeight = bitmap.getHeight();
		Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);

		float radius = targetHeight / 2;
		float triangleHeight = (float) (Math.sqrt(3) * radius / 2);
		float centerX = targetWidth / 2;
		float centerY = targetHeight / 2;
		
		Canvas canvas = new Canvas(targetBitmap);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG|Paint.ANTI_ALIAS_FLAG));
		Path path = new Path();
		path.moveTo(centerX, centerY + radius);
		path.lineTo(centerX - triangleHeight, centerY + radius / 2);
		path.lineTo(centerX - triangleHeight, centerY - radius / 2);
		path.lineTo(centerX, centerY - radius);
		path.lineTo(centerX + triangleHeight, centerY - radius / 2);
		path.lineTo(centerX + triangleHeight, centerY + radius / 2);
		path.moveTo(centerX, centerY + radius);
		canvas.clipPath(path);
		canvas.drawBitmap(bitmap, new Rect(0, 0, targetWidth, targetHeight), new Rect(0, 0, targetWidth, targetHeight), null);
		return targetBitmap;
	}

	/**
	 * 把本地的drawable转换成六边形图片
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}
	
	/**
	 * 根据颜色值判断颜色
	 * @param colorType
	 * @param val
	 * @return
	 */
	public static int colorForValue(String colorType, float val) {
		int color = 0;
		if (TextUtils.equals(colorType, "jiangshui")) {
			if (val >= 0 && val < 1) {
				return 0xff2EAD06;
			} else if (val >= 1 && val < 10) {
				return 0xff000000;
			} else if (val >= 10 && val < 25) {
				return 0xff0901EC;
			} else if (val >= 25 && val < 50) {
				return 0xffC804C8;
			} else if (val >= 50) {
				return 0xffC50724;
			}
		} else if (TextUtils.equals(colorType, "wendu")) {
			if (val < -30) {
				return 0xff201885;
			} else if (val >= -30 && val < -20) {
				return 0xff114AD9;
			} else if (val >= -20 && val < -10) {
				return 0xff4DB4F7;
			} else if (val >= -10 && val < 0) {
				return 0xffD1F8F3;
			} else if (val >= 0 && val < 10) {
				return 0xffF9F2BB;
			} else if (val >= 10 && val < 20) {
				return 0xffF9DE45;
			} else if (val >= 20 && val < 30) {
				return 0xffFFA800;
			} else if (val >= 30 && val < 40) {
				return 0xffFF6D00;
			} else if (val >= 40 && val < 50) {
				return 0xffE60000;
			} else if (val >= 50) {
				return 0xff9E0001;
			}
		} else if (TextUtils.equals(colorType, "bianwen")) {
			if (val > 0) {
				return 0xffFF0000;
			} else if (val == 0) {
				return 0xff000000;
			} else {
				return 0xff0000FF;
			}
		} else if (TextUtils.equals(colorType, "radar")) {
			return 0xffff00ff;
		} else if (TextUtils.equals(colorType, "shidu")) {
			if (val >= 0 && val < 10) {
				return 0xffFF6000;
			} else if (val >= 10 && val < 30) {
				return 0xffFEA51A;
			} else if (val >= 30 && val < 50) {
				return 0xffFFFC9F;
			} else if (val >= 50) {
				return 0xffD6E6DA;
			}
		}

		return color;

	}
	
	/**
	 * 根据风速获取风向标
	 * @param context
	 * @param speed
	 * @return
	 */
	public static Bitmap getWindMarker(Context context, float speed) {
		Bitmap bitmap = null;
		if(speed >= 1 && speed <=2){
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind12);
	    }else if(speed >= 3 && speed <=4){
	    	bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind34);
	    }else if(speed >= 5 && speed <=6){
	    	bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind56);
	    }else if(speed >= 7 && speed <=8){
	    	bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind78);
	    }else if(speed >8){
	    	bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind8s);
	    }
		return bitmap;
	}
	
	/**
	 * 读取assets下文件
	 * @param fileName
	 * @return
	 */
	public static String getFromAssets(Context context, String fileName) {
		String Result = "";
		try {
			InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			while ((line = bufReader.readLine()) != null)
				Result += line;
			return Result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Result;
	}
	
	/**
	 * 回执区域
	 * @param context
	 * @param aMap
	 */
	public static void drawDistrict(Context context, AMap aMap) {
		if (aMap == null) {
			return;
		}
		String result = CommonUtil.getFromAssets(context, "heilongjiang.json");
		if (!TextUtils.isEmpty(result)) {
			try {
				JSONObject obj = new JSONObject(result);
				JSONArray array = obj.getJSONArray("features");
				int transparency = 255;
				for (int i = 0; i < array.length(); i++) {
					JSONObject itemObj = array.getJSONObject(i);
					
					JSONObject properties = itemObj.getJSONObject("properties");
					String name = properties.getString("name");
					JSONArray cp = properties.getJSONArray("cp");
					for (int m = 0; m < cp.length(); m++) {
						double lat = cp.getDouble(1);
						double lng = cp.getDouble(0);
						
						LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						View view = inflater.inflate(R.layout.rainfall_fact_marker_view2, null);
						TextView tvName = (TextView) view.findViewById(R.id.tvName);
						if (!TextUtils.isEmpty(name)) {
							tvName.setText(name);
						}
						MarkerOptions options = new MarkerOptions();
						options.anchor(0.5f, 0.5f);
						options.position(new LatLng(lat, lng));
						options.icon(BitmapDescriptorFactory.fromView(view));
						aMap.addMarker(options);
					}
					
					JSONObject geometry = itemObj.getJSONObject("geometry");
					JSONArray coordinates = geometry.getJSONArray("coordinates");
					JSONArray array2 = coordinates.getJSONArray(0);
					PolygonOptions polylineOption = new PolygonOptions();
					transparency = 255-(i+1)*15;
					polylineOption.fillColor(Color.argb(transparency, 0, 97, 194));
					polylineOption.strokeColor(Color.TRANSPARENT);
					for (int j = 0; j < array2.length(); j++) {
						JSONArray itemArray = array2.getJSONArray(j);
						double lng = itemArray.getDouble(0);
						double lat = itemArray.getDouble(1);
						polylineOption.add(new LatLng(lat, lng));
					}
					aMap.addPolygon(polylineOption);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 绘制黑龙江
	 */
	public static void drawHLJJson(Context context, AMap aMap) {
		if (aMap == null) {
			return;
		}
		String result = CommonUtil.getFromAssets(context, "heilongjiang.json");
		if (!TextUtils.isEmpty(result)) {
			try {
				JSONObject obj = new JSONObject(result);
				JSONArray array = obj.getJSONArray("features");
				for (int i = 0; i < array.length(); i++) {
					JSONObject itemObj = array.getJSONObject(i);

					JSONObject properties = itemObj.getJSONObject("properties");
					String name = properties.getString("name");
					JSONObject geometry = itemObj.getJSONObject("geometry");
					JSONArray coordinates = geometry.getJSONArray("coordinates");
					JSONArray array2 = coordinates.getJSONArray(0);
					PolylineOptions polylineOption = new PolylineOptions();
					if (name.contains("加格达奇")) {
						polylineOption.setDottedLine(true);
					}
					polylineOption.width(6).color(0xff406bbf);
					for (int j = 0; j < array2.length(); j++) {
						JSONArray itemArray = array2.getJSONArray(j);
						double lng = itemArray.getDouble(0);
						double lat = itemArray.getDouble(1);
						polylineOption.add(new LatLng(lat, lng));
					}
					aMap.addPolyline(polylineOption);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

}
