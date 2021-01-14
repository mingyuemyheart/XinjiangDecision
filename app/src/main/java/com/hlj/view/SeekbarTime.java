package com.hlj.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.hlj.dto.StrongStreamDto;
import com.hlj.utils.CommonUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import shawn.cxwl.com.hlj.R;

/**
 * 进度条时间
 */
public class SeekbarTime extends View {

    private Context mContext;
    private Paint textP;//画线画笔
    private List<StrongStreamDto> dataList = new ArrayList<>();//雷达图层数据
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
    private SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm", Locale.CHINA);

    public SeekbarTime(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public SeekbarTime(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public SeekbarTime(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        textP = new Paint();
        textP.setAntiAlias(true);
    }

    /**
     * 设置数据
     * @param dataList
     */
    public void setData(List<StrongStreamDto> dataList) {
        if (dataList.size() <= 0) {
            return;
        }
        this.dataList.clear();
        this.dataList.addAll(dataList);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = canvas.getWidth();
        float h = canvas.getHeight();
        float leftMargin = CommonUtil.dip2px(mContext, 40);//左边距
        float rightMargin = CommonUtil.dip2px(mContext, 20);//右边距
        float seekBarWidth = w-leftMargin-rightMargin;//seekbar宽度
        float itemWidth = seekBarWidth/(dataList.size()-1);//每个刻度对应的宽度

        canvas.drawColor(Color.TRANSPARENT);

        //绘制时间
        textP.setColor(getResources().getColor(R.color.text_color2));
        textP.setTextSize(CommonUtil.dip2px(mContext, 10));
        for (int i = 0; i < dataList.size(); i+=6) {
            StrongStreamDto dto = dataList.get(i);
            try {
                String startTime = sdf2.format(sdf1.parse(dto.startTime));
                Log.e("startTime", startTime);
                float startTimeWidth = textP.measureText(startTime);
                canvas.drawText(startTime, leftMargin-startTimeWidth+itemWidth*i, h, textP);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }

}
