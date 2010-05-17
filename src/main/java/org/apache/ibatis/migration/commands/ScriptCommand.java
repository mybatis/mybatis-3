package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.MigrationException;
import org.apache.ibatis.migration.MigrationReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class ScriptCommand extends BaseCommand {

  public ScriptCommand(File repository, String environment, boolean force) {
    super(repository, environment, force);
  }

  public void execute(String... sparams) {
    try {
      if (sparams == null || sparams.length < 1 || sparams[0] == null) {
        throw new MigrationException("The script command requires a range of versions from v1 - v2.");
      }
      StringTokenizer parser = new StringTokenizer(sparams[0]);
      if (parser.countTokens() != 2) {
        throw new MigrationException("The script command requires a range of versions from v1 - v2.");
      }
      BigDecimal v1 = new BigDecimal(parser.nextToken());
      BigDecimal v2 = new BigDecimal(parser.nextToken());
      boolean undo = v1.compareTo(v2) > 0;
      Properties variables = environmentProperties();
      List<Change> migrations = getMigrations();
      Collections.sort(migrations);
      if (undo) {
        Collections.reverse(migrations);
      }
      for (Change change : migrations) {
        if (shouldRun(change, v1, v2)) {
          printStream.println("-- " + change.getFilename());
          File file = scriptFile(change.getFilename());
          FileReader fileReader = new FileReader(file);
          MigrationReader migrationReader = new MigrationReader(fileReader, undo, variables);
          char[] cbuf = new char[1024];
          int l;
          while ((l = migrationReader.read(cbuf)) == cbuf.length) {
            printStream.print(new String(cbuf, 0, l));
          }
          printStream.print(new String(cbuf, 0, l - 1));
          printStream.println();
          printStream.println();
          printStream.println(undo ? generateVersionDelete(change) : generateVersionInsert(change));
          printStream.println();
        }
      }
    } catch (IOException e) {
      throw new MigrationException("Error generating script. Cause: " + e, e);
    }

  }

  private String generateVersionInsert(Change change) {
    return "INSERT INTO " + changelogTable() + " (ID, APPLIED_AT, DESCRIPTION) " +
        "VALUES (" + change.getId() + ", '" + generateAppliedTimeStampAsString() + "', '" + change.getDescription().replace('\'', ' ') + "');";
  }

  private String generateVersionDelete(Change change) {
    return "DELETE FROM " + changelogTable() + " WHERE ID = " + change.getId() + ";";
  }

  private boolean shouldRun(Change change, BigDecimal v1, BigDecimal v2) {
    BigDecimal id = change.getId();
    if (v1.compareTo(v2) > 0) {
      return (id.compareTo(v2) >= 0 && id.compareTo(v1) <= 0);
    } else {
      return (id.compareTo(v1) >= 0 && id.compareTo(v2) <= 0);
    }
  }

}
