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
package org.apache.ibatis.benchmarks.jmh.basic;

import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.binding.BoundAuthorMapper;
import org.apache.ibatis.binding.BoundBlogMapper;
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
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@Fork(1)
@Warmup(iterations = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class BasicBlogBenchmark {

  @State(Scope.Benchmark)
  public static class SessionFactoryState {

    private SqlSessionFactory sqlSessionFactory;

    @Setup
    public void setup() throws Exception {
      DataSource dataSource = BaseDataTest.createBlogDataSource();
      BaseDataTest.runScript(dataSource, BaseDataTest.BLOG_DDL);
      BaseDataTest.runScript(dataSource, BaseDataTest.BLOG_DATA);

      TransactionFactory transactionFactory = new JdbcTransactionFactory();
      Environment environment = new Environment("Production", transactionFactory, dataSource);
      Configuration configuration = new Configuration(environment);
      configuration.getTypeAliasRegistry().registerAlias(Blog.class);
      configuration.getTypeAliasRegistry().registerAlias(Post.class);
      configuration.getTypeAliasRegistry().registerAlias(Author.class);
      configuration.addMapper(BoundBlogMapper.class);
      configuration.addMapper(BoundAuthorMapper.class);
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    public SqlSessionFactory getSqlSessionFactory() {
      return sqlSessionFactory;
    }
  }

  @Benchmark
  public Blog retrieveSingleBlogUsingConstructorWithResultMap(SessionFactoryState sessionFactoryState) {
    try (SqlSession sqlSession = sessionFactoryState.getSqlSessionFactory().openSession()) {
      final BoundBlogMapper mapper = sqlSession.getMapper(BoundBlogMapper.class);
      return mapper.selectBlogUsingConstructorWithResultMap(1);
    }
  }

  @Benchmark
  public Blog retrieveSingleBlog(SessionFactoryState sessionFactoryState) {
    try (SqlSession sqlSession = sessionFactoryState.getSqlSessionFactory().openSession()) {
      final BoundBlogMapper mapper = sqlSession.getMapper(BoundBlogMapper.class);
      return mapper.selectBlog(1);
    }
  }
}
