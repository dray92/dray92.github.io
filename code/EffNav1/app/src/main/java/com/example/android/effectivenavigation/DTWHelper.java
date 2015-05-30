package com.example.android.effectivenavigation;

/**
 * Created by Debosmit on 5/24/15.
 */
public class DTWHelper extends DynamicTimeWarping {
    private DynamicTimeWarping dynamicTimeWarping;

    // initiates a Dynamic Time Warp process for two int arrays, t and r
    public DTWHelper(int[] t, int[] r) {
        super(t, r);
        dynamicTimeWarping = new DynamicTimeWarping(t,r);
        dynamicTimeWarping.dtw();
    }

    public DynamicTimeWarping getDTW() {
        return dynamicTimeWarping;
    }
}
