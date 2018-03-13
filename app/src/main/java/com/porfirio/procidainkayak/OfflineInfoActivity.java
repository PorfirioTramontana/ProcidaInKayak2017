package com.porfirio.procidainkayak;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class OfflineInfoActivity extends Activity {

    private TextView textView;
    private StringBuilder text = new StringBuilder();

protected void onCreate(Bundle savedInstanceState) {
    BufferedReader reader = null;
    FileInputStream fs = null;
    DataInputStream in = null;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.offline_info_activity);


    InputStream input;

    try {
//
//        fs = new FileInputStream(getAssets()+"info.txt");
//        in = new DataInputStream(fs);
//        reader = new BufferedReader(new InputStreamReader(in));

//        AssetManager assetManager = this.getBaseContext().getAssets();
//        input = assetManager.open("info.txt");

        input = this.getApplicationContext().getResources().openRawResource(R.raw.info);
        int size = input.available();
        byte[] buffer = new byte[size];
        try {
            input.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        input.close();
        String text = new String(buffer);

//
//        // do reading, usually loop until end of file reading
//        String mLine;
//        while ((mLine = reader.readLine()) != null) {
//            text.append(mLine);
//            text.append('\n');
//        }
//    } catch (IOException e) {
//        Toast.makeText(getApplicationContext(),"Error reading file!",Toast.LENGTH_LONG).show();
//        e.printStackTrace();
//    } finally {
//        if (reader != null) {
//            try {
//                reader.close();
//            } catch (IOException e) {
//                //log the exception
//            }
//        }

        TextView output= (TextView) findViewById(R.id.infoText);
        output.setText(text);

    } catch (IOException e) {
        e.printStackTrace();
    }
}
}
