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
package org.apache.ibatis.jdbc;

import org.apache.ibatis.type.*;

/**
 * @author Clinton Begin
 * @author Adam Gent
 */
public enum Null {
    BOOLEAN(new BooleanTypeHandler(), JdbcType.BOOLEAN),

    BYTE(new ByteTypeHandler(), JdbcType.TINYINT),
    SHORT(new ShortTypeHandler(), JdbcType.SMALLINT),
    INTEGER(new IntegerTypeHandler(), JdbcType.INTEGER),
    LONG(new LongTypeHandler(), JdbcType.BIGINT),
    FLOAT(new FloatTypeHandler(), JdbcType.FLOAT),
    DOUBLE(new DoubleTypeHandler(), JdbcType.DOUBLE),
    BIGDECIMAL(new BigDecimalTypeHandler(), JdbcType.DECIMAL),

    STRING(new StringTypeHandler(), JdbcType.VARCHAR),
    CLOB(new ClobTypeHandler(), JdbcType.CLOB),
    LONGVARCHAR(new ClobTypeHandler(), JdbcType.LONGVARCHAR),

    BYTEARRAY(new ByteArrayTypeHandler(), JdbcType.LONGVARBINARY),
    BLOB(new BlobTypeHandler(), JdbcType.BLOB),
    LONGVARBINARY(new BlobTypeHandler(), JdbcType.LONGVARBINARY),

    OBJECT(new ObjectTypeHandler(), JdbcType.OTHER),
    OTHER(new ObjectTypeHandler(), JdbcType.OTHER),
    TIMESTAMP(new DateTypeHandler(), JdbcType.TIMESTAMP),
    DATE(new DateOnlyTypeHandler(), JdbcType.DATE),
    TIME(new TimeOnlyTypeHandler(), JdbcType.TIME),
    SQLTIMESTAMP(new SqlTimestampTypeHandler(), JdbcType.TIMESTAMP),
    SQLDATE(new SqlDateTypeHandler(), JdbcType.DATE),
    SQLTIME(new SqlTimeTypeHandler(), JdbcType.TIME);

    private TypeHandler<?> typeHandler;
    private JdbcType jdbcType;

    private Null(TypeHandler<?> typeHandler, JdbcType jdbcType) {
        this.typeHandler = typeHandler;
        this.jdbcType = jdbcType;
    }

    public TypeHandler<?> getTypeHandler() {
        return typeHandler;
    }

    public JdbcType getJdbcType() {
        return jdbcType;
    }
}
