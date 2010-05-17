package com.ibatis.sqlmap;

import org.apache.ibatis.reflection.factory.ObjectFactory;
import com.testdomain.*;

import java.util.List;
import java.util.Properties;

/**
 * @author Jeff Butler
 */
public class ResultObjectFactoryImpl implements ObjectFactory {

  /**
   *
   */
  public ResultObjectFactoryImpl() {
    super();
  }

  /* (non-Javadoc)
   * @see com.ibatis.sqlmap.engine.mapping.result.ResultObjectFactory#createInstance(java.lang.String, java.lang.Class)
   */
  public Object create(Class clazz) {

    Object obj = null;

    if (clazz.equals(IItem.class)) {
      obj = new IItemImpl();
    } else if (clazz.equals((ISupplier.class))) {
      obj = new ISupplierImpl();
    } else if (clazz.equals((ISupplierKey.class))) {
      obj = new ISupplierKeyImpl();
    }

    return obj;
  }

  public Object create(Class type, List<Class> constructorArgTypes, List<Object> constructorArgs) {
    return create(type);
  }

  public void setProperties(Properties properties) {
  }
}
