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
        String[] keyProperties = delimitedStringtoArray(ms.getKeyProperty());
        final MetaObject metaParam = configuration.newMetaObject(parameter);
        if (keyProperties != null) {
          TypeHandler<?>[] typeHandlers = new TypeHandler<?>[keyProperties.length];
          // calculate type handlers for the key properties
          for (int i = 0; i < keyProperties.length; i++) {
            if (metaParam.hasSetter(keyProperties[i])) {
              Class<?> keyPropertyType = metaParam.getSetterType(keyProperties[i]);
              TypeHandler<?> th = typeHandlerRegistry.getTypeHandler(keyPropertyType);
              typeHandlers[i] = th;
            }
          }
          
          ResultSet rs = stmt.getGeneratedKeys();
          try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();
            if (colCount >= keyProperties.length) {
              while (rs.next()) {
                for (int i = 0; i < keyProperties.length; i++) {
                  TypeHandler<?> th = typeHandlers[i];
                  if (th != null) {
                    Object value = th.getResult(rs, i + 1);
                    metaParam.setValue(keyProperties[i], value);
                  }
                }
              }
            }
          } finally {
            if (rs != null) {
              try {
                rs.close();
              } catch (Exception e) {
                // ignore
                ;
              }
            }
          }
        }
      }
    } catch (Exception e) {
      throw new ExecutorException("Error getting generated key or setting result to parameter object. Cause: " + e, e);
    }
  }

  /**
   * Return a String array of key column names.  This is used for the
   * case where the driver requires that the generated key column
   * be called out (Oracle and PostgreSQL).  In these cases, the driver
   * will not correctly return the generated key unless the field is named.
   * 
   * We allow more than one column name, and similarly we allow more then one
   * key property, for the case where the table contains more than one generated value.
   * 
   * @return
   */
  public String[] getKeyColumnNames() {
    return delimitedStringtoArray(keyColumnName);
  }
  
  private static String[] delimitedStringtoArray(String in) {
    if (in == null || in.trim().length() == 0) {
      return null;
    } else {
      String[] answer = in.split(",");
      return answer;
    }  
  }
}
