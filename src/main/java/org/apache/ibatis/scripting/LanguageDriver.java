/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.scripting;

import java.util.Map;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;

public interface LanguageDriver {

  /**
   * Constants for language driver context.
   *
   * @since 3.5.1
   */
  class ContextKey {

    private ContextKey() {
      // NOP
    }

    /**
     * The context key name for statement id.
     */
    public static final String STATEMENT_ID = "statementId";

  }

  /**
   * Creates a {@link ParameterHandler} that passes the actual parameters to the the JDBC statement.
   *
   * @param mappedStatement The mapped statement that is being executed
   * @param parameterObject The input parameter object (can be null)
   * @param boundSql The resulting SQL once the dynamic language has been executed.
   * @return a {@link ParameterHandler} that passes the actual parameters to the the JDBC statement
   * @author Frank D. Martinez [mnesarco]
   * @see DefaultParameterHandler
   */
  ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);

  /**
   * Creates an {@link SqlSource} that will hold the statement read from a mapper xml file.
   * It is called during startup, when the mapped statement is read from a class or an xml file.
   *
   * @param configuration The MyBatis configuration
   * @param script XNode parsed from a XML file
   * @param parameterType input parameter type got from a mapper method or specified in the parameterType xml attribute. Can be null.
   * @return an {@link SqlSource} that will hold the statement read from a mapper xml file
   */
  SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType);

  /**
   * Creates an {@link SqlSource} that will hold the statement read from a mapper xml file.
   * It is called during startup, when the mapped statement is read from a class or an xml file.
   *
   * @param configuration The MyBatis configuration
   * @param script XNode parsed from a XML file
   * @param parameterType input parameter type got from a mapper method or specified in the parameterType xml attribute. Can be null.
   * @param context context for language driver (e.g. statement id, etc...)
   * @return an {@link SqlSource} that will hold the statement read from a mapper xml file
   * @since 3.5.1
   */
  default SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType, Map<String, Object> context) {
    return createSqlSource(configuration, script, parameterType);
  }

  /**
   * Creates an {@link SqlSource} that will hold the statement read from an annotation.
   * It is called during startup, when the mapped statement is read from a class or an xml file.
   *
   * @param configuration The MyBatis configuration
   * @param script The content of the annotation
   * @param parameterType input parameter type got from a mapper method or specified in the parameterType xml attribute. Can be null.
   * @return an {@link SqlSource} that will hold the statement read from an annotation
   */
  SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType);

  /**
   * Creates an {@link SqlSource} that will hold the statement read from an annotation.
   * It is called during startup, when the mapped statement is read from a class or an xml file.
   *
   * @param configuration The MyBatis configuration
   * @param script The content of the annotation
   * @param parameterType input parameter type got from a mapper method or specified in the parameterType xml attribute. Can be null.
   * @param context context for language driver (e.g. statement id, etc...)
   * @return an {@link SqlSource} that will hold the statement read from an annotation
   * @since 3.5.1
   */
  default SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType, Map<String, Object> context) {
    return createSqlSource(configuration, script, parameterType);
  }

}
