package com.JNJABA.monitor;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/*
 * The way to store data is by calling these functions
 * SharedPreferences settings = getApplicationContext().getSharedPreferences(getResources().getString(R.string.monitor_data), MODE_PRIVATE);
 * SharedPreferences.Editor editor = settings.edit();
 * 
 * editor.putString("name", "Brian");
 * editor.apply();
 * 
 * The way to get data from our project(though I don't think you will need to in this activity) is
 * 
 * String userName = settings.getString(getResources().getString(R.string.user_username), false);
 */

public class LoginActivity extends Activity {
	private Profile profile;
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		Button login = (Button) findViewById(R.id.bLogin);
		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginActivity.this, MainMenuActivity.class));
			}
			
		});
		
		profile = new Profile(this);
		updateProfile();
		
	}
	
	//Add all acquired values here that the profile will need
	private void updateProfile() {
		profile.setFirstName("James");
		profile.setLastName("Whalen");
		profile.setActivityLevel(3);
		profile.setAge(21);
		profile.setBmi(18.5);
		profile.setEmail("james_whalen@student.uml.edu");
		profile.setAddress("141 Marginal St, Lowell MA");
		profile.setSex("Male");
		profile.setHeight(6);
		profile.setWeight(190);
		
		Log.d("Login Activity", "Contact should be added");
		
		profile.addEmergencyContact(new EmergencyContact(profile)
				.setFirstName("Brian")
				.setLastName("Mandel")
				.setPhoneNumber("555")
				.setRelation("Best Friend"));
		
		profile.updatePreferences();
	}
	
	@Override
	protected void onStop() {
		finish();
		
		super.onStop();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

}
