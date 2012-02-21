package org.apache.ibatis.submitted.clearcache;

public interface PersonDAO {
	public void create(Person p);	
	public Person findById(int id);	
	public void update(Person p);	
	public void delete(int id);
	public void deleteAddress(int addressId);
}
