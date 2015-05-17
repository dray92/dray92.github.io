package com.lannbox.rfduinotest;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Debosmit on 5/17/15.
 */
public class BluetoothGod {

    private static BluetoothDevice myBluetoothDevice;

    public static void setBT(BluetoothDevice bt) {
        myBluetoothDevice = bt;
    }

    public static BluetoothDevice getBT() {

        return myBluetoothDevice;
    }
}