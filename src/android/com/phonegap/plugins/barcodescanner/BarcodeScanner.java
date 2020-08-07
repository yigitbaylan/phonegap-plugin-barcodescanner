/**
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 * Copyright (c) Matt Kane 2010
 * Copyright (c) 2011, IBM Corporation
 * Copyright (c) 2013, Maciej Nux Jaros
 */
package com.phonegap.plugins.barcodescanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.content.pm.PackageManager;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PermissionHelper;

import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureActivity;

/**
 * This calls out to the ZXing barcode reader and returns the result.
 *
 * @sa https://github.com/apache/cordova-android/blob/master/framework/src/org/apache/cordova/CordovaPlugin.java
 */
public class BarcodeScanner extends CordovaPlugin {
    public static final int REQUEST_CODE = 0x0ba7c;
    private static final String LOG_TAG = "BarcodeScanner";

    private String [] permissions = { Manifest.permission.CAMERA };

    private JSONArray requestArgs;
    private CallbackContext callbackContext;

    /**
     * Constructor.
     */
    public BarcodeScanner() {
    }

    /**
     * Executes the request.
     *
     * This method is called from the WebView thread. To do a non-trivial amount of work, use:
     *     cordova.getThreadPool().execute(runnable);
     *
     * To run on the UI thread, use:
     *     cordova.getActivity().runOnUiThread(runnable);
     *
     * @param action          The action to execute.
     * @param args            The exec() arguments.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return                Whether the action was valid.
     *
     * @sa https://github.com/apache/cordova-android/blob/master/framework/src/org/apache/cordova/CordovaPlugin.java
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        this.requestArgs = args;

        if (action.equals(Options.ENCODE)) {
            JSONObject obj = args.optJSONObject(0);
            if (obj != null) {
                String type = obj.optString(Options.TYPE);
                String data = obj.optString(Options.DATA);

                // If the type is null then force the type to text
                if (type == null) {
                    type = Options.TEXT_TYPE;
                }

                if (data == null) {
                    callbackContext.error("User did not specify data to encode");
                    return true;
                }

                encode(type, data);
            } else {
                callbackContext.error("User did not specify data to encode");
                return true;
            }
        } else if (action.equals(Options.SCAN)) {

            //android permission auto add
            if(!hasPermisssion()) {
                requestPermissions(0);
            } else {
                scan(args);
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * Starts an intent to scan and decode a barcode.
     */
    public void scan(final JSONArray args) {

        final CordovaPlugin that = this;

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                scanCustomScanner(that.cordova.getActivity());
                Intent intentScan =  scanCustomScanner(that.cordova.getActivity());
                intentScan.setAction(Intents.Scan.ACTION);
                intentScan.addCategory(Intent.CATEGORY_DEFAULT);

                // add config as intent extras
                if (args.length() > 0) {

                    JSONObject obj;
                    JSONArray names;
                    String key;
                    Object value;

                    for (int i = 0; i < args.length(); i++) {

                        try {
                            obj = args.getJSONObject(i);
                        } catch (JSONException e) {
                            Log.i("CordovaLog", e.getLocalizedMessage());
                            continue;
                        }

                        names = obj.names();
                        for (int j = 0; j < names.length(); j++) {
                            try {
                                key = names.getString(j);
                                value = obj.get(key);

                                if (value instanceof Integer) {
                                    intentScan.putExtra(key, (Integer) value);
                                } else if (value instanceof String) {
                                    intentScan.putExtra(key, (String) value);
                                }

                            } catch (JSONException e) {
                                Log.i("CordovaLog", e.getLocalizedMessage());
                            }
                        }

                        intentScan.putExtra(Intents.Scan.CAMERA_ID, obj.optBoolean(Options.PREFER_FRONTCAMERA, false) ? 1 : 0);
//                        intentScan.putExtra(Intents.Scan.SHOW_FLIP_CAMERA_BUTTON, obj.optBoolean(SHOW_FLIP_CAMERA_BUTTON, false));
//                        intentScan.putExtra(Intents.Scan.SHOW_TORCH_BUTTON, obj.optBoolean(SHOW_TORCH_BUTTON, false));
                        intentScan.putExtra(Intents.Scan.TORCH_ENABLED, obj.optBoolean(Options.TORCH_ON, false));
                        intentScan.putExtra(Options.SHOW_LASER, obj.optBoolean(Options.SHOW_LASER, true));
//                        intentScan.putExtra(Intents.Scan.SAVE_HISTORY, obj.optBoolean(SAVE_HISTORY, false));
                        boolean beep = obj.optBoolean(Options.DISABLE_BEEP, false);
                        intentScan.putExtra(Intents.Scan.BEEP_ENABLED, !beep);
//                        if (obj.has(RESULTDISPLAY_DURATION)) {
//                            intentScan.putExtra(Intents.Scan.RESULT_DISPLAY_DURATION_MS, "" + obj.optLong(RESULTDISPLAY_DURATION));
//                        }
                        if (obj.has(Options.FORMATS)) {
                            intentScan.putExtra(Intents.Scan.FORMATS, obj.optString(Options.FORMATS));
                        }
                        if (obj.has(Options.PROMPT)) {
                            intentScan.putExtra(Intents.Scan.PROMPT_MESSAGE, obj.optString(Options.PROMPT));
                        }
                        if (obj.has(Options.ORIENTATION)) {
                            intentScan.putExtra(Intents.Scan.ORIENTATION_LOCKED, obj.optString(Options.ORIENTATION));
                        }
                        if(obj.has(Options.LASER_COLOR)){
                            intentScan.putExtra(Options.LASER_COLOR, obj.optString(Options.LASER_COLOR));
                        }
                        if(obj.has(Options.BORDER_COLOR)){
                            intentScan.putExtra(Options.BORDER_COLOR, obj.optString(Options.BORDER_COLOR));
                        }
                        if(obj.has(Options.BORDER_THICKNESS)){
                            intentScan.putExtra(Options.BORDER_THICKNESS, obj.optInt(Options.BORDER_THICKNESS));
                        }
                        if(obj.has(Options.DIVIDE_DISTANCE)){
                            intentScan.putExtra(Options.DIVIDE_DISTANCE, obj.optInt(Options.DIVIDE_DISTANCE));
                        }
                        if(obj.has(Options.BORDER_COLOR)){
                            intentScan.putExtra(Options.BORDER_COLOR, obj.optString(Options.BORDER_COLOR));
                        }
                        if(obj.has(Options.FONT_SIZE)){
                            intentScan.putExtra(Options.FONT_SIZE, obj.optInt(Options.FONT_SIZE));
                        }
                        if(obj.has(Options.FONT_COLOR)){
                            int color;
                            try{
                                color = Color.parseColor(Options.FONT_COLOR);
                            }
                            catch (Exception e){
                                color = Color.parseColor("#FFFFFF");
                            }
                            intentScan.putExtra(Options.FONT_COLOR, color);
                        }
                    }

                }

                // avoid calling other phonegap apps
                intentScan.setPackage(that.cordova.getActivity().getApplicationContext().getPackageName());

                that.cordova.startActivityForResult(that, intentScan, REQUEST_CODE);
            }
        });
    }

    public Intent scanCustomScanner(Activity activity) {
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setOrientationLocked(true);
        integrator.setCaptureActivity(CustomScannerActivity.class);
        integrator.setPrompt("Scan something");
        integrator.setBeepEnabled(false);
        Intent intent = integrator.createScanIntent();
        return intent;
    }

    /**
     * Called when the barcode scanner intent completes.
     *
     * @param requestCode The request code originally supplied to startActivityForResult(),
     *                       allowing you to identify who this result came from.
     * @param resultCode  The integer result code returned by the child activity through its setResult().
     * @param intent      An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE && this.callbackContext != null) {
            if (resultCode == Activity.RESULT_OK) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put(Options.TEXT, intent.getStringExtra("SCAN_RESULT"));
                    obj.put(Options.FORMAT, intent.getStringExtra("SCAN_RESULT_FORMAT"));
                    obj.put(Options.CANCELLED, false);
                } catch (JSONException e) {
                    Log.d(LOG_TAG, "This should never happen");
                }
                //this.success(new PluginResult(PluginResult.Status.OK, obj), this.callback);
                this.callbackContext.success(obj);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put(Options.TEXT, "");
                    obj.put(Options.FORMAT, "");
                    obj.put(Options.CANCELLED, true);
                } catch (JSONException e) {
                    Log.d(LOG_TAG, "This should never happen");
                }
                //this.success(new PluginResult(PluginResult.Status.OK, obj), this.callback);
                this.callbackContext.success(obj);
            } else {
                //this.error(new PluginResult(PluginResult.Status.ERROR), this.callback);
                this.callbackContext.error("Unexpected error");
            }
        }
    }

    /**
     * Initiates a barcode encode.
     *
     * @param type Endoiding type.
     * @param data The data to encode in the bar code.
     */
    public void encode(String type, String data) {
//        Intent intentEncode = new Intent(this.cordova.getActivity().getBaseContext(), EncodeActivity.class);
//        intentEncode.setAction(Intents.Encode.ACTION);
//        intentEncode.putExtra(Intents.Encode.TYPE, type);
//        intentEncode.putExtra(Intents.Encode.DATA, data);
//        // avoid calling other phonegap apps
//        intentEncode.setPackage(this.cordova.getActivity().getApplicationContext().getPackageName());
//
//        this.cordova.getActivity().startActivity(intentEncode);
    }

    /**
     * check application's permissions
     */
    public boolean hasPermisssion() {
        for(String p : permissions)
        {
            if(!PermissionHelper.hasPermission(this, p))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * We override this so that we can access the permissions variable, which no longer exists in
     * the parent class, since we can't initialize it reliably in the constructor!
     *
     * @param requestCode The code to get request action
     */
    public void requestPermissions(int requestCode)
    {
        PermissionHelper.requestPermissions(this, requestCode, permissions);
    }

    /**
     * processes the result of permission request
     *
     * @param requestCode The code to get request action
     * @param permissions The collection of permissions
     * @param grantResults The result of grant
     */
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException
    {
        PluginResult result;
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                Log.d(LOG_TAG, "Permission Denied!");
                result = new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION);
                this.callbackContext.sendPluginResult(result);
                return;
            }
        }

        switch(requestCode)
        {
            case 0:
                scan(this.requestArgs);
                break;
        }
    }

    /**
     * This plugin launches an external Activity when the camera is opened, so we
     * need to implement the save/restore API in case the Activity gets killed
     * by the OS while it's in the background.
     */
    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }
}
