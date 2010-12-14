package org.apache.ibatis.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.ibatis.mapping.StatementType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SelectKey {
  public abstract String[] statement();
  public abstract String keyProperty();
  public abstract boolean before();
  public abstract Class<?> resultType();
  public abstract StatementType statementType() default StatementType.PREPARED;
}
