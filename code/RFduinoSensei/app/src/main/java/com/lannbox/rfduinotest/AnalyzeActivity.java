package com.lannbox.rfduinotest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.UUID;

public class AnalyzeActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {
    final private static int STATE_BLUETOOTH_OFF = 1;
    final private static int STATE_DISCONNECTED = 2;
    final private static int STATE_CONNECTING = 3;
    final private static int STATE_CONNECTED = 4;

    private int state;

    private boolean scanStarted;
    private boolean scanning;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;

    private RFduinoService rfduinoService;

    private Button enableBluetoothButton;
    private TextView scanStatusText;
    private Button scanButton;
    private TextView deviceInfoText;
    private TextView connectionStatusText;
    private Button connectButton;
    private EditData valueEdit;
    private Button sendZeroButton;
    private Button sendValueButton;
    private Button disconnectButton;
    private LinearLayout dataLayout;

    private String sportSelected;
    private String formSelected;
    private boolean disconnectFlag;

    Vibrator v;
    boolean vibrateFlag;

    private int plus = 0, minus = 0;

    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
            if (state == BluetoothAdapter.STATE_ON) {
                upgradeState(STATE_DISCONNECTED);
            } else if (state == BluetoothAdapter.STATE_OFF) {
                downgradeState(STATE_BLUETOOTH_OFF);
            }
        }
    };

    private final BroadcastReceiver scanModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanning = (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_NONE);
            scanStarted &= scanning;
            updateUi();
        }
    };

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

    // BroadcastReceiver for Rfduino
    private final BroadcastReceiver rfduinoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            if (RFduinoService.ACTION_CONNECTED.equals(action)) {
                upgradeState(STATE_CONNECTED);
            } else if (RFduinoService.ACTION_DISCONNECTED.equals(action)) {
                downgradeState(STATE_DISCONNECTED);

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


                    // Start button is pressed
                    if ( dataStr.equals("S00000000000") ) {

                        vibrateFlag = true;
                        Log.d("Feedback Char", "S");
                        generateNoteOnSD("temp.txt", "SSSSSSSSSSSS\n");

                    // Start button is pressed while it is on
                    } else if ( dataStr.contains("R00000000000")) {
                        Log.d("Feedback Char", "R");
                        generateNoteOnSD("temp.txt", "RRRRRRRRRRRR\n");

                    // Plus button is pressed while it is on
                    } else if ( dataStr.contains("P00000000000")) {
                        plus++;
                        Log.d("Plus", "" + plus);
                        final TextView txtValue = (TextView) findViewById(R.id.textView9);
                        txtValue.setText("Score: " + Integer.toString(plus-minus));
                        Log.d("Feedback Char", "P");
                        generateNoteOnSD("temp.txt", "PPPPPPPPPPPP\n");

                    // Negative button is pressed while it is on
                    } else if (dataStr.equals("N00000000000")) {
                        minus++;
                        Log.d("Minus", "" + minus);
                        final TextView txtValue = (TextView) findViewById(R.id.textView9);
                        txtValue.setText("Score: " + Integer.toString(plus - minus));
                        Log.d("Feedback Char", "N");
                        generateNoteOnSD("temp.txt", "NNNNNNNNNNNN\n");

//                    } else if ( dataStr.equals("R00000000000")) {
//                        Log.d("Feedback Char", "R");
//                        generateNoteOnSD("temp.txt", "RRRRRRRRRRRR\n");
//
                    // This is if the data from RFduino is not a button press, therefore just a data sent by IMU
                    } else {
                        // On the first data send, vibrate the phone for 1 second
                        if (vibrateFlag) {
                            vibrateFlag = false;
                            v.vibrate(1000);
                        }

                        generateNoteOnSD("temp.txt", dataToHex + "\n");
                    }



                    Log.d("DataSTR:", dataToHex);


                }

                //addData(data);

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        sportSelected = intent.getStringExtra("SPORT_ID");
        formSelected = intent.getStringExtra("FORM_ID");

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrateFlag = false;
        disconnectFlag = true;
//        Can be buggy with back button
//        Log.d("IN ANALYZE: sport id:", sportSelected);
//        Log.d("IN ANALYZE: form id:", formSelected);
        setContentView(R.layout.activity_analyze);

//        if (bluetoothAdapter == null) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        }
        // Bluetooth
        enableBluetoothButton = (Button) findViewById(R.id.enableBluetooth);
        enableBluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableBluetoothButton.setEnabled(false);
                enableBluetoothButton.setText(
                        bluetoothAdapter.enable() ? "Enabling bluetooth..." : "Enable failed!");
            }
        });

        // Find Device
        scanStatusText = (TextView) findViewById(R.id.scanStatus);

        scanButton = (Button) findViewById(R.id.scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanStarted = true;
                bluetoothAdapter.startLeScan(
                        new UUID[]{ RFduinoService.UUID_SERVICE },
                        AnalyzeActivity.this);
            }
        });

        // Device Info
        deviceInfoText = (TextView) findViewById(R.id.deviceInfo);

        // Connect Device
        connectionStatusText = (TextView) findViewById(R.id.connectionStatus);

        connectButton = (Button) findViewById(R.id.connect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                disconnectFlag = false;
                connectionStatusText.setText("Connecting...");
                Intent rfduinoIntent = new Intent(AnalyzeActivity.this, RFduinoService.class);
                bindService(rfduinoIntent, rfduinoServiceConnection, BIND_AUTO_CREATE);
            }
        });

        disconnectButton = (Button) findViewById(R.id.disconnect);

        // Send
        valueEdit = (EditData) findViewById(R.id.value);
        valueEdit.setImeOptions(EditorInfo.IME_ACTION_SEND);
        valueEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendValueButton.callOnClick();
                    return true;
                }
                return false;
            }
        });

        sendZeroButton = (Button) findViewById(R.id.sendZero);
        sendZeroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rfduinoService.send(new byte[]{0});
            }
        });

        sendValueButton = (Button) findViewById(R.id.sendValue);
        sendValueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rfduinoService.send(valueEdit.getData());
            }
        });



        dataLayout = (LinearLayout) findViewById(R.id.dataLayout);



    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(scanModeReceiver, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
        registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(rfduinoReceiver, RFduinoService.getIntentFilter());

        updateState(bluetoothAdapter.isEnabled() ? STATE_DISCONNECTED : STATE_BLUETOOTH_OFF);
    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
////        Log.d("AnalyzeActivity.onStop", "Stopping");
//        bluetoothAdapter.stopLeScan(this);
//
//        unregisterReceiver(scanModeReceiver);
//        unregisterReceiver(bluetoothStateReceiver);
//        unregisterReceiver(rfduinoReceiver);
//    }

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

    private void updateUi() {
        // Enable Bluetooth
        boolean on = state > STATE_BLUETOOTH_OFF;
        enableBluetoothButton.setEnabled(!on);
        enableBluetoothButton.setText(on ? "Bluetooth enabled" : "Enable Bluetooth");
        scanButton.setEnabled(on);

        // Scan
        if (scanStarted && scanning) {
            scanStatusText.setText("Scanning...");
            scanButton.setText("Stop Scan");
            scanButton.setEnabled(true);
        } else if (scanStarted) {
            scanStatusText.setText("Scan started...");
            scanButton.setEnabled(false);
        } else {
            scanStatusText.setText("");
            scanButton.setText("Scan");
            scanButton.setEnabled(true);
        }

        // Connect
        boolean connected = false;
        String connectionText = "Disconnected";
        if (state == STATE_CONNECTING) {
            connectionText = "Connecting...";
        } else if (state == STATE_CONNECTED) {
            connected = true;
            connectionText = "Connected";
        }
        connectionStatusText.setText(connectionText);
        connectButton.setEnabled(bluetoothDevice != null && state == STATE_DISCONNECTED);

        // Send
        sendZeroButton.setEnabled(connected);
        sendValueButton.setEnabled(connected);
    }

    private void addData(byte[] data) {
        View view = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, dataLayout, false);

        TextView text1 = (TextView) view.findViewById(android.R.id.text1);



        text1.setText(HexAsciiHelper.bytesToHex(data));

        String ascii = HexAsciiHelper.bytesToAsciiMaybe(data);
        if (ascii != null) {
            TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            text2.setText(ascii);
        }

        dataLayout.addView(
                view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLeScan(BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        bluetoothAdapter.stopLeScan(this);
        bluetoothDevice = device;

        AnalyzeActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceInfoText.setText(
                        BluetoothHelper.getDeviceInfoText(bluetoothDevice, rssi, scanRecord));
                updateUi();
            }
        });
    }

    public void done(View view) throws FileNotFoundException {
        Intent intent = new Intent(this, SummaryActivity.class);


        File root = new File(Environment.getExternalStorageDirectory(), "temp_sensei");
        if (!root.exists()) {
            root.mkdirs();
        }

        String tempFileName = "temp.txt";
        File tempFile = new File(root, tempFileName);

        Scanner scanFile = new Scanner(tempFile);
        int formSelectedInt = Integer.parseInt(formSelected);
        String dataFile  = "data" + formSelectedInt + ".txt";

        Log.d("Datafile", dataFile);

        while (scanFile.hasNextLine()) {
            String line = scanFile.nextLine();
            generateNoteOnSD(dataFile, line + "\n");
        }

        PrintWriter writer = new PrintWriter(tempFile);
        writer.print("");
        writer.close();






        startActivity(intent);
    }

    public void generateNoteOnSD(String sFileName, String sBody){
        try
        {
            File root = new File(Environment.getExternalStorageDirectory(), "temp_sensei");
            if (!root.exists()) {
                root.mkdirs();
            }

            File gpxfile = new File(root, sFileName);
            FileWriter writer;
            if (gpxfile.exists()) {
                writer = new FileWriter(gpxfile, true);
            } else {
                writer = new FileWriter(gpxfile);
            }

            writer.append(sBody);
            writer.flush();
            writer.close();
            //Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        }
        catch(IOException e)
        {
            e.printStackTrace();
//            importError = e.getMessage();
//            iError();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d("AnalyzeActivity", "ORIENTATION_LANDSCAPE");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d("AnalyzeActivity", "ORIENTATION_PORTRAIT");
        }
    }

    public void disconnect(View view) {

        if (!disconnectFlag) {
            Log.d("AnalyzeActivity.onStop", "Stopping");
            bluetoothAdapter.stopLeScan(this);

            unregisterReceiver(scanModeReceiver);
            unregisterReceiver(bluetoothStateReceiver);
            unregisterReceiver(rfduinoReceiver);
            disconnectButton.setEnabled(false);
//            connectButton.setEnabled(true);
//            downgradeState(STATE_DISCONNECTED);
//            scanStatusText.setText("");
//            scanButton.setText("Scan");
//            scanButton.setEnabled(true);
        }


        disconnectFlag = true;
    }




}
