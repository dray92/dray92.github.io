package com.sensei.geom;

/**
 * Created by Debosmit on 7/2/15.
 */

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class MessageListenerService extends WearableListenerService
{
    private static final String START_ACTIVITY_PATH = "/start";
    private String TAG = getClass().getSimpleName();
    final Handler h = new Handler();
    private Context mContext;
    final Runnable r = new Runnable()
    {
        public void run()
        {
            Intent localIntent = new Intent(MessageListenerService.this.mContext, MainActivity.class);
            localIntent.addFlags(268468224);
            MessageListenerService.this.startActivity(localIntent);
        }
    };

    public void onCreate()
    {
        super.onCreate();
        this.mContext = this;
    }

    public void onMessageReceived(MessageEvent paramMessageEvent)
    {
        if (paramMessageEvent.getPath().equals("/start"))
        {
            Notification localNotification = new NotificationCompat.Builder(this).setSmallIcon(2130837539).setLargeIcon(BitmapFactory.decodeResource(getResources(), 2130837551)).setContentTitle("Teeing up").setContentText("launching VimoGolf").setLocalOnly(true).setDefaults(2).build();
            NotificationManagerCompat.from(this).notify(4, localNotification);
            this.h.postDelayed(this.r, 1000L);
        }
        do
            return;
        while (!paramMessageEvent.getPath().equals("/pingwatch"));
//        Wearable.NodeApi.getConnectedNodes(MyApplication.mGoogleApiClient).setResultCallback(new ResultCallback()
//        {
//
//            public void onResult(NodeApi.GetConnectedNodesResult paramGetConnectedNodesResult)
//            {
//                Iterator localIterator = paramGetConnectedNodesResult.getNodes().iterator();
//                while (localIterator.hasNext())
//                {
//                    Node localNode = (Node)localIterator.next();
//                    Wearable.MessageApi.sendMessage(MyApplication.mGoogleApiClient, localNode.getId(), "/pongback", null).setResultCallback(new ResultCallback()
//                    {
//                        public void onResult(MessageApi.SendMessageResult paramSendMessageResult)
//                        {
//                            Status localStatus = paramSendMessageResult.getStatus();
//                            Log.d(MessageListenerService.this.TAG, "Ping response: " + localStatus.toString());
//                        }
//                    });
//                }
//            }
//        });
    }
}