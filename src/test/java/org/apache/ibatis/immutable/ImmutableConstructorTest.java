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
package org.apache.ibatis.immutable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.domain.blog.Section;
import org.apache.ibatis.domain.blog.immutable.ImmutableAuthor;
import org.apache.ibatis.domain.blog.immutable.ImmutableBlog;
import org.apache.ibatis.domain.blog.immutable.ImmutablePost;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImmutableConstructorTest {

  private SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  void setup() throws Exception {
    final DataSource dataSource = BaseDataTest.createBlogDataSource();
    BaseDataTest.runScript(dataSource, BaseDataTest.BLOG_DDL);
    BaseDataTest.runScript(dataSource, BaseDataTest.BLOG_DATA);

    final TransactionFactory transactionFactory = new JdbcTransactionFactory();
    final Environment environment = new Environment("Production", transactionFactory, dataSource);
    final Configuration configuration = new Configuration(environment);

    configuration.addMapper(ImmutableBlogMapper.class);

    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
  }

  @Test
  void shouldSelectImmutableBlogUsingCollectionInConstructor() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      final ImmutableBlogMapper mapper = session.getMapper(ImmutableBlogMapper.class);
      final ImmutableBlog blog = mapper.retrieveFullImmutableBlog(1);

      assertEquals(1, blog.getId());
      assertEquals("Jim Business", blog.getTitle());

      final ImmutableAuthor author = blog.getAuthor();
      assertThat(author).isNotNull().isInstanceOf(ImmutableAuthor.class);
      assertThat(author.getEmail()).isEqualTo("jim@ibatis.apache.org");
      assertThat(author.getFavouriteSection()).isNotNull().isEqualTo(Section.NEWS);
      assertThat(author.getUsername()).isEqualTo("jim");
      assertThat(author.getPassword()).isNotEmpty();
      assertThat(author.getId()).isEqualTo(101);

      final List<ImmutablePost> posts = blog.getPosts();
      assertThat(posts).isNotNull().hasSize(2);

      final ImmutablePost postOne = posts.get(0);
      assertThat(postOne).isNotNull().isInstanceOf(ImmutablePost.class);
      assertThat(postOne.getCreatedOn()).isNotNull();
      assertThat(postOne.getAuthor()).isNotNull();
      assertThat(postOne.getSection()).isEqualTo(Section.NEWS);
      assertThat(postOne.getSubject()).isEqualTo("Corn nuts");
      assertThat(postOne.getBody()).isEqualTo("I think if I never smelled another corn nut it would be too soon...");
      assertThat(postOne.getComments()).isNotNull().extracting("name", "comment").containsExactly(
          tuple("troll", "I disagree and think..."), tuple("anonymous", "I agree and think troll is an..."));
      assertThat(postOne.getTags()).isNotNull().extracting("name").containsExactly("funny", "cool", "food");

      final ImmutablePost postTwo = posts.get(1);
      assertThat(postTwo).isNotNull().isInstanceOf(ImmutablePost.class);
      assertThat(postTwo.getCreatedOn()).isNotNull();
      assertThat(postTwo.getAuthor()).isNotNull();
      assertThat(postTwo.getSection()).isEqualTo(Section.VIDEOS);
      assertThat(postTwo.getSubject()).isEqualTo("Paul Hogan on Toy Dogs");
      assertThat(postTwo.getBody()).isEqualTo("That's not a dog.  THAT's a dog!");
      assertThat(postTwo.getComments()).isNotNull().isEmpty();

      assertThat(postTwo.getTags()).isNotNull().extracting("name").containsExactly("funny");
    }
  }

  @Test
  void shouldSelectAllImmutableBlogsUsingCollectionInConstructor() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      ImmutableBlogMapper mapper = session.getMapper(ImmutableBlogMapper.class);
      List<ImmutableBlog> blogs = mapper.retrieveAllBlogsAndPostsJoined();

      assertThat(blogs).isNotNull().hasSize(2);
      for (ImmutableBlog blog : blogs) {
        assertThat(blog).isNotNull().isInstanceOf(ImmutableBlog.class).extracting(ImmutableBlog::getPosts).isNotNull();
      }
    }
  }

  @Test
  void shouldSelectBlogWithoutPosts() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      ImmutableBlogMapper mapper = session.getMapper(ImmutableBlogMapper.class);
      List<ImmutableBlog> blogs = mapper.retrieveAllBlogsWithoutPosts();

      assertThat(blogs).isNotNull().hasSize(2);
    }
  }

  @Test
  void shouldSelectBlogWithPostsButNoCommentsOrTags() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      ImmutableBlogMapper mapper = session.getMapper(ImmutableBlogMapper.class);
      List<ImmutableBlog> blogs = mapper.retrieveAllBlogsWithPostsButNoCommentsOrTags();

      assertThat(blogs).isNotNull().hasSize(2);
    }
  }

  @Test
  void shouldFailToSelectBlogWithMissingConstructorForPostComments() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      ImmutableBlogMapper mapper = session.getMapper(ImmutableBlogMapper.class);
      assertThatThrownBy(mapper::retrieveAllBlogsWithMissingConstructor).isInstanceOf(PersistenceException.class)
          .hasCauseInstanceOf(ReflectionException.class).hasMessageContaining(
              "Error instantiating class org.apache.ibatis.domain.blog.immutable.ImmutablePost with invalid types");
    }
  }
}
