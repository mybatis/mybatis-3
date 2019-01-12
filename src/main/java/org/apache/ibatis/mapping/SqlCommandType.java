/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.mapping;

import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.session.SqlSession;

/**
 * @author Clinton Begin
 */
public enum SqlCommandType {
  UNKNOWN((method, sqlSession, args)->method.executeUnknown()),
  INSERT((method, sqlSession, args)->method.executeInsert(sqlSession, args)),
  UPDATE((method, sqlSession, args)->method.executeUpdate(sqlSession, args)),
  DELETE((method, sqlSession, args)->method.executeDelete(sqlSession, args)),
  SELECT((method, sqlSession, args)->method.executeSelect(sqlSession, args)),
  FLUSH((method, sqlSession, args)->method.executeFlush(sqlSession));

  private SqlCommandExecutor sqlCommandExecutor;

  SqlCommandType(SqlCommandExecutor sqlCommandExecutor){
    this.sqlCommandExecutor = sqlCommandExecutor;
  }

  public Object executeCommand(MapperMethod method, SqlSession sqlSession, Object[] args){
    return sqlCommandExecutor.execute(method, sqlSession, args);
  }

  private interface SqlCommandExecutor{
    Object execute(MapperMethod method, SqlSession sqlSession, Object[] args);
  }
}
