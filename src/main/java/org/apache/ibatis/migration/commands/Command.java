package org.apache.ibatis.migration.commands;

public interface Command {
  void execute(String... params);
}
