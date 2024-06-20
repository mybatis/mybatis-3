package org.apache.ibatis.submitted.select_with_like;


import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;

public class SelectWithLikeTest {


  private static Configuration configuration;

  @BeforeAll
  static void setUp() {
    configuration = new Configuration();
    configuration.addMapper(Mapper.class);
  }


  @Test
  void testSelectWithLikeLeft() {
    Object parameterObject = "jack";
    MappedStatement mappedStatement = configuration.getMappedStatement("selectWithLikeRight");
    PreparedStatement preparedStatement = (PreparedStatement) Proxy.newProxyInstance(
      SelectWithLikeTest.class.getClassLoader(), new Class[] { PreparedStatement.class }, (proxy, method, args) -> {
        if (method.getName().equals("setString")) {
          Assertions.assertEquals(args[1], parameterObject + "%");
        }
        return null;
      });

    BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
    DefaultParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    parameterHandler.setParameters(preparedStatement);
  }

  @Test
  void testSelectWithNoLike() {
    Object parameterObject = "jack";
    MappedStatement mappedStatement = configuration.getMappedStatement("selectWithNoLike");
    PreparedStatement preparedStatement = (PreparedStatement) Proxy.newProxyInstance(
      SelectWithLikeTest.class.getClassLoader(), new Class[] { PreparedStatement.class }, (proxy, method, args) -> {
        if (method.getName().equals("setString")) {
          Assertions.assertEquals(args[1], parameterObject);
        }
        return null;
      });

    BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
    DefaultParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    parameterHandler.setParameters(preparedStatement);
  }

  @Test
  void testSelectWithOodLike() {
    Object parameterObject = "jack";
    MappedStatement mappedStatement = configuration.getMappedStatement("selectWithOodLike");
    PreparedStatement preparedStatement = (PreparedStatement) Proxy.newProxyInstance(
      SelectWithLikeTest.class.getClassLoader(), new Class[] { PreparedStatement.class }, (proxy, method, args) -> {
        if (method.getName().equals("setString")) {
          Assertions.assertEquals(args[1], parameterObject);
        }
        return null;
      });

    BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
    DefaultParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    parameterHandler.setParameters(preparedStatement);
  }
}
