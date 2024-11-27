package com.example.avatarmind.robotmotion;

import android.app.Activity;
import android.os.Bundle;
import android.robot.motion.RobotMotion;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class EmojiActivity extends Activity implements OnClickListener {

    private static final int[] EMOJI_ARRAY = {RobotMotion.Emoji.SMILE,
            RobotMotion.Emoji.SAD, RobotMotion.Emoji.CRY,
            RobotMotion.Emoji.SHY, RobotMotion.Emoji.ANGRY,
            RobotMotion.Emoji.BLINK, RobotMotion.Emoji.FROWN};

    private ImageView mTitleBack;

    private Spinner mEmojiSelector;

    private Button mBtnRun;

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
        setContentView(R.layout.activity_emoji);

        mTitleBack = (ImageView) findViewById(R.id.common_title_back);
        TextView title = (TextView) findViewById(R.id.common_title_text);
        title.setText(R.string.unit_emoji);

        mBtnRun = (Button) findViewById(R.id.emoji_run);
        mBtnReset = (Button) findViewById(R.id.emoji_reset);

        mEmojiSelector = (Spinner) findViewById(R.id.emoji_selector);
        String[] emojiStr = getResources().getStringArray(R.array.emoji_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, R.layout.spinner_item, emojiStr);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        mEmojiSelector.setAdapter(adapter);
    }

    private void initListener() {
        mTitleBack.setOnClickListener(this);
        mBtnRun.setOnClickListener(this);
        mBtnReset.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.common_title_back:
                finish();
                break;
            case R.id.emoji_run:
                int position = mEmojiSelector.getSelectedItemPosition();
                mRobotMotion.emoji((int) EMOJI_ARRAY[position]);
                break;
            case R.id.emoji_reset:
                mRobotMotion.emoji(RobotMotion.Emoji.DEFAULT);
                break;
            default:
                break;
        }

    }
}

