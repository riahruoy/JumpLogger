package com.fuyo.jumplogger;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;


import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	Button startButton;
	Button stopButton;
	Button goinButton;
	Button gooutButton;
	CheckBox gpsCheckBox;
	CheckBox ssidCheckBox;
	CheckBox accelCheckBox;
	CheckBox nwCheckBox;
	CheckBox nwLowEnergyCheckBox;
	boolean isOut = true;
	TextView logTextView;
	BroadcastReceiver logReceiver;
	IntentFilter logIntentFilter;
	SharedPreferences sharedPref;
    private final SimpleDateFormat logTimeSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.JAPAN); //SimpleDataFormat is not thread-safe. Don't make it static.
    private final String APK_URL = "http://";
    private static final int MENU_ID_MENU1 = (Menu.FIRST);
	private static final String goinFileName = "building.txt"; 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPref.contains("id")) {
        	Editor e = sharedPref.edit();
        	Random r = new Random();
        	String key = Integer.toString(r.nextInt(1000000));
        	e.putString("id", key);
        	e.commit();
        }
        final String id = sharedPref.getString("id", "id_error");
        boolean isLogGPS = sharedPref.getBoolean("isLogGPS", false);
        boolean isLogSSID = sharedPref.getBoolean("isLogSSID", false);
        boolean isLogAccel = sharedPref.getBoolean("isLogAccel", false);
        boolean isLogNW = sharedPref.getBoolean("isLogNW", true);
        boolean isLowEnergyNW = sharedPref.getBoolean("isLowEnergyNW", true);
        
        TextView tv = (TextView)findViewById(R.id.idTextView);
        tv.setText("http://overtone.nilab.ecl.ntt.co.jp/~fujii/datacollect/logList.php?accessId=" + id);
        
        logTextView = (TextView)findViewById(R.id.logTextView);
        logIntentFilter = new IntentFilter();
        logIntentFilter.addAction(DataCollectorService.INTENT_ACTION);
        logReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String logString = intent.getExtras().getString("message");
				logTextView.setText(logString);
			}
        	
        };
        
        nwCheckBox = (CheckBox)findViewById(R.id.checkBoxNWFlag);
        nwCheckBox.setChecked(isLogNW);
        nwCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor e = sharedPref.edit();
				e.putBoolean("isLogNW", isChecked);
				e.commit();
			}
		});
        nwLowEnergyCheckBox = (CheckBox)findViewById(R.id.checkBoxLowEnergyFlag);
        nwLowEnergyCheckBox.setChecked(isLowEnergyNW);
        nwLowEnergyCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor e = sharedPref.edit();
				e.putBoolean("isLowEnergyNW", isChecked);
				e.commit();
			}
		});

        gpsCheckBox = (CheckBox)findViewById(R.id.checkBoxGPSFlag);
        gpsCheckBox.setChecked(isLogGPS);
        gpsCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor e = sharedPref.edit();
				e.putBoolean("isLogGPS", isChecked);
				e.commit();
			}
		});
        ssidCheckBox = (CheckBox)findViewById(R.id.CheckBoxSSID);
        ssidCheckBox.setChecked(isLogSSID);
        ssidCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor e = sharedPref.edit();
				e.putBoolean("isLogSSID", isChecked);
				e.commit();
			}
		});
        accelCheckBox = (CheckBox)findViewById(R.id.CheckBoxAccel);
        accelCheckBox.setChecked(isLogAccel);
        accelCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor e = sharedPref.edit();
				e.putBoolean("isLogAccel", isChecked);
				e.commit();
			}
		});
        
        startButton = (Button)findViewById(R.id.startService);
        stopButton = (Button)findViewById(R.id.stopService);
        goinButton = (Button)findViewById(R.id.goinButton);
        gooutButton = (Button)findViewById(R.id.gooutButton);
        startButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onCheckFinish(true);
			}
		});
        stopButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(), DataCollectorService.class);
				intent.putExtra("id", sharedPref.getString("id", "id_error"));
				stopService(intent);
				setInputEnabled(false);
			}
		});
        goinButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN);
		        String prefix = sdf.format(Calendar.getInstance().getTime());
		        String logDir = Environment.getExternalStorageDirectory().getPath() + "/logDir/" + prefix + "/";
				File file = new File(logDir + goinFileName);
				try {
					FileWriter fileWriter = new FileWriter(file, true);
					fileWriter.write(logTimeSdf.format(Calendar.getInstance().getTime()) + ",in\n");
					fileWriter.close();
					Toast.makeText(MainActivity.this, "goin" +
							"", Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
        gooutButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
		        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN);
		        String prefix = sdf.format(Calendar.getInstance().getTime());
		        String logDir = Environment.getExternalStorageDirectory().getPath() + "/logDir/" + prefix + "/";
				File file = new File(logDir + goinFileName);
				try {
					FileWriter fileWriter = new FileWriter(file, true);
					fileWriter.write(logTimeSdf.format(Calendar.getInstance().getTime()) + ",out\n");
					fileWriter.close();
					Toast.makeText(MainActivity.this, "外に出ました", Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
        
        Button uploadButton = (Button)findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
		    	Intent intent = new Intent(MainActivity.this, LogUploader.class);

		    	intent.putExtra("accessId", id);
		    	MainActivity.this.startService(intent);
			}
		});
        
        int versionCode = -1;
        String versionName = "versionNone";
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
	        versionCode = pInfo.versionCode;
	        versionName = pInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TextView tvVersion = (TextView)findViewById(R.id.idTextViewVersion);
		tvVersion.setText("version : " + versionName + "." + versionCode);
		
		
        UpdateCheckAsyncTask ucat = new UpdateCheckAsyncTask(this, versionCode, new UpdateCheckAsyncTask.UpdateCheckListener() {
			
			@Override
			public void onNewVersionFound(final String apkName) {
				new AlertDialog.Builder(MainActivity.this)
					.setTitle("New Version Found")
					.setMessage("New version found :" + apkName)
					.setPositiveButton("download", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (isServiceRunning()) {
								Intent intent = new Intent(getBaseContext(), DataCollectorService.class);
								intent.putExtra("id", sharedPref.getString("id", "id_error"));
								stopService(intent);
								setInputEnabled(false);
							}
							Uri uri = Uri.parse("http://153.128.40.95/experiment/datacollect/apk/" + apkName);
							Intent i = new Intent(Intent.ACTION_VIEW, uri);
							startActivity(i);
							finish();
						}
					})
					.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
						
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
    protected void onResume() {
    	super.onResume();
    	
    	registerReceiver(logReceiver, logIntentFilter);
    	if (isServiceRunning()) {
    		setInputEnabled(true);
    	} else {
    		setInputEnabled(false);
    	}
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	unregisterReceiver(logReceiver);
    }
    
    private void setInputEnabled(boolean isLoggingStarted) {
		startButton.setEnabled(!isLoggingStarted);
		stopButton.setEnabled(isLoggingStarted);
		nwCheckBox.setEnabled(!isLoggingStarted);
		nwLowEnergyCheckBox.setEnabled(!isLoggingStarted);
		gpsCheckBox.setEnabled(!isLoggingStarted);
		ssidCheckBox.setEnabled(!isLoggingStarted);
		accelCheckBox.setEnabled(!isLoggingStarted);
		goinButton.setEnabled(isLoggingStarted);
		gooutButton.setEnabled(isLoggingStarted);
    }
    
    public void onCheckFinish(boolean nwEnabled) {
    	if (nwEnabled) {
	        final String id = sharedPref.getString("id", "id_error");
			Intent intent = new Intent(this, DataCollectorService.class);
			intent.putExtra("isLogNW", sharedPref.getBoolean("isLogNW", true));
			intent.putExtra("isLowEnergyNW", sharedPref.getBoolean("isLowEnergyNW", true));
			intent.putExtra("isLogGPS",sharedPref.getBoolean("isLogGPS", false));
			intent.putExtra("isLogSSID", sharedPref.getBoolean("isLogSSID", false));
			intent.putExtra("isLogAccel", sharedPref.getBoolean("isLogAccel", false));
			intent.putExtra("id", id);
			setInputEnabled(true);
			startService(intent);
			
    	} else {
    		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    		alertDialogBuilder.setTitle("Error")
    		.setMessage("NW location doesn't responding\n(StackOverflow (Question #15747543) only restarting will fix this problem.")
    		.create().show();
    	}
    	
    }
    
    private boolean isServiceRunning() {
    	final String serviceName = DataCollectorService.class.getCanonicalName();
    	ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
    	List<RunningServiceInfo> services = am.getRunningServices(Integer.MAX_VALUE);
    	for (RunningServiceInfo info :services) {
    		if (serviceName.equals(info.service.getClassName())) {
    			return true;
    		}
    	}
    	return false;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(Menu.NONE, MENU_ID_MENU1, Menu.NONE, "setting");
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ID_MENU1:
        	Intent intent = new Intent(this, LoginActivity.class);
        	intent.setAction(Intent.ACTION_VIEW);
        	startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
}
