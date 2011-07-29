package org.apache.ibatis.migration.commands;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationReader;

import java.io.File;
import java.util.List;

public class UpCommand extends BaseCommand {

  private boolean runOneStepOnly = false;

  public UpCommand(File repository, String environment, boolean force) {
    super(repository, environment, force);
  }

  public UpCommand(File repository, String environment, boolean force, boolean runOneStepOnly) {
    super(repository, environment, force);
    this.runOneStepOnly = runOneStepOnly;
  }

  public void execute(String... params) {
    try {
      Change lastChange = null;
      if (changelogExists()) {
        lastChange = getLastAppliedChange();
      }
      List<Change> migrations = getMigrations();
      int steps = 0;
      for (Change change : migrations) {
        if (lastChange == null || change.getId().compareTo(lastChange.getId()) > 0) {
          printStream.println(horizontalLine("Applying: " + change.getFilename(), 80));
          ScriptRunner runner = getScriptRunner();
          try {
            runner.runScript(new MigrationReader(scriptFileReader(scriptFile(change.getFilename())), false, environmentProperties()));
          } finally {
            runner.closeConnection();
          }
          insertChangelog(change);
          printStream.println();
          steps++;
          final int limit = getStepCountParameter(Integer.MAX_VALUE, params);
          if (steps == limit || runOneStepOnly) {
            break;
          }
        }
      }
    } catch (Exception e) {
      throw new MigrationException("Error executing command.  Cause: " + e, e);
    }
  }

}
