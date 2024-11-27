package com.example.androidmultiplyapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.Random;

public class MainActivity extends Activity {

    private SpeechManager mSpeechManager;
    private RobotMotion mRobotMotion = new RobotMotion();
    private SpeechRecognizer mSpeechRecognizer;
    private Handler handler = new Handler();
    private Random random = new Random();
    private int correctAnswer;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;
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
        initializeSpeechRecognizer();
        setupButtons();
        startRandomBlinking();
    }

    private boolean isMicrophoneAvailable() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "RECORD_AUDIO permission not granted. Requesting now...");
                requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);
            } else {
                Log.d("Permissions", "RECORD_AUDIO permission already granted.");
            }
        } else {
            Log.d("Permissions", "Permissions are automatically granted for SDK < M.");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) { // Matches the request code in checkPermissions()
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "User granted RECORD_AUDIO permission.");
            } else {
                Log.e("Permissions", "User denied RECORD_AUDIO permission.");
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

    private void initializeSpeechRecognizer() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            setupSpeechRecognizer();
        } else {
            Log.e("MainActivity", "Speech recognition is not available.");
            triggerRobotPrompt("Speech recognition is unavailable. Please check your device.");
        }
    }

    private void setupSpeechRecognizer() {
        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d("MainActivity", "SpeechRecognizer is ready.");
            }

            @Override
            public void onError(int error) {
                Log.e("MainActivity", "SpeechRecognizer error: " + error);
                if (retryCount < MAX_RETRIES) {
                    retryCount++;
                    triggerRobotPrompt("I didn't hear anything. Let's try again.");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            restartSpeechRecognizer();
                        }
                    }, 2000);
                } else {
                    triggerRobotPrompt("I'm sorry, I couldn't understand you. Let's move on.");
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = (ArrayList<String>) results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    retryCount = 0; // Reset retries on success
                    processUserAnswer(matches.get(0));
                } else {
                    triggerRobotPrompt("I didn't catch that. Could you try again?");
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

    private void restartSpeechRecognizer() {
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        setupSpeechRecognizer();
    }

    private void setupButtons() {
        Button testListenButton = (Button) findViewById(R.id.btn_test_listen);
        testListenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMultiplicationGame();
            }
        });

        Button exitButton = (Button) findViewById(R.id.btn_exit);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitApplication();
            }
        });

        Button resetButton = (Button) findViewById(R.id.btn_reset);
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
            Button numberButton = (Button) findViewById(resID);
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

    private void startMultiplicationGame() {
        int num1 = random.nextInt(10) + 1;
        int num2 = random.nextInt(10) + 1;
        correctAnswer = num1 * num2;

        String question = "What is " + num1 + " times " + num2 + "?";
        triggerRobotPrompt(question);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startListeningForAnswer();
            }
        }, 5000);
    }

    private void startListeningForAnswer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);

        try {
            mSpeechRecognizer.startListening(intent);
        } catch (Exception e) {
            Log.e("MainActivity", "Error starting SpeechRecognizer", e);
            triggerRobotPrompt("Error with speech recognition. Please try again.");
        }
    }

    private boolean processUserAnswer(String userResponse) {
        try {
            int userAnswer = Integer.parseInt(userResponse.replaceAll("[^0-9]", ""));
            if (userAnswer == correctAnswer) {
                triggerRobotPrompt("Correct! Great job!");
                return true;
            } else {
                triggerRobotPrompt("That's incorrect. The correct answer is " + correctAnswer + ".");
                return false;
            }
        } catch (NumberFormatException e) {
            Log.e("MainActivity", "Error parsing user response: " + userResponse, e);
            triggerRobotPrompt("I couldn't understand your answer. Let's try again.");
            return false;
        }
    }
    private void cleanupSpeechManager() {
        if (mSpeechManager != null) {
            try {
                Log.d("MainActivity", "Cleaning up SpeechManager...");
            } catch (Exception e) {
                Log.e("MainActivity", "Error cleaning up SpeechManager.", e);
            } finally {
                mSpeechManager = null; // Nullify the SpeechManager
                Log.d("MainActivity", "SpeechManager reference nullified.");
            }
        }
    }

    private void exitApplication() {
        Log.d("MainActivity", "Exiting application...");

        // Clean up SpeechRecognizer
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;
        }

        // Clean up SpeechManager
        cleanupSpeechManager();

        // Remove handler callbacks
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        // Finish activity
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "onDestroy() called.");
        exitApplication();
    }
} 