package com.porfirio.procidainkayak;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;


public class CalendarActivity extends AppCompatActivity {


    private WebView wv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

        wv = (WebView) findViewById(R.id.webView);

        //wv.loadUrl("file:///android_asset/calendar2017.jpg");

        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setLoadWithOverviewMode(true);
        wv.getSettings().setUseWideViewPort(true);
        String data = "<body>" + "<img src=\"calendar2018.jpg\"/></body>";

        wv.loadDataWithBaseURL("file:///android_asset/",data , "text/html", "utf-8",null);

    }
}
