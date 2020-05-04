/**
 *    Copyright 2009-2020 the original author or authors.
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
package org.apache.ibatis.submitted.repeatable;

import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.UpdateProvider;

@CacheNamespace(readWrite = false)
public interface Mapper {

  @Select(value = "SELECT id, name, 'HSQL' as databaseName FROM users WHERE id = #{id}", databaseId = "hsql")
  @Select(value = "SELECT id, name, 'DERBY' as databaseName FROM users WHERE id = #{id}", databaseId = "derby")
  @Select("SELECT id, name, 'DEFAULT' as databaseName FROM users WHERE id = #{id}")
  @Options(useCache = false, databaseId = "hsql")
  @Options(useCache = false, databaseId = "derby")
  User getUser(Integer id);

  @SelectProvider(type = HsqlSqlProvider.class, method = "getUserUsingProvider", databaseId = "hsql")
  @SelectProvider(type = DerbySqlProvider.class, method = "getUserUsingProvider", databaseId = "derby")
  @SelectProvider(type = DefaultSqlProvider.class, method = "getUserUsingProvider")
  @Options(databaseId = "hsql")
  @Options(databaseId = "derby")
  @Options(flushCache = Options.FlushCachePolicy.TRUE)
  User getUserUsingProvider(Integer id);

  @SelectProvider(type = HsqlSqlProvider.class, method = "getUserUsingProvider", databaseId = "hsql")
  @Select(value = "SELECT id, name, 'DERBY' as databaseName FROM users WHERE id = #{id}", databaseId = "derby")
  @Select("SELECT id, name, 'DEFAULT' as databaseName FROM users WHERE id = #{id}")
  @Options(useCache = false, databaseId = "hsql")
  @Options(useCache = false, databaseId = "derby")
  User getUserUsingBoth(Integer id);

  @Insert(value = "INSERT INTO users (id, name) VALUES(#{id}, #{name} || ' HSQL')", databaseId = "hsql")
  @Insert(value = "INSERT INTO users (id, name) VALUES(#{id}, #{name} || ' DERBY')", databaseId = "derby")
  @Insert("INSERT INTO users (id, name) VALUES(#{id}, #{name} || ' DEFAULT')")
  @SelectKey(statement = "SELECT COUNT(*) + 1 FROM users", keyProperty = "id", before = true, resultType = Integer.class, databaseId = "hsql")
  @SelectKey(statement = "SELECT COUNT(*) + 1001 FROM users", keyProperty = "id", before = true, resultType = Integer.class, databaseId = "derby")
  @SelectKey(statement = "SELECT COUNT(*) + 10001 FROM users", keyProperty = "id", before = true, resultType = Integer.class)
  void insertUser(User user);

  @InsertProvider(type = HsqlSqlProvider.class, method = "insertUserUsingProvider", databaseId = "hsql")
  @InsertProvider(type = DerbySqlProvider.class, method = "insertUserUsingProvider", databaseId = "derby")
  @InsertProvider(type = DefaultSqlProvider.class, method = "insertUserUsingProvider")
  @SelectKey(statement = "SELECT COUNT(*) + 1 FROM users", keyProperty = "id", before = true, resultType = Integer.class, databaseId = "hsql")
  @SelectKey(statement = "SELECT COUNT(*) + 1001 FROM users", keyProperty = "id", before = true, resultType = Integer.class, databaseId = "derby")
  @SelectKey(statement = "SELECT COUNT(*) + 10001 FROM users", keyProperty = "id", before = true, resultType = Integer.class)
  void insertUserUsingProvider(User user);

  @Update(value = "UPDATE users SET name = name || ' HSQL' WHERE id = #{id}", databaseId = "hsql")
  @Update(value = "UPDATE users SET name = name || ' DERBY' WHERE id = #{id}", databaseId = "derby")
  @Update("UPDATE users SET name = name || ' DEFAULT' WHERE id = #{id}")
  void updateUserName(Integer id);

  @UpdateProvider(type = HsqlSqlProvider.class, method = "updateUserNameUsingProvider", databaseId = "hsql")
  @UpdateProvider(type = DerbySqlProvider.class, method = "updateUserNameUsingProvider", databaseId = "derby")
  @UpdateProvider(type = DefaultSqlProvider.class, method = "updateUserNameUsingProvider")
  void updateUserNameUsingProvider(Integer id);

  @Delete(value = "DELETE FROM users WHERE name LIKE '%HSQL%'", databaseId = "hsql")
  @Delete(value = "DELETE FROM users WHERE name LIKE '%DERBY%'", databaseId = "derby")
  @Delete("DELETE FROM users WHERE name LIKE '%DEFAULT%'")
  void delete();

  @DeleteProvider(type = HsqlSqlProvider.class, method = "delete", databaseId = "hsql")
  @DeleteProvider(type = DerbySqlProvider.class, method = "delete", databaseId = "derby")
  @DeleteProvider(type = DefaultSqlProvider.class, method = "delete")
  void deleteUsingProvider();

  @Select("SELECT COUNT(*) FROM users")
  int count();

  @Select("SELECT COUNT(*) FROM users WHERE name LIKE '%' || #{dataabse} || '%'")
  int countByCurrentDatabase(String database);

  class HsqlSqlProvider {
    public static String getUserUsingProvider() {
      return "SELECT id, name, 'HSQL' as databaseName FROM users WHERE id = #{id}";
    }
    public static String insertUserUsingProvider() {
      return "INSERT INTO users (id, name) VALUES(#{id}, #{name} || ' HSQL')";
    }
    public static String updateUserNameUsingProvider() {
      return "UPDATE users SET name = name || ' HSQL' WHERE id = #{id}";
    }
    public static String delete() {
      return "DELETE FROM users WHERE name LIKE '%HSQL%'";
    }
  }

  class DerbySqlProvider {
    public static String getUserUsingProvider() {
      return "SELECT id, name, 'DERBY' as databaseName FROM users WHERE id = #{id}";
    }
    public static String insertUserUsingProvider() {
      return "INSERT INTO users (id, name) VALUES(#{id}, #{name} || ' DERBY')";
    }
    public static String updateUserNameUsingProvider() {
      return "UPDATE users SET name = name || ' DERBY' WHERE id = #{id}";
    }
    public static String delete() {
      return "DELETE FROM users WHERE name LIKE '%DERBY%'";
    }
  }

  class DefaultSqlProvider {
    public static String getUserUsingProvider() {
      return "SELECT id, name, 'DEFAULT' as databaseName FROM users WHERE id = #{id}";
    }

    public static String insertUserUsingProvider() {
      return "INSERT INTO users (id, name) VALUES(#{id}, #{name} || ' DEFAULT')";
    }
    public static String updateUserNameUsingProvider() {
      return "UPDATE users SET name = name || ' DEFAULT' WHERE id = #{id}";
    }
    public static String delete() {
      return "DELETE FROM users WHERE name LIKE '%DEFAULT%'";
    }
  }

}
