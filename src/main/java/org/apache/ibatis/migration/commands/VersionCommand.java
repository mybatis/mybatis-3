package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.Change;
import org.apache.ibatis.migration.MigrationException;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public class VersionCommand extends BaseCommand {

  public VersionCommand(File repository, String environment, boolean force) {
    super(repository, environment, force);
  }

  public void execute(String... params) {
    ensureParamsPassed(params);
    ensureNumericParam(params);
    ensureVersionExists(params);

    BigDecimal version = new BigDecimal(params[0]);

    Change change = getLastAppliedChange();
    if (version.compareTo(change.getId()) > 0) {
      printStream.println("Upgrading to: " + version);
      Command up = new UpCommand(basePath, environment, force, true);
      while (!version.equals(change.getId())) {
        up.execute();
        change = getLastAppliedChange();
      }
    } else if (version.compareTo(change.getId()) < 0) {
      printStream.println("Downgrading to: " + version);
      Command down = new DownCommand(basePath, environment, force);
      while (!version.equals(change.getId())) {
        down.execute();
        change = getLastAppliedChange();
      }
    } else {
      printStream.println("Already at version: " + version);
    }
    printStream.println();
  }

  private void ensureParamsPassed(String... params) {
    if (paramsEmpty(params)) {
      throw new MigrationException("No target version specified for migration.");
    }
  }

  private void ensureNumericParam(String... params) {
    try {
      new BigDecimal(params[0]);
    } catch (Exception e) {
      throw new MigrationException("The version number must be a numeric integer.  " + e, e);
    }
  }

  private void ensureVersionExists(String... params) {
    List<Change> migrations = getMigrations();
    if (!migrations.contains(new Change(new BigDecimal(params[0])))) {
      throw new MigrationException("A migration for the specified version number does not exist.");
    }
  }

}
