package com.fuyo.jumplogger;


import com.fuyo.jumplogger.LogDataBase.OnStopCompleteListener;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationLogger extends AbstractLogger{
	LocationListener locationListener;
	LocationManager locationManager;
	final String type;
	public LocationLogger(Context context, LocationManager locationManager, final String type, final String accessId, final String label) {
		super(context, "location." + type, accessId, label);
		this.type = "location."+type;
  		locationListener = new LocationListener() {
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			
			@Override
			public void onProviderEnabled(String provider) {}
			
			@Override
			public void onProviderDisabled(String provider) {}
			
			@Override
			public void onLocationChanged(Location location) {
				String str = location.getLatitude() + "," + location.getLongitude() + "," +location.getAltitude() +"," + location.getAccuracy()
						+ "," + location.getSpeed() + "," + location.getBearing();
				logDataBase.add(str);
			}
		};
		this.locationManager = locationManager;
		logDataBase.writeLogHeader("Latitude,Longitude,Altitude,Accuracy,Speed,Bearing");
		
	}

	@Override
	public void startLogging() {
		if (type.contains("gps")) {
	     	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListener);
		} else {
	     	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
		}
	}

	@Override
	public void stopLogging(final OnStopCompleteListener listener) {
    	locationManager.removeUpdates(locationListener);
    	logDataBase.close(listener);
	}
}
