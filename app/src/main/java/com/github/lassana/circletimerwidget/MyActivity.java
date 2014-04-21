package com.github.lassana.circletimerwidget;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.github.lassana.circletimerwidget.widget.CircleTimerWidget;

public class MyActivity extends Activity {

    private CircleTimerWidget mCircleTimerWidget;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayShowHomeEnabled(false);
        }

        mCircleTimerWidget = (CircleTimerWidget) findViewById(R.id.circle);
        mCircleTimerWidget.setCircleWidgetCallback(new CircleTimerWidget.CircleWidgetCallback() {
            @Override
            public void onZoneChanged(int mIndicatorZone) {
                setTitle(Integer.toString(mIndicatorZone));
            }
        });
        if ( savedInstanceState == null ) {
            setTitle("0");
        } else {
            setTitle(savedInstanceState.getCharSequence("title"));
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putCharSequence("title", getTitle());
        super.onSaveInstanceState(outState);
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
            setTitle("0");
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
