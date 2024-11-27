package com.example.avatarmind.robotmotion;

import android.app.Activity;
import android.os.Bundle;
import android.robot.motion.RobotMotion;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class HeadActivity extends Activity implements OnClickListener {

    private ImageView mTitleBack;

    private Button mNodBtn;

    private Button mShakeBtn;

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
        setContentView(R.layout.activity_head);

        mTitleBack = (ImageView) findViewById(R.id.common_title_back);
        TextView title = (TextView) findViewById(R.id.common_title_text);
        title.setText(R.string.unit_head);

        mNodBtn = (Button) findViewById(R.id.head_nod);
        mShakeBtn = (Button) findViewById(R.id.head_shake);
    }

    private void initListener() {
        mTitleBack.setOnClickListener(this);
        mNodBtn.setOnClickListener(this);
        mShakeBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.common_title_back:
                finish();
                break;
            case R.id.head_nod:
                mRobotMotion.nodHead();
                break;
            case R.id.head_shake:
                mRobotMotion.shakeHead();
                break;
            default:
                break;
        }

    }

}
