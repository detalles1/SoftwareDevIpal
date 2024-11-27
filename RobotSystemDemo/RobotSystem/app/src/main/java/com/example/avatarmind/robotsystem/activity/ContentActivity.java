package com.example.avatarmind.robotsystem.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.robot.hw.RobotDevices;
import android.robot.hw.RobotSystem;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.avatarmind.robotsystem.R;
import com.example.avatarmind.robotsystem.adapter.ContentAdapter;
import com.example.avatarmind.robotsystem.bean.ContentBean;

import java.util.ArrayList;

import static com.example.avatarmind.robotsystem.constants.Constants.ENVIRONMENT_SAFETY_STATUS;
import static com.example.avatarmind.robotsystem.constants.Constants.SENSOR_EVENT;
import static com.example.avatarmind.robotsystem.constants.Constants.TOUCH_EVENT;

public class ContentActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "ContentActivity";

    private RobotSystem mRobotSystem;

    private static final int UPDATE_RESULT = 0x100;

    private final String KEY_TYPE = "type";

    private final String KEY_RESULT = "result";

    private ImageView mTitleBack;

    private TextView mTitleText;

    private Button mClearList;

    private ContentBean mContentBean;

    private ArrayList<ContentBean> mContentBeanArrayList = new ArrayList<ContentBean>();

    private ListView mListView;

    private ContentAdapter mContentAdapter;

    private int mName;

    private RobotSystem.OnResult onResult = new RobotSystem.OnResult() {
        @Override
        public void onCompleted(int session_id, int result, int error_code) {
            if (result != 1 || error_code != 0) {
                Toast.makeText(ContentActivity.this, "execute command error", Toast.LENGTH_LONG).show();
            }
        }
    };

    private RobotSystem.Listener listener = new RobotSystem.Listener() {
        @Override
        public void onMessage(int from, int what, int arg1, int arg2) {
            if (mName == 0) {
                Log.i(TAG, "error");
                return;
            }
            if (mName == ENVIRONMENT_SAFETY_STATUS) {
                listingMotionSafe(from, what, arg1);
            }

            if (mName == TOUCH_EVENT) {
                listingTouchEvent(from, what, arg1);
            }

            if (mName == SENSOR_EVENT) {
                listingSensorEvent(from, what, arg1);
            }

        }
    };

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_RESULT:
                    Bundle bundle = msg.getData();
                    String type = bundle.getString(KEY_TYPE);
                    String result = bundle.getString(KEY_RESULT);
                    mContentBean = new ContentBean();
                    mContentBean.setType(type);
                    mContentBean.setInfo(result);
                    mContentBeanArrayList.add(mContentBean);
                    mContentAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    private void updateUI(String motionType, String motionResult) {
        Message msg = new Message();
        msg.what = UPDATE_RESULT;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TYPE, motionType);
        bundle.putString(KEY_RESULT, motionResult);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        mName = getIntent().getIntExtra("name", 0);
        initView();
        initListener();
        Log.i(TAG, "onCreate: mName = " + mName);
        mRobotSystem = new RobotSystem();
        mRobotSystem.registerListener(listener);
        mRobotSystem.setEnable(RobotDevices.DeviceType.MOTION_SAFE, 0, 1, onResult);
    }

    private void initView() {
        Log.i(TAG, "initView: ");
        mTitleBack = (ImageView) findViewById(R.id.common_title_back);
        mTitleText = (TextView) findViewById(R.id.common_title_text);
        mClearList = (Button) findViewById(R.id.bt_clear_list);
        mListView = (ListView) findViewById(R.id.lv_list_view);
        ContentBean contentBean = new ContentBean();
        if (mName == 0) {
            Log.i(TAG, "initView: error");
            return;
        }
        if (mName == ENVIRONMENT_SAFETY_STATUS) {
            mTitleText.setText("Environment Safety Status");
            contentBean.setType("Event Type");
            contentBean.setInfo("Event Result");
        } else if (mName == TOUCH_EVENT) {
            mTitleText.setText("Touch Event");
            contentBean.setType("Event Type");
            contentBean.setInfo("Event Position");
        } else if (mName == SENSOR_EVENT) {
            mTitleText.setText("Sensor Event");
            contentBean.setType("Event Type");
            contentBean.setInfo("Event Position");
        }
        mContentBeanArrayList.add(contentBean);
        mContentAdapter = new ContentAdapter(ContentActivity.this, mContentBeanArrayList);
        mListView.setAdapter(mContentAdapter);
    }

    private void initListener() {
        mTitleBack.setOnClickListener(this);
        mClearList.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.common_title_back:
                finish();
                break;
            case R.id.bt_clear_list:
                mContentBeanArrayList.clear();
                ContentBean contentBean = new ContentBean();
                if (mName == 0) {
                    Log.i(TAG, "onClick: error");
                    return;
                }
                if (mName == ENVIRONMENT_SAFETY_STATUS) {
                    contentBean.setType("Event Type");
                    contentBean.setInfo("Event Result");
                } else if (mName == TOUCH_EVENT) {
                    contentBean.setType("Event Type");
                    contentBean.setInfo("Event Position");
                } else if (mName == SENSOR_EVENT) {
                    contentBean.setType("Event Type");
                    contentBean.setInfo("Event Position");
                }
                mContentBeanArrayList.add(contentBean);
                mContentAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    private void listingMotionSafe(int from, int what, int arg1) {
        if (from == RobotSystem.CallbackCommand.RC_EVENT_TYPE) {
            if (what == RobotSystem.CallbackCommand.RC_CHANGE_MOTION_SAFE_STATUS) {
                if (arg1 == 0) {
                    updateUI("motion status", "safe");
                }
                if (arg1 == 1) {
                    updateUI("motion status", "unsafe");
                }
            }
        }
    }

    private void listingTouchEvent(int from, int what, int arg1) {
        if (from == RobotSystem.CallbackCommand.RF_EVENT_TYPE) {
            if (what == RobotSystem.CallbackCommand.RF_EVENT_TOUCH) {
                if (arg1 == RobotSystem.CallbackCommand.RF_HEAD_TOUCH) {
                    updateUI("Touch", "Head");
                }
                if (arg1 == RobotSystem.CallbackCommand.RF_LEFT_SHOULDER_TOUCH) {
                    updateUI("Touch", "Left Arm");
                }
                if (arg1 == RobotSystem.CallbackCommand.RF_RIGHT_SHOULDER_TOUCH) {
                    updateUI("Touch", "Right Arm");
                }
                if (arg1 == RobotSystem.CallbackCommand.RF_LEFT_OXTER_TOUCH) {
                    updateUI("Touch", "Right Side");
                }
                if (arg1 == RobotSystem.CallbackCommand.RF_RIGHT_OXTER_TOUCH) {
                    updateUI("Touch", "Left Side");
                }
            }

            if (what == RobotSystem.CallbackCommand.RF_EVENT_LONG_TOUCH) {
                if (arg1 == RobotSystem.CallbackCommand.RF_HEAD_TOUCH) {
                    updateUI("Long Touch", "Head");
                }

                if (arg1 == RobotSystem.CallbackCommand.RF_LEFT_SHOULDER_TOUCH) {
                    updateUI("Long Touch", "Left Arm");
                }
                if (arg1 == RobotSystem.CallbackCommand.RF_RIGHT_SHOULDER_TOUCH) {
                    updateUI("Long Touch", "Right Arm");
                }
                if (arg1 == RobotSystem.CallbackCommand.RF_LEFT_OXTER_TOUCH) {
                    updateUI("Long Touch", "Right Side");
                }

                if (arg1 == RobotSystem.CallbackCommand.RF_RIGHT_OXTER_TOUCH) {
                    updateUI("Long Touch", "Left Side");
                }
            }

            if (what == RobotSystem.CallbackCommand.RF_EVENT_RELEASE) {
                if (arg1 == RobotSystem.CallbackCommand.RF_HEAD_TOUCH) {
                    updateUI("Release", "Head");
                }
            }
        }
    }

    private void listingSensorEvent(int from, int what, int arg1) {
        if (from == RobotSystem.CallbackCommand.RC_SENSOR_TYPE) {
            if (what == RobotSystem.CallbackCommand.RC_FRONT_UPPER_OBSTACLE) {
                updateUI("Top", "Ahead");
            }

            if (what == RobotSystem.CallbackCommand.RC_FRONT_COLLISION) {
                updateUI("Collision", "Ahead");
            }

            if (what == RobotSystem.CallbackCommand.RC_APPROACH_SLOW) {
                if (arg1 == RobotSystem.CallbackCommand.EVENT_FRONT) {
                    updateUI("Slow approaching", "Ahead");
                }
                if (arg1 == RobotSystem.CallbackCommand.EVENT_BACK) {
                    updateUI("Slow approaching", "Rear");
                }
                if (arg1 == RobotSystem.CallbackCommand.EVENT_LEFT) {
                    updateUI("Slow approaching", "Left");
                }
                if (arg1 == RobotSystem.CallbackCommand.EVENT_RIGHT) {
                    updateUI("Slow approaching", "Right");
                }
            }

            if (what == RobotSystem.CallbackCommand.RC_APPROACH_FAST) {
                if (arg1 == RobotSystem.CallbackCommand.EVENT_FRONT) {
                    updateUI("Fast approaching", "Ahead");
                }
                if (arg1 == RobotSystem.CallbackCommand.EVENT_BACK) {
                    updateUI("Fast approaching", "Rear");
                }
                if (arg1 == RobotSystem.CallbackCommand.EVENT_LEFT) {
                    updateUI("Fast approaching", "Left");
                }
                if (arg1 == RobotSystem.CallbackCommand.EVENT_RIGHT) {
                    updateUI("Fast approaching", "Right");
                }
            }

            if (what == RobotSystem.CallbackCommand.RC_FRONT_COLLISION_RELEASE) {
                updateUI("Release", "Ahead");
            }
        }
    }
}


