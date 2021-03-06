package com.example.android.study.commonres.widget.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Scroller;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.android.study.commonres.R;

import java.lang.reflect.Field;

@SuppressLint("AppCompatCustomView")
public class DragFrameLayout extends ImageView {

    public static final int TOUCH_THRESHOLD = 5;

    public int margin_edge;
    int transX = 0;
    int transY = 0;
    private Scroller scroller;
    private float downX, downY;
    private float lastX, lastY;
    private float curX, curY;
    private int lastOffset;
    private int width, height;
    private int viewHeight;
    private int viewWidth;
    private int statusBarHeight;
    private Callback callback;

    public DragFrameLayout(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public DragFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }


    public DragFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        resolveAttr(context, attrs);
        scroller = new Scroller(getContext());
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();

        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;//
        statusBarHeight = getStatusBarHeight();
        if (statusBarHeight == 0) {
            statusBarHeight = (int) (25 * displayMetrics.scaledDensity + 0.5f);
        }
        height -= statusBarHeight;
        //???????????????actionBar?????????
        margin_edge = 10;
    }

    private void resolveAttr(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DragFrameLayout);
        margin_edge = array.getDimensionPixelSize(R.styleable.DragFrameLayout_margin_edge, 10);
        array.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = getWidth();
        viewHeight = getHeight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (scroller.computeScrollOffset()) {
            return super.onTouchEvent(event);
        }
        curX = event.getRawX();
        curY = event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = lastX = event.getRawX();
                downY = lastY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                onMove();
                lastX = curX;
                lastY = curY;
                break;
            case MotionEvent.ACTION_UP:
                onScrollEdge();
                break;
        }
        return true;
    }

    private void onMove() {
        int dx = (int) (curX - lastX);
        int dy = (int) (curY - lastY);

        if (getLeft() + dx < margin_edge) {
            dx = 0;
        } else if (getLeft() + viewWidth + dx > width - margin_edge) {
            dx = 0;
        }

        if (getTop() + dy < margin_edge) {
            dy = 0;
        } else if (getTop() + viewHeight + dy > height - margin_edge) {
            dy = 0;
        }

        setPosition1(dx, dy);
        setPosition4(dx, dy);
    }

    private void setPosition1(int dx, int dy) {
        layout(getLeft() + dx, getTop() + dy, getLeft() + viewWidth + dx, getTop() + viewHeight + dy);
    }

    private void setPosition2(int dx, int dy) {
        offsetLeftAndRight(dx);
        offsetTopAndBottom(dy);
    }

    /**
     * ????????????????????????
     *
     * @param dx
     * @param dy
     */
    private void setPosition3(int dx, int dy) {
        transX += dx;
        transY += dy;
        setTranslationX(transX);
        setTranslationY(transY);
    }

    private void setPosition4(int dx, int dy) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        layoutParams.topMargin = layoutParams.topMargin + dy;
        layoutParams.rightMargin = layoutParams.rightMargin - dx;
        setLayoutParams(layoutParams);
    }

    private void onScrollEdge() {
        if (Math.abs(curX - downX) < TOUCH_THRESHOLD && Math.abs(curY - downY) < TOUCH_THRESHOLD) {
            if (callback != null) {
                callback.onClick();
            }
            return;
        }
        int dx;
        if (getLeft() > (width - getRight())) {
            dx = width - getRight() - margin_edge;
        } else {
            dx = margin_edge - getLeft();
        }
        lastOffset = 0;
        scroller.startScroll(getScrollX(), getScrollY(), dx, 0);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {

            int dx = scroller.getCurrX() - lastOffset;
            lastOffset = scroller.getCurrX();
            setPosition1(dx, 0);
            //setPosition2(dx, 0);
            //setPosition3(dx, 0);
            setPosition4(dx, 0);
            invalidate();
        }
        super.computeScroll();
    }

    public int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onClick();
    }
}
