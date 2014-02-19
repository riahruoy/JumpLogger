package com.fuyo.jumplogger;

import java.util.List;

import com.fuyo.jumplogger.LogDataBase.OnStopCompleteListener;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorLogger extends AbstractLogger {
	SensorManager sensorManager;
	SensorEventListener sensorEventListener;
	final int sensorType;
	Sensor sensor;
	final int delay;
	public SensorLogger(Context context, SensorManager sensorManager, final int type, final int delay, final String accessId, final String label) throws SensorNotFoundException {
		super(context, getStringType(type), accessId, label);
		this.sensorManager = sensorManager;
		this.sensorType = type;
		this.delay = delay;
		List<Sensor> sensors = sensorManager.getSensorList(type);
		if (sensors.size() == 0) {
			throw new SensorNotFoundException();
		}
		sensor = sensors.get(0);
		
		
		logDataBase.writeLogHeader(getHeader(type));
		
		sensorEventListener = new SensorEventListener() {
			@Override
			public void onSensorChanged(SensorEvent event) {
				if (event.sensor.getType() == sensor.getType()) {
					float[] values = event.values.clone();
					StringBuffer str = new StringBuffer();
					switch (event.sensor.getType()) {
					case Sensor.TYPE_ACCELEROMETER:
					case Sensor.TYPE_MAGNETIC_FIELD:
					case Sensor.TYPE_GYROSCOPE:
					case Sensor.TYPE_LINEAR_ACCELERATION:
					case Sensor.TYPE_GRAVITY:
					case Sensor.TYPE_ROTATION_VECTOR:
						str.append(values[0]);
						str.append(',');
						str.append(values[1]);
						str.append(',');
						str.append(values[2]);
						break;
					case Sensor.TYPE_LIGHT:
					case Sensor.TYPE_PRESSURE:
					case Sensor.TYPE_PROXIMITY:
					case Sensor.TYPE_RELATIVE_HUMIDITY:
					case Sensor.TYPE_AMBIENT_TEMPERATURE:
						str.append(values[0]);
						break;
						
						
					}
					logDataBase.add(str.toString());
				}
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
	}

	@Override
	public void startLogging() {
    	sensorManager.registerListener(sensorEventListener, sensor, delay);

	}
	@Override
	public void stopLogging(final OnStopCompleteListener listener) {
    	sensorManager.unregisterListener(sensorEventListener);
		logDataBase.close(listener);
	}
	
	
	private static String getStringType(final int type) {
		String[] result = getStringTypeFromIntType(type);
		return result[0];
	}
	private static String getHeader(final int type) {
		String[] result= getStringTypeFromIntType(type);
		return result[1];
	}
	
	private static String[] getStringTypeFromIntType(final int type) {
		String name = "unknown";
		String header = "";
		switch (type) {
		case Sensor.TYPE_ACCELEROMETER:
			name = "accelerometer";
			header = "accelX-gX(m/s^2),accelY-gY(m/s^2),accelZ-gZ(m/s^2)";
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			name = "magnetic_filed";
			header = "x(uT),y(uT),z(uT)";
			break;
		case Sensor.TYPE_GYROSCOPE:
			name = "gyroscope";
			header = "AngularSpeedX,AngularSpeedY,AngularSpeedZ";
			break;
		case Sensor.TYPE_LINEAR_ACCELERATION:
			name = "linear_acceleration";
			header = "x(m/s^2),y(m/s^2),z(m/s^2)";
			break;
		case Sensor.TYPE_GRAVITY:
			name = "gravity";
			header = "x(m/s^2),y(m/s^2),z(m/s^2)";
			break;
		case Sensor.TYPE_ROTATION_VECTOR:
			name = "rotation_vector";
			header = "xsin(theta/2),ysin(theta/2),zsin(theta/2)";
			break;
		case Sensor.TYPE_LIGHT:
			name ="light";
			header = "Light(lux)";
			break;
		case Sensor.TYPE_PRESSURE:
			name = "pressure";
			header = "Pressure(hPa)";
			break;
		case Sensor.TYPE_PROXIMITY:
			name = "proximity";
			header = "Proximity(cm)";
			
			break;
		case Sensor.TYPE_RELATIVE_HUMIDITY:
			name ="relative_humidity";
			header = "RelativeHumidity(%)";
			break;
		case Sensor.TYPE_AMBIENT_TEMPERATURE:
			name ="ambient_temperature";
			header = "AmbientTemperature(C)";
			break;
		}
		name = "sensor." + name;
		String[] result = {name, header};
		return result;
		
	}
}
