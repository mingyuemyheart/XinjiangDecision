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
import android.view.View;


import com.hlj.dto.FactDto;
import com.hlj.utils.CommonUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * 降水柱状图
 * @author shawn_sun
 *
 */

public class FactRainView extends View{

	private Context mContext = null;
	private List<FactDto> tempList = new ArrayList<>();
	private float maxValue = 0;
	private float minValue = 0;
	private Paint lineP = null;//画线画笔
	private Paint textP = null;//写字画笔
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("HH时");
	private SimpleDateFormat sdf3 = new SimpleDateFormat("dd日");
	private Bitmap bitmap = null;

	public FactRainView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public FactRainView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public FactRainView(Context context, AttributeSet attrs, int defStyleAttr) {
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
		
		bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.iv_marker_rain),
				(int)(CommonUtil.dip2px(mContext, 25)), (int)(CommonUtil.dip2px(mContext, 25)));
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
			
			maxValue = tempList.get(0).factRain;
			minValue = tempList.get(0).factRain;
			for (int i = 0; i < tempList.size(); i++) {
				FactDto dto = tempList.get(i);
				if (maxValue <= dto.factRain) {
					maxValue = dto.factRain;
				}
				if (minValue >= dto.factRain) {
					minValue = dto.factRain;
				}
			}
			
			if (maxValue == 0 && minValue == 0) {
				maxValue = 5;
				minValue = 0;
			}else {
				int totalDivider = (int) (Math.ceil(Math.abs(maxValue)));
				int itemDivider = 1;
				if (totalDivider <= 5) {
					itemDivider = 1;
				}else if (totalDivider <= 10 && totalDivider > 5) {
					itemDivider = 2;
				}else if (totalDivider <= 15 && totalDivider > 10) {
					itemDivider = 3;
				}else if (totalDivider <= 20 && totalDivider > 15) {
					itemDivider = 4;
				}else if (totalDivider <= 25 && totalDivider > 20) {
					itemDivider = 5;
				}else if (totalDivider <= 30 && totalDivider > 25) {
					itemDivider = 6;
				}else if (totalDivider <= 35 && totalDivider > 30) {
					itemDivider = 7;
				}else if (totalDivider <= 40 && totalDivider > 35) {
					itemDivider = 8;
				}else if (totalDivider <= 45 && totalDivider > 40) {
					itemDivider = 9;
				}else {
					itemDivider = 10;
				}
				maxValue = (float) (Math.ceil(maxValue)+itemDivider);
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
		float chartH = h-CommonUtil.dip2px(mContext, 45);
		float leftMargin = CommonUtil.dip2px(mContext, 20);
		float rightMargin = CommonUtil.dip2px(mContext, 20);
		float topMargin = CommonUtil.dip2px(mContext, 10);
		float bottomMargin = CommonUtil.dip2px(mContext, 35);
		
		int size = tempList.size();
		float columnWidth = chartW/size;
		//获取曲线上每个温度点的坐标
		for (int i = 0; i < size; i++) {
			FactDto dto = tempList.get(i);
			dto.x = columnWidth*i + columnWidth/2 + leftMargin;
			
			float value = dto.factRain;
			dto.y = chartH-chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
			tempList.set(i, dto);
		}
		
		for (int i = 0; i < size-1; i++) {
			FactDto dto = tempList.get(i);
			//绘制区域
			Path rectPath = new Path();
			rectPath.moveTo(dto.x-columnWidth/2, topMargin);
			rectPath.lineTo(dto.x+columnWidth/2, topMargin);
			rectPath.lineTo(dto.x+columnWidth/2, h-bottomMargin);
			rectPath.lineTo(dto.x-columnWidth/2, h-bottomMargin);
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
			linePath.moveTo(dto.x-columnWidth/2, topMargin);
			linePath.lineTo(dto.x-columnWidth/2, h-bottomMargin);
			linePath.close();
			lineP.setColor(0xfff1f1f1);
			lineP.setStyle(Style.STROKE);
			canvas.drawPath(linePath, lineP);
		}
		
		int totalDivider = (int) (Math.ceil(Math.abs(maxValue)));
		int itemDivider = 1;
		if (totalDivider <= 5) {
			itemDivider = 1;
		}else if (totalDivider <= 10 && totalDivider > 5) {
			itemDivider = 2;
		}else if (totalDivider <= 15 && totalDivider > 10) {
			itemDivider = 3;
		}else if (totalDivider <= 20 && totalDivider > 15) {
			itemDivider = 4;
		}else if (totalDivider <= 25 && totalDivider > 20) {
			itemDivider = 5;
		}else if (totalDivider <= 30 && totalDivider > 25) {
			itemDivider = 6;
		}else if (totalDivider <= 35 && totalDivider > 30) {
			itemDivider = 7;
		}else if (totalDivider <= 40 && totalDivider > 35) {
			itemDivider = 8;
		}else if (totalDivider <= 45 && totalDivider > 40) {
			itemDivider = 9;
		}else {
			itemDivider = 10;
		}
		for (int i = (int) minValue; i <= totalDivider; i+=itemDivider) {
			float dividerY;
			int value = i;
			dividerY = chartH-chartH*Math.abs(value)/(Math.abs(maxValue)+Math.abs(minValue)) + topMargin;
			lineP.setColor(0xfff1f1f1);
			canvas.drawLine(leftMargin, dividerY, w-rightMargin, dividerY, lineP);
			textP.setColor(0xff999999);
			textP.setTextSize(CommonUtil.dip2px(mContext, 10));
			canvas.drawText(String.valueOf(i), CommonUtil.dip2px(mContext, 5), dividerY, textP);
		}
		
		for (int i = 0; i < size; i++) {
			FactDto dto = tempList.get(i);
			//绘制柱形图
			lineP.setColor(0xff0096d3);
			lineP.setStyle(Style.FILL);
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
			canvas.drawRect(dto.x-columnWidth/4, dto.y, dto.x+columnWidth/4, h-bottomMargin, lineP);
			
			//绘制曲线上每个点的数据值
			if (dto.factRain != 0) {
				textP.setColor(0xff0096d3);
				textP.setTextSize(CommonUtil.dip2px(mContext, 10));
				float tempWidth = textP.measureText(dto.factRain+"");
				canvas.drawBitmap(bitmap, dto.x-bitmap.getWidth()/2, dto.y-bitmap.getHeight()-CommonUtil.dip2px(mContext, 2.5f), textP);
				canvas.drawText(dto.factRain+"", dto.x-tempWidth/2, dto.y-bitmap.getHeight()/2, textP);
			}
			
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
