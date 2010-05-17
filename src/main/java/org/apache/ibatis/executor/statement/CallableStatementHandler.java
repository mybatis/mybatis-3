package org.apache.ibatis.executor.statement;

import org.apache.ibatis.executor.*;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.util.List;

public class CallableStatementHandler extends BaseStatementHandler {

  public CallableStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) {
    super(executor, mappedStatement, parameter, rowBounds, resultHandler);
  }

  public int update(Statement statement)
      throws SQLException {
    CallableStatement cs = (CallableStatement) statement;
    cs.execute();
    int rows = cs.getUpdateCount();
    Object parameterObject = boundSql.getParameterObject();
    KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
    keyGenerator.processAfter(executor, mappedStatement, cs, parameterObject);
    resultSetHandler.handleOutputParameters(cs);
    return rows;
  }

  public void batch(Statement statement)
      throws SQLException {
    CallableStatement cs = (CallableStatement) statement;
    cs.addBatch();
  }

  public List query(Statement statement, ResultHandler resultHandler)
      throws SQLException {
    CallableStatement cs = (CallableStatement) statement;
    cs.execute();
    List resultList = resultSetHandler.handleResultSets(cs);
    resultSetHandler.handleOutputParameters(cs);
    return resultList;
  }

  protected Statement instantiateStatement(Connection connection) throws SQLException {
    String sql = boundSql.getSql();
    if (mappedStatement.getResultSetType() != null) {
      return connection.prepareCall(sql, mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
    } else {
      return connection.prepareCall(sql);
    }
  }

  public void parameterize(Statement statement) throws SQLException {
    KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
    ErrorContext.instance().store();
    keyGenerator.processBefore(executor, mappedStatement, statement, boundSql.getParameterObject());
    ErrorContext.instance().recall();
    rebindGeneratedKey();
    registerOutputParameters((CallableStatement) statement);
    parameterHandler.setParameters((CallableStatement) statement);
  }

  private void registerOutputParameters(CallableStatement cs) throws SQLException {
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    for (int i = 0, n = parameterMappings.size(); i < n; i++) {
      ParameterMapping parameterMapping = parameterMappings.get(i);
      if (parameterMapping.getMode() == ParameterMode.OUT || parameterMapping.getMode() == ParameterMode.INOUT) {
        if (null == parameterMapping.getJdbcType()) {
          throw new ExecutorException("The JDBC Type must be specified for output parameterArray.  Paramter: " + parameterMapping.getProperty());
        } else {
          if (parameterMapping.getNumericScale() != null && (parameterMapping.getJdbcType() == JdbcType.NUMERIC || parameterMapping.getJdbcType() == JdbcType.DECIMAL)) {
            cs.registerOutParameter(i + 1, parameterMapping.getJdbcType().TYPE_CODE, parameterMapping.getNumericScale());
          } else {
            if (parameterMapping.getJdbcTypeName() == null) {
              cs.registerOutParameter(i + 1, parameterMapping.getJdbcType().TYPE_CODE);
            } else {
              cs.registerOutParameter(i + 1, parameterMapping.getJdbcType().TYPE_CODE, parameterMapping.getJdbcTypeName());
            }
          }
        }
      }
    }
  }

}
