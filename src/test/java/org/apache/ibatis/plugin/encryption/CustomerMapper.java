package org.apache.ibatis.plugin.encryption;

import org.apache.ibatis.annotations.Mapper;
/**
 * @author SoungRyoul Kim
 * Thank my mentor Ikchan Sim who taught me.
 */
@Mapper
public interface CustomerMapper {

	void insert(Customer customer);

	Customer select(String id);

	
}
