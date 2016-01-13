package com.sensei.geom;

/**
 * Created by Debosmit on 7/2/15.
 */
import android.app.IntentService;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ErrorService extends IntentService
{
    public ErrorService()
    {
        super("ErrorService");
    }

    private List<String> getNodes(GoogleApiClient paramGoogleApiClient)
    {
        ArrayList localArrayList = new ArrayList();
        Iterator localIterator = ((NodeApi.GetConnectedNodesResult)Wearable.NodeApi.getConnectedNodes(paramGoogleApiClient).await()).getNodes().iterator();
        while (localIterator.hasNext())
            localArrayList.add(((Node)localIterator.next()).getId());
        return localArrayList;
    }

    public void onCreate()
    {
        super.onCreate();
    }

    // ERROR //
    protected void onHandleIntent(android.content.Intent paramIntent)
    {
        // Byte code:
        //   0: getstatic 79	co/vimo/golf/MyApplication:mGoogleApiClient	Lcom/google/android/gms/common/api/GoogleApiClient;
        //   3: astore_2
        //   4: aload_2
        //   5: invokeinterface 84 1 0
        //   10: ifne +10 -> 20
        //   13: aload_2
        //   14: invokeinterface 88 1 0
        //   19: pop
        //   20: aload_0
        //   21: aload_2
        //   22: invokespecial 90	co/vimo/golf/ErrorService:getNodes	(Lcom/google/android/gms/common/api/GoogleApiClient;)Ljava/util/List;
        //   25: astore_3
        //   26: new 92	java/io/ByteArrayOutputStream
        //   29: dup
        //   30: invokespecial 93	java/io/ByteArrayOutputStream:<init>	()V
        //   33: astore 4
        //   35: aconst_null
        //   36: astore 5
        //   38: new 95	java/io/ObjectOutputStream
        //   41: dup
        //   42: aload 4
        //   44: invokespecial 98	java/io/ObjectOutputStream:<init>	(Ljava/io/OutputStream;)V
        //   47: astore 6
        //   49: aload 6
        //   51: aload_1
        //   52: ldc 100
        //   54: invokevirtual 106	android/content/Intent:getSerializableExtra	(Ljava/lang/String;)Ljava/io/Serializable;
        //   57: invokevirtual 110	java/io/ObjectOutputStream:writeObject	(Ljava/lang/Object;)V
        //   60: aload 4
        //   62: invokevirtual 114	java/io/ByteArrayOutputStream:toByteArray	()[B
        //   65: astore 13
        //   67: new 116	com/google/android/gms/wearable/DataMap
        //   70: dup
        //   71: invokespecial 117	com/google/android/gms/wearable/DataMap:<init>	()V
        //   74: astore 14
        //   76: aload 14
        //   78: ldc 119
        //   80: getstatic 125	android/os/Build:BOARD	Ljava/lang/String;
        //   83: invokevirtual 129	com/google/android/gms/wearable/DataMap:putString	(Ljava/lang/String;Ljava/lang/String;)V
        //   86: aload 14
        //   88: ldc 131
        //   90: getstatic 134	android/os/Build:FINGERPRINT	Ljava/lang/String;
        //   93: invokevirtual 129	com/google/android/gms/wearable/DataMap:putString	(Ljava/lang/String;Ljava/lang/String;)V
        //   96: aload 14
        //   98: ldc 136
        //   100: getstatic 139	android/os/Build:MODEL	Ljava/lang/String;
        //   103: invokevirtual 129	com/google/android/gms/wearable/DataMap:putString	(Ljava/lang/String;Ljava/lang/String;)V
        //   106: aload 14
        //   108: ldc 141
        //   110: getstatic 144	android/os/Build:MANUFACTURER	Ljava/lang/String;
        //   113: invokevirtual 129	com/google/android/gms/wearable/DataMap:putString	(Ljava/lang/String;Ljava/lang/String;)V
        //   116: aload 14
        //   118: ldc 146
        //   120: getstatic 149	android/os/Build:PRODUCT	Ljava/lang/String;
        //   123: invokevirtual 129	com/google/android/gms/wearable/DataMap:putString	(Ljava/lang/String;Ljava/lang/String;)V
        //   126: aload 14
        //   128: ldc 100
        //   130: aload 13
        //   132: invokevirtual 153	com/google/android/gms/wearable/DataMap:putByteArray	(Ljava/lang/String;[B)V
        //   135: getstatic 157	com/google/android/gms/wearable/Wearable:MessageApi	Lcom/google/android/gms/wearable/MessageApi;
        //   138: aload_2
        //   139: aload_3
        //   140: iconst_0
        //   141: invokeinterface 161 2 0
        //   146: checkcast 163	java/lang/String
        //   149: ldc 165
        //   151: aload 14
        //   153: invokevirtual 166	com/google/android/gms/wearable/DataMap:toByteArray	()[B
        //   156: invokeinterface 172 5 0
        //   161: pop
        //   162: aload 6
        //   164: ifnull +8 -> 172
        //   167: aload 6
        //   169: invokevirtual 175	java/io/ObjectOutputStream:close	()V
        //   172: aload 4
        //   174: invokevirtual 176	java/io/ByteArrayOutputStream:close	()V
        //   177: return
        //   178: astore 16
        //   180: return
        //   181: astore 7
        //   183: aload 7
        //   185: invokevirtual 179	java/io/IOException:printStackTrace	()V
        //   188: aload 5
        //   190: ifnull +8 -> 198
        //   193: aload 5
        //   195: invokevirtual 175	java/io/ObjectOutputStream:close	()V
        //   198: aload 4
        //   200: invokevirtual 176	java/io/ByteArrayOutputStream:close	()V
        //   203: return
        //   204: astore 11
        //   206: return
        //   207: astore 8
        //   209: aload 5
        //   211: ifnull +8 -> 219
        //   214: aload 5
        //   216: invokevirtual 175	java/io/ObjectOutputStream:close	()V
        //   219: aload 4
        //   221: invokevirtual 176	java/io/ByteArrayOutputStream:close	()V
        //   224: aload 8
        //   226: athrow
        //   227: astore 17
        //   229: goto -57 -> 172
        //   232: astore 12
        //   234: goto -36 -> 198
        //   237: astore 10
        //   239: goto -20 -> 219
        //   242: astore 9
        //   244: goto -20 -> 224
        //   247: astore 8
        //   249: aload 6
        //   251: astore 5
        //   253: goto -44 -> 209
        //   256: astore 7
        //   258: aload 6
        //   260: astore 5
        //   262: goto -79 -> 183
        //
        // Exception table:
        //   from	to	target	type
        //   172	177	178	java/io/IOException
        //   38	49	181	java/io/IOException
        //   198	203	204	java/io/IOException
        //   38	49	207	finally
        //   183	188	207	finally
        //   167	172	227	java/io/IOException
        //   193	198	232	java/io/IOException
        //   214	219	237	java/io/IOException
        //   219	224	242	java/io/IOException
        //   49	162	247	finally
        //   49	162	256	java/io/IOException
    }
}