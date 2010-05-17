package org.apache.ibatis.executor.statement;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class SimpleStatementHandler extends BaseStatementHandler {

  public SimpleStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) {
    super(executor, mappedStatement, parameter, rowBounds, resultHandler);
  }

  public int update(Statement statement)
      throws SQLException {
    String sql = boundSql.getSql();
    Object parameterObject = boundSql.getParameterObject();
    KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
    int rows;
    if (keyGenerator instanceof Jdbc3KeyGenerator) {
      statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
      rows = statement.getUpdateCount();
      keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
    } else if (keyGenerator instanceof SelectKeyGenerator) {
      statement.execute(sql);
      rows = statement.getUpdateCount();
      keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
    } else {
      statement.execute(sql);
      rows = statement.getUpdateCount();
    }
    return rows;
  }

  public void batch(Statement statement)
      throws SQLException {
    String sql = boundSql.getSql();
    statement.addBatch(sql);
  }

  public List query(Statement statement, ResultHandler resultHandler)
      throws SQLException {
    String sql = boundSql.getSql();
    statement.execute(sql);
    return resultSetHandler.handleResultSets(statement);
  }

  protected Statement instantiateStatement(Connection connection) throws SQLException {
    if (mappedStatement.getResultSetType() != null) {
      return connection.createStatement(mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
    } else {
      return connection.createStatement();
    }
  }

  public void parameterize(Statement statement) throws SQLException {
    // N/A
  }

}
