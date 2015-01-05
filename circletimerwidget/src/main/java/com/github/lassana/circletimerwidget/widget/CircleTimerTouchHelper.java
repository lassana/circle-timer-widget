package com.github.lassana.circletimerwidget.widget;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * @author Nikolai Doronin {@literal <lassana.nd@gmail.com>}
 * @since 1/4/2015.
 */
class CircleTimerTouchHelper extends ExploreByTouchHelper {

    private CircleTimerView mInstance;

    public CircleTimerTouchHelper(CircleTimerView forView) {
        super(forView);
        mInstance = forView;
    }

    @Override
    protected int getVirtualViewAt(float x, float y) {
        return mInstance.calculateZoneIndex(x, y);
        //return INVALID_ID;
    }

    @Override
    protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
        for (int i = 0; i < mInstance.getHitchCount(); ++i) virtualViewIds.add(i);
    }

    private String createTextForVirtualView(int virtualViewId) {
        return mInstance.getContext().getString(
                R.string.text_indicator_position,
                mInstance.getHitchNames() == null ? String.valueOf(virtualViewId) : mInstance.getHitchNames()[virtualViewId]);
    }

    @Override
    protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
        final String text = createTextForVirtualView(virtualViewId);
        event.setContentDescription(text);
        event.getText().add(text);
    }

    @Override
    protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfoCompat node) {
        final String text = createTextForVirtualView(virtualViewId);
        node.setContentDescription(text);
        node.setText(text);

        node.setBoundsInParent(new Rect(
                mInstance.getCanvasWidth() / 2 - mInstance.getRadius(),
                mInstance.getCanvasHeight() / 2 - mInstance.getRadius(),
                mInstance.getCanvasWidth() / 2 + mInstance.getRadius(),
                mInstance.getCanvasHeight() / 2 + mInstance.getRadius()));

        node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK);
    }

    @Override
    protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
        if ( action == AccessibilityNodeInfo.ACTION_CLICK) {
            invalidateVirtualView(virtualViewId);
            sendEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED);
            return true;
        }
        return false;
    }
}
