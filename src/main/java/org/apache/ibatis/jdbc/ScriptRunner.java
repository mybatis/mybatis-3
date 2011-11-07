package org.apache.ibatis.jdbc;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.regex.Pattern;

public class ScriptRunner {

  private static final String LINE_SEPARATOR = System.getProperty("line.separator","\n");

  private static final String DEFAULT_DELIMITER = ";";

  private static final String S_N = "(\\s|\\n)+";
  private static final String IDENTIFIER = "(\\S+|\"[^\"]+\")";
  private static final String BLOCK_START = "(^|" + S_N + ")" +
            "create" + S_N +
            "(or" + S_N + "replace" + S_N + ")?" +
            "(function|library|package(" + S_N + "body)?|procedure|trigger|type)" + S_N +
            IDENTIFIER + S_N +
            ".*";
  
  private final Pattern blockStart = Pattern.compile(BLOCK_START, Pattern.CASE_INSENSITIVE);

  private Connection connection;

  private boolean stopOnError;
  private boolean autoCommit;
  private boolean sendFullScript;

  private PrintWriter logWriter = new PrintWriter(System.out);
  private PrintWriter errorLogWriter = new PrintWriter(System.err);

  private String DatabaseProductName;
  private String delimiter = DEFAULT_DELIMITER;
  private boolean fullLineDelimiter = false;

  public ScriptRunner(Connection connection) {
    this.connection = connection;
  }

  public void setStopOnError(boolean stopOnError) {
    this.stopOnError = stopOnError;
  }

  public void setAutoCommit(boolean autoCommit) {
    this.autoCommit = autoCommit;
  }

  public void setSendFullScript(boolean sendFullScript) {
    this.sendFullScript = sendFullScript;
  }

  public void setLogWriter(PrintWriter logWriter) {
    this.logWriter = logWriter;
  }

  public void setErrorLogWriter(PrintWriter errorLogWriter) {
    this.errorLogWriter = errorLogWriter;
  }

  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  public void setFullLineDelimiter(boolean fullLineDelimiter) {
    this.fullLineDelimiter = fullLineDelimiter;
  }

  public void runScript(Reader reader) {
    try {
      setAutoCommit();
      DatabaseMetaData md = connection.getMetaData();
        DatabaseProductName = md.getDatabaseProductName().toUpperCase().trim();
      try {
        if ("ORACLE".equals(DatabaseProductName)) {
          executeOracleScript(reader);    
        } else {
          if (sendFullScript) {
            executeFullScript(reader);
          } else {
            executeLineByLine(reader);
          }
         }
      } finally {
        rollbackConnection();
      }
    } catch (SQLException e) {
      String message = "\nError retrieving database metadata\nCause: " + e;
      throw new RuntimeSqlException(message, e);
    }
  }

  private void executeFullScript(Reader reader) {
    StringBuilder script = new StringBuilder();
    try {
      BufferedReader lineReader = new BufferedReader(reader);
      String line;
      while ((line = lineReader.readLine()) != null) {
        script.append(line);
        script.append(LINE_SEPARATOR);
      }
      executeStatement(script.toString());
      commitConnection();
    } catch (Exception e) {
      String message = "\nError executing: \n" + script + "Cause: " + e;
      throw new RuntimeSqlException(message, e);
    }
  }

  private void executeLineByLine(Reader reader) {
    StringBuilder command = new StringBuilder();
    try {
      BufferedReader lineReader = new BufferedReader(reader);
      String line;
      while ((line = lineReader.readLine()) != null) {
        command = handleLine(command, line);
      }
      commitConnection();
      checkForMissingLineTerminator(command);
    } catch (Exception e) {
      String message = "\nError executing: \n" + command + "Cause: " + e;
      throw new RuntimeSqlException(message, e);
    }
  }

  private void executeOracleScript(Reader reader) {
    StringBuilder command = new StringBuilder();
    try {
      boolean plsqlMode = false;
      BufferedReader lineReader = new BufferedReader(reader);
      String line;
      while ((line = lineReader.readLine()) != null) {
        String trimmedLine = line.trim();
        if (trimmedLine.length() == 0) {
          continue;
        }
        if (trimmedLine.matches("[/.]")) {
          /*
            Terminate PL/SQL subprograms by entering a period (.) by itself on
            a new line. You can also terminate and execute a PL/SQL subprogram
            by entering a slash (/) by itself on a new line.
          */
          println(command);
          executeStatement(command.toString().trim());
          plsqlMode = false;
          command.setLength(0);
        } else if (!plsqlMode &&
                    (blockStart.matcher(command).find() ||
                     "begin".equalsIgnoreCase(line) ||
                     "declare".equalsIgnoreCase(line)
                    )
                  ) {
          plsqlMode = true;
          command.append(line);
          command.append(LINE_SEPARATOR);
        } else if (!plsqlMode &&
                    ( ("exit" + delimiter).equalsIgnoreCase(line) ||
                       "exit".equalsIgnoreCase(line)
                    )
                  ) {
          return;
        } else if (!plsqlMode && line.endsWith(delimiter)) {
          command.append(line.substring(0, line.lastIndexOf(delimiter)));
          println(command);
          executeStatement(command.toString().trim());
          command.setLength(0);
        } else {
          command.append(line);
          command.append(LINE_SEPARATOR);
        }
      }
      // Check to see if we have an unexecuted statement in command.
      if (command.length() > 0) {
        println(command);
        executeStatement(command.toString().trim());
      }
      commitConnection();
      checkForMissingLineTerminator(command);
    } catch (Exception e) {
      String message = "\nError executing: \n" + command + "Cause: " + e;
      throw new RuntimeSqlException(message, e);
    }
  }

  public void run(String sql) throws SQLException {
    Statement stmt = connection.createStatement();
    try {
      stmt.execute(sql);
    } finally {
      try {
        stmt.close();
      } catch (SQLException e) {
        //ignore
      }
    }
  }

  public void closeConnection() {
    try {
      connection.close();
    } catch (Exception e) {
      // ignore
    }
  }

  private void setAutoCommit() {
    try {
      if (autoCommit != connection.getAutoCommit()) {
        connection.setAutoCommit(autoCommit);
      }
    } catch (Throwable t) {
      throw new RuntimeSqlException("Could not set AutoCommit to " + autoCommit + ". Cause: " + t, t);
    }
  }

  private void commitConnection() {
    try {
      if (!connection.getAutoCommit()) {
        connection.commit();
      }
    } catch (Throwable t) {
      throw new RuntimeSqlException("Could not commit transaction. Cause: " + t, t);
    }
  }

  private void rollbackConnection() {
    try {
      if (!connection.getAutoCommit()) {
        connection.rollback();
      }
    } catch (Throwable t) {
      // ignore
    }
  }

  private void checkForMissingLineTerminator(StringBuilder command) {
    if (command != null && command.toString().trim().length() > 0) {
      throw new RuntimeSqlException("Line missing end-of-line terminator (" + delimiter + ") => " + command);
    }
  }

  private StringBuilder handleLine(StringBuilder command, String line) throws SQLException, UnsupportedEncodingException {
    String trimmedLine = line.trim();
    if (commandReadyToExecute(trimmedLine)) {
      command.append(line.substring(0, line.lastIndexOf(delimiter)));
      command.append(LINE_SEPARATOR);
      println(command);
      executeStatement(command.toString());
      command.setLength(0);
    } else if (trimmedLine.length() > 0) {
      command.append(line);
      command.append(LINE_SEPARATOR);
    }
    return command;
  }

  private boolean commandReadyToExecute(String trimmedLine) {
    return !fullLineDelimiter && trimmedLine.endsWith(delimiter)
        || fullLineDelimiter && trimmedLine.equals(delimiter);
  }

  private void executeStatement(String command) throws SQLException, UnsupportedEncodingException {
    boolean hasResults = false;
    Statement statement = connection.createStatement();
    if (stopOnError) {
      hasResults = statement.execute(command);
    } else {
      try {
        hasResults = statement.execute(command);
      } catch (SQLException e) {
      }
    }
    printResults(statement, hasResults);
    try {
      statement.close();
    } catch (Exception e) {
      // Ignore to workaround a bug in some connection pools
    }
  }

  private void printResults(Statement statement, boolean hasResults) {
    try {
      if (hasResults) {
        ResultSet rs = statement.getResultSet();
        if (rs != null) {
          ResultSetMetaData md = rs.getMetaData();
          int cols = md.getColumnCount();
          for (int i = 0; i < cols; i++) {
            String name = md.getColumnLabel(i + 1);
            print(name + "\t");
          }
          println("");
          while (rs.next()) {
            for (int i = 0; i < cols; i++) {
              String value = rs.getString(i + 1);
              print(value + "\t");
            }
            println("");
          }
        }
      }
    } catch (SQLException e) {
      printlnError("Error printing results: " + e.getMessage());
    }
  }

  private void print(Object o) {
    if (logWriter != null) {
      logWriter.print(o);
      logWriter.flush();
    }
  }

  private void println(Object o) {
    if (logWriter != null) {
      logWriter.println(o);
      logWriter.flush();
    }
  }

  private void printlnError(Object o) {
    if (errorLogWriter != null) {
      errorLogWriter.println(o);
      errorLogWriter.flush();
    }
  }
}
