package org.apache.ibatis.migration;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.ibatis.migration.commands.BootstrapCommand;
import org.apache.ibatis.migration.commands.DownCommand;
import org.apache.ibatis.migration.commands.InitializeCommand;
import org.apache.ibatis.migration.commands.NewCommand;
import org.apache.ibatis.migration.commands.PendingCommand;
import org.apache.ibatis.migration.commands.ScriptCommand;
import org.apache.ibatis.migration.commands.StatusCommand;
import org.apache.ibatis.migration.commands.UpCommand;
import org.apache.ibatis.migration.commands.VersionCommand;

public class CommandLine {

  private static final String PATH_PREFIX = "--path=";
  private static final String ENV_PREFIX = "--env=";
  private static final String FORCE = "--force";
  private static final String TRACE = "--trace";
  private static final String HELP = "--help";
  private static final String TEMPLATE_PREFIX = "--template=";
  private static final String INIT = "init";
  private static final String BOOTSTRAP = "bootstrap";
  private static final String NEW = "new";
  private static final String UP = "up";
  private static final String DOWN = "down";
  private static final String PENDING = "pending";
  private static final String SCRIPT = "script";
  private static final String VERSION = "version";
  private static final String STATUS = "status";
  private static final Set<String> KNOWN_COMMANDS = Collections.unmodifiableSet(
          new HashSet<String>(Arrays.asList(INIT, NEW, UP, VERSION, DOWN, PENDING, STATUS, BOOTSTRAP, SCRIPT)));
  private PrintStream printStream;
  private File repository;
  private String environment;
  private String template;
  private boolean force;
  private boolean trace;
  private String command;
  private String params;
  private String parseError;
  private boolean help;

  public CommandLine(String[] args) {
    this.printStream = System.out;
    parse(args);
    validate();
  }

  public void setPrintStream(PrintStream out) {
    this.printStream = out;
  }

  public PrintStream getPrintStream() {
    return this.printStream;
  }

  public void execute() {
    boolean error = false;
    try {
      if (help) {
        printUsage();
      } else if (parseError != null) {
        error = true;
        printError();
        printUsage();
      } else {
        try {
          runCommand();
        } catch (Exception e) {
          error = true;
          printStream.println("\nERROR: " + e.getMessage());
          if (trace) {
            e.printStackTrace();
          }
        }
      }
    } finally {
      printStream.flush();
      if (error) {
        System.exit(1);
      }
    }
  }

  private void runCommand() {
    printStream.println("------------------------------------------------------------------------");
    printStream.printf("MyBatis Migrations - %s%n", command);
    printStream.println("------------------------------------------------------------------------");

    long start = System.currentTimeMillis();
    int exit = 0;

    try {
      if (INIT.equals(command)) {
        new InitializeCommand(repository, environment, force).execute(params);
      } else if (BOOTSTRAP.equals(command)) {
        new BootstrapCommand(repository, environment, force).execute(params);
      } else if (NEW.equals(command)) {
        new NewCommand(repository, environment, template, force).execute(params);
      } else if (STATUS.equals(command)) {
        new StatusCommand(repository, environment, force).execute(params);
      } else if (UP.equals(command)) {
        new UpCommand(repository, environment, force).execute(params);
      } else if (VERSION.equals(command)) {
        new VersionCommand(repository, environment, force).execute(params);
      } else if (PENDING.equals(command)) {
        new PendingCommand(repository, environment, force).execute(params);
      } else if (DOWN.equals(command)) {
        new DownCommand(repository, environment, force).execute(params);
      } else if (SCRIPT.equals(command)) {
        new ScriptCommand(repository, environment, force).execute(params);
      } else {
        String match = null;
        for (String knownCommand : KNOWN_COMMANDS) {
          if (knownCommand.startsWith(command)) {
            if (match != null) {
              throw new MigrationException("Ambiguous command shortcut: " + command);
            }
            match = knownCommand;
          }
        }
        if (match != null) {
          command = match;
          runCommand();
        } else {
          throw new MigrationException("Attempt to execute unknown command: " + command);
        }
      }
    } catch (Throwable t) {
      exit = -1;
      t.printStackTrace(printStream);
    } finally {
      printStream.println("------------------------------------------------------------------------");
      printStream.printf("MyBatis Migrations %s%n", (exit < 0) ? "FAILURE" : "SUCCESS");
      printStream.printf("Total time: %ss%n", ((System.currentTimeMillis() - start) / 1000));
      printStream.printf("Finished at: %s%n", new Date());

      final Runtime runtime = Runtime.getRuntime();
      final int megaUnit = 1024 * 1024;
      printStream.printf("Final Memory: %sM/%sM%n",
                (runtime.totalMemory() - runtime.freeMemory()) / megaUnit,
                runtime.totalMemory() / megaUnit);

      printStream.println("------------------------------------------------------------------------");

      System.exit(exit);
    }
  }

  private void parse(String[] args) {
    for (String arg : args) {
      if (arg.startsWith(PATH_PREFIX) && arg.length() > PATH_PREFIX.length()) {
        repository = new File(arg.split("=")[1]);
      } else if (arg.startsWith(ENV_PREFIX) && arg.length() > ENV_PREFIX.length()) {
        environment = arg.split("=")[1];
      } else if (arg.startsWith(TEMPLATE_PREFIX) && arg.length() > TEMPLATE_PREFIX.length()) {
        template = arg.split("=")[1];
      } else if (arg.startsWith(TRACE)) {
        trace = true;
      } else if (arg.startsWith(FORCE)) {
        force = true;
      } else if (arg.startsWith(HELP)) {
        help = true;
      } else if (command == null) {
        command = arg;
      } else if (params == null) {
        params = arg;
      } else {
        params += " ";
        params += arg;
      }
    }
  }

  private void validate() {
    if (repository == null) {
      repository = new File("./");
    }
    if (environment == null) {
      environment = "development";
    }
    if (repository.exists() && !repository.isDirectory()) {
      parseError = ("Migrations path must be a directory: " + repository.getAbsolutePath());
    } else {
      repository = new File(repository.getAbsolutePath());
      if (command == null) {
        parseError = "No command specified.";
      }
    }
  }

  private void printError() {
    printStream.println(parseError);
    printStream.flush();
  }

  private void printUsage() {
    printStream.println();
    printStream.println("Usage: migrate command [parameter] [--path=<directory>] [--env=<environment>] [--template=<path to custom template>]");
    printStream.println();
    printStream.println("--path=<directory>   Path to repository.  Default current working directory.");
    printStream.println("--env=<environment>  Environment to configure. Default environment is 'development'.");
    printStream.println("--template=<template>  Path to custom template for creating new sql scripts.");
    printStream.println("--force              Forces script to continue even if SQL errors are encountered.");
    printStream.println("--help               Displays this usage message.");
    printStream.println("--trace              Shows additional error details (if any).");
    printStream.println();
    printStream.println("Commands:");
    printStream.println("  init               Creates (if necessary) and initializes a migration path.");
    printStream.println("  bootstrap          Runs the bootstrap SQL script (see scripts/bootstrap.sql for more).");
    printStream.println("  new <description>  Creates a new migration with the provided description.");
    printStream.println("  up [n]             Run unapplied migrations, ALL by default, or 'n' specified.");
    printStream.println("  down [n]           Undoes migrations applied to the database. ONE by default or 'n' specified.");
    printStream.println("  version <version>  Migrates the database up or down to the specified version.");
    printStream.println("  pending            Force executes pending migrations out of order (not recommended).");
    printStream.println("  status             Prints the changelog from the database if the changelog table exists.");
    printStream.println("  script <v1> <v2>   Generates a delta migration script from version v1 to v2 (undo if v1 > v2).");
    printStream.println("");
    printStream.println("  * Shortcuts are accepted by using the first few (unambiguous) letters of each command..");
    printStream.println();
    printStream.flush();
  }
}
