package com.JNJABA.monitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

public class MainMenuService extends Service implements LocationListener {
	private static final String TAG = "Monitor-Service";
	private static final int NOTIFICATION_ID = 1;
	private static final String CLOUD_WEBPAGE = "I do no know yet";
	private static final long DELAY = 5000;
	
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	
	private Location lastLocation;
	private LocationManager locationManager;
	private boolean hasFallen = false;
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;

	private final int UPDATE_TIME = 900000; // 15 min
	private final int UPDATE_DISTANCE = 15000; // around 9 miles
	
	private final class ServiceHandler extends Handler {
	      public ServiceHandler(Looper looper) {
	          super(looper);
	      }
	      @Override
	      public void handleMessage(Message msg) {
	    	  this.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					handleActionMonitor();
					updatePreferences();
					
					ServiceHandler.this.postDelayed(this, DELAY);
				}
	    	  });
	      }
	}
	
	public void onCreate() {
		HandlerThread background = new HandlerThread("ServiceStartsThread", Process.THREAD_PRIORITY_BACKGROUND);
		background.start();
		mServiceLooper = background.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		settings = getApplicationContext().getSharedPreferences(getResources().getString(R.string.monitor_data), MODE_PRIVATE);
		editor = settings.edit();
		
		final Intent notificationIntent = new Intent(getApplicationContext(), MainMenuActivity.class);
		final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		final Notification notification = new Notification.Builder(
				getApplicationContext())
				.setSmallIcon(android.R.drawable.ic_media_play)
				.setOngoing(true).setContentTitle("Monitoring")
				.setContentText("Click to access Monitoring app")
				.setContentIntent(pendingIntent).build();

		callGPS(hasFallen);
		
		startForeground(NOTIFICATION_ID, notification);
	}

	// The intent will contain any data the service needs to use
	public int onStartCommand(Intent intent, int flags, int startId) {
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);
		
		return START_STICKY;
	}

	// Start Monitoring for fall
	private void handleActionMonitor() {
		callGPS(hasFallen);
		
		if (fallDetected()) {
			callGPS(hasFallen);
			//storeData();
			sendWarning();
			hasFallen = false;
		}
	}

	// Get GPS location
	private void callGPS(boolean emergency) {

		// updates GPS every 15 minutes or when someone moves across the span of
		// the US
		// not meant to be updated based on area traveled.
		
		if(emergency || settings.getBoolean(getResources().getString(R.string.in_health_activity), false)) {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
			lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		else {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_TIME, UPDATE_DISTANCE,this);
			lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		
		updatePreferences();
	}
	
	private void updatePreferences() {
		editor.putString(getResources().getString(R.string.user_location_latitude), String.valueOf(lastLocation.getLatitude()));
		editor.putString(getResources().getString(R.string.user_location_longitude),String.valueOf(lastLocation.getLongitude()));
		editor.putString(getResources().getString(R.string.user_fall_status), Boolean.toString(hasFallen));
		
		log(settings.getString(getResources().getString(R.string.user_location_latitude), "No Lat Value"));
		log(settings.getString(getResources().getString(R.string.user_location_longitude), "No Lon Value"));
		log(settings.getString(getResources().getString(R.string.user_fall_status), "No Fall Value"));
		
		editor.apply();
	}
	
	// Sends data to Server periodically or on Emergency
	private void storeData() {
		// Will most likely have to start a new thread
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost post = new HttpPost(CLOUD_WEBPAGE);
		
		try {
			List<NameValuePair> values = new ArrayList<NameValuePair>();
			//Send my data
			post.setEntity(new UrlEncodedFormEntity(values));
			
			HttpResponse response = httpClient.execute(post);
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Runs the fall detection algorithm
	private boolean fallDetected() {

		// Test for fall
		if (hasFallen) {
			hasFallen = true;
			updatePreferences();
			return true;
		}

		return false;
	}

	// Starts the activity to warn of fall. last GPS location is sent
	private void sendWarning() {
		Intent emergency = new Intent(getApplicationContext(),
				EmergencyActivity.class);
		emergency.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		emergency.putExtra("latitude", lastLocation.getLatitude());
		emergency.putExtra("longitude", lastLocation.getLongitude());
		emergency.putExtra("emergency number", settings.getString(getResources().getString(R.string.emergency_phone_number), "tel:555"));

		startActivity(emergency);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLocationChanged(Location currentLocation) {
		// TODO Auto-generated method stub

		lastLocation = currentLocation;
		updatePreferences();
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

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		locationManager.removeUpdates(this);
	}
	
}
