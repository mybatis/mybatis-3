package domain.misc;

import domain.jpetstore.Product;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;

public class CustomBeanWrapperFactory implements ObjectWrapperFactory {
  public boolean hasWrapperFor(Object object) {
    if (object instanceof Product) {
      return true;
    } else {
      return false;
    }
  }
  
  public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
    return new CustomBeanWrapper(metaObject, object);
  }
}
