package com.JNJABA.monitor;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.Menu;
import android.widget.TextView;

public class HealthActivity extends Activity {
	private static final int DELAY = 500;
	
	private SharedPreferences settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_health);
		
		settings = getApplicationContext().getSharedPreferences(getResources().getString(R.string.monitor_data), MODE_PRIVATE);
		
		final TextView tvHeartRate = (TextView) findViewById(R.id.tvHeartRateData);
		final TextView tvWalkingSpeed = (TextView) findViewById(R.id.tvWalkingSpeedData);
		final TextView tvFallStatus = (TextView) findViewById(R.id.tvFallStatusData);
		final TextView tvLocation = (TextView) findViewById(R.id.tvLocationData);
		final TextView tvOverallHealth = (TextView) findViewById(R.id.tvOverallHealthData);
		
		final Handler handler = new Handler();
		handler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String latitude = settings.getString(getResources().getString(R.string.user_location_latitude), "Uknown");
				String longitude = settings.getString(getResources().getString(R.string.user_location_longitude), "Unknown");
				
				tvHeartRate.setText(settings.getString(getResources().getString(R.string.user_heart_rate), "Unkown"));
				tvWalkingSpeed.setText(settings.getString(getResources().getString(R.string.user_walking_speed), "Unknown"));
				tvFallStatus.setText(settings.getString(getResources().getString(R.string.user_fall_status), "Unknown"));
				tvLocation.setText(latitude + ", " + longitude);
				tvOverallHealth.setText(settings.getString(getResources().getString(R.string.user_overall_health), "Unknown"));
				
				handler.postDelayed(this, DELAY);
			}
			
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.health, menu);
		return true;
	}

}
