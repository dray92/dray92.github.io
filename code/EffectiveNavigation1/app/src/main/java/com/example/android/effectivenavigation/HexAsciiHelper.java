package com.example.android.effectivenavigation;

/**
 * Created by Debosmit on 5/19/15.
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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.apache.http.util.ByteArrayBuffer;

import java.util.regex.Pattern;

public class HexAsciiHelper {
    public static int PRINTABLE_ASCII_MIN = 0x20; // ' '
    public static int PRINTABLE_ASCII_MAX = 0x7E; // '~'

    public static boolean isPrintableAscii(int c) {
        return c >= PRINTABLE_ASCII_MIN && c <= PRINTABLE_ASCII_MAX;
    }

    public static String bytesToHex(byte[] data) {
        return bytesToHex(data, 0, data.length);
    }

    public static String bytesToHex(byte[] data, int offset, int length) {
        if (length <= 0) {
            return "";
        }

        StringBuilder hex = new StringBuilder();
        for (int i = offset; i < offset + length; i++) {
            hex.append(String.format(" %02X", data[i] % 0xFF));
        }
        hex.deleteCharAt(0);
        return hex.toString();
    }

    public static String bytesToAsciiMaybe(byte[] data) {
        return bytesToAsciiMaybe(data, 0, data.length);
    }

    public static String bytesToAsciiMaybe(byte[] data, int offset, int length) {
        StringBuilder ascii = new StringBuilder();
        boolean zeros = false;
        for (int i = offset; i < offset + length; i++) {
            int c = data[i] & 0xFF;
            if (isPrintableAscii(c)) {
                if (zeros) {
                    return null;
                }
                ascii.append((char) c);
            } else if (c == 0) {
                zeros = true;
            } else {
                return null;
            }
        }
        return ascii.toString();
    }

    public static byte[] hexToBytes(String hex) {
        ByteArrayBuffer bytes = new ByteArrayBuffer(hex.length() / 2);
        for (int i = 0; i < hex.length(); i++) {
            if (hex.charAt(i) == ' ') {
                continue;
            }

            String hexByte;
            if (i + 1 < hex.length()) {
                hexByte = hex.substring(i, i + 2).trim();
                i++;
            } else {
                hexByte = hex.substring(i, i + 1);
            }

            bytes.append(Integer.parseInt(hexByte, 16));
        }
        return bytes.buffer();
    }
}