package com.example.mymicapplication;

import android.content.Context;
import android.robot.speech.SpeechManager;
import android.util.Log;

public class SpeechManagerWrapper {

    private SpeechManager mSpeechManager;
    private Context mContext;
    private static final String SPEECH_MANAGER_PACKAGE = "com.avatar.dialog";

    // Constructor
    public SpeechManagerWrapper(Context context) {
        this.mContext = context;
    }

    // Initialize and return the SpeechManager
    public SpeechManager getSpeechManager(SpeechManager.OnConnectListener listener) {
        if (mSpeechManager == null) {
            mSpeechManager = new SpeechManager(mContext, listener, SPEECH_MANAGER_PACKAGE);
            Log.d("SpeechManagerWrapper", "SpeechManager initialized.");
        }
        return mSpeechManager;
    }

    // Release resources held by SpeechManager
    public void releaseSpeechManager() {
        if (mSpeechManager != null) {
            mSpeechManager = null; // Nullify the reference
            Log.d("SpeechManagerWrapper", "SpeechManager released.");
        }
    }
}
