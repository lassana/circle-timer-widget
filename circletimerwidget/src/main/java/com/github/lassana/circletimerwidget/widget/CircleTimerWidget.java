package com.github.lassana.circletimerwidget.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import com.github.lassana.circletimerwidget.widget.R;

/**
 * @author Nikolay
 * @since 4/9/2014.
 */
public class CircleTimerWidget extends View {

    private static final String TAG = "CircleWidget";
    private int mIndicatorZone = 0;

    private float mCircleWidth;
    private float mSeparatorLength;
    private int mSeparatesCount;
    private int mIndicatorResourceId;

    private Bitmap mIndicatorBitmap;
    private Paint mExtCirclePaint;
    private Paint mIntCirclePaint;
    private Paint mSeparatorPaint;
    private Paint mIndicatorPaint;

    private float mCanvasHeight;
    private float mCanvasWidth;

    private CircleWidgetCallback mCircleWidgetCallback;

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

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //Log.v(TAG, "onFling; x:" + e2.getX() + "; y: " + e2.getY());
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            //Log.v(TAG, "onSingleTapUp; x:" + e.getX() + "; y: " + e.getY());
            return super.onSingleTapUp(e);
        }
    };

    public static interface CircleWidgetCallback {
        void onZoneChanged(int mIndicatorZone);
    }

    private static class WidgetSavedState extends Preference.BaseSavedState {
        int currentZone;

        public WidgetSavedState(Parcel source) {
            super(source);
        }

        public WidgetSavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentZone);
        }

        public static final Creator<WidgetSavedState> CREATOR = new Creator<WidgetSavedState>() {
            @Override
            public WidgetSavedState createFromParcel(Parcel source) {
                return new WidgetSavedState(source);
            }

            @Override
            public WidgetSavedState[] newArray(int size) {
                return new WidgetSavedState[size];
            }
        };
    }

    public CircleTimerWidget(Context context) {
        super(context);
        initView();
    }

    public CircleTimerWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        initView();
    }


    public CircleTimerWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        initView();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray array = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CircleTimerWidget, 0, 0);
        try {
            mCircleWidth = array.getDimensionPixelSize(R.styleable.CircleTimerWidget_circle_width, 0);
            mSeparatesCount = array.getInt(R.styleable.CircleTimerWidget_separates_count, 12);
            mSeparatorLength = array.getDimensionPixelSize(R.styleable.CircleTimerWidget_separator_length, 0);
            mIndicatorResourceId = array.getResourceId(R.styleable.CircleTimerWidget_indicator_drawable, 0);
        } finally {
            array.recycle();
        }
    }

    private void initView() {
        if ( ! isInEditMode() ) {
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

        if ( mIndicatorResourceId != 0 ) {
            mIndicatorBitmap = BitmapFactory.decodeResource(getResources(), mIndicatorResourceId);
            mIndicatorBitmap = Bitmap.createScaledBitmap(mIndicatorBitmap, (int)mSeparatorLength*2, (int)mSeparatorLength*2, true);
        }

        mExtCirclePaint = new Paint();
        mExtCirclePaint.setColor(getResources().getColor(android.R.color.holo_blue_dark));
        mIntCirclePaint = new Paint();
        mIntCirclePaint.setColor(getResources().getColor(android.R.color.white));

        mSeparatorPaint = new Paint();
        mSeparatorPaint.setColor(getResources().getColor(android.R.color.holo_blue_dark));
        mSeparatorPaint.setStrokeWidth(1);

        mIndicatorPaint = new Paint();
        mIndicatorPaint.setColor(getResources().getColor(android.R.color.holo_red_dark));
        mIndicatorPaint.setStrokeWidth(3);
    }


    public void setCircleWidgetCallback(CircleWidgetCallback circleWidgetCallback) {
        mCircleWidgetCallback = circleWidgetCallback;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        WidgetSavedState state = new WidgetSavedState(super.onSaveInstanceState());
        state.currentZone = mIndicatorZone;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if ( state instanceof WidgetSavedState ) {
            WidgetSavedState savedState = (WidgetSavedState) state;
            super.onRestoreInstanceState(savedState.getSuperState());
            mIndicatorZone = savedState.currentZone;
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Log.i(TAG, "onDraw");
        super.onDraw(canvas);

        mCanvasWidth = canvas.getWidth();
        mCanvasHeight = canvas.getHeight();

        float radius = Math.min(mCanvasWidth, mCanvasHeight) / 2;

        float x = mCanvasWidth / 2;
        float y = mCanvasHeight / 2;

        //Bitmap circleBackground = Bitmap.createBitmap(mCanvasWidth, mCanvasHeight, Bitmap.C)

        canvas.drawCircle(x, y, radius, mExtCirclePaint);
        canvas.drawCircle(x, y, radius - mCircleWidth, mIntCirclePaint);

        // TODO calculate in #initView
        for ( int i=0; i<mSeparatesCount; ++i ) {
            double angle = Math.toRadians(((float)i / mSeparatesCount * 360.0f) - 90f);
            float startX = (float) (x+(radius -10-mSeparatorLength)*Math.cos(angle));
            float startY = (float) (y+(radius -10-mSeparatorLength)*Math.sin(angle));
            float stopX  = (float) (x+(radius -10)*Math.cos(angle));
            float stopY  = (float) (y+(radius -10)*Math.sin(angle));
            canvas.drawLine(startX, startY, stopX, stopY, mSeparatorPaint);
        }

        double angle = Math.toRadians(((float)mIndicatorZone / mSeparatesCount * 360.0f) - 90f);
        if ( mIndicatorBitmap != null ) {
            float startX = (float) (x+(radius -15-mSeparatorLength*2)*Math.cos(angle));
            float startY = (float) (y+(radius -15-mSeparatorLength*2)*Math.sin(angle));
            canvas.drawBitmap(mIndicatorBitmap, startX-mSeparatorLength, startY-mSeparatorLength, mIndicatorPaint);
        } else {
            float startX = (float) (x+(radius -15-mSeparatorLength*3)*Math.cos(angle));
            float startY = (float) (y+(radius    -15-mSeparatorLength*3)*Math.sin(angle));
            float stopX  = (float) (x+(radius -15-mSeparatorLength)*Math.cos(angle));
            float stopY  = (float) (y+(radius -15-mSeparatorLength)*Math.sin(angle));
            canvas.drawLine(startX, startY, stopX, stopY, mIndicatorPaint);
        }
    }

    private void handleMotionEvent(MotionEvent e) {
        int indicatorZone = calculateZoneIndex(e.getX(), e.getY());
        if ( indicatorZone != mIndicatorZone ) {
            mIndicatorZone = indicatorZone;
            invalidate();
            if ( mCircleWidgetCallback != null ) mCircleWidgetCallback.onZoneChanged(mIndicatorZone);
        }
    }

    private int calculateZoneIndex(float touchX, float touchY) {
        long startTime = SystemClock.elapsedRealtime();
        float lastMinDistance = Float.MAX_VALUE;
        int rvalue = mIndicatorZone;
        float radius = Math.min(mCanvasWidth, mCanvasHeight) / 2;
        float x = mCanvasWidth / 2;
        float y = mCanvasHeight / 2;
        for ( int i=0; i<mSeparatesCount; ++i ) {
            double angle = Math.toRadians(((float) i / mSeparatesCount * 360.0f) - 90f);
            float stopX  = (float) (x + (radius - 10) * Math.cos(angle));
            float stopY  = (float) (y + (radius - 10) * Math.sin(angle));
            float distance = (float)Math.sqrt((touchX-stopX)*(touchX-stopX)+(touchY-stopY)*(touchY-stopY));
            if ( distance < lastMinDistance ) {
                rvalue = i;
                lastMinDistance = distance;
            }
        }
        //Log.v(TAG, "calculateZoneIndex stats: " + (SystemClock.elapsedRealtime() - startTime));
        return rvalue;
    }

    private boolean isPointInsideCircle(float pointX, float pointY, float circleX, float circleY, int circleRadius) {
        return (pointX - circleX)*(pointX - circleX) + (pointY - circleY)*(pointY - circleY) <= circleRadius * circleRadius;
    }

    public void setIndicatorZone(int indicatorZone) {
        mIndicatorZone = indicatorZone;
    }
}
