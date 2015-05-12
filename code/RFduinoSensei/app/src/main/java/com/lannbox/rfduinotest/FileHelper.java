package com.lannbox.rfduinotest;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


/**
 * Created by Jango on 5/11/2015.
 */
public class FileHelper {

    // deletes file in the sensei_temp folder
    public static void clearContents(String filename, Context context) throws FileNotFoundException {
        File root = new File(Environment.getExternalStorageDirectory(), "temp_sensei");
        if (!root.exists()) {
            root.mkdirs();
        }


        File fileToClear = new File(root, filename);
        if (fileToClear.exists()) {
            PrintWriter writer = new PrintWriter(fileToClear);
            writer.print("");
            writer.close();

            Toast.makeText(context, "Deleted " + filename, Toast.LENGTH_SHORT).show();
        }

    }
}
