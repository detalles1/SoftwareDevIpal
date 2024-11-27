package com.example.avatarmind.robotsystem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.avatarmind.robotsystem.activity.ContentActivity;

import java.util.ArrayList;
import java.util.List;

import static com.example.avatarmind.robotsystem.constants.Constants.ENVIRONMENT_SAFETY_STATUS;
import static com.example.avatarmind.robotsystem.constants.Constants.SENSOR_EVENT;
import static com.example.avatarmind.robotsystem.constants.Constants.TOUCH_EVENT;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private ImageView mTitleBack;

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        initView();
        initListener();
    }

    private void initView() {
        setContentView(R.layout.activity_main);

        mTitleBack = (ImageView) findViewById(R.id.common_title_back);

        mListView = (ListView) findViewById(R.id.main_list);

        mListView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1, getOptions()));
    }

    private void initListener() {
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                switch (position) {
                    case 0:
                        toContentActivity(ENVIRONMENT_SAFETY_STATUS);
                        break;
                    case 1:
                        toContentActivity(TOUCH_EVENT);
                        break;
                    case 2:
                        toContentActivity(SENSOR_EVENT);
                        break;
                    default:
                        break;
                }
            }

            private void toContentActivity(int value) {
                Intent intent = new Intent();
                intent.putExtra("name", value);
                intent.setClass(MainActivity.this, ContentActivity.class);
                startActivity(intent);
            }
        });

        mTitleBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }

        });
    }

    private List<String> getOptions() {
        List<String> data = new ArrayList<String>();
        data.add(getString(R.string.motion_safe));
        data.add(getString(R.string.touch_event));
        data.add(getString(R.string.sensor_event));

        return data;
    }
}