package com.hlj.view;

/**
 * 绘制平滑曲线
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
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

import shawn.cxwl.com.hlj.R;

public class CubicView2 extends View{

    private Context mContext = null;
    private SimpleDateFormat sdf0 = new SimpleDateFormat("yyyyMMddHHmm");
    private SimpleDateFormat sdf1 = new SimpleDateFormat("HH时");
    private SimpleDateFormat sdf2 = new SimpleDateFormat("HH");
    private List<WeatherDto> tempList = new ArrayList<>();
    private int maxTemp = 0;//最高温度
    private int minTemp = 0;//最低温度
    private Paint lineP = null;//画线画笔
    private Paint textP = null;//写字画笔
    private int totalDivider = 0;
    private int itemDivider = 1;

    public CubicView2(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public CubicView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public CubicView2(Context context, AttributeSet attrs, int defStyleAttr) {
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
            }else if (totalDivider <= 15) {
                itemDivider = 2;
            }else if (totalDivider <= 25) {
                itemDivider = 3;
            }else if (totalDivider <= 40) {
                itemDivider = 4;
            }else {
                itemDivider = 5;
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
        for (int i = minTemp; i <= maxTemp; i+=itemDivider) {
            float dividerY = chartH*Math.abs(maxTemp-i)/totalDivider;
            lineP.setColor(0x30ffffff);
            canvas.drawLine(CommonUtil.dip2px(mContext, 25), dividerY, w-rightMargin, dividerY, lineP);
            textP.setColor(mContext.getResources().getColor(R.color.white));
            textP.setTextSize(CommonUtil.dip2px(mContext, 10));
            canvas.drawText(i+"℃", CommonUtil.dip2px(mContext, 5), dividerY, textP);
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
            lineP.setStrokeWidth(3.0f);
            canvas.drawPath(pathLow, lineP);
        }

        float halfX = (tempList.get(1).x - tempList.get(0).x)/3;
//        float halfX = chartW/6/3;
        for (int i = 0; i < tempList.size(); i++) {
            WeatherDto dto = tempList.get(i);

            //绘制曲线上每个时间点marker
            lineP.setColor(getResources().getColor(R.color.cubic_color));
            lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 5));
            canvas.drawPoint(dto.x, dto.y, lineP);

            try {
                long zao8 = sdf2.parse("05").getTime();
                long wan8 = sdf2.parse("18").getTime();
                long current = sdf2.parse(sdf2.format(sdf0.parse(dto.hourlyTime))).getTime();
                Bitmap b;
                if (current >= zao8 && current < wan8) {
                    b = WeatherUtil.getBitmap(mContext, dto.hourlyCode);
                }else {
                    b = WeatherUtil.getNightBitmap(mContext, dto.hourlyCode);
                }
                Bitmap newBit = ThumbnailUtils.extractThumbnail(b, (int)(CommonUtil.dip2px(mContext, 18)), (int)(CommonUtil.dip2px(mContext, 18)));
                canvas.drawBitmap(newBit, dto.x-newBit.getWidth()/2, dto.y-CommonUtil.dip2px(mContext, 25f), textP);
                textP.setColor(getResources().getColor(R.color.white));
                textP.setTextSize(CommonUtil.dip2px(mContext, 10));
                float tempWidth = textP.measureText(String.valueOf(dto.hourlyTemp)+"℃");
                canvas.drawText(String.valueOf(dto.hourlyTemp)+"℃", dto.x-tempWidth/2, dto.y+(int)(CommonUtil.dip2px(mContext, 15)), textP);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //绘制风速风向
            lineP.setColor(0x30ffffff);
            lineP.setStyle(Style.FILL);
            lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
            float windHeight = h-CommonUtil.dip2px(mContext, WeatherUtil.getHourWindForceHeight(dto.hourlyWindForceCode));
            canvas.drawRect(dto.x-halfX+CommonUtil.dip2px(mContext, 2), windHeight, dto.x+halfX-CommonUtil.dip2px(mContext, 2), h-CommonUtil.dip2px(mContext, 20), lineP);

            textP.setColor(getResources().getColor(R.color.white));
            textP.setTextSize(CommonUtil.dip2px(mContext, 10));
            String windDir = mContext.getString(WeatherUtil.getWindDirection(dto.hourlyWindDirCode));
            float hWindDir = textP.measureText(windDir);
            canvas.drawText(windDir, dto.x-hWindDir/2, windHeight-CommonUtil.dip2px(mContext, 15f), textP);

            textP.setColor(getResources().getColor(R.color.white));
            textP.setTextSize(CommonUtil.dip2px(mContext, 10));
            String windForce = WeatherUtil.getHourWindForce(dto.hourlyWindForceCode);
            float hWindForce = textP.measureText(windForce);
            canvas.drawText(windForce, dto.x-hWindForce/2, windHeight-CommonUtil.dip2px(mContext, 3f), textP);

            //绘制每个时间点上的时间值
            textP.setColor(getResources().getColor(R.color.white));
            textP.setTextSize(CommonUtil.dip2px(mContext, 10));
            try {
                String hourlyTime = sdf1.format(sdf0.parse(dto.hourlyTime));
                canvas.drawText(hourlyTime, dto.x-CommonUtil.dip2px(mContext, 12.5f), h-CommonUtil.dip2px(mContext, 5f), textP);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        //绘制时间点一行的直线
        lineP.setStyle(Style.STROKE);
        lineP.setColor(0x30ffffff);
        lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
        canvas.drawLine(0, h-CommonUtil.dip2px(mContext, 20), w, h-CommonUtil.dip2px(mContext, 20), lineP);
    }

}