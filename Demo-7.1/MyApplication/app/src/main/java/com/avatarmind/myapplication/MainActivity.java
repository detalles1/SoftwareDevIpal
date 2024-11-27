package com.avatarmind.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.robot.speech.SpeechManager;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener{

    private SpeechManager mSpeechManager;

    private Button mBtnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSpeechManager = (SpeechManager) this.getSystemService("speech");
        mSpeechManager.forceStartSpeaking("Hello World");

        mBtnBack = (Button) findViewById(R.id.btn_left);
        mBtnBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_left:
                finish();
                break;
        }

    }
}
