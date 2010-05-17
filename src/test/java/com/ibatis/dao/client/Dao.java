package com.ibatis.dao.client;

/**
 * The interface that identifies and describes Data Access Objects.
 * <p/>
 * No methods are declared by this interface.  However, if you provide
 * a constructor with a single parameter of type DaoManager,
 * that constructor will be used to instantiate the Dao such and
 * the managing DaoManager instance will be passed in as a parameter.
 * The DaoManager instance will allow you to easily access transactions
 * and other DAOs.
 * <p/>
 */
public interface Dao {

}
