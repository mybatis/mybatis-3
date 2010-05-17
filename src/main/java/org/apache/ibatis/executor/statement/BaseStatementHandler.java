package org.apache.ibatis.executor.statement;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseStatementHandler implements StatementHandler {

  protected final Configuration configuration;
  protected final ObjectFactory objectFactory;
  protected final TypeHandlerRegistry typeHandlerRegistry;
  protected final ResultSetHandler resultSetHandler;
  protected final ParameterHandler parameterHandler;

  protected final Executor executor;
  protected final MappedStatement mappedStatement;
  protected final RowBounds rowBounds;

  protected final BoundSql boundSql;

  protected BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) {
    this.configuration = mappedStatement.getConfiguration();
    this.executor = executor;
    this.mappedStatement = mappedStatement;
    this.rowBounds = rowBounds;

    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    this.objectFactory = configuration.getObjectFactory();

    this.boundSql = mappedStatement.getBoundSql(parameterObject);

    this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject, boundSql);
    this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, rowBounds, parameterHandler, resultHandler, boundSql);
  }

  public BoundSql getBoundSql() {
    return boundSql;
  }

  public ParameterHandler getParameterHandler() {
    return parameterHandler;
  }

  public Statement prepare(Connection connection)
      throws SQLException {
    ErrorContext.instance().sql(boundSql.getSql());
    Statement statement = null;
    try {
      statement = instantiateStatement(connection);
      setStatementTimeout(statement);
      setFetchSize(statement);
      return statement;
    } catch (SQLException e) {
      closeStatement(statement);
      throw e;
    } catch (Exception e) {
      closeStatement(statement);
      throw new ExecutorException("Error preparing statement.  Cause: " + e, e);
    }
  }

  protected abstract Statement instantiateStatement(Connection connection)
      throws SQLException;

  protected void setStatementTimeout(Statement stmt)
      throws SQLException {
    Integer timeout = mappedStatement.getTimeout();
    Integer defaultTimeout = configuration.getDefaultStatementTimeout();
    if (timeout != null) {
      stmt.setQueryTimeout(timeout);
    } else if (defaultTimeout != null) {
      stmt.setQueryTimeout(defaultTimeout);
    }
  }

  protected void setFetchSize(Statement stmt)
      throws SQLException {
    Integer fetchSize = mappedStatement.getFetchSize();
    if (fetchSize != null) {
      stmt.setFetchSize(fetchSize);
    }
  }

  protected void closeStatement(Statement statement) {
    try {
      if (statement != null) {
        statement.close();
      }
    } catch (SQLException e) {
      //ignore
    }

  }

  protected void rebindGeneratedKey() {
    if (boundSql.getParameterObject() != null) {
      String keyStatementName = mappedStatement.getId() + SelectKeyGenerator.SELECT_KEY_SUFFIX;
      if (configuration.hasStatement(keyStatementName)) {
        MappedStatement keyStatement = configuration.getMappedStatement(keyStatementName);
        if (keyStatement != null) {
          String keyProperty = keyStatement.getKeyProperty();
          MetaObject metaParam = configuration.newMetaObject(boundSql.getParameterObject());
          if (keyProperty != null && metaParam.hasSetter(keyProperty) && metaParam.hasGetter(keyProperty)) {
            boundSql.setAdditionalParameter(keyProperty, metaParam.getValue(keyProperty));
          }
        }
      }
    }
  }

}
