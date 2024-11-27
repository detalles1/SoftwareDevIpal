package com.example.androidmultiplyapplication;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.util.Log;

public class ModeSelectionActivity extends Activity {
    private int selectedNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selection);

        // Retrieve the number from the intent
        selectedNumber = getIntent().getIntExtra("number", -1);
        if (selectedNumber == -1) {
            Log.e("ModeSelectionActivity", "Number extra is missing from intent!");
            finish(); // Exit if missing to avoid crashes
            return;
        }

        // Back button to go back to MainActivity
        Button backButton = (Button) findViewById(R.id.btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // End this activity and return to the previous one
            }
        });

        // Set up Shuffle button
        Button shuffleButton = (Button) findViewById(R.id.btn_shuffle);
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchTableActivity(true); // Pass "true" for shuffle mode
            }
        });

        // Set up In Order button
        Button inOrderButton = (Button) findViewById(R.id.btn_in_order);
        inOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchTableActivity(false); // Pass "false" for in order mode
            }
        });
    }

    private void launchTableActivity(boolean isShuffle) {
        Intent intent = new Intent(ModeSelectionActivity.this, TableActivity.class);
        intent.putExtra("number", selectedNumber);
        intent.putExtra("isShuffle", isShuffle);
        startActivity(intent);
        finish();
    }
}
