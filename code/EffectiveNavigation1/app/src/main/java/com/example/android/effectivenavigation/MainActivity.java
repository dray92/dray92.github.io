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

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

// ADDED @targetapi and implements BluetoothAdapter.LeScanCallback to test callback on btAdapter LeScanCallback
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainActivity extends FragmentActivity implements ActionBar.TabListener, BluetoothAdapter.LeScanCallback {

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
    static boolean scanStarted;

    private static BluetoothAdapter btAdapter;
    private static BluetoothDevice btDevice;

    private RFduinoService rfduinoService;

    /**
        Bluetooth State
        State machine
     */
    final private static int STATE_BLUETOOTH_OFF = 1;
    final private static int STATE_DISCONNECTED = 2;
    final private static int STATE_CONNECTING = 3;
    final private static int STATE_CONNECTED = 4;

    private int state;

    // DEBUG VARIABLES
    String debug_deviceInfo = "";

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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

        // when the app is loaded, scan started should be set to false
        scanStarted = false;

        // initializing the bluetooth adapter when the tab is loaded
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null) {
            Toast.makeText(getApplicationContext(), "Fucked. You are completely fucked. " +
                    "Do not have access to Bluetooth Radio", Toast.LENGTH_LONG);
            System.exit(0);
        }

        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOn, 0);

        // for testing purposes ONLY
//        Set<BluetoothDevice> pairedDevices;
//        pairedDevices = btAdapter.getBondedDevices();

        boolean btAdapterDisc = btAdapter.startDiscovery();
        boolean btAdapterScan = btAdapter.startLeScan(
                new UUID[]{RFduinoService.UUID_SERVICE}, MainActivity.this);

        // pointer to the button that 'starts looking for senseiii'
        Button connectBTButton = (Button) findViewById(R.id.demo_collection_button);
        connectBTButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
//                connectionStatusText.setText("Connecting...");
                Intent rfduinoIntent = new Intent(MainActivity.this, RFduinoService.class);
                bindService(rfduinoIntent, rfduinoServiceConnection, BIND_AUTO_CREATE);
            }
        });


        /*
            private final ServiceConnection rfduinoServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    rfduinoService = ((RFduinoService.LocalBinder) service).getService();
                    if (rfduinoService.initialize()) {
                        if (rfduinoService.connect(bluetoothDevice.getAddress())) {
                            upgradeState(STATE_CONNECTING);
                        }
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    rfduinoService = null;
                    downgradeState(STATE_DISCONNECTED);
                }
            };
         */

//        BluetoothDevice[] devices = (BluetoothDevice[])pairedDevices.toArray();
//        Log.e("Size", "" + devices.length);
//        Log.e("Elem1", "" + devices[0]);
        // check to ensure that device bluetooth is turned on
        /*
                enableBTButton = (Button) findViewById(R.id.enableBluetooth);
                enableBTButton.setOnClickListener(new View.onClickListener(){
                    @Override
                    public void onClick(View v) {
                        enableBTButton.setEnabled(false);
                        enableBTButton.setText(btAdapter.enable() ? "Enabling BT..." : "Enable Failed...");
                    }
                });
         */


    }

    private final ServiceConnection rfduinoServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            rfduinoService = ((RFduinoService.LocalBinder) service).getService();
            if (rfduinoService.initialize()) {
                if (rfduinoService.connect(btDevice.getAddress())) {
                    upgradeState(STATE_CONNECTING);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            rfduinoService = null;
            downgradeState(STATE_DISCONNECTED);
        }
    };

    private void upgradeState(int newState) {
        if (newState > state) {
            updateState(newState);
        }
    }

    private void downgradeState(int newState) {
        if (newState < state) {
            updateState(newState);
        }
    }

    private void updateState(int newState) {
        state = newState;
        updateUi();
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

    public void onLeScan(BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        btAdapter.stopLeScan(this);
        btDevice = device;

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                debug_deviceInfo = BluetoothHelper.getDeviceInfoText(btDevice, rssi, scanRecord);
                updateUi();
            }
        });

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
            switch (i) {
                case 0:
                    // The first section of the app is the most interesting -- it offers
                    // a launchpad into the other demonstrations in this example application.
                    return new LaunchpadSectionFragment();

                default:
                    // The other sections of the app are dummy placeholders.
                    Fragment fragment = new DummySectionFragment();
                    Bundle args = new Bundle();
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

    /**
     * A fragment that launches other parts of the demo application.
     */
    public static class LaunchpadSectionFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_launchpad, container, false);

            // Demonstration of a collection-browsing activity.
            rootView.findViewById(R.id.demo_collection_button)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            scanStarted = true;
//                            btAdapter.startLeScan(new UUID[]{RFduinoService.UUID_SERVICE});
//                            btAdapter.startLeScan(new UUID[]{RFduinoService.UUID_SERVICE}, null);
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

            return rootView;
        }
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


    // need to update to reflect changes with changes in Bluetooth
    private void updateUi() {
        // Enable Bluetooth
//        boolean on = state > STATE_BLUETOOTH_OFF;
//        enableBluetoothButton.setEnabled(!on);
//        enableBluetoothButton.setText(on ? "Bluetooth enabled" : "Enable Bluetooth");
//        scanButton.setEnabled(on);
//
//        // Scan
//        if (scanStarted && scanning) {
//            scanStatusText.setText("Scanning...");
//            scanButton.setText("Stop Scan");
//            scanButton.setEnabled(true);
//        } else if (scanStarted) {
//            scanStatusText.setText("Scan started...");
//            scanButton.setEnabled(false);
//        } else {
//            scanStatusText.setText("");
//            scanButton.setText("Scan");
//            scanButton.setEnabled(true);
//        }
//
//        // Connect
//        boolean connected = false;
//        String connectionText = "Disconnected";
//        if (state == STATE_CONNECTING) {
//            connectionText = "Connecting...";
//        } else if (state == STATE_CONNECTED) {
//            connected = true;
//            connectionText = "Connected";
//        }
//        connectionStatusText.setText(connectionText);
//        connectButton.setEnabled(bluetoothDevice != null && state == STATE_DISCONNECTED);
//
//        // Send
//        sendZeroButton.setEnabled(connected);
//        sendValueButton.setEnabled(connected);
    }
}
