package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.MigrationException;

import java.io.File;
import java.util.Properties;

public class InitializeCommand extends BaseCommand {

  public InitializeCommand(File repository, String environment, boolean force) {
    super(repository, environment, force);
  }

  public void execute(String... args) {
    printStream.println("Initializing: " + basePath);

    createDirectoryIfNecessary(basePath);
    ensureDirectoryIsEmpty(basePath);

    createDirectoryIfNecessary(envPath);
    createDirectoryIfNecessary(scriptPath);
    createDirectoryIfNecessary(driverPath);

    copyResourceTo("org/apache/ibatis/migration/template_README", baseFile("README"));
    copyResourceTo("org/apache/ibatis/migration/template_environment.properties", environmentFile());
    copyResourceTo("org/apache/ibatis/migration/template_bootstrap.sql", scriptFile("bootstrap.sql"));
    copyResourceTo("org/apache/ibatis/migration/template_changelog.sql", scriptFile(getNextIDAsString() + "_create_changelog.sql"));
    copyResourceTo("org/apache/ibatis/migration/template_migration.sql", scriptFile(getNextIDAsString() + "_first_migration.sql"),
        new Properties() {
          {
            setProperty("description", "First migration.");
          }
        });
    printStream.println("Done!");
    printStream.println();
  }

  protected void ensureDirectoryIsEmpty(File path) {
    String[] list = path.list();
    if (list.length != 0) {
      for (String entry : list) {
        if (!entry.startsWith(".")) {
          throw new MigrationException("Directory must be empty (.svn etc allowed): " + path.getAbsolutePath());
        }
      }
    }
  }

  protected void createDirectoryIfNecessary(File path) {
    if (!path.exists()) {
      File parent = new File(path.getParent());
      createDirectoryIfNecessary(parent);
      printStream.println("Creating: " + path.getName());
      if (!path.mkdir()) {
        throw new MigrationException("Could not create directory path for an unknown reason. Make sure you have access to the directory.");
      }
    }
  }


}
