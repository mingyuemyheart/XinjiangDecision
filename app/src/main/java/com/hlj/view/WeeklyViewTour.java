package com.hlj.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

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
 * 一周预报曲线图-旅游界面
 */
public class WeeklyViewTour extends View{

	private Context mContext;
	private List<WeatherDto> tempList = new ArrayList<>();
	private int maxTemp = 0;//最高温度
	private int minTemp = 0;//最低温度
	private Paint lineP = null;//画线画笔
	private Paint textP = null;//写字画笔
	private Paint roundP = null;//aqi背景颜色画笔
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd", Locale.CHINA);
	private int totalDivider = 0;
	private int itemDivider = 1;
	private long foreDate = 0, currentDate = 0;

	public WeeklyViewTour(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public WeeklyViewTour(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public WeeklyViewTour(Context context, AttributeSet attrs, int defStyleAttr) {
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

		roundP = new Paint();
		roundP.setStyle(Paint.Style.FILL);
		roundP.setStrokeCap(Paint.Cap.ROUND);
		roundP.setAntiAlias(true);
	}

	/**
	 * 对polyView进行赋值
	 */
	public void setData(List<WeatherDto> dataList, long foreDate, long currentDate) {
		this.foreDate = foreDate;
		this.currentDate = currentDate;
		if (!dataList.isEmpty()) {
			tempList.clear();
			tempList.addAll(dataList);

			maxTemp = tempList.get(0).highTemp;
			minTemp = tempList.get(0).lowTemp;
			for (int i = 0; i < tempList.size(); i++) {
				if (maxTemp <= tempList.get(i).highTemp) {
					maxTemp = tempList.get(i).highTemp;
				}
				if (minTemp >= tempList.get(i).lowTemp) {
					minTemp = tempList.get(i).lowTemp;
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
			}else if (totalDivider <= 15) {
				itemDivider = 2;
			}else if (totalDivider <= 25) {
				itemDivider = 3;
			}else if (totalDivider <= 40) {
				itemDivider = 4;
			}else {
				itemDivider = 5;
			}
//			maxTemp = maxTemp+itemDivider*3/2;
//			minTemp = minTemp-itemDivider;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.TRANSPARENT);
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		float chartW = w-CommonUtil.dip2px(mContext, 40);
		float chartH = h-CommonUtil.dip2px(mContext, 260);
		float leftMargin = CommonUtil.dip2px(mContext, 20);
		float rightMargin = CommonUtil.dip2px(mContext, 20);
		float topMargin = CommonUtil.dip2px(mContext, 140);

		int size = tempList.size();
		//获取曲线上每个温度点的坐标
		for (int i = 0; i < size; i++) {
			WeatherDto dto = tempList.get(i);

			//获取最高温度对应的坐标点信息
			dto.highX = (chartW/(size-1))*i + leftMargin;
			float highTemp = tempList.get(i).highTemp;
			dto.highY = chartH*Math.abs(maxTemp-highTemp)/totalDivider+topMargin;
			Log.e("highTemp", highTemp+"---"+dto.highY);

			//获取最低温度的对应的坐标点信息
			dto.lowX = (chartW/(size-1))*i + leftMargin;
			float lowTemp = tempList.get(i).lowTemp;
			dto.lowY = chartH*Math.abs(maxTemp-lowTemp)/totalDivider+topMargin;
			Log.e("lowTemp", lowTemp+"---"+dto.lowY);

			tempList.set(i, dto);
		}

		for (int i = 0; i < size; i++) {
			WeatherDto dto = tempList.get(i);

			//绘制周几、日期、天气现象和天气现象图标
			textP.setColor(getResources().getColor(R.color.white));
			textP.setTextSize(getResources().getDimension(R.dimen.level_5));
			String week = mContext.getString(R.string.week)+dto.week.substring(dto.week.length()-1, dto.week.length());
			if (currentDate > foreDate) {
				if (i == 0) {
					week = "昨天";
				}else if (i == 1) {
					week = "今天";
				}else if (i == 2) {
					week = "明天";
				}else {
					week = dto.week;
				}
			}else {
				if (i == 0) {
					week = "今天";
				}else if (i == 1) {
					week = "明天";
				}else {
					week = dto.week;
				}
			}
			float weekText = textP.measureText(week);
			canvas.drawText(week, dto.highX-weekText/2, CommonUtil.dip2px(mContext, 20), textP);

//			try {
//				String date = sdf2.format(sdf1.parse(dto.date));
//				float dateText = textP.measureText(date);
//				canvas.drawText(date, dto.highX-dateText/2, CommonUtil.dip2px(mContext, 37), textP);
//			} catch (ParseException e) {
//				e.printStackTrace();
//			}
//
//			float highPheText = textP.measureText(dto.highPhe);//天气现象字符串占像素宽度
//			canvas.drawText(dto.highPhe, dto.highX-highPheText/2, CommonUtil.dip2px(mContext, 54), textP);

			Bitmap b = WeatherUtil.getBitmap(mContext, dto.highPheCode);
			Bitmap newBit = ThumbnailUtils.extractThumbnail(b, (int)(CommonUtil.dip2px(mContext, 20)), (int)(CommonUtil.dip2px(mContext, 20)));
			canvas.drawBitmap(newBit, dto.highX-newBit.getWidth()/2, CommonUtil.dip2px(mContext, 30), textP);

			Bitmap lb = WeatherUtil.getNightBitmap(mContext, dto.lowPheCode);
			Bitmap newLbit = ThumbnailUtils.extractThumbnail(lb, (int)(CommonUtil.dip2px(mContext, 20)), (int)(CommonUtil.dip2px(mContext, 20)));
			canvas.drawBitmap(newLbit, dto.lowX-newLbit.getWidth()/2, CommonUtil.dip2px(mContext, 55), textP);

			//绘制曲线上每个时间点的温度值
			textP.setColor(getResources().getColor(R.color.white));
			textP.setTextSize(CommonUtil.dip2px(mContext, 13));
			float highText = textP.measureText(dto.highTemp+"/"+dto.lowTemp+"°");//高温字符串占像素宽度
			canvas.drawText(dto.highTemp+"/"+dto.lowTemp+"°", dto.highX-highText/2, CommonUtil.dip2px(mContext, 90), textP);

//			float lowPheText = textP.measureText(dto.lowPhe);//天气现象字符串占像素宽度
//			canvas.drawText(dto.lowPhe, dto.lowX-lowPheText/2, CommonUtil.dip2px(mContext, 150), textP);

//			//绘制风力风向
//			textP.setColor(getResources().getColor(R.color.white));
//			textP.setTextSize(CommonUtil.dip2px(mContext, 12));
//			String windDir = mContext.getString(WeatherUtil.getWindDirection(Integer.valueOf(dto.windDir)));
//			float windDirWidth = textP.measureText(windDir);//低温字符串所占的像素宽度
//			float windForceWidth = textP.measureText(dto.windForceString);//低温字符串所占的像素宽度
//			canvas.drawText(windDir, dto.highX-windDirWidth/2, CommonUtil.dip2px(mContext, 165), textP);
//			canvas.drawText(dto.windForceString, dto.highX-windForceWidth/2, CommonUtil.dip2px(mContext, 180), textP);
		}

	}

}
