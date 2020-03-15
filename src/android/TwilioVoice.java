package com.codealer.twiliovoice;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import com.google.gson.Gson;

public class TwilioVoice extends CordovaPlugin {

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        TwilioVoiceUtils.initialize(cordova, webView, this);
        TwilioVoiceOutgoing.initialize();

        TwilioVoiceUtils.checkPermissions();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        TwilioVoiceUtils.jsContext = callbackContext;

        if (!TwilioVoiceUtils.hasAllPermissions()) {
            TwilioVoiceUtils.requestPermissions();
            return true;
        }

        if (action.equals("setToken")) {
            this.setToken(args.getString(0));
            return true;
        }
        else if (action.equals("connect")) {
            JSONObject params = args.optJSONObject(0);
            TwilioVoiceOutgoing.connect(params);
            return true;
        }
        else if (action.equals("disconnect")) {
            this.disconnect();
            return true;
        }
        else if (action.equals("toggleSpeaker")) {
            this.toggleSpeaker();
            return true;
        }
        else if (action.equals("toggleMute")) {
            this.toggleMute();
            return true;
        }

        return false;
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for(int i = 0; i < grantResults.length; i++) {
            int grant = grantResults[i];
            String permission = permissions[i];

            if (grant == 0) {
                TwilioVoiceUtils.addGrantedPermission(permission);
            }
        }

        if (!TwilioVoiceUtils.hasAllPermissions()) {
            String perm = new Gson().toJson(TwilioVoiceUtils.getUngrantedPermissions());
            TwilioVoiceUtils.returnError("permissions_required", "Permission required", perm);
        }
    }

    private void setToken(String token) {
        if (token != null && token.length() > 0) {
            String currentToken = TwilioVoiceUtils.accessToken;
            TwilioVoiceUtils.accessToken = token;
            if (currentToken == null) {
                TwilioVoiceUtils.returnSuccess("initialized", token);
            }
            TwilioVoiceUtils.returnSuccess("tokenSet", token);
        }
        else {
            TwilioVoiceUtils.returnError("token_missing", "Token parameter is missing", null);
        }
    }

    private void disconnect() {
        if (TwilioVoiceUtils.activeCall != null) {
            TwilioVoiceUtils.activeCall.disconnect();
        }
    }

    private void toggleSpeaker() {
        TwilioVoiceUtils.speakerOn = !TwilioVoiceUtils.speakerOn;
        TwilioVoiceUtils.audioManager.setSpeakerphoneOn(TwilioVoiceUtils.speakerOn);
        TwilioVoiceUtils.returnSuccess("speakerToggled", String.valueOf(TwilioVoiceUtils.speakerOn));
    }

    private void toggleMute() {
        if (TwilioVoiceUtils.activeCall != null && TwilioVoiceUtils.callInProgressStates.contains(TwilioVoiceUtils.activeCall.getState().toString())) {
            TwilioVoiceUtils.muteOn = !TwilioVoiceUtils.activeCall.isMuted();
            TwilioVoiceUtils.activeCall.mute(TwilioVoiceUtils.muteOn);
            TwilioVoiceUtils.returnSuccess("muteToggled", String.valueOf(TwilioVoiceUtils.muteOn));
        }
    }
}
