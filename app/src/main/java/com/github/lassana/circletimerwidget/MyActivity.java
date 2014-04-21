package com.github.lassana.circletimerwidget;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.github.lassana.circletimerwidget.widget.CircleTimerWidget;


public class MyActivity extends Activity {

    private CircleTimerWidget mCircleTimerWidget;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mCircleTimerWidget = (CircleTimerWidget) findViewById(R.id.circle);
        mCircleTimerWidget.setCircleWidgetCallback(new CircleTimerWidget.CircleWidgetCallback() {
            @Override
            public void onZoneChanged(int mIndicatorZone) {
                setTitle(Integer.toString(mIndicatorZone));
            }
        });
        setTitle("0");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            mCircleTimerWidget.setIndicatorZone(0);
            mCircleTimerWidget.invalidate();
            setTitle("0");
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
