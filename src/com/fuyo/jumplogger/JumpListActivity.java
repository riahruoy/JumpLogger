package com.fuyo.jumplogger;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by 05YFU on 11/23/2014.
 */
public class JumpListActivity extends Activity {
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    JumpListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jump_list);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new JumpListAdapter(new String[]{"a", "b", "c"});
        mRecyclerView.setAdapter(mAdapter);

    }
}