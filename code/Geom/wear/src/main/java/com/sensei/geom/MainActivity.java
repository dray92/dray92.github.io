package com.sensei.geom;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;


import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataApi.DataItemResult;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends Activity
        implements SensorEventListener
{
    public static final int INVALID = -1000;
    private String TAG = getClass().getSimpleName();
    private float bw;
    private float bx;
    private float by;
    private float bz;
    final Handler h = new Handler();
    private long lastStateTimestamp;
    private Values mAccel = new Values(null);
    private Sensor mAccelSensor;
    private float mAngle = -1000.0F;
    private TextView mCalibrate;
    private Values mGyro = new Values(null);
    private Sensor mGyroSensor;
    private boolean mHit = false;
    private boolean mHitZone = false;
    private ImageView mImage;
    private long mLastSavedTimestamp;
    private boolean mLeftHandedSwing;
    private boolean mLogging = false;
    private TextView mMessage;
    float[] mRotationMatrix = new float[9];
    private Values mRoto = new Values(null);
    private Sensor mRotoSensor;
    private ArrayList<Data> mSaveArray;
    private State mState = State.HUNTING;
    private ImageView mTargetLine;
    private ImageView mTargetLineBackground;
    private Button mTargetLineButton;
    private TextView mTopMessage;
    private boolean mYNeg = false;
    private boolean mYPos = false;
    private boolean mZNeg = false;
    private boolean mZPos = false;
    private float maxY;
    private float maxZ;
    private float minY;
    private float minZ;
    final Runnable r = new Runnable()
    {
        public void run()
        {
            HomeActivity.access$102(HomeActivity.this, false);
            HomeActivity.this.startListenToSensor();
        }
    };

    private void enterState(State paramState, long paramLong)
    {
        this.mState = paramState;
        this.lastStateTimestamp = paramLong;
        if (paramState == State.HUNTING)
        {
            this.mSaveArray.clear();
            this.mImage.setImageResource(2130837548);
            this.mTopMessage.setText("Address\nball to start");
            this.mMessage.setText("");
            return;
        }
        if (paramState == State.REST)
        {
            this.mImage.setImageResource(2130837547);
            this.mTopMessage.setText("");
            this.mMessage.setText("Recording\n(swing now)");
            return;
        }
        this.mImage.setImageResource(2130837549);
        this.mTopMessage.setText("Swinging");
        this.mMessage.setText("");
    }

    private float getCurrentYAngle()
    {
        float[] arrayOfFloat1 = new float[9];
        float[] arrayOfFloat2 = new float[4];
        arrayOfFloat2[0] = this.mRoto.x;
        arrayOfFloat2[1] = this.mRoto.y;
        arrayOfFloat2[2] = this.mRoto.z;
        arrayOfFloat2[3] = this.mRoto.w;
        SensorManager.getRotationMatrixFromVector(arrayOfFloat1, arrayOfFloat2);
        float f1 = arrayOfFloat1[1];
        float f2 = arrayOfFloat1[4];
        float f3 = (float)Math.toDegrees(Math.atan(f1 / f2));
        if (f2 < 0.0F)
            f3 += 180.0F;
        if (f3 < 0.0F)
            f3 += 360.0F;
        return f3;
    }

    private void startListenToSensor()
    {
        enterState(State.HUNTING, 0L);
        Log.d(this.TAG, "start listening to sensor");
        MyApplication.mSensorManager.registerListener(this, this.mAccelSensor, 0);
        MyApplication.mSensorManager.registerListener(this, this.mGyroSensor, 0);
        MyApplication.mSensorManager.registerListener(this, this.mRotoSensor, 0);
        this.mRoto.timestamp = 0L;
        this.mAccel.timestamp = 0L;
    }

    private void stopListenToSensor()
    {
        Log.d(this.TAG, "stop listening to sensor");
        MyApplication.mSensorManager.unregisterListener(this);
    }

    public void alignClicked(View paramView)
    {
        startListenToSensor();
        if (this.mAngle == -1000.0F)
        {
            this.mAngle = getCurrentYAngle();
            this.mTargetLineButton.setText(2131361793);
            return;
        }
        this.mAngle = -1000.0F;
        this.mTargetLine.setRotation(0.0F);
        this.mTargetLineButton.setText(2131361794);
    }

    public void logSensorData()
    {
        PutDataMapRequest localPutDataMapRequest = PutDataMapRequest.create("/report" + UUID.randomUUID().toString());
        localPutDataMapRequest.getDataMap().putLong("time", System.currentTimeMillis());
        if (this.mAngle != -1000.0F)
            localPutDataMapRequest.getDataMap().putFloat("targetline", this.mAngle);
        localPutDataMapRequest.getDataMap().putFloat("gyromax", this.mGyroSensor.getMaximumRange());
        if ((this.mSaveArray != null) && (this.mSaveArray.size() > 0))
            Log.d(this.TAG, "Reporting swing data");
        try
        {
            ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
            new ObjectOutputStream(localByteArrayOutputStream).writeObject(this.mSaveArray);
            Asset localAsset2 = Asset.createFromBytes(localByteArrayOutputStream.toByteArray());
            localAsset1 = localAsset2;
            localPutDataMapRequest.getDataMap().putAsset("Swing", localAsset1);
            PutDataRequest localPutDataRequest = localPutDataMapRequest.asPutDataRequest();
            Wearable.DataApi.putDataItem(MyApplication.mGoogleApiClient, localPutDataRequest).setResultCallback(new ResultCallback()
            {
                public void onResult(DataApi.DataItemResult paramDataItemResult)
                {
                    if (!paramDataItemResult.getStatus().isSuccess())
                        Log.e(HomeActivity.this.TAG, "ERROR: failed to putDataItem, status code: " + paramDataItemResult.getStatus().getStatusCode());
                }
            });
            return;
        }
        catch (IOException localIOException)
        {
            while (true)
            {
                Log.e(this.TAG, localIOException.toString());
                Asset localAsset1 = null;
            }
        }
    }

    public void onAccuracyChanged(Sensor paramSensor, int paramInt)
    {
        if (((paramInt == 1) || (paramInt == 0)) && (paramSensor.getType() == 11))
        {
            this.mCalibrate.setVisibility(0);
            return;
        }
        this.mCalibrate.setVisibility(4);
    }

    public void onActivityResult(int paramInt1, int paramInt2, Intent paramIntent)
    {
    }

    protected void onCreate(Bundle paramBundle)
    {
        super.onCreate(paramBundle);
        setContentView(2130903040);
        this.mMessage = ((TextView)findViewById(2131492886));
        this.mTopMessage = ((TextView)findViewById(2131492885));
        this.mImage = ((ImageView)findViewById(2131492884));
        this.mTargetLine = ((ImageView)findViewById(2131492888));
        this.mTargetLineBackground = ((ImageView)findViewById(2131492887));
        this.mTargetLineButton = ((Button)findViewById(2131492889));
        this.mCalibrate = ((TextView)findViewById(2131492890));
        getWindow().addFlags(2097280);
        NotificationManagerCompat.from(this).cancel(4);
        MyApplication.mSensorManager = (SensorManager)getSystemService("sensor");
        this.mAccelSensor = MyApplication.mSensorManager.getDefaultSensor(1);
        this.mGyroSensor = MyApplication.mSensorManager.getDefaultSensor(4);
        this.mRotoSensor = MyApplication.mSensorManager.getDefaultSensor(11);
        this.mSaveArray = new ArrayList();
    }

    protected void onPause()
    {
        Log.d(this.TAG, "pause called");
        super.onPause();
    }

    protected void onResume()
    {
        super.onResume();
        startListenToSensor();
    }

    public void onSensorChanged(SensorEvent paramSensorEvent)
    {
        Values localValues = this.mRoto;
        float f1;
        float f2;
        if (paramSensorEvent.sensor.getType() == 11)
        {
            localValues = this.mRoto;
            SensorManager.getRotationMatrixFromVector(this.mRotationMatrix, paramSensorEvent.values);
            this.mRoto.w = paramSensorEvent.values[3];
            localValues.x = paramSensorEvent.values[0];
            localValues.y = paramSensorEvent.values[1];
            localValues.z = paramSensorEvent.values[2];
            localValues.timestamp = paramSensorEvent.timestamp;
            if ((paramSensorEvent.sensor.getType() == 4) && (this.mAccel.timestamp != 0L) && (this.mRoto.timestamp != 0L))
            {
                f1 = this.mRotationMatrix[6];
                f2 = this.mRotationMatrix[7];
                switch (4.$SwitchMap$co$vimo$golf$HomeActivity$State[this.mState.ordinal()])
                {
                    default:
                        Log.d(this.TAG, "Unknown state");
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                }
            }
        }
        while (true)
        {
            if ((this.mState != State.HUNTING) && (paramSensorEvent.timestamp != this.mLastSavedTimestamp))
            {
                Data localData = new Data();
                localData.ax = this.mAccel.x;
                localData.ay = this.mAccel.y;
                localData.az = this.mAccel.z;
                localData.rx = this.mGyro.x;
                localData.ry = this.mGyro.y;
                localData.rz = this.mGyro.z;
                localData.vx = this.mRoto.x;
                localData.vy = this.mRoto.y;
                localData.vz = this.mRoto.z;
                localData.vw = this.mRoto.w;
                localData.timestamp = this.mAccel.timestamp;
                this.mSaveArray.add(localData);
                this.mLastSavedTimestamp = paramSensorEvent.timestamp;
            }
            return;
            if (paramSensorEvent.sensor.getType() == 4)
            {
                localValues = this.mGyro;
                break;
            }
            if (paramSensorEvent.sensor.getType() != 1)
                break;
            localValues = this.mAccel;
            break;
            if ((Math.abs(f1) < 0.2D) && (Math.abs(f2) < 0.2D))
            {
                this.mTargetLine.setVisibility(0);
                this.mTargetLineBackground.setVisibility(0);
                this.mTargetLineButton.setVisibility(0);
                if (this.mAngle != -1000.0F)
                {
                    float f3 = getCurrentYAngle();
                    this.mTargetLine.setRotation(this.mAngle - f3);
                }
            }
            while (true)
            {
                if (f1 >= -0.7D)
                    break label548;
                enterState(State.REST, this.mGyro.timestamp);
                this.mLeftHandedSwing = false;
                break;
                this.mTargetLine.setVisibility(4);
                this.mTargetLineBackground.setVisibility(4);
                this.mTargetLineButton.setVisibility(4);
            }
            label548: if (f1 <= 0.7D)
                continue;
            enterState(State.REST, this.mGyro.timestamp);
            this.mLeftHandedSwing = true;
            continue;
            if (Math.abs(f1) > 0.7D)
            {
                if (this.mSaveArray.size() <= 300)
                    continue;
                this.mSaveArray.remove(0);
                continue;
            }
            enterState(State.CONFIRMING, this.mGyro.timestamp);
            continue;
            if (this.mGyro.timestamp - this.lastStateTimestamp > 2000000000L)
            {
                enterState(State.HUNTING, this.mGyro.timestamp);
                continue;
            }
            if (Math.abs(f1) > 0.7D)
            {
                enterState(State.HUNTING, this.mGyro.timestamp);
                continue;
            }
            if (f1 > 0.0F);
            for (int k = 1; k != this.mLeftHandedSwing; k = 0)
            {
                enterState(State.CONFIRMED, this.mGyro.timestamp);
                break;
            }
            if (this.mGyro.timestamp - this.lastStateTimestamp > 1000000000L)
            {
                enterState(State.HUNTING, this.mGyro.timestamp);
                continue;
            }
            if (f1 < 0.0F);
            for (int j = 1; j != this.mLeftHandedSwing; j = 0)
            {
                this.mHitZone = false;
                this.bx = (-this.mRoto.x);
                this.by = (-this.mRoto.y);
                this.bz = (-this.mRoto.z);
                this.bw = this.mRoto.w;
                enterState(State.IMPACT, this.mGyro.timestamp);
                break;
            }
            if (((f1 < -0.5D) && (!this.mLeftHandedSwing)) || ((f1 > 0.5D) && (this.mLeftHandedSwing)))
                this.mHitZone = true;
            if (this.mGyro.timestamp - this.lastStateTimestamp > 1200000000L)
            {
                enterState(State.HUNTING, this.mGyro.timestamp);
                continue;
            }
            if (f1 > 0.0F);
            for (int i = 1; ; i = 0)
            {
                if (i == this.mLeftHandedSwing)
                    break label1114;
                double d = 2.0D * Math.toDegrees(Math.acos(this.bw * this.mRoto.w - this.bx * this.mRoto.x - this.by * this.mRoto.y - this.bz * this.mRoto.z));
                Log.d(this.TAG, "HITZONE " + this.mHitZone + " Angle " + Double.toString(d));
                if ((this.mHitZone) && (d >= 90.0D) && (d <= 270.0D))
                    break label1116;
                enterState(State.HUNTING, this.mGyro.timestamp);
                break;
            }
            label1114: continue;
            label1116: if (this.mLogging)
                continue;
            this.mLogging = true;
            this.mState = State.HUNTING;
            stopListenToSensor();
            ((Vibrator)getSystemService("vibrator")).vibrate(300L);
            this.mImage.setImageResource(2130837504);
            this.mTopMessage.setText("Captured.\nSee it on Phone.");
            this.mMessage.setText("");
            new Thread(new Runnable()
            {
                public void run()
                {
                    MainActivity.this.logSensorData();
                }
            }).start();
            this.h.postDelayed(this.r, 5000L);
        }
    }

    protected void onStart()
    {
        Log.d(this.TAG, "start called");
        super.onStart();
        startListenToSensor();
    }

    protected void onStop()
    {
        Log.d(this.TAG, "stop called");
        super.onStop();
        stopListenToSensor();
        finish();
    }



    public static enum State {
        HUNTING;
        static State REST;
        static State CONFIRMING;
        static State CONFIRMED;
        static State IMPACT;

        static
        {
            HUNTING("HUNTING", 0);
            REST = new State("REST", 1);
            CONFIRMING = new State("CONFIRMING", 2);
            CONFIRMED = new State("CONFIRMED", 3);
            IMPACT = new State("IMPACT", 4);
            State[] arrayOfState = new State[5];
            arrayOfState[0] = HUNTING;
            arrayOfState[1] = REST;
            arrayOfState[2] = CONFIRMING;
            arrayOfState[3] = CONFIRMED;
            arrayOfState[4] = IMPACT;
            $VALUES = arrayOfState;
        }

        State(String hunting, int i) {
        }
    };

    private class Values
    {
        public long timestamp;
        public float w;
        public float x;
        public float y;
        public float z;

        private Values()
        {
        }
    }
}