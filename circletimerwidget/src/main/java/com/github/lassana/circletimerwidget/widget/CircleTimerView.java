package com.github.lassana.circletimerwidget.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Nikolai Doronin
 * @since 1/2/15.
 */
public class CircleTimerView extends View {

    /* Drawing attributes */
    private int mTopColor;
    private int mBottomColor;
    private int mCenterColor;
    private int mEdgeColor;
    private float mCircleLineWidth;
    private float mHitchSize;
    private float mHitchPadding;
    private int mHitchCount;
    private float mIndicatorSize;
    private float mIndicatorPadding;

    /* Touch listener */
    private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            //Log.v(TAG, "onDown; x:" + e.getX() + "; y: " + e.getY());
            handleMotionEvent(e);
            return super.onDown(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //Log.v(TAG, "onScroll; x:" + e2.getX() + "; y: " + e2.getY());
            handleMotionEvent(e2);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    };

    /* Some fields for drawing */
    private Paint mExternalCirclePaint;
    private Paint mInternalCirclePaint;
    private PointF[] mHitchPositionData;
    private int mIndicatorPosition = 0;
    private int mCanvasWidth;
    private int mCanvasHeight;

    /* Callback */
    private CircleTimerListener mCircleTimerListener;

    public CircleTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        initView();
    }

    public CircleTimerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        initView();
    }

    @SuppressWarnings("UnusedDeclaration")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleTimerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttrs(attrs);
        initView();
    }

    private void initAttrs(@NonNull AttributeSet attrs) {
        final TypedArray array = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CircleTimerWidget, 0, 0);
        try {
            mTopColor = array.getColor(R.styleable.CircleTimerWidget_top_color, Color.parseColor("#33CCCCCC"));
            mBottomColor = array.getColor(R.styleable.CircleTimerWidget_bottom_color, Color.parseColor("#CCCCCC"));
            mCenterColor = array.getColor(R.styleable.CircleTimerWidget_center_color, Color.parseColor("#fffbfbfb"));
            mEdgeColor = array.getColor(R.styleable.CircleTimerWidget_edge_color, Color.parseColor("#e8e8e8"));
            mCircleLineWidth = array.getDimensionPixelSize(R.styleable.CircleTimerWidget_circle_line_width, 3);
            mHitchSize = array.getDimensionPixelSize(R.styleable.CircleTimerWidget_hitch_size, 30);
            mHitchPadding = array.getDimensionPixelSize(R.styleable.CircleTimerWidget_hitch_padding, 15);
            mHitchCount = array.getInt(R.styleable.CircleTimerWidget_hitch_count, 12);
            mIndicatorSize = array.getDimensionPixelSize(R.styleable.CircleTimerWidget_indicator_size, 50);
            mIndicatorPadding = array.getDimensionPixelSize(R.styleable.CircleTimerWidget_indicator_padding, 15);
        } finally {
            array.recycle();
        }
    }

    private void initView() {
        if (!isInEditMode()) {
            final GestureDetector gestureDetector = new GestureDetector(getContext(), mGestureListener);
            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });
        }

        setWillNotDraw(false);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        CircleTimerSavedState state = new CircleTimerSavedState(super.onSaveInstanceState());
        state.position = mIndicatorPosition;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof CircleTimerSavedState) {
            CircleTimerSavedState savedState = (CircleTimerSavedState) state;
            super.onRestoreInstanceState(savedState.getSuperState());
            mIndicatorPosition = savedState.position;
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode()) return; // TODO do something special in 'edit mode'

        final int measuredWidth = getMeasuredWidth();
        final int measuredHeight = getMeasuredHeight();

        if (mExternalCirclePaint == null) {
            mExternalCirclePaint = new Paint();
            mExternalCirclePaint.setShader(new LinearGradient(
                    0,
                    0,
                    measuredWidth / 2,
                    measuredHeight / 2,
                    mTopColor,
                    mBottomColor,
                    Shader.TileMode.CLAMP));
        }

        if (mInternalCirclePaint == null) {
            mInternalCirclePaint = new Paint();
            Shader gradient = new RadialGradient(
                    measuredWidth / 2,
                    measuredHeight / 2,
                    Math.min(measuredHeight, measuredWidth) / 2,
                    mCenterColor,
                    mEdgeColor,
                    Shader.TileMode.CLAMP);
            mInternalCirclePaint.setShader(gradient);
        }

        mCanvasWidth = canvas.getWidth();
        mCanvasHeight = canvas.getHeight();
        final float radius = Math.min(mCanvasWidth, mCanvasHeight) / 2;
        final float circleCenterX = mCanvasWidth / 2;
        final float circleCenterY = mCanvasHeight / 2;

        canvas.drawCircle(circleCenterX, circleCenterY, radius - mHitchSize, mExternalCirclePaint);
        canvas.drawCircle(circleCenterX, circleCenterY, radius - mHitchSize - mCircleLineWidth, mInternalCirclePaint);

        if (mHitchPositionData == null) {
            mHitchPositionData = new PointF[mHitchCount];
            double angle;
            for (int i = 0; i < mHitchCount; ++i) {
                angle = Math.toRadians(((float) i / mHitchCount * 360.0f) - 90f);
                mHitchPositionData[i] = new PointF(
                        (float) (circleCenterX + (radius - (mHitchSize / 2)) * Math.cos(angle)),
                        (float) (circleCenterY + (radius - (mHitchSize / 2)) * Math.sin(angle)));
            }
        }

        for (PointF nextPosition : mHitchPositionData) {
            canvas.drawCircle(nextPosition.x, nextPosition.y, (mHitchSize - mHitchPadding) / 2, mExternalCirclePaint);
            canvas.drawCircle(nextPosition.x, nextPosition.y, (mHitchSize - mHitchPadding) / 2 - mCircleLineWidth, mInternalCirclePaint);
        }

        final double indicatorAngle = Math.toRadians(((float) mIndicatorPosition / mHitchCount * 360.0f) - 90f);
        final float indicatorX = (float) (circleCenterX + (radius - mHitchSize - mCircleLineWidth - mIndicatorSize / 2) * Math.cos(indicatorAngle));
        final float indicatorY = (float) (circleCenterY + (radius - mHitchSize - mCircleLineWidth - mIndicatorSize / 2) * Math.sin(indicatorAngle));
        canvas.drawCircle(indicatorX, indicatorY, (mIndicatorSize - mIndicatorPadding) / 2, mExternalCirclePaint);
        canvas.drawCircle(indicatorX, indicatorY, (mIndicatorSize - mIndicatorPadding) / 2 - mCircleLineWidth, mInternalCirclePaint);
    }

    private void handleMotionEvent(MotionEvent e) {
        int indicatorPosition = calculateZoneIndex(e.getX(), e.getY());
        if (indicatorPosition != mIndicatorPosition) {
            mIndicatorPosition = indicatorPosition;
            invalidate();
            if ( mCircleTimerListener != null ) mCircleTimerListener.onPositionChanged(mIndicatorPosition);
        }
    }

    private int calculateZoneIndex(float touchX, float touchY) {
        float lastMinDistance = Float.MAX_VALUE;
        int rvalue = mIndicatorPosition;
        float radius = Math.min(mCanvasWidth, mCanvasHeight) / 2;
        float x = mCanvasWidth / 2;
        float y = mCanvasHeight / 2;
        for (int i = 0; i < mHitchCount; ++i) {
            double angle = Math.toRadians(((float) i / mHitchCount * 360.0f) - 90f);
            float endX = (float) (x + (radius) * Math.cos(angle));
            float endY = (float) (y + (radius) * Math.sin(angle));
            float distance = (float) Math.sqrt((touchX - endX) * (touchX - endX) + (touchY - endY) * (touchY - endY));
            if (distance < lastMinDistance) {
                rvalue = i;
                lastMinDistance = distance;
            }
        }
        return rvalue;
    }

    public void setCircleTimerListener(CircleTimerListener circleTimerListener) {
        mCircleTimerListener = circleTimerListener;
    }

    public int getIndicatorPosition() {
        return mIndicatorPosition;
    }

    public void setIndicatorPosition(int newPosition) {
        if ( newPosition < 0 ) throw new IllegalArgumentException("New position value cannot be smaller than zero!");
        if ( newPosition >= mHitchCount ) throw new IllegalArgumentException("New position value cannot be larger that count of hitch!");
        mIndicatorPosition = newPosition;
        invalidate();
    }
}
