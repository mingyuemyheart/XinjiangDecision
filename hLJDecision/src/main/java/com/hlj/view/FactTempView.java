package com.hlj.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.hlj.dto.FactDto;
import com.hlj.utils.CommonUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import shawn.cxwl.com.hlj.decision.R;

/**
 * 温度曲线
 * @author shawn_sun
 *
 */

public class FactTempView extends View{
	
	private Context mContext = null;
	private List<FactDto> tempList = new ArrayList<>();
	private float maxValue = 0;
	private float minValue = 0;
	private float averValue = 0;
	private float averY = 0;//平均值的y轴值
	private Paint lineP = null;//画线画笔
	private Paint textP = null;//写字画笔
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH时");
	private SimpleDateFormat sdf3 = new SimpleDateFormat("dd日");
	private Bitmap bitmap = null;
	private Bitmap point = null;
	private int totalDivider = 0;
	private int itemDivider = 1;
	
	public FactTempView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public FactTempView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public FactTempView(Context context, AttributeSet attrs, int defStyleAttr) {
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
		
		bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.iv_marker_temp),
				(int)(CommonUtil.dip2px(mContext, 25)), (int)(CommonUtil.dip2px(mContext, 25)));
		point = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.iv_point_temp), 
				(int)(CommonUtil.dip2px(mContext, 10)), (int)(CommonUtil.dip2px(mContext, 10)));
	}
	
	/**
	 * 对cubicView进行赋值
	 */
	public void setData(List<FactDto> dataList) {
		if (!dataList.isEmpty()) {
			tempList.clear();
			tempList.addAll(dataList);
			if (tempList.isEmpty()) {
				return;
			}
			
			float total = 0;
			maxValue = tempList.get(0).factTemp;
			minValue = tempList.get(0).factTemp;
			int count = 0;
			for (int i = 0; i < tempList.size(); i++) {
				FactDto dto = tempList.get(i);
				if (maxValue <= dto.factTemp) {
					maxValue = dto.factTemp;
				}
				if (minValue >= dto.factTemp) {
					minValue = dto.factTemp;
				}
				
				total += dto.factTemp;
				count++;
			}
			averValue = total/count;
			
			if (maxValue == 0 && minValue == 0) {
				totalDivider = (int) (maxValue-minValue);
				maxValue = 5;
				minValue = 0;
			}else {
				if (maxValue > 0 && minValue > 0) {
					totalDivider = (int) (maxValue-minValue);
				}else if (maxValue >= 0 && minValue <= 0) {
					totalDivider = (int) (maxValue-minValue);
				}else if (maxValue < 0 && minValue < 0) {
					totalDivider = (int) Math.abs(maxValue+minValue);
				}

				if (totalDivider <= 5) {
					itemDivider = 1;
				}else if (totalDivider > 5 && totalDivider <= 15) {
					itemDivider = 2;
				}else if (totalDivider > 15 && totalDivider <= 25) {
					itemDivider = 3;
				}else if (totalDivider > 25 && totalDivider <= 40) {
					itemDivider = 4;
				}else {
					itemDivider = 5;
				}
				
				maxValue = (float) (Math.ceil(maxValue)+itemDivider);
				minValue = (float) (Math.floor(minValue)-itemDivider);
			}
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
		float chartW = w-CommonUtil.dip2px(mContext, 40);
		float chartH = h-CommonUtil.dip2px(mContext, 80);
		float leftMargin = CommonUtil.dip2px(mContext, 20);
		float rightMargin = CommonUtil.dip2px(mContext, 20);
		float bottomMargin = CommonUtil.dip2px(mContext, 35);

		int size = tempList.size();
		float columnWidth = chartW/(size-1);
		//获取曲线上每个温度点的坐标
		for (int i = 0; i < size; i++) {
			FactDto dto = tempList.get(i);
			dto.x = columnWidth*i+leftMargin;

			float value = dto.factTemp;
			dto.y = chartH*Math.abs(maxValue-value)/totalDivider;
			Log.e("temp", value+"---"+dto.y);

			tempList.set(i, dto);
		}
		averY = chartH*Math.abs(maxValue-averValue)/totalDivider;

		for (int i = 0; i < size-1; i++) {
			FactDto dto = tempList.get(i);
			//绘制区域
			Path rectPath = new Path();
			rectPath.moveTo(dto.x, 0);
			rectPath.lineTo(dto.x+columnWidth, 0);
			rectPath.lineTo(dto.x+columnWidth, h-bottomMargin);
			rectPath.lineTo(dto.x, h-bottomMargin);
			rectPath.close();
			if (i%8 == 0 || i%8 == 1 || i%8 == 2 || i%8 == 3) {
				lineP.setColor(Color.WHITE);
			}else {
				lineP.setColor(0xfff9f9f9);
			}
			lineP.setStyle(Style.FILL);
			canvas.drawPath(rectPath, lineP);
		}
		
		for (int i = 0; i < size; i++) {
			FactDto dto = tempList.get(i);
			//绘制分割线
			Path linePath = new Path();
			linePath.moveTo(dto.x, 0);
			linePath.lineTo(dto.x, h-bottomMargin);
			linePath.close();
			lineP.setColor(0xfff1f1f1);
			lineP.setStyle(Style.STROKE);
			canvas.drawPath(linePath, lineP);
		}
		
		if (maxValue != 5 && minValue != 0) {
			//绘制平均线
			lineP.setColor(0xfff2b100);
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
			canvas.drawLine(leftMargin, averY, w-rightMargin, averY, lineP);
		}

		//绘制刻度线
		for (int i = (int) minValue; i <= maxValue; i+=itemDivider) {
			float dividerY = chartH*Math.abs(maxValue-i)/totalDivider;
			lineP.setColor(0xfff1f1f1);
			canvas.drawLine(leftMargin, dividerY, w-rightMargin, dividerY, lineP);
			textP.setColor(0xff999999);
			textP.setTextSize(CommonUtil.dip2px(mContext, 10));
			canvas.drawText(String.valueOf(i), CommonUtil.dip2px(mContext, 5), dividerY, textP);
		}
		
		//绘制曲线
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

			if (y2 != 0 && y3 != 0 && y4 != 0) {
				Path linePath = new Path();
				linePath.moveTo(x1, y1);
				linePath.cubicTo(x3, y3, x4, y4, x2, y2);
				lineP.setColor(0xffef4900);
				lineP.setStyle(Style.STROKE);
				lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 3));
				canvas.drawPath(linePath, lineP);
			}
		}
		
		for (int i = 0; i < size; i++) {
			FactDto dto = tempList.get(i);
			canvas.drawBitmap(point, dto.x-point.getWidth()/2, dto.y-point.getHeight()/2, lineP);
			
			//绘制曲线上每个点的数据值
			textP.setColor(Color.WHITE);
			textP.setTextSize(CommonUtil.dip2px(mContext, 10));
			float tempWidth = textP.measureText(dto.factTemp+"");
			canvas.drawBitmap(bitmap, dto.x-bitmap.getWidth()/2, dto.y-bitmap.getHeight()-CommonUtil.dip2px(mContext, 2.5f), textP);
			canvas.drawText(dto.factTemp+"", dto.x-tempWidth/2, dto.y-bitmap.getHeight()/2, textP);
			
			//绘制24小时
			try {
				if (!TextUtils.isEmpty(dto.factTime)) {
					if (i == 0) {
						String time1 = sdf3.format(sdf1.parse(dto.factTime));
						if (!TextUtils.isEmpty(time1)) {
							float text = textP.measureText(time1);
							textP.setColor(0xff999999);
							textP.setTextSize(CommonUtil.dip2px(mContext, 10));
							canvas.drawText(time1, dto.x-text/2, h-CommonUtil.dip2px(mContext, 10f), textP);
						}
					}
					String time = sdf2.format(sdf1.parse(dto.factTime));
					if (!TextUtils.isEmpty(time)) {
						float text = textP.measureText(time);
						textP.setColor(0xff999999);
						textP.setTextSize(CommonUtil.dip2px(mContext, 10));
						canvas.drawText(time, dto.x-text/2, h-CommonUtil.dip2px(mContext, 20f), textP);
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		lineP.reset();
		textP.reset();
	}
	
}
