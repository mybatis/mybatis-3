/*
 *    Copyright 2009-2012 The MyBatis Team
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
package org.apache.ibatis.executor.keygen;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.RowBounds;

import java.sql.Statement;
import java.util.List;

public class SelectKeyGenerator implements KeyGenerator {
  public static final String SELECT_KEY_SUFFIX = "!selectKey";
  private boolean executeBefore;
  private MappedStatement keyStatement;

  public SelectKeyGenerator(MappedStatement keyStatement, boolean executeBefore) {
    this.executeBefore = executeBefore;
    this.keyStatement = keyStatement;
  }

  public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
    if (executeBefore) {
      processGeneratedKeys(executor, ms, parameter);
    }
  }

  public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
    if (!executeBefore) {
      processGeneratedKeys(executor, ms, parameter);
    }
  }

  private void processGeneratedKeys(Executor executor, MappedStatement ms, Object parameter) {
    try {
      final Configuration configuration = ms.getConfiguration();
      if (parameter != null) {
        String keyStatementName = ms.getId() + SELECT_KEY_SUFFIX;
        if (configuration.hasStatement(keyStatementName)) {

          if (keyStatement != null && keyStatement.getKeyProperties() != null) {
            String keyProperty = keyStatement.getKeyProperties()[0]; //just one key property is supported
            final MetaObject metaParam = configuration.newMetaObject(parameter);
            if (keyProperty != null && metaParam.hasSetter(keyProperty)) {
              // Do not close keyExecutor.
              // The transaction will be closed by parent executor.
              Executor keyExecutor = configuration.newExecutor(executor.getTransaction(), ExecutorType.SIMPLE);
              List<Object> values = keyExecutor.query(keyStatement, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
              if (values.size() > 1) {
                throw new ExecutorException("Select statement for SelectKeyGenerator returned more than one value.");
              }
              metaParam.setValue(keyProperty, values.get(0));
            }
          }
        }
      }
    } catch (Exception e) {
      throw new ExecutorException("Error selecting key or setting result to parameter object. Cause: " + e, e);
    }
  }

}
