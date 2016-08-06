package com.roman.refreshlayout;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class RefreshActivity extends Activity {

    private ListView mListView;
    private ArrayList<String> mData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh);
        for (int i = 0; i < 30; i++) {
            mData.add(i + "奥特曼" + i);
        }
        mListView = (ListView) findViewById(R.id.main_listview);
        mListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mData));
    }
}
