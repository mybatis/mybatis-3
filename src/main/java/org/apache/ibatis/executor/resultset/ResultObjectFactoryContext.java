/*
 *    Copyright 2009-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.executor.resultset;

import java.lang.reflect.Constructor;
import java.util.List;

import org.apache.ibatis.mapping.ResultMap;

public class ResultObjectFactoryContext {

  private ResultSetWrapper rsw;

  private ResultMap resultMap;

  private String columnPrefix;

  private List<Class<?>> constructorArgTypes;

  private List<Object> constructorArgs;

  private Constructor<?> constructor;

  public ResultSetWrapper getRsw() {
    return rsw;
  }

  public ResultMap getResultMap() {
    return resultMap;
  }

  public String getColumnPrefix() {
    return columnPrefix;
  }

  public List<Class<?>> getConstructorArgTypes() {
    return constructorArgTypes;
  }

  public List<Object> getConstructorArgs() {
    return constructorArgs;
  }

  public Constructor<?> getConstructor() {
    return constructor;
  }

  public void setRsw(ResultSetWrapper rsw) {
    this.rsw = rsw;
  }

  public void setResultMap(ResultMap resultMap) {
    this.resultMap = resultMap;
  }

  public void setColumnPrefix(String columnPrefix) {
    this.columnPrefix = columnPrefix;
  }

  public void setConstructorArgTypes(List<Class<?>> constructorArgTypes) {
    this.constructorArgTypes = constructorArgTypes;
  }

  public void setConstructorArgs(List<Object> constructorArgs) {
    this.constructorArgs = constructorArgs;
  }

  public void setConstructor(Constructor<?> constructor) {
    this.constructor = constructor;
  }

  public ResultObjectFactoryContext(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix,
      List<Class<?>> constructorArgTypes, List<Object> constructorArgs, Constructor<?> constructor) {
    this.rsw = rsw;
    this.resultMap = resultMap;
    this.columnPrefix = columnPrefix;
    this.constructorArgTypes = constructorArgTypes;
    this.constructorArgs = constructorArgs;
    this.constructor = constructor;
  }
}
