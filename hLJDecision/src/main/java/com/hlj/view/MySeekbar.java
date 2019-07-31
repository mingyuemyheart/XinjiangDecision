package com.hlj.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.media.ThumbnailUtils;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.hlj.dto.StrongStreamDto;
import com.hlj.utils.CommonUtil;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import shawn.cxwl.com.hlj.R;

/**
 * Created by shawn on 2017/8/23.
 */

public class MySeekbar extends View {

    private Context mContext = null;
    private Paint lineP = null;//画线画笔
    private Paint dashP = null;//虚线画笔
    private Paint textP = null;//写字画笔
    private Bitmap playBit = null;//播放按钮
    private Bitmap pauseBit = null;//暂停按钮

    private List<StrongStreamDto> dataList = new ArrayList<>();//雷达图层数据
    private HashMap<String, JSONObject> dataMap = new HashMap<>();//强对流数据
    private String startTime, currentTime, endTime;
    private int currentIndex = 0;//当前时间对应的索引
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmm");
    private SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
    private boolean isDraggingThumb = false;//是否拖拽滑块
    public int playingIndex = 0;//线程播放时索引
    public String playingTime = null;
    private String BROAD_CLICKMENU = "broad_clickMenu";//点击播放或暂停
    private final int STATE_NONE = 0;
    private final int STATE_PLAYING = 1;
    private final int STATE_PAUSE = 2;
    private int STATE = STATE_NONE;

    public MySeekbar(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public MySeekbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public MySeekbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        lineP = new Paint();
        lineP.setStyle(Paint.Style.STROKE);
        lineP.setStrokeCap(Paint.Cap.ROUND);
        lineP.setAntiAlias(true);

        dashP = new Paint();
        dashP.setStyle(Paint.Style.STROKE);
        dashP.setStrokeCap(Paint.Cap.ROUND);
        dashP.setAntiAlias(true);

        textP = new Paint();
        textP.setAntiAlias(true);

        playBit = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.shawn_icon_play),
                (int)(CommonUtil.dip2px(mContext, 40)), (int)(CommonUtil.dip2px(mContext, 40)));

        pauseBit = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.shawn_icon_pause),
                (int)(CommonUtil.dip2px(mContext, 40)), (int)(CommonUtil.dip2px(mContext, 40)));
    }

    /**
     * 设置数据
     * @param radarList
     */
    public void setData(List<StrongStreamDto> radarList, HashMap<String, JSONObject> hashMap) {
        if (radarList.isEmpty()) {
            return;
        }
        dataList.clear();
        dataList.addAll(radarList);

        for (int i = 0; i < dataList.size(); i++) {
            StrongStreamDto dto = dataList.get(i);
            try {
                if (i == 0) {
                    startTime = sdf2.format(sdf1.parse(dto.time));
                }else if (i == dataList.size()-1) {
                    endTime = sdf2.format(sdf1.parse(dto.time));
                }
                if (TextUtils.equals(dto.tag, "currentTime")) {
                    currentTime = dto.time;
                    currentIndex = i;
                    playingIndex = currentIndex;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (!hashMap.isEmpty()) {
            dataMap.clear();
            dataMap.putAll(hashMap);
        }
    }

    private float margin2 = 0;
    private float itemWidth = 0;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = canvas.getWidth();
        float h = canvas.getHeight();
        float leftMargin = CommonUtil.dip2px(mContext, 10);//左边距
        float rightMargin = CommonUtil.dip2px(mContext, 20);//右边距
        float margin1 = CommonUtil.dip2px(mContext, 10);//滑块距离播放按钮距离
        margin2 = leftMargin+playBit.getWidth()+margin1;//seekbar距离左边长度
        float seekBarWidth = w-margin2-rightMargin;//seekbar宽度
        itemWidth = seekBarWidth/(dataList.size()-1);//每个刻度对应的宽度

        canvas.drawColor(Color.TRANSPARENT);

        //绘制强对流时间数据
        lineP.setColor(0xff1077ad);
        lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
        lineP.setStyle(Paint.Style.FILL);

        PathEffect pathEffect = new DashPathEffect(new float[]{10,10,10,10}, 1);
        dashP.setColor(0xff1077ad);
        dashP.setStrokeWidth(CommonUtil.dip2px(mContext, 1));
        dashP.setPathEffect(pathEffect);
        for (int i = 0; i < dataList.size(); i++) {
            StrongStreamDto dto = dataList.get(i);
            if (dataMap.containsKey(dto.time)) {
                if (i <= currentIndex) {//实况时间
                    canvas.drawRect(margin2+itemWidth*i-CommonUtil.dip2px(mContext, 3),
                            h/2-CommonUtil.dip2px(mContext, 10),
                            margin2+itemWidth*i+CommonUtil.dip2px(mContext, 3),
                            h/2, lineP);

                    canvas.drawRect(margin2+itemWidth*(i+1)-CommonUtil.dip2px(mContext, 3),
                            h/2-CommonUtil.dip2px(mContext, 10),
                            margin2+itemWidth*(i+1)+CommonUtil.dip2px(mContext, 3),
                            h/2, lineP);
                }else {//预报时间
                    canvas.drawRect(margin2+itemWidth*i-CommonUtil.dip2px(mContext, 3),
                            h/2-CommonUtil.dip2px(mContext, 10),
                            margin2+itemWidth*i+CommonUtil.dip2px(mContext, 3),
                            h/2, dashP);

                    canvas.drawRect(margin2+itemWidth*(i+1)-CommonUtil.dip2px(mContext, 3),
                            h/2-CommonUtil.dip2px(mContext, 10),
                            margin2+itemWidth*(i+1)+CommonUtil.dip2px(mContext, 3),
                            h/2, dashP);
                }
            }
        }

        //绘制seekbar实况实线
        lineP.setColor(0xff00a8ff);
        lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 2));
        canvas.drawLine(margin2, h/2, margin2+itemWidth*currentIndex, h/2, lineP);

        //绘制seekbar预报虚线
        Path path = new Path();
        path.moveTo(margin2+itemWidth*currentIndex, h/2);
        path.lineTo(w-rightMargin, h/2);
        dashP.setColor(0xff00a8ff);
        dashP.setStrokeWidth(CommonUtil.dip2px(mContext, 2));
        PathEffect dashPe = new DashPathEffect(new float[]{10,10,10,10}, 1);
        dashP.setPathEffect(dashPe);
        canvas.drawPath(path, dashP);

        //绘制开始时间
        textP.setColor(Color.WHITE);
        textP.setTextSize(CommonUtil.dip2px(mContext, 12));
        float startTimeWidth = textP.measureText(startTime);
        canvas.drawText(startTime, margin2-startTimeWidth/2, h/2+CommonUtil.dip2px(mContext, 23), textP);

        //绘制结束时间
        textP.setColor(Color.WHITE);
        textP.setTextSize(CommonUtil.dip2px(mContext, 12));
        float entTimeWidth = textP.measureText(endTime);
        canvas.drawText(endTime, w-rightMargin-entTimeWidth/2, h/2+CommonUtil.dip2px(mContext, 23), textP);

        //绘制当前时间
        textP.setColor(Color.WHITE);
        textP.setTextSize(CommonUtil.dip2px(mContext, 12));
        float currentTimeWidth = textP.measureText("现在");
        canvas.drawText("现在", margin2+itemWidth*currentIndex-currentTimeWidth/2, h/2+CommonUtil.dip2px(mContext, 23), textP);

        //绘制播放按钮
        if (STATE == STATE_NONE) {
            canvas.drawBitmap(playBit, leftMargin, h/2-playBit.getHeight()/2, lineP);

            if (isDraggingThumb == false) {
                //线程暂停时，绘制当前时间对应滑块
                lineP.setColor(Color.WHITE);
                lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 12));
                canvas.drawPoint(margin2+itemWidth*currentIndex, h/2, lineP);
                lineP.setColor(0xffffba00);
                lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 8));
                canvas.drawPoint(margin2+itemWidth*currentIndex, h/2, lineP);

                //绘制滑块滑动对应的时间
                if (!TextUtils.isEmpty(currentTime)) {
                    try {
                        textP.setColor(Color.WHITE);
                        textP.setTextSize(CommonUtil.dip2px(mContext, 12));
                        float timeWidth = textP.measureText(sdf2.format(sdf1.parse(currentTime)));
                        canvas.drawText(sdf2.format(sdf1.parse(currentTime)), margin2+itemWidth*currentIndex-timeWidth/2, h/2-CommonUtil.dip2px(mContext, 15), textP);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else if (STATE == STATE_PLAYING){
            canvas.drawBitmap(pauseBit, leftMargin, h/2-playBit.getHeight()/2, lineP);

            if (isDraggingThumb == false) {
                //线程播放时，绘制变动时间对应滑块
                lineP.setColor(Color.WHITE);
                lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 12));
                canvas.drawPoint(margin2+itemWidth*playingIndex, h/2, lineP);
                lineP.setColor(0xffffba00);
                lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 8));
                canvas.drawPoint(margin2+itemWidth*playingIndex, h/2, lineP);

                //绘制滑块滑动对应的时间
                if (!TextUtils.isEmpty(playingTime)) {
                    try {
                        textP.setColor(Color.WHITE);
                        textP.setTextSize(CommonUtil.dip2px(mContext, 12));
                        float timeWidth = textP.measureText(sdf2.format(sdf1.parse(playingTime)));
                        canvas.drawText(sdf2.format(sdf1.parse(playingTime)), margin2+itemWidth*playingIndex-timeWidth/2, h/2-CommonUtil.dip2px(mContext, 15), textP);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else if (STATE == STATE_PAUSE){
            canvas.drawBitmap(playBit, leftMargin, h/2-playBit.getHeight()/2, lineP);

            if (isDraggingThumb == false) {
                //线程播放时，绘制变动时间对应滑块
                lineP.setColor(Color.WHITE);
                lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 12));
                canvas.drawPoint(margin2+itemWidth*playingIndex, h/2, lineP);
                lineP.setColor(0xffffba00);
                lineP.setStrokeWidth(CommonUtil.dip2px(mContext, 8));
                canvas.drawPoint(margin2+itemWidth*playingIndex, h/2, lineP);

                //绘制滑块滑动对应的时间
                if (!TextUtils.isEmpty(playingTime)) {
                    try {
                        textP.setColor(Color.WHITE);
                        textP.setTextSize(CommonUtil.dip2px(mContext, 12));
                        float timeWidth = textP.measureText(sdf2.format(sdf1.parse(playingTime)));
                        canvas.drawText(sdf2.format(sdf1.parse(playingTime)), margin2+itemWidth*playingIndex-timeWidth/2, h/2-CommonUtil.dip2px(mContext, 15), textP);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float lastX = event.getX();
        float lastY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //判断是否点击了播放、暂停按钮
                if (lastX > CommonUtil.dip2px(mContext, 10) && lastX < CommonUtil.dip2px(mContext, 50)
                        && lastY > CommonUtil.dip2px(mContext, 10) && lastY < CommonUtil.dip2px(mContext, 50)) {
                    isDraggingThumb = false;
                    if (STATE == STATE_NONE) {//暂停-->播放状态
                        STATE = STATE_PLAYING;
                    }else if (STATE == STATE_PLAYING){//播放-->暂停状态
                        STATE = STATE_PAUSE;
                    }else if (STATE == STATE_PAUSE) {
                        STATE = STATE_PLAYING;
                    }
                    Intent intent = new Intent();
                    intent.setAction(BROAD_CLICKMENU);
                    intent.putExtra("currentIndex", currentIndex);
                    mContext.sendBroadcast(intent);
                    Log.e("xy", lastX+"--"+lastY);
                }
                break;
            case MotionEvent.ACTION_MOVE:

                break;

            default:
                break;
        }
        return super.onTouchEvent(event);
    }

}
