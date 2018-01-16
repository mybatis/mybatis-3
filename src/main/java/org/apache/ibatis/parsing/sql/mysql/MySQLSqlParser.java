/**
 *    Copyright 2009-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.parsing.sql.mysql;

import org.apache.ibatis.jdbc.RuntimeSqlException;
import org.apache.ibatis.parsing.ParsingException;
import org.apache.ibatis.parsing.sql.Delimiter;
import org.apache.ibatis.parsing.sql.SqlParser;
import org.apache.ibatis.parsing.sql.SqlParserFactory;
import org.apache.ibatis.parsing.sql.SqlStatementBuilder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

public class MySQLSqlParser extends SqlParser {
  /**
   * The keyword that indicates a change in delimiter.
   */
  private static final String DELIMITER_KEYWORD = "DELIMITER";

  private Delimiter delimiter = Delimiter.SEMICOLON;

  @Override
  public Integer suitabilityFor(Connection c, Map<Object, Object> additionalProps) {
    String dbProduceName;
    try {
      DatabaseMetaData metaData = c.getMetaData();
      dbProduceName = metaData.getDatabaseProductName();
    } catch (SQLException e) {
      return null;
    }
    dbProduceName = dbProduceName.toUpperCase(Locale.ROOT);
    return dbProduceName.contains("MYSQL") ? 1 : null;
  }

  @Override
  public SqlParser withProperties(Map<Object, Object> additionalProps) {
    String delimFromProps = (String) additionalProps.get(SqlParserFactory.DELIMITER_PROP);
    if(delimFromProps != null){
      delimiter = delimiter.withDelimiter(delimFromProps);
    }
    Boolean fullLineFromProps = (Boolean) additionalProps.get(SqlParserFactory.FULLLINE_DELIMITER_PROP);
    if(fullLineFromProps != null){
      delimiter = delimiter.withAloneOnLine(fullLineFromProps);
    }
    return this;
  }

  protected String readNextStatement() {
    SqlStatementBuilder sqlStatementBuilder = new MySQLSqlStatementBuilder(delimiter);


    while (!sqlStatementBuilder.isTerminated()) {
      String thisLine;
      try {
        thisLine = lineReader.readLine();
      } catch (IOException e) {
        throw new RuntimeSqlException("Error reading SQL from source file", e);
      }
      if (thisLine == null) {
        break;
      }
      if (sqlStatementBuilder.isEmpty()) {
        if (isPreStatement(sqlStatementBuilder, thisLine)) {
          continue;
        }
      }

      try {
        sqlStatementBuilder.addLine(thisLine);
      } catch (Exception e) {
        throw new ParsingException("Parsing bug (" + e.getMessage() + ") at: " + thisLine, e);
      }

      if (sqlStatementBuilder.canDiscard()) {
        sqlStatementBuilder = new MySQLSqlStatementBuilder(delimiter);
      }
    }
    return sqlStatementBuilder.isEmpty() ? null : sqlStatementBuilder.getSqlStatement();
  }

  private boolean isPreStatement(SqlStatementBuilder sqlStatementBuilder, String thisLine) {
    if (thisLine.trim().isEmpty()) {
      return true;
    }

    Delimiter newDelimiter = extractNewDelimiterFromLine(thisLine);
    if (newDelimiter != null) {
      delimiter = newDelimiter;
      sqlStatementBuilder.setDelimiter(delimiter);
      // Skip this line as it was an explicit delimiter change directive outside of any statements.
      return true;
    }
    return false;
  }

  private Delimiter extractNewDelimiterFromLine(String line) {
    if (line != null && line.toUpperCase().startsWith(DELIMITER_KEYWORD)) {
      return delimiter.withDelimiter(line.substring(DELIMITER_KEYWORD.length()).trim());
    }

    return null;
  }

}