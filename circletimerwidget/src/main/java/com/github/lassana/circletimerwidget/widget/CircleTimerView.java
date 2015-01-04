package com.github.lassana.circletimerwidget.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Nikolai Doronin
 * @since 1/2/15.
 */
public class CircleTimerView extends View {

    public static final boolean IS_LAYER_TYPES_AVAILABLE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    /* Drawing attributes */
    private int mStartColor;
    private int mEndColor;
    private int mInnerColor;
    private int mOuterColor;
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
    private Paint mExternalCircleWithShadowPaint;
    private PointF[] mHitchPositionData;
    private int mIndicatorPosition = 0;
    private int mCanvasWidth;
    private int mCanvasHeight;

    /* Callback */
    private CircleTimerListener mCircleTimerListener;

    /* Accessibility fields */
    private ExploreByTouchHelper mExploreByTouchHelper;
    private CharSequence[] mHitchNames;

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
            mStartColor = array.getColor(R.styleable.CircleTimerWidget_start_color, Color.parseColor("#A6A1A4"));
            mEndColor = array.getColor(R.styleable.CircleTimerWidget_end_color, Color.parseColor("#333133"));
            mInnerColor = array.getColor(R.styleable.CircleTimerWidget_inner_color, Color.parseColor("#F2EDF0"));
            mOuterColor = array.getColor(R.styleable.CircleTimerWidget_outer_color, Color.parseColor("#D1CDD0"));
            mCircleLineWidth = array.getDimensionPixelSize(R.styleable.CircleTimerWidget_circle_line_width, 3);
            mHitchSize = array.getDimensionPixelSize(R.styleable.CircleTimerWidget_hitch_size, 45);
            mHitchPadding = array.getDimensionPixelSize(R.styleable.CircleTimerWidget_hitch_padding, 30);
            mHitchCount = array.getInt(R.styleable.CircleTimerWidget_hitch_count, 12);
            mIndicatorSize = array.getDimensionPixelSize(R.styleable.CircleTimerWidget_indicator_size, 50);
            mIndicatorPadding = array.getDimensionPixelSize(R.styleable.CircleTimerWidget_indicator_padding, 15);
            mHitchNames = array.getTextArray(R.styleable.CircleTimerWidget_android_entries);
            if (mHitchNames != null && mHitchNames.length != mHitchCount) {
                throw new IllegalArgumentException("Length of \"android:entries\" array should equals to hitch count!");
            }
        } finally {
            array.recycle();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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

        //if (IS_LAYER_TYPES_AVAILABLE && !isInEditMode()) setLayerType(View.LAYER_TYPE_HARDWARE, null);

        setWillNotDraw(false);

        mExploreByTouchHelper = new CircleTimerTouchHelper(this);
        ViewCompat.setAccessibilityDelegate(this, mExploreByTouchHelper);
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int measuredWidth = getMeasuredWidth();
        final int measuredHeight = getMeasuredHeight();

        if (mExternalCirclePaint == null || mExternalCircleWithShadowPaint == null) {
            mExternalCirclePaint = new Paint();
            final LinearGradient gradient = new LinearGradient(
                    0,
                    0,
                    measuredWidth,
                    measuredHeight,
                    mStartColor,
                    mEndColor,
                    Shader.TileMode.CLAMP);
            mExternalCirclePaint.setShader(gradient);
            mExternalCirclePaint.setStyle(Paint.Style.STROKE);
            mExternalCirclePaint.setStrokeWidth(mCircleLineWidth);

            //mExternalCirclePaint.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL));
            //mExternalCirclePaint.setHinting(Paint.HINTING_ON);
            mExternalCirclePaint.setAntiAlias(true);

            mExternalCircleWithShadowPaint = new Paint(mExternalCirclePaint);
            if (!isInEditMode()) {
                mExternalCircleWithShadowPaint.setShadowLayer(4.0f, 0.0f, 2.0f, Color.BLACK);
                if (IS_LAYER_TYPES_AVAILABLE) setLayerType(LAYER_TYPE_SOFTWARE, mExternalCircleWithShadowPaint);
            }
        }

        if (mInternalCirclePaint == null) {
            mInternalCirclePaint = new Paint();
            Shader gradient = new RadialGradient(
                    measuredWidth / 2,
                    measuredHeight / 2,
                    Math.min(measuredHeight, measuredWidth) / 2,
                    mInnerColor,
                    mOuterColor,
                    Shader.TileMode.CLAMP);
            mInternalCirclePaint.setShader(gradient);
            //mInternalCirclePaint.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL));
            //mInternalCirclePaint.setHinting(Paint.HINTING_ON);
            mInternalCirclePaint.setAntiAlias(true);
        }

        mCanvasWidth = canvas.getWidth();
        mCanvasHeight = canvas.getHeight();
        final float radius = Math.min(mCanvasWidth, mCanvasHeight) / 2;
        final float circleCenterX = mCanvasWidth / 2;
        final float circleCenterY = mCanvasHeight / 2;

        canvas.drawCircle(circleCenterX, circleCenterY, radius - mHitchSize, mExternalCircleWithShadowPaint);
        canvas.drawCircle(circleCenterX, circleCenterY, radius - mHitchSize - mCircleLineWidth / 2, mInternalCirclePaint);

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
            canvas.drawCircle(nextPosition.x, nextPosition.y, (mHitchSize - mHitchPadding) / 2 - mCircleLineWidth / 2, mInternalCirclePaint);
        }

        final double indicatorAngle = Math.toRadians(((float) mIndicatorPosition / mHitchCount * 360.0f) - 90f);
        final float indicatorX = (float) (circleCenterX + (radius - mHitchSize - mCircleLineWidth - mIndicatorSize / 2) * Math.cos(indicatorAngle));
        final float indicatorY = (float) (circleCenterY + (radius - mHitchSize - mCircleLineWidth - mIndicatorSize / 2) * Math.sin(indicatorAngle));
        canvas.drawCircle(indicatorX, indicatorY, (mIndicatorSize - mIndicatorPadding) / 2, mExternalCirclePaint);
        //canvas.drawCircle(indicatorX, indicatorY, (mIndicatorSize - mIndicatorPadding) / 2 - mCircleLineWidth/2, mInternalCirclePaint);
    }

    private void handleMotionEvent(MotionEvent e) {
        int indicatorPosition = calculateZoneIndex(e.getX(), e.getY());
        if (indicatorPosition != mIndicatorPosition) {
            mIndicatorPosition = indicatorPosition;
            invalidate();
            if (mCircleTimerListener != null) mCircleTimerListener.onPositionChanged(mIndicatorPosition);
        }
    }

    protected int calculateZoneIndex(float touchX, float touchY) {
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
        if (newPosition < 0) throw new IllegalArgumentException("New position value cannot be smaller than zero!");
        if (newPosition >= mHitchCount)
            throw new IllegalArgumentException("New position value cannot be larger that count of hitch!");
        mIndicatorPosition = newPosition;
        invalidate();
    }

    public int getHitchCount() {
        return mHitchCount;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected boolean dispatchHoverEvent(@NonNull MotionEvent event) {
        if (mExploreByTouchHelper != null && mExploreByTouchHelper.dispatchHoverEvent(event)) {
            return true;
        }
        return super.dispatchHoverEvent(event);
    }

    protected CharSequence[] getHitchNames() {
        return mHitchNames;
    }
}
