package com.example.apimultiplyapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.robot.motion.RobotMotion;
import android.robot.speech.SpeechManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Random;

public class MainActivity extends Activity {

    private SpeechManager mSpeechManager;
    private RobotMotion mRobotMotion = new RobotMotion();
    private Handler handler = new Handler();
    private Random random = new Random();
    private int correctAnswer;
    private boolean isSpeechManagerInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isMicrophoneAvailable()) {
            Log.e("MainActivity", "No microphone available on this device.");
            triggerRobotPrompt("No microphone detected. Speech features will not work.");
            return;
        }

        checkPermissions();
        initializeSpeechManager();
        setupButtons();
        startRandomBlinking();
    }

    private boolean isMicrophoneAvailable() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Microphone permission granted.");
            } else {
                Log.e("MainActivity", "Microphone permission denied.");
                triggerRobotPrompt("Microphone permission is required for speech features.");
            }
        }
    }

    private void initializeSpeechManager() {
        mSpeechManager = new SpeechManager(this, new SpeechManager.OnConnectListener() {
            @Override
            public void onConnect(boolean status) {
                if (status) {
                    Log.d("MainActivity", "SpeechManager initialized successfully!");
                    mSpeechManager.setTtsEnable(true);
                    isSpeechManagerInitialized = true;
                    initializeTtsListener();
                    triggerInitialPrompt();
                } else {
                    Log.e("MainActivity", "Failed to initialize SpeechManager.");
                    triggerRobotPrompt("SpeechManager is not ready. Please wait.");
                }
            }
        }, "com.avatar.dialog");
    }

    private void setupButtons() {
        Button testListenButton = (Button)findViewById(R.id.btn_test_listen);
        testListenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTestListen();
            }
        });

        Button exitButton = (Button)findViewById(R.id.btn_exit);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitApplication();
            }
        });

        Button resetButton = (Button)findViewById(R.id.btn_reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSpeechManagerInitialized) {
                    triggerRobotPrompt("Let's practice multiplication. Please select a number.");
                } else {
                    Log.e("MainActivity", "SpeechManager not initialized. Cannot reset.");
                }
            }
        });

        for (int i = 1; i <= 10; i++) {
            final int selectedNumber = i;
            int resID = getResources().getIdentifier("button" + i, "id", getPackageName());
            Button numberButton = (Button)findViewById(resID);
            if (numberButton != null) {
                numberButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openModeSelectionActivity(selectedNumber);
                    }
                });
            }
        }
    }

    private void handleTestListen() {
        try {
            // Start speech recognition using RobotApiHelper
            RobotApiHelper.startSpeechRecognition();
            triggerRobotPrompt("Listening... Please speak.");

            // Fetch and process the result after a delay
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String apiResponse = RobotApiHelper.fetchSpeechResult();
                    if (apiResponse != null) {
                        handleApiResponse(apiResponse);
                    } else {
                        triggerRobotPrompt("No response from API. Please try again.");
                    }
                }
            }, 5000); // Adjust delay as needed
        } catch (Exception e) {
            Log.e("MainActivity", "Error using RobotApiHelper for Test Listen", e);
            triggerRobotPrompt("Error accessing speech recognition. Please try again.");
        }
    }

    private void handleApiResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            String recognizedText = jsonResponse.getString("recognizedText");
            Log.d("MainActivity", "Recognized Text: " + recognizedText);
            // You can process the recognizedText further here
        } catch (JSONException e) {
            Log.e("MainActivity", "Error parsing API response", e);
        }
    }

    private void openModeSelectionActivity(int number) {
        Intent intent = new Intent(this, ModeSelectionActivity.class);
        intent.putExtra("number", number);
        startActivity(intent);
    }

    private void initializeTtsListener() {
        if (mSpeechManager != null) {
            mSpeechManager.setTtsListener(new SpeechManager.TtsListener() {
                @Override
                public void onBegin(int requestId) {
                    Log.d("MainActivity", "TTS started: requestId = " + requestId);
                }

                @Override
                public void onEnd(int requestId) {
                    Log.d("MainActivity", "TTS finished: requestId = " + requestId);
                }

                @Override
                public void onError(int requestId) {
                    Log.e("MainActivity", "TTS error: requestId = " + requestId);
                    triggerRobotPrompt("There was an error with speech output. Please try again.");
                }
            });
        }
    }

    private void triggerInitialPrompt() {
        triggerRobotPrompt("Let's practice multiplication. Please select a number.");
    }

    private void triggerRobotPrompt(String phrase) {
        if (mSpeechManager != null && isSpeechManagerInitialized && mSpeechManager.getTtsEnable()) {
            mSpeechManager.forceStartSpeaking(phrase, false, false);
            Log.d("MainActivity", "Robot is speaking: " + phrase);
        } else {
            Log.e("MainActivity", "Cannot start TTS: SpeechManager not initialized or TTS disabled.");
        }
    }

    private void startRandomBlinking() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mRobotMotion.emoji(RobotMotion.Emoji.BLINK);
                int randomBlinkInterval = 2000 + random.nextInt(8000);
                handler.postDelayed(this, randomBlinkInterval);
            }
        });
    }

    private void exitApplication() {
        Log.d("MainActivity", "Exiting application...");

        if (mSpeechManager != null) {
            mSpeechManager.setAsrListener(null);
        }

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "onDestroy() called.");
        exitApplication();
    }
}
