/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.effectivenavigation;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * three primary sections of the app. We use a {@link android.support.v4.app.FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will display the three primary sections of the app, one at a
     * time.
     */
    ViewPager mViewPager;

    BluetoothManager btManager;
    BluetoothAdapter btAdapter; // as the name suggests :)
    BluetoothDevice btDevice;   // as the name suggests :)

    private final static int REQUEST_ENABLE_BT = 1;

    private final static boolean DEBUG_ON = true;

    private static int oldState;        // stores BluetoothProfile

    private Vibrator v;

    private ToneGenerator toneG;

    public static final String tempFilepath = "sensei";
    public static final String tempFilename = "temp.txt";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();

        // if there isn't an adapter, and it is not enabled; requests
        // the user enables Bluetooth if it is currently disabled
        if(btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }


        //updateState(btAdapter.isEnabled() ? STATE_DISCONNECTED : STATE_BLUETOOTH_OFF);
        oldState = btAdapter.isEnabled() ? STATE_DISCONNECTED : STATE_BLUETOOTH_OFF;

        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);   // max volume

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // if filepath exists, delete children
        File dir = new File(Environment.getExternalStorageDirectory(),tempFilepath);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }


    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    private RFduinoService rfduinoService;

    private int STATE_BLUETOOTH_OFF = 1;
    private int STATE_DISCONNECTED = 2;
    private int STATE_CONNECTING = 3;
    private static int STATE_CONNECTED = 4;

    private boolean scanning;
    private boolean scanStarted;
    private boolean streamStarted = false;
    private boolean firstLineToBeIgnored = false;

    TextView plus;
    TextView minus;

    private final BroadcastReceiver rfduinoStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        plus = (TextView) findViewById(R.id.plus);
        minus = (TextView) findViewById(R.id.minus);

        final String action = intent.getAction();

        //upgradeState(STATE_CONNECTED); updateState();
        if (RFduinoService.ACTION_CONNECTED.equals(action)) {
            oldState = (STATE_CONNECTED > oldState) ? STATE_CONNECTED : oldState;

            //downgradeState(STATE_DISCONNECTED); updateState();
        } else if (RFduinoService.ACTION_DISCONNECTED.equals(action)) {
            oldState = (STATE_DISCONNECTED < oldState) ? STATE_DISCONNECTED : oldState;

            // when there is something to be read from RFduino:
        } else if (RFduinoService.ACTION_DATA_AVAILABLE.equals(action)) {

            // This the data read from an RfduinoBLE.send
            byte[] data = intent.getByteArrayExtra(RFduinoService.EXTRA_DATA);
            if (data.length > 0) {
                String dataStr = null;

                // convert data to hexAscii String format
                String dataToHex = HexAsciiHelper.bytesToHex(data);
                try {
                    // convert data to String
                    dataStr = new String(data, "US-ASCII");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if(dataStr.equals("S00000000000")) {
                    streamStarted = true;
                    writeToFile(tempFilename, "S\n");
                    firstLineToBeIgnored = true;
                }
                if(dataStr.equals("R00000000000")) {
                    streamStarted = false;
                    writeToFile(tempFilename, "R\n");
                    toneG.startTone(ToneGenerator.TONE_PROP_ACK, 500);
                }
                if(!(plus == null && minus == null)) {
                    if (dataStr.startsWith("P") && !streamStarted) {
                        plus.setText("" + (Integer.parseInt((String) plus.getText()) + 1));
                    } else if (dataStr.startsWith("N")) {
                        minus.setText("" + (Integer.parseInt((String) minus.getText()) + 1));
                    }
                }

                if(streamStarted) {
                    if(!firstLineToBeIgnored)
                        writeToFile(tempFilename, dataToHex + "\n");
                    else {
                        v.vibrate(1000);    // vibrate for one second
                        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000); // 3 beeps
//                    toneG.startTone(ToneGenerator.TONE_CDMA_HIGH_PBX_S_X4, 1000);   // 2 trings
//                    toneG.startTone(ToneGenerator.TONE_PROP_ACK, 1000);              // 2 ACK beeps

                    }
                    firstLineToBeIgnored = false;
                    if (dataStr.equals("P00000000000") || dataStr.equals("N00000000000")) {
                        writeToFile(tempFilename, dataStr.charAt(0) + "\n");
                    }
                }

                Log.d("Receiving data as hex:", dataToHex);
                Log.d("Receiving data as str:", dataStr);
                Log.d("State of bluetooth:", Integer.toString(oldState));
            }
        }
        Button connectButton = (Button)rootView.findViewById(R.id.demo_collection_button);
        if(oldState == BluetoothProfile.STATE_DISCONNECTED)
            connectButton.setText("Look for Senseiii");
        else
            connectButton.setText("Disconnect from Senseiii");
        }
    };

    private void writeToFile(String filename, String lineToAdd) {
        try {

            File root = new File(Environment.getExternalStorageDirectory(), tempFilepath);

            boolean directoryPresent = false;

            // if the root doesn't exist, create a new directory called _directoryName_
            if (!root.exists())
                directoryPresent = root.mkdirs();

            directoryPresent |= root.exists();

            // a new directory was successfully created, or it already existed;
            if(directoryPresent) {
                File file = new File(root, filename);
                FileWriter writer;

                // if file exists, append to the file
                if (file.exists())
                    writer = new FileWriter(file, true);

                    // if file doesn't exist, create a new file
                else
                    writer = new FileWriter(file);

                writer.append(lineToAdd);
                writer.flush();
                writer.close();
            } else {
                // directory was not present and could not be created
                Toast.makeText(this, "Directory " + tempFilepath + " not present",
                        Toast.LENGTH_SHORT).show();
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment;
            Bundle args = new Bundle();
            switch (i) {
                case 0:
                    // The first section of the app is the most interesting -- it offers
                    // a launchpad into the other demonstrations in this example application.
                    return new LaunchpadSectionFragment();

                case 1:
                    fragment = new DataSectionFragment();
                    args.putInt(DataSectionFragment.ARG_SECTION_NUMBER, i + 1);
                    fragment.setArguments(args);

                    return fragment;
                case 2:
                    fragment = new ConsistencySectionFragment();

                    args.putInt(ConsistencySectionFragment.ARG_SECTION_NUMBER, i + 1);
                    fragment.setArguments(args);
                    return fragment;
                case 3:
                    // The other sections of the app are dummy placeholders.
                    fragment = new ScorekeeperSectionFragment();
                    args.putInt(ScorekeeperSectionFragment.ARG_SECTION_NUMBER, i + 1);
                    fragment.setArguments(args);
                    return fragment;

                default:
                    // The other sections of the app are dummy placeholders.
                    fragment = new DummySectionFragment();
                    args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
                    fragment.setArguments(args);
                    return fragment;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // access string tab from strings.xml
            // getResources gives a gateway to the values folder, getString...

            // mess[] is an array of names of tabs
            // 0 -> tab1; 1 -> tab2; ...
            String mess[] = {getResources().getString(R.string.tab1),
                    getResources().getString(R.string.tab2),
                    getResources().getString(R.string.tab3),
                    getResources().getString(R.string.tab4)};

            return mess[position];
        }
    }
    static View rootView;
    /**
     * A fragment that launches other parts of the demo application.
     */
    @SuppressLint("ValidFragment")
    public class LaunchpadSectionFragment extends Fragment implements BluetoothAdapter.LeScanCallback {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_section_launchpad, container, false);

            // FOR DEBUGGING
            final TextView t = (TextView) rootView.findViewById(R.id.test);
            final Button b = (Button) rootView.findViewById(R.id.demo_collection_button);
            if(!DEBUG_ON) {
                t.setText("");
            }

            if(DEBUG_ON) {
                t.setText("Tester Section");
                int val = BluetoothProfile.STATE_CONNECTED;
                if(BluetoothProfile.STATE_CONNECTED == STATE_CONNECTED || oldState == STATE_CONNECTED) {
                    if(DEBUG_ON)
                        t.setText("Bluetooth Connected");
                    b.setText("Disconnect from Senseiii");
                } else {
                    b.setText("Look for Senseiii");
                }
            }


            // Demonstration of a collection-browsing activity.
            b.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onClick(View view) {
                    String demo_collection_button_string = (String) b.getText();
                    boolean rfduinoBindServiceReturn;

                    // looking for senseiii to connect to
                    if(demo_collection_button_string.startsWith("Look") ) {
                        // enable bluetooth adapter
                        btAdapter.enable();

                        if (DEBUG_ON)
                            t.setText("Bluetooth Adapter Enabled");

                        // start bluetooth low energy scan
                        btAdapter.startLeScan(new UUID[]{RFduinoService.UUID_SERVICE}, leScanCallBack);

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                        if (DEBUG_ON)
                            t.setText("Bluetooth Scan Started");

                        Intent rfduinoIntent = new Intent(MainActivity.this, RFduinoService.class);
                        rfduinoBindServiceReturn = bindService(rfduinoIntent,
                                rfduinoServiceConnection, BIND_AUTO_CREATE);

                        if (DEBUG_ON)
                            t.setText("Attempting to connect to RFduino");
                        if(BluetoothProfile.STATE_CONNECTED == STATE_CONNECTED || oldState == STATE_CONNECTED) {
                            b.setText("Disconnect from Senseiii");
                        }

                    }
                    //
                    else if(demo_collection_button_string.startsWith("Disconnect")) {
                        unbindService(rfduinoServiceConnection);

                        if (DEBUG_ON)
                            t.setText("Disconnected from RFduino");

                        try {
                            b.setText("Disconnecting...");
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        b.setText("Look for Senseiii");

                        // disable bluetooth adapter
                        btAdapter.disable();
                    }

                }
            });

            // Demonstration of navigating to external activities.
            // BUTTON CURRENTLY COMMENTED OUT
            /*rootView.findViewById(R.id.demo_external_activity)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Create an intent that asks the user to pick a photo, but using
                            // FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET, ensures that relaunching
                            // the application from the device home screen does not return
                            // to the external activity.
                            Intent externalActivityIntent = new Intent(Intent.ACTION_PICK);
                            externalActivityIntent.setType("image*//*");
                            externalActivityIntent.addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            startActivity(externalActivityIntent);
                        }
                    });*/

            registerReceiver(scanModeReceiver, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
            registerReceiver(btStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            registerReceiver(rfduinoStateReceiver, RFduinoService.getIntentFilter());




            return rootView;
        }

        private BluetoothAdapter.LeScanCallback leScanCallBack = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                btAdapter.stopLeScan(this);
                btDevice = device;

                final Button b = (Button) rootView.findViewById(R.id.demo_collection_button);

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(DEBUG_ON) {
                            final TextView t = (TextView) rootView.findViewById(R.id.test);
                            //t.setText(BluetoothHelper.getDeviceInfoText(btDevice, rssi, scanRecord));
                            int val = BluetoothProfile.STATE_CONNECTED;
                            t.setText("Connected to: \"" +
                                    BluetoothHelper.getDeviceName(btDevice, rssi, scanRecord) + "\"");
                            b.setText("Disconnect from Senseiii");
                        }
                    }
                });
            }
        };

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, final byte[] scanRecord) {
            btAdapter.stopLeScan(this);
            btDevice = device;

            if(DEBUG_ON) {
                final TextView t = (TextView) rootView.findViewById(R.id.test);
                t.setText(BluetoothHelper.getDeviceInfoText(btDevice, rssi, scanRecord));
            }
        }

        private final ServiceConnection rfduinoServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                rfduinoService = ((RFduinoService.LocalBinder) service).getService();
                boolean btDeviceClear = (btDevice == null);
                while(btDeviceClear) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    btDeviceClear = (btDevice == null);
                }
                if (rfduinoService.initialize()) {

                    if (rfduinoService.connect(btDevice.getAddress())) {
                        oldState = (STATE_CONNECTING > oldState) ? STATE_CONNECTING : oldState;//upgradeState(STATE_CONNECTING);
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                rfduinoService = null;
                oldState = (STATE_DISCONNECTED < oldState) ? STATE_DISCONNECTED : oldState;//downgradeState(STATE_DISCONNECTED);
            }
        };

        private final BroadcastReceiver scanModeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                scanning = (btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_NONE);
                scanStarted &= scanning;
                //updateUi();
            }
        };

        private final BroadcastReceiver btStateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);

                //upgradeState(STATE_DISCONNECTED); updateState(STATE_DISCONNECTED);
                if (state == BluetoothAdapter.STATE_ON)
                    oldState = (STATE_DISCONNECTED > oldState) ? STATE_DISCONNECTED : oldState;

                    //downgradeState(STATE_BLUETOOTH_OFF); updateState(STATE_BLUETOOTH_OFF);
                else if (state == BluetoothAdapter.STATE_OFF)
                    oldState = (STATE_BLUETOOTH_OFF < oldState) ? STATE_BLUETOOTH_OFF : oldState;
            }
        };
//        public void onStart() {
//            registerReceiver(scanModeReceiver, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
//            registerReceiver(btStateReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
//            registerReceiver(btStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
//            registerReceiver(rfduinoStateReceiver, RFduinoService.getIntentFilter());
//
//            //updateState(btAdapter.isEnabled() ? STATE_DISCONNECTED : STATE_BLUETOOTH_OFF);
//            oldState = btAdapter.isEnabled() ? STATE_DISCONNECTED : STATE_BLUETOOTH_OFF;
//        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_dummy, container, false);
            Bundle args = getArguments();
            ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                    getString(R.string.dummy_section_text, args.getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * Adapted from dummy fragment representing a section of the app, but that
     * simply displays dummy text.
     * Converted to: Data section -> collects data, stores it...
     */
    public static class DataSectionFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "section_number";
        private View myRootview;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_data, container, false);
            Bundle args = getArguments();
            ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                    getString(R.string.data_section_text, args.getInt(ARG_SECTION_NUMBER)));
            myRootview = rootView;
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            try {
                drawGraph();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void drawGraph() {
            GraphView graph = (GraphView) myRootview.findViewById(R.id.graph);
            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
            });
            graph.addSeries(series);

            /*DataPoint[] myArr = new DataPoint[15];
            for(int i = 0 ; i < 15 ; i++)
                myArr[i] = new DataPoint(i, (int)(10*Math.random()));

            LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>(myArr);
            graph.addSeries(series2);*/
            super.onStart();
        }
    }

    /**
     * Adapted from dummy fragment representing a section of the app, but that
     * simply displays dummy text.
     * Converted to: Consistency section -> opens data file, scans segments, gives a score...
     */
    public static class ConsistencySectionFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "section_number";
        private View myRootView;
        private TextView t;
        private final int CONSISTENCY_THRESHOLD = 10000000;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_consistency, container, false);
            Bundle args = getArguments();
            myRootView = rootView;
            t = (TextView) rootView.findViewById(R.id.test);
            return rootView;
        }


        @Override
        public void onStart() {
            super.onStart();
            try {
                getScore();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void getScore() throws IOException {
            // open file
            TempFileHandler fileHandler = new TempFileHandler(tempFilepath, tempFilename);

            runDTW(fileHandler);
        }

        private void runDTW(TempFileHandler fileHandler) {
            Motion[] mySwings = fileHandler.getMotions();

            t.setText("Number of motions: " + mySwings.length);

            int consistencyScore = 0;

            for (int i = 0; i < mySwings.length - 1; i++) {
                printMotionComparerToDebugger(mySwings[i], mySwings[i + 1]);
                consistencyScore += calculateConsistencyHelper(mySwings, i, i + 1);
            }
            int consistencyMax = (mySwings.length - 1) * 3;

            double calculatedScore = (100.0 * consistencyScore) / consistencyMax;

            t.setText(t.getText() + "||" + "Score: " + calculatedScore);
        }

        private void printMotionComparerToDebugger(Motion m1, Motion m2) {
            Log.d("Motion 1, Accel X:", Arrays.toString(m1.getAccelX()));
            Log.d("Motion 2, Accel X:", Arrays.toString(m2.getAccelX()));
            Log.d("Motion 1, Accel Y:", Arrays.toString(m1.getAccelY()));
            Log.d("Motion 2, Accel Y:", Arrays.toString(m2.getAccelY()));
            Log.d("Motion 1, Accel Z:", Arrays.toString(m1.getAccelZ()));
            Log.d("Motion 2, Accel Z:", Arrays.toString(m2.getAccelZ()));
        }

        private int calculateConsistencyHelper(Motion[] mySwings, int i, int i1) {
            Motion swing1 = mySwings[i];
            Motion swing2 = mySwings[i1];

            int consistency = 0;

            DTWHelper[] dtw = new DTWHelper[3];
            dtw[0] = new DTWHelper(swing1.getAccelX(), swing2.getAccelX());
            dtw[1] = new DTWHelper(swing1.getAccelY(), swing2.getAccelY());
            dtw[2] = new DTWHelper(swing1.getAccelZ(), swing2.getAccelZ());

            for(DTWHelper singleDTW: dtw) {
                DynamicTimeWarping myDtw = singleDTW.getDTW();
                Log.d("print cost path", Double.toString(myDtw.getPathCost()));
                if (myDtw.getPathCost() < CONSISTENCY_THRESHOLD) {
                    consistency++;
                }
            }
            return consistency;
        }
    }

    /**
     * Adapted from dummy fragment representing a section of the app, but that
     * simply displays dummy text.
     * Converted to: Scorekeeper section -> Keeps score...
     */
    public static class ScorekeeperSectionFragment extends Fragment /*implements DialogInterface.OnClickListener*//* implements PopupMenu.OnMenuItemClickListener */{

        public static final String ARG_SECTION_NUMBER = "section_number";

        boolean valueUpdated = false;
        TextView t;
        View myRootView;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_scorekeeper, container, false);
            Bundle args = getArguments();

            myRootView = rootView;

            // prints "This is the Scorekeeper Section"
//            ((TextView) rootView.findViewById(android.R.id.text1)).setText(
//                    getString(R.string.scorekeeper_section_text, args.getInt(ARG_SECTION_NUMBER)));

            final TextView plus = (TextView)rootView.findViewById(R.id.plus);
            final TextView minus = (TextView)rootView.findViewById(R.id.minus);
            final TextView reset = (TextView)rootView.findViewById(R.id.reset);
            t = (TextView)rootView.findViewById(R.id.test);
            final TextView popupMenu = (TextView)rootView.findViewById(R.id.popup_scorekeeper);
            final int[] oldVal = {Integer.parseInt((String) plus.getText())};

            final int incrementDecrementAmount = 1;

            popupMenu.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

//                    // 1. Instantiate an AlertDialog.Builder with its constructor
//                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//
//                    // 2. Chain together various setter methods to set the dialog characteristics
//                    builder.setMessage("popup")
//                            .setTitle("popup_title");
//
//                    // 3. Get the AlertDialog from create()
//                    AlertDialog dialog = builder.create();

                PopupMenu popup = new PopupMenu(getActivity().getApplicationContext(), v);

                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.popup_scorekeeper, popup.getMenu());
                    setHasOptionsMenu(true);
                // debug
                if(DEBUG_ON)
                    t.setText("Pop up");

                popup.show();
                }
            });

            reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                minus.setText("0");
                plus.setText("0");
                }
            });

            /* onclick on plus and minus buttons increments value */
            plus.setOnClickListener(new View.OnClickListener() {

                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onClick(View v) {
                int plusVal = Integer.parseInt((String) plus.getText());
                plus.setText("" + (plusVal + incrementDecrementAmount));
                }

            });

            minus.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onClick(View v) {
                    int minusVal = Integer.parseInt((String) minus.getText());
                    minus.setText("" + (minusVal + incrementDecrementAmount));
                }

            });

            /* longclick on plus and minus buttons decrement value */
            plus.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
//              if(!valueUpdated)
                plus.setText("" + (Integer.parseInt((String) plus.getText())
                        - incrementDecrementAmount));

                if (oldVal[0] != Integer.parseInt((String) plus.getText())) {
                    oldVal[0] = Integer.parseInt((String) plus.getText());
                    valueUpdated = !valueUpdated;
                }
                return true;
                }

            });
            minus.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
//              if(!valueUpdated)
                minus.setText("" + (Integer.parseInt((String) minus.getText())
                        - incrementDecrementAmount));

                if(oldVal[0] != Integer.parseInt((String) minus.getText())) {
                    oldVal[0] = Integer.parseInt((String) minus.getText());
                    valueUpdated = !valueUpdated;
                }
                return true;
                }

            });
            return rootView;

        }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle item selection

            if(DEBUG_ON)
                t.setText("Pop up option chosen");

            switch (item.getItemId()) {
                case R.id.popup_scorekeeper_tennis:
//                    popup_scorekeeper_tennis();
                    return true;
                case R.id.popup_scorekeeper_basketball:
//                    popup_scorekeeper_basketball();
                    return true;
                case R.id.popup_scorekeeper_golf:
//                    popup_scorekeeper_golf();
                    return true;
                case R.id.popup_scorekeeper_squash:
//                    popup_scorekeeper_squash();
                    return true;
                case R.id.popup_scorekeeper_badminton:
//                    popup_scorekeeper_badminton();
                    return true;
                case R.id.popup_scorekeeper_racquetball:
//                    popup_scorekeeper_racquetball();
                    return true;
                case R.id.popup_scorekeeper_tabletennis:
//                    popup_scorekeeper_tabletennis();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }


    }
}
