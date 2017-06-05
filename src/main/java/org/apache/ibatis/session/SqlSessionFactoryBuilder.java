/**
 * Copyright 2009-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;

/**
 * Builds {@link SqlSession} instances.
 *
 * @author Clinton Begin
 */
// TODO: 17/4/26 by zmyer
public class SqlSessionFactoryBuilder {

    // TODO: 17/4/26 by zmyer
    public SqlSessionFactory build(Reader reader) {
        return build(reader, null, null);
    }

    // TODO: 17/4/26 by zmyer
    public SqlSessionFactory build(Reader reader, String environment) {
        return build(reader, environment, null);
    }

    // TODO: 17/4/26 by zmyer
    public SqlSessionFactory build(Reader reader, Properties properties) {
        return build(reader, null, properties);
    }

    // TODO: 17/4/26 by zmyer
    public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
        try {
            //创建xml配置构建器
            XMLConfigBuilder parser = new XMLConfigBuilder(reader, environment, properties);
            //开始解析xml,并构建session对象
            return build(parser.parse());
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                reader.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    // TODO: 17/4/26 by zmyer
    public SqlSessionFactory build(InputStream inputStream) {
        return build(inputStream, null, null);
    }

    // TODO: 17/4/26 by zmyer
    public SqlSessionFactory build(InputStream inputStream, String environment) {
        return build(inputStream, environment, null);
    }

    // TODO: 17/4/26 by zmyer
    public SqlSessionFactory build(InputStream inputStream, Properties properties) {
        return build(inputStream, null, properties);
    }

    // TODO: 17/4/26 by zmyer
    public SqlSessionFactory build(InputStream inputStream, String environment,
        Properties properties) {
        try {
            //创建xml配置构建器
            XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
            //解析xml,创建会话工厂对象
            return build(parser.parse());
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                inputStream.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    // TODO: 17/4/26 by zmyer
    public SqlSessionFactory build(Configuration config) {
        return new DefaultSqlSessionFactory(config);
    }

}
