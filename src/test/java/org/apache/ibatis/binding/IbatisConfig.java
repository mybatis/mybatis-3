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
package org.apache.ibatis.binding;

import domain.blog.Author;
import domain.blog.Blog;
import domain.blog.Post;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;

public class IbatisConfig {

  public SqlSessionFactory getSqlSessionFactory() {
    try {
      DataSource dataSource = BaseDataTest.createBlogDataSource();
      BaseDataTest.runScript(dataSource, BaseDataTest.BLOG_DDL);
      BaseDataTest.runScript(dataSource, BaseDataTest.BLOG_DATA);
      TransactionFactory transactionFactory = new JdbcTransactionFactory();
      Environment environment = new Environment("Production", transactionFactory, dataSource);
      Configuration configuration = new Configuration(environment);
      configuration.setLazyLoadingEnabled(true);
      configuration.getTypeAliasRegistry().registerAlias(Blog.class);
      configuration.getTypeAliasRegistry().registerAlias(Post.class);
      configuration.getTypeAliasRegistry().registerAlias(Author.class);
      configuration.addMapper(BoundBlogMapper.class);
      configuration.addMapper(BoundAuthorMapper.class);
      return new SqlSessionFactoryBuilder().build(configuration);
    } catch (Exception e) {
      throw new RuntimeException("Error initializing SqlSessionFactory. Cause: " + e, e);
    }
  }

}
