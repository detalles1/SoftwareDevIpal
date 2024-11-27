package com.example.avatarmind.robotmotion;

import android.app.Activity;
import android.os.Bundle;
import android.robot.motion.RobotMotion;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class WheelActivity extends Activity implements OnClickListener {

    private ImageView mTitleBack;

    private EditText mEtDistance;

    private EditText mEtAngle;

    private Button mBtnDistance;

    private Button mBtnAngle;

    private RobotMotion mRobotMotion = new RobotMotion();

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
        setContentView(R.layout.activity_wheel);

        mTitleBack = (ImageView) findViewById(R.id.common_title_back);
        TextView title = (TextView) findViewById(R.id.common_title_text);
        title.setText(R.string.unit_wheel);

        mEtDistance = (EditText) findViewById(R.id.wheel_distance);
        mEtAngle = (EditText) findViewById(R.id.wheel_angle);

        mBtnDistance = (Button) findViewById(R.id.wheel_walk);
        mBtnAngle = (Button) findViewById(R.id.wheel_turn);
    }

    private void initListener() {
        mTitleBack.setOnClickListener(this);
        mBtnDistance.setOnClickListener(this);
        mBtnAngle.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.common_title_back:
                finish();
                break;
            case R.id.wheel_walk:
                String length = mEtDistance.getText().toString();
                if (!TextUtils.isEmpty(length)) {
                    int distance = Integer.parseInt(length);
                    mRobotMotion.startWalk(distance, 1, 0);
                }
                break;
            case R.id.wheel_turn:
                String degree = mEtAngle.getText().toString();
                if (!TextUtils.isEmpty(degree)) {
                    int angle = Integer.parseInt(degree);
                    mRobotMotion.turn(angle, 2);
                }
                break;
            default:
                break;
        }
    }

}
