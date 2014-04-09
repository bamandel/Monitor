package com.JNJABA.monitor;

import android.content.Context;
import android.content.SharedPreferences;

public class EmergencyContact {
	private static int relationInstances = 1;
	private int relationNumber;
	private Profile profile;
	
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private Context context;
	
	public EmergencyContact(Profile pro) {
		profile = pro;
		settings = profile.getSettings();
		editor = profile.getEditor();
		context = profile.getContext();
		
		relationNumber = relationInstances;
		relationInstances++;
	}
	
	public String toString() {
		return "";
	}
	
	public void updatePreferences() {
		editor.apply();
	}
	
	public String getPhoneNumber() {return settings.getString(context.getResources().getString(R.string.emergency_phone_number), "Unknown");}
	public EmergencyContact setPhoneNumber(String phoneNumber) {
		editor.putString(context.getString(R.string.emergency_phone_number), phoneNumber);
		return this;
	}
	public String getEmail() {return settings.getString(context.getResources().getString(R.string.emergency_email), "Unknown");}
	public EmergencyContact setEmail(String email) {
		editor.putString(context.getString(R.string.emergency_email), email);
		return this;
	}
	public String getFirstName() {return settings.getString(context.getResources().getString(R.string.emergency_first_name), "Unknown");}
	public EmergencyContact setFirstName(String firstName) {
		editor.putString(context.getString(R.string.emergency_first_name), firstName);
		return this;
	}
	public String getLastName() {return settings.getString(context.getResources().getString(R.string.emergency_last_name), "Unknown");}
	public EmergencyContact setLastName(String lastName) {
		editor.putString(context.getString(R.string.emergency_last_name), lastName);
		return this;
	}
	public String getRelation() {return settings.getString(context.getResources().getString(R.string.emergency_relation), "Unknown");}
	public EmergencyContact setRelation(String relation) {
		editor.putString(context.getString(R.string.emergency_relation), relation);
		return this;
	}
	public int getRelationNumber() {return relationNumber;}
}
