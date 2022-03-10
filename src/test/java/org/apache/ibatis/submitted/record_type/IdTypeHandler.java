package org.apache.ibatis.submitted.record_type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public abstract class IdTypeHandler<INNER, OUTER extends Supplier<INNER>> extends BaseTypeHandler<OUTER> {

    private final Class<INNER> innerClass;
    private final Function<INNER, OUTER> constructor;

    public IdTypeHandler(Class<INNER> innerClass, Function<INNER, OUTER> constructor) {
        this.innerClass = innerClass;
        this.constructor = constructor;
    }

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, OUTER outer, JdbcType jdbcType)
            throws SQLException
    {
        preparedStatement.setObject(i, outer.get());
    }

    @Override
    public OUTER getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        INNER o = resultSet.getObject(columnName, innerClass);
        return resultSet.wasNull() || o == null ? null : constructor.apply(o);
    }

    @Override
    public OUTER getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        INNER o = callableStatement.getObject(i, innerClass);
        return callableStatement.wasNull() || o == null ? null : constructor.apply(o);
    }

    @Override
    public OUTER getNullableResult(ResultSet resultSet, int i) throws SQLException {
        INNER o = resultSet.getObject(i, innerClass);
        return resultSet.wasNull() || o == null ? null : constructor.apply(o);
    }
}
