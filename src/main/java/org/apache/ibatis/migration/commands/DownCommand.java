package org.apache.ibatis.migration.commands;

import java.io.File;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.jdbc.SqlRunner;
import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationReader;

public class DownCommand extends BaseCommand {

  public DownCommand(File repository, String environment, boolean force) {
    super(repository, environment, force);
  }

  public void execute(String... params) {
    try {
      Change lastChange = getLastAppliedChange();
      List<Change> migrations = getMigrations();
      Collections.reverse(migrations);
      int steps = 0;
      for (Change change : migrations) {
        if (change.getId().equals(lastChange.getId())) {
          printStream.println(horizontalLine("Undoing: " + change.getFilename(), 80));
          ScriptRunner runner = getScriptRunner();
          try {
            runner.runScript(new MigrationReader(scriptFileReader(scriptFile(change.getFilename())), true, environmentProperties()));
          } finally {
            runner.closeConnection();
          }
          if (changelogExists()) {
            deleteChange(change);
          } else {
            printStream.println("Changelog doesn't exist. No further migrations will be undone (normal for the last migration).");
          }
          printStream.println();
          steps++;
          final int limit = getStepCountParameter(1, params);
          if (steps == limit) {
            break;
          }
          lastChange = getLastAppliedChange();
        }
      }
    } catch (Exception e) {
      throw new MigrationException("Error undoing last migration.  Cause: " + e, e);
    }
  }

  protected void deleteChange(Change change) {
    SqlRunner runner = getSqlRunner();
    try {
      runner.delete("delete from " + changelogTable() + " where id = ?", change.getId());
    } catch (SQLException e) {
      throw new MigrationException("Error querying last applied migration.  Cause: " + e, e);
    } finally {
      runner.closeConnection();
    }
  }

}
