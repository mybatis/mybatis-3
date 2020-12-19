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
package org.apache.ibatis.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Optional;
import java.util.Properties;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.exceptions.ExceptionFactory.DefaultExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;

/**
 * Builds {@link SqlSession} instances.
 *
 * @author Clinton Begin
 */
public class SqlSessionFactoryBuilder {

  public SqlSessionFactory build(Reader reader) {
    return build(reader, null, null, null);
  }

  public SqlSessionFactory build(Reader reader, ExceptionFactory exceptionFactory) {
      return build(reader, null, null, exceptionFactory);
    }

  public SqlSessionFactory build(Reader reader, String environment) {
    return build(reader, environment, null, null);
  }

  public SqlSessionFactory build(Reader reader, String environment, ExceptionFactory exceptionFactory) {
      return build(reader, environment, null, exceptionFactory);
    }

  public SqlSessionFactory build(Reader reader, Properties properties) {
    return build(reader, null, properties, null);
  }

  public SqlSessionFactory build(Reader reader, Properties properties, ExceptionFactory exceptionFactory) {
      return build(reader, null, properties, exceptionFactory);
    }

  public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
    return build(reader, environment, properties, null);
  }
  
  public SqlSessionFactory build(Reader reader, String environment, Properties properties, ExceptionFactory pExceptionFactory) {
    try {
      XMLConfigBuilder parser = new XMLConfigBuilder(reader, environment, properties);
      return build(parser.parse());
    } catch (Exception e) {
      ExceptionFactory exceptionFactory = Optional.ofNullable(pExceptionFactory).orElse(DefaultExceptionFactory.INSTANCE);
      throw exceptionFactory.wrapException("Error building SqlSession.", e);
    } finally {
      ErrorContext.instance().reset();
      try {
        reader.close();
      } catch (IOException e) {
        // Intentionally ignore. Prefer previous error.
      }
    }
  }

  public SqlSessionFactory build(InputStream inputStream) {
    return build(inputStream, null, null, null);
  }
  
  public SqlSessionFactory build(InputStream inputStream, ExceptionFactory exceptionFactory) {
    return build(inputStream, null, null, exceptionFactory);
  }

  public SqlSessionFactory build(InputStream inputStream, String environment) {
    return build(inputStream, environment, null, null);
  }
  
  public SqlSessionFactory build(InputStream inputStream, String environment, ExceptionFactory exceptionFactory) {
    return build(inputStream, environment, null, exceptionFactory);
  }

  public SqlSessionFactory build(InputStream inputStream, Properties properties) {
    return build(inputStream, null, properties, null);
  }
  
  public SqlSessionFactory build(InputStream inputStream, Properties properties, ExceptionFactory exceptionFactory) {
    return build(inputStream, null, properties, exceptionFactory);
  }

  public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
    return build(inputStream, environment, properties, null);
  }
  
  public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties, ExceptionFactory pExceptionFactory) {
    try {
      XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
      return build(parser.parse());
    } catch (Exception e) {
      ExceptionFactory exceptionFactory = Optional.ofNullable(pExceptionFactory).orElse(DefaultExceptionFactory.INSTANCE);
      throw exceptionFactory.wrapException("Error building SqlSession.", e);
    } finally {
      ErrorContext.instance().reset();
      try {
        inputStream.close();
      } catch (IOException e) {
        // Intentionally ignore. Prefer previous error.
      }
    }
  }

  public SqlSessionFactory build(Configuration config) {
    return new DefaultSqlSessionFactory(config);
  }

}
