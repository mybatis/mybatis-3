/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.type;

import com.alibaba.fastjson.JSON;
import org.apache.ibatis.executor.result.ResultMapException;
import org.apache.ibatis.reflection.MetaObject;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A Implement example of JsonTypeHandler
 * @author tianhao
 */
public class FastJsonTypeHandler extends BaseJsonTypeHandler {

    private final Map<String, Type> fieldClassCache = new HashMap<String, Type>();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, JSON.toJSONString(parameter));
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName, String property, Object targetObject)
            throws SQLException {
        try {
            String jsonStr = rs.getString(columnName);
            String key = targetObject.getClass().getName() + ":" + columnName;
            if (fieldClassCache.containsKey(key)) {
                Type type = fieldClassCache.get(key);
                return JSON.parseObject(jsonStr, type);
            }
            Field field = getField(targetObject.getClass(), property);
            Type type = field.getGenericType();
            fieldClassCache.put(key, type);
            return JSON.parseObject(jsonStr, type);
        } catch (Exception e) {
            throw new ResultMapException("Error attempting to get column '" + columnName +
                    "' from result set.  Cause: " + e, e);
        }
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName, Type targetType) throws SQLException {
        try {
            String jsonStr = rs.getString(columnName);
            return JSON.parseObject(jsonStr, targetType);
        } catch (Exception e) {
            throw new ResultMapException("Error attempting to get column '" + columnName +
                    "' from result set.  Cause: " + e, e);
        }
    }

    /*
     * Find name field in cls and it's superClass,until found or encountered Object.class
     */
    private static Field getField(Class cls, String name) throws NoSuchFieldException {
        try {
            return cls.getDeclaredField(name);
        } catch (NoSuchFieldException var6) {
            Class current = cls;
            while (cls != Object.class) {
                try {
                    return current.getDeclaredField(name);
                } catch (NoSuchFieldException var5) {
                    current = current.getSuperclass();
                }
            }
        }
        throw new NoSuchElementException("No Field '"+ name + "' for Class "+ cls.getName());
    }
}
