/*
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
package org.apache.ibatis.submitted.permissions;

import java.io.Reader;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PermissionsTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/permissions/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/permissions/CreateDB.sql");
  }

  @Test // see issue #168
  void checkNestedResultMapLoop() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final PermissionsMapper mapper = sqlSession.getMapper(PermissionsMapper.class);

      final List<Resource> resources = mapper.getResources();
      Assertions.assertEquals(2, resources.size());

      final Resource firstResource = resources.get(0);
      final List<Principal> principalPermissions = firstResource.getPrincipals();
      Assertions.assertEquals(1, principalPermissions.size());

      final Principal firstPrincipal = principalPermissions.get(0);
      final List<Permission> permissions = firstPrincipal.getPermissions();
      Assertions.assertEquals(2, permissions.size());

      final Permission firstPermission = firstPrincipal.getPermissions().get(0);
      Assertions.assertSame(firstResource, firstPermission.getResource());
      final Permission secondPermission = firstPrincipal.getPermissions().get(1);
      Assertions.assertSame(firstResource, secondPermission.getResource());
    }
  }

  @Test
  void checkNestedSelectLoop() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final PermissionsMapper mapper = sqlSession.getMapper(PermissionsMapper.class);

      final List<Resource> resources = mapper.getResource("read");
      Assertions.assertEquals(1, resources.size());

      final Resource firstResource = resources.get(0);
      final List<Principal> principalPermissions = firstResource.getPrincipals();
      Assertions.assertEquals(1, principalPermissions.size());

      final Principal firstPrincipal = principalPermissions.get(0);
      final List<Permission> permissions = firstPrincipal.getPermissions();
      Assertions.assertEquals(4, permissions.size());

      boolean readFound = false;
      for (Permission permission : permissions) {
        if ("read".equals(permission.getPermission())) {
          Assertions.assertSame(firstResource, permission.getResource());
          readFound = true;
        }
      }

      if (!readFound) {
        Assertions.fail();
      }
    }
  }

}
