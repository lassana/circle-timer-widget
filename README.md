circle-timer-widget
===================

Round timer widget for Android without any PNG.

![Screenshot](raw/1.png) ![Screenshot](raw/2.png)

## Usage

xml:

    <com.github.lassana.circletimerwidget.widget.CircleTimerView
        android:id="@+id/circle"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:start_color="#FFDEBC"
        app:end_color="#ff8800"
        app:inner_color="#33b5e5"
        app:outer_color="#0099cc"
        app:circle_line_width="3dp"
        app:hitch_size="30dp"
        app:hitch_padding="20dp"
        app:hitch_count="12"
        app:indicator_size="33dp"
        app:indicator_padding="10dp"/>

code:

    circleTimerWidget.setCircleTimerListener(new CircleTimerListener() {
        @Override
        public void onPositionChanged(int newPosition) {
            // ...
        }
    });
---

This project is licensed under [the FreeBSD License](LICENSE).
