package com.hlj.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hlj.view.wheelview.NumericWheelAdapter;
import com.hlj.view.wheelview.OnWheelScrollListener;
import com.hlj.view.wheelview.WheelView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import shawn.cxwl.com.hlj.R;

/**
 * 预警筛选
 */
public class ShawnWarningStatisticScreenActivity extends BaseActivity implements OnClickListener{

    private Context mContext;
    private TextView tvStartTime,tvEndTime,tvArea,tvContent;
    private WheelView year,month,day,hour,minute;
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
    private SimpleDateFormat sdf5 = new SimpleDateFormat("yyyyMMdd000000", Locale.CHINA);
    private SimpleDateFormat sdf6 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
    private String startTime, endTime,areaName,areaId;
    private RelativeLayout reLayout;
    private boolean startOrEnd = true;//true为start

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shawn_activity_warning_statistic_screen);
        mContext = this;
        initWidget();
        initWheelView();
    }

    private void initWidget() {
        LinearLayout llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("预警筛选");
        tvStartTime = findViewById(R.id.tvStartTime);
        tvStartTime.setOnClickListener(this);
        tvEndTime = findViewById(R.id.tvEndTime);
        tvEndTime.setOnClickListener(this);
        tvArea = findViewById(R.id.tvArea);
        tvArea.setOnClickListener(this);
        TextView tvCheck = findViewById(R.id.tvCheck);
        tvCheck.setOnClickListener(this);
        TextView tvNegtive = findViewById(R.id.tvNegtive);
        tvNegtive.setOnClickListener(this);
        TextView tvPositive = findViewById(R.id.tvPositive);
        tvPositive.setOnClickListener(this);
        tvContent = findViewById(R.id.tvContent);
        reLayout = findViewById(R.id.reLayout);
        reLayout.setOnClickListener(this);

        try {
            startTime = getIntent().getStringExtra("startTime");
            tvStartTime.setText(sdf2.format(sdf6.parse(startTime)));
            endTime = getIntent().getStringExtra("endTime");
            tvEndTime.setText(sdf2.format(sdf6.parse(endTime)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        areaId = getIntent().getStringExtra("areaId");
        areaName = getIntent().getStringExtra("areaName");
        tvArea.setText(areaName);
    }

    private void initWheelView() {
        Calendar c = Calendar.getInstance();
        int curYear = c.get(Calendar.YEAR);
        int curMonth = c.get(Calendar.MONTH) + 1;//通过Calendar算出的月数要+1
        int curDate = c.get(Calendar.DATE);
        int curHour = c.get(Calendar.HOUR_OF_DAY);
        int curMinute = c.get(Calendar.MINUTE);

        year = findViewById(R.id.year);
        NumericWheelAdapter numericWheelAdapter1=new NumericWheelAdapter(this,1950, curYear);
        numericWheelAdapter1.setLabel("年");
        year.setViewAdapter(numericWheelAdapter1);
        year.setCyclic(false);//是否可循环滑动
        year.addScrollingListener(scrollListener);

        month = findViewById(R.id.month);
        NumericWheelAdapter numericWheelAdapter2=new NumericWheelAdapter(this,1, 12, "%02d");
        numericWheelAdapter2.setLabel("月");
        month.setViewAdapter(numericWheelAdapter2);
        month.setCyclic(false);
        month.addScrollingListener(scrollListener);

        day = findViewById(R.id.day);
        initDay(curYear,curMonth);
        day.setCyclic(false);

        hour = findViewById(R.id.hour);
        NumericWheelAdapter numericWheelAdapter3=new NumericWheelAdapter(this,1, 23, "%02d");
        numericWheelAdapter3.setLabel("时");
        hour.setViewAdapter(numericWheelAdapter3);
        hour.setCyclic(false);
        hour.addScrollingListener(scrollListener);

        minute = findViewById(R.id.minute);
        NumericWheelAdapter numericWheelAdapter4=new NumericWheelAdapter(this,1, 59, "%02d");
        numericWheelAdapter4.setLabel("分");
        minute.setViewAdapter(numericWheelAdapter4);
        minute.setCyclic(false);
        minute.addScrollingListener(scrollListener);

        year.setVisibleItems(7);
        month.setVisibleItems(7);
        day.setVisibleItems(7);
        hour.setVisibleItems(7);
        minute.setVisibleItems(7);

        year.setCurrentItem(curYear - 1950);
        month.setCurrentItem(curMonth - 1);
        day.setCurrentItem(curDate - 1);
        hour.setCurrentItem(curHour - 1);
        minute.setCurrentItem(curMinute);
    }

    private OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
        @Override
        public void onScrollingStarted(WheelView wheel) {
        }
        @Override
        public void onScrollingFinished(WheelView wheel) {
            int n_year = year.getCurrentItem() + 1950;//年
            int n_month = month.getCurrentItem() + 1;//月
            initDay(n_year,n_month);
        }
    };

    /**
     */
    private void initDay(int arg1, int arg2) {
        NumericWheelAdapter numericWheelAdapter=new NumericWheelAdapter(this,1, getDay(arg1, arg2), "%02d");
        numericWheelAdapter.setLabel("日");
        day.setViewAdapter(numericWheelAdapter);
    }

    /**
     *
     * @param year
     * @param month
     * @return
     */
    private int getDay(int year, int month) {
        int day;
        boolean flag;
        switch (year % 4) {
            case 0:
                flag = true;
                break;
            default:
                flag = false;
                break;
        }
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                day = 31;
                break;
            case 2:
                day = flag ? 29 : 28;
                break;
            default:
                day = 30;
                break;
        }
        return day;
    }

    /**
     * 时间图层动画
     * @param flag
     * @param view
     */
    private void timeLayoutAnimation(boolean flag, final RelativeLayout view) {
        AnimationSet animationSet = new AnimationSet(true);
        TranslateAnimation animation;
        if (!flag) {
            animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,1f);
        }else {
            animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,0f,
                    Animation.RELATIVE_TO_SELF,1f,
                    Animation.RELATIVE_TO_SELF,0f);
        }
        animation.setDuration(400);
        animationSet.addAnimation(animation);
        animationSet.setFillAfter(true);
        view.startAnimation(animationSet);
        animationSet.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                view.clearAnimation();
            }
        });
    }

    /**
     */
    private void setTextViewValue() {
        String yearStr = String.valueOf(year.getCurrentItem()+1950);
        String monthStr = String.valueOf((month.getCurrentItem() + 1) < 10 ? "0" + (month.getCurrentItem() + 1) : (month.getCurrentItem() + 1));
        String dayStr = String.valueOf(((day.getCurrentItem()+1) < 10) ? "0" + (day.getCurrentItem()+1) : (day.getCurrentItem()+1));
        String hourStr = String.valueOf(((hour.getCurrentItem()+1) < 10) ? "0" + (hour.getCurrentItem()+1) : (hour.getCurrentItem()+1));
        String minuteStr = String.valueOf(((minute.getCurrentItem()+1) < 10) ? "0" + (minute.getCurrentItem()+1) : (minute.getCurrentItem()+1));
        String time = yearStr+"年"+monthStr+"月"+dayStr+"日";

        if (startOrEnd) {
            try {
                tvStartTime.setText(time);
                startTime = sdf5.format(sdf2.parse(time));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else {
            try {
                tvEndTime.setText(time);
                endTime = sdf5.format(sdf2.parse(time));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void bootTimeLayoutAnimation() {
        if (reLayout.getVisibility() == View.GONE) {
            timeLayoutAnimation(true, reLayout);
            reLayout.setVisibility(View.VISIBLE);
        }else {
            timeLayoutAnimation(false, reLayout);
            reLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.tvStartTime:
                startOrEnd = true;
                tvContent.setText(getString(R.string.select_start_time));
                bootTimeLayoutAnimation();

                int y = Integer.valueOf(startTime.substring(0, 4));
                int m = Integer.valueOf(startTime.substring(4, 6));
                int d = Integer.valueOf(startTime.substring(6, 8));
                year.setCurrentItem(y - 1950);
                month.setCurrentItem(m - 1);
                day.setCurrentItem(d - 1);
                break;
            case R.id.tvEndTime:
                startOrEnd = false;
                tvContent.setText(getString(R.string.select_end_time));
                bootTimeLayoutAnimation();

                int y2 = Integer.valueOf(endTime.substring(0, 4));
                int m2 = Integer.valueOf(endTime.substring(4, 6));
                int d2 = Integer.valueOf(endTime.substring(6, 8));
                year.setCurrentItem(y2 - 1950);
                month.setCurrentItem(m2 - 1);
                day.setCurrentItem(d2 - 1);
                break;
            case R.id.tvArea:
                startActivityForResult(new Intent(mContext, ShawnWarningStatisticScreenAreaActivity.class), 1000);
                break;
            case R.id.tvNegtive:
                bootTimeLayoutAnimation();
                break;
            case R.id.tvPositive:
                setTextViewValue();
                bootTimeLayoutAnimation();
                break;
            case R.id.tvCheck:
                try {
                    long lStart = sdf6.parse(startTime).getTime();
                    long lEnd = sdf6.parse(endTime).getTime();
                    if (lStart >= lEnd) {
                        Toast.makeText(mContext, getString(R.string.start_big_end), Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        Intent intent = new Intent();
                        intent.putExtra("startTime", startTime);
                        intent.putExtra("endTime", endTime);
                        intent.putExtra("areaName", areaName);
                        intent.putExtra("areaId", areaId);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1000:
                    areaName = data.getExtras().getString("areaName");
                    tvArea.setText(areaName);
                    areaId = data.getExtras().getString("areaId");
                    break;
            }
        }
    }
}
