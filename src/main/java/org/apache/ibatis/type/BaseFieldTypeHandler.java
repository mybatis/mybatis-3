package org.apache.ibatis.type;

import java.lang.reflect.Field;

public abstract class BaseFieldTypeHandler<T> extends BaseTypeHandler<T> implements FieldTypeHandler<T> {
  private Field field;

  @Override
  public Field getField() {
    return field;
  }

  @Override
  public void setField(Field field) {
    this.field = field;
  }
}
