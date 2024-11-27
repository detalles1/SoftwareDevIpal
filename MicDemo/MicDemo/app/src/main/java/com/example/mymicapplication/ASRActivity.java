package com.example.mymicapplication;

import android.app.Activity;
import android.os.Bundle;
import android.robot.speech.SpeechManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

public class ASRActivity extends Activity {

    private SpeechManager mSpeechManager;
    private boolean isSpeechManagerInitialized = false;
    private int correctAnswer;
    private String lastQuestion = "";
    private Random random = new Random();

    private TextView questionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asr); // Use the layout for ASRActivity

        // Initialize UI components
        questionTextView = (TextView)findViewById(R.id.question_text_view);
        Button btnBack = (Button)findViewById(R.id.btn_back);
        Button btnRepeat = (Button)findViewById(R.id.btn_repeat);
        Button btnReset = (Button)findViewById(R.id.btn_reset);

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
                    readQuestionAloud(lastQuestion);
                    waitForAnswer();
                }
            }
        });

        // Reset Button
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateNewQuestion();
            }
        });
    }

    private void initializeSpeechManager() {
        mSpeechManager = new SpeechManager(this, new SpeechManager.OnConnectListener() {
            @Override
            public void onConnect(boolean status) {
                if (status) {
                    Log.d("ASRActivity", "SpeechManager initialized successfully!");
                    mSpeechManager.setTtsEnable(true); // Enable TTS
                    isSpeechManagerInitialized = true;

                    // Generate the initial question
                    generateNewQuestion();
                } else {
                    Log.e("ASRActivity", "Failed to initialize SpeechManager.");
                    questionTextView.setText("Error initializing SpeechManager.");
                }
            }
        }, "com.avatar.dialog");
    }

    private void generateNewQuestion() {
        if (!isSpeechManagerInitialized) {
            Log.e("ASRActivity", "SpeechManager is not initialized. Cannot generate a new question.");
            return;
        }

        int num1 = random.nextInt(10) + 1;
        int num2 = random.nextInt(10) + 1;
        correctAnswer = num1 * num2;

        lastQuestion = "What is " + num1 + " times " + num2 + "?";
        questionTextView.setText(lastQuestion);

        // Read the new question aloud and wait for an answer
        readQuestionAloud(lastQuestion);
        waitForAnswer();
    }

    private void readQuestionAloud(String question) {
        if (isSpeechManagerInitialized && mSpeechManager.getTtsEnable()) {
            mSpeechManager.forceStartSpeaking(question, false, false);
            Log.d("ASRActivity", "Reading question aloud: " + question);
        } else {
            Log.e("ASRActivity", "Cannot read question. SpeechManager not initialized or TTS disabled.");
        }
    }

    private void waitForAnswer() {
        mSpeechManager.setAsrListener(new SpeechManager.AsrListener() {
            @Override
            public void onBegin() {
                Log.d("ASRActivity", "Listening for answer...");
            }

            @Override
            public void onVolumeChanged(float volume) {
                Log.d("ASRActivity", "Volume level: " + volume);
            }

            @Override
            public boolean onResult(String result) {
                Log.d("ASRActivity", "ASR Result: " + result);
                processUserAnswer(result);
                return true;
            }

            @Override
            public void onError(int errorCode) {
                Log.e("ASRActivity", "ASR Error Code: " + errorCode);
                readQuestionAloud("I didn't hear you. Please try again.");
                waitForAnswer(); // Retry listening
            }

            @Override
            public void onEnd() {
                Log.d("ASRActivity", "Listening session ended.");
            }
        });

        mSpeechManager.startListening();
    }

    private void processUserAnswer(String userResponse) {
        try {
            int userAnswer = Integer.parseInt(userResponse.replaceAll("[^0-9]", ""));
            if (userAnswer == correctAnswer) {
                readQuestionAloud("Correct! Well done!");
            } else {
                readQuestionAloud("That's incorrect. The correct answer is " + correctAnswer + ".");
            }
        } catch (NumberFormatException e) {
            Log.e("ASRActivity", "Invalid response: " + userResponse, e);
            readQuestionAloud("I couldn't understand your answer. Please try again.");
            waitForAnswer(); // Retry listening
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSpeechManager != null) {
            mSpeechManager = null;
            Log.d("ASRActivity", "SpeechManager cleaned up.");
        }
    }
}
