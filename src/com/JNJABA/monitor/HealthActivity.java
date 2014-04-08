package com.JNJABA.monitor;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class HealthActivity extends Activity implements Runnable{
	private static final String TAG = "Monitor-Health";
	
	private static final int SECOND = 1000;
	private static final int DELAY = 5 * SECOND;
	
	private TextView tvHeartRate, tvWalkingSpeed, tvFallStatus, tvLocation, tvOverallHealth;
	private final Handler handler = new Handler();
	
	private SharedPreferences settings;
	private SharedPreferences.Editor edit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_health);
		
		settings = getApplicationContext().getSharedPreferences(getResources().getString(R.string.monitor_data), MODE_PRIVATE);
		edit = settings.edit();
		
		tvHeartRate = (TextView) findViewById(R.id.tvHeartRateData);
		tvWalkingSpeed = (TextView) findViewById(R.id.tvWalkingSpeedData);
		tvFallStatus = (TextView) findViewById(R.id.tvFallStatusData);
		tvLocation = (TextView) findViewById(R.id.tvLocationData);
		tvOverallHealth = (TextView) findViewById(R.id.tvOverallHealthData);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		edit.putBoolean(getResources().getString(R.string.in_health_activity), true);
		edit.apply();
		
		handler.post(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		edit.putBoolean(getResources().getString(R.string.in_health_activity), false);
		edit.apply();
		handler.removeCallbacks(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.health, menu);
		return true;
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
	public void run() {
		// TODO Auto-generated method stub
		try {
			String latitude = settings.getString(getResources().getString(R.string.user_location_latitude), "Unknown").toString();
			log(latitude.toString());
			String longitude = settings.getString(getResources().getString(R.string.user_location_longitude), "Unknown").toString();
			log(longitude.toString());
			
			tvHeartRate.setText(settings.getString(getResources().getString(R.string.user_heart_rate), "Unknown").toString());
			tvWalkingSpeed.setText(settings.getString(getResources().getString(R.string.user_walking_speed), "Unknown").toString());
			tvFallStatus.setText(settings.getString(getResources().getString(R.string.user_fall_status), "Unknown").toString());
			tvLocation.setText(latitude + ", " + longitude);
			tvOverallHealth.setText(settings.getString(getResources().getString(R.string.user_overall_health), "Unknown").toString());
		} catch (Exception e) {
			log("Exception thrown");
			e.printStackTrace();
		}
		log("About to recall handler");
		handler.postDelayed(this, DELAY);
	}
}
