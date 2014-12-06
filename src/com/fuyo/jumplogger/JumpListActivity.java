package com.fuyo.jumplogger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Yohei FUJII on 11/23/2014.
 */
public class JumpListActivity extends Activity {
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    JumpListAdapter mAdapter;
    SharedPreferences sharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jump_list);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        if (!sharedPref.contains("email")) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            startActivity(intent);
            finish();
        }
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ArrayList<JumpRecord> records = new ArrayList<JumpRecord>();
        try {
            InputStream is = openFileInput(JumpRecord.FILENAME);
            records = JumpRecord.readAll(is);
            is.close();
        } catch (IOException e) {

        }

        // specify an adapter (see also next example)
//        JumpRecord record = new JumpRecord();
//        record.date = "2014-12-01 12:30:30";
//        record.location = "Happo one ski resort";
//        record.duration = 1.3462;
//        for (int i = 0; i < 50; i++) {
//            records.add(record);
//        }

        mAdapter = new JumpListAdapter(records);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this));
    }
    @Override
    public void onResume() {
        super.onResume();
        reloadOnlineJumprecord();
    }

    private void reloadOnlineJumprecord() {
        if (sharedPref.contains("email")) {
            final String url = "https://www.iijuf.net/jumplogger/api/api.jump.php";
            final String email = sharedPref.getString("email", "defEmail");
            final String password = sharedPref.getString("password", "defPass");
            final String label = "test";
            final String type = "list";
            AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
                String email;
                String password;
                String label;
                String url;
                String type;

                public AsyncTask<Void, Void, String> set(String type, String url, String email, String password, String label) {
                    this.url = url;
                    this.email = email;
                    this.password = password;
                    this.label = label;
                    this.type = type;
                    return this;
                }

                @Override
                protected String doInBackground(Void... voids) {
                    String result = Downloader.downloadString(type, url, email, password, label);
                    if (result.length() > 0) {
                        InputStream is = new ByteArrayInputStream(result.getBytes());
                        try {
                            ArrayList<JumpRecord> records = JumpRecord.readAll(is);
                            JumpRecord.writeAll(records, openFileOutput(JumpRecord.FILENAME, MODE_PRIVATE));
                            mAdapter.setData(records);
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //TODO separate server error and connection error
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(JumpListActivity.this, "id or password incorrect", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    return "";
                }

                @Override
                protected void onPostExecute(String result) {
                    mAdapter.notifyDataSetChanged();
                    mRecyclerView.invalidate();
                }

            }.set(type, url, email, password, label);
            task.execute();
        }
    }
}