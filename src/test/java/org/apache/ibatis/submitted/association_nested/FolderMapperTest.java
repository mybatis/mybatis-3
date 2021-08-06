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
package org.apache.ibatis.submitted.association_nested;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Loïc Guerrin <guerrin@fullsix.com>
 */
class FolderMapperTest {

  @Test
  void testFindWithChildren() throws Exception {
    try (Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:association_nested", "SA", "");
         Statement stmt = conn.createStatement()) {
      stmt.execute("create table folder (id int, name varchar(100), parent_id int)");
      stmt.execute("insert into folder (id, name) values(1, 'Root')");
      stmt.execute("insert into folder values(2, 'Folder 1', 1)");
      stmt.execute("insert into folder values(3, 'Folder 2', 1)");
      stmt.execute("insert into folder values(4, 'Folder 2_1', 3)");
      stmt.execute("insert into folder values(5, 'Folder 2_2', 3)");
    }

    /**
     * Root/
     *    Folder 1/
     *    Folder 2/
     *      Folder 2_1
     *      Folder 2_2
     */

    String resource = "org/apache/ibatis/submitted/association_nested/mybatis-config.xml";
    try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
      SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
      try (SqlSession session = sqlSessionFactory.openSession()) {
        FolderMapper postMapper = session.getMapper(FolderMapper.class);

        List<FolderFlatTree> folders = postMapper.findWithSubFolders("Root");

        Assertions.assertEquals(3, folders.size());
      }
    }
  }

}
