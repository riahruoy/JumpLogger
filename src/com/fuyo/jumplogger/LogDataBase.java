package com.fuyo.jumplogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;



public class LogDataBase {
	private long totalCount;
	private long lastUpdate;
    public static final String LOGDIR_NAME = "jumplogger";
    public static final String UPLOAD_LOGDIR_NAME = "uploadDir";
    public static final String TEMP_LOGDIR_NAME = "_tmp_upload";
    private final SimpleDateFormat logTimeSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.JAPAN); //SimpleDataFormat is not thread-safe. Don't make it static.
    private final File logFile;
    private final String logFilePath;
    private long startTime;
    private final long TIME_CHUNK = 30 * 60 * 1000; //30 minutes
    private BufferedWriter logWriter;
	String accessId;
	String logType;
	String lastString = "";
	private final String logDir;
	private boolean isFileLogging = true;
	private boolean isFileUploading = true;
	Context context;
	final String label;
	ConnectivityManager connectivityManger;
	private SharedPreferences sharedPref;
	
	public LogDataBase(Context context, String logType, String accessId, String label) {
		this.accessId = accessId;
		this.logType = logType;
		this.context = context;
		this.label = label;
		sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		startTime = System.currentTimeMillis();
		lastUpdate = startTime;
		totalCount = 0;
        logDir = Environment.getExternalStorageDirectory().getPath() + "/" + LOGDIR_NAME + "/" + label + "/";
        logFilePath = logDir + logType + ".txt";
       	logFile = new File(logFilePath);
       	logFile.getParentFile().mkdirs();
       	try {
			logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public String getLogDir() {
		return logDir;
	}
	public void writeLogHeader(String str) {
		if (isFileLogging) {
			if (logFile.length() > 0) {
				//append mode
				//header is already written;
			} else {
				try {
					logWriter.write("Date," + str + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public void add(String str) {
		StringBuffer stringBuffer = new StringBuffer();
		Date nowDate = Calendar.getInstance().getTime();
		stringBuffer.append(logTimeSdf.format(nowDate)).append(',').append(str).append('\n');
		totalCount++;
		lastUpdate = System.currentTimeMillis();
		if (isFileLogging) {
			try {
				logWriter.write(stringBuffer.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public long getTotalCount() {
		return totalCount;
	}
	public long getLastUpdate() {
		return lastUpdate;
	}
	public String getLastString() {
		return lastString;
	}
	public String getLogType() {
		return logType;
	}
	public synchronized void close(final OnStopCompleteListener listener) {
		if (isFileLogging) {
    		try {
				logWriter.flush();
	    		logWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (isFileUploading) {
			final String folderId = label;
			final String fileName = Environment.getExternalStorageDirectory().getPath() + "/" + LOGDIR_NAME + "/"+UPLOAD_LOGDIR_NAME + "/" + folderId + "/" + logType + ".txt";
			File file = new File(fileName);
			file.getParentFile().mkdirs();
			try {
				copyTransfer(logFilePath, fileName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		listener.onStopComplete();
	}
	public boolean isConnected() {
		  
		NetworkInfo ni = connectivityManger.getActiveNetworkInfo();
		if (ni != null) {
			return connectivityManger.getActiveNetworkInfo().isConnected();
		}
		return false;
	}
	
	
	  
	public static interface OnStopCompleteListener {
		void onStopComplete();
	}
	
	public static void moveTransfer(String src, String dest) throws IOException {
		copyTransfer(src, dest);
		File prevFile = new File(src);
		prevFile.delete();
	}
	public static void copyTransfer(String src, String dest) throws IOException {
		FileChannel srcChannel = new FileInputStream(src).getChannel();
		FileChannel destChannel = new FileOutputStream(dest).getChannel();
		try {
			srcChannel.transferTo(0, srcChannel.size(), destChannel);
		} finally {
			srcChannel.close();
			destChannel.close();
		}
	}
}
