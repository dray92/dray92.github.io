package com.example.android.effectivenavigation;

/**
 * Created by Debosmit on 5/24/15.
 */
import java.util.Arrays;

public class DynamicTimeWarping {
    private int[] t;
    private int[] r;
    private double pathCost; // unnormalized distance between t and r
    private double[][] accumulatedDistanceMatrix;
    private double[][] d; // to verify and compare values with MATLAB only
    private int k; // normalization factor (should use this to normalize our data!)
    private double[][] w; // optimal path

    // constructs DynamicTimeWarping Object given int array t and r
    public DynamicTimeWarping(int[] t, int[] r) {
        this.t = t;
        this.r = r;
        pathCost = 0.0;
        accumulatedDistanceMatrix = new double[t.length][r.length];
        d = new double[t.length][r.length];
        k = 1;
        w = new double[][] {{t.length-1, r.length-1}};
    }

    // processes the double array t and r to compute dtw values
    // throws IllegalArgumentException if length of t or r is less than 1
    public void dtw() {

        if (t.length < 1 || r.length < 1) {
            throw new IllegalArgumentException("data length is less than 1");
        }

        /**
         *  if in Matlab, nested for loops can be replaced with:
         *  d=(repmat(t(:),1,M)-repmat(r(:)',N,1)).^2;
         *  this replaces the nested for loops from above Thanks Georg Schmitz
         */
        for(int i = 0  ; i < t.length; i++)
            for(int j = 0  ; j < r.length ; j++)
                d[i][j]= (double) (t[i]-r[j])*(t[i]-r[j]);

        accumulatedDistanceMatrix[0][0] = d[0][0];

        for(int i = 1 ; i < t.length ; i++)
            accumulatedDistanceMatrix[i][0]=d[i][0]+accumulatedDistanceMatrix[i-1][0];

        for(int i = 1 ; i < r.length ; i++)
            accumulatedDistanceMatrix[0][i]=d[0][i]+accumulatedDistanceMatrix[0][i-1];

        for(int n = 1 ; n < t.length ; n++)
            for(int m = 1 ; m < r.length ; m++)
                accumulatedDistanceMatrix[n][m] = d[n][m] +
                        min( accumulatedDistanceMatrix[n-1][m],
                                accumulatedDistanceMatrix[n-1][m-1],
                                accumulatedDistanceMatrix[n][m-1] );

        pathCost =  accumulatedDistanceMatrix[t.length-1][r.length-1]; // unnormalized distance between t and r

       /* // THIS IS CRASHING!!!*/
//         supplementary dtw (not used in recent stable build of Senseiii)
//        int n = t.length-1;
//        int m = r.length-1;
//        double minValue;
//        int minIndex;
//        double[][] oldW; // used for updating & appending arrays to 2d array w
//
//        while ((n+m)!=0) {
//            if (n == 0)
//                m = m-1;
//            else if (m == 0) {
//                n = n-1;
//            } else {
//
//                minValue = min(accumulatedDistanceMatrix[n-1][m],
//                        accumulatedDistanceMatrix[n][m-1],
//                        accumulatedDistanceMatrix[n-1][m-1]);
//
//                minIndex = minIndex(accumulatedDistanceMatrix[n-1][m],
//                        accumulatedDistanceMatrix[n][m-1],
//                        accumulatedDistanceMatrix[n-1][m-1]);
//
//                switch (minIndex) {
//                    case 1:
//                        n = n-1;
//                    case 2:
//                        m = m-1;
//                    case 3:
//                        n = n-1;
//                        m = m-1;
//                    default:
//                }
//            }
//
//            k = k+1; // we need to use this value to normalize data
//
//            //MATLAB to Java: w = cat(1,w,[n,m]); START
//            oldW = w;
//            w = Arrays.copyOf(oldW, oldW.length + 1);
//            w[oldW.length] = new double[] {n,m};
//            //MATLAB to Java: w = cat(1,w,[n,m]); END
//
//        }
    }

    // returns minimum of three double values
    private double min(double a, double b, double c) { return Math.min(Math.min(a, b), c); }

    // returns the index of the minimum of three double values
    // Note: This does not use 0-indexing
    private int minIndex(double a, double b, double c) {
        double min = Math.min(a, b);
        min = Math.min(min, c);
        if (Double.compare(min,  a) == 0)
            return 1;
        else if (Double.compare(min,  b) == 0)
            return 2;
        return 3;
    }

    // returns small d
    // NOTE: To compare values to MATLAB for verification of dtw
    public double[][] getSmallD() { return d; }

    // returns the path cost
    public double getPathCost() { return pathCost; }

    // returns the accumulated distance matrix
    public double[][] getAccumulatedDistanceMatrix() { return accumulatedDistanceMatrix; }

    // returns an int containing the normalization factor
    public int getK() { return k; }

    // returns a double array containing the optimal path
    public double[][] getW() { return w; }

}
