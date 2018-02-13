/**
 * Copyright 2009-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.ibatis.binding.handler.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.binding.handler.MapperHandlerContext;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.lang.UsesJava8;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

/**
 * @author Felix Lin
 * @since 3.4.6
 */
@UsesJava8
public class OptionalMapperHandler extends SelectMapperHandler {

    @Override
    public boolean supports(MapperHandlerContext context) {
        return super.supports(context)
                && Optional.class.isAssignableFrom(context.getMethod().getReturnType());
    }

    @Override
    public Object execute(SqlSession sqlSession, Object[] args, MapperHandlerContext context) {
        Object result = null;
        Class<?> returnType = getOptionalType(context);
        if (null == returnType) {
            throw new RuntimeException("not supported return type: [" + context.getMethod().getGenericReturnType() + "]");
        }

        String sqlId = context.getMappedStatement().getId();
        Object param = context.getParamResolver().getNamedParams(args);
        RowBounds rowBounds = extractParam(args, RowBounds.class);
        if (context.getConfiguration().getObjectFactory().isCollection(returnType) || returnType.isArray()) {
            result = super.executeMany(sqlSession, sqlId, param, rowBounds, returnType);
        } else if (Map.class.isAssignableFrom(returnType) && context.getMethod().isAnnotationPresent(MapKey.class)) {
            result = super.executeMany(sqlSession, sqlId, param, rowBounds, returnType);
        } else if (Cursor.class.isAssignableFrom(returnType)) {
            result = super.executeCursor(sqlSession, sqlId, param, rowBounds);
        } else {
            result = sqlSession.selectOne(sqlId, param);
        }
        return Optional.ofNullable(result);
    }

    private Class<?> getOptionalType(MapperHandlerContext context) {
        ParameterizedType genericReturnType = (ParameterizedType) context.getMethod().getGenericReturnType();
        Type type = genericReturnType.getActualTypeArguments()[0];
        Class<?> cls = null;
        if (type instanceof ParameterizedType) {
            cls = (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof Class) {
            cls = (Class<?>) type;
        }
        return cls;
    }

}
