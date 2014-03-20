package com.JNJABA.monitor;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class MainMenuService extends Service implements LocationListener{
	private static final String TAG = "Monitor-Service";
	private static final int DELAY = 1000;
	private static final int FAST = 1;
	private static final int SLOW = 0;
	private static final int NOTIFICATION_ID = 1;
	
	private Location lastLocation;
	private LocationManager locationManager;
	private boolean hasFallen = false;
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private int startId;
	
	private final int UPDATE_TIME = 900000;       //15 min
	private final int UPDATE_DISTANCE = 4000000;  //Span of the continental US
	
	public void onCreate() {
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		settings = getApplicationContext().getSharedPreferences(getResources().getString(R.string.monitor_data), MODE_PRIVATE);
		editor = settings.edit();
		
		final Intent notificationIntent = new Intent(getApplicationContext(),
                MainMenuActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
		
		final Notification notification = new Notification.Builder(getApplicationContext())
		.setSmallIcon(android.R.drawable.ic_media_play)
        .setOngoing(true).setContentTitle("Monitoring")
        .setContentText("Click to access Monitoring app")
        .setContentIntent(pendingIntent).build();
		
		startForeground(NOTIFICATION_ID, notification);
	}
	
	//The intent will contain any data the service needs to use
	public int onStartCommand(Intent intent, int flags, int startId) {	
		this.startId = startId;
		
		callGPS(SLOW, hasFallen);
		
		//Need to redo the handler to deal with new threads
		final Handler handler = new Handler();
		
		Thread background = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try{
					log("Thread starting");
					handleActionMonitor();
				} catch (Exception e) {
					log("Exception caught");
					log(e.getMessage());
				}
				
				handler.postDelayed(this, DELAY);
			}
			
		});
		
		background.start();
		if(background.isAlive())
			log("Background Thread is alive");
		if(!background.isAlive())
			log("Background Thread died");
		if(background.isInterrupted())
			log("Interupted??");
		
		return START_CONTINUATION_MASK;
	}
	
	//Start Monitoring for fall
	private void handleActionMonitor() {
		if(fallDetected()) {
			callGPS(FAST, hasFallen);
			sendWarning();
			hasFallen = false;
		}
	}
	
	//Get GPS location
	//Use FAST speed if emergency is detected otherwise go SLOW
	private void callGPS(int speed, boolean emergency) {		
		
		//updates GPS every 15 minutes or when someone moves across the span of the US
		//not meant to be updated based on area traveled.
		
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_TIME, UPDATE_DISTANCE, this);
		lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		
		//Stores lastLocation into phone app memory for later access
		editor.putString(getResources().getString(R.string.user_location_latitude),
				String.valueOf(lastLocation.getLatitude()));
		editor.putString(getResources().getString(R.string.user_location_longitude),
				String.valueOf(lastLocation.getLongitude()));
		
		log(Double.toString(lastLocation.getLatitude()));
		log(Double.toString(lastLocation.getLongitude()));
		
		editor.commit();
	}
	
	//Sends data to Server periodically or on Emergency
	private void storeData() {
		//Will most likely have to start a new thread
		return;
	}
	
	//Runs the fall detection algorithm
	private boolean fallDetected() {
		
		//Test for fall
		if(hasFallen) {
			editor.putBoolean(getResources().getString(R.string.user_fall_status), true);
			hasFallen = true;
			return true;
		}
		
		return false;
	}
	
	//Starts the activity to warn of fall. last GPS location is sent
	private void sendWarning() {
		Intent emergency = new Intent(getApplicationContext(), EmergencyActivity.class);
		emergency.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		emergency.putExtra("latitude", lastLocation.getLatitude());
		emergency.putExtra("longitude", lastLocation.getLongitude());
		
		startActivity(emergency);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private long age(Location location) {
		return System.currentTimeMillis() - location.getTime();
	}
	
	@Override
	public void onLocationChanged(Location currentLocation) {
		// TODO Auto-generated method stub
		
		if(lastLocation == null || (age(currentLocation) > age(lastLocation))) {
			lastLocation = currentLocation;
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	private void log(String msg) {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.i(TAG, msg);
	}
}
