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
package org.apache.ibatis.type;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GenericTypeJsonTypeHandler implements TypeHandler{
	private Type type;
	private static final Gson gson = new Gson();

	public GenericTypeJsonTypeHandler(Type type){
		this.type = type;
	}
	@Override
	public void setParameter(PreparedStatement ps, int i, Object parameter,
			JdbcType jdbcType) throws SQLException {
		ps.setString(i, gson.toJson(parameter));
	}

	@Override
	public Object getResult(ResultSet rs, String columnName) throws SQLException {
		String json = rs.getString(columnName);
		return gson.fromJson(json, type);
	}

	@Override
	public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
		String json = rs.getString(columnIndex);
		return gson.fromJson(json, type);
	}

	@Override
	public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
		String json = cs.getString(columnIndex);
		return gson.fromJson(json, type);
	}
}
