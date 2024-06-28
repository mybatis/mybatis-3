/*
 *    Copyright 2009-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.binding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.domain.blog.Blog;
import org.apache.ibatis.domain.blog.Post;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ConstructorCollectionBindingTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setup() throws Exception {
    DataSource dataSource = BaseDataTest.createBlogDataSource();
    BaseDataTest.runScript(dataSource, BaseDataTest.BLOG_DDL);
    BaseDataTest.runScript(dataSource, BaseDataTest.BLOG_DATA);
    TransactionFactory transactionFactory = new JdbcTransactionFactory();
    Environment environment = new Environment("Production", transactionFactory, dataSource);
    Configuration configuration = new Configuration(environment);
    configuration.setLazyLoadingEnabled(true);
    configuration.setUseActualParamName(false); // to test legacy style reference (#{0} #{1})

    configuration.setExperimentalConstructorCollectionMapping(true); // # issue 101

    configuration.getTypeAliasRegistry().registerAlias(Blog.class);
    configuration.getTypeAliasRegistry().registerAlias(Post.class);
    configuration.getTypeAliasRegistry().registerAlias(Author.class);
    configuration.addMapper(BoundBlogMapper.class);
    configuration.addMapper(BoundAuthorMapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
  }

  @Test // issue #480 and #101
  void shouldExecuteBoundSelectBlogUsingConstructorWithResultMapCollection() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Blog blog = mapper.selectBlogUsingConstructorWithResultMapCollection(1);
      assertEquals(1, blog.getId());
      assertEquals("Jim Business", blog.getTitle());
      assertNotNull(blog.getAuthor(), "author should not be null");
      List<Post> posts = blog.getPosts();
      assertThat(posts).isNotNull().hasSize(2);
    }
  }
}
