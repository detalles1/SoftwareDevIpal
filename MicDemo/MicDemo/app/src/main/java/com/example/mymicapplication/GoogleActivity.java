package com.example.mymicapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.robot.motion.RobotMotion;
import android.robot.speech.SpeechManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class GoogleActivity extends Activity {

    private SpeechManager mSpeechManager;
    private RobotMotion mRobotMotion = new RobotMotion(); // RobotMotion instance
    private Handler handler = new Handler();
    private Random random = new Random();
    private int correctAnswer;
    private String lastQuestion = "";
    private boolean isSpeechManagerInitialized = false;

    // UI Components
    private TextView questionTextView;
    private Button btnReset, btnRepeat, btnBack;

    private static final String TAG = "GoogleActivity";
    private static final int SPEECH_REQUEST_CODE = 100;

    // Word-to-number map
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);

        questionTextView = (TextView) findViewById(R.id.question_text_view);
        btnReset = (Button) findViewById(R.id.btn_reset);
        btnRepeat = (Button) findViewById(R.id.btn_repeat);
        btnBack = (Button) findViewById(R.id.btn_back);

        initializeSpeechManager();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startMultiplicationGame();
            }
        }, 1000);

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetRobotState();
                startMultiplicationGame();
            }
        });

        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetRobotState();
                speakQuestion(lastQuestion, false);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetRobotState();
                finish();
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
                } else {
                    Log.e(TAG, "Failed to initialize SpeechManager.");
                    isSpeechManagerInitialized = false;
                }
            }
        });
    }

    private void startMultiplicationGame() {
        if (!isSpeechManagerInitialized) {
            Log.e(TAG, "SpeechManager not initialized yet. Delaying game start.");
            return;
        }

        int num1 = random.nextInt(10) + 1;
        int num2 = random.nextInt(10) + 1;
        correctAnswer = num1 * num2;

        lastQuestion = "What is " + num1 + " times " + num2 + "?";
        questionTextView.setText(lastQuestion);

        speakQuestion(lastQuestion, true);
    }

    private void startListeningForAnswer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please speak your answer...");

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Log.e(TAG, "Google Speech Recognition is not available.", e);
            speakQuestion("Google Speech Recognition is not available on this device.", false);
        }
    }

    private void processUserAnswer(String userResponse) {
        try {
            String cleanedResponse = userResponse.trim();
            int userAnswer = parseWordNumber(cleanedResponse);

            if (userAnswer == correctAnswer) {
                showPositiveFeedback();
            } else {
                showNegativeFeedback();
            }
        } catch (NumberFormatException e) {
            speakQuestion("I couldn't understand your answer. Please try again.", false);
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

        throw new NumberFormatException("Invalid word-based number: " + input);
    }

    private void speakQuestion(final String phrase, final boolean startListeningAfter) {
        if (mSpeechManager != null && mSpeechManager.getTtsEnable()) {
            mSpeechManager.forceStartSpeaking(phrase, false, false);
            if (startListeningAfter) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startListeningForAnswer();
                    }
                }, phrase.split("\\s+").length * 400);
            }
        }
    }

    private void showPositiveFeedback() {
        mRobotMotion.emoji((int) RobotMotion.Emoji.SMILE); // Smile emoji
        mRobotMotion.nodHead(); // Nod motion

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                speakQuestion("Correct! Well done!", false);
                resetRobotState();
            }
        }, 2000); // Delay for nodding
    }

    private void showNegativeFeedback() {
        mRobotMotion.emoji((int) RobotMotion.Emoji.SAD); // Sad emoji
        mRobotMotion.shakeHead(); // Shake motion

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                speakQuestion("That's incorrect. The correct answer is " + correctAnswer + ".", false);
                resetRobotState();
            }
        }, 2000); // Delay for shaking head
    }

    private void resetRobotState() {
        mRobotMotion.emoji((int) RobotMotion.Emoji.DEFAULT); // Reset emoji
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (results != null && !results.isEmpty()) {
                    processUserAnswer(results.get(0));
                }
            } else {
                speakQuestion("I couldn't process your speech. Please try again.", false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetRobotState();
    }
}
