package com.example.mymicapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.content.pm.PackageManager;
import android.robot.speech.SpeechManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class AndroidActivity extends Activity {

    private SpeechManager mSpeechManager;
    private SpeechRecognizer mSpeechRecognizer;
    private Handler handler = new Handler();
    private Random random = new Random();
    private int correctAnswer;
    private String lastQuestion = "";
    private boolean isSpeechManagerInitialized = false;

    // UI Components
    private TextView questionTextView;
    private Button btnBack, btnReset, btnRepeat;

    private static final String TAG = "AndroidActivity";

    // Map for word-based number conversions
    private static final Map<String, Integer> wordToNumberMap = new HashMap<String, Integer>();
    static {
        wordToNumberMap.put("zero", 0);
        wordToNumberMap.put("one", 1);
        wordToNumberMap.put("two", 2);
        wordToNumberMap.put("three", 3);
        wordToNumberMap.put("four", 4);
        wordToNumberMap.put("five", 5);
        wordToNumberMap.put("six", 6);
        wordToNumberMap.put("seven", 7);
        wordToNumberMap.put("eight", 8);
        wordToNumberMap.put("nine", 9);
        wordToNumberMap.put("ten", 10);
        wordToNumberMap.put("eleven", 11);
        wordToNumberMap.put("twelve", 12);
        wordToNumberMap.put("thirteen", 13);
        wordToNumberMap.put("fourteen", 14);
        wordToNumberMap.put("fifteen", 15);
        wordToNumberMap.put("sixteen", 16);
        wordToNumberMap.put("seventeen", 17);
        wordToNumberMap.put("eighteen", 18);
        wordToNumberMap.put("nineteen", 19);
        wordToNumberMap.put("twenty", 20);
        wordToNumberMap.put("thirty", 30);
        wordToNumberMap.put("forty", 40);
        wordToNumberMap.put("fifty", 50);
        wordToNumberMap.put("sixty", 60);
        wordToNumberMap.put("seventy", 70);
        wordToNumberMap.put("eighty", 80);
        wordToNumberMap.put("ninety", 90);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android);

        // Find UI components
        questionTextView = (TextView) findViewById(R.id.question_text_view);
        btnBack = (Button) findViewById(R.id.btn_back);
        btnReset = (Button) findViewById(R.id.btn_reset);
        btnRepeat = (Button) findViewById(R.id.btn_repeat);

        initializeSpeechManager();
        initializeSpeechRecognizer();

        // Back button to return to MainActivity
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRobotSpeech();
                finish();
            }
        });

        // Reset button to generate a new question
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRobotSpeech();
                startMultiplicationGame();
            }
        });

        // Repeat button to repeat the last question
        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRobotSpeech();
                triggerRobotPrompt(lastQuestion);
            }
        });
    }

    private void initializeSpeechManager() {
        mSpeechManager = new SpeechManager(this, new SpeechManager.OnConnectListener() {
            @Override
            public void onConnect(boolean status) {
                if (status) {
                    Log.d(TAG, "SpeechManager initialized successfully!");
                    mSpeechManager.setTtsEnable(true);
                    isSpeechManagerInitialized = true;
                    startMultiplicationGame();
                } else {
                    Log.e(TAG, "Failed to initialize SpeechManager.");
                    isSpeechManagerInitialized = false;
                }
            }
        });
    }

    private void initializeSpeechRecognizer() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "SpeechRecognizer is ready.");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "User started speaking.");
            }

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "User finished speaking.");
            }

            @Override
            public void onError(int error) {
                Log.e(TAG, "SpeechRecognizer error code: " + error + " - " + getErrorText(error));
                triggerRobotPrompt("I couldn't understand that. Please try again.");
            }


            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = (ArrayList<String>) results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    processUserAnswer(matches.get(0));
                } else {
                    triggerRobotPrompt("I didn't catch that. Please try again.");
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startMultiplicationGame() {
        if (!isSpeechManagerInitialized) {
            Log.e(TAG, "SpeechManager is not initialized yet.");
            return;
        }

        int num1 = random.nextInt(10) + 1;
        int num2 = random.nextInt(10) + 1;
        correctAnswer = num1 * num2;

        lastQuestion = "What is " + num1 + " times " + num2 + "?";
        questionTextView.setText(lastQuestion);

        triggerRobotPrompt(lastQuestion);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startListeningForAnswer();
            }
        }, 5000); // Adjust delay to allow TTS to complete
    }

    private void startListeningForAnswer() {
        stopRobotSpeech();
        if (!isMicrophoneAvailable()) {
            Log.e(TAG, "Microphone is unavailable.");
            triggerRobotPrompt("Microphone is unavailable. Please check your settings.");
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        try {
            mSpeechRecognizer.startListening(intent);
            Log.d(TAG, "Speech recognition started.");
        } catch (Exception e) {
            Log.e(TAG, "Error starting SpeechRecognizer", e);
            triggerRobotPrompt("Error with speech recognition. Please try again.");
        }
    }

    private void processUserAnswer(String userResponse) {
        try {
            int userAnswer = parseWordNumber(userResponse.trim());
            if (userAnswer == correctAnswer) {
                triggerRobotPrompt("Correct! Well done!");
            } else {
                triggerRobotPrompt("That's incorrect. The correct answer is " + correctAnswer + ".");
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing user response: " + userResponse, e);
            triggerRobotPrompt("I couldn't understand your answer. Please try again.");
        }
    }

    private int parseWordNumber(String input) throws NumberFormatException {
        input = input.toLowerCase(Locale.ROOT).trim();
        if (input.matches("\\d+")) {
            return Integer.parseInt(input);
        }
        if (wordToNumberMap.containsKey(input)) {
            return wordToNumberMap.get(input);
        }
        if (input.contains(" ")) {
            String[] parts = input.split(" ");
            if (parts.length == 2) {
                Integer tens = wordToNumberMap.get(parts[0]);
                Integer units = wordToNumberMap.get(parts[1]);
                if (tens != null && units != null) {
                    return tens + units;
                }
            }
        }
        throw new NumberFormatException("Invalid word-based number: " + input);
    }

    private void triggerRobotPrompt(String phrase) {
        if (isSpeechManagerInitialized && mSpeechManager.getTtsEnable()) {
            mSpeechManager.forceStartSpeaking(phrase, false, false);
            Log.d(TAG, "Robot speaking: " + phrase);
        } else {
            Log.e(TAG, "SpeechManager not initialized or TTS disabled.");
        }
    }

    private void stopRobotSpeech() {
        if (mSpeechManager != null) {
            mSpeechManager.setTtsEnable(false);
            mSpeechManager.setTtsEnable(true);
        }
    }

    private String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client-side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No match found";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input detected";
            default:
                return "Unknown error";
        }
    }
    private boolean isMicrophoneAvailable() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        boolean isMuted = audioManager != null && audioManager.isMicrophoneMute();
        Log.d(TAG, "Microphone status: " + (isMuted ? "Muted" : "Available"));
        return audioManager != null && !isMuted;
    }
    private void requestMicrophonePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting RECORD_AUDIO permission.");
                requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
        if (mSpeechManager != null) {
            stopRobotSpeech();
        }
    }
}
