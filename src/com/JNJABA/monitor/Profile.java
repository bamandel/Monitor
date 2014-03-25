package com.JNJABA.monitor;

import android.graphics.Bitmap;

public class Profile {
	private String firstName, lastName;
	private int age;
	private String sex;
	private int weight, height;
	private String email;
	private String address1, address2;
	private double bmi;
	private int activityLevel;
	private Bitmap proPic;
	private EmergencyContact emergencyContact;
	
	public Profile(String firstName, String lastName, int age, String sex,
			int weight, int height, String email, String address1,
			String address2, double bmi, int activityLevel) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
		this.sex = sex;
		this.weight = weight;
		this.height = height;
		this.email = email;
		this.address1 = address1;
		this.address2 = address2;
		this.bmi = bmi;
		this.activityLevel = activityLevel;
	}
	
	public void addProPic(Bitmap pic) {proPic = pic;}
	public Bitmap getProPic() {return proPic;}
	public void addAddress2(String address2) {this.address2 = address2;}
	public String getAddress2() {return address2;}
	public void addEmergencyContact(EmergencyContact contact) {emergencyContact = contact;}
	public EmergencyContact getEmergencyContact() {return emergencyContact;}
	
	public String toString() {
		return "";
	}
	
	public String getFirstName() {return firstName;}
	public void setFirstName(String firstName) {this.firstName = firstName;}
	public String getLastName() {return lastName;}
	public void setLastName(String lastName) {this.lastName = lastName;}
	public int getAge() {return age;}
	public void setAge(int age) {this.age = age;}
	public String getSex() {return sex;}
	public void setSex(String sex) {this.sex = sex;}
	public int getWeight() {return weight;}
	public void setWeight(int weight) {this.weight = weight;}
	public int getHeight() {return height;}
	public void setHeight(int height) {this.height = height;}
	public String getEmail() {return email;}
	public void setEmail(String email) {this.email = email;}
	public String getAddress() {return address1;}
	public void setAddress(String address1) {this.address1 = address1;}
	public double getBmi() {return bmi;}
	public void setBmi(double bmi) {this.bmi = bmi;}
	public int getActivityLevel() {return activityLevel;}
	public void setActivityLevel(int activityLevel) {this.activityLevel = activityLevel;}
}
