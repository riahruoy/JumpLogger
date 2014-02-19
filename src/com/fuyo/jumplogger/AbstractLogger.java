package com.fuyo.jumplogger;



import com.fuyo.jumplogger.LogDataBase.OnStopCompleteListener;

import android.content.Context;


public abstract class AbstractLogger {
	protected LogDataBase logDataBase = null;
	protected String logName = "error";
	protected AbstractLogger(Context context, String logType, String accessId, String label) {
		logDataBase = new LogDataBase(context, logType, accessId, label);
	}
	protected AbstractLogger() {
		
	}
	abstract public void startLogging();
    abstract public void stopLogging(final OnStopCompleteListener listener);
    
    public long getLoggedCount() {
    	if (logDataBase == null) return 0;
    	return logDataBase.getTotalCount();
    }
    

    public String getLogType() {
    	if (logDataBase == null) return "waiting...";
    	return logDataBase.getLogType();
    }
    
    public String getLastLog() {
    	if (logDataBase == null) return "waiting...";
    	String str = logDataBase.getLastString();
    	final int MAX_LENGTH = 50;
    	return (str.length() > MAX_LENGTH ? (str.substring(0,MAX_LENGTH-3) + "...") : str); 
    }
    public String getLogDir() {
    	return logDataBase.getLogDir();
    }
    public static class SensorNotFoundException extends Exception {
		private static final long serialVersionUID = 1406935441927470713L;
    }
}
