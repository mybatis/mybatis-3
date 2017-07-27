/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.submitted.sqlprovider;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

public interface BaseMapper<T> {

  @SelectProvider(type= OurSqlBuilder.class, method= "buildSelectByIdProviderContextOnly")
  @ContainsLogicalDelete
  T selectById(Integer id);

  @SelectProvider(type= OurSqlBuilder.class, method= "buildSelectByIdProviderContextOnly")
  T selectActiveById(Integer id);

  @SelectProvider(type= OurSqlBuilder.class, method= "buildSelectByNameOneParamAndProviderContext")
  @ContainsLogicalDelete
  List<T> selectByName(String name);

  @SelectProvider(type= OurSqlBuilder.class, method= "buildSelectByNameOneParamAndProviderContext")
  List<T> selectActiveByName(String name);

  @SelectProvider(type= OurSqlBuilder.class, method= "buildSelectByIdAndNameMultipleParamAndProviderContextWithAtParam")
  @ContainsLogicalDelete
  List<T> selectByIdAndNameWithAtParam(@Param("id") Integer id, @Param("name") String name);

  @SelectProvider(type= OurSqlBuilder.class, method= "buildSelectByIdAndNameMultipleParamAndProviderContextWithAtParam")
  List<T> selectActiveByIdAndNameWithAtParam(@Param("id") Integer id, @Param("name") String name);

  @SelectProvider(type= OurSqlBuilder.class, method= "buildSelectByIdAndNameMultipleParamAndProviderContext")
  @ContainsLogicalDelete
  List<T> selectByIdAndName(Integer id, String name);

  @SelectProvider(type= OurSqlBuilder.class, method= "buildSelectByIdAndNameMultipleParamAndProviderContext")
  List<T> selectActiveByIdAndName(Integer id, String name);

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  @interface ContainsLogicalDelete {
    boolean value() default false;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface Meta {
    String tableName();
  }

}
