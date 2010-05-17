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
