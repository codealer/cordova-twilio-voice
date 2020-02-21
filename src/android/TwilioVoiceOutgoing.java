package com.codealer.twiliovoice;

import org.json.JSONObject;

import java.util.*;

import com.twilio.voice.ConnectOptions;
import com.twilio.voice.Voice;

public class TwilioVoiceOutgoing {
    public static void initialize() {

    }

    public static void connect(JSONObject params) {
        TwilioVoiceUtils.cordova.getThreadPool().execute(new Runnable(){
            public void run() {
                if (TwilioVoiceUtils.activeCall == null || !TwilioVoiceUtils.callInProgressStates.contains(TwilioVoiceUtils.activeCall.getState().toString())) {
                    TwilioVoiceUtils.returnSuccess("statusChanged", "connecting");
                    HashMap options = TwilioVoiceUtils.jsonObjectToMap(params);
                    ConnectOptions connectOptions = new ConnectOptions.Builder(TwilioVoiceUtils.accessToken).params(options).build();
                    TwilioVoiceUtils.activeCall = Voice.connect(TwilioVoiceUtils.cordova.getActivity(), connectOptions, TwilioVoiceUtils.activeCallListener);
                }
            }
        });
    }
}
