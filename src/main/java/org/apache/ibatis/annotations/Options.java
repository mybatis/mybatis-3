/*
 *    Copyright 2009-2011 The MyBatis Team
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

import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.mapping.StatementType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Options {
  public abstract boolean useCache() default true;

  public abstract boolean flushCache() default false;

  public abstract ResultSetType resultSetType() default ResultSetType.FORWARD_ONLY;

  public abstract StatementType statementType() default StatementType.PREPARED;

  public abstract int fetchSize() default -1;

  public abstract int timeout() default -1;

  public abstract boolean useGeneratedKeys() default false;

  public abstract String keyProperty() default "id";
  
  public abstract String keyColumn() default ""; 
}
