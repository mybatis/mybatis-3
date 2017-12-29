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

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Month;

import org.apache.ibatis.lang.UsesJava8;

/**
 *
 * @since 3.4.5
 * @author Björn Raupach
 */
@UsesJava8
public class MonthTypeHandler extends BaseTypeHandler<Month> {
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Month month, JdbcType type) throws SQLException {
        ps.setInt(i, month.getValue());
    }

    @Override
    public Month getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int month = rs.getInt(columnName);
        return month == 0 ? null : Month.of(month);
    }

    @Override
    public Month getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int month = rs.getInt(columnIndex);
        return month == 0 ? null : Month.of(month);
    }

    @Override
    public Month getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int month = cs.getInt(columnIndex);
        return month == 0 ? null : Month.of(month);
    }
    
}
