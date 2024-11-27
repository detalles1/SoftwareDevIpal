package com.example.apimultiplyapplication;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.robot.speech.SpeechManager;
import android.robot.motion.RobotMotion;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TableActivity extends Activity {

    private SpeechManager mSpeechManager;
    private RobotMotion mRobotMotion = new RobotMotion();
    private TextView resultView;
    private int number;
    private Handler handler = new Handler();
    private List<Integer> questions;
    private int currentIndex = 0;
    private final int speechDelay = 3500;

    private RelativeLayout layout;
    private final int[] colors = {Color.WHITE, Color.YELLOW, Color.CYAN, Color.GREEN};
    private int colorIndex = 0;
    private final int minBlinkInterval = 2000;
    private final int maxBlinkInterval = 10000;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        // Initialize layout and result view
        layout = (RelativeLayout)findViewById(R.id.relative_layout);
        resultView = (TextView)findViewById(R.id.result_view);
        if (layout == null || resultView == null) {
            Log.e("TableActivity", "Layout or result view is missing in activity_table.xml!");
            finish();
            return;
        }

        // Retrieve intent extras
        number = getIntent().getIntExtra("number", -1);
        boolean isShuffle = getIntent().getBooleanExtra("isShuffle", true);
        if (number == -1) {
            Log.e("TableActivity", "Number extra is missing!");
            finish();
            return;
        }

        // Initialize SpeechManager
        mSpeechManager = (SpeechManager) getSystemService("speech");
        if (mSpeechManager != null) {
            mSpeechManager.setTtsEnable(true);
        }

        initializeQuestions(isShuffle);
        displayNextQuestion();
    }

    private void initializeQuestions(boolean isShuffle) {
        questions = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            questions.add(i);
        }
        if (isShuffle) {
            Collections.shuffle(questions);
        }
    }

    private void displayNextQuestion() {
        if (currentIndex < questions.size()) {
            int i = questions.get(currentIndex);
            int result = number * i;
            final String equation = number + " * " + i + " = " + result;
            resultView.setText(equation);

            if (mSpeechManager != null) {
                mSpeechManager.forceStartSpeaking(equation);
            }

            currentIndex++;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    displayNextQuestion();
                }
            }, speechDelay);
        } else {
            finishWithPrompt();
        }
    }

    private void finishWithPrompt() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSpeechManager != null) {
                    mSpeechManager.forceStartSpeaking("Let's practice multiplication again.");
                }
                finish();
            }
        }, 1000);
    }
}
