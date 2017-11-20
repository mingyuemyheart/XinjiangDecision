package com.hlj.trend;

/**
 * 24小时实况相对湿度曲线图
 */
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.hlj.dto.TrendDto;
import com.hlj.utils.CommonUtil;

public class HumidityView extends View{
	
	private Context mContext = null;
	private List<TrendDto> tempList = new ArrayList<TrendDto>();
	private int maxValue = 0;
	private int minValue = 0;
	private Paint lineP = null;//画线画笔
	private Paint textP = null;//写字画笔
	private int saveTime = 0;
	private int time = 0;//发布时间
	
	public HumidityView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public HumidityView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public HumidityView(Context context, AttributeSet attrs, int defStyleAttr) {
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
	}
	
	/**
	 * 对cubicView进行赋值
	 */
	public void setData(List<TrendDto> dataList, int time) {
		this.time = time;
		this.saveTime = time;
		if (!dataList.isEmpty()) {
			tempList.addAll(dataList);
			
			maxValue = tempList.get(0).humidity;
			minValue = tempList.get(0).humidity;
			for (int i = 0; i < tempList.size(); i++) {
				if (maxValue <= tempList.get(i).humidity) {
					maxValue = tempList.get(i).humidity;
				}
				if (minValue >= tempList.get(i).humidity) {
					minValue = tempList.get(i).humidity;
				}
			}
			
			maxValue = 100;
			minValue = 0;
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (tempList.isEmpty()) {
			return;
		}
		
		canvas.drawColor(0x30ffffff);
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		float chartW = w-CommonUtil.dip2px(mContext, 30);
		float chartH = h-CommonUtil.dip2px(mContext, 40);
		float leftMargin = CommonUtil.dip2px(mContext, 20);
		float rightMargin = CommonUtil.dip2px(mContext, 10);
		float topMargin = CommonUtil.dip2px(mContext, 20);
		float bottomMargin = CommonUtil.dip2px(mContext, 20);
		float chartMaxH = chartH * maxValue / (Math.abs(maxValue)+Math.abs(minValue));//同时存在正负值时，正值高度
		
//		canvas.drawColor(Color.BLACK);
//		Paint pp = new Paint();
//		pp.setColor(Color.RED);
//		pp.setStyle(Style.FILL);
//		canvas.drawRect(CommonUtil.dip2px(mContext, 10), CommonUtil.dip2px(mContext, 10), w-CommonUtil.dip2px(mContext, 10), h-bottomMargin, pp);
		
		int size = tempList.size();
		
		//获取曲线上每个温度点的坐标
		for (int i = 0; i < size; i++) {
			TrendDto dto = tempList.get(i);
			dto.x = (chartW/(size-1))*i + leftMargin;
			
			float value = tempList.get(i).humidity;
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
		
		//绘制刻度线，每间隔为20
		int totalDivider = Math.abs(maxValue)+Math.abs(minValue);
		int itemDivider = 20;
		if (totalDivider <= 100) {
			itemDivider = 20;
		}
		for (int i = 0; i <= totalDivider; i+=itemDivider) {
//			float dividerX = CommonUtil.dip2px(mContext, 5);
			float dividerY = 0;
			int value = i;
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
			lineP.setColor(0x30ffffff);
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
			canvas.drawLine(leftMargin, dividerY, w-rightMargin, dividerY, lineP);
			textP.setColor(Color.WHITE);
			textP.setTextSize(CommonUtil.dip2px(mContext, 10));
			canvas.drawText(String.valueOf(i), CommonUtil.dip2px(mContext, 5), dividerY-CommonUtil.dip2px(mContext, 2), textP);
		}
		
		//绘制曲线
		Path linePath = new Path();
		for (int i = 0; i < tempList.size(); i++) {
			TrendDto dto = tempList.get(i);
			if (i == 0) {
				linePath.moveTo(dto.x, dto.y);
			}else {
				linePath.lineTo(dto.x, dto.y);
			}
		}
		lineP.setColor(Color.WHITE);
		lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
		canvas.drawPath(linePath, lineP);
		
		for (int i = 0; i < tempList.size(); i++) {
			TrendDto dto = tempList.get(i);
			
			//绘制区域
			if (i != tempList.size()-1) {
				Path rectPath = new Path();
				rectPath.moveTo(dto.x, dto.y);
				rectPath.lineTo(tempList.get(i+1).x, tempList.get(i+1).y);
				rectPath.lineTo(tempList.get(i+1).x, h-bottomMargin);
				rectPath.lineTo(dto.x, h-bottomMargin);
				rectPath.close();
				lineP.setColor(0x30ffffff);
				lineP.setStyle(Style.FILL);
				canvas.drawPath(rectPath, lineP);
			}
			
			//绘制曲线上每个点的白点
			lineP.setColor(Color.WHITE);
			lineP.setStyle(Paint.Style.STROKE);
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 5));
			canvas.drawPoint(dto.x, dto.y, lineP);
			
//			//绘制曲线上每个点的数据值
//			textP.setColor(Color.WHITE);
//			textP.setTextSize(CommonUtil.dip2px(mContext, 10));
//			float tempWidth = textP.measureText(String.valueOf(tempList.get(i).humidity));
//			canvas.drawText(String.valueOf(tempList.get(i).humidity), dto.x-tempWidth/2, dto.y-CommonUtil.dip2px(mContext, 5f), textP);
			
//			//绘制刻度
//			lineP.setColor(Color.WHITE);
//			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
//			canvas.drawLine(dto.x, h-bottomMargin, dto.x, h-CommonUtil.dip2px(mContext, 25), lineP);
			
			//绘制24小时
			textP.setColor(Color.WHITE);
			textP.setTextSize(CommonUtil.dip2px(mContext, 10));
			
			if (time > 23) {
				time = 0;
			}
			
			float text = textP.measureText(String.valueOf(time));
			canvas.drawText(String.valueOf(time), dto.x-text/2, h-CommonUtil.dip2px(mContext, 5f), textP);
			time++;
		}
		
		time = saveTime;
		
	}

}
