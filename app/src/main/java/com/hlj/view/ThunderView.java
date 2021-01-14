package com.hlj.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.hlj.dto.StrongStreamDto;
import com.hlj.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 雷电图统计图
 */
public class ThunderView extends View {

	private Context mContext ;
	private List<StrongStreamDto> dataList = new ArrayList<>();
	private int maxValue,minValue,itemDivider;
	private Paint lineP,textP;//画线画笔
	private String type;

	public ThunderView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public ThunderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public ThunderView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		init();
	}

	private void init() {
		lineP = new Paint();
		lineP.setStyle(Style.STROKE);
		lineP.setStrokeCap(Paint.Cap.ROUND);
		lineP.setAntiAlias(true);
		
		textP = new Paint();
		textP.setAntiAlias(true);
	}
	
	/**
	 * 对cubicView进行赋值
	 * @param type hour,day,tendays,month,annual
	 */
	public void setData(List<StrongStreamDto> dataList, String type) {
		this.type = type;
		if (!dataList.isEmpty()) {
			this.dataList.clear();
			this.dataList.addAll(dataList);
			
			maxValue = dataList.get(0).thunderCount;
			minValue = dataList.get(0).thunderCount;
			for (int i = 0; i < dataList.size(); i++) {
				if (maxValue <= dataList.get(i).thunderCount) {
					maxValue = dataList.get(i).thunderCount;
				}
				if (minValue >= dataList.get(i).thunderCount) {
					minValue = dataList.get(i).thunderCount;
				}
			}
			
			if (maxValue <= 4) {//为了数据计算/4不抛异常
				maxValue = 80;
			}

			minValue = 0;
			itemDivider = maxValue/4;

		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (dataList.isEmpty()) {
			return;
		}
		
		canvas.drawColor(Color.TRANSPARENT);
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		float chartW = w- CommonUtil.dip2px(mContext, 30);
		float chartH = h-CommonUtil.dip2px(mContext, 30);
		float leftMargin = CommonUtil.dip2px(mContext, 20);
		float rightMargin = CommonUtil.dip2px(mContext, 10);
		float topMargin = CommonUtil.dip2px(mContext, 10);
		float bottomMargin = CommonUtil.dip2px(mContext, 20);

		int size = dataList.size();
		//获取曲线上每个温度点的坐标
		for (int i = 0; i < size; i++) {
			StrongStreamDto dto = dataList.get(i);
			dto.x = (chartW/(size-1))*i + leftMargin;
			
			int value = dataList.get(i).thunderCount;
			dto.y = chartH - chartH* Math.abs(value)/(Math.abs(maxValue)+ Math.abs(minValue)) + topMargin;
			dataList.set(i, dto);
		}

		//绘制横向分割线、刻度
		lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 0.5f));
		lineP.setColor(0xff6ED9D9);
		lineP.setStyle(Style.STROKE);
		textP.setColor(getResources().getColor(R.color.white));
		textP.setTextSize(CommonUtil.dip2px(mContext, 10));
		for (int i = minValue; i <= maxValue; i+=itemDivider) {
			float dividerY = chartH - chartH*(i-minValue)/(maxValue-minValue) + topMargin;
			canvas.drawLine(leftMargin, dividerY, w-rightMargin, dividerY, lineP);
			canvas.drawText(String.valueOf(i), 0, dividerY, textP);
		}
		float dividerY = chartH - chartH*(maxValue-minValue)/(maxValue-minValue) + topMargin;
		String unit = "";
		if (TextUtils.equals(type, "hour")) {
			unit = "(频次/小时)";
		}else if (TextUtils.equals(type, "day")) {
			unit = "(频次/天)";
		}else if (TextUtils.equals(type, "tendays")) {
			unit = "(频次/旬)";
		}else if (TextUtils.equals(type, "month")) {
			unit = "(频次/月)";
		}else if (TextUtils.equals(type, "annual")) {
			unit = "(频次/年)";
		}
		canvas.drawText(unit, (int)CommonUtil.dip2px(mContext, 20), dividerY-(int)CommonUtil.dip2px(mContext, 2), textP);


		lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 0.5f));
		lineP.setColor(0xff6ED9D9);
		lineP.setStyle(Style.STROKE);
		textP.setColor(getResources().getColor(R.color.white));
		for (int i = 0; i < size; i++) {
			StrongStreamDto dto = dataList.get(i);

			//绘制纵向分割线
			Path linePath = new Path();
			linePath.moveTo(dto.x, topMargin);
			linePath.lineTo(dto.x, h-bottomMargin);
			linePath.close();
			canvas.drawPath(linePath, lineP);

			//绘制时间
			if (TextUtils.equals(type, "tendays")) {
				textP.setTextSize(CommonUtil.dip2px(mContext, 8));
				float tempWidth1 = textP.measureText((i/3+1)+"");
				canvas.drawText((i/3+1)+"", dto.x-tempWidth1/2, h-CommonUtil.dip2px(mContext, 10), textP);
				if (i % 3 == 0) {
					float tempWidth2 = textP.measureText("上");
					canvas.drawText("上", dto.x-tempWidth2/2, h-CommonUtil.dip2px(mContext, 2), textP);
				}else if (i % 3 == 1) {
					float tempWidth2 = textP.measureText("中");
					canvas.drawText("中", dto.x-tempWidth2/2, h-CommonUtil.dip2px(mContext, 2), textP);
				}else if (i % 3 == 2) {
					float tempWidth2 = textP.measureText("下");
					canvas.drawText("下", dto.x-tempWidth2/2, h-CommonUtil.dip2px(mContext, 2), textP);
				}
			}else {
				textP.setTextSize(CommonUtil.dip2px(mContext, 10));
				float tempWidth = textP.measureText(dto.thunderTime);
				canvas.drawText(dto.thunderTime, dto.x-tempWidth/2, h-CommonUtil.dip2px(mContext, 5), textP);
			}
		}

		//绘制区域
		lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
		lineP.setColor(0x606ED9D9);
		lineP.setStyle(Style.FILL);
		for (int i = 0; i < size-1; i++) {
			float x1 = dataList.get(i).x;
			float y1 = dataList.get(i).y;
			float x2 = dataList.get(i+1).x;
			float y2 = dataList.get(i+1).y;
			
			if (i != size-1) {
				Path rectPath = new Path();
				rectPath.moveTo(x1, y1);
				rectPath.lineTo(x2, y2);
				rectPath.lineTo(x2, h-bottomMargin);
				rectPath.lineTo(x1, h-bottomMargin);
				rectPath.close();
				canvas.drawPath(rectPath, lineP);
			}
		}

		//绘制曲线
		lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
		lineP.setColor(0xff6ED9D9);
		lineP.setStyle(Style.STROKE);
		for (int i = 0; i < size-1; i++) {
			float x1 = dataList.get(i).x;
			float y1 = dataList.get(i).y;
			float x2 = dataList.get(i+1).x;
			float y2 = dataList.get(i+1).y;

			Path linePath = new Path();
			linePath.moveTo(x1, y1);
			linePath.lineTo(x2, y2);
			canvas.drawPath(linePath, lineP);
		}

		textP.setColor(0xff6ED9D9);
		textP.setTextSize(CommonUtil.dip2px(mContext, 10));
		for (int i = 0; i < size; i++) {
			StrongStreamDto dto = dataList.get(i);

			if (dto.thunderCount > 0) {
				canvas.drawCircle(dto.x, dto.y, (int)CommonUtil.dip2px(mContext, 1), lineP);

				//绘制次数
				float countWidth = textP.measureText(dto.thunderCount+"");
				if (dto.thunderCount < (maxValue+minValue)/2) {
					canvas.drawText(dto.thunderCount+"", dto.x-countWidth/2, dto.y-CommonUtil.dip2px(mContext, 5), textP);
				}else {
					canvas.drawText(dto.thunderCount+"", dto.x-countWidth/2, dto.y+CommonUtil.dip2px(mContext, 15), textP);
				}
			}
		}
		
	}

}
