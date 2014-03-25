package com.JNJABA.monitor;

public class EmergencyContact {
	private String phoneNumber;
	private String email;
	private String firstName, lastName;
	private String relation;
	
	public EmergencyContact(String uPhoneNumber, String uEmail, String uFirstName, String uLastName, String uRelation) {
		phoneNumber = uPhoneNumber;
		email = uEmail;
		firstName = uFirstName;
		lastName = uLastName;
		relation = uRelation;
	}
	
	public String toString() {
		return "";
	}
	
	public String getPhoneNumber() {return phoneNumber;}
	public void setPhoneNumber(String phoneNumber) {this.phoneNumber = phoneNumber;}
	public String getEmail() {return email;}
	public void setEmail(String email) {this.email = email;}
	public String getFirstName() {return firstName;}
	public void setFirstName(String firstName) {this.firstName = firstName;}
	public String getLastName() {return lastName;}
	public void setLastName(String lastName) {this.lastName = lastName;}
	public String getRelation() {return relation;}
	public void setRelation(String relation) {this.relation = relation;}
}
