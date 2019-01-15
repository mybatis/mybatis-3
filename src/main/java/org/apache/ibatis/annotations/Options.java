/**
 *    Copyright 2009-2018 the original author or authors.
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
package org.apache.ibatis.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Options {
  /**
   * The options for the {@link Options#flushCache()}.
   * The default is {@link FlushCachePolicy#DEFAULT}
   */
  enum FlushCachePolicy {
    /** <code>false</code> for select statement; <code>true</code> for insert/update/delete statement. */
    DEFAULT,
    /** Flushes cache regardless of the statement type. */
    TRUE,
    /** Does not flush cache regardless of the statement type. */
    FALSE
  }

  /**
   * The options for the {@link Options#generatedKeys()}.
   * 
   * @author Kazuki Shimizu
   * @since 3.5.0
   */
  enum GeneratedKeysPolicy {
    /**
     * Follow global configuration (See {@link Configuration#isUseGeneratedKeys()})
     */
    DEFAULT(null),
    /**
     * Use the JDBC standard generated keys
     */
    USE(Boolean.TRUE),
    /**
     * Not use the JDBC standard generated keys
     */
    NOT_USE(Boolean.FALSE);

    private final Boolean useGeneratedKeys;

    GeneratedKeysPolicy(Boolean useGeneratedKeys) {
      this.useGeneratedKeys = useGeneratedKeys;
    }

    /**
     * Return whether use the JDBC standard generated keys.
     * @param defaultValue a value when policy is {@link #DEFAULT}.
     * @return If use the JDBC standard generated keys, return {@code true}.
     */
    public boolean isUseGeneratedKeys(boolean defaultValue) {
      if (this.useGeneratedKeys == null) {
        return defaultValue;
      }
      return this.useGeneratedKeys;
    }

  }

  boolean useCache() default true;

  FlushCachePolicy flushCache() default FlushCachePolicy.DEFAULT;

  ResultSetType resultSetType() default ResultSetType.DEFAULT;

  StatementType statementType() default StatementType.PREPARED;

  int fetchSize() default -1;

  int timeout() default -1;

  /**
   * If set to {@code true}, mark to use the JDBC standard generated keys. Otherwise, depends on
   * {@link #generatedKeys()}.
   *
   * @deprecated Since 3.5.0, Please change to use the {@link #generatedKeys()} instead of this.
   *             This attribute will remove at future.
   */
  @Deprecated
  boolean useGeneratedKeys() default false;

  /**
   * @return A usage policy for the JDBC standard generated keys
   * @see GeneratedKeysPolicy
   * @since 3.5.0
   */
  GeneratedKeysPolicy generatedKeys() default GeneratedKeysPolicy.DEFAULT;

  String keyProperty() default "";

  String keyColumn() default "";
  
  String resultSets() default "";

}
