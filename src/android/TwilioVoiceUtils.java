package com.codealer.twiliovoice;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;

import android.util.Log;
import android.Manifest;
import android.media.AudioManager;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.twilio.voice.Voice;
import com.twilio.voice.Call;
import com.twilio.voice.CallException;
import com.twilio.voice.ConnectOptions;

public class TwilioVoiceUtils {
    public static CordovaInterface cordova;
    public static CordovaWebView webView;
    public static CordovaPlugin plugin;

    public static List<String> grantedPermissions = new ArrayList<String>();
    public static List<String> requiredPermissions = new ArrayList<String>();
    public static List<String> callInProgressStates = new ArrayList<String>();

    public static String accessToken;
    public static CallbackContext jsContext;
    public static Call activeCall;

    public static AudioManager audioManager;
    public static Boolean speakerOn = false;
    public static Boolean muteOn = false;

    public static void initialize(CordovaInterface cordova, CordovaWebView webView, CordovaPlugin plugin) {
        TwilioVoiceUtils.cordova = cordova;
        TwilioVoiceUtils.webView = webView;
        TwilioVoiceUtils.plugin = plugin;

        TwilioVoiceUtils.requiredPermissions.add(Manifest.permission.RECORD_AUDIO);

        TwilioVoiceUtils.callInProgressStates.add(Call.State.CONNECTED.toString());
        TwilioVoiceUtils.callInProgressStates.add(Call.State.CONNECTING.toString());
        TwilioVoiceUtils.callInProgressStates.add(Call.State.RINGING.toString());
        TwilioVoiceUtils.callInProgressStates.add(Call.State.RECONNECTING.toString());

        audioManager = (AudioManager) cordova.getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    public static void checkPermissions() {
        if (!TwilioVoiceUtils.hasAllPermissions()) {
            TwilioVoiceUtils.requestPermissions();
        }
    }

    public static void requestPermissions() {
        String[] tmpPermissions = new String[TwilioVoiceUtils.requiredPermissions.size()];
        tmpPermissions = TwilioVoiceUtils.requiredPermissions.toArray(tmpPermissions);
        TwilioVoiceUtils.cordova.requestPermissions(plugin, 1000, tmpPermissions);
    }

    public static boolean hasAllPermissions() {
        return TwilioVoiceUtils.getUngrantedPermissions().size() == 0;
    }

    public static List<String> getUngrantedPermissions() {
        List<String> out = new ArrayList<String>();

        if (TwilioVoiceUtils.requiredPermissions.size() > 0) {
            for(int i = 0; i < TwilioVoiceUtils.requiredPermissions.size(); i++) {
                if (!TwilioVoiceUtils.grantedPermissions.contains(TwilioVoiceUtils.requiredPermissions.get(i))) {
                    out.add(TwilioVoiceUtils.requiredPermissions.get(i));
                }
            }
        }
        return out;
    }

    public static void addGrantedPermission(String permission) {
        if (!TwilioVoiceUtils.grantedPermissions.contains(permission)) {
            TwilioVoiceUtils.grantedPermissions.add(permission);
        }
    }

    public static void returnError(String id, String message, String paramsJson) {
        if (TwilioVoiceUtils.jsContext != null) {
            try {
                JSONObject params = new JSONObject();
                params.put("id", id);
                params.put("error", message);
                params.put("params", paramsJson);
                TwilioVoiceUtils.returnResponse("error", params);
            }
            catch(JSONException e) {}
        }
    }

    public static void returnSuccess(String event, String paramsJson) {
        if (TwilioVoiceUtils.jsContext != null) {
            try {
                JSONObject params = new JSONObject();
                params.put("event", event);
                params.put("data", paramsJson);
                TwilioVoiceUtils.returnResponse("success", params);
            }
            catch(JSONException e) {}
        }
    }

    public static void returnResponse(String status, JSONObject response) {
        PluginResult.Status responseStatus = status.equals("success") ? PluginResult.Status.OK : PluginResult.Status.ERROR;
        PluginResult result = new PluginResult(responseStatus, response);
        result.setKeepCallback(true);
        TwilioVoiceUtils.jsContext.sendPluginResult(result);
    }

    public static HashMap jsonObjectToMap(JSONObject jsonObject) {
        HashMap<String, String> output = new Gson().fromJson(jsonObject.toString(), HashMap.class);
        return output;
    }

    public static Call.Listener activeCallListener = new Call.Listener() {
        @Override
        public void onRinging(Call call) {
            TwilioVoiceUtils.returnSuccess("statusChanged", "ringing");
        }

        @Override
        public void onConnectFailure(Call call, CallException error) {
            TwilioVoiceUtils.returnSuccess("statusChanged", "connectFailure");
        }

        @Override
        public void onConnected(Call call) {
            TwilioVoiceUtils.returnSuccess("statusChanged", "connected");
        }

        @Override
        public void onReconnecting(Call call, CallException error) {
            TwilioVoiceUtils.returnSuccess("statusChanged", "reconnecting");
        }

        @Override
        public void onReconnected(Call call) {
            TwilioVoiceUtils.returnSuccess("statusChanged", "reconnected");
        }

        @Override
        public void onDisconnected(Call call, CallException error) {
            TwilioVoiceUtils.returnSuccess("statusChanged", "disconnected");
        }
    };

}
