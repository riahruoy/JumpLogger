package com.fuyo.jumplogger;

import android.location.Geocoder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;




import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.fuyo.jumplogger.LogDataBase.OnStopCompleteListener;


public class DataCollectorService extends Service {
    static final String TAG="LocalService";
    SensorManager sensorManager;
    private static final int NOTIFICATION_ID = 1;
    private Notification notification;
    private boolean isLogGPS = true;
    private boolean isLogNW = true;
    private boolean isLogAccel = true;
    private boolean isLogSSID = true;
    private boolean isLogSound = true;
    private boolean isLogBluetooth = true;
    private boolean isLogArduino = true;
    private LoggerManager loggers;
    private String accessId = "id_error";
    public static final String INTENT_ACTION = "LOGDATA_SHOW";
    
    private Thread showThread;
    private boolean showThreadFlag;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        Toast.makeText(this, "Start Logging...", Toast.LENGTH_SHORT).show();

        showThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (showThreadFlag) {
					for (int i = 0; i < 10; i++) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (!showThreadFlag) { 
							break;
						}
					}
					//TODO should use AIDL??
					Intent broadcastIntent = new Intent();
					String str = loggers.getLogMessage();
					broadcastIntent.putExtra("message", str);
					broadcastIntent.setAction(INTENT_ACTION);
					DataCollectorService.this.sendBroadcast(broadcastIntent);
				}
			}
        	
        });
		
    }

    private void updateNotification() {
    	String text = "Logging...";
    	Intent intent = new Intent(this, MainActivity.class);
    	PendingIntent pintent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    	
    	
    	
        notification = new NotificationCompat.Builder(this)
    	.setContentTitle("SensorTest")
    	.setContentText(text)
    	.setContentIntent(pintent)
    	.setSmallIcon(R.drawable.ic_launcher)
    	.build();
            

        startForeground(NOTIFICATION_ID, notification);
    	
    }
    
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        accessId = sharedPref.getString("id", "id_error");
        isLogGPS = sharedPref.getBoolean("isLogGPS", true);
        isLogNW = sharedPref.getBoolean("isLogNW", true);
        isLogAccel = sharedPref.getBoolean("isLogAccel", true);
        isLogSSID = sharedPref.getBoolean("isLogSSID", true);
        
        if (intent != null) {
    		accessId = intent.getExtras().getString("id");
    		isLogGPS = intent.getExtras().getBoolean("isLogGPS");
    		isLogSSID = intent.getExtras().getBoolean("isLogSSID");
    		isLogAccel = intent.getExtras().getBoolean("isLogAccel");
    		isLogNW = intent.getExtras().getBoolean("isLogNW");
        }
        
        loggers = new LoggerManager();

		if (isLogAccel) {
			int[] types = {
					Sensor.TYPE_ACCELEROMETER,
					Sensor.TYPE_MAGNETIC_FIELD,
					Sensor.TYPE_GYROSCOPE,
					Sensor.TYPE_LINEAR_ACCELERATION,
					Sensor.TYPE_GRAVITY,
					Sensor.TYPE_ROTATION_VECTOR,
					Sensor.TYPE_LIGHT,
					Sensor.TYPE_PRESSURE,
					Sensor.TYPE_PROXIMITY,
					Sensor.TYPE_RELATIVE_HUMIDITY,
					Sensor.TYPE_AMBIENT_TEMPERATURE
			};
			SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
			for (int i = 0; i < types.length; i++) {
				try {
					loggers.put("sensor."+types[i] ,new SensorLogger(this, sensorManager, types[i], SensorManager.SENSOR_DELAY_FASTEST, accessId));
				} catch (com.fuyo.jumplogger.AbstractLogger.SensorNotFoundException e) {
				}
			}
		}
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		loggers.put("location.gps", new LocationLogger(this, locationManager, "gps", accessId));
		
		loggers.startLogging();
		
		showThreadFlag = true;
		showThread.start();
        updateNotification();
        Log.i(TAG, "onStartCommand Received start id " + startId + ": " + intent);
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        loggers.stopLogging(new OnStopCompleteListener() {
			@Override
			public void onStopComplete() {
		        showThreadFlag = false;
		        try {
					showThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		        Toast.makeText(DataCollectorService.this, "service stopped", Toast.LENGTH_SHORT).show();
		        rescanSdcard();
			}
        	
        });
    }
    public void rescanSdcard(){ 
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, 
                        Uri.parse("file://" +  Environment.getExternalStorageDirectory()))); 
    } 

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private static class LoggerManager {
		private TreeMap<String, AbstractLogger> logMap;
		private int loggerCount;
		public LoggerManager() {
	    	logMap = new TreeMap<String, AbstractLogger>();
	    	loggerCount = 0;
		}
		public String getLogMessage() {
			String str = "";
			for (Map.Entry<String, AbstractLogger> entry : logMap.entrySet()) {
				if (entry.getValue().getLoggedCount() > 0) {
					str += entry.getValue().getLogType() + " : " + entry.getValue().getLoggedCount() + "\n";
					str += entry.getValue().getLastLog() + "\n";
				}
			}
			return str;
		}
		public void put(String key, AbstractLogger logger) {
			logMap.put(key, logger);
		}
		public void startLogging() {
			for (Map.Entry<String, AbstractLogger> entry : logMap.entrySet()) {
				loggerCount++;
				entry.getValue().startLogging();
			}			
		}
		public void stopLogging(final OnStopCompleteListener onStopCompleteListener) {
			final OnStopCompleteListener listener = new OnStopCompleteListener() {
				@Override
				public void onStopComplete() {
					loggerCount--;
					if (loggerCount == 0) {
						onStopCompleteListener.onStopComplete();
					}
				}
			};
			for (Map.Entry<String, AbstractLogger> entry : logMap.entrySet()) {
				entry.getValue().stopLogging(listener);
			}			
		}
	}
}