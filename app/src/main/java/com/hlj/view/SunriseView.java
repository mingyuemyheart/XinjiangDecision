package com.hlj.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.hlj.utils.CommonUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import shawn.cxwl.com.hlj.R;

/**
 * 日出日落
 */
@SuppressLint({ "DrawAllocation", "SimpleDateFormat" })
public class SunriseView extends View{
	
	private Context mContext = null;
	private Paint dashP = null;
	private Paint lineP = null;//画线画笔
	private Paint textP = null;//写字画笔
	private Bitmap bitmap = null;
	private String sunrise = null;//日出时间
	private String sunset = null;//日落时间
	private SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd ");
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	public SunriseView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public SunriseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public SunriseView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		init();
	}
	
	private void init() {
		dashP = new Paint();
		dashP.setStyle(Style.STROKE);
		dashP.setStrokeCap(Paint.Cap.ROUND);
		dashP.setAntiAlias(true);
		
		lineP = new Paint();
		lineP.setStyle(Style.STROKE);
		lineP.setStrokeCap(Paint.Cap.ROUND);
		lineP.setAntiAlias(true);
		
		textP = new Paint();
		textP.setAntiAlias(true);
		
		bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.day00_mini),
				(int)(CommonUtil.dip2px(mContext, 20)), (int)(CommonUtil.dip2px(mContext, 20)));
	}
	
	/**
	 * 对cubicView进行赋值
	 */
	public void setData(String sunrise, String sunset) {
		this.sunrise = sunrise;
		this.sunset = sunset;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(0xff6B7EDA);
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		float chartW = w-CommonUtil.dip2px(mContext, 40);
		float chartH = h-CommonUtil.dip2px(mContext, 40);
		float leftMargin = CommonUtil.dip2px(mContext, 20);
		float rightMargin = CommonUtil.dip2px(mContext, 20);
		float topMargin = CommonUtil.dip2px(mContext, 20);
		float bottomMargin = CommonUtil.dip2px(mContext, 40);
		
		try {
			//绘制圆形
			dashP.reset();
			dashP.setColor(Color.WHITE);
			dashP.setStrokeWidth(2.0f);
			dashP.setStyle(Style.STROKE);
			DashPathEffect dpe = new DashPathEffect(new float[]{8,8,8,8}, 1);
			dashP.setPathEffect(dpe);
			float radius = (h-bottomMargin);//半径
			float centerX = w/2;//圆心
			float centerY = h;//圆心
			canvas.drawCircle(centerX, centerY, radius, dashP);
			
			long rise = sdf3.parse(sdf2.format(new Date())+sunrise).getTime();
			long set = sdf3.parse(sdf2.format(new Date())+sunset).getTime();
			long now = new Date().getTime();
//			now = sdf3.parse(sdf2.format(new Date())+"16:10").getTime();
			if (now < rise) {
				now = rise+1000*60*6;
			}else if (now > set) {
				now = set-1000*60*6;
			}
			float startX = (w-radius*2)/2;//日出点
			float endX = (w-radius*2)/2+radius*2;//日落点
			float sunX = (now-rise)*(endX-startX)/(set-rise);//x轴上距离圆心的三角形距离
			
			//绘制矩形
			lineP.setColor(0x30ffffff);
			lineP.setStrokeWidth(2.0f);
			lineP.setStyle(Style.FILL);
			RectF rectF = new RectF(centerX-radius, h-radius, centerX+radius, h+radius);
//			canvas.drawRect(rectF, lineP);

			//绘制当前日出弧线
			float a = Math.abs(radius-sunX);
			float c = radius;
			float b = (float) Math.sqrt(c*c - a*a);
			textP.setColor(Color.WHITE);
			textP.setTextSize(CommonUtil.dip2px(mContext, 10));
			float cWidth = (float) ((now-rise)*(endX-startX)/(set-rise))+startX;
//			canvas.drawText(cWidth+"", cWidth, h-b, textP);

			if (now < ((set-rise)/2+rise)) {//正午时分
				canvas.drawArc(rectF, 180, (float) (90-Math.asin(a/c)*180/Math.PI), true, lineP);

				//绘制三角形
				Path trPath = new Path();
				trPath.moveTo(centerX, centerY);
				trPath.lineTo(cWidth, h-b);
				trPath.lineTo(cWidth, h);
				trPath.close();
				lineP.setColor(0xff6B7EDA);
				canvas.drawPath(trPath, lineP);
			}else {
				canvas.drawArc(rectF, 180, (float) (90+Math.asin(a/c)*180/Math.PI), true, lineP);

				//绘制三角形
				Path trPath = new Path();
				trPath.moveTo(centerX, centerY);
				trPath.lineTo(cWidth, h-b);
				trPath.lineTo(cWidth, h);
				trPath.close();
				lineP.setColor(0x30ffffff);
				canvas.drawPath(trPath, lineP);
			}
			
			//绘制太阳
			canvas.drawBitmap(bitmap, cWidth-bitmap.getWidth()/2, h-b-bitmap.getHeight()/2, textP);
			
			//绘制最下方部分遮挡
			lineP.setColor(0xff6B7EDA);
			lineP.setStyle(Style.FILL);
			canvas.drawRect(new RectF(0, h-bottomMargin, w, h), lineP);

			//绘制日出日落圆点
			lineP.setColor(Color.YELLOW);
			lineP.setStyle(Style.FILL);
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 5));
			canvas.drawPoint(startX+CommonUtil.dip2px(mContext, 5.5f), h-bottomMargin, lineP);
			canvas.drawPoint(endX-CommonUtil.dip2px(mContext, 5.5f), h-bottomMargin, lineP);
			
			//绘制日出日落时间
			textP.setColor(Color.WHITE);
			textP.setTextSize(CommonUtil.dip2px(mContext, 12));
			if (!TextUtils.isEmpty(sunrise)) {
				float sunriseWidth1 = textP.measureText("日出");
				canvas.drawText("日出", startX+CommonUtil.dip2px(mContext, 10f)-sunriseWidth1/2, h-bottomMargin+CommonUtil.dip2px(mContext, 15), textP);
				float sunriseWidth = textP.measureText(sunrise);
				canvas.drawText(sunrise, startX+CommonUtil.dip2px(mContext, 10f)-sunriseWidth/2, h-bottomMargin+CommonUtil.dip2px(mContext, 30), textP);
			}
			if (!TextUtils.isEmpty(sunset)) {
				float sunriseWidth1 = textP.measureText("日落");
				canvas.drawText("日落", endX-CommonUtil.dip2px(mContext, 10f)-sunriseWidth1/2, h-bottomMargin+CommonUtil.dip2px(mContext, 15), textP);
				float sunsetWidth = textP.measureText(sunset);
				canvas.drawText(sunset, endX-CommonUtil.dip2px(mContext, 10f)-sunsetWidth/2, h-bottomMargin+CommonUtil.dip2px(mContext, 30), textP);
			}
			
			//绘制时间点一行的直线
			lineP.setStyle(Style.STROKE);
			lineP.setColor(0x30ffffff);
			lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
			canvas.drawLine(leftMargin, h-bottomMargin, w-rightMargin, h-bottomMargin, lineP);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}
	
}
