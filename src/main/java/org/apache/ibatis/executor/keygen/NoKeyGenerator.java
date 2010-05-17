package org.apache.ibatis.executor.keygen;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;

import java.sql.Statement;

public class NoKeyGenerator implements KeyGenerator {

  public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
  }

  public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
  }

}
