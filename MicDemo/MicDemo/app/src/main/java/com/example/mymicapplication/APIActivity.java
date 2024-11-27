package com.example.mymicapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.robot.speech.SpeechManager;

import java.util.Random;

public class APIActivity extends Activity {

    private static final int MAX_RETRIES = 3; // Maximum retry attempts
    private static final int SPEECH_DELAY_MULTIPLIER = 200; // Estimated time per character in ms
    private int retryCount = 0;

    private SpeechManager mSpeechManager;
    private boolean isSpeechManagerInitialized = false;
    private int correctAnswer;
    private String lastQuestion = "";
    private Random random = new Random();

    private TextView questionTextView;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api);

        // Initialize UI components
        questionTextView = (TextView) findViewById(R.id.question_text_view);
        Button btnBack = (Button) findViewById(R.id.btn_back);
        Button btnRepeat = (Button) findViewById(R.id.btn_repeat);
        Button btnReset = (Button) findViewById(R.id.btn_reset);

        // Initialize SpeechManager
        initializeSpeechManager();

        // Back Button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to the previous activity
            }
        });

        // Repeat Button
        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!lastQuestion.isEmpty()) {
                    retryCount = 0; // Reset retries
                    readQuestionAloud(lastQuestion);
                }
            }
        });

        // Reset Button
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryCount = 0; // Reset retries
                generateNewQuestion();
            }
        });
    }

    private void initializeSpeechManager() {
        mSpeechManager = new SpeechManager(this, new SpeechManager.OnConnectListener() {
            @Override
            public void onConnect(boolean status) {
                if (status) {
                    Log.d("APIActivity", "SpeechManager initialized successfully!");
                    mSpeechManager.setTtsEnable(true); // Enable TTS
                    isSpeechManagerInitialized = true;

                    // Generate and speak the initial question after initialization
                    generateNewQuestion();
                } else {
                    Log.e("APIActivity", "Failed to initialize SpeechManager.");
                }
            }
        }, "com.avatar.dialog");
    }

    private void generateNewQuestion() {
        if (!isSpeechManagerInitialized) {
            Log.e("APIActivity", "SpeechManager not initialized yet. Cannot generate question.");
            return;
        }

        int num1 = random.nextInt(10) + 1;
        int num2 = random.nextInt(10) + 1;
        correctAnswer = num1 * num2;

        lastQuestion = "What is " + num1 + " times " + num2 + "?";
        questionTextView.setText(lastQuestion);

        // Read the new question aloud and start listening
        readQuestionAloud(lastQuestion);
    }

    private void readQuestionAloud(final String question) {
        if (isSpeechManagerInitialized && mSpeechManager.getTtsEnable()) {
            try {
                mSpeechManager.forceStartSpeaking(question, false, false);
                Log.d("APIActivity", "Robot speaking: " + question);

                // Estimate delay based on speaking duration
                int estimatedDelay = Math.max(question.length() * SPEECH_DELAY_MULTIPLIER, 3000); // Minimum delay of 3 seconds

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (retryCount < MAX_RETRIES) {
                            startListening();
                        } else {
                            Log.e("APIActivity", "Maximum retries reached. Stopping further attempts.");
                        }
                    }
                }, estimatedDelay);
            } catch (Exception e) {
                Log.e("APIActivity", "Error while speaking: ", e);
            }
        } else {
            Log.e("APIActivity", "SpeechManager not initialized or TTS disabled.");
        }
    }

    private void startListening() {
        Log.d("APIActivity", "Start listening called."); // Add this log
        if (retryCount >= MAX_RETRIES) {
            Log.e("APIActivity", "Maximum retries reached. Ending interaction.");
            readQuestionAloud("I couldn't understand your response. Please ask for help.");
            return;
        }

        retryCount++;
        RobotAPIHelper.startSpeechRecognition(); // Start speech recognition via API
        Log.d("APIActivity", "Speech recognition started."); // Add this log

        // Poll for speech result
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000); // Wait for the speech recognition to complete
                    final String result = RobotAPIHelper.fetchSpeechResult(); // Fetch the result from the API

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result != null) {
                                processUserAnswer(result);
                            } else {
                                Log.e("APIActivity", "No result received.");
                                if (retryCount < MAX_RETRIES) {
                                    readQuestionAloud("I didn't hear you. Please try again.");
                                }
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    Log.e("APIActivity", "Error while waiting for result: ", e);
                }
            }
        }).start();
    }

    private void processUserAnswer(String userResponse) {
        try {
            int userAnswer = Integer.parseInt(userResponse.replaceAll("[^0-9]", ""));
            if (userAnswer == correctAnswer) {
                retryCount = 0; // Reset retries on success
                readQuestionAloud("Correct! Well done!");
            } else {
                retryCount = 0; // Reset retries on a complete response
                readQuestionAloud("That's incorrect. The correct answer is " + correctAnswer + ".");
            }
        } catch (NumberFormatException e) {
            Log.e("APIActivity", "Invalid response: " + userResponse, e);
            if (retryCount < MAX_RETRIES) {
                readQuestionAloud("I couldn't understand your answer. Please try again.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RobotAPIHelper.stopSpeechRecognition(); // Stop speech recognition if still active
    }
}
