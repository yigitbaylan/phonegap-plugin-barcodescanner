package com.phonegap.plugins.barcodescanner;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ViewfinderView;

import java.util.Random;

import capacitor.android.plugins.R;

/**
 * Custom Scannner Activity extending from Activity to display a custom layout form scanner view.
 */
public class CustomScannerActivity extends Activity {

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private Button switchFlashlightButton;
    private CustomViewfinderView viewfinderView;
    TextView placeholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_scanner);

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);

        viewfinderView = findViewById(R.id.zxing_viewfinder_view);
        placeholder = (TextView) findViewById(R.id.zxing_status_view);

        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();

        changeMaskColor(null);
        changeLaserVisibility(false);
        if(getIntent().hasExtra(Intents.Scan.PROMPT_MESSAGE)){
            placeholder.setText(getIntent().getStringExtra(Intents.Scan.PROMPT_MESSAGE));
        }
        if(getIntent().hasExtra(Options.FONT_COLOR)){
            placeholder.setTextColor(getIntent().getIntExtra(Options.FONT_COLOR, Color.parseColor("#FFFFFF")));
        }
        if(getIntent().hasExtra(Options.FONT_SIZE)){
            placeholder.setTextSize(getIntent().getIntExtra(Options.FONT_SIZE, 24));
        }
        if(getIntent().hasExtra(Options.DIVIDE_DISTANCE)){
            viewfinderView.setBorderThickness(getIntent().getIntExtra(Options.DIVIDE_DISTANCE, 4));
        }

        if(getIntent().hasExtra(Options.LASER_COLOR)){
            viewfinderView.setLaserColor(getIntent().getStringExtra(Options.LASER_COLOR));
        }
        if(getIntent().hasExtra(Options.SHOW_LASER)){
            viewfinderView.setLaserVisibility(getIntent().getBooleanExtra(Options.SHOW_LASER, true));
        }
        if(getIntent().hasExtra(Options.BORDER_COLOR)){
            viewfinderView.setBorderColor(getIntent().getStringExtra(Options.BORDER_COLOR));
        }
        if(getIntent().hasExtra(Options.BORDER_THICKNESS)){
            viewfinderView.setBorderThickness(getIntent().getIntExtra(Options.BORDER_THICKNESS, 15));
        }
        if(getIntent().hasExtra(Options.DIVIDE_DISTANCE)){
            viewfinderView.setBorderThickness(getIntent().getIntExtra(Options.DIVIDE_DISTANCE, 4));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    public void changeMaskColor(View view) {
        int color = Color.argb(80, 0, 0, 0);
        viewfinderView.setBackgroundColor(color);
    }

    public void changeLaserVisibility(boolean visible) {
        viewfinderView.setLaserVisibility(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}