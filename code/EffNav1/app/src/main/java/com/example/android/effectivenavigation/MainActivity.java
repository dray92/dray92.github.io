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
import android.graphics.Color;
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

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.UUID;

import static java.util.Calendar.*;

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

    private final static boolean DEBUG_ON = false;

    private static int oldState;        // stores BluetoothProfile

    private Vibrator v;

    private ToneGenerator toneG;

    private static int systemTime = 0;

    private int startMotionSeconds;
    private boolean motionBeepFLag;
    private static final double SECONDS_TO_MOTION = 2.25;

    private static final int BUFFER_CONSISTENCY_BOUND = 20;

    public static final String tempFilepath = "sensei";
    public static final String tempFilename = "temp.txt";

    public static TempFileHandler fileHandler;
    ScorekeeperHelper myScorekeeper;
    private final int DEFAULT_SCORE_TO_WIN = 21;
    private final int DEFAULT_SCORE_TO_WIN_BASKETBALL = 15;
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
        systemTime = getInstance().get(SECOND);

        myScorekeeper = new ScorekeeperHelper();
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

    TextView player1;
    TextView player2;

    private final BroadcastReceiver rfduinoStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        player1 = (TextView) findViewById(R.id.plus);
        player2 = (TextView) findViewById(R.id.minus);

        final String action = intent.getAction();

        //upgradeState(STATE_CONNECTED); updateState();
        if (RFduinoService.ACTION_CONNECTED.equals(action)) {
            oldState = (STATE_CONNECTED > oldState) ? STATE_CONNECTED : oldState;

            //downgradeState(STATE_DISCONNECTED); updateState();
        } else if (RFduinoService.ACTION_DISCONNECTED.equals(action)) {
            oldState = (STATE_DISCONNECTED < oldState) ? STATE_DISCONNECTED : oldState;

            // when there is something to be read from RFduino:
        } else if (RFduinoService.ACTION_DATA_AVAILABLE.equals(action)) {
            systemTime = getInstance().get(SECOND);
            Log.d("System Time: ", systemTime + "");
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
                if(!(player1 == null && player2 == null)) {
                    if (dataStr.equals("P00000000000") && !streamStarted) {
                        myScorekeeper.incrementScore(1, player1, player2, getApplicationContext());
                        Log.d("P pressed", "asdf");

                    } else if (dataStr.equals("N00000000000")) {
                        myScorekeeper.incrementScore(2, player1, player2, getApplicationContext());
                        Log.d("N pressed", "asdf");

                    }
                }

                if(streamStarted) {
                    if(!firstLineToBeIgnored) {
                        writeToFile(tempFilename, dataToHex + "\n");
                        if((getInstance().get(SECOND) - startMotionSeconds > SECONDS_TO_MOTION) && motionBeepFLag) {
                             toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000);
                            motionBeepFLag = false;
                        }
                    }
                    else {
                        v.vibrate(1000);    // vibrate for one second
                        toneG.startTone(ToneGenerator.TONE_CDMA_ANSWER, 1000); // 3 beeps
                        startMotionSeconds = getInstance().get(SECOND);
                        motionBeepFLag = true;
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
        else if(oldState == BluetoothProfile.STATE_CONNECTED)
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

            if(BluetoothProfile.STATE_CONNECTED == STATE_CONNECTED || oldState == STATE_CONNECTED) {
                b.setText("Disconnect from Senseiii");
            } else {
                b.setText("Look for Senseiii");
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
                    }
                        b.setText("Disconnect from Senseiii");
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
    }

//    /**
//     * A dummy fragment representing a section of the app, but that simply displays dummy text.
//     */
//    public static class DummySectionFragment extends Fragment {
//
//        public static final String ARG_SECTION_NUMBER = "section_number";
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_section_dummy, container, false);
//            Bundle args = getArguments();
//            ((TextView) rootView.findViewById(android.R.id.text1)).setText(
//                    getString(R.string.dummy_section_text, args.getInt(ARG_SECTION_NUMBER)));
//            return rootView;
//        }
//    }

    /**
     * Adapted from dummy fragment representing a section of the app, but that
     * simply displays dummy text.
     * Converted to: Data section -> collects data, stores it...
     */
    public static class DataSectionFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "section_number";
        private View myRootview;
        private GraphView[] graphs;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_data, container, false);
            Bundle args = getArguments();

            myRootview = rootView;

            graphs = new GraphView[]{(GraphView) myRootview.findViewById(R.id.graph1),
                    (GraphView) myRootview.findViewById(R.id.graph2)};

            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();
            try {
                drawAccelGraph();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void updateGlobalFileHandler() throws IOException {
            if(fileHandler == null)
                fileHandler = new TempFileHandler(tempFilepath, tempFilename);

            TempFileHandler checkUpdate = new TempFileHandler(tempFilepath, tempFilename);

            if(checkUpdate.getNumMotions() > fileHandler.getNumMotions())
                fileHandler = checkUpdate;

        }

        private void drawAccelGraph() throws IOException {

            updateGlobalFileHandler();

            if(fileHandler.getNumMotions() > 0) {

                Motion[] mySwings = fileHandler.getMotions();

                int magAccel[];
                DataPoint[] dataset;
                DataPoint[] maxDataset = new DataPoint[mySwings.length];
                int thickness = 2;
                int max;
                for(int i = 0 ; i < mySwings.length ; i++) {
                    magAccel = mySwings[i].getAccelMagVector();
                    dataset = new DataPoint[magAccel.length];

                    max = magAccel[0];

                    for(int j = 0 ; j < magAccel.length ; j++) {
                        dataset[j] = new DataPoint(j, magAccel[j]);
                        max = Math.max(max, magAccel[j]);
                    }
                    maxDataset[i] = new DataPoint(i+1, max);
                    drawLineGraph(graphs[0], dataset, i, thickness);
                }
                drawBarGraph(graphs[1], maxDataset);
            }
        }

        private int[] colors = {Color.GRAY, Color.BLUE, Color.DKGRAY, Color.GREEN, Color.YELLOW,
                Color.LTGRAY, Color.MAGENTA, Color.RED, Color.BLACK};

        private void drawLineGraph(GraphView graph, DataPoint[] dataset, int i, int thickness) {
            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataset);
            series.setColor(colors[i%colors.length]);
            series.setThickness(thickness);
            GridLabelRenderer style = graph.getGridLabelRenderer();
            style.setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
            style.setPadding(30);
            graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        // show normal x values
                        return super.formatLabel(value, isValueX);
                    } else {
                        // show currency for y values
                        return super.formatLabel(value / (16384 / 8), isValueX) + " g";
                    }
                }
            });
            setYAxisBounds(graph);
            graph.setTitle("Swing Accelerations");
            graph.setTitleColor(Color.GRAY);
            graph.addSeries(series);
            super.onStart();
        }

        private void drawBarGraph(GraphView graph, DataPoint[] dataset) {
            BarGraphSeries<DataPoint> series = new BarGraphSeries<DataPoint>(dataset);
            series.setColor(colors[(int) (Math.random() * 8)]);

            series.setSpacing(50);

            series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
                @Override
                public int get(DataPoint data) {
                    return Color.rgb((int) data.getX() * 255 / 4, (int) Math.abs(data.getY() * 255 / 6), 100);
                }
            });

            GridLabelRenderer style = graph.getGridLabelRenderer();
            style.setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
            style.setPadding(30);
            style.setLabelsSpace(30);
            graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        // show normal x values
                        return super.formatLabel(value, isValueX);
                    } else {
                        // show currency for y values
                        return super.formatLabel(value / (16384 / 8), isValueX) + " g";
                    }
                }
            });
            setYAxisBounds(graph);
            graph.setTitle("Peak Acceleration");
            graph.setTitleColor(Color.GRAY);
            graph.addSeries(series);
            super.onStart();
        }
    }

    private static void setYAxisBounds(GraphView graph) {
        Viewport curViewPort = graph.getViewport();
        curViewPort.setMinY(0);
        curViewPort.setMaxY(16384 * 2);
        graph.getViewport().setYAxisBoundsManual(true);
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
        private int CONSISTENCY_THRESHOLD = 1000000;
        TextView scoreView;
        TextView numView;
        TextView speedView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_consistency, container, false);
            Bundle args = getArguments();
            myRootView = rootView;
            t = (TextView) rootView.findViewById(R.id.test);
            scoreView = (TextView) rootView.findViewById(R.id.score);
            numView = (TextView) rootView.findViewById(R.id.num);
            //speedView = (TextView) rootView.findViewById(R.id.speed);
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

        public void updateGlobalFileHandler() throws IOException {
            if(fileHandler == null)
                fileHandler = new TempFileHandler(tempFilepath, tempFilename);

            TempFileHandler checkUpdate = new TempFileHandler(tempFilepath, tempFilename);

            if(checkUpdate.getNumMotions() > fileHandler.getNumMotions())
                fileHandler = checkUpdate;

        }

        public void getScore() throws IOException {
            updateGlobalFileHandler();
            runDTW(fileHandler);
        }

        private void runDTW(TempFileHandler fileHandler) {
            Motion[] mySwings = fileHandler.getMotions();

            if(DEBUG_ON)
                t.setText("Number of motions: " + mySwings.length);

            numView.setText(mySwings.length + " swings");
            double consistencyScore = 0;

            for (int i = 0; i < mySwings.length - 1; i++) {
                consistencyScore += calculateConsistencyHelper(mySwings, i, i + 1);
            }
            int consistencyMax = (mySwings.length - 1);

            double score = round(consistencyScore/consistencyMax, 2);

            if(DEBUG_ON)
                t.setText(t.getText() + "||" + "Score: " + score + "%");

            scoreView.setText(score + "%");

            double maxSpeed = 0.0d;
            for(Motion swing: mySwings)
                maxSpeed = Math.max(swing.getMaxVelocity(), maxSpeed);

        }

        private static double round(double value, int places) {
            if (places < 0) throw new IllegalArgumentException();

            long factor = (long) Math.pow(10, places);
            value = value * factor;
            long tmp = Math.round(value);
            return (double) tmp / factor;
        }

        private void printMotionComparerToDebugger(Motion m1, Motion m2) {
            Log.d("Motion 1, Accel X:", Arrays.toString(m1.getAccelX()));
            Log.d("Motion 2, Accel X:", Arrays.toString(m2.getAccelX()));
            Log.d("Motion 1, Accel Y:", Arrays.toString(m1.getAccelY()));
            Log.d("Motion 2, Accel Y:", Arrays.toString(m2.getAccelY()));
            Log.d("Motion 1, Accel Z:", Arrays.toString(m1.getAccelZ()));
            Log.d("Motion 2, Accel Z:", Arrays.toString(m2.getAccelZ()));
        }

        private double calculateConsistencyHelper(Motion[] mySwings, int i, int i1) {
            Motion swing1 = mySwings[i];
            Motion swing2 = mySwings[i1];

            DTWHelper[] dtw = new DTWHelper[3];
            dtw[0] = new DTWHelper(swing1.getAccelX(), swing2.getAccelX());
            dtw[1] = new DTWHelper(swing1.getAccelY(), swing2.getAccelY());
            dtw[2] = new DTWHelper(swing1.getAccelZ(), swing2.getAccelZ());

            Log.d("average of absolute value of x for swing 1: ", Integer.toString(swing1.getAbsAvgX()));
            Log.d("max of absolute value of x for swing 1: ", Integer.toString(swing1.getxMax()));
            Log.d("average of absolute value of x for swing 2: ", Integer.toString(swing2.getAbsAvgX()));
            Log.d("max of absolute value of x for swing 2: ", Integer.toString(swing2.getxMax()));
            Log.d("square of difference between xmax and xavg swing 1: ", Integer.toString((int) Math.pow(Math.abs(swing1.getAbsAvgX() - swing1.getxMax()), 2)));
            Log.d("square of difference between xmax and xavg swing 2: ", Integer.toString((int) Math.pow(Math.abs(swing2.getAbsAvgX() - swing2.getxMax()), 2)));
            Log.d("Int average: ", Double.toString(((Math.pow(Math.abs(swing1.getAbsAvgX() - swing1.getxMax()), 2)) + (Math.pow(Math.abs(swing2.getAbsAvgX() - swing2.getxMax()), 2))) / 2));
            int threshold = CONSISTENCY_THRESHOLD + (int) Math.pow(Math.abs(swing1.getAbsAvgX() - swing1.getxMax()), 2) + (int) Math.pow(Math.abs(swing2.getAbsAvgX() - swing2.getxMax()), 2);
            Log.d("X - Proposed Threshold: ", Integer.toString(threshold));


            Log.d("average of absolute value of y for swing 1: ", Integer.toString(swing1.getAbsAvgY()));
            Log.d("max of absolute value of y for swing 1: ", Integer.toString(swing1.getyMax()));
            Log.d("average of absolute value of y for swing 2: ", Integer.toString(swing2.getAbsAvgY()));
            Log.d("max of absolute value of y for swing 2: ", Integer.toString(swing2.getyMax()));
            Log.d("square of difference between ymax and yavg swing 1: ", Integer.toString((int) Math.pow(Math.abs(swing1.getAbsAvgY() - swing1.getyMax()), 2)));
            Log.d("square of difference between ymax and yavg swing 2: ", Integer.toString((int) Math.pow(Math.abs(swing2.getAbsAvgY() - swing2.getyMax()), 2)));
            Log.d("Int average: ", Double.toString(((Math.pow(Math.abs(swing1.getAbsAvgY() - swing1.getyMax()), 2)) + (Math.pow(Math.abs(swing2.getAbsAvgY() - swing2.getyMax()), 2))) / 2));
            threshold = CONSISTENCY_THRESHOLD + (int) Math.pow(Math.abs(swing1.getAbsAvgY() - swing1.getyMax()), 2) + (int) Math.pow(Math.abs(swing2.getAbsAvgY() - swing2.getyMax()), 2);
            Log.d("Y - Proposed Threshold: ", Integer.toString(threshold));


            Log.d("average of absolute value of z for swing 1: ", Integer.toString(swing1.getAbsAvgZ()));
            Log.d("max of absolute value of z for swing 1: ", Integer.toString(swing1.getzMax()));
            Log.d("average of absolute value of z for swing 2: ", Integer.toString(swing2.getAbsAvgZ()));
            Log.d("max of absolute value of z for swing 2: ", Integer.toString(swing2.getzMax()));
            Log.d("square of difference between zmax and zavg swing 1: ", Integer.toString((int) Math.pow(Math.abs(swing1.getAbsAvgZ() - swing1.getzMax()), 2)));
            Log.d("square of difference between zmax and zavg swing 2: ", Integer.toString((int) Math.pow(Math.abs(swing2.getAbsAvgZ() - swing2.getzMax()), 2)));
            Log.d("Int average: ", Double.toString(((Math.pow(Math.abs(swing1.getAbsAvgZ() - swing1.getzMax()), 2)) + (Math.pow(Math.abs(swing2.getAbsAvgZ() - swing2.getzMax()), 2))) / 2));
            threshold = CONSISTENCY_THRESHOLD + (int) Math.pow(Math.abs(swing1.getAbsAvgZ() - swing1.getzMax()), 2) + (int) Math.pow(Math.abs(swing2.getAbsAvgZ() - swing2.getzMax()), 2);
            Log.d("Z - Proposed Threshold: ", Integer.toString(threshold));


            int consistencyThresholdX = CONSISTENCY_THRESHOLD + (int) Math.pow(Math.abs(
                    swing1.getAbsAvgX() - swing1.getxMax()), 2) + (int) Math.pow(
                    Math.abs(swing2.getAbsAvgX() - swing2.getxMax()), 2);

            int consistencyThresholdY = CONSISTENCY_THRESHOLD + (int) Math.pow(
                    Math.abs(swing1.getAbsAvgY() - swing1.getyMax()), 2) + (int) Math.pow(
                    Math.abs(swing2.getAbsAvgY() - swing2.getyMax()), 2);

            int consistencyThresholdZ = CONSISTENCY_THRESHOLD + (int) Math.pow(Math.abs(
                    swing1.getAbsAvgZ() - swing1.getzMax()), 2) + (int) Math.pow(
                    Math.abs(swing2.getAbsAvgZ() - swing2.getzMax()), 2);

            Log.d("X pathcost: ", "" + dtw[0].getDTW().getPathCost());
            Log.d("Y pathcost: ", "" + dtw[1].getDTW().getPathCost());
            Log.d("Z pathcost: ", "" + dtw[2].getDTW().getPathCost());

            double primaryXCost = dtw[0].getDTW().getPathCost() / consistencyThresholdX;
            double primaryYCost = dtw[1].getDTW().getPathCost() / consistencyThresholdY;
            double primaryZCost = dtw[2].getDTW().getPathCost() / consistencyThresholdZ;

            Log.d("X -> Is pathCost less than threshold? ", "" + 100/primaryXCost);
            Log.d("Y -> Is pathCost less than threshold? ", "" + 100/primaryYCost);
            Log.d("Z -> Is pathCost less than threshold? ", "" + 100/primaryZCost);

            printMotionComparerToDebugger(swing1, swing2);

            double xCost = (100/primaryXCost) > 100 ?
                    (100 - (primaryXCost * BUFFER_CONSISTENCY_BOUND)) : (100/primaryXCost);

            double yCost = (100/primaryYCost) > 100 ?
                    (100 - (primaryYCost * BUFFER_CONSISTENCY_BOUND)) : (100/primaryYCost);

            double zCost = (100/primaryZCost) > 100 ?
                    (100 - (primaryZCost * BUFFER_CONSISTENCY_BOUND)) : (100/primaryZCost);

            return (xCost + yCost + zCost)/3;
        }
    }

    /**
     * Adapted from dummy fragment representing a section of the app, but that
     * simply displays dummy text.
     * Converted to: Scorekeeper section -> Keeps score...
     */

    @SuppressLint("ValidFragment")
    public class ScorekeeperSectionFragment extends Fragment {
        // class constants
        public static final String ARG_SECTION_NUMBER = "section_number";

        TextView t;
        View myRootView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_scorekeeper, container, false);
//            Bundle args = getArguments();
            myRootView = rootView;
            final TextView reset = (TextView)rootView.findViewById(R.id.reset);
            t = (TextView)rootView.findViewById(R.id.test);
            final TextView popupMenu = (TextView)rootView.findViewById(R.id.popup_scorekeeper);
            final TextView player1 = (TextView)rootView.findViewById(R.id.plus);
            final TextView player2 = (TextView)rootView.findViewById(R.id.minus);
            popupMenu.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                PopupMenu popup = new PopupMenu(getActivity().getApplicationContext(), v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.popup_scorekeeper, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                    if(DEBUG_ON)
                        t.setText("Pop up option set: " + item.getTitle());
                    switch (item.getItemId()) {
                        case R.id.popup_scorekeeper_tennis:
                            myScorekeeper.changeSportToTennis();
                            // change sets to win based on what user inputs
                            // setsToWin = {user_input}
                            player1.setBackground(getResources().getDrawable((R.drawable.tennisa)));
                            player2.setBackground(getResources().getDrawable((R.drawable.tennisb)));
                            myScorekeeper.resetScores(player1, player2);
                            player1.setTextSize(20);
                            player2.setTextSize(20);
                            return true;
                        case R.id.popup_scorekeeper_basketball:
                            myScorekeeper.changeSportToBasketball();
                            myScorekeeper.changeScoreToWin(DEFAULT_SCORE_TO_WIN_BASKETBALL);
                            player1.setBackground(getResources().getDrawable((R.drawable.basketballa)));
                            player2.setBackground(getResources().getDrawable((R.drawable.basketballb)));
                            player1.setTextSize(40);
                            player2.setTextSize(40);
                            myScorekeeper.resetScores(player1, player2);
                            return true;
                        case R.id.popup_scorekeeper_golf:
                            myScorekeeper.changeSportToGolf();
                            myScorekeeper.changeScoreToWin(-1); // never allow them to win
                            player1.setBackground(getResources().getDrawable((R.drawable.golfa)));
                            player2.setBackground(getResources().getDrawable((R.drawable.golfb)));
                            myScorekeeper.resetScores(player1, player2);
                            player1.setTextSize(40);
                            player2.setTextSize(40);
                            return true;

                        default:
                            myScorekeeper.changeSportToDefault();
                            myScorekeeper.changeScoreToWin(DEFAULT_SCORE_TO_WIN);
                            player1.setBackground(getResources().getDrawable((R.drawable.greencircle)));
                            player2.setBackground(getResources().getDrawable((R.drawable.redcircle)));
                            myScorekeeper.resetScores(player1, player2);
                            player1.setTextSize(40);
                            player2.setTextSize(40);
                            return true;
                    }
                    }
                });

                // debug
                if(DEBUG_ON)
                    t.setText("Pop up");

                popup.show();
                }
            });

            reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                myScorekeeper.resetScores(player1, player2);
                }
            });


            /* onclick on plus and minus buttons increments value */
            player1.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onClick(View v) {
                    myScorekeeper.incrementScore(1, player1, player2, getApplicationContext());
                }
            });

            player2.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onClick(View v) {
                    myScorekeeper.incrementScore(2, player1, player2, getApplicationContext());

                }

            });

            /* longclick on plus and minus buttons decrement value */
            player1.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    myScorekeeper.decrementScore(1, player1, player2);
                return true;
                }

            });
            player2.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                myScorekeeper.decrementScore(2, player1, player2);
                    return true;
                }
            });
            return rootView;
        }
    }
}
