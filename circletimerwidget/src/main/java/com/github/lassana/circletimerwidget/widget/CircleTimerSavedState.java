package com.github.lassana.circletimerwidget.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.support.annotation.NonNull;

/**
* @author Nikolai Doronin
* @since 1/2/15.
*/
class CircleTimerSavedState extends Preference.BaseSavedState {
    int position;

    public CircleTimerSavedState(Parcel source) {
        super(source);
    }

    public CircleTimerSavedState(Parcelable superState) {
        super(superState);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(position);
    }

    public static final Creator<CircleTimerSavedState> CREATOR = new Creator<CircleTimerSavedState>() {
        @Override
        public CircleTimerSavedState createFromParcel(Parcel source) {
            return new CircleTimerSavedState(source);
        }

        @Override
        public CircleTimerSavedState[] newArray(int size) {
            return new CircleTimerSavedState[size];
        }
    };

}
