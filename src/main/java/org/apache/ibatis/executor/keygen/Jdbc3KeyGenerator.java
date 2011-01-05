package org.apache.ibatis.executor.keygen;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class Jdbc3KeyGenerator implements KeyGenerator {

  private String keyColumnName;
  
  public Jdbc3KeyGenerator(String keyColumnName) {
    this.keyColumnName = keyColumnName;
  }
    
  public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
  }

  public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
    try {
      final Configuration configuration = ms.getConfiguration();
      final TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
      if (parameter != null) {
        String keyProperty = ms.getKeyProperty();
        final MetaObject metaParam = configuration.newMetaObject(parameter);
        if (keyProperty != null && metaParam.hasSetter(keyProperty)) {
          Class<?> keyPropertyType = metaParam.getSetterType(keyProperty);
          TypeHandler th = typeHandlerRegistry.getTypeHandler(keyPropertyType);
          if (th != null) {
            ResultSet rs = stmt.getGeneratedKeys();
            try {
              ResultSetMetaData rsmd = rs.getMetaData();
              int colCount = rsmd.getColumnCount();
              if (colCount > 0) {
                String colName;
                if (keyColumnName != null && keyColumnName.length() > 0) {
                  colName = keyColumnName;
                } else {
                  colName = rsmd.getColumnName(1);
                }
              
                while (rs.next()) {
                  Object value = th.getResult(rs, colName);
                  metaParam.setValue(keyProperty, value);
                }
              }
            } finally {
              try {
                if (rs != null) rs.close();
              } catch (Exception e) {
                //ignore
              }
            }
          }
        }
      }
    } catch (Exception e) {
      throw new ExecutorException("Error getting generated key or setting result to parameter object. Cause: " + e, e);
    }
  }
}
