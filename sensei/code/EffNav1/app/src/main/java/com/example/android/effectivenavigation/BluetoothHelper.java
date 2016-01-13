package com.example.android.effectivenavigation;

/**
 * Created by Debosmit on 5/22/15.
 */

/*
 * Copyright (C) 2013 Lann Martin
 *
 * Licensed under the Apache License, Version 2.0 as below
 *
 * This file incorporates work covered by the following notice:
 *
 *   Copyright (C) 2013 The Android Open Source Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
import android.bluetooth.BluetoothDevice;

import java.util.UUID;

public class BluetoothHelper {
    public static String shortUuidFormat = "0000%04X-0000-1000-8000-00805F9B34FB";

    public static UUID sixteenBitUuid(long shortUuid) {
        assert shortUuid >= 0 && shortUuid <= 0xFFFF;
        return UUID.fromString(String.format(shortUuidFormat, shortUuid & 0xFFFF));
    }

    public static String getDeviceInfoText(BluetoothDevice device, int rssi, byte[] scanRecord) {
        return new StringBuilder()
                .append("Name: ").append(device.getName())
                .append("\nMAC: ").append(device.getAddress())
                .append("\nRSSI: ").append(rssi)
                .append("\nScan Record:").append(parseScanRecord(scanRecord))
                .toString();
    }

    public static String getDeviceName(BluetoothDevice device, int rssi, byte[] scanRecord) {
        return new StringBuilder().append(device.getName()).toString();
    }

    // Bluetooth Spec V4.0 - Vol 3, Part C, section 8
    private static String parseScanRecord(byte[] scanRecord) {
        StringBuilder output = new StringBuilder();
        int i = 0;
        while (i < scanRecord.length) {
            int len = scanRecord[i++] & 0xFF;
            if (len == 0) break;
            switch (scanRecord[i] & 0xFF) {
                // https://www.bluetooth.org/en-us/specification/assigned-numbers/generic-access-profile
                case 0x0A: // Tx Power
                    output.append("\n  Tx Power: ").append(scanRecord[i+1]);
                    break;
                case 0xFF: // Manufacturer Specific data (RFduinoBLE.advertisementData)
                    output.append("\n  Advertisement Data: ")
                            .append(HexAsciiHelper.bytesToHex(scanRecord, i + 3, len));

                    String ascii = HexAsciiHelper.bytesToAsciiMaybe(scanRecord, i + 3, len);
                    if (ascii != null) {
                        output.append(" (\"").append(ascii).append("\")");
                    }
                    break;
            }
            i += len;
        }
        return output.toString();
    }
}