package org.apache.ibatis.migration.commands;

import org.apache.ibatis.io.ExternalResources;
import org.apache.ibatis.migration.MigrationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

public class NewCommand extends BaseCommand {

  private static final String MIGRATIONS_HOME = "MIGRATIONS_HOME";
  private static final String MIGRATIONS_HOME_PROPERTY = "migrationHome";
  private static final String CUSTOM_NEW_COMMAND_TEMPATE_PROPERTY = "new_command.template";
  private static final String MIGRATIONS_PROPERTIES = "migration.properties";

  public NewCommand(File repository, String environment, String template, boolean force) {
    super(repository, environment, template, force);
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
    String migrationsHome = "";
    migrationsHome = System.getenv(MIGRATIONS_HOME);

    // Check if there is a system property
    if (migrationsHome == null) {
      migrationsHome = System.getProperty(MIGRATIONS_HOME_PROPERTY);
    }

    if (this.template != null) {
      copyExternalResourceTo(template, scriptFile(filename), variables);
    } else if ((migrationsHome != null) && (!migrationsHome.equals(""))) {
      try {
        //get template name from properties file
        final String customConfiguredTemplate = ExternalResources.getConfiguredTemplate(migrationsHome + "/" + MIGRATIONS_PROPERTIES, CUSTOM_NEW_COMMAND_TEMPATE_PROPERTY);
        copyExternalResourceTo(migrationsHome + "/" + customConfiguredTemplate, scriptFile(filename), variables);
      } catch (FileNotFoundException e) {
        printStream.append("Your migrations configuration did not find your custom template.  Using the default template.");
        copyDefaultTemplate(variables, filename);
      }
    } else {
      copyDefaultTemplate(variables, filename);
    }

    printStream.println("Done!");
    printStream.println();
  }

  private void copyDefaultTemplate(Properties variables, String filename) {
    copyResourceTo("org/apache/ibatis/migration/template_migration.sql", scriptFile(filename), variables);
  }
}
