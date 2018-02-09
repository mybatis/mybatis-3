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

import java.lang.reflect.Method;

import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.handler.MapperHandlerContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.SqlSession;

/**
 * @author Felix Lin
 * @since 3.4.6
 */
public class UpdateMapperHandler extends AbstractMapperHandler {

    @Override
    public boolean supports(MapperHandlerContext context) {
        MappedStatement statement = context.getMappedStatement();
        if (null != statement) {
            SqlCommandType type = statement.getSqlCommandType();
            return type.equals(SqlCommandType.INSERT) || type.equals(SqlCommandType.UPDATE) || type.equals(SqlCommandType.DELETE);
        }
        return false;
    }

    @Override
    public Object execute(SqlSession sqlSession, Object[] args, MapperHandlerContext context) {
        MappedStatement statement = context.getMappedStatement();
        Object param = context.getParamResolver().getNamedParams(args);
        int count = sqlSession.update(statement.getId(), param);
        return rowCountResult(context.getMethod(), context.getReturnType(), count);
    }

    private Object rowCountResult(Method method, Class<?> returnType, int rowCount) {
        final Object result;
        if (void.class.equals(returnType)) {
            result = null;
        } else if (Integer.class.equals(returnType) || Integer.TYPE.equals(returnType)) {
            result = rowCount;
        } else if (Long.class.equals(returnType) || Long.TYPE.equals(returnType)) {
            result = (long) rowCount;
        } else if (Boolean.class.equals(returnType) || Boolean.TYPE.equals(returnType)) {
            result = rowCount > 0;
        } else {
            throw new BindingException("Mapper method '" + method + "' has an unsupported return type: " + returnType);
        }
        return result;
    }

}
