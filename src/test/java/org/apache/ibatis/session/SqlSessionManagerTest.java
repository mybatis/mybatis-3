/*
 *    Copyright 2009-2012 the original author or authors.
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
package org.apache.ibatis.session;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.junit.BeforeClass;
import org.junit.Test;

import domain.blog.Author;
import domain.blog.mappers.AuthorMapper;

public class SqlSessionManagerTest extends BaseDataTest {

  private static SqlSessionManager manager;

  @BeforeClass
  public static void setup() throws Exception {
    createBlogDataSource();
    final String resource = "org/apache/ibatis/builder/MapperConfig.xml";
    final Reader reader = Resources.getResourceAsReader(resource);
    manager = SqlSessionManager.newInstance(reader);
  }

  @Test
  public void shouldThrowExceptionIfMappedStatementDoesNotExistAndSqlSessionIsOpen() throws Exception {
    try {
      manager.startManagedSession();
      manager.selectList("ThisStatementDoesNotExist");
      fail("Expected exception to be thrown due to statement that does not exist.");
    } catch (PersistenceException e) {
      assertTrue(e.getMessage().contains("does not contain value for ThisStatementDoesNotExist"));
    } finally {
      manager.close();
    }
  }

  @Test
  public void shouldCommitInsertedAuthor() throws Exception {
    try {
      manager.startManagedSession();
      AuthorMapper mapper = manager.getMapper(AuthorMapper.class);
      Author expected = new Author(500, "cbegin", "******", "cbegin@somewhere.com", "Something...", null);
      mapper.insertAuthor(expected);
      manager.commit();
      Author actual = mapper.selectAuthor(500);
      assertNotNull(actual);
    } finally {
      manager.close();
    }
  }

  @Test
  public void shouldRollbackInsertedAuthor() throws Exception {
    try {
      manager.startManagedSession();
      AuthorMapper mapper = manager.getMapper(AuthorMapper.class);
      Author expected = new Author(501, "lmeadors", "******", "lmeadors@somewhere.com", "Something...", null);
      mapper.insertAuthor(expected);
      manager.rollback();
      Author actual = mapper.selectAuthor(501);
      assertNull(actual);
    } finally {
      manager.close();
    }
  }

  @Test
  public void shouldImplicitlyRollbackInsertedAuthor() throws Exception {
    manager.startManagedSession();
    AuthorMapper mapper = manager.getMapper(AuthorMapper.class);
    Author expected = new Author(502, "emacarron", "******", "emacarron@somewhere.com", "Something...", null);
    mapper.insertAuthor(expected);
    manager.close();
    Author actual = mapper.selectAuthor(502);
    assertNull(actual);
  }

}
