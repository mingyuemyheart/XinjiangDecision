package com.hlj.view;

/**
 * 分钟级降水图
 */
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.view.View;

import com.hlj.dto.WeatherDto;
import com.hlj.utils.CommonUtil;

import shawn.cxwl.com.hlj.R;


public class MinuteFallView extends View{

	private Context mContext = null;
	private List<WeatherDto> tempList = new ArrayList<>();
	private float maxValue = 0;
	private float minValue = 0;
	private Paint lineP = null;//画线画笔
	private Paint textP = null;//写字画笔
	private float level1 = 0.05f, level2 = 0.15f, level3 = 0.35f;//0.05-0.15是小雨，0.15-0.35是中雨, 0.35以上是大雨
	private String type = null;
	private String rain_level1 = "小雨";
	private String rain_level2 = "中雨";
	private String rain_level3 = "大雨";
	private Bitmap bitmap1, bitmap2, bitmap3;

	public MinuteFallView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public MinuteFallView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public MinuteFallView(Context context, AttributeSet attrs, int defStyleAttr) {
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

		bitmap1 = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.sl_1),
				(int)(CommonUtil.dip2px(mContext, 15)), (int)(CommonUtil.dip2px(mContext, 15)));
		bitmap2 = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.sl_2),
				(int)(CommonUtil.dip2px(mContext, 18)), (int)(CommonUtil.dip2px(mContext, 18)));
		bitmap3 = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.sl_3),
				(int)(CommonUtil.dip2px(mContext, 20)), (int)(CommonUtil.dip2px(mContext, 20)));
	}

	/**
	 * 对cubicView进行赋值
	 */
	public void setData(List<WeatherDto> dataList, String type) {
		this.type = type;
		if (type.contains("雪")) {
			rain_level1 = "小雪";
			rain_level2 = "中雪";
			rain_level3 = "大雪";
		}else {
			rain_level1 = "小雨";
			rain_level2 = "中雨";
			rain_level3 = "大雨";
		}

		if (!dataList.isEmpty()) {
			tempList.clear();
			tempList.addAll(dataList);

			maxValue = tempList.get(0).minuteFall;
			minValue = tempList.get(0).minuteFall;
			for (int i = 0; i < tempList.size(); i++) {
				if (maxValue <= tempList.get(i).minuteFall) {
					maxValue = tempList.get(i).minuteFall;
				}
				if (minValue >= tempList.get(i).minuteFall) {
					minValue = tempList.get(i).minuteFall;
				}
			}

			maxValue = 0.5f;
			minValue = 0;

		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (tempList.isEmpty()) {
			return;
		}

		canvas.drawColor(Color.TRANSPARENT);
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		float chartW = w-CommonUtil.dip2px(mContext, 20);
		float chartH = h-CommonUtil.dip2px(mContext, 30);
		float leftMargin = CommonUtil.dip2px(mContext, 10);
		float rightMargin = CommonUtil.dip2px(mContext, 10);
		float topMargin = CommonUtil.dip2px(mContext, 10);
		float bottomMargin = CommonUtil.dip2px(mContext, 20);
		float chartMaxH = chartH * maxValue / (Math.abs(maxValue)+Math.abs(minValue));//同时存在正负值时，正值高度

		int size = tempList.size();
		//获取曲线上每个温度点的坐标
		for (int i = 0; i < size; i++) {
			WeatherDto dto = tempList.get(i);
			dto.x = (chartW/(size-1))*i + leftMargin;

			float value = tempList.get(i).minuteFall;
			if (value >= 0) {
				dto.y = chartMaxH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				if (minValue >= 0) {
					dto.y = chartH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				}
			}else {
				dto.y = chartMaxH + chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				if (maxValue < 0) {
					dto.y = chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
				}
			}
			tempList.set(i, dto);
		}

		//绘制曲线
//		for (int i = 0; i < size-1; i++) {
//			float x1 = tempList.get(i).x;
//			float y1 = tempList.get(i).y;
//			float x2 = tempList.get(i+1).x;
//			float y2 = tempList.get(i+1).y;
//
//			Path linePath = new Path();
//			linePath.moveTo(x1, y1);
//			linePath.lineTo(x2, y2);
//			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 2));
//			lineP.setColor(0xff0cf6ff);
//			lineP.setStyle(Style.STROKE);
//			canvas.drawPath(linePath, lineP);
//		}

		//绘制区域
		for (int i = 0; i < size-1; i++) {
			float x1 = tempList.get(i).x;
			float y1 = tempList.get(i).y;
			float x2 = tempList.get(i+1).x;
			float y2 = tempList.get(i+1).y;

			//绘制区域
			if (i != size-1) {
				Path rectPath = new Path();
				rectPath.moveTo(x1, y1);
				rectPath.lineTo(x2, y2);
				rectPath.lineTo(x2, h-bottomMargin);
				rectPath.lineTo(x1, h-bottomMargin);
				rectPath.close();
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
				lineP.setColor(0x300cf6ff);
				lineP.setStyle(Style.FILL);
				canvas.drawPath(rectPath, lineP);
			}
		}

		//绘制小雨与中雨的分割线
		float dividerY = 0;
		float value = level2;
		if (value >= 0) {
			dividerY = chartMaxH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
			if (minValue >= 0) {
				dividerY = chartH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
			}
		}else {
			dividerY = chartMaxH + chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
			if (maxValue < 0) {
				dividerY = chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
			}
		}
		lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 0.5f));
		lineP.setColor(0x60ffffff);
		canvas.drawLine(leftMargin, dividerY, w-rightMargin, dividerY, lineP);
		textP.setColor(Color.WHITE);
		textP.setTextSize(CommonUtil.dip2px(mContext, 10));
//		canvas.drawText(String.valueOf(value)+"mm", CommonUtil.dip2px(mContext, 5), dividerY-CommonUtil.dip2px(mContext, 2.5f), textP);
		textP.setTextSize(CommonUtil.dip2px(mContext, 10));
		canvas.drawText(rain_level1, chartW-CommonUtil.dip2px(mContext, 10), dividerY+CommonUtil.dip2px(mContext, 17), textP);
		canvas.drawBitmap(bitmap1, CommonUtil.dip2px(mContext, 15), dividerY+CommonUtil.dip2px(mContext, 7), textP);

		//绘制中雨与大雨的分割线
		dividerY = 0;
		value = level3;
		if (value >= 0) {
			dividerY = chartMaxH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
			if (minValue >= 0) {
				dividerY = chartH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
			}
		}else {
			dividerY = chartMaxH + chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
			if (maxValue < 0) {
				dividerY = chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
			}
		}
		lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 0.5f));
		lineP.setColor(0x60ffffff);
		canvas.drawLine(leftMargin, dividerY, w-rightMargin, dividerY, lineP);
		textP.setColor(Color.WHITE);
		textP.setTextSize(CommonUtil.dip2px(mContext, 10));
//		canvas.drawText(String.valueOf(value)+"mm", CommonUtil.dip2px(mContext, 5), dividerY-CommonUtil.dip2px(mContext, 2.5f), textP);
		textP.setTextSize(CommonUtil.dip2px(mContext, 10));
		canvas.drawText(rain_level2, chartW-CommonUtil.dip2px(mContext, 10), dividerY+CommonUtil.dip2px(mContext, 22f), textP);
		canvas.drawBitmap(bitmap2, CommonUtil.dip2px(mContext, 13), dividerY+bitmap2.getHeight()/2, textP);
		textP.setTextSize(CommonUtil.dip2px(mContext, 10));
		canvas.drawText(rain_level3, chartW-CommonUtil.dip2px(mContext, 10), dividerY-CommonUtil.dip2px(mContext, 10f), textP);
		canvas.drawBitmap(bitmap3, CommonUtil.dip2px(mContext, 12), dividerY-CommonUtil.dip2px(mContext, 25f), textP);

		//绘制分钟刻度线
		dividerY = 0;
		value = 0;
		if (value >= 0) {
			dividerY = chartMaxH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
			if (minValue >= 0) {
				dividerY = chartH - chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
			}
		}else {
			dividerY = chartMaxH + chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
			if (maxValue < 0) {
				dividerY = chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
			}
		}
		lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
		lineP.setColor(Color.WHITE);
		canvas.drawLine(leftMargin, dividerY, w-rightMargin, dividerY, lineP);
		canvas.drawLine(leftMargin, topMargin, leftMargin, dividerY, lineP);

		for (int i = 0; i < size; i++) {
			WeatherDto dto = tempList.get(i);
			//绘制分钟刻度线上的刻度
			if (i == 0 || i == 20 || i == 40 || i == 60 || i == 80 || i == 100 || i == 119) {
				lineP.setColor(Color.WHITE);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
				canvas.drawLine(dto.x, dividerY, dto.x, dividerY+CommonUtil.dip2px(mContext, 4), lineP);
			}
			//绘制10、30、50分钟值
			if (i == 20 || i == 60 || i == 100) {
				textP.setColor(Color.WHITE);
				textP.setTextSize(CommonUtil.dip2px(mContext, 10));
				float tempWidth = textP.measureText(i+"分钟");
				canvas.drawText(i+"分钟", dto.x-tempWidth/2, dividerY+CommonUtil.dip2px(mContext, 15), textP);
			}
		}

	}

}
