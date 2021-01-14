package com.hlj.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * 解决scrollview与listview嵌套
 */

public class ScrollviewListview extends ListView{

    public ScrollviewListview(Context context) {
        super(context);
    }

    public ScrollviewListview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollviewListview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}
