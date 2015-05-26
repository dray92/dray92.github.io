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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
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

        // registering receivers
//        registerReceiver(scanModeReceiver, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
//        registerReceiver(btStateReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
//        registerReceiver(btStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
//        registerReceiver(rfduinoStateReceiver, RFduinoService.getIntentFilter());

        //updateState(btAdapter.isEnabled() ? STATE_DISCONNECTED : STATE_BLUETOOTH_OFF);
        oldState = btAdapter.isEnabled() ? STATE_DISCONNECTED : STATE_BLUETOOTH_OFF;

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

    private final BroadcastReceiver rfduinoStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
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
    View rootView;
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
            rootView.findViewById(R.id.demo_external_activity)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Create an intent that asks the user to pick a photo, but using
                            // FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET, ensures that relaunching
                            // the application from the device home screen does not return
                            // to the external activity.
                            Intent externalActivityIntent = new Intent(Intent.ACTION_PICK);
                            externalActivityIntent.setType("image/*");
                            externalActivityIntent.addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            startActivity(externalActivityIntent);
                        }
                    });

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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_data, container, false);
            Bundle args = getArguments();
            ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                    getString(R.string.data_section_text, args.getInt(ARG_SECTION_NUMBER)));

            return rootView;
        }
    }

    /**
     * Adapted from dummy fragment representing a section of the app, but that
     * simply displays dummy text.
     * Converted to: Consistency section -> opens data file, scans segments, gives a score...
     */
    public static class ConsistencySectionFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_consistency, container, false);
            Bundle args = getArguments();
            ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                    getString(R.string.consistency_section_text, args.getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * Adapted from dummy fragment representing a section of the app, but that
     * simply displays dummy text.
     * Converted to: Scorekeeper section -> Keeps score...
     */
    public static class ScorekeeperSectionFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_scorekeeper, container, false);
            Bundle args = getArguments();
            ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                    getString(R.string.scorekeeper_section_text, args.getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }
}
