package com.hlj.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.hlj.common.MyApplication;
import com.hlj.dto.WeatherDto;
import com.hlj.utils.CommonUtil;
import com.hlj.utils.WeatherUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import shawn.cxwl.com.hlj.R;

/**
 * 逐小时预报
 */
public class CubicView extends View{

	private Context mContext;
	private SimpleDateFormat sdf0 = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
	private SimpleDateFormat sdf1 = new SimpleDateFormat("HH:00", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH", Locale.CHINA);
	private List<WeatherDto> tempList = new ArrayList<>();
	private int maxTemp = 0;//最高温度
	private int minTemp = 0;//最低温度
	private Paint lineP = null;//画线画笔
	private Paint textP = null;//写字画笔
	private int totalDivider = 0;
	private int itemDivider = 1;
	private Bitmap wBitmap;

	public CubicView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public CubicView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public CubicView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		init();
	}

	private void init() {
		lineP = new Paint();
		lineP.setStyle(Paint.Style.STROKE);
		lineP.setStrokeCap(Paint.Cap.ROUND);
		lineP.setAntiAlias(true);

		textP = new Paint();
		textP.setAntiAlias(true);

		wBitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.icon_wind),
				(int)(CommonUtil.dip2px(mContext, 10)), (int)(CommonUtil.dip2px(mContext, 10)));
	}

	/**
	 * 对cubicView进行赋值
	 */
	public void setData(List<WeatherDto> dataList) {
		if (!dataList.isEmpty()) {
			tempList.clear();
			tempList.addAll(dataList);

			maxTemp = tempList.get(0).hourlyTemp;
			minTemp = tempList.get(0).hourlyTemp;
			for (int i = 0; i < tempList.size(); i++) {
				if (maxTemp <= tempList.get(i).hourlyTemp) {
					maxTemp = tempList.get(i).hourlyTemp;
				}
				if (minTemp >= tempList.get(i).hourlyTemp) {
					minTemp = tempList.get(i).hourlyTemp;
				}
			}

			if (maxTemp > 0 && minTemp > 0) {
				totalDivider = maxTemp-minTemp;
			}else if (maxTemp >= 0 && minTemp <= 0) {
				totalDivider = maxTemp-minTemp;
			}else if (maxTemp < 0 && minTemp < 0) {
				totalDivider = maxTemp-minTemp;
			}
			if (totalDivider <= 5) {
				itemDivider = 1;
			}else if (totalDivider <= 10) {
				itemDivider = 3;
			}else if (totalDivider <= 15) {
				itemDivider = 5;
			}else if (totalDivider <= 20) {
				itemDivider = 10;
			}else {
				itemDivider = 15;
			}
			maxTemp = maxTemp+itemDivider*3/2;
			minTemp = minTemp-itemDivider*3/2;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (tempList.isEmpty()) {
			return;
		}

		float w = canvas.getWidth();
		float h = canvas.getHeight();
		canvas.drawColor(Color.TRANSPARENT);
		float chartW = w- CommonUtil.dip2px(mContext, 60);
		float chartH = h-CommonUtil.dip2px(mContext, 150);
		float leftMargin = CommonUtil.dip2px(mContext, 40);
		float rightMargin = CommonUtil.dip2px(mContext, 20);

		int size = tempList.size();
		//获取曲线上每个温度点的坐标
		for (int i = 0; i < size; i++) {
			WeatherDto dto = tempList.get(i);
			dto.x = (chartW/(size-1))*i + leftMargin;

			float temp = dto.hourlyTemp;
			dto.y = chartH*Math.abs(maxTemp-temp)/totalDivider;
			Log.e("temp", temp+"---"+dto.y);
			tempList.set(i, dto);
		}

		//绘制刻度线
		textP.setColor(mContext.getResources().getColor(R.color.white));
		textP.setTextSize(CommonUtil.dip2px(mContext, 10));
		for (int i = minTemp; i <= maxTemp; i+=itemDivider) {
			float dividerY = chartH*Math.abs(maxTemp-i)/totalDivider;
			canvas.drawText(i+"°", CommonUtil.dip2px(mContext, 5), dividerY, textP);
		}

		for (int i = 0; i < size-1; i++) {
			float x1 = tempList.get(i).x;
			float y1 = tempList.get(i).y;
			float x2 = tempList.get(i+1).x;
			float y2 = tempList.get(i+1).y;

			float wt = (x1 + x2) / 2;

			float x3 = wt;
			float y3 = y1;
			float x4 = wt;
			float y4 = y2;

			Path pathLow = new Path();
			pathLow.moveTo(x1, y1);
			pathLow.cubicTo(x3, y3, x4, y4, x2, y2);
			lineP.setColor(getResources().getColor(R.color.cubic_color));
			lineP.setStrokeWidth(5.0f);
			canvas.drawPath(pathLow, lineP);
		}

		canvas.drawText("风力", CommonUtil.dip2px(mContext, 5), h-CommonUtil.dip2px(mContext, 23), textP);

		float halfX = (tempList.get(1).x - tempList.get(0).x)/2;
		int windForceCode = -1;
		for (int i = 0; i < tempList.size(); i++) {
			WeatherDto dto = tempList.get(i);

			if (i == 0) {
				windForceCode = dto.hourlyWindForceCode;
			}

			//绘制曲线上每个时间点marker
			lineP.setColor(getResources().getColor(R.color.cubic_color));
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 5));

			//绘制风速风向
			lineP.setColor(0x60000000);
			lineP.setStyle(Style.FILL);
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
			canvas.drawRect(dto.x-halfX, h-CommonUtil.dip2px(mContext, 35), dto.x+halfX, h-CommonUtil.dip2px(mContext, 20), lineP);

			if (windForceCode != dto.hourlyWindForceCode) {
				windForceCode = dto.hourlyWindForceCode;
				textP.setColor(getResources().getColor(R.color.white));
				textP.setTextSize(CommonUtil.dip2px(mContext, 10));

				Matrix matrix = new Matrix();
				matrix.postScale(1, 1);
				float rotation = 0;
				String dir = mContext.getString(WeatherUtil.getWindDirection(dto.hourlyWindDirCode));
				if (TextUtils.equals(dir, "北风")) {
					rotation = 0f;
				}else if (TextUtils.equals(dir, "东北风")) {
					rotation = 45f;
				}else if (TextUtils.equals(dir, "东风")) {
					rotation = 90f;
				}else if (TextUtils.equals(dir, "东南风")) {
					rotation = 135f;
				}else if (TextUtils.equals(dir, "南风")) {
					rotation = 180f;
				}else if (TextUtils.equals(dir, "西南风")) {
					rotation = 225f;
				}else if (TextUtils.equals(dir, "西风")) {
					rotation = 270f;
				}else if (TextUtils.equals(dir, "西北风")) {
					rotation = 315f;
				}else {
					rotation = 0f;
				}
				matrix.postRotate(rotation);
				Bitmap b = Bitmap.createBitmap(wBitmap, 0, 0, wBitmap.getWidth(), wBitmap.getHeight(), matrix, true);
				canvas.drawBitmap(b, dto.x-b.getWidth(), h-CommonUtil.dip2px(mContext, 33), textP);

				String windForce = WeatherUtil.getHourWindForce(dto.hourlyWindForceCode);
				float wind = textP.measureText(windForce);
				canvas.drawText(windForce, dto.x, h-CommonUtil.dip2px(mContext, 23), textP);
			}

			//绘制每个时间点上的时间值
			if (i % 2 == 0) {
				textP.setColor(getResources().getColor(R.color.white));
				textP.setTextSize(CommonUtil.dip2px(mContext, 10));
				try {
					String hourlyTime = i == 0 ? "现在" : sdf1.format(sdf0.parse(dto.hourlyTime));
					canvas.drawText(hourlyTime, dto.x-CommonUtil.dip2px(mContext, 12.5f), h-CommonUtil.dip2px(mContext, 5f), textP);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}

		//绘制时间点一行的直线
		lineP.setStyle(Style.STROKE);
		lineP.setColor(0x30ffffff);
		lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
		canvas.drawLine(0, h-CommonUtil.dip2px(mContext, 20), w, h-CommonUtil.dip2px(mContext, 20), lineP);

	}

}
