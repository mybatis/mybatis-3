/*
 *    Copyright 2009-2021 the original author or authors.
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
package org.apache.ibatis.submitted.typehandler;

import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.submitted.typehandler.Product.ProductId;
import org.apache.ibatis.type.JdbcType;

public interface Mapper {

  @Select("select * from users where id = #{value}")
  @Results({
    @Result(column="id", property="id"),
    @Result(column="name", property="name"),
    @Result(column="city", property="city", jdbcType=JdbcType.CHAR),
    @Result(column="state", property="state", jdbcType=JdbcType.VARCHAR)
  })
  User getUser(Integer id);

  @Insert({
    "insert into product (name) values (#{name})"
  })
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insertProduct(Product product);

  @Select("select id, name from product where name = #{value}")
  Product getProductByName(String name);

  Product getProductByNameXml(String name);

  @Select("select id, name from product where name = #{value}")
  @ConstructorArgs({
    @Arg(id = true, column="id", javaType = ProductId.class, jdbcType=JdbcType.INTEGER),
    @Arg(column="name")
  })
  Product getProductByNameUsingConstructor(String name);

  @Select("select id from product where name = #{value}")
  ProductId getProductIdByName(String name);
}
