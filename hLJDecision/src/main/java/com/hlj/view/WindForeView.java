package com.hlj.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.hlj.dto.WindDto;
import com.hlj.utils.CommonUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 风场曲线图
 */
public class WindForeView extends View{
	
	private Context mContext;
	private SimpleDateFormat sdf0 = new SimpleDateFormat("yyyyMMddHH", Locale.CHINA);
	private SimpleDateFormat sdf1 = new SimpleDateFormat("HH", Locale.CHINA);
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM月dd日", Locale.CHINA);
	private List<WindDto> tempList = new ArrayList<>();
	private float maxValue,minValue;
	private Paint lineP,textP;
	private float totalDivider = 0;
	private int itemDivider = 1;

//	private float clickX,clickY;
//	private String speed;//选择点的风速
//	private boolean isFirst = true;//判断是否为第一次绘制

	public WindForeView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public WindForeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public WindForeView(Context context, AttributeSet attrs, int defStyleAttr) {
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
	public void setData(List<WindDto> dataList) {
		if (!dataList.isEmpty()) {
			tempList.clear();
			tempList.addAll(dataList);
			
			maxValue = Float.valueOf(tempList.get(0).speed);
			minValue = Float.valueOf(tempList.get(0).speed);
			for (int i = 0; i < tempList.size(); i++) {
				if (maxValue <= Float.valueOf(tempList.get(i).speed)) {
					maxValue = Float.valueOf(tempList.get(i).speed);
				}
				if (minValue >= Float.valueOf(tempList.get(i).speed)) {
					minValue = Float.valueOf(tempList.get(i).speed);
				}
			}

			maxValue = maxValue+itemDivider;
			minValue = 0;
			totalDivider = maxValue-minValue;
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
		float chartW = w- CommonUtil.dip2px(mContext, 40);
		float chartH = h-CommonUtil.dip2px(mContext, 60);
		float leftMargin = CommonUtil.dip2px(mContext, 20);
		float rightMargin = CommonUtil.dip2px(mContext, 20);
		float topMargin = CommonUtil.dip2px(mContext, 20);
		float bottomMargin = CommonUtil.dip2px(mContext, 40);
		
		int size = tempList.size();
		//获取曲线上每个温度点的坐标
		for (int i = 0; i < size; i++) {
			WindDto dto = tempList.get(i);
			dto.x = (chartW/(size-1))*i + leftMargin;
			
			float value = Float.valueOf(dto.speed);
			dto.y = chartH*Math.abs(maxValue-value)/totalDivider+topMargin;

			tempList.set(i, dto);
		}

		//绘制x、y轴
		lineP.setColor(0xff868686);
		lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
		canvas.drawLine(leftMargin, topMargin, leftMargin, h-bottomMargin, lineP);
		canvas.drawLine(leftMargin, h-bottomMargin, w-rightMargin, h-bottomMargin, lineP);
		
		for (int i = 0; i <= totalDivider; i+=itemDivider) {
			float dividerY = chartH - chartH*Math.abs(i)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;

			//绘制横向分割线
			lineP.setColor(0xff868686);
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 0.2f));
			canvas.drawLine(leftMargin, dividerY, w-rightMargin, dividerY, lineP);

			//刻度
			textP.setColor(0xff868686);
			textP.setTextSize(CommonUtil.dip2px(mContext, 10));
			canvas.drawText(i+"", CommonUtil.dip2px(mContext, 5), dividerY, textP);
			if (i == maxValue) {
				canvas.drawText("风速(m/s)", leftMargin, dividerY-CommonUtil.dip2px(mContext, 5), textP);
			}
		}
		
		//绘制贝塞尔曲线
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
			
			lineP.setColor(Color.WHITE);
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 2));
			Path pathLow = new Path();
			pathLow.moveTo(x1, y1);
			pathLow.cubicTo(x3, y3, x4, y4, x2, y2);
			canvas.drawPath(pathLow, lineP);
		}
		
		for (int i = 0; i < size; i++) {
			WindDto dto = tempList.get(i);
			//绘制每个时间点上的时间值
			textP.setColor(0xff868686);
			textP.setTextSize(CommonUtil.dip2px(mContext, 10));
			try {
				String hourlyTime = sdf1.format(sdf0.parse(dto.date));
				float hourWidth = textP.measureText(hourlyTime+"时");
				canvas.drawText(hourlyTime+"时", dto.x-hourWidth/2, h-bottomMargin+CommonUtil.dip2px(mContext, 15), textP);

				if (i == 0) {
					canvas.drawText(sdf2.format(sdf0.parse(dto.date)), dto.x-hourWidth/2, h-CommonUtil.dip2px(mContext, 5), textP);
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
//		if (isFirst) {
//			clickX = tempList.get(0).x;
//			clickY = tempList.get(0).y;
//			speed = tempList.get(0).speed;
//			isFirst = false;
//		}
//		if (clickX != 0 && clickY != 0) {
//			lineP.setColor(getResources().getColor(R.color.blue));
//			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 6));
//			canvas.drawPoint(clickX, clickY, lineP);
//
//			textP.setColor(getResources().getColor(R.color.white));
//			textP.setTextSize(CommonUtil.dip2px(mContext, 12));
//			float tempWidth = textP.measureText(speed+getResources().getString(R.string.unit_speed));
//			canvas.drawText(speed+getResources().getString(R.string.unit_speed), clickX-tempWidth/2, clickY-CommonUtil.dip2px(mContext, 10), textP);
//		}
	}
	
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		switch (event.getAction()) {
//		case MotionEvent.ACTION_DOWN:
//			float x = event.getX();
//			float y = event.getY();
//			for (int i = 0; i < tempList.size(); i++) {
//				WindDto dto = tempList.get(i);
//				if (x > dto.x-50 && x < dto.x+50 && y > dto.y-50 && y < dto.y+50) {
//					clickX = dto.x;
//					clickY = dto.y;
//					speed = dto.speed;
//					postInvalidate();
//					break;
//				}
//			}
//			break;
//		case MotionEvent.ACTION_UP:
//
//			break;
//
//		default:
//			break;
//		}
//		return super.onTouchEvent(event);
//	}

}
