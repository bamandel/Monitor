package com.JNJABA.monitor;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

public class MainMenuService extends Service {
	private static final String TAG = "Monitor-Service";
	private static final int FAST = 1;
	private static final int SLOW = 0;
	
	private Location lastLocation;
	private boolean hasFallen = false;
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private int startId;
	
	private final int UPDATE_TIME = 900000;       //15 min
	private final int UPDATE_DISTANCE = 4000000;  //Span of the continental US
	
	public void onCreate() {
		settings = getApplicationContext().getSharedPreferences(getResources().getString(R.string.monitor_data), MODE_PRIVATE);
		editor = settings.edit();
		
		startForeground(1, new Notification());
		
		handleActionMonitor();
	}
	
	//The intent will contain any data the service needs to use
	public int onStartCommand(Intent intent, int flags, int startId) {	
		this.startId = startId;
		
		return START_CONTINUATION_MASK;
	}
	
	//Start Monitoring for fall
	private void handleActionMonitor() {
		callGPS(SLOW, false);
		
		if(fallDetected()) {
			callGPS(FAST, hasFallen);
			sendWarning();
			hasFallen = false;
		}
	}
	
	//Get GPS location
	//Use FAST speed if emergency is detected otherwise go SLOW
	private void callGPS(int speed, boolean emergency) {
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		Criteria criteria = new Criteria();
		criteria.setCostAllowed(false);
		
		if(emergency) {
			criteria.setAccuracy(Criteria.ACCURACY_HIGH);
		}
		else {
			criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
		}
		
		String bestProvider = locationManager.getBestProvider(criteria, false);
		
		LocationListener locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				//Sends data to the Cloud or stores it on local database
				//makeUseOfNewLocation(location);
				/*
				 * Location is your current location
				 * store it and access it
				 */
			}

			@Override
			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}
		};
		
		//updates GPS every 15 minutes or when someone moves across the span of the US
		//not meant to be updated based on area traveled.
		
		if(!emergency) {
			locationManager.requestLocationUpdates(bestProvider, UPDATE_TIME, UPDATE_DISTANCE, locationListener);
		}
		else {
			locationManager.requestLocationUpdates(bestProvider, 0, 0, locationListener);
		}
		
		lastLocation = locationManager.getLastKnownLocation(bestProvider);
		
		//Stores lastLocation into phone app memory for later access
		editor.putLong(getResources().getString(R.string.user_location_latitude),
				Double.doubleToRawLongBits(lastLocation.getLatitude()));
		editor.putLong(getResources().getString(R.string.user_location_longitude),
				Double.doubleToRawLongBits(lastLocation.getLongitude()));
	}
	
	//Sends data to Server periodically or on Emergency
	private void storeData() {
		
		
	}
	
	//Runs the fall detection algorithm
	private boolean fallDetected() {
		
		//Test for fall
		if(false) {
			editor.putBoolean(getResources().getString(R.string.user_fall_status), true);
			return true;
		}
		
		return false;
	}
	
	//Starts the activity to warn of fall. last GPS location is sent
	private void sendWarning() {
		Intent emergency = new Intent(getApplicationContext(), EmergencyActivity.class);
		
		emergency.putExtra("latitude", lastLocation.getLatitude());
		emergency.putExtra("longitude", lastLocation.getLongitude());
		
		startActivity(emergency);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
