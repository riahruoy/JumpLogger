package com.fuyo.jumplogger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

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
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
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

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.attachToRecyclerView(mRecyclerView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(JumpListActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        int versionCode = -1;
        String versionName = "versionNone";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = pInfo.versionCode;
            versionName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



        UpdateCheckAsyncTask ucat = new UpdateCheckAsyncTask(this, versionCode, new UpdateCheckAsyncTask.UpdateCheckListener() {

            @Override
            public void onNewVersionFound(final String apkName) {
                new AlertDialog.Builder(JumpListActivity.this)
                        .setTitle("New Version Found")
                        .setMessage("New version found :" + apkName)
                        .setPositiveButton("download", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri = Uri.parse("http://jumplogger.iijuf.net/apk/" + apkName);
                                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(i);
                                finish();
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setCancelable(true)
                        .show();

            }
        });
        ucat.execute(new String[]{});



    }
    @Override
    public void onResume() {
        super.onResume();
        reloadOnlineJumprecord();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.jump_detail, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
                    mRecyclerView.setAdapter(mAdapter);
                }

            }.set(type, url, email, password, label);
            task.execute();
        }
    }
}