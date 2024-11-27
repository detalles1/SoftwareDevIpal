package com.example.avatarmind.robotsystem.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.avatarmind.robotsystem.R;
import com.example.avatarmind.robotsystem.bean.ContentBean;

import java.util.ArrayList;

public class ContentAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<ContentBean> contentBeanArrayList;

    public ContentAdapter(Context context, ArrayList<ContentBean> lists) {
        super();
        this.context = context;
        this.contentBeanArrayList = lists;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return contentBeanArrayList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return arg0;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int index, View view, ViewGroup arg2) {
        ContentBean mContentBean = contentBeanArrayList.get(index);
        if (view == null) {
            view = inflater.inflate(R.layout.list_item, null);
        }
        view.setBackgroundColor(Color.WHITE);
        TextView tvEventType = (TextView) view.findViewById(R.id.tv_event_type);
        TextView tvEventInfo = (TextView) view.findViewById(R.id.tv_event_info);
        tvEventType.setTextColor(Color.BLACK);
        tvEventInfo.setTextColor(Color.BLACK);
        tvEventType.setText(mContentBean.getType());
        tvEventInfo.setText(mContentBean.getInfo());

        if (index % 2 != 0) {
            view.setBackgroundColor(Color.argb(250, 255, 255, 255));
        } else {
            view.setBackgroundColor(Color.argb(250, 224, 243, 250));
        }

        return view;
    }
}
