package com.example.multipyapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.robot.motion.RobotMotion;
import android.robot.speech.SpeechManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class TableActivity extends Activity {

    private SpeechManager mSpeechManager;
    private RobotMotion mRobotMotion = new RobotMotion();
    private TextView resultView;
    private Button btnBack, btnReset, btnRepeat;

    private Handler handler = new Handler();
    private List<Integer> questions;
    private int currentIndex = 0;
    private int correctAnswer;
    private boolean isSpeechManagerInitialized = false;

    private static final int SPEECH_REQUEST_CODE = 100;
    private static final String TAG = "TableActivity";

    private int number; // Number passed from MainActivity
    private boolean isGameMode; // Flag for Game Mode
    private boolean isShuffleMode; // Flag for Shuffle Mode
    private Random random = new Random();


    // Word-to-number map for parsing answers
    private static final Map<String, Integer> wordToNumberMap = new HashMap<>();

    static {
        initializeWordToNumberMap();
    }

    private static void initializeWordToNumberMap() {
        String[] basicNumbers = {
                "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"
        };

        // Map basic numbers to their values
        for (int i = 0; i <= 10; i++) {
            wordToNumberMap.put(basicNumbers[i], i);
        }

        // Add common misinterpretations or variations
        wordToNumberMap.put("o", 0); // Sometimes "o" is said instead of "zero"
        wordToNumberMap.put("oh", 0); // Alternative for zero
        wordToNumberMap.put("won", 1); // Phonetic misinterpretation of "one"
        wordToNumberMap.put("to", 2); // Phonetic misinterpretation of "two"
        wordToNumberMap.put("too", 2); // Phonetic misinterpretation of "two"
        wordToNumberMap.put("tree", 3); // Common mispronunciation of "three"
        wordToNumberMap.put("for", 4); // Common misinterpretation of "four"
        wordToNumberMap.put("fore", 4); // Alternative spelling for "four"
        wordToNumberMap.put("ate", 8); // Misinterpretation of "eight"
        wordToNumberMap.put("nigh", 9); // Phonetic misinterpretation
        wordToNumberMap.put("night", 9); // Common misrecognition
        wordToNumberMap.put("nine.", 9); // Recognizer may add punctuation
        wordToNumberMap.put("9", 9); // Handle recognized digit instead of word
        wordToNumberMap.put("won", 1); // Phonetic misinterpretation of one
        wordToNumberMap.put("sixth", 6); // Common misinterpretation of six
        wordToNumberMap.put("sex", 6); // Phonetic misinterpretation of six

        // Add tens mapping
        String[] tens = {"", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
        for (int i = 2; i <= 9; i++) {
            wordToNumberMap.put(tens[i], i * 10);
        }

        // Add teens mapping
        String[] teens = {"eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};
        for (int i = 0; i < teens.length; i++) {
            wordToNumberMap.put(teens[i], 11 + i);
        }

        // Add compound numbers
        for (int i = 2; i <= 9; i++) {
            for (int j = 1; j <= 9; j++) {
                String compound = tens[i] + " " + basicNumbers[j];
                wordToNumberMap.put(compound, i * 10 + j);
            }
        }

        // Add "one hundred"
        wordToNumberMap.put("one hundred", 100);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        resultView = (TextView) findViewById(R.id.result_view);
        btnBack = (Button) findViewById(R.id.btn_back);
        btnReset = (Button) findViewById(R.id.btn_reset);
        btnRepeat = (Button) findViewById(R.id.btn_repeat);

        initializeSpeechManager();

        // Set up the Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Finish the activity and return to the previous screen
            }
        });

        // Set up the Reset button
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame(); // Reset the game
            }
        });

        // Set up the Repeat button
        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repeatCurrentQuestion(); // Repeat the current question
            }
        });

        // Set up the Enter Answer button
        Button btnEnterAnswer = (Button) findViewById(R.id.btn_enter_answer);
        btnEnterAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNumberPadDialog(); // Open a number pad dialog for manual input
            }
        });

        // Set up the Shuffle button
        Button btnShuffle = (Button) findViewById(R.id.btn_shuffle);
        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShuffleMode = true; // Enable shuffle mode
                initializeQuestions(); // Reinitialize questions in shuffled order
                displayNextQuestion(); // Start displaying the first question
            }
        });

        // Set up the In Order button
        Button btnInOrder = (Button) findViewById(R.id.btn_in_order);
        btnInOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShuffleMode = false; // Disable shuffle mode
                initializeQuestions(); // Reinitialize questions in sequential order
                displayNextQuestion(); // Start displaying the first question
            }
        });

        // Retrieve parameters from MainActivity
        number = getIntent().getIntExtra("number", -1);
        isGameMode = getIntent().getBooleanExtra("isGameMode", false);
        isShuffleMode = getIntent().getBooleanExtra("isShuffle", false);

        if (isGameMode) {
            moveTextViewToTopIfGameMode();
        }

        if (number == -1) {
            Log.e(TAG, "Number not passed from MainActivity. Exiting...");
            finish();
            return;
        }

        // Initialize the questions and start the first one
        initializeQuestions();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                displayNextQuestion();
            }
        }, 1000);
    }

    private void initializeSpeechManager() {
        try {
            mSpeechManager = new SpeechManager(this, new SpeechManager.OnConnectListener() {
                @Override
                public void onConnect(boolean status) {
                    if (status) {
                        mSpeechManager.setTtsEnable(true);
                        isSpeechManagerInitialized = true;
                        Log.d(TAG, "SpeechManager initialized successfully.");
                    } else {
                        isSpeechManagerInitialized = false;
                        Log.e(TAG, "Failed to initialize SpeechManager.");
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "SpeechManager initialization failed: " + e.getMessage());
        }
    }

    private void initializeQuestions() {
        questions = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            questions.add(i); // Populate the list with numbers 1 through 10
        }

        if (isShuffleMode) {
            Collections.shuffle(questions); // Shuffle only for Shuffle mode
        }
        currentIndex = 0; // Reset index to start from the first question
    }

    private void displayNextQuestion() {
        if (currentIndex >= questions.size()) {
            finishGame(); // End the game if all questions are answered
            return;
        }

        int multiplier = questions.get(currentIndex);
        correctAnswer = number * multiplier;

        String equation = isGameMode
                ? "What is " + number + " times " + multiplier + "?"
                : number + " times " + multiplier + " equals " + correctAnswer;

        resultView.setText(equation);

        if (mSpeechManager != null && isSpeechManagerInitialized) {
            mSpeechManager.forceStartSpeaking(equation, false, false);
        }

        if (isGameMode) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startListeningForAnswer();
                }
            }, 3000);
        } else {
            currentIndex++;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    displayNextQuestion();
                }
            }, 4000);
        }
    }

    private void repeatCurrentQuestion() {
        if (currentIndex >= questions.size() || currentIndex < 0) {
            speakQuestion("No question to repeat.", false);
            return;
        }

        // Get the current multiplier
        int multiplier = questions.get(currentIndex);
        String equation = isGameMode
                ? "What is " + number + " times " + multiplier + "?"
                : number + " times " + multiplier + " equals " + correctAnswer;

        // Display and speak the question
        resultView.setText(equation);

        if (mSpeechManager != null && isSpeechManagerInitialized) {
            mSpeechManager.forceStartSpeaking(equation, false, false);

            // If in Game Mode, wait for user input
            if (isGameMode) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startListeningForAnswer(); // Trigger speech recognition
                    }
                }, equation.split("\\s+").length * 400); // Wait based on question length
            }
        }
    }

    private void resetGame() {
        currentIndex = 0; // Reset the question index
        initializeQuestions(); // Re-initialize questions

        if (isShuffleMode) {
            Collections.shuffle(questions); // Shuffle for random order
        }

        if (isGameMode) {
            // Generate a new random multiplier for Game Mode
            int multiplier = random.nextInt(10) + 1; // Ensure random is initialized
            correctAnswer = number * multiplier;
            String equation = "What is " + number + " times " + multiplier + "?";

            resultView.setText(equation);

            if (mSpeechManager != null && isSpeechManagerInitialized) {
                mSpeechManager.forceStartSpeaking(equation, false, false);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startListeningForAnswer(); // Activate mic for response
                    }
                }, equation.split("\\s+").length * 400);
            }
        } else {
            // For In Order or Shuffle mode, display the next question
            displayNextQuestion();
        }
    }

    private void startListeningForAnswer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your answer, like 'The answer is nine'.");

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Log.e(TAG, "Google Speech Recognition is not available.", e);
            speakQuestion("Google Speech Recognition is not available on this device.", false);
        }
    }

    private void processUserAnswer(String userResponse) {
        userResponse = normalizeResult(userResponse); // Normalize the response
        Log.d(TAG, "Normalized user response: " + userResponse); // Debugging log

        int userAnswer = -1;
        try {
            // Attempt to parse directly as a number
            userAnswer = Integer.parseInt(userResponse);
            Log.d(TAG, "Parsed numeric answer: " + userAnswer);
        } catch (NumberFormatException e) {
            // Extract possible numbers from phrases
            String[] words = userResponse.split("\\s+");
            StringBuilder parsedNumber = new StringBuilder();
            for (String word : words) {
                if (wordToNumberMap.containsKey(word)) {
                    parsedNumber.append(wordToNumberMap.get(word)); // Concatenate number parts
                }
            }

            if (!parsedNumber.toString().isEmpty()) {
                try {
                    userAnswer = Integer.parseInt(parsedNumber.toString());
                    Log.d(TAG, "Mapped and parsed answer: " + userAnswer);
                } catch (NumberFormatException ex) {
                    Log.e(TAG, "Failed to parse concatenated number: " + parsedNumber);
                }
            }
        }

        // Check the parsed answer against the correct answer
        if (userAnswer == correctAnswer) {
            showPositiveFeedback();
        } else if (userAnswer != -1) {
            showNegativeFeedback();
        } else {
            speakQuestion("I couldn't understand your answer. Please try again.", false);
        }
    }

    private void speakQuestion(String phrase, boolean startListeningAfter) {
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
        } else {
            Log.e(TAG, "Text-to-Speech is not enabled.");
        }
    }

    private void showPositiveFeedback() {
        // Show smile emoji
        mRobotMotion.emoji((int) RobotMotion.Emoji.SMILE);

        // Nod the robot's head
        mRobotMotion.nodHead();

        // Speak a positive response
        if (mSpeechManager != null && isSpeechManagerInitialized) {
            mSpeechManager.forceStartSpeaking("Correct! Well done!", false, false);
        }

        // Increment index for the next question
        currentIndex++;

        // Delay before moving to the next question
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                displayNextQuestion();
            }
        }, 2000); // Adjust delay as needed
    }

    private String normalizeResult(String input) {
        if (input == null || input.isEmpty()) return "";
        return input.trim().toLowerCase(); // Convert to lowercase for uniformity
    }


    private void showNegativeFeedback() {
        // Show sad emoji
        mRobotMotion.emoji((int) RobotMotion.Emoji.SAD);

        // Shake the robot's head
        mRobotMotion.shakeHead();

        // Speak the correct answer
        if (mSpeechManager != null && isSpeechManagerInitialized) {
            String response = "Incorrect. The correct answer is " + correctAnswer + ".";
            mSpeechManager.forceStartSpeaking(response, false, false);
        }

        // Delay before moving to the next question
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                displayNextQuestion();
            }
        }, 2000); // Adjust delay as needed
    }


    private void finishGame() {
        mSpeechManager.forceStartSpeaking("Great job! You've completed the activity.");
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (results != null && !results.isEmpty()) {
                Log.d(TAG, "Raw recognized results: " + results); // Log the raw results
                processUserAnswer(results.get(0)); // Process the first result
            } else {
                Log.e(TAG, "No recognition results received.");
            }
        }
    }

    private void moveTextViewToTopIfGameMode() {
        if (isGameMode) {
            // Get current layout parameters of the TextView
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) resultView.getLayoutParams();

            // Remove conflicting alignment rules
            params.addRule(RelativeLayout.CENTER_IN_PARENT, 0); // Remove center alignment
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE); // Align to the top

            // Center horizontally between Back and Repeat buttons
            params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE); // Center horizontally

            // Apply the updated parameters
            resultView.setLayoutParams((RelativeLayout.LayoutParams) params);
        }
    }
    private void openNumberPadDialog() {
        // Create a simple input dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Your Answer");

        // Add an EditText for input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER); // Restrict to numbers
        builder.setView(input);

        // Add "OK" and "Cancel" buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userInput = input.getText().toString().trim();
                processManualAnswer(userInput); // Process the manual input
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel(); // Close dialog
            }
        });

        builder.show();
    }

    private void processManualAnswer(String userInput) {
        try {
            int userAnswer = Integer.parseInt(userInput); // Parse input as integer
            if (userAnswer == correctAnswer) {
                showPositiveFeedback();
            } else {
                showNegativeFeedback();
            }
        } catch (NumberFormatException e) {
            speakQuestion("Invalid input. Please enter a number.", false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRobotMotion.emoji((int) RobotMotion.Emoji.DEFAULT);
    }
}
