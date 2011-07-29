package org.apache.ibatis.migration.commands;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationReader;

import java.io.File;

public class BootstrapCommand extends BaseCommand {

  public BootstrapCommand(File repository, String environment, boolean force) {
    super(repository, environment, force);
  }

  public void execute(String... params) {
    try {
      if (changelogExists() && !force) {
        printStream.println("For your safety, the bootstrap SQL script will only run before migrations are applied (i.e. before the changelog exists).  If you're certain, you can run it using the --force option.");
      } else {
        File bootstrap = scriptFile("bootstrap.sql");
        if (bootstrap.exists()) {
          printStream.println(horizontalLine("Applying: bootstrap.sql", 80));
          ScriptRunner runner = getScriptRunner();
          try {
            runner.runScript(new MigrationReader(scriptFileReader(bootstrap), false, environmentProperties()));
          } finally {
            runner.closeConnection();
          }
          printStream.println();
        } else {
          printStream.println("Error, could not run bootstrap.sql.  The file does not exist.");
        }
      }
    } catch (Exception e) {
      throw new MigrationException("Error running bootstrapper.  Cause: " + e, e);
    }
  }

}
