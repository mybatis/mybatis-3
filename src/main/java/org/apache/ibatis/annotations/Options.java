package org.apache.ibatis.annotations;

import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.mapping.StatementType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Options {
  public abstract boolean useCache() default true;

  public abstract boolean flushCache() default false;

  public abstract ResultSetType resultSetType() default ResultSetType.FORWARD_ONLY;

  public abstract StatementType statementType() default StatementType.PREPARED;

  public abstract int fetchSize() default -1;

  public abstract int timeout() default -1;

  public abstract boolean useGeneratedKeys() default false;

  public abstract String keyProperty() default "id";
  
  public abstract String keyColumn() default ""; 
}
