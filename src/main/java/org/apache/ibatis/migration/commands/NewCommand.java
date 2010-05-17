package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.MigrationException;

import java.io.File;
import java.util.Properties;

public class NewCommand extends BaseCommand {

  public NewCommand(File repository, String environment, boolean force) {
    super(repository, environment, force);
  }

  public void execute(String... params) {
    if (paramsEmpty(params)) {
      throw new MigrationException("No description specified for new migration.");
    }
    String description = params[0];
    Properties variables = new Properties();
    variables.setProperty("description", description);
    existingEnvironmentFile();
    String filename = getNextIDAsString() + "_" + description.replace(' ', '_') + ".sql";
    copyResourceTo("org/apache/ibatis/migration/template_migration.sql", scriptFile(filename), variables);
    printStream.println("Done!");
    printStream.println();
  }

}
