package org.apache.ibatis.binding;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

public class MapperMethod {

  private SqlSession sqlSession;
  private Configuration config;

  private SqlCommandType type;
  private String commandName;

  private Class<?> declaringInterface;
  private Method method;

  private boolean returnsList;

  private Integer rowBoundsIndex;
  private List<String> paramNames;
  private List<Integer> paramPositions;

  private boolean hasNamedParameters;

  public MapperMethod(Class<?> declaringInterface, Method method, SqlSession sqlSession) {
    paramNames = new ArrayList<String>();
    paramPositions = new ArrayList<Integer>();
    this.sqlSession = sqlSession;
    this.method = method;
    this.config = sqlSession.getConfiguration();
    this.hasNamedParameters = false;
    this.declaringInterface = declaringInterface;
    setupFields();
    setupMethodSignature();
    setupCommandType();
    validateStatement();
  }

  public Object execute(Object[] args) throws SQLException {
    Object result;
    if (SqlCommandType.INSERT == type) {
      Object param = getParam(args);
      result = sqlSession.insert(commandName, param);
    } else if (SqlCommandType.UPDATE == type) {
      Object param = getParam(args);
      result = sqlSession.update(commandName, param);
    } else if (SqlCommandType.DELETE == type) {
      Object param = getParam(args);
      result = sqlSession.delete(commandName, param);
    } else if (SqlCommandType.SELECT == type) {
      if (returnsList) {
        result = executeForList(args);
      } else {
        Object param = getParam(args);
        result = sqlSession.selectOne(commandName, param);
      }
    } else {
      throw new BindingException("Unkown execution method for: " + commandName);
    }
    return result;
  }

  private Object executeForList(Object[] args) throws SQLException {
    Object result;
    if (rowBoundsIndex != null) {
      Object param = getParam(args);
      RowBounds rowBounds = (RowBounds) args[rowBoundsIndex];
      result = sqlSession.selectList(commandName, param, rowBounds);
    } else {
      Object param = getParam(args);
      result = sqlSession.selectList(commandName, param);
    }
    return result;
  }

  private Object getParam(Object[] args) {
    final int paramCount = paramPositions.size();
    if (args == null || paramCount == 0) {
      return null;
    } else if (!hasNamedParameters && paramCount == 1) {
      return args[paramPositions.get(0)];
    } else {
      Map<String, Object> param = new HashMap<String, Object>();
      for (int i = 0; i < paramCount; i++) {
        param.put(paramNames.get(i), args[paramPositions.get(i)]);
      }
      return param;
    }
  }

  // Setup //

  private void setupFields() {
    this.commandName = declaringInterface.getName() + "." + method.getName();
  }

  private void setupMethodSignature() {
    if (List.class.isAssignableFrom(method.getReturnType())) {
      returnsList = true;
    }
    final Class<?>[] argTypes = method.getParameterTypes();
    for (int i = 0; i < argTypes.length; i++) {
      if (RowBounds.class.isAssignableFrom(argTypes[i])) {
        rowBoundsIndex = i;
      } else {
        String paramName = String.valueOf(paramPositions.size());
        paramName = getParamNameFromAnnotation(i, paramName);
        paramNames.add(paramName);
        paramPositions.add(i);
      }
    }
  }

  private String getParamNameFromAnnotation(int i, String paramName) {
    Object[] paramAnnos = method.getParameterAnnotations()[i];
    for (int j = 0; j < paramAnnos.length; j++) {
      if (paramAnnos[j] instanceof Param) {
        hasNamedParameters = true;
        paramName = ((Param) paramAnnos[j]).value();
      }
    }
    return paramName;
  }

  private void setupCommandType() {
    MappedStatement ms = config.getMappedStatement(commandName);
    type = ms.getSqlCommandType();
    if (type == SqlCommandType.UNKNOWN) {
      throw new BindingException("Unknown execution method for: " + commandName);
    }
  }

  private void validateStatement() {
    try {
      config.getMappedStatement(commandName);
    } catch (Exception e) {
      throw new BindingException("Invalid bound statement (not found): " + commandName, e);
    }
  }

}
