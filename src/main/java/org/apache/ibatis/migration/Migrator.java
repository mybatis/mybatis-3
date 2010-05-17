package org.apache.ibatis.migration;

public class Migrator {

  public static void main(String[] args) throws Exception {
    try {
      new CommandLine(args).execute();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

}
