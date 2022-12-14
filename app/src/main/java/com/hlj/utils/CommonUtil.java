package com.hlj.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
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
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.hlj.common.CONST;
import com.hlj.common.MyApplication;
import com.hlj.dto.AgriDto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shawn.cxwl.com.hlj.R;

public class CommonUtil {

	/** 
     * ??????????????????????????? dp ????????? ????????? px(??????) 
     */  
    public static float dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return dpValue * scale;
    }  
  
    /** 
     * ??????????????????????????? px(??????) ????????? ????????? dp 
     */  
    public static float px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return pxValue / scale;
    }

	public static int widthPixels(Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return dm.widthPixels;
	}

	public static int heightPixels(Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return dm.heightPixels;
	}

	/**
	 * ???????????????
	 * @return ????????????????????????
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
	 * ??????ScrollView???ListView???????????????
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
	 * ??????ScrollView???GridView???????????????
	 */
	public static void setGridViewHeightBasedOnChildren(GridView gridView) {
		ListAdapter listAdapter = gridView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		
		Class<GridView> tempGridView = GridView.class; // ??????gridview????????????class
		int column = -1;
        try {
 
            Field field = tempGridView.getDeclaredField("mRequestedNumColumns"); // ?????????????????????
            field.setAccessible(true); // ??????????????????
            column = Integer.valueOf(field.get(gridView).toString()); // ??????????????????
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
	 * ??????????????????????????????????????????
	 * @param fa ????????????????????????
	 * @param fb ????????????????????????
	 * @return
	 */
	public static int getPheCode(int fa, int fb) {
		int pheCode = 0;
		SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
		
		try {
			long currentTime = new Date().getTime();//????????????
			long eight = sdf1.parse("08:00").getTime();//??????08:00
			long twenty = sdf1.parse("20:00").getTime();//??????20:00
			
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
	 * ???Assets???????????????
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
	 * ??????????????????
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
	 * ??????????????????
	 * @param editText ?????????
	 * @param context ?????????
	 */
	public static void hideInputSoft(EditText editText, Context context) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}
	
	/**
	 * ?????????????????????
	 * 
	 * @param bitmap
	 *            ??????Bitmap??????
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
     * ???????????????????????? 
     * @param url
     * @return
     */
	public static Bitmap getHttpBitmap(String url) {
		URL myFileURL;
		Bitmap bitmap = null;
		try {
			myFileURL = new URL(url);
			// ????????????
			HttpURLConnection conn = (HttpURLConnection) myFileURL.openConnection();
			// ?????????????????????6000?????????conn.setConnectionTiem(0);????????????????????????
			conn.setConnectTimeout(6000);
			// ???????????????????????????
			conn.setDoInput(true);
			// ???????????????
			conn.setUseCaches(false);
			// ?????????????????????????????????
			conn.connect();
			// ???????????????
			InputStream is = conn.getInputStream();
			// ??????????????????
			bitmap = BitmapFactory.decodeStream(is);
			// ???????????????
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	/**
	 * ????????????????????????
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
	 * ????????????drawable????????????????????????
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
	 * ???????????????????????????
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
	 * ???????????????????????????
	 * @param context
	 * @param windForce
	 * @return
	 */
	public static Bitmap getWindMarker(Context context, int windForce) {
		Bitmap bitmap = null;
		if(windForce <=2){
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind12);
	    }else if(windForce <=4){
	    	bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind34);
	    }else if(windForce <=6){
	    	bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind56);
	    }else if(windForce <=8){
	    	bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind78);
	    }else {
	    	bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind8s);
	    }
		return bitmap;
	}

	/**
	 * ???????????????????????????
	 * @param context
	 * @param speed
	 * @return
	 */
	public static Bitmap getWindMarker(Context context, double speed) {
		Bitmap bitmap = null;
		if (speed <= 0.2) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind12);
		}else if (speed > 0.2 && speed <= 1.5) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind12);
		}else if (speed > 1.5 && speed <= 3.3) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind12);
		}else if (speed > 3.3 && speed <= 5.4) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind34);
		}else if (speed > 5.4 && speed <= 7.9) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind34);
		}else if (speed > 7.9 && speed <= 10.7) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind56);
		}else if (speed > 10.7 && speed <= 13.8) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind56);
		}else if (speed > 13.8 && speed <= 17.1) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind78);
		}else if (speed > 17.1 && speed <= 20.7) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind78);
		}else if (speed > 20.7 && speed <= 24.4) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind8s);
		}else if (speed > 24.4 && speed <= 28.4) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind8s);
		}else if (speed > 28.4 && speed <= 32.6) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind8s);
		}else if (speed > 32.6 && speed < 99999.0) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iv_wind8s);
		}
		return bitmap;
	}
	
	/**
	 * ??????assets?????????
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
	 * ???????????????
	 */
	public static void drawHLJJson(final Context context, final AMap aMap) {
		if (aMap == null) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				String result = CommonUtil.getFromAssets(context, "xinjiang_geo.json");
				if (!TextUtils.isEmpty(result)) {
					try {
						LatLngBounds.Builder builder = LatLngBounds.builder();
						JSONObject obj = new JSONObject(result);
						if (!obj.isNull("polyline")) {
							String polyline = obj.getString("polyline");
							String[] item = polyline.split(";");
							PolygonOptions polygonOption = new PolygonOptions();
							polygonOption.fillColor(0x40889BE8).strokeColor(context.getResources().getColor(R.color.colorPrimary)).strokeWidth(5f);
							for (int i = 0; i < item.length; i++) {
								String[] latLng = item[i].split(",");
								double lng = Double.parseDouble(latLng[0]);
								double lat = Double.parseDouble(latLng[1]);
								polygonOption.add(new LatLng(lat, lng));
								builder.include(new LatLng(lat, lng));
							}
							aMap.addPolygon(polygonOption);
							if (item.length > 0) {
								aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * ???????????????
	 */
	public static void drawHLJJsonLine(final Context context, final AMap aMap) {
		if (aMap == null) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				String result = CommonUtil.getFromAssets(context, "xinjiang_geo.json");
				if (!TextUtils.isEmpty(result)) {
					try {
						LatLngBounds.Builder builder = LatLngBounds.builder();
						JSONObject obj = new JSONObject(result);
						if (!obj.isNull("polyline")) {
							String polyline = obj.getString("polyline");
							String[] item = polyline.split(";");
							PolylineOptions polygonOption = new PolylineOptions();
							polygonOption.color(context.getResources().getColor(R.color.colorPrimary));
							polygonOption.width(5f);
							for (int i = 0; i < item.length; i++) {
								String[] latLng = item[i].split(",");
								double lng = Double.parseDouble(latLng[0]);
								double lat = Double.parseDouble(latLng[1]);
								polygonOption.add(new LatLng(lat, lng));
								builder.include(new LatLng(lat, lng));
							}
							aMap.addPolyline(polygonOption);
							if (item.length > 0) {
								aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * ??????????????????
	 */
	public static void drawRailWay(final Context context, final AMap aMap, final ArrayList<Polyline> polylines, final String lineName) {
		if (aMap == null) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				String result = CommonUtil.getFromAssets(context, "railway.json");
				if (!TextUtils.isEmpty(result)) {
					try {
						JSONObject obj = new JSONObject(result);
						JSONArray array = obj.getJSONArray("features");
						for (int i = 0; i < array.length(); i++) {
							JSONObject itemObj = array.getJSONObject(i);

							if (TextUtils.equals(lineName, "?????????")) {
								JSONObject geometry = itemObj.getJSONObject("geometry");
								JSONArray coordinates = geometry.getJSONArray("coordinates");
								if (coordinates.length() > 20) {
									PolylineOptions polylineOption = new PolylineOptions();
//									polylineOption.width(10).color(context.getResources().getColor(R.color.orange));
									for (int m = 0; m < coordinates.length(); m++) {
										JSONArray itemArray = coordinates.getJSONArray(m);
										double lng = itemArray.getDouble(0);
										double lat = itemArray.getDouble(1);
										polylineOption.add(new LatLng(lat, lng));
									}
									Polyline polyline = aMap.addPolyline(polylineOption);
									polyline.setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.bg_railway_line));
									polyline.setWidth(20);
									polyline.setZIndex(1000);
									polylines.add(polyline);
								}
							} else if (TextUtils.equals(lineName, "??????????????????")) {
								JSONObject properties = itemObj.getJSONObject("properties");
								if (!properties.isNull("NAME")) {
									String name = properties.getString("NAME");
									if (TextUtils.equals(lineName, name)) {
										JSONObject geometry = itemObj.getJSONObject("geometry");
										JSONArray coordinates = geometry.getJSONArray("coordinates");
										if (coordinates.length() > 20) {
											PolylineOptions polylineOption = new PolylineOptions();
//											polylineOption.width(10).color(context.getResources().getColor(R.color.orange));
											for (int m = 0; m < coordinates.length(); m++) {
												JSONArray itemArray = coordinates.getJSONArray(m);
												double lng = itemArray.getDouble(0);
												double lat = itemArray.getDouble(1);
												polylineOption.add(new LatLng(lat, lng));
											}
											Polyline polyline = aMap.addPolyline(polylineOption);
											polyline.setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.bg_railway_line));
											polyline.setWidth(20);
											polyline.setZIndex(1000);
											polylines.add(polyline);
										}
									}
								}
							} else {
								JSONObject properties = itemObj.getJSONObject("properties");
								if (!properties.isNull("NAME")) {
									String name = properties.getString("NAME");
									if (!TextUtils.isEmpty(name) && lineName.contains(name)) {
										JSONObject geometry = itemObj.getJSONObject("geometry");
										JSONArray coordinates = geometry.getJSONArray("coordinates");
										if (coordinates.length() > 20) {
											PolylineOptions polylineOption = new PolylineOptions();
//											polylineOption.width(10).color(context.getResources().getColor(R.color.orange));
											for (int m = 0; m < coordinates.length(); m++) {
												JSONArray itemArray = coordinates.getJSONArray(m);
												double lng = itemArray.getDouble(0);
												double lat = itemArray.getDouble(1);
												polylineOption.add(new LatLng(lat, lng));
											}
											Polyline polyline = aMap.addPolyline(polylineOption);
											polyline.setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.bg_railway_line));
											polyline.setWidth(20);
											polyline.setZIndex(1000);
											polylines.add(polyline);
										}
									}
								}
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * ??????????????????
	 */
	public static void drawRoadLine(final Context context, final AMap aMap, final ArrayList<Polyline> polylines, final String lineName) {
		if (aMap == null) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				String result = CommonUtil.getFromAssets(context, "road.json");
				if (!TextUtils.isEmpty(result)) {
					try {
						JSONObject obj = new JSONObject(result);
						JSONArray array = obj.getJSONArray("features");
						for (int i = 0; i < array.length(); i++) {
							JSONObject itemObj = array.getJSONObject(i);

							if (TextUtils.equals(lineName, "?????????")) {
								JSONObject geometry = itemObj.getJSONObject("geometry");
								JSONObject properties = itemObj.getJSONObject("properties");
								String xzdj = "";
								if (!properties.isNull("xzdj")) {
									xzdj = properties.getString("xzdj");
								}
								JSONArray coordinates = geometry.getJSONArray("coordinates");
								if (coordinates.length() > 20) {
									PolylineOptions polylineOption = new PolylineOptions();
									if (TextUtils.equals(xzdj, "??????")) {
										polylineOption.width(10).color(0xffFF9C2B);
									} else {
										polylineOption.width(10).color(0xff3E4269);
									}
									for (int m = 0; m < coordinates.length(); m++) {
										JSONArray itemArray = coordinates.getJSONArray(m);
										double lng = itemArray.getDouble(0);
										double lat = itemArray.getDouble(1);
										polylineOption.add(new LatLng(lat, lng));
									}
									Polyline polyline = aMap.addPolyline(polylineOption);
									polyline.setZIndex(1000);
									polylines.add(polyline);
								}
							} else if (TextUtils.equals(lineName, "??????????????????")) {
								JSONObject properties = itemObj.getJSONObject("properties");
								String xzdj = "";
								if (!properties.isNull("xzdj")) {
									xzdj = properties.getString("xzdj");
								}
								if (!properties.isNull("NAME")) {
									String name = properties.getString("NAME");
									if (TextUtils.equals(lineName, name)) {
										JSONObject geometry = itemObj.getJSONObject("geometry");
										JSONArray coordinates = geometry.getJSONArray("coordinates");
										if (coordinates.length() > 20) {
											PolylineOptions polylineOption = new PolylineOptions();
											if (TextUtils.equals(xzdj, "??????")) {
												polylineOption.width(10).color(0xffFF9C2B);
											} else {
												polylineOption.width(10).color(0xff3E4269);
											}
											for (int m = 0; m < coordinates.length(); m++) {
												JSONArray itemArray = coordinates.getJSONArray(m);
												double lng = itemArray.getDouble(0);
												double lat = itemArray.getDouble(1);
												polylineOption.add(new LatLng(lat, lng));
											}
											Polyline polyline = aMap.addPolyline(polylineOption);
											polyline.setZIndex(1000);
											polylines.add(polyline);
										}
									}
								}
							} else {
								JSONObject properties = itemObj.getJSONObject("properties");
								String xzdj = "";
								if (!properties.isNull("xzdj")) {
									xzdj = properties.getString("xzdj");
								}
								if (!properties.isNull("NAME")) {
									String name = properties.getString("NAME");
									if (TextUtils.equals(lineName, name)) {
										JSONObject geometry = itemObj.getJSONObject("geometry");
										JSONArray coordinates = geometry.getJSONArray("coordinates");
										if (coordinates.length() > 20) {
											PolylineOptions polylineOption = new PolylineOptions();
											if (TextUtils.equals(xzdj, "??????")) {
												polylineOption.width(10).color(0xffFF9C2B);
											} else {
												polylineOption.width(10).color(0xff3E4269);
											}
											for (int m = 0; m < coordinates.length(); m++) {
												JSONArray itemArray = coordinates.getJSONArray(m);
												double lng = itemArray.getDouble(0);
												double lat = itemArray.getDouble(1);
												polylineOption.add(new LatLng(lat, lng));
											}
											Polyline polyline = aMap.addPolyline(polylineOption);
											polyline.setZIndex(1000);
											polylines.add(polyline);
										}
									}
								}
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * ??????????????????
	 */
	public static void drawPowerLine1100(final Context context, final AMap aMap, final ArrayList<Polyline> polylines, final String lineName) {
		drawPowerLine(context, aMap, polylines, lineName, "power1100.json");
	}

	/**
	 * ??????????????????
	 */
	public static void drawPowerLine750(final Context context, final AMap aMap, final ArrayList<Polyline> polylines, final String lineName) {
		drawPowerLine(context, aMap, polylines, lineName, "power750.json");
	}

	/**
	 * ??????????????????
	 */
	public static void drawPowerLine500(final Context context, final AMap aMap, final ArrayList<Polyline> polylines, final String lineName) {
		drawPowerLine(context, aMap, polylines, lineName, "power500.json");
	}

	/**
	 * ??????????????????
	 */
	public static void drawPowerLine220(final Context context, final AMap aMap, final ArrayList<Polyline> polylines, final String lineName) {
		drawPowerLine(context, aMap, polylines, lineName, "power220.json");
	}

	/**
	 * ??????????????????
	 */
	public static void drawPowerLine(final Context context, final AMap aMap, final ArrayList<Polyline> polylines, final String lineName, final String jsonName) {
		if (aMap == null) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				String result = CommonUtil.getFromAssets(context, jsonName);
				if (!TextUtils.isEmpty(result)) {
					try {
						JSONObject obj = new JSONObject(result);
						JSONArray array = obj.getJSONArray("features");
						for (int i = 0; i < array.length(); i++) {
							JSONObject itemObj = array.getJSONObject(i);

							if (TextUtils.equals(lineName, "?????????")) {
								JSONObject geometry = itemObj.getJSONObject("geometry");
								JSONArray coordinates = geometry.getJSONArray("coordinates");
								if (coordinates.length() > 20) {
									PolylineOptions polylineOption = new PolylineOptions();
									polylineOption.width(10).color(0xff3E4269);
									for (int m = 0; m < coordinates.length(); m++) {
										JSONArray itemArray = coordinates.getJSONArray(m);
										double lng = itemArray.getDouble(0);
										double lat = itemArray.getDouble(1);
										polylineOption.add(new LatLng(lat, lng));
									}
									Polyline polyline = aMap.addPolyline(polylineOption);
									polyline.setZIndex(1000);
									polylines.add(polyline);
								}
							} else {
								JSONObject properties = itemObj.getJSONObject("properties");
								if (!properties.isNull("name")) {
									String name = properties.getString("name");
									if (TextUtils.equals(lineName, name)) {
										JSONObject geometry = itemObj.getJSONObject("geometry");
										JSONArray coordinates = geometry.getJSONArray("coordinates");
										if (coordinates.length() > 20) {
											PolylineOptions polylineOption = new PolylineOptions();
											polylineOption.width(10).color(0xff3E4269);
											for (int m = 0; m < coordinates.length(); m++) {
												JSONArray itemArray = coordinates.getJSONArray(m);
												double lng = itemArray.getDouble(0);
												double lat = itemArray.getDouble(1);
												polylineOption.add(new LatLng(lat, lng));
											}
											Polyline polyline = aMap.addPolyline(polylineOption);
											polyline.setZIndex(1000);
											polylines.add(polyline);
										}
									}
								}
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * ??????????????????????????????
	 * @param i (+1???????????????-1???????????????0????????????)
	 * @return
	 */
	public static String getDate(String time, int i) {
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
		Calendar c = Calendar.getInstance();
		try {
			Date date = sdf2.parse(time);
			c.setTime(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		c.add(Calendar.DAY_OF_MONTH, i);
		return sdf1.format(c.getTime());
	}

	/**
	 * ?????????????????????????????????
	 * @param i (+1???????????????-1???????????????0????????????)
	 * @return
	 */
	public static String getWeek(String time, int i) {
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
		Calendar c = Calendar.getInstance();
		try {
			Date date = sdf2.parse(time);
			c.setTime(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		c.add(Calendar.DAY_OF_WEEK, i);

		String week = "";
		switch (c.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SUNDAY:
				week = "??????";
				break;
			case Calendar.MONDAY:
				week = "??????";
				break;
			case Calendar.TUESDAY:
				week = "??????";
				break;
			case Calendar.WEDNESDAY:
				week = "??????";
				break;
			case Calendar.THURSDAY:
				week = "??????";
				break;
			case Calendar.FRIDAY:
				week = "??????";
				break;
			case Calendar.SATURDAY:
				week = "??????";
				break;
		}
		return week;
	}

	/**
	 * ??????http://decision.tianqi.cn??????????????????
	 * @return
	 */
	public static String getRequestHeader() {
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd00");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd06");
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd12");
		SimpleDateFormat sdf4 = new SimpleDateFormat("yyyyMMdd18");
		SimpleDateFormat sdf5 = new SimpleDateFormat("yyyyMMddHH");
		long time1 = 0, time2 = 0, time3 = 0, time4 = 0;
		long currentTime = 0;
		try {
			time1 = sdf5.parse(sdf1.format(new Date())).getTime();
			time2 = sdf5.parse(sdf2.format(new Date())).getTime();
			time3 = sdf5.parse(sdf3.format(new Date())).getTime();
			time4 = sdf5.parse(sdf4.format(new Date())).getTime();
			currentTime = new Date().getTime();
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		String date = null;
		if (currentTime >= time1 && currentTime < time2) {
			date = sdf1.format(new Date());
		}else if (currentTime >= time2 && currentTime < time3) {
			date = sdf2.format(new Date());
		}else if (currentTime >= time3 && currentTime < time4) {
			date = sdf3.format(new Date());
		}else if (currentTime >= time4) {
			date = sdf4.format(new Date());
		}
		String publicKey = "http://decision.tianqi.cn/?date="+date;//??????
		String privateKye = "url_private_key_789";//??????
		String result = "";
		try{
			byte[] rawHmac = null;
			byte[] keyBytes = privateKye.getBytes("UTF-8");
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);
			rawHmac = mac.doFinal(publicKey.getBytes("UTF-8"));
			result = Base64.encodeToString(rawHmac, Base64.DEFAULT);
//			result = URLEncoder.encode(result, "UTF-8");
			result = "http://decision.tianqi.cn/"+result;
		}catch(Exception e){
			Log.e("SceneException", e.getMessage(), e);
		}
		return result;
	}

	/**
	 * ??????GPS???????????????GPS??????AGPS?????????????????????????????????
	 * @param context
	 * @return true ????????????
	 */
	public static final boolean isLocationOpen(final Context context) {
		LocationManager locationManager  = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		// ??????GPS??????????????????????????????????????????????????????24????????????????????????????????????????????????????????????????????????
		boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		// ??????WLAN???????????????(3G/2G)???????????????????????????AGPS?????????GPS??????????????????????????????????????????????????????????????????????????????????????????????????????
		boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (gps || network) {
			return true;
		}
		return false;
	}

	/**
	 * ???????????????
	 *
	 * @param datetime
	 * @return
	 */
	public static String dateToWeek(String datetime) {
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
		String[] weekDays = { "?????????", "?????????", "?????????", "?????????", "?????????", "?????????", "?????????" };
		Calendar cal = Calendar.getInstance(); // ??????????????????
		try {
			Date date = f.parse(datetime);
			cal.setTime(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1; // ?????????????????????????????????
		if (w < 0)
			w = 0;
		return weekDays[w];
	}

	/**
	 * ??????listview??????
	 * @param listView
	 */
	public static int getListViewHeightBasedOnChildren(ListView listView) {
		int height = 0;
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return height;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}
		height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		return height;
	}

	/**
	 * ?????????wps-office
	 * @param filePath
	 */
	public static void intentWPSOffice(Context context, String filePath) {
		if (context == null || TextUtils.isEmpty(filePath)) {
			return;
		}
		Uri uri;
		File file = new File(filePath);
		if (file.exists()) {
			final String authority = context.getPackageName()+".fileprovider";
			boolean build = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
			uri = build ? FileProvider.getUriForFile(context, authority, file) : Uri.fromFile(file);
		}else {
			uri = Uri.parse(filePath);
		}
		Intent intent = context.getPackageManager().getLaunchIntentForPackage("cn.wps.moffice_eng");//WPS??????????????????
		if (intent == null) {
			context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=cn.wps.moffice_eng")));
			Toast.makeText(context, "?????????WPS Office???????????????", Toast.LENGTH_SHORT).show();
			return;
		}
		Bundle bundle = new Bundle();
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//???????????????????????????????????????????????????Uri??????????????????
		intent.setData(uri);//???????????????????????????????????????????????????????????????????????????????????????????????????????????????
		intent.putExtras(bundle);
		context.startActivity(intent);
	}

	/**
	 * ???????????????????????????
	 * @param context
	 * @param speed
	 * @return
	 */
	public static Bitmap getStrongWindMarker(Context context, double speed) {
		Bitmap bitmap = null;
		if (speed > 17.1 && speed <= 20.7) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.fzj_wind_78);
		}else if (speed > 20.7 && speed <= 24.4) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.fzj_wind_89);
		}else if (speed > 24.4 && speed <= 28.4) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.fzj_wind_910);
		}else if (speed > 28.4 && speed <= 32.6) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.fzj_wind_1011);
		}else if (speed > 32.6 && speed < 99999.0) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.fzj_wind_1112);
		}
		return bitmap;
	}

	/**
	 * ??????????????????
	 */
	public static void submitClickCount(final String columnId, final String name) {
		if (TextUtils.isEmpty(columnId) || TextUtils.isEmpty(name)) {
			return;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH", Locale.CHINA);
		String addtime = sdf.format(new Date());
		final String clickUrl = String.format("http://xinjiangdecision.tianqi.cn:81/home/work/clickCount?addtime=%s&appid=%s&eventid=menuClick_%s&eventname=%s&userid=%s&username=%s",
				addtime, CONST.APPID, columnId, name, MyApplication.UID, MyApplication.USERNAME);
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(clickUrl).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
					}
				});
			}
		}).start();
	}

	/**
	 * ?????????????????????
	 * @param graymap
	 * @return
	 */
	public static Bitmap gray2Binary(Bitmap graymap, int color) {
		//??????????????????????????????
		int width = graymap.getWidth();
		int height = graymap.getHeight();
		//?????????????????????
		Bitmap binarymap = null;
		binarymap = graymap.copy(Config.ARGB_8888, true);
		//?????????????????????????????????????????????
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				//????????????????????????
				int col = binarymap.getPixel(i, j);
				//??????alpha????????????
				int alpha = col & 0xFF000000;
				//?????????????????????RGB??????
				int red = (col & 0x00FF0000) >> 16;
				int green = (col & 0x0000FF00) >> 8;
				int blue = (col & 0x000000FF);
				// ?????????X = 0.3??R+0.59??G+0.11??B?????????X???????????????RGB
//				int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
				int gray = color;
				//??????????????????????????????
				if (gray <= 95) {
					gray = 0;
				} else {
					gray = 255;
				}
				// ??????ARGB
				int newColor = alpha | (gray << 16) | (gray << 8) | gray;
				//?????????????????????????????????
				binarymap.setPixel(i, j, newColor);
			}
		}
		return binarymap;
	}

	public static Bitmap grayScaleImage(Bitmap bm) {
//		int width = bitmap.getWidth();
//		int height = bitmap.getHeight();
//		Bitmap grayImg = null;
//		try {
//			float a = 1;
//			float b = 1;
//			float c = 2;
//			float lum = 0;
//			ColorMatrix colorMatrix = new ColorMatrix();
//			colorMatrix.setSaturation(0);
////			colorMatrix.set(new float[]
////							{a, b, c, 0, lum,
////							a, b, c, 0, lum,
////							a, b, c, 0, lum,
////							0, 0, 0, 1, 0});
//
//			grayImg = Bitmap.createBitmap(width, height, Config.ARGB_8888);
//			Canvas canvas = new Canvas(grayImg);
//			Paint paint = new Paint();
//			ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
//			paint.setColorFilter(colorMatrixFilter);
//			canvas.drawBitmap(bitmap, 0, 0, paint);
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
//		return grayImg;


		Bitmap bitmap = null;
		// ????????????????????????
		int width = bm.getWidth();
		int height = bm.getHeight();
		// ?????????????????????
		bitmap = bm.copy(Config.ARGB_8888, true);
		// ????????????????????????,????????????????????????
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				// ????????????????????????
				int pixel = bitmap.getPixel(i, j);
				// ??????Alpha????????????
				int alpha = pixel & 0xFF000000;
				// ??????Red??????
				int red = (pixel & 0x00FF0000) >> 16;
				// ??????Green??????
				int green = (pixel & 0x0000FF00) >> 8;
				// ??????Blue??????
				int blue = pixel & 0x000000FF;
				// ????????????????????????,????????????????????????
				int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
				// ????????????????????????
				if (gray <= 95) {
					gray = 0;
				} else {
					gray = 255;
				}
				// ?????????????????????
				int newPiexl = alpha | (gray << 16) | (gray << 8) | gray;
				// ????????????????????????
				bitmap.setPixel(i, j, newPiexl);
			}
		}
		return bitmap;
	}

	/**
	 * ???????????????????????????????????????
	 * ????????????0???WIFI??????1???3G??????2???2G??????3
	 * @param context
	 * @return
	 */
	public static int getConnectedType(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
				return mNetworkInfo.getType();
			}
		}
		return -1;
	}

	/**
	 * ??????aqi?????????aqi???????????????????????????
	 * @param value
	 * @return
	 */
	public static String getAqiDes(Context context, int value) {
		String aqi;
		if (value <= 50) {
			aqi = context.getString(R.string.aqi_level1);
		}else if (value <= 100) {
			aqi = context.getString(R.string.aqi_level2);
		}else if (value <= 150) {
			aqi = context.getString(R.string.aqi_level3);
		}else if (value <= 200) {
			aqi = context.getString(R.string.aqi_level4);
		}else if (value <= 300) {
			aqi = context.getString(R.string.aqi_level5);
		}else {
			aqi = context.getString(R.string.aqi_level6);
		}
		return aqi;
	}

	/**
	 * ??????aqi????????????????????????????????????
	 * @param value
	 * @return
	 */
	public static int getCornerBackground(int value) {
		int drawable;
		if (value <= 50) {
			drawable = R.drawable.corner_aqi_one;
		}else if (value <= 100) {
			drawable = R.drawable.corner_aqi_two;
		}else if (value <= 150) {
			drawable = R.drawable.corner_aqi_three;
		}else if (value <= 200) {
			drawable = R.drawable.corner_aqi_four;
		}else if (value <= 300) {
			drawable = R.drawable.corner_aqi_five;
		}else {
			drawable = R.drawable.corner_aqi_six;
		}
		return drawable;
	}

	/**
	 * ??????????????????
	 * @return
	 */
	public static String randomColor() {
		Random rnd = new Random();
		String r = Integer.toHexString(rnd.nextInt(256));
		if (r.length() <= 1) {
			r = "0"+r;
		}
		String g = Integer.toHexString(rnd.nextInt(256));
		if (g.length() <= 1) {
			g = "0"+g;
		}
		String b = Integer.toHexString(rnd.nextInt(256));
		if (b.length() <= 1) {
			b = "0"+b;
		}
		Log.e("colorcolor", "#"+r+g+b);
		return "#"+r+g+b;
	}

	public static void topToBottom(View view) {
		startAnimation(false, view);
		view.setVisibility(View.GONE);
	}

	public static void bottomToTop(View view) {
		startAnimation(true, view);
		view.setVisibility(View.VISIBLE);
	}

	/**
	 * @param flag false?????????map???true?????????list
	 */
	private static void startAnimation(boolean flag, final View view) {
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation animation;
		if (flag) {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,1.0f,
					Animation.RELATIVE_TO_SELF,0f);
		}else {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,1.0f);
		}
		animation.setDuration(400);
		animationSet.addAnimation(animation);
		animationSet.setFillAfter(true);
		view.startAnimation(animationSet);
		animationSet.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				view.clearAnimation();
			}
		});
	}

	/**
	 * ????????????????????????????????????
	 * @param context
	 * @param warningType
	 * @return
	 */
	public static String getWarningNameByType(Context context, String warningType) {
		String warningName = "";
		String[] array1 = context.getResources().getStringArray(R.array.warningTypes);
		for (int i = 0; i < array1.length; i++) {
			String[] value = array1[i].split(",");
			if (TextUtils.equals(warningType, value[0])) {
				warningName = value[1];
				break;
			}
		}
		return warningName;
	}

	/**
	 * ????????????????????????????????????
	 * @return
	 */
	public static List<AgriDto> getAllLocalImages(Context context) {
		List<AgriDto> list = new ArrayList<>();
		if (context != null) {
			Cursor cursor = context.getContentResolver().query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null,
					null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
					String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
					String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
					String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
					String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
					long fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));

					AgriDto dto = new AgriDto();
					dto.imageName = title;
					dto.imgUrl = path;
					dto.fileSize = fileSize;
					list.add(0, dto);
				}
				cursor.close();
			}
		}

		return list;
	}

	/**
	 * ?????????????????????
	 * @param size
	 * @return
	 */
	public static String getFormatSize(long size) {
		long kiloByte = size / 1024;
		if (kiloByte < 1) {
			return "0KB";
		}

		long megaByte = kiloByte / 1024;
		if (megaByte < 1) {
			BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
			return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
		}

		long gigaByte = megaByte / 1024;
		if (gigaByte < 1) {
			BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
			return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
		}

		long teraBytes = gigaByte / 1024;
		if (teraBytes < 1) {
			BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
			return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
		}
		BigDecimal result4 = new BigDecimal(teraBytes);
		return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()+ "TB";
	}

	/**
	 * ????????????????????????
	 * @param context
	 * @param file
	 */
	public static void notifyAlbum(Context context, File file) {
		try {
			MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
	}

}
