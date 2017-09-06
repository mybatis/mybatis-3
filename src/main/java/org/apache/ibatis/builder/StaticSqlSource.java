/**
 * Copyright 2009-2015 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.builder;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Clinton Begin
 * changes：<ul>
 * <li> support sql in query with whole array or collection,such as: "select * from Users where tag in(#{tagsCollectionOrArray})" </li>
 * <li> support insert a whole bean,such as "insert info Users(name,pasw) Values(#{user})" Instead Of "insert info Users(name,pasw) Values(#{user.name}, #{user.pasw})"</li>
 * </ul>
 */
public class StaticSqlSource implements SqlSource {

    private String sql;
    private List<ParameterMapping> parameterMappings;
    private Configuration configuration;
    private SqlSegEntry[] sqlCache;
    private List<ParameterMapping> parameterMappingsForSelectIn;

    public StaticSqlSource(Configuration configuration, String sql) {
        this(configuration, sql, null);
    }

    public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
		trySqlEnhance(configuration, sql, parameterMappings);
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        String sql = this.sql;
        List<ParameterMapping> parameterMappings = this.parameterMappings;
		// Select IN with array|collection support.
        if (sqlCache != null) {
            Map<?, ?> argMap = (Map<?, ?>) parameterObject;
            StringBuilder sqlBuilder = new StringBuilder(sql.length() + 128);
            for (SqlSegEntry e : sqlCache) {
                sqlBuilder.append(e.segment);
                if (e.index >= 0) {
                    ParameterMapping m = parameterMappings.get(e.index);
                    String key = m.getProperty();
                    Object obj;
                    if (argMap.containsKey(key)) {
                        obj = argMap.get(key);
                    } else {
                        obj = argMap.values().iterator().next();
                    }
                    appendParameter(sqlBuilder, obj);
                }
            }
            sql = sqlBuilder.toString();
            parameterMappings = this.parameterMappingsForSelectIn;
        }
        return new BoundSql(configuration, sql, parameterMappings, parameterObject);
    }

	private void trySqlEnhance(Configuration configuration, String sql, List<ParameterMapping> parameterMappings){
		if (parameterMappings != null && parameterMappings.size() > 0) {
            String type = sql.substring(0, sql.indexOf(' ')).toLowerCase();
            if ("select".equals(type)) {
                SqlSegEntry[] sqlCache = cache(sql);
                if (sqlCache != null) {
                    this.sqlCache = sqlCache;
                    final int LEN = parameterMappings.size();
                    HashSet<Integer> indexSet = new HashSet<>();
                    for (SqlSegEntry e : sqlCache) {
                        if (e.index >= 0) {
                            indexSet.add(e.index);
                        }
                    }
                    ArrayList<ParameterMapping> paramMappings = new ArrayList<>(LEN);
                    for (int i = 0; i < LEN; i++) {
                        if (!indexSet.contains(i)) {
                            paramMappings.add(parameterMappings.get(i));
                        }
                    }
                    parameterMappingsForSelectIn = paramMappings;
                }
            } else if ("insert".equals(type)) {
                tryHandleInsertWithOneBeanParameter(sql, configuration.isMapUnderscoreToCamelCase(), parameterMappings);
            }
        }
	}
    /**
     * support insert a whole bean, such as "insert info Users(name,pasw) Values(#{user})"
     *
     * @param sql
     * @param isMapUnderscoreToCamelCase
     * @param parameterMappings
     */
    private void tryHandleInsertWithOneBeanParameter(String sql, boolean isMapUnderscoreToCamelCase, List<ParameterMapping> parameterMappings) {
        int qStart = sql.lastIndexOf('(');
        String suffix = sql.substring(qStart).replaceAll("[\\s]+", "");
        if (!suffix.equals("(?)")) {
            return;
        }
        String insertPrev = sql.substring(0, qStart);
        int columnStart = sql.indexOf('(');
        int columnEnd = sql.indexOf(')', columnStart + 1);
        String[] columns = sql.substring(columnStart + 1, columnEnd).split("[,]");
        List<String> insertColumns;
        if (isMapUnderscoreToCamelCase) {
            insertColumns = new ArrayList<>(columns.length);
            for (String field : columns) {
                String[] _s = field.split("_");
                if (_s.length > 1) {
                    StringBuilder fs = new StringBuilder(field.length());
                    fs.append(_s[0]);
                    for (int i = 1; i < _s.length; i++) {
                        char[] cs = _s[i].toCharArray();
                        fs.append(Character.toUpperCase(cs[0]));
                        fs.append(cs, 1, cs.length - 1);
                    }
                    field = fs.toString();
                }
                insertColumns.add(field);
            }
        } else {
            insertColumns = Arrays.asList(columns);
        }

        ArrayList<ParameterMapping> parameterMappingsForInsert = new ArrayList<>(columns.length);
        String property = parameterMappings.get(0).getProperty();
        String prev = "";
        {
            char c = property.charAt(0);
            boolean isNumber = c >= '0' && c <= '9';
            if (!isNumber) {
                prev = property + '.';
            }
        }

        StringBuilder sb = new StringBuilder(sql.length() + 2 + columns.length * 2);
        sb.append(insertPrev).append('(');
        for (String column : insertColumns) {
            ParameterMapping mapping = new ParameterMapping.Builder(configuration, prev + column, Object.class).build();
            parameterMappingsForInsert.add(mapping);
            sb.append("?,");
        }
        sb.setCharAt(sb.length() - 1, ')');
		// replace the sql to normal: insert info Users(name,pasw) Values(#{user.name}, #{user.pasw})
        this.sql = sb.toString();
		// fix parameterMappings to normal.
        this.parameterMappings = parameterMappingsForInsert;
    }

    private void appendParameter(StringBuilder sb, Object arrObj) {
        Class<?> clazz = arrObj.getClass();
        final char STR = '\'';
        if (clazz.isArray()) {
            sb.append('(');
            int len = Array.getLength(arrObj);
            Class<?> eClass = clazz.getComponentType();
            if (CharSequence.class.isAssignableFrom(eClass)) {
                sb.append(STR).append(Array.get(arrObj, 0)).append(STR);
                for (int i = 1; i < len; i++) {
                    sb.append(',').append(STR).append(Array.get(arrObj, i)).append(STR);
                }
                sb.append(')');
            } else {
                sb.append(Array.get(arrObj, 0));
                for (int i = 1; i < len; i++) {
                    sb.append(',').append(Array.get(arrObj, i));
                }
                sb.append(')');
            }
        } else if (Collection.class.isAssignableFrom(clazz)) {
            sb.append('(');
            Collection<?> col = (Collection<?>) arrObj;
            Iterator<?> i = col.iterator();
            if (i.hasNext()) {
                boolean isString = false;
                Object eObj = i.next();
                isString = eObj instanceof CharSequence;
                if (isString) {
                    sb.append(STR).append(eObj).append(STR);
                    while (i.hasNext()) {
                        sb.append(',').append(STR).append(i.next()).append(STR);
                    }
                } else {
                    sb.append(eObj);
                    while (i.hasNext()) {
                        sb.append(',').append(i.next());
                    }
                }
            }
            sb.append(')');
        }
    }

    private static SqlSegEntry[] cache(String sql) {
        String[] segs = sql.split("[?]");
        int countIn = 0;
        ArrayList<SqlSegEntry> list = new ArrayList<>(segs.length);
        StringBuilder sb = new StringBuilder(sql.length());
        for (int i = 0; i < segs.length; i++) {
            String s = segs[i], ts = s.trim().toLowerCase(), tm = ts;
            if (ts.endsWith("(")) {
                tm = ts.substring(0, ts.length() - 1).trim();
            } else if (ts.length() == 0) {
                continue;
            }
            if (tm.endsWith(" in")) {
                int st = 0;
                if (ts.charAt(0) == ')') {
                    st = s.indexOf(')') + 1;
                }
                if (tm.length() != ts.length()) {
                    s = s.substring(st, s.lastIndexOf('('));
                } else if (st > 0) {
                    s = s.substring(st);
                }
                sb.append(s);
                SqlSegEntry e = new SqlSegEntry();
                e.index = i;
                e.segment = sb.toString();
                list.add(e);
                sb.delete(0, sb.length());
                countIn++;
            } else {
                if (ts.charAt(0) == ')') {
                    s = s.substring(s.indexOf(')') + 1);
                }
                sb.append(s);
                if (i < segs.length - 1) {
                    sb.append('?');
                }
            }
        }
        if (countIn == 0) {
            return null;
        }
        if (sb.length() > 0) {
            SqlSegEntry e = new SqlSegEntry();
            e.segment = sb.toString();
            list.add(e);
        }
        return list.toArray(new SqlSegEntry[list.size()]);
    }

    // --------
    private static class SqlSegEntry {
        String segment;
        int index = -1;
    }
}
