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
    private File logFile;
    private long startTime;
    private final long TIME_CHUNK = 30 * 60 * 1000; //30 minutes
    private BufferedWriter logWriter;
	String accessId;
	String logType;
	String lastString = "";
	private String logDir = "";
	private boolean isFileLogging = true;
	private boolean USE_UTSLITE = false;
	private boolean isFileUploading = true;
	private String uploadFileName;
	private String uploadDir = "";
	private File uploadFile;
	private BufferedWriter instanceWriter;
	private final SharedPreferences sharedPref;
	Context context;
	ConnectivityManager connectivityManger;
	private String logHeader = "";
	
	public LogDataBase(Context context, String logType, String accessId, String label) {
		this.accessId = accessId;
		this.logType = logType;
		this.context = context;
		sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		startTime = System.currentTimeMillis();
		lastUpdate = startTime;
		totalCount = 0;
		if (isFileLogging) {
	        logDir = Environment.getExternalStorageDirectory().getPath() + "/" + LOGDIR_NAME + "/" + label + "/";
        	logFile = new File(logDir + logType + ".txt");
        	logFile.getParentFile().mkdirs();
        	try {
				logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true)));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (isFileUploading) {
	        uploadDir = Environment.getExternalStorageDirectory().getPath() + "/" + LOGDIR_NAME + "/"+TEMP_LOGDIR_NAME;
	        uploadFileName = uploadDir + "/" + logType + ".txt";
	        uploadFile = new File(uploadFileName);
        	uploadFile.getParentFile().mkdirs();
			try {
				instanceWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(uploadFile)));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	public void writeLogHeader(String str) {
		logHeader = str;
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
		if (isFileUploading) {
			if (uploadFile.length() > 0) {
				//append mode
				//header is already written;
			} else {
				try {
					instanceWriter.write("Date," + str + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public String getLogDir() {
		return logDir;
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
		if (isFileUploading) {
			try {
				instanceWriter.write(stringBuffer.toString());
				
				//periodically...
				long nowTime = System.currentTimeMillis();
				if (nowTime - startTime >  TIME_CHUNK) {
					startTime = nowTime;
					moveToUploadFolder();

					//upload
			    	Intent intent = new Intent(context, LogUploader.class);
			    	intent.putExtra("accessId", accessId);
			    	intent.putExtra("email", sharedPref.getString("email", "error_email"));
			    	intent.putExtra("password", sharedPref.getString("password", "error_pass"));
			    	context.startService(intent);
					
					
				}
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
	    		instanceWriter.flush();
	    		instanceWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (isFileUploading) {
			final String folderId = getNextFolder();
			final String fileName = Environment.getExternalStorageDirectory().getPath() + "/" + LOGDIR_NAME + "/"+UPLOAD_LOGDIR_NAME + "/" + folderId + "/" + logType + ".txt";
			File file = new File(fileName);
			file.getParentFile().mkdirs();
			try {
				moveTransfer(uploadFileName, fileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
	
	private void moveToUploadFolder() {
		//check smallest number
		try {
			instanceWriter.flush();
			instanceWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final String folderId = getNextFolder();
		final String fileName = Environment.getExternalStorageDirectory().getPath() + "/" + LOGDIR_NAME + "/"+UPLOAD_LOGDIR_NAME + "/" + folderId + "/" + logType + ".txt";
		File file = new File(fileName);
		file.getParentFile().mkdirs();
		try {
			moveTransfer(uploadFileName, fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			instanceWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(uploadFile)));
			try {
				instanceWriter.write("Date," + logHeader + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	private String getNextFolder() {
//		final String targetFolder = Environment.getExternalStorageDirectory().getPath() + "/" + LOGDIR_NAME + "/"+UPLOAD_LOGDIR_NAME + "/";
		return Long.toString(System.currentTimeMillis());
		
//		for (int i = 0; i < 1000000; i++) {
//			String subdir =  getNumberFolderString(i, 5);
//			File dir = new File(targetFolder + subdir);
//			dir.getParentFile().mkdir();
//			if (!dir.exists()) return subdir;
//			
//		}
//		return "error";
	}
	
	private String getNumberFolderString(int num, int keta) {
		StringBuilder sb = new StringBuilder();
		sb.append(num);
		for (int i = sb.length() + 1; i <= keta; i++) {
			sb.insert(0, '0');
		}
		return sb.toString();
	}
	  
	public static interface OnStopCompleteListener {
		void onStopComplete();
	}
	
	public static class LogSQLiteOpenHelper extends SQLiteOpenHelper {
		static final String DB = "sensor_log";
		static final String TABLE = "sensordata";
		static int DB_VERSION = 1;
		public LogSQLiteOpenHelper(Context c) {
			super(c, DB, null, DB_VERSION);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table "+TABLE+" (id integer primary key autoincrement, data varchar(512), date varchar(64), type varchar(32));");			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("drop table " + TABLE +";");
			onCreate(db);
		}
	}

	public static void moveTransfer(String src, String dest) throws IOException {
		FileChannel srcChannel = new FileInputStream(src).getChannel();
		FileChannel destChannel = new FileOutputStream(dest).getChannel();
		try {
			srcChannel.transferTo(0, srcChannel.size(), destChannel);
		} finally {
			srcChannel.close();
			destChannel.close();
		}
		File prevFile = new File(src);
		prevFile.delete();
	}
}
