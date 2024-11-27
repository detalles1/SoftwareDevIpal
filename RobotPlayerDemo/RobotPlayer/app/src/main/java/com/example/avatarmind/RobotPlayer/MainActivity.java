package com.example.avatarmind.RobotPlayer;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private ImageView mTitleBack;

    private ListView mListView;

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

        mListView = (ListView) findViewById(R.id.main_list);
        mListView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1, getOptions()));
    }

    private void initListener() {
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("Mode", position);
                intent.setClass(MainActivity.this, RunArmActivity.class);
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
        data.add(getString(R.string.run_by_file));
        data.add(getString(R.string.run_by_streams));

        return data;
    }
}