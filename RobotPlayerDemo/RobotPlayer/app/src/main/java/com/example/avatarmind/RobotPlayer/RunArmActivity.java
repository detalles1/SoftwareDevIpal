package com.example.avatarmind.RobotPlayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.robot.hw.RobotDevices;
import android.robot.motion.RobotMotion;
import android.robot.motion.RobotPlayer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;


public class RunArmActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "RunArmActivity";

    private RobotPlayer mRobotPlayer;

    private RobotMotion mRobotMotion;

    private ImageView mTitleBack;

    private Button mRunBtn;

    private Button mPauseBtn;

    private Button mResumeBtn;

    private Button mStopBtn;

    private int mArmLen = 0;

    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().hide();
        }

        mRobotPlayer = new RobotPlayer();
        mRobotMotion = new RobotMotion();
        Intent intent = getIntent();
        mPosition = intent.getIntExtra("Mode", -1);

        if (mPosition != -1) {
            if (mPosition == 0) {
                mRobotPlayer.setDataSource("/sdcard/media/test.arm");

            } else if (mPosition == 1) {
                mRobotPlayer.setDataSource(getFromAssets("test.arm"), 0, mArmLen);

            }

            mRobotPlayer.prepare();
            initView();
            initListener();
        }
    }

    private void initView() {
        setContentView(R.layout.activity_arm);

        mTitleBack = (ImageView) findViewById(R.id.common_title_back);
        TextView title = (TextView) findViewById(R.id.common_title_text);

        if (mPosition == 0) {
            title.setText("Run .arm Files By File");
        } else if (mPosition == 1) {
            title.setText("Run .arm Files By Streams");
        }

        mRunBtn = (Button) findViewById(R.id.run);
        mPauseBtn = (Button) findViewById(R.id.pause);
        mResumeBtn = (Button) findViewById(R.id.resume);
        mStopBtn = (Button) findViewById(R.id.stop);

    }

    private void initListener() {
        mTitleBack.setOnClickListener(this);
        mRunBtn.setOnClickListener(this);
        mPauseBtn.setOnClickListener(this);
        mResumeBtn.setOnClickListener(this);
        mStopBtn.setOnClickListener(this);
    }

    private byte[] getFromAssets(String fileName) {
        try {
            InputStream in = getResources().getAssets().open(fileName);
            mArmLen = in.available();
            byte[] buffer = new byte[mArmLen];
            in.read(buffer);
            return buffer;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.common_title_back:
                finish();
                mRobotMotion.reset(RobotDevices.Units.ALL_MOTORS);
                break;

            case R.id.run:
                mRobotPlayer.start();
                break;

            case R.id.pause:
                mRobotPlayer.pause();
                break;

            case R.id.resume:
                mRobotPlayer.resume();
                break;

            case R.id.stop:
                mRobotPlayer.stop();
                break;

            default:
                break;
        }
    }
}
