package com.hlj.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 解决scrollview与gridview嵌套
 */

public class ScrollviewGridview extends GridView{

    public ScrollviewGridview(Context context) {
        super(context);
    }

    public ScrollviewGridview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollviewGridview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}
