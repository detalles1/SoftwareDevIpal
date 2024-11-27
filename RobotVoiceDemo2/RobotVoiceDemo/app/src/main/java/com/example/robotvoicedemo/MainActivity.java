package com.example.robotvoicedemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.robot.speech.SpeechManager;
import android.robot.speech.SpeechManager.AsrListener;
import android.robot.speech.SpeechManager.NluListener;
import android.robot.speech.SpeechManager.TtsListener;
import android.robot.speech.SpeechService;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private ImageView mBtnBack;

    private TextView mTTSStatus;

    private Button mBtnTTSStart;

    private EditText mTTSContent;

    private InputMethodManager mInputMethodManager;

    private SpeechManager mSpeechManager;



    private TtsListener mTtsListener = new TtsListener() {
        @Override
        public void onBegin(int requestId) {
            mTTSStatus.setText(getString(R.string.tts_start_speaking)
                    + requestId);
        }

        @Override
        public void onEnd(int requestId) {
            mTTSStatus.setText(getString(R.string.tts_stop_speaking)
                    + requestId);
        }

        @Override
        public void onError(int error) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        initData();
        initView();
        initListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSpeechManager.setTtsListener(null);
    }

    private void initData() {
        mSpeechManager = (SpeechManager) getSystemService(SpeechService.SERVICE_NAME);
        mInputMethodManager = (InputMethodManager) this.getApplicationContext()
                .getSystemService(Context
                        .INPUT_METHOD_SERVICE);
    }

    private void initView() {
        mBtnBack = (ImageView) findViewById(R.id.common_title_back);
        mTTSContent = (EditText) findViewById(R.id.et_tts_content);
        mTTSStatus = (TextView) findViewById(R.id.tv_tts_status);
        mBtnTTSStart = (Button) findViewById(R.id.btn_tts_start);

        mBtnBack.setOnClickListener(this);
        mBtnTTSStart.setOnClickListener(this);

    }

    private void initListener() {
        mSpeechManager.setTtsListener(mTtsListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.common_title_back:
                finish();
                break;
            case R.id.btn_tts_start:
                mInputMethodManager.hideSoftInputFromWindow(
                        mTTSContent.getWindowToken(), 0);
                String tts = mTTSContent.getText().toString();
                if (!TextUtils.isEmpty(tts)) {
                    mSpeechManager.startSpeaking(tts);
                }
                break;
            default:
                break;
        }
    }
}
