package com.example.android.effectivenavigation;

/**
 * Created by Debosmit on 5/24/15.
 */
public class DTWHelper extends DynamicTimeWarping{
    private DynamicTimeWarping dynamicTimeWarping;

    // initiates a Dynamic Time Warp process for two double arrays, t and r
    public DTWHelper(double[] t, double[] r) {
        super(t, r);
        dynamicTimeWarping = new DynamicTimeWarping(t,r);
        dynamicTimeWarping.dtw();
    }
}
