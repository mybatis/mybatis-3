package org.apache.ibatis.migration.commands;

import org.apache.ibatis.migration.Change;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatusCommand extends BaseCommand {

  public StatusCommand(File repository, String environment, boolean force) {
    super(repository, environment, force);
  }

  public void execute(String... params) {
    printStream.println("ID             Applied At          Description");
    printStream.println(horizontalLine("", 80));
    List<Change> merged = new ArrayList<Change>();
    List<Change> migrations = getMigrations();
    if (changelogExists()) {
      List<Change> changelog = getChangelog();
      for (Change change : migrations) {
        int index = changelog.indexOf(change);
        if (index > -1) {
          merged.add(changelog.get(index));
        } else {
          merged.add(change);
        }
      }
      Collections.sort(merged);
    } else {
      merged.addAll(migrations);
    }
    for (Change change : merged) {
      printStream.println(change);
    }
    printStream.println();
  }


}
