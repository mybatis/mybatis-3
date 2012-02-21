package org.apache.ibatis.submitted.clearcache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Person implements Serializable {
	private int id;
	private String firstname;
	private String lastname;
	private List<Address> addresses = new ArrayList<Address>();
	
	public Person() {}
	
	public Person(int id, String firstname, String lastname) {
		setId(id);
		setFirstname(firstname);
		setLastname(lastname);
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getFirstname() {
		return firstname;
	}
	
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	
	public String getLastname() {
		return lastname;
	}
	
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
		
	public List<Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id=" + id);
		sb.append(", lastname=" + lastname);
		sb.append(", firstname=" + firstname);
		return sb.toString();
	}
	
	
}
