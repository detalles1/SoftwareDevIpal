package com.example.avatarmind.robotmotion;

import android.app.Activity;
import android.os.Bundle;
import android.robot.hw.RobotDevices;
import android.robot.motion.RobotMotion;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MotorsActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MotorsActivity";

    private ImageView mTitleBack;

    private TextView mRangeHint;

    private Spinner mDirection;

    private Spinner mAction;

    private EditText mAngle;

    private Button mBtnSubmit;

    private Button mBtnReset;

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
        setContentView(R.layout.activity_motors);

        mTitleBack = (ImageView) findViewById(R.id.common_title_back);
        TextView title = (TextView) findViewById(R.id.common_title_text);
        title.setText(R.string.unit_motors);

        mAngle = (EditText) findViewById(R.id.arm_degree);
        mBtnSubmit = (Button) findViewById(R.id.arm_confirm);
        mBtnReset = (Button) findViewById(R.id.motors_reset);
        mRangeHint = (TextView) findViewById(R.id.range_hint);

        mDirection = (Spinner) findViewById(R.id.arm_direction);
        String[] directionStr = getResources().getStringArray(R.array.arm_direction);
        ArrayAdapter<String> directionAdapter = new ArrayAdapter<String>
                (this, R.layout.spinner_item, directionStr);
        directionAdapter.setDropDownViewResource(R.layout.spinner_item);
        mDirection.setAdapter(directionAdapter);

        mAction = (Spinner) findViewById(R.id.arm_action);
        String[] actionStr = getResources().getStringArray(R.array.arm_action);
        ArrayAdapter<String> actionAdapter = new ArrayAdapter<String>
                (this, R.layout.spinner_item, actionStr);
        actionAdapter.setDropDownViewResource(R.layout.spinner_item);
        mAction.setAdapter(actionAdapter);
        mAction.setSelection(0, true);
    }

    private void initListener() {
        mTitleBack.setOnClickListener(this);
        mBtnSubmit.setOnClickListener(this);
        mBtnReset.setOnClickListener(this);

        mAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mRangeHint.setText(R.string.range_arm_rotation);
                        break;
                    case 1:
                        mRangeHint.setText(R.string.range_arm_swing);
                        break;
                    case 2:
                        mRangeHint.setText(R.string.range_forearm_rotation);
                        break;
                    case 3:
                        mRangeHint.setText(R.string.range_forearm_swing);
                        break;
                    case 4:
                        mRangeHint.setText(R.string.range_wrist);
                        break;
                    case 5:
                        mRangeHint.setText(R.string.range_arm_rotation);
                        break;
                    case 6:
                        mRangeHint.setText(R.string.range_arm_swing);
                        break;
                    case 7:
                        mRangeHint.setText(R.string.range_forearm_rotation);
                        break;
                    case 8:
                        mRangeHint.setText(R.string.range_forearm_swing);
                        break;
                    case 9:
                        mRangeHint.setText(R.string.range_wrist);
                        break;
                    case 10:
                        mRangeHint.setText(R.string.range_neck_rotation);
                        break;
                    case 11:
                        mRangeHint.setText(R.string.range_neck_tilt);
                        break;
                    default:
                        mRangeHint.setText("");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.common_title_back:
                finish();
                break;
            case R.id.arm_confirm:
                int action = mAction.getSelectedItemPosition();
                int direction = mDirection.getSelectedItemPosition();
                String degree = mAngle.getText().toString();
                int angle;
                if (!TextUtils.isEmpty(degree)) {
                    angle = Integer.parseInt(degree);
                } else {
                    Toast.makeText(this, R.string.null_angle, Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (tellInputLegality(action, direction, angle)) {
                    robotMove(action, direction, angle);
                } else {
                    Toast.makeText(this, R.string.wrong_angle, Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case R.id.motors_reset:
                mRobotMotion.reset((int) RobotDevices.Units.ALL_MOTORS);
                break;
            default:
                break;
        }
    }

    private void robotMove(int action, int direction, int angle) {
        if (direction == 1) {
            angle = angle - 2 * angle;
        }
        switch (action) {
            case 0:
                mRobotMotion.startMotor(
                        (int) RobotDevices.Motors.ARM_ROTATION_RIGHT, angle,
                        1000, 1);
                break;
            case 1:
                mRobotMotion.startMotor(
                        (int) RobotDevices.Motors.ARM_SWING_RIGHT, angle,
                        1000, 1);
                break;
            case 2:
                mRobotMotion.startMotor(
                        (int) RobotDevices.Motors.FOREARM_ROTATION_RIGHT,
                        angle, 1000, 1);
                break;
            case 3:
                mRobotMotion.startMotor(
                        (int) RobotDevices.Motors.FOREARM_SWING_RIGHT,
                        angle, 1000, 1);
                break;
            case 4:
                mRobotMotion.startMotor((int) RobotDevices.Motors.WRIST_RIGHT,
                        angle, 1000, 1);
                break;
            case 5:
                mRobotMotion.startMotor(
                        (int) RobotDevices.Motors.ARM_ROTATION_LEFT, angle,
                        1000, 1);
                break;
            case 6:
                mRobotMotion.startMotor(
                        (int) RobotDevices.Motors.ARM_SWING_LEFT, angle,
                        1000, 1);
                break;
            case 7:
                mRobotMotion.startMotor(
                        (int) RobotDevices.Motors.FOREARM_ROTATION_LEFT,
                        angle, 1000, 1);
                break;
            case 8:
                mRobotMotion.startMotor(
                        (int) RobotDevices.Motors.FOREARM_SWING_LEFT, angle,
                        1000, 1);
                break;
            case 9:
                mRobotMotion.startMotor((int) RobotDevices.Motors.WRIST_LEFT,
                        angle, 1000, 1);
                break;
            case 10:
                mRobotMotion.startMotor((int) RobotDevices.Motors.NECK_ROTATION,
                        angle, 1000, 1);
                break;
            case 11:
                mRobotMotion.startMotor((int) RobotDevices.Motors.NECK_TILT,
                        angle, 1000, 1);
                byte[] bytes = new byte[10];
                mRobotMotion.startMotor(bytes, 10);
                break;
            default:
                break;
        }
    }

    private boolean tellInputLegality(int action, int direction, int angle) {
        boolean legality = false;
        Log.d(TAG, "action: " + action + "  direction: " + direction
                + "  angle: " + angle);
        switch (action) {
            case 0:
                if (direction == 0 && (angle >= 0 && angle <= 175)) {
                    legality = true;
                } else if (direction == 1 && (angle >= 0 && angle <= 25)) {
                    legality = true;
                }
                break;
            case 1:
                if (direction == 0 && (angle >= 0 && angle <= 65)) {
                    legality = true;
                }
                break;
            case 2:
                if (angle >= 0 && angle <= 80) {
                    legality = true;
                }
                break;
            case 3:
                if (direction == 0 && (angle >= 0 && angle <= 90)) {
                    legality = true;
                }
                break;
            case 4:
                if (angle >= 0 && angle <= 80) {
                    legality = true;
                }
                break;
            case 5:
                if (direction == 0 && (angle >= 0 && angle <= 175)) {
                    legality = true;
                } else if (direction == 1 && (angle >= 0 && angle <= 25)) {
                    legality = true;
                }
                break;
            case 6:
                if (direction == 0 && (angle >= 0 && angle <= 65)) {
                    legality = true;
                }
                break;
            case 7:
                if (angle >= 0 && angle <= 80) {
                    legality = true;
                }
                break;
            case 8:
                if (direction == 0 && (angle >= 0 && angle <= 90)) {
                    legality = true;
                }
                break;
            case 9:
                if (angle >= 0 && angle <= 80) {
                    legality = true;
                }
                break;
            case 10:
                if (angle >= 0 && angle <= 40) {
                    legality = true;
                }
                break;
            case 11:
                if (direction == 0 && (angle >= 0 && angle <= 15)) {
                    legality = true;
                } else if (direction == 1 && (angle >= 0 && angle <= 12)) {
                    legality = true;
                }
                break;
            default:
                break;
        }

        return legality;
    }
}

