package org.apache.ibatis.migration;

import org.apache.ibatis.parsing.PropertyParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

public class MigrationReader extends Reader {

  private static final String LINE_SEPARATOR = System.getProperty("line.separator","\n");

  private Reader target;

  public MigrationReader(Reader source, boolean undo, Properties variables) throws IOException {
    try {
      BufferedReader reader = new BufferedReader(source);
      StringBuilder doBuilder = new StringBuilder();
      StringBuilder undoBuilder = new StringBuilder();
      StringBuilder currentBuilder = doBuilder;
      String line;
      while ((line = reader.readLine()) != null) {
        if (line != null) {
          if (line.trim().startsWith("--//")) {
            if (line.contains("@UNDO")) {
              currentBuilder = undoBuilder;
            }
            line = line.replace("--//", "-- ");
          }
          currentBuilder.append(line);
          currentBuilder.append(LINE_SEPARATOR);
        }
      }
      if (undo) {
        target = new StringReader(PropertyParser.parse(undoBuilder.toString(), variables));
      } else {
        target = new StringReader(PropertyParser.parse(doBuilder.toString(), variables));
      }
    } finally {
      source.close();
    }
  }

  public int read(char[] cbuf, int off, int len) throws IOException {
    return target.read(cbuf, off, len);
  }

  public void close() throws IOException {
    target.close();
  }

}
