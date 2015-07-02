package com.sensei.geom;

/**
 * Created by Debosmit on 7/2/15.
 */

import android.app.Application;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

public class MyApplication extends Application
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    public static GoogleApiClient mGoogleApiClient;
    public static SensorManager mSensorManager;
    private String TAG = getClass().getSimpleName();
    private Thread.UncaughtExceptionHandler mDefaultUEH;
    private Thread.UncaughtExceptionHandler mWearUEH = new Thread.UncaughtExceptionHandler()
    {
        public void uncaughtException(Thread paramThread, Throwable paramThrowable)
        {
            Intent localIntent = new Intent(MyApplication.this, ErrorService.class);
            localIntent.putExtra("exception", paramThrowable);
            MyApplication.this.startService(localIntent);
            MyApplication.this.mDefaultUEH.uncaughtException(paramThread, paramThrowable);
        }
    };

    public void onConnected(Bundle paramBundle)
    {
        Log.d(this.TAG, "Connected");
    }

    public void onConnectionFailed(ConnectionResult paramConnectionResult)
    {
        Log.d(this.TAG, "Failed to connect to Google Api Client with error code " + paramConnectionResult.getErrorCode());
    }

    public void onConnectionSuspended(int paramInt)
    {
        Log.d(this.TAG, "Connection suspended");
    }

    public void onCreate()
    {
        super.onCreate();
        this.mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this.mWearUEH);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
    }

    public void onPeerConnected(Node paramNode)
    {
        Log.d(this.TAG, "onPeerConnected: " + paramNode);
    }

    public void onPeerDisconnected(Node paramNode)
    {
        Log.d(this.TAG, "onPeerDisconnected: " + paramNode);
    }
}