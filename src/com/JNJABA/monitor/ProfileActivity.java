package com.JNJABA.monitor;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class ProfileActivity extends Activity {
	private Profile profile = Profile.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		TextView firstName = (TextView) findViewById(R.id.tvFirstName);
		firstName.setText(profile.getFirstName());
		TextView lastName = (TextView) findViewById(R.id.tvLastName);
		lastName.setText(profile.getLastName());
		TextView age = (TextView) findViewById(R.id.tvAge);
		age.setText("" + profile.getAge());
		TextView sex = (TextView) findViewById(R.id.tvSex);
		sex.setText(profile.getSex());
		TextView street = (TextView) findViewById(R.id.tvStreet);
		street.setText(profile.getAddress());
		TextView city = (TextView) findViewById(R.id.tvCity);
		city.setText(profile.getAddress());
		TextView state = (TextView) findViewById(R.id.tvOptional);
		state.setText(profile.getAddress());
		TextView phoneNumber = (TextView) findViewById(R.id.tvPhoneNumber);
		phoneNumber.setText("Not available");
		TextView email = (TextView) findViewById(R.id.tvEmail);
		email.setText(profile.getEmail());
		TextView activityLevel = (TextView) findViewById(R.id.tvActivityLevel);
		activityLevel.setText("" + profile.getActivityLevel());
		TextView BMI = (TextView) findViewById(R.id.tvBMI);
		BMI.setText(String.valueOf("" + profile.getBmi()));
		
		TextView emergencyName = (TextView) findViewById(R.id.tvEmergencyName);
		TextView emergencyPhone = (TextView) findViewById(R.id.tvEmergencyNumber);
		TextView emergencyRelation = (TextView) findViewById(R.id.tvEmergencyRelation);
		
		if(profile.getEmergencyContact(1) != null) {
			Log.d("Profile Activity", "Emergency contact is not null");
			//The 1 gets the 1st Contact Object
			emergencyName.setText(profile.getEmergencyContact(1).getFirstName() + " " + profile.getEmergencyContact(1).getLastName());
			emergencyPhone.setText(profile.getEmergencyContact(1).getPhoneNumber());
			emergencyRelation.setText(profile.getEmergencyContact(1).getRelation());
		}
		else
			Log.d("Profile Activity", "Emergency contact is null!!!");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile, menu);
		return true;
	}

}
