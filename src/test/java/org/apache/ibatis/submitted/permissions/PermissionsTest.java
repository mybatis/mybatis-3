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
package org.apache.ibatis.submitted.permissions;

import java.io.Reader;
import java.sql.Connection;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class PermissionsTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create a SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/permissions/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/permissions/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.setErrorLogWriter(null);
    runner.runScript(reader);
    conn.commit();
    conn.close();
    reader.close();
  }

  @Test // see issue #168
  public void checkNestedResultMapLoop() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      final PermissionsMapper mapper = sqlSession.getMapper(PermissionsMapper.class);

      final List<Resource> resources = mapper.getResources();
      Assert.assertEquals(2, resources.size());

      final Resource firstResource = resources.get(0);
      final List<Principal> principalPermissions = firstResource.getPrincipals();
      Assert.assertEquals(1, principalPermissions.size());
      
      final Principal firstPrincipal = principalPermissions.get(0);
      final List<Permission> permissions = firstPrincipal.getPermissions();
      Assert.assertEquals(2, permissions.size());
      
      final Permission firstPermission = firstPrincipal.getPermissions().get(0);
      Assert.assertSame(firstResource, firstPermission.getResource());
      final Permission secondPermission = firstPrincipal.getPermissions().get(1);
      Assert.assertSame(firstResource, secondPermission.getResource());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void checkNestedSelectLoop() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      final PermissionsMapper mapper = sqlSession.getMapper(PermissionsMapper.class);

      final List<Resource> resources = mapper.getResource("read");
      Assert.assertEquals(1, resources.size());

      final Resource firstResource = resources.get(0);
      final List<Principal> principalPermissions = firstResource.getPrincipals();
      Assert.assertEquals(1, principalPermissions.size());
      
      final Principal firstPrincipal = principalPermissions.get(0);
      final List<Permission> permissions = firstPrincipal.getPermissions();
      Assert.assertEquals(4, permissions.size());

      boolean readFound = false;
      for (Permission permission : permissions) {
        if ("read".equals(permission.getPermission())) {
          Assert.assertSame(firstResource, permission.getResource());
          readFound = true;
        }
      }
      
      if (!readFound) {
        Assert.fail();
      }

    } finally {
      sqlSession.close();
    }
  }
  
}
