package com.github.lassana.circletimerwidget;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import com.github.lassana.circletimerwidget.widget.CircleTimerListener;
import com.github.lassana.circletimerwidget.widget.CircleTimerView;

public class MyActivity extends Activity {

    private CircleTimerView mCircleTimerWidget;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mCircleTimerWidget = (CircleTimerView) findViewById(R.id.circle);
        mCircleTimerWidget.setCircleTimerListener(new CircleTimerListener() {
            @Override
            public void onPositionChanged(int newPosition) {
                setTitle(String.valueOf(newPosition));
            }
        });
        setTitle(savedInstanceState == null ? "0" : savedInstanceState.getCharSequence("title"));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
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
        if (item.getItemId() == R.id.action_reset) {
            mCircleTimerWidget.setIndicatorPosition(0);
            setTitle("0");
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
