package com.clarkgarrett.solartilt.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;

import com.clarkgarrett.solartilt.R;
import com.clarkgarrett.solartilt.Utility;

import java.nio.charset.StandardCharsets;

public class PrivacyPolicyActivity extends Activity {

    private String TAG = "## My Info ##";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        WebView wvPrivacy = findViewById(R.id.wvPrivacy);
        wvPrivacy.loadUrl("file:///android_asset/privacy_policy.htm" );
    }
}