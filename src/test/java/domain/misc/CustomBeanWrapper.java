package domain.misc;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.BeanWrapper;

public class CustomBeanWrapper extends BeanWrapper {
  public CustomBeanWrapper(MetaObject metaObject, Object object) {
    super(metaObject, object);
  }
}
