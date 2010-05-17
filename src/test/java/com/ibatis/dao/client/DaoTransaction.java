package com.ibatis.dao.client;

/**
 * This interface marks a class as being a DaoTransaction.  Some examples
 * of DAO Transaction implementations would be JDBC, JTA, SQL Map and
 * Hibernate.
 * <p/>
 * No methods are declared by this interface, but DAO templates are
 * provided to simplify accessing implementation specific transaction
 * artifacts such as JDBC connections etc.
 * <p/>
 *
 * @see com.ibatis.dao.client.template.DaoTemplate
 */
public interface DaoTransaction {

}
