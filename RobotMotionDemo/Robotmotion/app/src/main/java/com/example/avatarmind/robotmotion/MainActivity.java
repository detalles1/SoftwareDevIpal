package com.example.avatarmind.robotmotion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity implements View.OnClickListener {

    private ImageView mTitleBack;

    private Button mBtnMotors;

    private Button mBtnHead;

    private Button mBtnWheel;

    private Button mBtnEmoji;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().hide();
        }

        initView();
        initListener();
    }

    private void initView() {
        setContentView(R.layout.activity_main);

        mTitleBack = (ImageView) findViewById(R.id.common_title_back);
        mBtnMotors = (Button) findViewById(R.id.main_motors);
        mBtnHead = (Button) findViewById(R.id.main_head);
        mBtnWheel = (Button) findViewById(R.id.main_wheel);
        mBtnEmoji = (Button) findViewById(R.id.main_emoji);
    }

    private void initListener() {
        mTitleBack.setOnClickListener(this);
        mBtnMotors.setOnClickListener(this);
        mBtnHead.setOnClickListener(this);
        mBtnWheel.setOnClickListener(this);
        mBtnEmoji.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.main_motors:
                intent.setClass(MainActivity.this, MotorsActivity.class);
                break;
            case R.id.main_head:
                intent.setClass(MainActivity.this, HeadActivity.class);
                break;
            case R.id.main_wheel:
                intent.setClass(MainActivity.this, WheelActivity.class);
                break;
            case R.id.main_emoji:
                intent.setClass(MainActivity.this, EmojiActivity.class);
                break;
            case R.id.common_title_back:
                finish();
                return;
            default:
                break;
        }
        startActivity(intent);
    }
}
