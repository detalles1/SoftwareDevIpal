package com.example.mymicapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.robot.motion.RobotMotion;
import android.robot.speech.SpeechManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private SpeechManager mSpeechManager;
    private SpeechRecognizer mSpeechRecognizer;
    private RobotMotion mRobotMotion = new RobotMotion();
    private boolean isSpeechManagerInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SpeechManager and SpeechRecognizer
        initializeSpeechManager();
        initializeSpeechRecognizer();

        // Setup UI buttons
        Button btnAndroid = (Button)findViewById(R.id.btn_android);
        btnAndroid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AndroidActivity.class);
                startActivity(intent);
            }
        });

        Button btnAsr = (Button)findViewById(R.id.btn_asr);
        btnAsr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ASRActivity.class);
                startActivity(intent);
            }
        });
        Button btnApi = (Button)findViewById(R.id.btn_api);
        btnApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, APIActivity.class);
                startActivity(intent);
            }
        });

        Button btnGood = (Button)findViewById(R.id.btn_google);
        btnGood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GoogleActivity.class);
                startActivity(intent);
            }
        });

        Button btnExit = (Button)findViewById(R.id.btn_exit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Exit the app
            }
        });
    }

    private void initializeSpeechManager() {
        mSpeechManager = new SpeechManager(this, new SpeechManager.OnConnectListener() {
            @Override
            public void onConnect(boolean status) {
                if (status) {
                    Log.d("MainActivity", "SpeechManager initialized successfully!");
                    mSpeechManager.setTtsEnable(true); // Enable TTS
                    isSpeechManagerInitialized = true;
                } else {
                    Log.e("MainActivity", "Failed to initialize SpeechManager.");
                    isSpeechManagerInitialized = false;
                }
            }
        }, "com.avatar.dialog");
    }

    private void initializeSpeechRecognizer() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d("MainActivity", "SpeechRecognizer is ready.");
            }

            @Override
            public void onError(int error) {
                Log.e("MainActivity", "SpeechRecognizer error: " + error);
                triggerRobotPrompt("Error with speech recognition. Please try again.");
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    handleSpeechInput(matches.get(0));
                }
            }

            @Override
            public void onBeginningOfSpeech() {}
            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {}
            @Override
            public void onPartialResults(Bundle partialResults) {}
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startListeningForSpeech() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        try {
            mSpeechRecognizer.startListening(intent);
        } catch (Exception e) {
            Log.e("MainActivity", "Error starting SpeechRecognizer", e);
            triggerRobotPrompt("Error with speech recognition. Please try again.");
        }
    }

    private void triggerRobotPrompt(String phrase) {
        if (isSpeechManagerInitialized && mSpeechManager.getTtsEnable()) {
            mSpeechManager.forceStartSpeaking(phrase, false, false);
            Log.d("MainActivity", "Robot speaking: " + phrase);
        } else {
            Log.e("MainActivity", "SpeechManager not initialized or TTS disabled.");
        }
    }

    private void handleSpeechInput(String userInput) {
        Log.d("MainActivity", "User said: " + userInput);
        triggerRobotPrompt("You said: " + userInput);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up SpeechManager and SpeechRecognizer
        if (mSpeechManager != null) {
            mSpeechManager = null;
            Log.d("MainActivity", "SpeechManager cleaned up.");
        }

        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;
            Log.d("MainActivity", "SpeechRecognizer cleaned up.");
        }
    }
}
