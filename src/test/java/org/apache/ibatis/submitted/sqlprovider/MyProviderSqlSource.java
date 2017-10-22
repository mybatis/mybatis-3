package org.apache.ibatis.submitted.sqlprovider;

import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author liuzh
 */
public class MyProviderSqlSource extends ProviderSqlSource {
  private final LanguageDriver languageDriver = new XMLLanguageDriver();
  private SqlSource sqlSource;

  public MyProviderSqlSource(Configuration configuration, Object provider) {
    super(configuration, provider);
  }

  public MyProviderSqlSource(Configuration configuration, Object provider, Class<?> mapperType, Method mapperMethod) {
    super(configuration, provider, mapperType, mapperMethod);
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    if (sqlSource == null) {
      synchronized (this) {
        if (sqlSource == null) {
          sqlSource = createSqlSource(parameterObject);
        }
      }
    }
    return sqlSource.getBoundSql(parameterObject);
  }

  @Override
  protected SqlSource createSqlSource(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) {
    return languageDriver.createSqlSource(configuration, originalSql, parameterType);
  }

}
