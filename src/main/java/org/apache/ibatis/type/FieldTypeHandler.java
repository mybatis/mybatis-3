package org.apache.ibatis.type;

import java.lang.reflect.Field;

public interface FieldTypeHandler<T> extends TypeHandler<T> {

  Field getField();

  void setField(Field field);
}
